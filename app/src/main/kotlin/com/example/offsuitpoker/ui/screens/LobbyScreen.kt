package com.example.offsuitpoker.ui.screens

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.widget.Toast
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.spring
import androidx.compose.animation.fadeIn
import androidx.compose.animation.scaleIn
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.offsuitpoker.data.PlayerProfile
import com.example.offsuitpoker.ui.components.PokerSlangDialog
import com.example.offsuitpoker.ui.components.QrScannerDialog
import com.example.offsuitpoker.util.CurrencyFormatter

@Composable
fun LobbyScreen(
    profile: PlayerProfile,
    localIpAddress: String,
    onUpdateName: (String) -> Unit,
    onBuyInStar: () -> Unit,
    onHostGame: () -> Unit,
    onJoinGame: (String) -> Unit,
    onSoloPractice: () -> Unit,
    onOpenSettings: () -> Unit = {}
) {
    var nameInput by remember(profile.playerName) { mutableStateOf(profile.playerName) }
    var hostIpInput by remember { mutableStateOf(localIpAddress) }
    var showScanner by remember { mutableStateOf(false) }
    var showManualIp by remember { mutableStateOf(false) }
    var showSlangDialog by remember { mutableStateOf(false) }
    val scrollState = rememberScrollState()
    val context = LocalContext.current

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFF08080A))
            .padding(horizontal = 16.dp, vertical = 10.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(scrollState),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Spacer(modifier = Modifier.height(10.dp))

            // Modern Luxury Title Row
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 10.dp, bottom = 4.dp),
                contentAlignment = Alignment.Center
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text(
                        text = "RUNNER RUNNER NUTS",
                        color = Color(0xFFEDEDED),
                        fontFamily = FontFamily(android.graphics.Typeface.create("sans-serif-condensed", android.graphics.Typeface.BOLD)),
                        fontSize = 24.sp,
                        fontWeight = FontWeight.Black,
                        letterSpacing = 2.sp,
                        textAlign = TextAlign.Center
                    )
                    Spacer(modifier = Modifier.height(2.dp))
                    Text(
                        text = "TEXAS HOLD'EM • PRIVATE LAN",
                        color = Color(0xFFD4AF37),
                        fontFamily = FontFamily.Monospace,
                        fontSize = 9.sp,
                        fontWeight = FontWeight.Bold,
                        letterSpacing = 2.sp
                    )
                }

                // Three-dot menu button
                IconButton(
                    onClick = onOpenSettings,
                    modifier = Modifier
                        .align(Alignment.CenterEnd)
                        .size(36.dp)
                ) {
                    Text(
                        text = "⋮",
                        color = Color(0xFFD4AF37),
                        fontSize = 24.sp,
                        fontWeight = FontWeight.Bold
                    )
                }
            }

            Spacer(modifier = Modifier.height(14.dp))

            // Tier 1: Modern Compact Player Identity Card
            Card(
                colors = CardDefaults.cardColors(containerColor = Color(0xFF111116)),
                shape = RoundedCornerShape(14.dp),
                border = BorderStroke(0.75.dp, Color(0xFF22222C)),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(
                    modifier = Modifier.padding(14.dp)
                ) {
                    // Profile Row: Avatar + Name TextField + Chips Balance
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        // Avatar Circle
                        Box(
                            modifier = Modifier
                                .size(38.dp)
                                .clip(CircleShape)
                                .background(Color(0xFF1B1B24))
                                .border(1.dp, Color(0xFFD4AF37), CircleShape),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = (profile.playerName.take(1).ifBlank { "P" }).uppercase(),
                                color = Color(0xFFD4AF37),
                                fontFamily = FontFamily.Monospace,
                                fontWeight = FontWeight.Black,
                                fontSize = 16.sp
                            )
                        }

                        Spacer(modifier = Modifier.width(10.dp))

                        // Compact In-place Editable Name
                        OutlinedTextField(
                            value = nameInput,
                            onValueChange = {
                                nameInput = it
                                onUpdateName(it)
                            },
                            placeholder = { Text("Player Name", fontFamily = FontFamily.Monospace, fontSize = 12.sp, color = Color(0xFF6B7280)) },
                            singleLine = true,
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedBorderColor = Color(0xFFD4AF37),
                                unfocusedBorderColor = Color(0xFF262633),
                                focusedTextColor = Color(0xFFEDEDED),
                                unfocusedTextColor = Color(0xFFEDEDED),
                                focusedContainerColor = Color(0xFF0C0C10),
                                unfocusedContainerColor = Color(0xFF0C0C10)
                            ),
                            textStyle = androidx.compose.ui.text.TextStyle(
                                fontFamily = FontFamily.Monospace,
                                fontWeight = FontWeight.Bold,
                                fontSize = 13.sp
                            ),
                            modifier = Modifier
                                .weight(1f)
                                .height(46.dp)
                        )

                        Spacer(modifier = Modifier.width(12.dp))

                        // Chips Count on the right
                        Column(horizontalAlignment = Alignment.End) {
                            Text(
                                text = "CHIPS",
                                color = Color(0xFF525260),
                                fontFamily = FontFamily.Monospace,
                                fontSize = 8.5.sp,
                                fontWeight = FontWeight.Bold
                            )
                            Text(
                                text = CurrencyFormatter.formatChips(profile.currentChips),
                                color = if (profile.currentChips > 0) Color.White else Color(0xFFEF4444),
                                fontFamily = FontFamily.Monospace,
                                fontWeight = FontWeight.Black,
                                fontSize = 15.sp
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(10.dp))

                    // Secondary Row: Stars + Buy-In CTA or Lifetime summary
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        // Stars System
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            if (profile.starsRemaining > 0) {
                                Text(
                                    text = "★".repeat(profile.starsRemaining),
                                    color = Color(0xFFD4AF37),
                                    fontSize = 15.sp,
                                    letterSpacing = 2.sp
                                )
                                Text(
                                    text = "☆".repeat((5 - profile.starsRemaining).coerceAtLeast(0)),
                                    color = Color(0xFF333340),
                                    fontSize = 15.sp,
                                    letterSpacing = 2.sp
                                )
                            } else {
                                Text(
                                    text = "ALL STARS USED",
                                    color = Color(0xFFEF4444),
                                    fontFamily = FontFamily.Monospace,
                                    fontSize = 9.5.sp,
                                    fontWeight = FontWeight.Bold
                                )
                            }
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text = "${profile.starsRemaining}/5",
                                color = Color(0xFF71717A),
                                fontFamily = FontFamily.Monospace,
                                fontSize = 10.sp
                            )
                        }

                        // Buy-In Button (only visible when broke and stars remain)
                        if (profile.currentChips == 0 && profile.starsRemaining > 0) {
                            Button(
                                onClick = onBuyInStar,
                                colors = ButtonDefaults.buttonColors(
                                    containerColor = Color(0xFFD4AF37),
                                    contentColor = Color(0xFF0C0C0E)
                                ),
                                shape = RoundedCornerShape(8.dp),
                                contentPadding = PaddingValues(horizontal = 12.dp, vertical = 2.dp),
                                modifier = Modifier.height(30.dp)
                            ) {
                                Text(
                                    text = "+${CurrencyFormatter.formatChips(10000)} (-1★)",
                                    fontFamily = FontFamily.Monospace,
                                    fontWeight = FontWeight.Black,
                                    fontSize = 10.sp
                                )
                            }
                        } else {
                            Text(
                                text = "Bought: ${CurrencyFormatter.formatChips(profile.totalBoughtInAmount)} / 50K",
                                color = Color(0xFF71717A),
                                fontFamily = FontFamily.Monospace,
                                fontSize = 9.5.sp
                            )
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(14.dp))

            // Tier 2: Clean Game Hub
            // 1. Host Game Card
            Card(
                colors = CardDefaults.cardColors(containerColor = Color(0xFF111116)),
                shape = RoundedCornerShape(14.dp),
                border = BorderStroke(0.75.dp, Color(0xFF22222C)),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(
                    modifier = Modifier.padding(14.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Button(
                        onClick = onHostGame,
                        enabled = profile.currentChips > 0 || profile.starsRemaining > 0,
                        colors = ButtonDefaults.buttonColors(
                            containerColor = Color(0xFFD4AF37),
                            contentColor = Color(0xFF0C0C0E),
                            disabledContainerColor = Color(0xFF1E1E26),
                            disabledContentColor = Color(0xFF525260)
                        ),
                        shape = RoundedCornerShape(10.dp),
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(46.dp)
                    ) {
                        Text(
                            text = "HOST TABLE",
                            fontFamily = FontFamily.Monospace,
                            fontWeight = FontWeight.Black,
                            fontSize = 13.sp,
                            letterSpacing = 1.sp
                        )
                    }

                    Spacer(modifier = Modifier.height(6.dp))

                    Text(
                        text = "Local LAN URL: http://$localIpAddress:8080",
                        color = Color(0xFF71717A),
                        fontFamily = FontFamily.Monospace,
                        fontSize = 9.5.sp
                    )
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            // 2. Join Game Card (QR Hero + Collapsible Manual IP)
            Card(
                colors = CardDefaults.cardColors(containerColor = Color(0xFF111116)),
                shape = RoundedCornerShape(14.dp),
                border = BorderStroke(0.75.dp, Color(0xFF22222C)),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(
                    modifier = Modifier.padding(14.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    // Primary Join CTA: Live Camera QR Scanner
                    Button(
                        onClick = { showScanner = true },
                        enabled = profile.currentChips > 0 || profile.starsRemaining > 0,
                        colors = ButtonDefaults.buttonColors(
                            containerColor = Color(0xFF1B1B26),
                            contentColor = Color(0xFFD4AF37)
                        ),
                        border = BorderStroke(1.dp, Color(0xFFD4AF37)),
                        shape = RoundedCornerShape(10.dp),
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(44.dp)
                    ) {
                        Text(
                            text = "📷 SCAN HOST QR CODE",
                            fontFamily = FontFamily.Monospace,
                            fontWeight = FontWeight.Black,
                            fontSize = 12.sp,
                            letterSpacing = 1.sp
                        )
                    }

                    Spacer(modifier = Modifier.height(6.dp))

                    // Collapsible Manual IP Button
                    Button(
                        onClick = { showManualIp = !showManualIp },
                        colors = ButtonDefaults.buttonColors(
                            containerColor = Color.Transparent,
                            contentColor = Color(0xFF71717A)
                        ),
                        contentPadding = PaddingValues(horizontal = 8.dp, vertical = 2.dp),
                        modifier = Modifier.height(26.dp)
                    ) {
                        Text(
                            text = if (showManualIp) "▲ HIDE MANUAL IP" else "▼ OR ENTER IP MANUALLY",
                            fontFamily = FontFamily.Monospace,
                            fontSize = 9.sp,
                            fontWeight = FontWeight.Bold,
                            letterSpacing = 0.5.sp
                        )
                    }

                    AnimatedVisibility(visible = showManualIp) {
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(top = 6.dp)
                        ) {
                            OutlinedTextField(
                                value = hostIpInput,
                                onValueChange = { hostIpInput = it },
                                label = { Text("Host Local IP", fontFamily = FontFamily.Monospace, fontSize = 10.sp) },
                                singleLine = true,
                                colors = OutlinedTextFieldDefaults.colors(
                                    focusedBorderColor = Color(0xFFD4AF37),
                                    unfocusedBorderColor = Color(0xFF2A2A36),
                                    focusedTextColor = Color(0xFFEDEDED),
                                    unfocusedTextColor = Color(0xFFEDEDED),
                                    focusedContainerColor = Color(0xFF0A0A0E),
                                    unfocusedContainerColor = Color(0xFF0A0A0E)
                                ),
                                textStyle = androidx.compose.ui.text.TextStyle(
                                    fontFamily = FontFamily.Monospace,
                                    fontSize = 12.sp
                                ),
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(48.dp)
                            )

                            Spacer(modifier = Modifier.height(8.dp))

                            Button(
                                onClick = { onJoinGame(hostIpInput) },
                                enabled = (profile.currentChips > 0 || profile.starsRemaining > 0) && hostIpInput.isNotBlank(),
                                colors = ButtonDefaults.buttonColors(
                                    containerColor = Color(0xFF161620),
                                    contentColor = Color(0xFFEDEDED)
                                ),
                                border = BorderStroke(0.75.dp, Color(0xFF2E2E3C)),
                                shape = RoundedCornerShape(8.dp),
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(38.dp)
                            ) {
                                Text(
                                    "CONNECT WITH IP",
                                    fontFamily = FontFamily.Monospace,
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 11.sp
                                )
                            }
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            // 3. Solo Practice Arena (Compact Row)
            Card(
                colors = CardDefaults.cardColors(containerColor = Color(0xFF111116)),
                shape = RoundedCornerShape(14.dp),
                border = BorderStroke(0.75.dp, Color(0xFF22222C)),
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 14.dp, vertical = 10.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = "SOLO PRACTICE ARENA",
                            color = Color(0xFFEDEDED),
                            fontFamily = FontFamily.Monospace,
                            fontWeight = FontWeight.Bold,
                            fontSize = 11.5.sp
                        )
                        Spacer(modifier = Modifier.height(2.dp))
                        Text(
                            text = "Offline AI Bots • ${CurrencyFormatter.formatChips(profile.practiceChips)} Chips",
                            color = Color(0xFF71717A),
                            fontFamily = FontFamily.Monospace,
                            fontSize = 9.sp
                        )
                    }

                    Button(
                        onClick = onSoloPractice,
                        colors = ButtonDefaults.buttonColors(
                            containerColor = Color(0xFF1B1724),
                            contentColor = Color(0xFFD8B4FE)
                        ),
                        border = BorderStroke(0.75.dp, Color(0xFF6B21A8)),
                        shape = RoundedCornerShape(8.dp),
                        contentPadding = PaddingValues(horizontal = 14.dp, vertical = 4.dp),
                        modifier = Modifier.height(34.dp)
                    ) {
                        Text(
                            text = "PLAY",
                            fontFamily = FontFamily.Monospace,
                            fontWeight = FontWeight.Black,
                            fontSize = 11.sp,
                            letterSpacing = 1.sp
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(14.dp))

            // Tier 3: Minimalist Footer
            // Slang Lexicon Pill
            Button(
                onClick = { showSlangDialog = true },
                colors = ButtonDefaults.buttonColors(
                    containerColor = Color(0xFF121218),
                    contentColor = Color(0xFFD4AF37)
                ),
                border = BorderStroke(0.75.dp, Color(0xFF332B18)),
                shape = RoundedCornerShape(10.dp),
                contentPadding = PaddingValues(horizontal = 12.dp, vertical = 6.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .height(38.dp)
            ) {
                Text(
                    text = "📖 POKER LEXICON & SLANG (17 TERMS)",
                    fontFamily = FontFamily.Monospace,
                    fontWeight = FontWeight.Bold,
                    fontSize = 10.5.sp,
                    letterSpacing = 0.5.sp
                )
            }

            Spacer(modifier = Modifier.height(12.dp))

            // Creator Credit & Buy Dev a Coffee
            Card(
                colors = CardDefaults.cardColors(containerColor = Color(0xFF0F0F14)),
                shape = RoundedCornerShape(14.dp),
                border = BorderStroke(0.75.dp, Color(0xFF1E1E28)),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(
                    modifier = Modifier.padding(14.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(
                        text = "CREATED BY DR. YASH LOHIYA",
                        color = Color(0xFFD4AF37),
                        fontFamily = FontFamily.Monospace,
                        fontSize = 10.sp,
                        fontWeight = FontWeight.Black,
                        letterSpacing = 1.5.sp
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = "\"A probability enthusiast, making poker's beautiful math accessible to all.\"",
                        color = Color(0xFF71717A),
                        fontSize = 10.sp,
                        textAlign = TextAlign.Center,
                        lineHeight = 14.sp
                    )
                    Spacer(modifier = Modifier.height(10.dp))
                    Button(
                        onClick = {
                            val upiUri = Uri.parse("upi://pay?pa=yash.lohiya716-1@okicici&pn=Dr%20Yash%20Lohiya&tn=Coffee%20for%20Runner%20Runner%20Nuts&cu=INR")
                            val intent = Intent(Intent.ACTION_VIEW, upiUri)
                            try {
                                val chooser = Intent.createChooser(intent, "Pay via UPI")
                                context.startActivity(chooser)
                            } catch (e: Exception) {
                                val clipboard = context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
                                val clip = ClipData.newPlainText("UPI ID", "yash.lohiya716-1@okicici")
                                clipboard.setPrimaryClip(clip)
                                Toast.makeText(context, "UPI ID copied: yash.lohiya716-1@okicici", Toast.LENGTH_LONG).show()
                            }
                        },
                        colors = ButtonDefaults.buttonColors(
                            containerColor = Color(0xFF1E1A12),
                            contentColor = Color(0xFFD4AF37)
                        ),
                        border = BorderStroke(1.dp, Color(0xFFD4AF37)),
                        shape = RoundedCornerShape(20.dp),
                        contentPadding = PaddingValues(horizontal = 16.dp, vertical = 6.dp),
                        modifier = Modifier.height(36.dp)
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.Center
                        ) {
                            Text("☕", fontSize = 13.sp)
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text = "BUY DEV A COFFEE",
                                fontFamily = FontFamily.Monospace,
                                fontSize = 10.5.sp,
                                fontWeight = FontWeight.Black,
                                letterSpacing = 1.sp
                            )
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(20.dp))
        }

        // Live Camera QR Scanner Modal
        if (showScanner) {
            QrScannerDialog(
                onIpScanned = { scannedIp ->
                    showScanner = false
                    hostIpInput = scannedIp
                    onJoinGame(scannedIp)
                },
                onDismiss = { showScanner = false }
            )
        }

        // Poker Slang & Lexicon Dialog
        if (showSlangDialog) {
            PokerSlangDialog(
                onDismiss = { showSlangDialog = false }
            )
        }
    }
}
