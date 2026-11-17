package com.example.offsuitpoker.ui.components

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.spring
import androidx.compose.animation.fadeIn
import androidx.compose.animation.slideInVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Slider
import androidx.compose.material3.SliderDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.offsuitpoker.audio.SoundManager
import com.example.offsuitpoker.model.ActionType
import com.example.offsuitpoker.model.PlayerSeat
import com.example.offsuitpoker.model.TableState
import com.example.offsuitpoker.util.CurrencyFormatter
import kotlin.math.max
import kotlin.math.min

@Composable
fun ActionControls(
    localPlayer: PlayerSeat,
    tableState: TableState,
    onAction: (ActionType, Int) -> Unit,
    modifier: Modifier = Modifier
) {
    val neededToCall = tableState.currentHighBet - localPlayer.roundBet
    val canCheck = neededToCall <= 0
    val callAmount = min(neededToCall, localPlayer.chips)
    val potOddsPercent = if (!canCheck && (tableState.totalPot + callAmount) > 0) {
        ((callAmount.toDouble() / (tableState.totalPot + callAmount)) * 100).toInt()
    } else 0

    val haptic = LocalHapticFeedback.current
    val tableMinBet = tableState.bigBlind
    val maxRaise = localPlayer.roundBet + localPlayer.chips

    val minRaise = if (tableState.currentHighBet == 0) {
        min(tableMinBet, maxRaise)
    } else {
        min(tableState.currentHighBet + tableMinBet, maxRaise)
    }

    var selectedRaiseAmount by remember(tableState.currentHighBet, localPlayer.chips) {
        mutableFloatStateOf(min(minRaise, maxRaise).toFloat())
    }

    var isRaiseDrawerOpen by remember { mutableStateOf(false) }

    Column(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 14.dp, vertical = 6.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        // Expandable Raise Drawer (Presets & Slider)
        if (isRaiseDrawerOpen && localPlayer.chips > neededToCall) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(14.dp))
                    .background(Color(0xFF101016))
                    .border(1.dp, Color(0xFF262636), RoundedCornerShape(14.dp))
                    .padding(10.dp)
            ) {
                // Presets Row
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    QuickBetButton(
                        text = "MIN (${CurrencyFormatter.formatIndian(minRaise)})",
                        onClick = {
                            selectedRaiseAmount = min(minRaise, maxRaise).toFloat()
                            SoundManager.playChipBet()
                            haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                        },
                        modifier = Modifier.weight(1f)
                    )

                    val halfPotTarget = localPlayer.roundBet + tableState.totalPot / 2
                    QuickBetButton(
                        text = "½ POT",
                        onClick = {
                            selectedRaiseAmount = halfPotTarget.coerceIn(minRaise, maxRaise).toFloat()
                            SoundManager.playChipBet()
                            haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                        },
                        modifier = Modifier.weight(1f)
                    )

                    val potTarget = localPlayer.roundBet + tableState.totalPot
                    QuickBetButton(
                        text = "POT",
                        onClick = {
                            selectedRaiseAmount = potTarget.coerceIn(minRaise, maxRaise).toFloat()
                            SoundManager.playChipBet()
                            haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                        },
                        modifier = Modifier.weight(1f)
                    )

                    QuickBetButton(
                        text = "ALL-IN",
                        onClick = {
                            selectedRaiseAmount = maxRaise.toFloat()
                            SoundManager.playChipBet()
                            haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                        },
                        isAccent = true,
                        modifier = Modifier.weight(1f)
                    )
                }

                // Slider Row
                if (maxRaise > minRaise) {
                    Spacer(modifier = Modifier.height(6.dp))
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = CurrencyFormatter.formatChips(selectedRaiseAmount.toInt()),
                            color = Color(0xFFD4AF37),
                            fontFamily = FontFamily.Monospace,
                            fontWeight = FontWeight.Black,
                            fontSize = 13.sp,
                            modifier = Modifier.width(76.dp)
                        )
                        Slider(
                            value = selectedRaiseAmount.coerceIn(minRaise.toFloat(), max(minRaise.toFloat(), maxRaise.toFloat())),
                            onValueChange = { rawVal ->
                                val step = tableMinBet.toFloat()
                                val snapped = ((rawVal - minRaise) / step).toInt() * step + minRaise
                                val finalVal = snapped.coerceIn(minRaise.toFloat(), maxRaise.toFloat())
                                if (finalVal != selectedRaiseAmount) {
                                    selectedRaiseAmount = finalVal
                                    SoundManager.playChipBet()
                                    haptic.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                                }
                            },
                            valueRange = minRaise.toFloat()..max(minRaise.toFloat() + 0.01f, maxRaise.toFloat()),
                            steps = 0,
                            colors = SliderDefaults.colors(
                                thumbColor = Color(0xFFD4AF37),
                                activeTrackColor = Color(0xFFD4AF37),
                                inactiveTrackColor = Color(0xFF282836)
                            ),
                            modifier = Modifier.weight(1f)
                        )
                    }
                }
            }
            Spacer(modifier = Modifier.height(8.dp))
        }

        // Clean Action Bar: [Fold] [Check/Call] [Raise 200] [↑]
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Button 0: FOLD
            Button(
                onClick = {
                    SoundManager.playFold()
                    haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                    onAction(ActionType.FOLD, 0)
                },
                colors = ButtonDefaults.buttonColors(
                    containerColor = Color(0xFF1C1014),
                    contentColor = Color(0xFFEF4444)
                ),
                shape = RoundedCornerShape(24.dp),
                border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFF7F1D1D)),
                contentPadding = PaddingValues(horizontal = 8.dp, vertical = 0.dp),
                modifier = Modifier
                    .weight(0.85f)
                    .height(46.dp)
            ) {
                Text(
                    text = "Fold",
                    fontFamily = FontFamily.SansSerif,
                    fontWeight = FontWeight.Bold,
                    fontSize = 14.sp
                )
            }

            // Button 1: CHECK or CALL
            if (canCheck) {
                Button(
                    onClick = {
                        SoundManager.playCheck()
                        haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                        onAction(ActionType.CHECK, 0)
                    },
                    colors = ButtonDefaults.buttonColors(
                        containerColor = Color(0xFF14141E),
                        contentColor = Color(0xFFEDEDED)
                    ),
                    shape = RoundedCornerShape(24.dp),
                    border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFF2E2E3E)),
                    contentPadding = PaddingValues(horizontal = 12.dp, vertical = 0.dp),
                    modifier = Modifier
                        .weight(1f)
                        .height(46.dp)
                ) {
                    Text(
                        text = "Check",
                        fontFamily = FontFamily.SansSerif,
                        fontWeight = FontWeight.Bold,
                        fontSize = 14.5.sp
                    )
                }
            } else {
                Button(
                    onClick = {
                        SoundManager.playChipBet()
                        haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                        onAction(ActionType.CALL, callAmount)
                    },
                    colors = ButtonDefaults.buttonColors(
                        containerColor = Color(0xFF14141E),
                        contentColor = Color(0xFFFFFFFF)
                    ),
                    shape = RoundedCornerShape(24.dp),
                    border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFF38384C)),
                    contentPadding = PaddingValues(horizontal = 8.dp, vertical = 0.dp),
                    modifier = Modifier
                        .weight(1f)
                        .height(46.dp)
                ) {
                    val callStr = CurrencyFormatter.formatIndian(callAmount)
                    Text(
                        text = "Call $callStr",
                        fontFamily = FontFamily.SansSerif,
                        fontWeight = FontWeight.Bold,
                        fontSize = 14.sp
                    )
                }
            }

            // Button 2: RAISE or BET
            val canRaise = localPlayer.chips > neededToCall
            if (canRaise) {
                val isRaiseAllIn = selectedRaiseAmount.toInt() >= maxRaise
                val raiseLabel = when {
                    isRaiseAllIn -> "All-In"
                    canCheck -> "Bet ${CurrencyFormatter.formatIndian(selectedRaiseAmount.toInt())}"
                    else -> "Raise ${CurrencyFormatter.formatIndian(selectedRaiseAmount.toInt())}"
                }
                Button(
                    onClick = {
                        val amt = selectedRaiseAmount.toInt()
                        SoundManager.playChipBet()
                        haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                        onAction(if (canCheck) ActionType.BET else ActionType.RAISE, amt)
                    },
                    colors = ButtonDefaults.buttonColors(
                        containerColor = if (isRaiseAllIn) Color(0xFF7F1D1D) else Color(0xFF14141E),
                        contentColor = if (isRaiseAllIn) Color.White else Color(0xFFEDEDED)
                    ),
                    shape = RoundedCornerShape(24.dp),
                    border = androidx.compose.foundation.BorderStroke(
                        1.dp,
                        if (isRaiseAllIn) Color(0xFFEF4444) else Color(0xFF2E2E3E)
                    ),
                    contentPadding = PaddingValues(horizontal = 8.dp, vertical = 0.dp),
                    modifier = Modifier
                        .weight(1f)
                        .height(46.dp)
                ) {
                    Text(
                        text = raiseLabel,
                        fontFamily = FontFamily.SansSerif,
                        fontWeight = FontWeight.Bold,
                        fontSize = 14.sp
                    )
                }

                // Button 3: [↑] Expand / Adjust Raise toggle
                Button(
                    onClick = { isRaiseDrawerOpen = !isRaiseDrawerOpen },
                    colors = ButtonDefaults.buttonColors(
                        containerColor = if (isRaiseDrawerOpen) Color(0xFF282518) else Color(0xFF14141E),
                        contentColor = if (isRaiseDrawerOpen) Color(0xFFD4AF37) else Color(0xFFA1A1AA)
                    ),
                    shape = RoundedCornerShape(24.dp),
                    border = androidx.compose.foundation.BorderStroke(
                        1.dp,
                        if (isRaiseDrawerOpen) Color(0xFFD4AF37) else Color(0xFF2E2E3E)
                    ),
                    contentPadding = PaddingValues(0.dp),
                    modifier = Modifier
                        .size(width = 46.dp, height = 46.dp)
                ) {
                    Text(
                        text = if (isRaiseDrawerOpen) "↓" else "↑",
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Black
                    )
                }
            }
        }
    }
}

@Composable
private fun QuickBetButton(
    text: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    isAccent: Boolean = false
) {
    Button(
        onClick = onClick,
        colors = ButtonDefaults.buttonColors(
            containerColor = if (isAccent) Color(0xFF3B1216) else Color(0xFF181822),
            contentColor = if (isAccent) Color(0xFFEF4444) else Color(0xFFD4AF37)
        ),
        shape = RoundedCornerShape(8.dp),
        border = androidx.compose.foundation.BorderStroke(
            0.75.dp,
            if (isAccent) Color(0xFF881337) else Color(0xFF2C2C3E)
        ),
        contentPadding = PaddingValues(horizontal = 2.dp, vertical = 0.dp),
        modifier = modifier.height(32.dp)
    ) {
        Text(
            text = text,
            fontFamily = FontFamily.Monospace,
            fontSize = 9.5.sp,
            fontWeight = FontWeight.Bold
        )
    }
}
