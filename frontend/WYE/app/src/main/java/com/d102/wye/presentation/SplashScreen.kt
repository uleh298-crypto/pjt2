package com.d102.wye.presentation

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.d102.wye.presentation.theme.BackGroundLightGreen
import com.d102.wye.presentation.theme.BackGroundLightGreen3
import com.d102.wye.presentation.theme.Background
import com.d102.wye.presentation.theme.PrimaryGreen
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

@Composable
fun LaunchSplashScreen(
    modifier: Modifier = Modifier,
    onAnimationFinished: () -> Unit = {}
) {
    val density = LocalDensity.current
    val whatsAlpha = remember { Animatable(0f) }
    val whatsOffsetX = remember { Animatable(0f) }
    val whatsOffsetY = remember { Animatable(0f) }
    val whatsScale = remember { Animatable(0.88f) }
    val whatsRotation = remember { Animatable(0f) }

    val yourAlpha = remember { Animatable(0f) }
    val yourOffsetX = remember { Animatable(0f) }
    val yourOffsetY = remember { Animatable(0f) }
    val yourScale = remember { Animatable(0.9f) }
    val yourRotation = remember { Animatable(0f) }

    val etfAlpha = remember { Animatable(0f) }
    val etfOffsetX = remember { Animatable(0f) }
    val etfOffsetY = remember { Animatable(0f) }
    val etfScale = remember { Animatable(0.92f) }
    val etfRotation = remember { Animatable(0f) }
    val screenAlpha = remember { Animatable(1f) }

    LaunchedEffect(Unit) {
        suspend fun animateWord(
            alpha: Animatable<Float, *>,
            offsetX: Animatable<Float, *>,
            offsetY: Animatable<Float, *>,
            scale: Animatable<Float, *>,
            rotation: Animatable<Float, *>,
            startX: Float,
            startY: Float,
            startScale: Float,
            startRotation: Float,
            delayMillis: Long
        ) {
            delay(delayMillis)
            offsetX.snapTo(startX)
            offsetY.snapTo(startY)
            scale.snapTo(startScale)
            rotation.snapTo(startRotation)
            alpha.snapTo(0f)
            kotlinx.coroutines.coroutineScope {
                launch {
                    alpha.animateTo(
                        targetValue = 1f,
                        animationSpec = tween(durationMillis = 360, easing = FastOutSlowInEasing)
                    )
                }
                launch {
                    offsetX.animateTo(
                        targetValue = 0f,
                        animationSpec = tween(durationMillis = 900, easing = FastOutSlowInEasing)
                    )
                }
                launch {
                    offsetY.animateTo(
                        targetValue = 0f,
                        animationSpec = tween(durationMillis = 900, easing = FastOutSlowInEasing)
                    )
                }
                launch {
                    scale.animateTo(
                        targetValue = 1f,
                        animationSpec = tween(durationMillis = 900, easing = FastOutSlowInEasing)
                    )
                }
                launch {
                    rotation.animateTo(
                        targetValue = 0f,
                        animationSpec = tween(durationMillis = 900, easing = FastOutSlowInEasing)
                    )
                }
            }
        }

        launch {
            animateWord(
                alpha = whatsAlpha,
                offsetX = whatsOffsetX,
                offsetY = whatsOffsetY,
                scale = whatsScale,
                rotation = whatsRotation,
                startX = with(density) { (-220).dp.toPx() },
                startY = with(density) { (-120).dp.toPx() },
                startScale = 0.88f,
                startRotation = -220f,
                delayMillis = 0L
            )
        }
        launch {
            animateWord(
                alpha = yourAlpha,
                offsetX = yourOffsetX,
                offsetY = yourOffsetY,
                scale = yourScale,
                rotation = yourRotation,
                startX = with(density) { 240.dp.toPx() },
                startY = with(density) { (-40).dp.toPx() },
                startScale = 0.9f,
                startRotation = 180f,
                delayMillis = 120L
            )
        }
        launch {
            animateWord(
                alpha = etfAlpha,
                offsetX = etfOffsetX,
                offsetY = etfOffsetY,
                scale = etfScale,
                rotation = etfRotation,
                startX = with(density) { (-180).dp.toPx() },
                startY = with(density) { 220.dp.toPx() },
                startScale = 0.92f,
                startRotation = -160f,
                delayMillis = 240L
            )
        }

        delay(2000)
        screenAlpha.animateTo(
            targetValue = 0f,
            animationSpec = tween(durationMillis = 400, easing = FastOutSlowInEasing)
        )
        onAnimationFinished()
    }

    Box(
        modifier = modifier
            .fillMaxSize()
            .graphicsLayer { alpha = screenAlpha.value }
            .background(Background)
    ) {
        SplashBackdrop()

        Column(
            modifier = Modifier
                .align(Alignment.Center)
                .padding(horizontal = 32.dp)
        ) {
            SplashWord(
                text = "What's",
                fontSize = 66.sp,
                lineHeight = 70.sp,
                letterSpacing = (-2.2).sp,
                alpha = whatsAlpha.value,
                offsetX = whatsOffsetX.value,
                offsetY = whatsOffsetY.value,
                scale = whatsScale.value,
                rotation = whatsRotation.value
            )
            Spacer(modifier = Modifier.height(4.dp))
            SplashWord(
                text = "your",
                fontSize = 66.sp,
                lineHeight = 70.sp,
                letterSpacing = (-2.2).sp,
                alpha = yourAlpha.value,
                offsetX = yourOffsetX.value,
                offsetY = yourOffsetY.value,
                scale = yourScale.value,
                rotation = yourRotation.value
            )
            Spacer(modifier = Modifier.height(4.dp))
            SplashWord(
                text = "ETF",
                fontSize = 66.sp,
                lineHeight = 70.sp,
                letterSpacing = (-2.2).sp,
                alpha = etfAlpha.value,
                offsetX = etfOffsetX.value,
                offsetY = etfOffsetY.value,
                scale = etfScale.value,
                rotation = etfRotation.value
            )
        }
    }
}

@Composable
private fun SplashBackdrop() {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(
                brush = Brush.verticalGradient(
                    colors = listOf(
                        Background,
                        BackGroundLightGreen3
                    )
                )
            )
    ) {
        Box(
            modifier = Modifier
                .align(Alignment.TopEnd)
                .offset(x = 72.dp, y = (-52).dp)
                .fillMaxWidth(0.6f)
                .height(280.dp)
                .background(
                    brush = Brush.radialGradient(
                        colors = listOf(
                            BackGroundLightGreen,
                            Color.Transparent
                        )
                    ),
                    shape = RoundedCornerShape(999.dp)
                )
        )

        Box(
            modifier = Modifier
                .align(Alignment.BottomStart)
                .offset(x = (-64).dp, y = 72.dp)
                .fillMaxWidth(0.52f)
                .height(220.dp)
                .background(
                    brush = Brush.radialGradient(
                        colors = listOf(
                            BackGroundLightGreen.copy(alpha = 0.82f),
                            Color.Transparent
                        )
                    ),
                    shape = RoundedCornerShape(999.dp)
                )
        )

    }
}

@Composable
private fun SplashWord(
    text: String,
    fontSize: androidx.compose.ui.unit.TextUnit,
    lineHeight: androidx.compose.ui.unit.TextUnit,
    letterSpacing: androidx.compose.ui.unit.TextUnit,
    alpha: Float,
    offsetX: Float,
    offsetY: Float,
    scale: Float,
    rotation: Float
) {
    Text(
        text = text,
        color = PrimaryGreen,
        modifier = Modifier.graphicsLayer {
            this.alpha = alpha
            translationX = offsetX
            translationY = offsetY
            scaleX = scale
            scaleY = scale
            rotationZ = rotation
        },
        textAlign = TextAlign.Start,
        style = TextStyle(
            fontFamily = MaterialTheme.typography.titleLarge.fontFamily,
            fontWeight = FontWeight.Black,
            fontSize = fontSize,
            lineHeight = lineHeight,
            letterSpacing = letterSpacing
        )
    )
}
