package com.example.offsuitpoker.ui.components

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
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
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.text.PlatformTextStyle
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.offsuitpoker.model.PlayerSeat
import com.example.offsuitpoker.util.CurrencyFormatter

enum class BadgeType {
    DEALER,
    SMALL_BLIND,
    BIG_BLIND
}

@Composable
fun PositionBadge(
    type: BadgeType,
    modifier: Modifier = Modifier
) {
    val isDealer = type == BadgeType.DEALER
    val text = when (type) {
        BadgeType.DEALER -> "D"
        BadgeType.SMALL_BLIND -> "SB"
        BadgeType.BIG_BLIND -> "BB"
    }
    val isMultiChar = text.length > 1

    Box(
        modifier = modifier
            .size(width = if (isMultiChar) 22.dp else 18.dp, height = 18.dp)
            .clip(CircleShape)
            .background(if (isDealer) Color.White else Color(0xFF0C0C10))
            .border(
                width = 1.2.dp,
                color = if (isDealer) Color.White else Color(0xFFD4AF37),
                shape = CircleShape
            ),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = text,
            color = if (isDealer) Color(0xFF0C0C0E) else Color(0xFFD4AF37),
            fontFamily = FontFamily.SansSerif,
            fontSize = if (isMultiChar) 8.5.sp else 10.5.sp,
            fontWeight = FontWeight.Black,
            textAlign = TextAlign.Center,
            style = TextStyle(
                platformStyle = PlatformTextStyle(includeFontPadding = false),
                lineHeight = if (isMultiChar) 8.5.sp else 10.5.sp
            )
        )
    }
}

/**
 * Clean, modern opponent representation inspired by the reference design:
 * - Circular avatar disc with player initial (no face icons)
 * - Hanging circular dealer / blind badge at the bottom-right of avatar
 * - Name in ALL CAPS
 * - Chips with Indian currency comma formatting
 * - Yellow chip bet capsule floating below the seat
 */
@Composable
fun OpponentSeatView(
    player: PlayerSeat,
    isCurrentTurn: Boolean,
    isDealer: Boolean,
    isSmallBlind: Boolean,
    isBigBlind: Boolean,
    showdownMode: Boolean,
    modifier: Modifier = Modifier,
    fourColor: Boolean = false,
    showFaceDownCards: Boolean = false,
    isWinner: Boolean = false
) {
    val infiniteTransition = rememberInfiniteTransition(label = "turn_pulse")
    val pulseAlpha by infiniteTransition.animateFloat(
        initialValue = 0.35f,
        targetValue = 1.0f,
        animationSpec = infiniteRepeatable(
            animation = tween(900, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "pulse_alpha"
    )
    val winnerPulse by infiniteTransition.animateFloat(
        initialValue = 1.0f,
        targetValue = 1.10f,
        animationSpec = infiniteRepeatable(
            animation = tween(650, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "winner_pulse"
    )

    val scaleFactor by animateFloatAsState(
        targetValue = when {
            isWinner -> winnerPulse
            isCurrentTurn -> 1.05f
            else -> 1.0f
        },
        animationSpec = spring(),
        label = "turn_scale"
    )

    val avatarBorderColor by animateColorAsState(
        targetValue = when {
            isWinner -> Color(0xFFD4AF37)
            isCurrentTurn -> Color(0xFFD4AF37).copy(alpha = pulseAlpha)
            player.folded -> Color(0xFF22222A)
            else -> Color(0xFF2E2E3C)
        },
        label = "border_color"
    )

    val avatarBackground = when {
        isWinner -> Color(0xFF2B2414)
        player.folded -> Color(0xFF101014).copy(alpha = 0.5f)
        isCurrentTurn -> Color(0xFF201D14)
        else -> Color(0xFF161620)
    }

    val initialLetter = player.name.trim().take(1).uppercase().ifEmpty { "?" }
    val showCards = showdownMode && !player.folded && player.cards.isNotEmpty()

    Box(
        modifier = modifier.scale(scaleFactor),
        contentAlignment = Alignment.TopCenter
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier = Modifier.width(60.dp)
        ) {
            // Winner Crown / Badge
            if (isWinner) {
                Text(
                    text = "🏆 WINNER",
                    color = Color(0xFFD4AF37),
                    fontFamily = FontFamily.Monospace,
                    fontSize = 7.5.sp,
                    fontWeight = FontWeight.Black,
                    letterSpacing = 0.5.sp,
                    modifier = Modifier.padding(bottom = 2.dp)
                )
            }
            // Revealed Cards at Showdown only
            if (showCards) {
                Row(
                    horizontalArrangement = Arrangement.spacedBy((-6).dp),
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.padding(bottom = 3.dp)
                ) {
                    CardView(
                        card = player.cards.getOrNull(0),
                        width = 28.dp,
                        height = 40.dp,
                        faceUp = true,
                        fourColor = fourColor
                    )
                    CardView(
                        card = player.cards.getOrNull(1),
                        width = 28.dp,
                        height = 40.dp,
                        faceUp = true,
                        fourColor = fourColor
                    )
                }
            }

            // Avatar Circle with Hanging Dealer / Blind Badge
            Box(
                modifier = Modifier.size(46.dp),
                contentAlignment = Alignment.Center
            ) {
                // Main Avatar Disc (No face emojis, sleek typography)
                Box(
                    modifier = Modifier
                        .size(42.dp)
                        .clip(CircleShape)
                        .background(avatarBackground)
                        .border(
                            width = if (isCurrentTurn) 1.8.dp else 1.dp,
                            color = avatarBorderColor,
                            shape = CircleShape
                        ),
                    contentAlignment = Alignment.Center
                ) {
                    if (player.folded) {
                        Text(
                            text = "FOLD",
                            color = Color(0xFF6B7280),
                            fontFamily = FontFamily.Monospace,
                            fontSize = 8.5.sp,
                            fontWeight = FontWeight.Bold
                        )
                    } else {
                        Text(
                            text = initialLetter,
                            color = if (isCurrentTurn) Color(0xFFD4AF37) else Color(0xFFEDEDED),
                            fontFamily = FontFamily.Monospace,
                            fontSize = 17.sp,
                            fontWeight = FontWeight.Black
                        )

                        // Action Overlay Pill (Matches 'Call' on Jason in reference image)
                        val actionLabel = when {
                            player.isAllIn -> "All-In"
                            player.lastActionText?.startsWith("CALL") == true -> "Call"
                            player.lastActionText?.startsWith("CHECK") == true -> "Check"
                            player.lastActionText?.startsWith("RAISE") == true -> "Raise"
                            player.lastActionText?.startsWith("ALL-IN") == true -> "All-In"
                            else -> null
                        }
                        if (actionLabel != null && !isCurrentTurn) {
                            Box(
                                modifier = Modifier
                                    .fillMaxSize()
                                    .background(Color(0xD90C0C12)),
                                contentAlignment = Alignment.Center
                            ) {
                                Text(
                                    text = actionLabel,
                                    color = Color.White,
                                    fontFamily = FontFamily.SansSerif,
                                    fontSize = 10.5.sp,
                                    fontWeight = FontWeight.Bold
                                )
                            }
                        }
                    }
                }

                // Hanging Circular Position Badge on bottom-right of avatar!
                if (isDealer) {
                    PositionBadge(
                        type = BadgeType.DEALER,
                        modifier = Modifier
                            .align(Alignment.BottomEnd)
                            .offset(x = 2.dp, y = 2.dp)
                    )
                } else if (isSmallBlind) {
                    PositionBadge(
                        type = BadgeType.SMALL_BLIND,
                        modifier = Modifier
                            .align(Alignment.BottomEnd)
                            .offset(x = 2.dp, y = 2.dp)
                    )
                } else if (isBigBlind) {
                    PositionBadge(
                        type = BadgeType.BIG_BLIND,
                        modifier = Modifier
                            .align(Alignment.BottomEnd)
                            .offset(x = 2.dp, y = 2.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.height(3.dp))

            // Player Name in ALL CAPS
            Text(
                text = player.name.uppercase(),
                color = if (player.folded) Color(0xFF555562) else Color(0xFFEDEDED),
                fontFamily = FontFamily.Monospace,
                fontSize = 10.5.sp,
                fontWeight = if (isCurrentTurn) FontWeight.Black else FontWeight.Bold,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
                textAlign = TextAlign.Center
            )

            // Chips formatted with Indian numbering
            Text(
                text = CurrencyFormatter.formatIndian(player.chips),
                color = if (player.folded) Color(0xFF444450) else Color(0xFFA1A1AA),
                fontFamily = FontFamily.Monospace,
                fontSize = 10.sp,
                fontWeight = FontWeight.SemiBold,
                textAlign = TextAlign.Center
            )

            // Bet Capsule: Hanging yellow pill below the seat (matches '100' / '50' in image)
            if (player.roundBet > 0) {
                Spacer(modifier = Modifier.height(4.dp))
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(10.dp))
                        .background(Color(0xFF141410))
                        .border(0.75.dp, Color(0xFFD4AF37).copy(alpha = 0.5f), RoundedCornerShape(10.dp))
                        .padding(horizontal = 7.dp, vertical = 2.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = CurrencyFormatter.formatIndian(player.roundBet),
                        color = Color(0xFFFACC15),
                        fontFamily = FontFamily.Monospace,
                        fontSize = 9.sp,
                        fontWeight = FontWeight.Black
                    )
                }
            }
        }
    }
}

/**
 * Local Player Pod (Bottom-Right card from reference image):
 * - Sides and height same as player cards (104.dp x 104.dp)
 * - Top: Hand evaluation rank (e.g. High Card, Pair of Kings) matching reference image
 * - Center: Avatar circle with initial + hanging dealer/blind badge
 * - Below Avatar: Player name in ALL CAPS
 * - Bottom: Total chips available to bet in Indian format (e.g. 1,900 / 10,000)
 */
@Composable
fun LocalPlayerPod(
    player: PlayerSeat,
    isCurrentTurn: Boolean,
    isDealer: Boolean,
    isSmallBlind: Boolean,
    isBigBlind: Boolean,
    liveHandDescription: String? = null,
    isWinner: Boolean = false,
    modifier: Modifier = Modifier
) {
    val infiniteTransition = rememberInfiniteTransition(label = "local_turn_pulse")
    val pulseAlpha by infiniteTransition.animateFloat(
        initialValue = 0.4f,
        targetValue = 1.0f,
        animationSpec = infiniteRepeatable(
            animation = tween(800, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "local_pulse_alpha"
    )
    val winnerPulse by infiniteTransition.animateFloat(
        initialValue = 1.0f,
        targetValue = 1.05f,
        animationSpec = infiniteRepeatable(
            animation = tween(650, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "local_winner_pulse"
    )

    val borderColor = when {
        isWinner -> Color(0xFFD4AF37)
        isCurrentTurn -> Color(0xFFD4AF37).copy(alpha = pulseAlpha)
        else -> Color(0xFF262634)
    }
    val initialLetter = player.name.trim().take(1).uppercase().ifEmpty { "Y" }

    // Format hand rank in clean Title Case (e.g. "High Card", "Pair of Kings", "Two Pair")
    val displayHandRank = when {
        isWinner -> "🏆 Winner!"
        player.folded -> "Folded"
        !liveHandDescription.isNullOrBlank() -> liveHandDescription
        else -> "High Card"
    }

    Box(
        modifier = modifier
            .graphicsLayer {
                if (isWinner) {
                    scaleX = winnerPulse
                    scaleY = winnerPulse
                }
            }
            .clip(RoundedCornerShape(16.dp))
            .background(if (isWinner) Color(0xFF1C180E) else Color(0xFF0F0F14))
            .border(if (isWinner || isCurrentTurn) 1.8.dp else 1.dp, borderColor, RoundedCornerShape(16.dp))
            .padding(horizontal = 8.dp, vertical = 7.dp),
        contentAlignment = Alignment.Center
    ) {
        Column(
            modifier = Modifier.fillMaxSize(),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.SpaceBetween
        ) {
            // 1. Top: Hand description (e.g. "High Card" matching reference image)
            Text(
                text = displayHandRank,
                color = if (player.folded) Color(0xFF6B7280) else Color(0xFFEDEDED),
                fontFamily = FontFamily.SansSerif,
                fontSize = 11.5.sp,
                fontWeight = FontWeight.SemiBold,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
                textAlign = TextAlign.Center,
                style = TextStyle(platformStyle = PlatformTextStyle(includeFontPadding = false), lineHeight = 13.sp)
            )

            // 2. Center: Avatar Disc with Hanging Position Badge
            Box(
                modifier = Modifier.size(34.dp),
                contentAlignment = Alignment.Center
            ) {
                Box(
                    modifier = Modifier
                        .size(30.dp)
                        .clip(CircleShape)
                        .background(if (isCurrentTurn) Color(0xFF201D14) else Color(0xFF181822))
                        .border(
                            width = if (isCurrentTurn) 1.5.dp else 1.dp,
                            color = if (isCurrentTurn) Color(0xFFD4AF37) else Color(0xFF383848),
                            shape = CircleShape
                        ),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = initialLetter,
                        color = Color.White,
                        fontFamily = FontFamily.Monospace,
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Black,
                        style = TextStyle(platformStyle = PlatformTextStyle(includeFontPadding = false))
                    )
                }

                // Hanging Dealer / Blinds Badge
                if (isDealer) {
                    PositionBadge(
                        type = BadgeType.DEALER,
                        modifier = Modifier
                            .align(Alignment.BottomEnd)
                            .offset(x = 3.dp, y = 3.dp)
                    )
                } else if (isSmallBlind) {
                    PositionBadge(
                        type = BadgeType.SMALL_BLIND,
                        modifier = Modifier
                            .align(Alignment.BottomEnd)
                            .offset(x = 3.dp, y = 3.dp)
                    )
                } else if (isBigBlind) {
                    PositionBadge(
                        type = BadgeType.BIG_BLIND,
                        modifier = Modifier
                            .align(Alignment.BottomEnd)
                            .offset(x = 3.dp, y = 3.dp)
                    )
                }
            }

            // 3. Player Name in ALL CAPS below avatar
            Text(
                text = player.name.uppercase(),
                color = Color(0xFFA1A1AA),
                fontFamily = FontFamily.Monospace,
                fontSize = 8.5.sp,
                fontWeight = FontWeight.Bold,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
                textAlign = TextAlign.Center,
                style = TextStyle(platformStyle = PlatformTextStyle(includeFontPadding = false), lineHeight = 10.sp)
            )

            // 4. Total chips available to bet in Indian format (matches 1,900 in reference image)
            Text(
                text = CurrencyFormatter.formatIndian(player.chips),
                color = Color.White,
                fontFamily = FontFamily.Monospace,
                fontSize = 13.sp,
                fontWeight = FontWeight.Black,
                textAlign = TextAlign.Center,
                style = TextStyle(platformStyle = PlatformTextStyle(includeFontPadding = false), lineHeight = 15.sp)
            )
        }
    }
}
