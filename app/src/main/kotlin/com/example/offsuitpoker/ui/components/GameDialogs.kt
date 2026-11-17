package com.example.offsuitpoker.ui.components

import android.graphics.Bitmap
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import com.example.offsuitpoker.data.PlayerProfile
import com.example.offsuitpoker.util.CurrencyFormatter

@Composable
fun PracticeStackDepletedDialog(
    onLeaveTable: () -> Unit,
    onDismiss: () -> Unit
) {
    Dialog(onDismissRequest = onDismiss) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(16.dp))
                .background(Color(0xFF0F0F14))
                .border(1.dp, Color(0xFFD4AF37), RoundedCornerShape(16.dp))
                .padding(22.dp)
        ) {
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Text(
                    text = "PRACTICE STACK DEPLETED",
                    color = Color(0xFFEF4444),
                    fontFamily = FontFamily.Monospace,
                    fontSize = 14.5.sp,
                    fontWeight = FontWeight.Black,
                    letterSpacing = 1.sp
                )

                Spacer(modifier = Modifier.height(10.dp))

                Text(
                    text = "You ran out of practice chips!\nLeave the table and rejoin to start fresh with a new ${CurrencyFormatter.formatChips(10000)} stack.",
                    color = Color(0xFFEDEDED),
                    fontSize = 13.5.sp,
                    textAlign = TextAlign.Center,
                    lineHeight = 19.sp
                )

                Spacer(modifier = Modifier.height(14.dp))

                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(8.dp))
                        .background(Color(0xFF16161D))
                        .border(0.75.dp, Color(0xFF272733), RoundedCornerShape(8.dp))
                        .padding(12.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "Practice chips are separate and free.\nZero lifetime stars are consumed.",
                        color = Color(0xFF71717A),
                        fontFamily = FontFamily.Monospace,
                        fontSize = 11.sp,
                        textAlign = TextAlign.Center
                    )
                }

                Spacer(modifier = Modifier.height(18.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    TextButton(
                        onClick = onDismiss,
                        modifier = Modifier.weight(1f)
                    ) {
                        Text("STAY", color = Color(0xFF71717A), fontFamily = FontFamily.Monospace, fontSize = 12.sp)
                    }

                    Button(
                        onClick = onLeaveTable,
                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFD4AF37), contentColor = Color(0xFF0C0C0E)),
                        shape = RoundedCornerShape(8.dp),
                        modifier = Modifier.weight(1.5f)
                    ) {
                        Text("LEAVE TABLE", fontFamily = FontFamily.Monospace, fontWeight = FontWeight.Black, fontSize = 11.5.sp)
                    }
                }
            }
        }
    }
}

@Composable
fun RebuyDialog(
    profile: PlayerProfile,
    onConfirmRebuy: () -> Unit,
    onDismiss: () -> Unit
) {
    Dialog(onDismissRequest = onDismiss) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(16.dp))
                .background(Color(0xFF0F0F14))
                .border(1.dp, if (profile.starsRemaining > 0) Color(0xFFD4AF37) else Color(0xFFEF4444), RoundedCornerShape(16.dp))
                .padding(22.dp)
        ) {
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                if (profile.starsRemaining > 0) {
                    Text(
                        text = "STACK DEPLETED",
                        color = Color(0xFFEF4444),
                        fontFamily = FontFamily.Monospace,
                        fontSize = 15.sp,
                        fontWeight = FontWeight.Black,
                        letterSpacing = 2.sp
                    )

                    Spacer(modifier = Modifier.height(10.dp))

                    Text(
                        text = "Sacrifice 1 Star for ${CurrencyFormatter.formatChips(10000)} chips?",
                        color = Color(0xFFEDEDED),
                        fontSize = 14.sp,
                        textAlign = TextAlign.Center
                    )

                    Spacer(modifier = Modifier.height(14.dp))

                    // Star transition box
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(8.dp))
                            .background(Color(0xFF16161D))
                            .border(0.75.dp, Color(0xFF272733), RoundedCornerShape(8.dp))
                            .padding(12.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Text(
                                text = "Current Stars: " + "★".repeat(profile.starsRemaining) + " (${profile.starsRemaining}/5)",
                                color = Color(0xFFD4AF37),
                                fontFamily = FontFamily.Monospace,
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Bold
                            )
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(
                                text = "After Buy-in: " + "★".repeat(profile.starsRemaining - 1) + " (${profile.starsRemaining - 1}/5)",
                                color = Color(0xFF71717A),
                                fontFamily = FontFamily.Monospace,
                                fontSize = 11.sp
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(12.dp))

                    Text(
                        text = "LIFETIME RULE:\nFixed allowance of 5 stars (${CurrencyFormatter.formatChips(50000)} total chips) over the entire lifetime of the app.",
                        color = Color(0xFF525260),
                        fontFamily = FontFamily.Monospace,
                        fontSize = 10.sp,
                        textAlign = TextAlign.Center,
                        lineHeight = 15.sp
                    )

                    Spacer(modifier = Modifier.height(18.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        TextButton(
                            onClick = onDismiss,
                            modifier = Modifier.weight(1f)
                        ) {
                            Text("CANCEL", color = Color(0xFF71717A), fontFamily = FontFamily.Monospace, fontSize = 12.sp)
                        }

                        Button(
                            onClick = onConfirmRebuy,
                            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFD4AF37), contentColor = Color(0xFF0C0C0E)),
                            shape = RoundedCornerShape(8.dp),
                            modifier = Modifier.weight(1.5f)
                        ) {
                            Text("REBUY ${CurrencyFormatter.formatChips(10000)}", fontFamily = FontFamily.Monospace, fontWeight = FontWeight.Black, fontSize = 11.sp)
                        }
                    }
                } else {
                    // Out of stars
                    Text(
                        text = "LIFETIME LIMIT REACHED",
                        color = Color(0xFFEF4444),
                        fontFamily = FontFamily.Monospace,
                        fontSize = 15.sp,
                        fontWeight = FontWeight.Black,
                        letterSpacing = 1.sp,
                        textAlign = TextAlign.Center
                    )

                    Spacer(modifier = Modifier.height(12.dp))

                    Text(
                        text = "All 5 stars and ${CurrencyFormatter.formatChips(50000)} chips have been used over your lifetime.\n\nNo further buy-ins are permitted.",
                        color = Color(0xFFA1A1AA),
                        fontSize = 13.sp,
                        textAlign = TextAlign.Center,
                        lineHeight = 18.sp
                    )

                    Spacer(modifier = Modifier.height(18.dp))

                    Button(
                        onClick = onDismiss,
                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF1E1E26)),
                        shape = RoundedCornerShape(8.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text("CLOSE", color = Color(0xFFD4D4D8), fontFamily = FontFamily.Monospace, fontWeight = FontWeight.Bold)
                    }
                }
            }
        }
    }
}

@Composable
fun HostQrDialog(
    hostIp: String,
    qrBitmap: Bitmap?,
    onDismiss: () -> Unit
) {
    Dialog(onDismissRequest = onDismiss) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(16.dp))
                .background(Color(0xFF0F0F14))
                .border(1.dp, Color(0xFFD4AF37), RoundedCornerShape(16.dp))
                .padding(20.dp)
        ) {
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Text(
                    text = "RUNNER RUNNER NUTS",
                    color = Color(0xFFD4AF37),
                    fontFamily = FontFamily.Monospace,
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Black,
                    letterSpacing = 2.sp
                )
                Text(
                    text = "LOCAL LAN HOST",
                    color = Color(0xFF71717A),
                    fontFamily = FontFamily.Monospace,
                    fontSize = 10.sp,
                    letterSpacing = 1.sp
                )

                Spacer(modifier = Modifier.height(14.dp))

                if (qrBitmap != null) {
                    Box(
                        modifier = Modifier
                            .size(190.dp)
                            .clip(RoundedCornerShape(8.dp))
                            .background(Color.White)
                            .padding(8.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Image(
                            bitmap = qrBitmap.asImageBitmap(),
                            contentDescription = "Host IP QR Code",
                            modifier = Modifier.fillMaxWidth()
                        )
                    }
                }

                Spacer(modifier = Modifier.height(14.dp))

                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(8.dp))
                        .background(Color(0xFF16161D))
                        .border(0.75.dp, Color(0xFF272733), RoundedCornerShape(8.dp))
                        .padding(vertical = 8.dp, horizontal = 12.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text(text = "Host Address", color = Color(0xFF71717A), fontFamily = FontFamily.Monospace, fontSize = 9.sp)
                        Text(
                            text = "$hostIp:8080",
                            color = Color(0xFFFFFFFF),
                            fontFamily = FontFamily.Monospace,
                            fontSize = 14.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                Button(
                    onClick = onDismiss,
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFD4AF37), contentColor = Color(0xFF0C0C0E)),
                    shape = RoundedCornerShape(8.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text("DONE", fontFamily = FontFamily.Monospace, fontWeight = FontWeight.Bold)
                }
            }
        }
    }
}
