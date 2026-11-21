package com.example.offsuitpoker.ui.components

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.FastOutLinearInEasing
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.offsuitpoker.audio.SoundManager
import com.example.offsuitpoker.model.Card
import kotlinx.coroutines.launch
import kotlin.math.roundToInt

@Composable
fun FlickableHoleCards(
    cards: List<Card>,
    isMyTurn: Boolean,
    isFolded: Boolean = false,
    liveHandDescription: String? = null,
    fourColor: Boolean = false,
    canCheck: Boolean = false,
    onFold: () -> Unit,
    modifier: Modifier = Modifier
) {
    val coroutineScope = rememberCoroutineScope()
    val haptic = LocalHapticFeedback.current
    val offsetY = remember { Animatable(0f) }
    val cardAlpha = remember { Animatable(1f) }
    var isFlickingFold by remember { mutableStateOf(false) }

    // When folded externally or cards reset on new hand
    LaunchedEffect(isFolded, cards) {
        if (isFolded && !isFlickingFold && offsetY.value == 0f) {
            // Slide up black and white cards from below!
            offsetY.snapTo(350f)
            offsetY.animateTo(
                targetValue = 0f,
                animationSpec = spring(
                    dampingRatio = Spring.DampingRatioMediumBouncy,
                    stiffness = Spring.StiffnessMediumLow
                )
            )
        } else if (!isFolded) {
            offsetY.snapTo(0f)
            cardAlpha.snapTo(1f)
            isFlickingFold = false
        }
    }

    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = modifier
    ) {
        // Flick up hint
        if (isMyTurn && !isFolded) {
            val isNearFold = offsetY.value < -60f
            Text(
                text = if (isNearFold) "RELEASE TO FOLD" else "↑ FLICK UP TO FOLD",
                color = if (isNearFold) Color(0xFFEF4444) else Color(0xFFD4AF37),
                fontFamily = FontFamily.Monospace,
                fontSize = 8.5.sp,
                fontWeight = FontWeight.Bold,
                letterSpacing = 0.8.sp,
                modifier = Modifier.padding(bottom = 2.dp)
            )
        } else {
            Spacer(modifier = Modifier.height(13.dp))
        }

        Box(
            contentAlignment = Alignment.Center
        ) {
            // Hole Cards Row with Drag Gesture (Strictly 1-Axis Vertical Flick Only)
            Row(
                horizontalArrangement = Arrangement.spacedBy((-20).dp),
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier
                    .offset { IntOffset(0, offsetY.value.roundToInt()) }
                    .graphicsLayer { alpha = cardAlpha.value }
                    .pointerInput(isMyTurn, isFolded) {
                        if (!isMyTurn || isFolded) return@pointerInput
                        detectDragGestures(
                            onDrag = { change, dragAmount ->
                                change.consume()
                                coroutineScope.launch {
                                    // Strictly 1-dimensional (Y-axis only) - cards only flick upwards into the muck
                                    offsetY.snapTo((offsetY.value + dragAmount.y).coerceAtMost(0f))
                                }
                            },
                            onDragEnd = {
                                coroutineScope.launch {
                                    // If flicked upwards significantly (offsetY < -90f)
                                    if (offsetY.value < -90f) {
                                        isFlickingFold = true
                                        SoundManager.playFold()
                                        haptic.performHapticFeedback(HapticFeedbackType.LongPress)

                                        // 1. Throw cards away vertically upward into the muck!
                                        launch { cardAlpha.animateTo(0f, tween(240)) }
                                        offsetY.animateTo(
                                            targetValue = -1200f,
                                            animationSpec = tween(durationMillis = 260, easing = FastOutLinearInEasing)
                                        )

                                        // 2. Dispatch fold to game engine
                                        onFold()

                                        // 3. Prepare black & white cards below screen
                                        offsetY.snapTo(350f)
                                        cardAlpha.snapTo(1f)
                                        isFlickingFold = false

                                        // 4. New black and white cards slide straight up from below!
                                        offsetY.animateTo(
                                            targetValue = 0f,
                                            animationSpec = spring(
                                                dampingRatio = Spring.DampingRatioMediumBouncy,
                                                stiffness = Spring.StiffnessMediumLow
                                            )
                                        )
                                    } else {
                                        launch { offsetY.animateTo(0f, spring(dampingRatio = Spring.DampingRatioMediumBouncy)) }
                                    }
                                }
                            },
                            onDragCancel = {
                                coroutineScope.launch {
                                    offsetY.animateTo(0f, spring())
                                }
                            }
                        )
                    }
            ) {
                CardView(
                    card = cards.getOrNull(0),
                    width = 84.dp,
                    height = 120.dp,
                    faceUp = true,
                    fourColor = fourColor,
                    isFolded = isFolded
                )
                CardView(
                    card = cards.getOrNull(1),
                    width = 84.dp,
                    height = 120.dp,
                    faceUp = true,
                    fourColor = fourColor,
                    isFolded = isFolded
                )
            }
        }
    }
}
