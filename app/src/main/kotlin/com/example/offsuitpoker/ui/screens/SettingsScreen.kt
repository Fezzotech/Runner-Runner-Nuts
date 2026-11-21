package com.example.offsuitpoker.ui.screens

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Build
import android.widget.Toast
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
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.IconButton
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.foundation.clickable
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.ui.window.Dialog
import com.example.offsuitpoker.audio.SoundManager
import com.example.offsuitpoker.data.PlayerProfile
import com.example.offsuitpoker.model.TableState
import com.example.offsuitpoker.util.AppLogger
import com.example.offsuitpoker.util.CurrencyFormatter

@Composable
fun SettingsScreen(
    profile: PlayerProfile,
    tableState: TableState,
    localIpAddress: String,
    onToggleFourColor: () -> Unit,
    onUpdateCurrency: (symbol: String, useIndianFormat: Boolean) -> Unit = { _, _ -> },
    onBack: () -> Unit,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val haptic = LocalHapticFeedback.current
    val scrollState = rememberScrollState()

    var logListVersion by remember { mutableStateOf(0) }
    val logs = remember(logListVersion) { AppLogger.getLogs() }

    var showCustomCurrencyDialog by remember { mutableStateOf(false) }
    var customSymbolInput by remember(profile.currencySymbol) { mutableStateOf(profile.currencySymbol) }
    var customIndianFormat by remember(profile.useIndianNumbering) { mutableStateOf(profile.useIndianNumbering) }

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(Color(0xFF08080A))
            .padding(horizontal = 16.dp)
            .verticalScroll(scrollState),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Spacer(modifier = Modifier.height(14.dp))

        // Top Navigation Bar (Clean single back arrow, centered title)
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 4.dp),
            contentAlignment = Alignment.Center
        ) {
            IconButton(
                onClick = {
                    haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                    onBack()
                },
                modifier = Modifier
                    .align(Alignment.CenterStart)
                    .size(36.dp)
            ) {
                Text(
                    text = "←",
                    color = Color(0xFFEDEDED),
                    fontSize = 22.sp,
                    fontWeight = FontWeight.Bold
                )
            }

            Text(
                text = "HELP & SETTINGS",
                color = Color(0xFFEDEDED),
                fontFamily = FontFamily.Monospace,
                fontSize = 13.sp,
                fontWeight = FontWeight.Black,
                letterSpacing = 1.sp
            )
        }

        Spacer(modifier = Modifier.height(20.dp))

        // CARD 1: DIAGNOSTICS & EXPORT LOG FILE (PRIMARY GOAL)
        Card(
            colors = CardDefaults.cardColors(containerColor = Color(0xFF0F0F14)),
            shape = RoundedCornerShape(14.dp),
            modifier = Modifier
                .fillMaxWidth()
                .border(1.dp, Color(0xFFD4AF37).copy(alpha = 0.5f), RoundedCornerShape(14.dp))
        ) {
            Column(modifier = Modifier.padding(18.dp)) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Box(
                        modifier = Modifier
                            .size(8.dp)
                            .clip(CircleShape)
                            .background(Color(0xFF22C55E))
                    )
                    Text(
                        text = "SUPPORT & ISSUE LOGGING",
                        color = Color(0xFFD4AF37),
                        fontFamily = FontFamily.Monospace,
                        fontSize = 11.5.sp,
                        fontWeight = FontWeight.Black,
                        letterSpacing = 1.sp
                    )
                }

                Spacer(modifier = Modifier.height(8.dp))

                Text(
                    text = "Encountered a bug, crash, or unexpected game state? Export your diagnostic log file and share it via WhatsApp, Gmail, Drive, or messaging to resolve it quickly.",
                    color = Color(0xFFB0B0C0),
                    fontSize = 12.5.sp,
                    lineHeight = 17.sp
                )

                Spacer(modifier = Modifier.height(14.dp))

                // Log Info Stats Pill
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(8.dp))
                        .background(Color(0xFF16161F))
                        .border(0.75.dp, Color(0xFF262636), RoundedCornerShape(8.dp))
                        .padding(horizontal = 12.dp, vertical = 8.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "Event Entries: ${logs.size}",
                        color = Color(0xFF9E9EB0),
                        fontFamily = FontFamily.Monospace,
                        fontSize = 10.5.sp
                    )
                    Text(
                        text = "Logcat: Active",
                        color = Color(0xFF22C55E),
                        fontFamily = FontFamily.Monospace,
                        fontSize = 10.5.sp,
                        fontWeight = FontWeight.Bold
                    )
                }

                Spacer(modifier = Modifier.height(16.dp))

                // Primary Button: Export Log File (Triggers Android Sharesheet)
                Button(
                    onClick = {
                        SoundManager.playChipBet()
                        haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                        AppLogger.i("SettingsScreen", "User initiated diagnostic log export")
                        AppLogger.exportAndShareLogs(context, profile, tableState)
                    },
                    colors = ButtonDefaults.buttonColors(
                        containerColor = Color(0xFFD4AF37),
                        contentColor = Color(0xFF0C0C0E)
                    ),
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(48.dp)
                ) {
                    Text(
                        text = "EXPORT & SHARE LOG FILE",
                        fontFamily = FontFamily.Monospace,
                        fontWeight = FontWeight.Black,
                        fontSize = 12.5.sp,
                        letterSpacing = 0.5.sp
                    )
                }

                Spacer(modifier = Modifier.height(10.dp))

                // Secondary Actions: Copy to Clipboard & Clear
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Button(
                        onClick = {
                            haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                            AppLogger.copyLogsToClipboard(context, profile, tableState)
                        },
                        colors = ButtonDefaults.buttonColors(
                            containerColor = Color(0xFF181822),
                            contentColor = Color(0xFFEDEDED)
                        ),
                        shape = RoundedCornerShape(8.dp),
                        border = androidx.compose.foundation.BorderStroke(0.75.dp, Color(0xFF2E2E40)),
                        modifier = Modifier
                            .weight(1f)
                            .height(36.dp)
                    ) {
                        Text(
                            text = "COPY TO CLIPBOARD",
                            fontFamily = FontFamily.Monospace,
                            fontSize = 10.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }

                    Button(
                        onClick = {
                            haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                            AppLogger.clearLogs()
                            logListVersion++
                        },
                        colors = ButtonDefaults.buttonColors(
                            containerColor = Color(0xFF181822),
                            contentColor = Color(0xFFEF4444)
                        ),
                        shape = RoundedCornerShape(8.dp),
                        border = androidx.compose.foundation.BorderStroke(0.75.dp, Color(0xFF3F1A20)),
                        modifier = Modifier
                            .weight(0.75f)
                            .height(36.dp)
                    ) {
                        Text(
                            text = "CLEAR",
                            fontFamily = FontFamily.Monospace,
                            fontSize = 10.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(18.dp))

        // CARD 2: SYSTEM & DEVICE SPECIFICATIONS
        Card(
            colors = CardDefaults.cardColors(containerColor = Color(0xFF0F0F14)),
            shape = RoundedCornerShape(14.dp),
            modifier = Modifier
                .fillMaxWidth()
                .border(0.75.dp, Color(0xFF22222E), RoundedCornerShape(14.dp))
        ) {
            Column(modifier = Modifier.padding(18.dp)) {
                Text(
                    text = "DEVICE & SYSTEM METRICS",
                    color = Color(0xFF525260),
                    fontFamily = FontFamily.Monospace,
                    fontSize = 10.sp,
                    fontWeight = FontWeight.Bold,
                    letterSpacing = 1.sp
                )

                Spacer(modifier = Modifier.height(12.dp))

                DiagRow("App", "Runner Runner Nuts v1.0 (RRN)")
                DiagRow("Package", context.packageName)
                DiagRow("Device", "${Build.MANUFACTURER.uppercase()} ${Build.MODEL}")
                DiagRow("Android OS", "Android ${Build.VERSION.RELEASE} (API ${Build.VERSION.SDK_INT})")
                DiagRow("Local IP", "http://$localIpAddress:8080")
                DiagRow("Player", "${profile.playerName} (${profile.playerId.take(8)}...)")
                DiagRow("Stars", "${profile.starsRemaining}/5 active (${CurrencyFormatter.formatChips(profile.currentChips)})")
                DiagRow("Practice Chips", CurrencyFormatter.formatChips(profile.practiceChips))
            }
        }

        Spacer(modifier = Modifier.height(18.dp))

        // CARD 3: GAMEPLAY PREFERENCES
        Card(
            colors = CardDefaults.cardColors(containerColor = Color(0xFF0F0F14)),
            shape = RoundedCornerShape(14.dp),
            modifier = Modifier
                .fillMaxWidth()
                .border(0.75.dp, Color(0xFF22222E), RoundedCornerShape(14.dp))
        ) {
            Column(modifier = Modifier.padding(18.dp)) {
                Text(
                    text = "GAMEPLAY PREFERENCES",
                    color = Color(0xFF525260),
                    fontFamily = FontFamily.Monospace,
                    fontSize = 10.sp,
                    fontWeight = FontWeight.Bold,
                    letterSpacing = 1.sp
                )

                Spacer(modifier = Modifier.height(12.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = "4-Color Deck",
                            color = Color(0xFFEDEDED),
                            fontSize = 13.5.sp,
                            fontWeight = FontWeight.SemiBold
                        )
                        Text(
                            text = "♣ Green, ♦ Blue, ♥ Red, ♠ Black for high clarity",
                            color = Color(0xFF71717A),
                            fontSize = 11.sp
                        )
                    }

                    Switch(
                        checked = profile.fourColorDeckEnabled,
                        onCheckedChange = {
                            haptic.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                            onToggleFourColor()
                        },
                        colors = SwitchDefaults.colors(
                            checkedThumbColor = Color(0xFFD4AF37),
                            checkedTrackColor = Color(0xFF382F12),
                            uncheckedThumbColor = Color(0xFF525260),
                            uncheckedTrackColor = Color(0xFF1B1B24)
                        )
                    )
                }

                Spacer(modifier = Modifier.height(16.dp))
                HorizontalDivider(color = Color(0xFF1C1C28), thickness = 0.75.dp)
                Spacer(modifier = Modifier.height(16.dp))

                // TABLE CURRENCY SELECTION
                Column(modifier = Modifier.fillMaxWidth()) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column {
                            Text(
                                text = "Table Currency",
                                color = Color(0xFFEDEDED),
                                fontSize = 13.5.sp,
                                fontWeight = FontWeight.SemiBold
                            )
                            Text(
                                text = "Sample: ${CurrencyFormatter.formatChips(50000)}",
                                color = Color(0xFFD4AF37),
                                fontFamily = FontFamily.Monospace,
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(12.dp))

                    // 5 Standard Presets
                    val standardPresets = listOf(
                        Triple("₹", "INR", true),
                        Triple("$", "USD", false),
                        Triple("€", "EUR", false),
                        Triple("£", "GBP", false),
                        Triple("🪙", "CHIPS", false)
                    )
                    val isCustom = standardPresets.none { it.first == profile.currencySymbol && it.third == profile.useIndianNumbering }

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        standardPresets.forEach { (symbol, label, isIndian) ->
                            val isSelected = !isCustom && profile.currencySymbol == symbol && profile.useIndianNumbering == isIndian
                            Box(
                                modifier = Modifier
                                    .weight(1f)
                                    .clip(RoundedCornerShape(8.dp))
                                    .background(if (isSelected) Color(0xFFD4AF37) else Color(0xFF161620))
                                    .border(
                                        1.dp,
                                        if (isSelected) Color(0xFFD4AF37) else Color(0xFF262636),
                                        RoundedCornerShape(8.dp)
                                    )
                                    .clickable {
                                        SoundManager.playChipBet()
                                        haptic.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                                        onUpdateCurrency(symbol, isIndian)
                                    }
                                    .padding(vertical = 8.dp),
                                contentAlignment = Alignment.Center
                            ) {
                                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                    Text(
                                        text = symbol,
                                        color = if (isSelected) Color(0xFF0C0C0E) else Color(0xFFEDEDED),
                                        fontSize = 13.sp,
                                        fontWeight = FontWeight.Bold
                                    )
                                    Text(
                                        text = label,
                                        color = if (isSelected) Color(0xFF0C0C0E) else Color(0xFF71717A),
                                        fontSize = 9.sp,
                                        fontFamily = FontFamily.Monospace,
                                        fontWeight = FontWeight.Bold
                                    )
                                }
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(8.dp))

                    // Custom Keyboard Entry Option
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(8.dp))
                            .background(if (isCustom) Color(0xFF231E12) else Color(0xFF161620))
                            .border(
                                1.dp,
                                if (isCustom) Color(0xFFD4AF37) else Color(0xFF262636),
                                RoundedCornerShape(8.dp)
                            )
                            .clickable {
                                SoundManager.playChipBet()
                                haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                                customSymbolInput = profile.currencySymbol
                                customIndianFormat = profile.useIndianNumbering
                                showCustomCurrencyDialog = true
                            }
                            .padding(horizontal = 14.dp, vertical = 10.dp)
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                Text(text = "✏️", fontSize = 13.sp)
                                Column {
                                    Text(
                                        text = if (isCustom) "Custom Currency: \"${profile.currencySymbol}\"" else "Custom Currency Symbol...",
                                        color = if (isCustom) Color(0xFFD4AF37) else Color(0xFFEDEDED),
                                        fontSize = 12.sp,
                                        fontWeight = FontWeight.SemiBold
                                    )
                                    Text(
                                        text = if (isCustom) {
                                            "Formatting: ${if (profile.useIndianNumbering) "Indian (1,00,000)" else "Western (100,000)"}"
                                        } else {
                                            "Type any symbol or tag (¥, ₩, CHF, CAD, PTS...)"
                                        },
                                        color = Color(0xFF71717A),
                                        fontSize = 10.5.sp
                                    )
                                }
                            }

                            Text(
                                text = if (isCustom) "EDIT" else "SET",
                                color = Color(0xFFD4AF37),
                                fontFamily = FontFamily.Monospace,
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(18.dp))

        // CARD 4: OFFLINE LAN & LIFETIME RULES
        Card(
            colors = CardDefaults.cardColors(containerColor = Color(0xFF0F0F14)),
            shape = RoundedCornerShape(14.dp),
            modifier = Modifier
                .fillMaxWidth()
                .border(0.75.dp, Color(0xFF22222E), RoundedCornerShape(14.dp))
        ) {
            Column(modifier = Modifier.padding(18.dp)) {
                Text(
                    text = "HOW TO PLAY • 100% OFFLINE LAN",
                    color = Color(0xFF525260),
                    fontFamily = FontFamily.Monospace,
                    fontSize = 10.sp,
                    fontWeight = FontWeight.Bold,
                    letterSpacing = 1.sp
                )

                Spacer(modifier = Modifier.height(10.dp))

                Text(
                    text = "• Private LAN: Connect all phones to the same Wi-Fi router or one phone's Portable Hotspot (no mobile data or internet needed).\n• Host: Tap 'HOST TABLE' on one device to generate the table IP & QR code.\n• Join: Other players tap 'SCAN QR' or enter the host's IP address.\n• 5 Stars Lifetime System: Fixed allowance of 5 stars (₹50,000 chips). Once all 5 stars are depleted, lifetime limit is reached to simulate real table stakes.\n• Solo Practice: Practice against offline AI bots. Practice chips are free and separate from your real LAN bankroll.",
                    color = Color(0xFF9E9EB0),
                    fontSize = 11.5.sp,
                    lineHeight = 17.sp
                )
            }
        }

        Spacer(modifier = Modifier.height(18.dp))

        // CARD 5: CREATOR & CREDITS
        Card(
            colors = CardDefaults.cardColors(containerColor = Color(0xFF0F0F14)),
            shape = RoundedCornerShape(14.dp),
            modifier = Modifier
                .fillMaxWidth()
                .border(0.75.dp, Color(0xFF22222E), RoundedCornerShape(14.dp))
        ) {
            Column(
                modifier = Modifier.padding(18.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(
                    text = "CREATOR",
                    color = Color(0xFF525260),
                    fontFamily = FontFamily.Monospace,
                    fontSize = 9.5.sp,
                    fontWeight = FontWeight.Bold,
                    letterSpacing = 1.5.sp
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = "Dr. Yash Lohiya",
                    color = Color(0xFFEDEDED),
                    fontFamily = FontFamily.SansSerif,
                    fontSize = 14.5.sp,
                    fontWeight = FontWeight.Bold
                )
                Spacer(modifier = Modifier.height(6.dp))
                Text(
                    text = "\"A probability enthusiast, made this app to make everyone fall in love with the best probability puzzle there is on earth, that is poker.\"",
                    color = Color(0xFF71717A),
                    fontSize = 11.sp,
                    textAlign = TextAlign.Center,
                    lineHeight = 15.sp
                )

                Spacer(modifier = Modifier.height(14.dp))

                // Buy Dev a Coffee (Direct UPI Intent)
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
                    modifier = Modifier.height(38.dp)
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.Center
                    ) {
                        Text("☕", fontSize = 14.sp)
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = "BUY DEV A COFFEE",
                            fontFamily = FontFamily.Monospace,
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Black,
                            letterSpacing = 1.sp
                        )
                    }
                }
            }
        }

        if (showCustomCurrencyDialog) {
            Dialog(onDismissRequest = { showCustomCurrencyDialog = false }) {
                Card(
                    colors = CardDefaults.cardColors(containerColor = Color(0xFF101016)),
                    shape = RoundedCornerShape(16.dp),
                    modifier = Modifier
                        .fillMaxWidth()
                        .border(1.dp, Color(0xFFD4AF37).copy(alpha = 0.6f), RoundedCornerShape(16.dp))
                ) {
                    Column(
                        modifier = Modifier.padding(20.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Text(
                            text = "CUSTOM CURRENCY",
                            color = Color(0xFFD4AF37),
                            fontFamily = FontFamily.Monospace,
                            fontSize = 13.sp,
                            fontWeight = FontWeight.Black,
                            letterSpacing = 1.sp
                        )

                        Spacer(modifier = Modifier.height(6.dp))

                        Text(
                            text = "Enter any symbol or code you want to display on tables, chips, and pots.",
                            color = Color(0xFF9E9EB0),
                            fontSize = 11.5.sp,
                            textAlign = TextAlign.Center
                        )

                        Spacer(modifier = Modifier.height(16.dp))

                        OutlinedTextField(
                            value = customSymbolInput,
                            onValueChange = { if (it.length <= 6) customSymbolInput = it },
                            label = { Text("Currency Symbol / Tag", color = Color(0xFF888899)) },
                            placeholder = { Text("e.g. ¥, ₩, CHF, CAD, PTS", color = Color(0xFF555566)) },
                            singleLine = true,
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedTextColor = Color(0xFFEDEDED),
                                unfocusedTextColor = Color(0xFFEDEDED),
                                focusedBorderColor = Color(0xFFD4AF37),
                                unfocusedBorderColor = Color(0xFF2E2E40),
                                cursorColor = Color(0xFFD4AF37)
                            ),
                            modifier = Modifier.fillMaxWidth()
                        )

                        Spacer(modifier = Modifier.height(14.dp))

                        // Numbering Format Style (Standard vs Indian)
                        Text(
                            text = "NUMBER GROUPING STYLE",
                            color = Color(0xFF525260),
                            fontFamily = FontFamily.Monospace,
                            fontSize = 10.sp,
                            fontWeight = FontWeight.Bold,
                            modifier = Modifier.align(Alignment.Start)
                        )

                        Spacer(modifier = Modifier.height(8.dp))

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            // Western 100,000
                            Box(
                                modifier = Modifier
                                    .weight(1f)
                                    .clip(RoundedCornerShape(8.dp))
                                    .background(if (!customIndianFormat) Color(0xFF231E12) else Color(0xFF161620))
                                    .border(
                                        1.dp,
                                        if (!customIndianFormat) Color(0xFFD4AF37) else Color(0xFF262636),
                                        RoundedCornerShape(8.dp)
                                    )
                                    .clickable { customIndianFormat = false }
                                    .padding(vertical = 10.dp),
                                contentAlignment = Alignment.Center
                            ) {
                                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                    Text(
                                        text = "Standard",
                                        color = if (!customIndianFormat) Color(0xFFD4AF37) else Color(0xFFEDEDED),
                                        fontSize = 12.sp,
                                        fontWeight = FontWeight.Bold
                                    )
                                    Text(
                                        text = "100,000",
                                        color = Color(0xFF71717A),
                                        fontFamily = FontFamily.Monospace,
                                        fontSize = 10.sp
                                    )
                                }
                            }

                            // Indian 1,00,000
                            Box(
                                modifier = Modifier
                                    .weight(1f)
                                    .clip(RoundedCornerShape(8.dp))
                                    .background(if (customIndianFormat) Color(0xFF231E12) else Color(0xFF161620))
                                    .border(
                                        1.dp,
                                        if (customIndianFormat) Color(0xFFD4AF37) else Color(0xFF262636),
                                        RoundedCornerShape(8.dp)
                                    )
                                    .clickable { customIndianFormat = true }
                                    .padding(vertical = 10.dp),
                                contentAlignment = Alignment.Center
                            ) {
                                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                    Text(
                                        text = "Lakh / Crore",
                                        color = if (customIndianFormat) Color(0xFFD4AF37) else Color(0xFFEDEDED),
                                        fontSize = 12.sp,
                                        fontWeight = FontWeight.Bold
                                    )
                                    Text(
                                        text = "1,00,000",
                                        color = Color(0xFF71717A),
                                        fontFamily = FontFamily.Monospace,
                                        fontSize = 10.sp
                                    )
                                }
                            }
                        }

                        Spacer(modifier = Modifier.height(16.dp))

                        // Live Preview Box
                        val sampleAmount = 50000L
                        val previewFormatted = if (customIndianFormat) {
                            CurrencyFormatter.formatIndian(sampleAmount)
                        } else {
                            CurrencyFormatter.formatWestern(sampleAmount)
                        }
                        val previewDisplay = when {
                            customSymbolInput.isEmpty() -> previewFormatted
                            customSymbolInput.length > 2 && customSymbolInput.all { it.isLetter() } -> "$previewFormatted $customSymbolInput"
                            else -> "$customSymbolInput$previewFormatted"
                        }

                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(10.dp))
                                .background(Color(0xFF0B0B10))
                                .border(0.75.dp, Color(0xFFD4AF37).copy(alpha = 0.4f), RoundedCornerShape(10.dp))
                                .padding(horizontal = 14.dp, vertical = 10.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = "PREVIEW: $previewDisplay",
                                color = Color(0xFFD4AF37),
                                fontFamily = FontFamily.Monospace,
                                fontSize = 12.5.sp,
                                fontWeight = FontWeight.Bold
                            )
                        }

                        Spacer(modifier = Modifier.height(18.dp))

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(10.dp)
                        ) {
                            Button(
                                onClick = { showCustomCurrencyDialog = false },
                                colors = ButtonDefaults.buttonColors(
                                    containerColor = Color(0xFF181822),
                                    contentColor = Color(0xFF888899)
                                ),
                                shape = RoundedCornerShape(10.dp),
                                modifier = Modifier
                                    .weight(1f)
                                    .height(44.dp)
                            ) {
                                Text("CANCEL", fontFamily = FontFamily.Monospace, fontSize = 11.5.sp, fontWeight = FontWeight.Bold)
                            }

                            Button(
                                onClick = {
                                    SoundManager.playChipBet()
                                    haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                                    onUpdateCurrency(customSymbolInput.trim(), customIndianFormat)
                                    showCustomCurrencyDialog = false
                                },
                                colors = ButtonDefaults.buttonColors(
                                    containerColor = Color(0xFFD4AF37),
                                    contentColor = Color(0xFF0C0C0E)
                                ),
                                shape = RoundedCornerShape(10.dp),
                                modifier = Modifier
                                    .weight(1f)
                                    .height(44.dp)
                            ) {
                                Text("APPLY", fontFamily = FontFamily.Monospace, fontSize = 11.5.sp, fontWeight = FontWeight.Black)
                            }
                        }
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(28.dp))
    }
}

@Composable
private fun DiagRow(label: String, value: String) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 3.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = label,
            color = Color(0xFF71717A),
            fontFamily = FontFamily.Monospace,
            fontSize = 11.sp
        )
        Text(
            text = value,
            color = Color(0xFFEDEDED),
            fontFamily = FontFamily.Monospace,
            fontSize = 11.sp,
            fontWeight = FontWeight.SemiBold,
            textAlign = TextAlign.End
        )
    }
}
