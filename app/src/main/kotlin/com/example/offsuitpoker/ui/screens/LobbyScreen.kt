package com.example.offsuitpoker.ui.screens

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.spring
import androidx.compose.animation.fadeIn
import androidx.compose.animation.scaleIn
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
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
    var showSlangDialog by remember { mutableStateOf(false) }
    val scrollState = rememberScrollState()

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFF08080A))
            .padding(16.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(scrollState),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Spacer(modifier = Modifier.height(16.dp))

            // Modern Luxury Title Row: Perfectly Centered Name with borderless Three-Dot Menu
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 16.dp, bottom = 4.dp),
                contentAlignment = Alignment.Center
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text(
                        text = "RUNNER RUNNER NUTS",
                        color = Color(0xFFEDEDED),
                        fontFamily = FontFamily(android.graphics.Typeface.create("sans-serif-condensed", android.graphics.Typeface.BOLD)),
                        fontSize = 25.sp,
                        fontWeight = FontWeight.Black,
                        letterSpacing = 2.5.sp,
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

                // Sleek, minimal three-dot menu on the same line without circle or border
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

            Spacer(modifier = Modifier.height(20.dp))

            // Player Profile Card (5-Star Lifetime System)
            Card(
                colors = CardDefaults.cardColors(containerColor = Color(0xFF111116)),
                shape = RoundedCornerShape(12.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .border(0.75.dp, Color(0xFF22222C), RoundedCornerShape(12.dp))
            ) {
                Column(
                    modifier = Modifier.padding(18.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(
                        text = "PLAYER IDENTITY",
                        color = Color(0xFF525260),
                        fontFamily = FontFamily.Monospace,
                        fontSize = 9.sp,
                        fontWeight = FontWeight.Bold,
                        letterSpacing = 1.5.sp
                    )

                    Spacer(modifier = Modifier.height(10.dp))

                    // Player Name Field
                    OutlinedTextField(
                        value = nameInput,
                        onValueChange = {
                            nameInput = it
                            onUpdateName(it)
                        },
                        label = { Text("Player Name", fontFamily = FontFamily.Monospace, fontSize = 11.sp) },
                        singleLine = true,
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = Color(0xFFD4AF37),
                            unfocusedBorderColor = Color(0xFF2A2A36),
                            focusedTextColor = Color(0xFFEDEDED),
                            unfocusedTextColor = Color(0xFFEDEDED)
                        ),
                        modifier = Modifier.fillMaxWidth(0.92f)
                    )

                    Spacer(modifier = Modifier.height(14.dp))

                    // Stars Display
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.Center
                    ) {
                        if (profile.starsRemaining > 0) {
                            Text(
                                text = "★".repeat(profile.starsRemaining),
                                color = Color(0xFFD4AF37),
                                fontSize = 20.sp,
                                letterSpacing = 3.sp
                            )
                        } else {
                            Text(
                                text = "ALL STARS USED",
                                color = Color(0xFFEF4444),
                                fontFamily = FontFamily.Monospace,
                                fontSize = 14.sp,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(4.dp))

                    Text(
                        text = "${profile.starsRemaining}/5 Stars Active",
                        color = Color(0xFF71717A),
                        fontFamily = FontFamily.Monospace,
                        fontSize = 11.sp
                    )

                    Spacer(modifier = Modifier.height(14.dp))

                    // Bankroll Metrics
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceAround
                    ) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Text("Current Chips", color = Color(0xFF525260), fontFamily = FontFamily.Monospace, fontSize = 10.sp)
                            Text(
                                CurrencyFormatter.formatChips(profile.currentChips),
                                color = if (profile.currentChips > 0) Color(0xFFFFFFFF) else Color(0xFFEF4444),
                                fontFamily = FontFamily.Monospace,
                                fontSize = 16.sp,
                                fontWeight = FontWeight.Bold
                            )
                        }

                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Text("Lifetime Bought", color = Color(0xFF525260), fontFamily = FontFamily.Monospace, fontSize = 10.sp)
                            Text(
                                "${CurrencyFormatter.formatChips(profile.totalBoughtInAmount)} / ${CurrencyFormatter.formatChips(50000)}",
                                color = Color(0xFFD4AF37),
                                fontFamily = FontFamily.Monospace,
                                fontSize = 14.sp,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }

                    // Initial Buy-in or Rebuy with Star
                    if (profile.currentChips == 0 && profile.starsRemaining > 0) {
                        Spacer(modifier = Modifier.height(14.dp))
                        AnimatedVisibility(
                            visible = true,
                            enter = fadeIn(spring()) + scaleIn()
                        ) {
                            Button(
                                onClick = onBuyInStar,
                                colors = ButtonDefaults.buttonColors(
                                    containerColor = Color(0xFFD4AF37),
                                    contentColor = Color(0xFF0C0C0E)
                                ),
                                shape = RoundedCornerShape(8.dp),
                                modifier = Modifier.fillMaxWidth(0.92f)
                            ) {
                                Text(
                                    "BUY-IN ${CurrencyFormatter.formatChips(10000)} (-1 ★)",
                                    fontFamily = FontFamily.Monospace,
                                    fontWeight = FontWeight.Black,
                                    fontSize = 12.sp,
                                    letterSpacing = 0.5.sp
                                )
                            }
                        }
                    } else if (profile.starsRemaining == 0 && profile.currentChips == 0) {
                        Spacer(modifier = Modifier.height(10.dp))
                        Text(
                            text = "LIFETIME CAP REACHED\n${CurrencyFormatter.formatChips(50000)} limit exhausted.",
                            color = Color(0xFFEF4444),
                            fontFamily = FontFamily.Monospace,
                            fontSize = 10.sp,
                            fontWeight = FontWeight.Bold,
                            textAlign = TextAlign.Center
                        )
                    }

                    Spacer(modifier = Modifier.height(12.dp))

                    // Lifetime System Explanation (In open, right by the Buy-In button, without info icon)
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(8.dp))
                            .background(Color(0xFF0A0A0E))
                            .border(0.75.dp, Color(0xFF1E1E28), RoundedCornerShape(8.dp))
                            .padding(horizontal = 10.dp, vertical = 7.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = "1 Star = ${CurrencyFormatter.formatChips(10000)} • Max 5 Stars (${CurrencyFormatter.formatChips(50000)} total) • Zero real money • 100% Offline LAN",
                            color = Color(0xFF71717A),
                            fontFamily = FontFamily.Monospace,
                            fontSize = 8.5.sp,
                            lineHeight = 12.5.sp,
                            textAlign = TextAlign.Center
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(20.dp))

            // Action 1: Host Local Game (Primary Gold CTA)
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
                    .height(48.dp)
            ) {
                Text(
                    text = "HOST TABLE (LOCAL IP)",
                    fontFamily = FontFamily.Monospace,
                    fontWeight = FontWeight.Black,
                    fontSize = 13.sp,
                    letterSpacing = 1.sp
                )
            }

            Spacer(modifier = Modifier.height(6.dp))
            Text(
                text = "Host IP: $localIpAddress (port 8080)",
                color = Color(0xFF525260),
                fontFamily = FontFamily.Monospace,
                fontSize = 10.sp
            )

            Spacer(modifier = Modifier.height(18.dp))

            // Action 2: Join Local Game (Stealth Dark Container)
            Card(
                colors = CardDefaults.cardColors(containerColor = Color(0xFF111116)),
                shape = RoundedCornerShape(12.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .border(0.75.dp, Color(0xFF22222C), RoundedCornerShape(12.dp))
            ) {
                Column(
                    modifier = Modifier.padding(14.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(
                        text = "JOIN LOCAL TABLE",
                        color = Color(0xFF71717A),
                        fontFamily = FontFamily.Monospace,
                        fontSize = 10.sp,
                        fontWeight = FontWeight.Bold,
                        letterSpacing = 1.sp
                    )

                    Spacer(modifier = Modifier.height(10.dp))

                    // Primary Join: Live Camera QR Scanner
                    Button(
                        onClick = { showScanner = true },
                        enabled = profile.currentChips > 0 || profile.starsRemaining > 0,
                        colors = ButtonDefaults.buttonColors(
                            containerColor = Color(0xFF1E1E28),
                            contentColor = Color(0xFFD4AF37)
                        ),
                        border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFFD4AF37)),
                        shape = RoundedCornerShape(8.dp),
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(44.dp)
                    ) {
                        Text(
                            text = "SCAN HOST QR CODE",
                            fontFamily = FontFamily.Monospace,
                            fontWeight = FontWeight.Black,
                            fontSize = 12.sp,
                            letterSpacing = 1.sp
                        )
                    }

                    Spacer(modifier = Modifier.height(12.dp))

                    Text(
                        text = "— OR ENTER IP MANUALLY —",
                        color = Color(0xFF525260),
                        fontFamily = FontFamily.Monospace,
                        fontSize = 9.sp,
                        fontWeight = FontWeight.Bold,
                        letterSpacing = 1.sp
                    )

                    Spacer(modifier = Modifier.height(8.dp))

                    OutlinedTextField(
                        value = hostIpInput,
                        onValueChange = { hostIpInput = it },
                        label = { Text("Host Local IP", fontFamily = FontFamily.Monospace, fontSize = 11.sp) },
                        singleLine = true,
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = Color(0xFFD4AF37),
                            unfocusedBorderColor = Color(0xFF2A2A36),
                            focusedTextColor = Color(0xFFEDEDED),
                            unfocusedTextColor = Color(0xFFEDEDED)
                        ),
                        modifier = Modifier.fillMaxWidth()
                    )

                    Spacer(modifier = Modifier.height(10.dp))

                    Button(
                        onClick = { onJoinGame(hostIpInput) },
                        enabled = (profile.currentChips > 0 || profile.starsRemaining > 0) && hostIpInput.isNotBlank(),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = Color(0xFF161620),
                            contentColor = Color(0xFFEDEDED)
                        ),
                        border = androidx.compose.foundation.BorderStroke(0.75.dp, Color(0xFF2E2E3C)),
                        shape = RoundedCornerShape(8.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text(
                            "CONNECT WITH IP",
                            fontFamily = FontFamily.Monospace,
                            fontWeight = FontWeight.Bold,
                            fontSize = 12.sp,
                            letterSpacing = 0.5.sp
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Action 3: Solo Offline Practice (Separate Practice Bankroll)
            Column(
                modifier = Modifier.fillMaxWidth(),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Button(
                    onClick = onSoloPractice,
                    colors = ButtonDefaults.buttonColors(
                        containerColor = Color(0xFF14141A),
                        contentColor = Color(0xFFEDEDED)
                    ),
                    border = androidx.compose.foundation.BorderStroke(0.75.dp, Color(0xFF272733)),
                    shape = RoundedCornerShape(10.dp),
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(44.dp)
                ) {
                    Text(
                        text = "SOLO PRACTICE (AI BOTS)",
                        fontFamily = FontFamily.Monospace,
                        fontWeight = FontWeight.SemiBold,
                        fontSize = 12.sp
                    )
                }

                Spacer(modifier = Modifier.height(4.dp))

                Text(
                    text = "Practice Bankroll: ${CurrencyFormatter.formatChips(profile.practiceChips)} • Separate from Local LAN Real Table",
                    color = Color(0xFF71717A),
                    fontFamily = FontFamily.Monospace,
                    fontSize = 8.5.sp,
                    textAlign = TextAlign.Center
                )
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Poker Slang & Origin Card
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(12.dp))
                    .background(Color(0xFF101017))
                    .border(0.75.dp, Color(0xFF2E2A1E), RoundedCornerShape(12.dp))
                    .padding(16.dp)
            ) {
                Column(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(
                        text = "POKER SLANG & THE NAME ORIGIN",
                        color = Color(0xFFD4AF37),
                        fontFamily = FontFamily.Monospace,
                        fontSize = 9.5.sp,
                        fontWeight = FontWeight.Bold,
                        letterSpacing = 1.5.sp
                    )
                    Spacer(modifier = Modifier.height(6.dp))
                    Text(
                        text = "Why 'Runner Runner Nuts'? Catching consecutive turn & river cards to form the unbeatable hand is poker's ultimate mathematical thrill. Explore essential slang terms and the culture.",
                        color = Color(0xFFA1A1AA),
                        fontSize = 11.sp,
                        textAlign = TextAlign.Center,
                        lineHeight = 15.5.sp
                    )
                    Spacer(modifier = Modifier.height(10.dp))
                    Button(
                        onClick = { showSlangDialog = true },
                        colors = ButtonDefaults.buttonColors(
                            containerColor = Color(0xFF1A1710),
                            contentColor = Color(0xFFD4AF37)
                        ),
                        border = androidx.compose.foundation.BorderStroke(0.75.dp, Color(0xFF4A3B14)),
                        shape = RoundedCornerShape(8.dp),
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(38.dp)
                    ) {
                        Text(
                            text = "POKER LEXICON & SLANG (17 TERMS)",
                            fontFamily = FontFamily.Monospace,
                            fontWeight = FontWeight.Bold,
                            fontSize = 11.sp,
                            letterSpacing = 0.5.sp
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Creator Details Card (Kept as the LAST card at the bottom)
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(12.dp))
                    .background(Color(0xFF0F0F14))
                    .border(0.75.dp, Color(0xFF242432), RoundedCornerShape(12.dp))
                    .padding(horizontal = 16.dp, vertical = 14.dp)
            ) {
                Column(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(
                        text = "CREATOR",
                        color = Color(0xFF71717A),
                        fontFamily = FontFamily.Monospace,
                        fontSize = 9.sp,
                        fontWeight = FontWeight.Bold,
                        letterSpacing = 2.sp
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = "Dr. Yash Lohiya",
                        color = Color(0xFFD4AF37),
                        fontFamily = FontFamily.Monospace,
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Black,
                        letterSpacing = 1.sp
                    )
                    Spacer(modifier = Modifier.height(6.dp))
                    Text(
                        text = "\"A probability enthusiast, made this app to make everyone fall in love with the best probability puzzle there is on earth, that is poker.\"",
                        color = Color(0xFFA1A1AA),
                        fontSize = 11.5.sp,
                        textAlign = TextAlign.Center,
                        lineHeight = 16.5.sp
                    )
                }
            }

            Spacer(modifier = Modifier.height(24.dp))
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
