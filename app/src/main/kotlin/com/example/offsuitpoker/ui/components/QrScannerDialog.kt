package com.example.offsuitpoker.ui.components

import android.Manifest
import android.content.Context
import android.content.pm.PackageManager
import android.hardware.Camera
import android.view.SurfaceHolder
import android.view.SurfaceView
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import androidx.core.content.ContextCompat
import com.example.offsuitpoker.audio.SoundManager
import com.google.zxing.BarcodeFormat
import com.google.zxing.BinaryBitmap
import com.google.zxing.DecodeHintType
import com.google.zxing.MultiFormatReader
import com.google.zxing.PlanarYUVLuminanceSource
import com.google.zxing.common.HybridBinarizer
import kotlin.math.roundToInt

@Suppress("DEPRECATION")
class CameraPreviewView(
    context: Context,
    private val onScanned: (String) -> Unit
) : SurfaceView(context), SurfaceHolder.Callback, Camera.PreviewCallback {

    private var camera: Camera? = null
    private var isScanned = false
    private val reader = MultiFormatReader().apply {
        setHints(mapOf(DecodeHintType.POSSIBLE_FORMATS to listOf(BarcodeFormat.QR_CODE)))
    }

    init {
        holder.addCallback(this)
    }

    override fun surfaceCreated(holder: SurfaceHolder) {
        try {
            camera = Camera.open()
            camera?.setDisplayOrientation(90)
            val params = camera?.parameters
            if (params?.supportedFocusModes?.contains(Camera.Parameters.FOCUS_MODE_CONTINUOUS_PICTURE) == true) {
                params.focusMode = Camera.Parameters.FOCUS_MODE_CONTINUOUS_PICTURE
            }
            camera?.parameters = params
            camera?.setPreviewDisplay(holder)
            camera?.setPreviewCallback(this)
            camera?.startPreview()
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    override fun surfaceChanged(holder: SurfaceHolder, format: Int, w: Int, h: Int) {
        if (holder.surface == null) return
        try {
            camera?.stopPreview()
            camera?.setPreviewDisplay(holder)
            camera?.setPreviewCallback(this)
            camera?.startPreview()
        } catch (_: Exception) {}
    }

    override fun surfaceDestroyed(holder: SurfaceHolder) {
        releaseCamera()
    }

    override fun onPreviewFrame(data: ByteArray?, cam: Camera?) {
        if (isScanned || data == null || cam == null) return

        try {
            val size = cam.parameters.previewSize ?: return
            val source = PlanarYUVLuminanceSource(
                data, size.width, size.height, 0, 0, size.width, size.height, false
            )
            val binaryBitmap = BinaryBitmap(HybridBinarizer(source))
            val result = reader.decodeWithState(binaryBitmap)

            val text = result?.text?.trim()
            if (!text.isNullOrBlank()) {
                isScanned = true
                post {
                    onScanned(text)
                }
            }
        } catch (_: Exception) {
            // No barcode found in current frame
        } finally {
            reader.reset()
        }
    }

    fun releaseCamera() {
        try {
            camera?.setPreviewCallback(null)
            camera?.stopPreview()
            camera?.release()
            camera = null
        } catch (_: Exception) {}
    }
}

@Composable
fun QrScannerDialog(
    onIpScanned: (String) -> Unit,
    onDismiss: () -> Unit
) {
    val context = LocalContext.current
    val haptic = LocalHapticFeedback.current

    var hasCameraPermission by remember {
        mutableStateOf(
            ContextCompat.checkSelfPermission(context, Manifest.permission.CAMERA) == PackageManager.PERMISSION_GRANTED
        )
    }

    val permissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission()
    ) { isGranted ->
        hasCameraPermission = isGranted
    }

    LaunchedEffect(Unit) {
        if (!hasCameraPermission) {
            permissionLauncher.launch(Manifest.permission.CAMERA)
        }
    }

    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(usePlatformDefaultWidth = false)
    ) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(Color(0xFF08080A))
        ) {
            if (hasCameraPermission) {
                var previewView: CameraPreviewView? = remember { null }

                DisposableEffect(Unit) {
                    onDispose {
                        previewView?.releaseCamera()
                    }
                }

                AndroidView(
                    factory = { ctx ->
                        CameraPreviewView(ctx) { rawText ->
                            val cleanIp = rawText
                                .removePrefix("ws://")
                                .removePrefix("http://")
                                .substringBefore(":")
                                .trim()

                            if (cleanIp.isNotEmpty()) {
                                haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                                SoundManager.playChipBet()
                                onIpScanned(cleanIp)
                            }
                        }.also { previewView = it }
                    },
                    modifier = Modifier.fillMaxSize()
                )

                // Laser Scan Animation
                val infiniteTransition = rememberInfiniteTransition(label = "laser_scan")
                val scanOffset by infiniteTransition.animateFloat(
                    initialValue = 0f,
                    targetValue = 240f,
                    animationSpec = infiniteRepeatable(
                        animation = tween(1500, easing = FastOutSlowInEasing),
                        repeatMode = RepeatMode.Reverse
                    ),
                    label = "scan_laser_offset"
                )

                // Viewfinder Reticle Overlay
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    Box(
                        modifier = Modifier
                            .size(260.dp)
                            .border(2.dp, Color(0xFFD4AF37), RoundedCornerShape(18.dp))
                    ) {
                        // Glowing laser line
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(2.dp)
                                .offset { IntOffset(0, scanOffset.roundToInt()) }
                                .background(Color(0xFFD4AF37))
                        )
                    }
                }
            } else {
                // Camera Permission Denied state
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(24.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.Center
                    ) {
                        Text(
                            text = "CAMERA PERMISSION REQUIRED",
                            color = Color(0xFFD4AF37),
                            fontFamily = FontFamily.Monospace,
                            fontWeight = FontWeight.Bold,
                            fontSize = 14.sp,
                            textAlign = TextAlign.Center
                        )
                        Spacer(modifier = Modifier.height(10.dp))
                        Text(
                            text = "To scan the host table's QR code, please grant camera permission or enter the IP manually.",
                            color = Color(0xFFA1A1AA),
                            fontSize = 12.sp,
                            textAlign = TextAlign.Center
                        )
                        Spacer(modifier = Modifier.height(16.dp))
                        Button(
                            onClick = { permissionLauncher.launch(Manifest.permission.CAMERA) },
                            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFD4AF37), contentColor = Color(0xFF0C0C0E)),
                            shape = RoundedCornerShape(8.dp)
                        ) {
                            Text("GRANT PERMISSION", fontFamily = FontFamily.Monospace, fontWeight = FontWeight.Black)
                        }
                    }
                }
            }

            // Top Header: Title & Instructions
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .align(Alignment.TopCenter)
                    .background(Color(0xCC08080A))
                    .padding(vertical = 16.dp, horizontal = 20.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(
                    text = "SCAN HOST QR CODE",
                    color = Color(0xFFEDEDED),
                    fontFamily = FontFamily(android.graphics.Typeface.create("sans-serif-condensed", android.graphics.Typeface.BOLD)),
                    fontWeight = FontWeight.Black,
                    fontSize = 20.sp,
                    letterSpacing = 2.sp
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = "Point camera at the host's QR code on their screen",
                    color = Color(0xFF71717A),
                    fontFamily = FontFamily.Monospace,
                    fontSize = 10.sp,
                    textAlign = TextAlign.Center
                )
            }

            // Bottom Bar: Close Button
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .align(Alignment.BottomCenter)
                    .background(Color(0xCC08080A))
                    .padding(vertical = 20.dp, horizontal = 24.dp),
                contentAlignment = Alignment.Center
            ) {
                Button(
                    onClick = onDismiss,
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF16161E), contentColor = Color(0xFFEF4444)),
                    border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFF451A1D)),
                    shape = RoundedCornerShape(10.dp),
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(48.dp)
                ) {
                    Text("CANCEL", fontFamily = FontFamily.Monospace, fontWeight = FontWeight.Bold, fontSize = 13.sp)
                }
            }
        }
    }
}
