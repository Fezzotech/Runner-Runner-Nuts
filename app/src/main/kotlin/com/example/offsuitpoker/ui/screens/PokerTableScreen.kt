package com.example.offsuitpoker.ui.screens

import android.graphics.Bitmap
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateIntAsState
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.scaleIn
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.offsuitpoker.audio.SoundManager
import com.example.offsuitpoker.data.PlayerProfile
import com.example.offsuitpoker.engine.HandEvaluator
import com.example.offsuitpoker.model.ActionType
import com.example.offsuitpoker.model.RoundPhase
import com.example.offsuitpoker.model.TableState
import com.example.offsuitpoker.network.NetworkUtils
import com.example.offsuitpoker.ui.components.ActionControls
import com.example.offsuitpoker.ui.components.AnimatedCommunityCard
import com.example.offsuitpoker.ui.components.FlickableHoleCards
import com.example.offsuitpoker.ui.components.HostQrDialog
import com.example.offsuitpoker.ui.components.LastHandDialog
import com.example.offsuitpoker.ui.components.LocalPlayerPod
import com.example.offsuitpoker.ui.components.OpponentSeatView
import com.example.offsuitpoker.ui.components.PokerSlangDialog
import com.example.offsuitpoker.ui.components.PracticeStackDepletedDialog
import com.example.offsuitpoker.ui.components.RebuyDialog
import com.example.offsuitpoker.util.CurrencyFormatter

@Composable
fun PokerTableScreen(
    profile: PlayerProfile,
    tableState: TableState,
    isHost: Boolean,
    hostIp: String,
    isPracticeMode: Boolean = false,
    onAction: (ActionType, Int) -> Unit,
    onStartHand: () -> Unit,
    onAddAiBot: () -> Unit,
    onToggleFourColor: () -> Unit,
    onToggleEscalatingBlinds: () -> Unit,
    onRabbitHunt: () -> Unit,
    onRebuyStar: () -> Unit,
    onLeaveTable: () -> Unit,
    onOpenSettings: () -> Unit = {}
) {
    var showQrDialog by remember { mutableStateOf(false) }
    var showRebuyDialog by remember { mutableStateOf(false) }
    var showPracticeDepletedDialog by remember { mutableStateOf(false) }
    var showLastHandDialog by remember { mutableStateOf(false) }
    var showSlangDialog by remember { mutableStateOf(false) }
    var qrBitmap by remember { mutableStateOf<Bitmap?>(null) }
    val haptic = LocalHapticFeedback.current

    val localSeat = tableState.seats.firstOrNull { it.id == profile.playerId }
    val isMyTurn = tableState.activeTurnPlayer?.id == profile.playerId &&
            localSeat != null &&
            !localSeat.folded &&
            !localSeat.isAllIn &&
            tableState.phase != RoundPhase.SHOWDOWN &&
            tableState.phase != RoundPhase.WAITING_FOR_PLAYERS

    val neededToCall = if (localSeat != null) tableState.currentHighBet - localSeat.roundBet else 0
    val canCheck = isMyTurn && neededToCall <= 0

    // Fluid Pot Counting Animation
    val animatedPot by animateIntAsState(
        targetValue = tableState.totalPot,
        animationSpec = tween(durationMillis = 500, easing = FastOutSlowInEasing),
        label = "pot_anim"
    )

    // Live Hand Strength Evaluation (works preflop as 'High Card' / 'Pair of X', and postflop)
    val liveHandDescription = remember(localSeat?.cards, tableState.communityCards) {
        if (localSeat != null && localSeat.cards.size == 2) {
            HandEvaluator.evaluateLiveHand(localSeat.cards, tableState.communityCards)
        } else null
    }

    // Trigger Rebuy Dialog when local player busts in LAN game, or Practice Depleted Dialog in Solo Practice
    LaunchedEffect(localSeat?.chips, tableState.phase) {
        if (localSeat != null && localSeat.chips <= 0 && tableState.phase == RoundPhase.SHOWDOWN) {
            if (isPracticeMode) {
                showPracticeDepletedDialog = true
            } else {
                showRebuyDialog = true
            }
        }
    }

    LaunchedEffect(showQrDialog) {
        if (showQrDialog && qrBitmap == null) {
            qrBitmap = NetworkUtils.generateQrCodeBitmap("http://$hostIp:8080")
        }
    }

    // Audio triggers
    LaunchedEffect(isMyTurn) {
        if (isMyTurn) {
            SoundManager.playTurnAlert()
        }
    }

    LaunchedEffect(tableState.communityCards.size) {
        if (tableState.communityCards.isNotEmpty()) {
            SoundManager.playCardDeal()
        }
    }

    LaunchedEffect(tableState.phase) {
        if (tableState.phase == RoundPhase.SHOWDOWN && tableState.winners.isNotEmpty()) {
            SoundManager.playWinFanfare()
        }
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFF000000)) // Pure AMOLED Black like reference
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .statusBarsPadding()
                .navigationBarsPadding()
                .pointerInput(isMyTurn, canCheck) {
                    detectTapGestures(
                        onDoubleTap = {
                            if (isMyTurn && canCheck) {
                                SoundManager.playCheck()
                                haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                                onAction(ActionType.CHECK, 0)
                            }
                        }
                    )
                },
            verticalArrangement = Arrangement.SpaceBetween
        ) {
            // TOP SECTION: Header (Row 1: Centered App Name, Row 2: Scrollable Options & Hand #) + Opponents
            Column(modifier = Modifier.fillMaxWidth()) {
                // ROW 1: Centered App Name
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 8.dp, bottom = 4.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "RUNNER RUNNER NUTS",
                        color = Color(0xFFD4AF37),
                        fontFamily = FontFamily(android.graphics.Typeface.create("sans-serif-condensed", android.graphics.Typeface.BOLD)),
                        fontWeight = FontWeight.Black,
                        fontSize = 17.sp,
                        letterSpacing = 2.sp
                    )
                }

                // ROW 2: Horizontally Scrollable Options & Hand #
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .horizontalScroll(rememberScrollState())
                        .padding(horizontal = 10.dp, vertical = 4.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    // Back / Leave button
                    Button(
                        onClick = onLeaveTable,
                        colors = ButtonDefaults.buttonColors(
                            containerColor = Color(0xFF14141A),
                            contentColor = Color(0xFFEDEDED)
                        ),
                        border = androidx.compose.foundation.BorderStroke(0.75.dp, Color(0xFF2E2E3A)),
                        shape = RoundedCornerShape(12.dp),
                        contentPadding = PaddingValues(horizontal = 10.dp, vertical = 0.dp),
                        modifier = Modifier.height(28.dp)
                    ) {
                        Text(
                            text = "← LEAVE",
                            fontSize = 9.sp,
                            fontFamily = FontFamily.Monospace,
                            fontWeight = FontWeight.Bold,
                            color = Color(0xFFEDEDED)
                        )
                    }

                    // Hand Number Badge
                    Box(
                        modifier = Modifier
                            .height(28.dp)
                            .clip(RoundedCornerShape(12.dp))
                            .background(Color(0xFF181822))
                            .border(0.75.dp, Color(0xFFD4AF37).copy(alpha = 0.6f), RoundedCornerShape(12.dp))
                            .padding(horizontal = 10.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = "HAND #${tableState.handCount}",
                            color = Color(0xFFD4AF37),
                            fontFamily = FontFamily.Monospace,
                            fontSize = 9.5.sp,
                            fontWeight = FontWeight.Black
                        )
                    }

                    // Mode Badge: Real LAN vs Practice Arena
                    if (isPracticeMode) {
                        Box(
                            modifier = Modifier
                                .height(28.dp)
                                .clip(RoundedCornerShape(12.dp))
                                .background(Color(0xFF231530))
                                .border(0.75.dp, Color(0xFFA855F7), RoundedCornerShape(12.dp))
                                .padding(horizontal = 10.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = "PRACTICE ARENA",
                                color = Color(0xFFD8B4FE),
                                fontFamily = FontFamily.Monospace,
                                fontSize = 9.sp,
                                fontWeight = FontWeight.Black
                            )
                        }
                    }

                    // 4-Color Deck Toggle
                    Button(
                        onClick = onToggleFourColor,
                        colors = ButtonDefaults.buttonColors(
                            containerColor = if (profile.fourColorDeckEnabled) Color(0xFF1E293B) else Color(0xFF14141A)
                        ),
                        border = androidx.compose.foundation.BorderStroke(
                            0.75.dp,
                            if (profile.fourColorDeckEnabled) Color(0xFF38BDF8) else Color(0xFF2E2E3A)
                        ),
                        shape = RoundedCornerShape(12.dp),
                        contentPadding = PaddingValues(horizontal = 10.dp, vertical = 0.dp),
                        modifier = Modifier.height(28.dp)
                    ) {
                        Text(
                            text = "4-COLOR DECK",
                            fontSize = 9.sp,
                            fontFamily = FontFamily.Monospace,
                            color = if (profile.fourColorDeckEnabled) Color(0xFF38BDF8) else Color(0xFF9CA3AF),
                            fontWeight = FontWeight.Bold
                        )
                    }

                    // Slang Lexicon
                    Button(
                        onClick = { showSlangDialog = true },
                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF14141A)),
                        border = androidx.compose.foundation.BorderStroke(0.75.dp, Color(0xFF2E2E3A)),
                        shape = RoundedCornerShape(12.dp),
                        contentPadding = PaddingValues(horizontal = 10.dp, vertical = 0.dp),
                        modifier = Modifier.height(28.dp)
                    ) {
                        Text(
                            text = "POKER SLANG",
                            fontSize = 9.sp,
                            fontFamily = FontFamily.Monospace,
                            color = Color(0xFFD4AF37),
                            fontWeight = FontWeight.Bold
                        )
                    }

                    // Last Hand Replayer
                    Button(
                        onClick = { showLastHandDialog = true },
                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF14141A)),
                        border = androidx.compose.foundation.BorderStroke(0.75.dp, Color(0xFF2E2E3A)),
                        shape = RoundedCornerShape(12.dp),
                        contentPadding = PaddingValues(horizontal = 10.dp, vertical = 0.dp),
                        modifier = Modifier.height(28.dp)
                    ) {
                        Text(
                            text = "↺ LAST HAND",
                            fontSize = 9.sp,
                            fontFamily = FontFamily.Monospace,
                            color = Color(0xFFE4E4E7),
                            fontWeight = FontWeight.Bold
                        )
                    }

                    // Help & Diagnostic Logs
                    Button(
                        onClick = onOpenSettings,
                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF14141A)),
                        border = androidx.compose.foundation.BorderStroke(0.75.dp, Color(0xFF2E2E3A)),
                        shape = RoundedCornerShape(12.dp),
                        contentPadding = PaddingValues(horizontal = 10.dp, vertical = 0.dp),
                        modifier = Modifier.height(28.dp)
                    ) {
                        Text(
                            text = "⚙ HELP & LOGS",
                            fontSize = 9.sp,
                            fontFamily = FontFamily.Monospace,
                            color = Color(0xFFE4E4E7),
                            fontWeight = FontWeight.Bold
                        )
                    }

                    if (isHost) {
                        // Escalating Blinds
                        Button(
                            onClick = onToggleEscalatingBlinds,
                            colors = ButtonDefaults.buttonColors(
                                containerColor = if (tableState.escalatingBlinds) Color(0xFF271317) else Color(0xFF14141A)
                            ),
                            border = androidx.compose.foundation.BorderStroke(
                                0.75.dp,
                                if (tableState.escalatingBlinds) Color(0xFF881337) else Color(0xFF2E2E3A)
                            ),
                            shape = RoundedCornerShape(12.dp),
                            contentPadding = PaddingValues(horizontal = 10.dp, vertical = 0.dp),
                            modifier = Modifier.height(28.dp)
                        ) {
                            Text(
                                text = if (tableState.escalatingBlinds) "BLINDS: L${tableState.blindLevel}" else "BLINDS: FIXED",
                                fontSize = 9.sp,
                                fontFamily = FontFamily.Monospace,
                                color = if (tableState.escalatingBlinds) Color(0xFFFCA5A5) else Color(0xFF9CA3AF),
                                fontWeight = FontWeight.Bold
                            )
                        }

                        // Host QR
                        Button(
                            onClick = { showQrDialog = true },
                            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF14141A)),
                            border = androidx.compose.foundation.BorderStroke(0.75.dp, Color(0xFF2E2E3A)),
                            shape = RoundedCornerShape(12.dp),
                            contentPadding = PaddingValues(horizontal = 10.dp, vertical = 0.dp),
                            modifier = Modifier.height(28.dp)
                        ) {
                            Text(
                                text = "HOST QR",
                                fontSize = 9.sp,
                                fontFamily = FontFamily.Monospace,
                                color = Color(0xFFE4E4E7),
                                fontWeight = FontWeight.Bold
                            )
                        }

                        // Add AI Bot
                        if (tableState.seats.size < 6) {
                            Button(
                                onClick = onAddAiBot,
                                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF14141A)),
                                border = androidx.compose.foundation.BorderStroke(0.75.dp, Color(0xFFD4AF37).copy(alpha = 0.5f)),
                                shape = RoundedCornerShape(12.dp),
                                contentPadding = PaddingValues(horizontal = 10.dp, vertical = 0.dp),
                                modifier = Modifier.height(28.dp)
                            ) {
                                Text(
                                    text = "+ ADD BOT",
                                    fontSize = 9.sp,
                                    fontFamily = FontFamily.Monospace,
                                    color = Color(0xFFD4AF37),
                                    fontWeight = FontWeight.Bold
                                )
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(8.dp))

                // Single Horizontal Row of Opponents across the top (Exact match to reference image!)
                val opponents = tableState.seats.filter { it.id != profile.playerId }
                if (opponents.isNotEmpty()) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 8.dp),
                        horizontalArrangement = Arrangement.SpaceEvenly,
                        verticalAlignment = Alignment.Top
                    ) {
                        for (opp in opponents) {
                            val originalIdx = tableState.seats.indexOf(opp)
                            OpponentSeatView(
                                player = opp,
                                isCurrentTurn = tableState.activeTurnIndex == originalIdx,
                                isDealer = tableState.dealerIndex == originalIdx,
                                isSmallBlind = tableState.smallBlindIndex == originalIdx,
                                isBigBlind = tableState.bigBlindIndex == originalIdx,
                                showdownMode = tableState.phase == RoundPhase.SHOWDOWN,
                                fourColor = profile.fourColorDeckEnabled
                            )
                        }
                    }
                } else {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 12.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = "TAP '+BOT' OR SHARE 'QR' FOR PLAYERS TO JOIN",
                            color = Color(0xFF525260),
                            fontFamily = FontFamily.Monospace,
                            fontSize = 9.sp,
                            fontWeight = FontWeight.Bold,
                            letterSpacing = 1.sp
                        )
                    }
                }
            }

            // MIDDLE SECTION: 5 Community Cards with diagonal hatched pattern & Pot number on right
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 12.dp)
            ) {
                // 5 Community Cards (Clean white with diagonal stripes when face down)
                Row(
                    horizontalArrangement = Arrangement.spacedBy(6.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    for (i in 0 until 5) {
                        val card = tableState.communityCards.getOrNull(i)
                        AnimatedCommunityCard(
                            card = card,
                            index = i,
                            width = 62.dp,
                            height = 88.dp,
                            fourColor = profile.fourColorDeckEnabled
                        )
                    }
                }

                Spacer(modifier = Modifier.height(8.dp))

                // Pot Total placed on the right below cards (Matches reference image!)
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 24.dp),
                    horizontalArrangement = Arrangement.End,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = CurrencyFormatter.formatIndian(animatedPot),
                        color = Color.White,
                        fontFamily = FontFamily.Monospace,
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Black
                    )
                }

                // Rabbit Hunt Button when hand completed before River
                if (tableState.phase == RoundPhase.SHOWDOWN && tableState.communityCards.size < 5) {
                    Spacer(modifier = Modifier.height(4.dp))
                    Button(
                        onClick = onRabbitHunt,
                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF14141C)),
                        border = androidx.compose.foundation.BorderStroke(0.75.dp, Color(0xFFD4AF37)),
                        shape = RoundedCornerShape(10.dp),
                        contentPadding = PaddingValues(horizontal = 10.dp, vertical = 2.dp),
                        modifier = Modifier.height(28.dp)
                    ) {
                        Text("RABBIT HUNT RUNOUT", fontSize = 9.sp, fontFamily = FontFamily.Monospace, color = Color(0xFFD4AF37), fontWeight = FontWeight.Bold)
                    }
                }

                // Showdown Winner announcement banner
                if (tableState.phase == RoundPhase.SHOWDOWN && tableState.winners.isNotEmpty()) {
                    Spacer(modifier = Modifier.height(6.dp))
                    AnimatedVisibility(
                        visible = true,
                        enter = scaleIn(spring(Spring.DampingRatioMediumBouncy)) + fadeIn()
                    ) {
                        val groupedByPlayer = tableState.winners.groupBy { it.playerId }
                        val winnerPrefix = if (groupedByPlayer.size > 1) "WINNERS: " else "WINNER: "
                        val winnerText = groupedByPlayer.values.joinToString(" | ") { playerWins ->
                            val first = playerWins.first()
                            val name = first.playerName.uppercase()
                            val hand = first.handDescription.uppercase()
                            if (playerWins.size > 1) {
                                val totalWon = CurrencyFormatter.formatChips(playerWins.sumOf { it.amountWon })
                                val breakdown = playerWins.joinToString(" + ") { win ->
                                    val potName = win.potDescription.ifEmpty { "POT" }.uppercase()
                                    "$potName ${CurrencyFormatter.formatChips(win.amountWon)}"
                                }
                                "$name WON $totalWon ($breakdown • $hand)"
                            } else {
                                val potTag = if (first.potDescription.isNotEmpty() && (tableState.sidePots.isNotEmpty() || tableState.winners.size > 1)) {
                                    " [${first.potDescription.uppercase()}]"
                                } else {
                                    ""
                                }
                                "$name WON ${CurrencyFormatter.formatChips(first.amountWon)}$potTag ($hand)"
                            }
                        }
                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(8.dp))
                                .background(Color(0xFF14141C))
                                .border(1.dp, Color(0xFFD4AF37), RoundedCornerShape(8.dp))
                                .padding(horizontal = 14.dp, vertical = 6.dp)
                        ) {
                            Text(
                                text = "$winnerPrefix$winnerText",
                                color = Color(0xFFEDEDED),
                                fontFamily = FontFamily.Monospace,
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold,
                                textAlign = TextAlign.Center,
                                lineHeight = 15.sp
                            )
                        }
                    }
                }
            }

            // BOTTOM SECTION: Local Bet Pill, Action Buttons, and Bottom Row (Hole Cards + Player Pod)
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .navigationBarsPadding()
            ) {
                // Local Player's Bet Capsule floating on the left (Matches '100' in reference image!)
                if (localSeat != null && localSeat.roundBet > 0) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 22.dp, vertical = 2.dp),
                        horizontalArrangement = Arrangement.Start
                    ) {
                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(10.dp))
                                .background(Color(0xFF141410))
                                .border(0.75.dp, Color(0xFFD4AF37).copy(alpha = 0.6f), RoundedCornerShape(10.dp))
                                .padding(horizontal = 10.dp, vertical = 3.dp)
                        ) {
                            Text(
                                text = CurrencyFormatter.formatIndian(localSeat.roundBet),
                                color = Color(0xFFFACC15),
                                fontFamily = FontFamily.Monospace,
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Black
                            )
                        }
                    }
                }

                // Action Controls or Deal Button
                val isHandCompleteOrWaiting = tableState.phase == RoundPhase.SHOWDOWN ||
                        tableState.phase == RoundPhase.WAITING_FOR_PLAYERS ||
                        tableState.phase == RoundPhase.HAND_COMPLETE

                val canDeal = tableState.seats.filter { it.chips > 0 }.size >= 2

                if (isHandCompleteOrWaiting) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 16.dp, vertical = 6.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        if (isPracticeMode && localSeat != null && localSeat.chips <= 0) {
                            Button(
                                onClick = {
                                    SoundManager.playChipBet()
                                    haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                                    onLeaveTable()
                                },
                                colors = ButtonDefaults.buttonColors(
                                    containerColor = Color(0xFFD4AF37),
                                    contentColor = Color(0xFF0C0C0E)
                                ),
                                shape = RoundedCornerShape(16.dp),
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(50.dp)
                            ) {
                                Text(
                                    text = "STACK DEPLETED • LEAVE & REJOIN",
                                    fontFamily = FontFamily.Monospace,
                                    fontSize = 13.sp,
                                    fontWeight = FontWeight.Black,
                                    letterSpacing = 0.5.sp
                                )
                            }
                        } else if (isHost && canDeal) {
                            Button(
                                onClick = {
                                    SoundManager.playCardDeal()
                                    onStartHand()
                                },
                                colors = ButtonDefaults.buttonColors(
                                    containerColor = Color(0xFFD4AF37),
                                    contentColor = Color(0xFF0C0C0E)
                                ),
                                shape = RoundedCornerShape(16.dp),
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(50.dp)
                            ) {
                                Text(
                                    text = if (tableState.handCount == 0) "START GAME & DEAL CARDS" else "DEAL NEXT HAND #${tableState.handCount + 1}",
                                    fontFamily = FontFamily.Monospace,
                                    fontSize = 13.5.sp,
                                    fontWeight = FontWeight.Black,
                                    letterSpacing = 1.sp
                                )
                            }
                        } else if (isHost) {
                            Text(
                                text = "NEED AT LEAST 2 PLAYERS (TAP '+BOT' TO ADD BOTS)",
                                color = Color(0xFF71717A),
                                fontFamily = FontFamily.Monospace,
                                fontSize = 10.5.sp,
                                fontWeight = FontWeight.Bold,
                                textAlign = TextAlign.Center
                            )
                        } else {
                            Text(
                                text = "WAITING FOR HOST TO DEAL NEXT HAND...",
                                color = Color(0xFFD4AF37),
                                fontFamily = FontFamily.Monospace,
                                fontSize = 11.5.sp,
                                fontWeight = FontWeight.Bold,
                                textAlign = TextAlign.Center
                            )
                        }
                    }
                } else if (isMyTurn && localSeat != null) {
                    // Action Buttons ([Check/Call] [Raise 200] [↑])
                    ActionControls(
                        localPlayer = localSeat,
                        tableState = tableState,
                        onAction = onAction
                    )
                } else {
                    // Waiting Indicator
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 6.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        val turnPlayer = tableState.activeTurnPlayer
                        Text(
                            text = if (turnPlayer != null) "WAITING FOR ${turnPlayer.name.uppercase()} TO ACT..." else "HAND IN PROGRESS...",
                            color = Color(0xFF6B7280),
                            fontFamily = FontFamily.Monospace,
                            fontSize = 10.5.sp,
                            fontWeight = FontWeight.Bold,
                            letterSpacing = 0.5.sp
                        )
                    }
                }

                Spacer(modifier = Modifier.height(4.dp))

                // Bottom Row: Hole Cards on Left + Local Player Pod on Right!
                // Both cards and player box have height = 104.dp and align at the exact same vertical latitude!
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(start = 16.dp, end = 16.dp, top = 2.dp, bottom = 20.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.Top
                ) {
                    // Left: Flickable Hole Cards (cards height = 104.dp; folds into gray translucent blur)
                    if (localSeat != null && localSeat.cards.isNotEmpty()) {
                        FlickableHoleCards(
                            cards = localSeat.cards,
                            isMyTurn = isMyTurn,
                            isFolded = localSeat.folded,
                            liveHandDescription = null, // Hand rank is displayed in the Pod!
                            fourColor = profile.fourColorDeckEnabled,
                            canCheck = canCheck,
                            onFold = { onAction(ActionType.FOLD, 0) }
                        )
                    } else {
                        Spacer(modifier = Modifier.size(width = 104.dp, height = 104.dp))
                    }

                    // Right: Local Player Pod (Sides and height same as cards: 104.dp x 104.dp! Exactly the same latitude!)
                    if (localSeat != null) {
                        val myOriginalIdx = tableState.seats.indexOf(localSeat)
                        LocalPlayerPod(
                            player = localSeat,
                            isCurrentTurn = tableState.activeTurnIndex == myOriginalIdx,
                            isDealer = tableState.dealerIndex == myOriginalIdx,
                            isSmallBlind = tableState.smallBlindIndex == myOriginalIdx,
                            isBigBlind = tableState.bigBlindIndex == myOriginalIdx,
                            liveHandDescription = if (!localSeat.folded) liveHandDescription else "FOLDED",
                            modifier = Modifier.size(104.dp)
                        )
                    }
                }
            }
        }

        // Practice Stack Depleted Dialog (Solo Practice Mode)
        if (showPracticeDepletedDialog) {
            PracticeStackDepletedDialog(
                onLeaveTable = {
                    showPracticeDepletedDialog = false
                    onLeaveTable()
                },
                onDismiss = { showPracticeDepletedDialog = false }
            )
        }

        // Rebuy Dialog (Real LAN game only)
        if (showRebuyDialog && !isPracticeMode) {
            RebuyDialog(
                profile = profile,
                onConfirmRebuy = {
                    onRebuyStar()
                    showRebuyDialog = false
                },
                onDismiss = { showRebuyDialog = false }
            )
        }

        // Host QR / IP Dialog
        if (showQrDialog) {
            HostQrDialog(
                hostIp = hostIp,
                qrBitmap = qrBitmap,
                onDismiss = { showQrDialog = false }
            )
        }

        // Last Hand History Replayer Dialog
        if (showLastHandDialog) {
            LastHandDialog(
                history = tableState.lastHand,
                fourColor = profile.fourColorDeckEnabled,
                onDismiss = { showLastHandDialog = false }
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
