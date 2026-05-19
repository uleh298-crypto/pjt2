package com.d102.wye.presentation.designsystem

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxScope
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.PathEffect
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.d102.wye.presentation.theme.Divider

@Composable
fun DashedContainer(
    modifier: Modifier = Modifier,
    height: Dp = 150.dp,
    strokeWidth: Dp = 1.dp,
    dashLength: Dp = 8.dp,
    dashGap: Dp = 8.dp,
    cornerRadius: Dp = 12.dp,
    borderColor: Color = Divider,
    contentAlignment: Alignment = Alignment.Center,
    content: @Composable BoxScope.() -> Unit
) {
    Box(
        modifier = modifier
            .fillMaxWidth()
            .height(height)
            .drawBehind {
                drawRoundRect(
                    color = borderColor,
                    style = Stroke(
                        width = strokeWidth.toPx(),
                        pathEffect = PathEffect.dashPathEffect(
                            intervals = floatArrayOf(dashLength.toPx(), dashGap.toPx()),
                            phase = 0f
                        )
                    ),
                    cornerRadius = CornerRadius(cornerRadius.toPx())
                )
            },
        contentAlignment = contentAlignment,
        content = content
    )
}