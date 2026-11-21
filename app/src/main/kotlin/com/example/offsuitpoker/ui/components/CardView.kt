package com.example.offsuitpoker.ui.components

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.spring
import androidx.compose.animation.fadeIn
import androidx.compose.animation.scaleIn
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.text.PlatformTextStyle
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.offsuitpoker.audio.SoundManager
import com.example.offsuitpoker.model.Card
import com.example.offsuitpoker.model.Suit

/**
 * Pure vector-drawn playing card suit icon that strictly adheres to the assigned color.
 * Eliminates Android system emoji font interception so 4-color decks (Electric Blue Diamonds,
 * Emerald Green Clubs, Crimson Hearts, Obsidian Spades) render with true 100% color fidelity.
 */
@Composable
fun SuitIcon(
    suit: Suit,
    color: Color,
    modifier: Modifier = Modifier
) {
    Canvas(modifier = modifier) {
        val w = size.width
        val h = size.height

        when (suit) {
            Suit.DIAMONDS -> {
                val path = Path().apply {
                    moveTo(w * 0.5f, 0f)
                    lineTo(w * 0.95f, h * 0.5f)
                    lineTo(w * 0.5f, h)
                    lineTo(w * 0.05f, h * 0.5f)
                    close()
                }
                drawPath(path, color)
            }

            Suit.HEARTS -> {
                val path = Path().apply {
                    moveTo(w * 0.5f, h * 0.92f)
                    cubicTo(w * 0.12f, h * 0.62f, 0f, h * 0.38f, 0f, h * 0.24f)
                    cubicTo(0f, h * 0.06f, w * 0.20f, 0f, w * 0.38f, 0f)
                    cubicTo(w * 0.46f, 0f, w * 0.5f, h * 0.10f, w * 0.5f, h * 0.16f)
                    cubicTo(w * 0.5f, h * 0.10f, w * 0.54f, 0f, w * 0.62f, 0f)
                    cubicTo(w * 0.80f, 0f, w, h * 0.06f, w, h * 0.24f)
                    cubicTo(w, h * 0.38f, w * 0.88f, h * 0.62f, w * 0.5f, h * 0.92f)
                    close()
                }
                drawPath(path, color)
            }

            Suit.SPADES -> {
                val path = Path().apply {
                    moveTo(w * 0.5f, 0f)
                    cubicTo(w * 0.12f, h * 0.34f, 0f, h * 0.56f, 0f, h * 0.70f)
                    cubicTo(0f, h * 0.85f, w * 0.22f, h * 0.86f, w * 0.42f, h * 0.75f)
                    lineTo(w * 0.38f, h)
                    lineTo(w * 0.62f, h)
                    lineTo(w * 0.58f, h * 0.75f)
                    cubicTo(w * 0.78f, h * 0.86f, w, h * 0.85f, w, h * 0.70f)
                    cubicTo(w, h * 0.56f, w * 0.88f, h * 0.34f, w * 0.5f, 0f)
                    close()
                }
                drawPath(path, color)
            }

            Suit.CLUBS -> {
                val r = w * 0.23f
                val cx = w * 0.5f
                val cy = h * 0.44f

                // Three lobes of the clover
                drawCircle(color, radius = r, center = Offset(cx, cy - r * 0.82f))
                drawCircle(color, radius = r, center = Offset(cx - r * 0.84f, cy + r * 0.32f))
                drawCircle(color, radius = r, center = Offset(cx + r * 0.84f, cy + r * 0.32f))
                // Central fill
                drawCircle(color, radius = r * 0.8f, center = Offset(cx, cy + r * 0.1f))

                // Stem
                val stem = Path().apply {
                    moveTo(cx - r * 0.20f, cy + r * 0.2f)
                    lineTo(cx - r * 0.45f, h)
                    lineTo(cx + r * 0.45f, h)
                    lineTo(cx + r * 0.20f, cy + r * 0.2f)
                    close()
                }
                drawPath(stem, color)
            }
        }
    }
}

@Composable
fun CardFace(
    card: Card,
    modifier: Modifier = Modifier,
    width: Dp = 54.dp,
    height: Dp = 76.dp,
    fourColor: Boolean = false,
    isFolded: Boolean = false
) {
    val isMini = width < 45.dp
    val cornerRadius = if (isMini) 5.dp else if (width >= 65.dp) 16.dp else 12.dp
    val shape = RoundedCornerShape(cornerRadius)
    val cardBg = if (isFolded) Color(0xFF14141C) else Color(0xFFFCFCFD)
    val cardBorder = if (isFolded) Color(0xFF383848) else Color(0xFFCBD5E1)

    val suitColor = if (isFolded) {
        Color(0xFFEEEEF0)
    } else if (fourColor) {
        when (card.suit) {
            Suit.HEARTS -> Color(0xFFDC2626) // Crimson Red
            Suit.DIAMONDS -> Color(0xFF2563EB) // Electric Blue
            Suit.CLUBS -> Color(0xFF16A34A) // Emerald Green
            Suit.SPADES -> Color(0xFF18181B) // Obsidian Black
        }
    } else {
        val isRedSuit = card.suit == Suit.HEARTS || card.suit == Suit.DIAMONDS
        if (isRedSuit) Color(0xFFDC2626) else Color(0xFF18181B)
    }

    if (isMini) {
        // MINI CARD (Opponent Showdown & Replay):
        // Clean vertical stack: Rank on top, Vector Suit below.
        Box(
            modifier = modifier
                .size(width, height)
                .shadow(3.dp, shape)
                .clip(shape)
                .background(cardBg)
                .border(0.8.dp, cardBorder, shape)
                .padding(horizontal = 2.dp, vertical = 2.dp),
            contentAlignment = Alignment.Center
        ) {
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center
            ) {
                Text(
                    text = card.rank.display,
                    color = suitColor,
                    fontFamily = FontFamily.Monospace,
                    fontSize = (width.value * 0.38f).sp,
                    fontWeight = FontWeight.Black,
                    style = TextStyle(
                        platformStyle = PlatformTextStyle(includeFontPadding = false),
                        lineHeight = (width.value * 0.40f).sp
                    )
                )
                Spacer(modifier = Modifier.height(2.dp))
                // Vector suit icon rendered in 100% exact suitColor!
                SuitIcon(
                    suit = card.suit,
                    color = suitColor,
                    modifier = Modifier.size((width.value * 0.38f).dp)
                )
            }
        }
    } else {
        // FULL CARD (Community Cards & Hole Cards):
        // Modern, clean, and legible with larger rank typography and subtle warm ambient glow
        val rankFontSize = if (card.rank.display.length > 1) (width.value * 0.40f).sp else (width.value * 0.46f).sp
        val suitIconSize = (width.value * 0.35f).dp

        Box(
            modifier = modifier
                .size(width, height)
                .shadow(
                    elevation = if (isFolded) 2.dp else 8.dp,
                    shape = shape,
                    spotColor = if (!isFolded) Color(0x40D4AF37) else Color(0x40000000),
                    ambientColor = Color(0x33000000)
                )
                .clip(shape)
                .background(cardBg)
                .border(
                    width = if (isFolded) 0.8.dp else 1.dp,
                    color = if (isFolded) cardBorder else Color(0xFFE2E8F0),
                    shape = shape
                )
                .padding(horizontal = (width.value * 0.10f).dp, vertical = (height.value * 0.07f).dp),
            contentAlignment = Alignment.TopStart
        ) {
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(1.dp)
            ) {
                Text(
                    text = card.rank.display,
                    color = suitColor,
                    fontFamily = FontFamily.SansSerif,
                    fontSize = rankFontSize,
                    fontWeight = FontWeight.Black,
                    style = TextStyle(platformStyle = PlatformTextStyle(includeFontPadding = false))
                )

                SuitIcon(
                    suit = card.suit,
                    color = suitColor,
                    modifier = Modifier.size(suitIconSize)
                )
            }
        }
    }
}

@Composable
fun CardBack(
    modifier: Modifier = Modifier,
    width: Dp = 54.dp,
    height: Dp = 76.dp
) {
    val isMini = width < 45.dp
    val cornerRadius = if (isMini) 6.dp else if (width >= 60.dp) 14.dp else 10.dp
    val shape = RoundedCornerShape(cornerRadius)
    Box(
        modifier = modifier
            .size(width, height)
            .shadow(
                elevation = if (isMini) 3.dp else 6.dp,
                shape = shape,
                spotColor = Color(0x33D4AF37),
                ambientColor = Color(0x33000000)
            )
            .clip(shape)
            .background(Color(0xFFFAFAFA))
            .border(1.dp, Color(0xFFE2E8F0), shape),
        contentAlignment = Alignment.Center
    ) {
        androidx.compose.foundation.Canvas(
            modifier = Modifier
                .fillMaxSize()
                .padding(if (isMini) 3.dp else 5.dp)
        ) {
            val step = if (isMini) 5.dp.toPx() else 7.dp.toPx()
            val stroke = if (isMini) 1.5.dp.toPx() else 2.2.dp.toPx()
            var x = -size.height
            while (x < size.width + size.height) {
                drawLine(
                    color = Color(0xFF1E293B),
                    start = androidx.compose.ui.geometry.Offset(x, size.height),
                    end = androidx.compose.ui.geometry.Offset(x + size.height, 0f),
                    strokeWidth = stroke
                )
                x += step
            }
        }
    }
}

@Composable
fun CardView(
    card: Card?,
    modifier: Modifier = Modifier,
    width: Dp = 54.dp,
    height: Dp = 76.dp,
    faceUp: Boolean = true,
    fourColor: Boolean = false,
    isFolded: Boolean = false
) {
    AnimatedVisibility(
        visible = true,
        enter = fadeIn(spring(stiffness = Spring.StiffnessMediumLow)) +
                scaleIn(initialScale = 0.75f, animationSpec = spring(dampingRatio = Spring.DampingRatioMediumBouncy))
    ) {
        if (!faceUp || card == null) {
            CardBack(modifier = modifier, width = width, height = height)
        } else {
            CardFace(
                card = card,
                modifier = modifier,
                width = width,
                height = height,
                fourColor = fourColor,
                isFolded = isFolded
            )
        }
    }
}

/**
 * Animated community card that dynamically flips open when dealt!
 */
@Composable
fun AnimatedCommunityCard(
    card: Card?,
    index: Int,
    modifier: Modifier = Modifier,
    width: Dp = 62.dp,
    height: Dp = 86.dp,
    fourColor: Boolean = false
) {
    if (card == null) {
        // Face-down community card slot with hatched stripes (matches reference image)
        CardBack(
            modifier = modifier,
            width = width,
            height = height
        )
    } else {
        // 3D Card Opening Flip Animation
        val flipProgress = remember(card) { Animatable(0f) }

        LaunchedEffect(card) {
            val staggerMs = if (index < 3) (index * 75).toLong() else 0L
            if (staggerMs > 0) kotlinx.coroutines.delay(staggerMs)
            SoundManager.playCardDeal()
            flipProgress.animateTo(
                targetValue = 1f,
                animationSpec = spring(
                    dampingRatio = Spring.DampingRatioMediumBouncy,
                    stiffness = Spring.StiffnessMediumLow
                )
            )
        }

        val progress = flipProgress.value
        val rotationY = (1f - progress) * 90f
        val scale = 0.82f + 0.18f * progress
        val alpha = progress.coerceIn(0.25f, 1f)

        Box(
            modifier = modifier
                .size(width, height)
                .graphicsLayer {
                    this.rotationY = rotationY
                    this.scaleX = scale
                    this.scaleY = scale
                    this.alpha = alpha
                    cameraDistance = 14f * density
                }
        ) {
            CardFace(
                card = card,
                width = width,
                height = height,
                fourColor = fourColor
            )
        }
    }
}
