package com.d102.wye.presentation.simulation.progress.result

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.drawWithCache
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.PathEffect
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.drawscope.clipRect
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.d102.wye.domain.model.BacktestPoint
import com.d102.wye.presentation.theme.MyDataYellow
import com.d102.wye.presentation.theme.PrimaryGreen
import com.d102.wye.presentation.theme.TextSecondary
import kotlin.math.abs

@Composable
fun BacktestChart(
    modifier: Modifier = Modifier,
    points: List<BacktestPoint>,
    overlayPoints: List<BacktestPoint>? = null,
    periodMonths: Int,
    isDashed: Boolean = false
) {
    val progress = remember(points) { Animatable(0f) }

    val showDays = periodMonths < 3

    LaunchedEffect(points) {
        progress.snapTo(0f)
        progress.animateTo(1f, animationSpec = tween(durationMillis = 1000))
    }
    val animProgress by progress.asState()

    val lineColor = when {
        isDashed -> TextSecondary
        else -> PrimaryGreen
    }

    val values = remember(points) { points.map { it.value } }
    val overlayValues = remember(overlayPoints) { overlayPoints?.map { it.value } ?: emptyList() }

    val minVal = remember(values, overlayValues) {
        (values + overlayValues).minOrNull() ?: 0.0
    }
    val maxVal = remember(values, overlayValues) {
        (values + overlayValues).maxOrNull() ?: 1.0
    }
    val displayMin = remember(minVal, maxVal) {
        if (maxVal - minVal < 10.0) {
            val center = (maxVal + minVal) / 2.0
            minOf(center - 5.0, 0.0)
        } else minVal
    }
    val displayMax = remember(minVal, maxVal) {
        if (maxVal - minVal < 10.0) {
            val center = (maxVal + minVal) / 2.0
            maxOf(center + 5.0, 0.0)
        } else maxVal
    }
    val range = remember(displayMin, displayMax) {
        if (abs(displayMax - displayMin) < 0.001) 1.0 else displayMax - displayMin
    }

    Column(modifier = modifier) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(160.dp)
                .padding(horizontal = 4.dp)
        ) {
            Canvas(
                modifier = Modifier
                    .fillMaxSize()
                    .drawWithCache {
                        val w = size.width
                        val verticalPadding = 1.dp.toPx()
                        val h = size.height - (verticalPadding * 2)

                        fun xOf(i: Int, size: Int) = (i.toFloat() / (size - 1).coerceAtLeast(1)) * w
                        fun yOf(v: Double) =
                            verticalPadding + h - ((v - displayMin) / range * h).toFloat()

                        val zeroY =
                            yOf(0.0).coerceIn(verticalPadding, size.height - verticalPadding)

                        // 메인 차트 라인 경로
                        val linePath = Path().apply {
                            moveTo(xOf(0, values.size), yOf(values[0]))
                            for (i in 1 until values.size) {
                                val x0 = xOf(i - 1, values.size);
                                val y0 = yOf(values[i - 1])
                                val x1 = xOf(i, values.size);
                                val y1 = yOf(values[i])
                                cubicTo((x0 + x1) / 2f, y0, (x0 + x1) / 2f, y1, x1, y1)
                            }
                        }

                        // ✨ 포인트 2: 마이데이터 차트 라인 경로
                        val overlayPath = if (overlayValues.isNotEmpty()) {
                            Path().apply {
                                moveTo(xOf(0, overlayValues.size), yOf(overlayValues[0]))
                                for (i in 1 until overlayValues.size) {
                                    val x0 = xOf(i - 1, overlayValues.size);
                                    val y0 = yOf(overlayValues[i - 1])
                                    val x1 = xOf(i, overlayValues.size);
                                    val y1 = yOf(overlayValues[i])
                                    cubicTo((x0 + x1) / 2f, y0, (x0 + x1) / 2f, y1, x1, y1)
                                }
                            }
                        } else null

                        // 메인 차트 그라디언트 채우기
                        val fillPath = Path().apply {
                            moveTo(xOf(0, values.size), zeroY)
                            lineTo(xOf(0, values.size), yOf(values[0]))
                            for (i in 1 until values.size) {
                                val x0 = xOf(i - 1, values.size);
                                val y0 = yOf(values[i - 1])
                                val x1 = xOf(i, values.size);
                                val y1 = yOf(values[i])
                                cubicTo((x0 + x1) / 2f, y0, (x0 + x1) / 2f, y1, x1, y1)
                            }
                            lineTo(xOf(values.size - 1, values.size), zeroY)
                            close()
                        }

                        // ✨ 포인트 3: 마이데이터 그라디언트 채우기 경로 추가
                        val overlayFillPath = if (overlayValues.isNotEmpty()) {
                            Path().apply {
                                moveTo(xOf(0, overlayValues.size), zeroY)
                                lineTo(xOf(0, overlayValues.size), yOf(overlayValues[0]))
                                for (i in 1 until overlayValues.size) {
                                    val x0 = xOf(i - 1, overlayValues.size);
                                    val y0 = yOf(overlayValues[i - 1])
                                    val x1 = xOf(i, overlayValues.size);
                                    val y1 = yOf(overlayValues[i])
                                    cubicTo((x0 + x1) / 2f, y0, (x0 + x1) / 2f, y1, x1, y1)
                                }
                                lineTo(xOf(overlayValues.size - 1, overlayValues.size), zeroY)
                                close()
                            }
                        } else null

                        onDrawBehind {
                            // 그리드 & 0% 기준선 (기존과 동일)
                            repeat(3) { i ->
                                val y = verticalPadding + (h * ((i + 1) / 4f))
                                drawLine(
                                    Color.Gray.copy(alpha = 0.1f),
                                    Offset(0f, y),
                                    Offset(w, y),
                                    0.5.dp.toPx()
                                )
                            }
                            if (displayMin < 0.0 || displayMax > 0.0) {
                                drawLine(
                                    color = Color.Gray.copy(alpha = 0.4f),
                                    start = Offset(0f, zeroY),
                                    end = Offset(w, zeroY),
                                    strokeWidth = 1.dp.toPx(),
                                    pathEffect = PathEffect.dashPathEffect(floatArrayOf(8f, 6f))
                                )
                            }

                            clipRect(right = w * animProgress) {
                                if (overlayValues.isNotEmpty()) {
                                    overlayFillPath?.let { path ->
                                        drawPath(
                                            path = path,
                                            brush = Brush.verticalGradient(
                                                colors = listOf(
                                                    MyDataYellow.copy(alpha = 0.2f), // 부드러운 노란색
                                                    Color.Transparent
                                                ),
                                                startY = verticalPadding,
                                                endY = size.height - verticalPadding
                                            )
                                        )
                                    }
                                    overlayPath?.let { path ->
                                        drawPath(
                                            path = path,
                                            color = MyDataYellow,
                                            style = Stroke(
                                                width = 1.5.dp.toPx(), // 메인보다 살짝 가늘게
                                                cap = StrokeCap.Round
                                            )
                                        )
                                    }
                                }

                                // 메인 차트 그라디언트 & 라인 (기존과 동일)
                                if (!isDashed) {
                                    drawPath(
                                        path = fillPath,
                                        brush = Brush.verticalGradient(
                                            colors = listOf(
                                                lineColor.copy(alpha = 0.25f),
                                                Color.Transparent
                                            ),
                                            startY = verticalPadding,
                                            endY = size.height - verticalPadding
                                        )
                                    )
                                }
                                drawPath(
                                    path = linePath,
                                    color = lineColor,
                                    style = Stroke(width = 2.dp.toPx(), cap = StrokeCap.Round)
                                )
                            }

                            // 마지막 점 (기존과 동일)
                            if (!isDashed && animProgress >= 0.99f) {
                                val lastX = xOf(values.size - 1, values.size)
                                val lastY = yOf(values.last())
                                drawCircle(
                                    color = lineColor,
                                    radius = 4.dp.toPx(),
                                    center = Offset(lastX, lastY)
                                )
                                drawCircle(
                                    color = Color.White,
                                    radius = 2.dp.toPx(),
                                    center = Offset(lastX, lastY)
                                )
                            }
                        }
                    }
            ) {}

            Text(
                text = "수익률 (%)",
                style = MaterialTheme.typography.labelSmall,
                color = TextSecondary.copy(alpha = 0.8f),
                modifier = Modifier.align(Alignment.TopStart)
            )
        }

        // x축 날짜 레이블 (기존과 동일)
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 4.dp),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            listOf(
                points.first().date,
                points[points.size / 2].date,
                points.last().date
            ).forEach { rawDate ->
                Text(
                    text = rawDate.toDateLabel(showDays),
                    style = MaterialTheme.typography.labelSmall.copy(fontSize = 10.sp),
                    color = TextSecondary
                )
            }
        }
    }
}

private fun String.toDateLabel(showDays: Boolean): String {
    val parts = this.split("-")
    if (parts.size >= 3) {
        val yy = parts[0].takeLast(2)
        val mm = parts[1]
        val dd = parts[2]
        return if (showDays) "$yy.$mm.$dd" else "$yy.$mm"
    }
    return this
}