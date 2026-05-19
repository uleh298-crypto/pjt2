import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.wrapContentWidth
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.d102.wye.presentation.theme.IconInactive
import com.d102.wye.presentation.theme.SurfaceVariant
import com.d102.wye.presentation.theme.TextPrimary

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun DonutChart(
    items: List<SectorWeight>,
    modifier: Modifier = Modifier,
    chartSize: Dp = 120.dp,
    strokeWidth: Dp = 16.dp
) {
    val total = items.sumOf { it.ratio.toDouble() }.toFloat()
    val isEmpty = items.isEmpty() || total == 0f

    val progress = remember(items) { Animatable(0f) }
    LaunchedEffect(items) {
        progress.snapTo(0f)
        progress.animateTo(1f, animationSpec = tween(durationMillis = 900))
    }
    val animProgress by progress.asState()

    Box(
        modifier = modifier.size(chartSize),
        contentAlignment = Alignment.Center
    ) {
        Canvas(modifier = Modifier.size(chartSize)) {
            if (isEmpty) {
                drawArc(
                    color = SurfaceVariant,
                    startAngle = -90f,
                    sweepAngle = 360f * animProgress,
                    useCenter = false,
                    style = Stroke(width = strokeWidth.toPx(), cap = StrokeCap.Butt)
                )
            } else {
                var startAngle = -90f
                items.forEach { item ->
                    val fullSweep = (item.ratio / total) * 360f
                    val animatedSweep = fullSweep * animProgress
                    drawArc(
                        color = item.color,
                        startAngle = startAngle,
                        sweepAngle = animatedSweep.coerceAtLeast(0f),
                        useCenter = false,
                        style = Stroke(width = strokeWidth.toPx(), cap = StrokeCap.Butt)
                    )
                    startAngle += (item.ratio / total) * 360f
                }
            }
        }

        if (isEmpty) {
            Text(
                text = "0%",
                style = MaterialTheme.typography.bodyMedium,
                color = IconInactive,
                textAlign = TextAlign.Center,
                modifier = Modifier
                    .wrapContentWidth(unbounded = true)
                    .padding()
            )
        } else {
            Text(
                text = "100%",
                style = MaterialTheme.typography.titleMedium,
                color = TextPrimary
            )
        }
    }
}

data class SectorWeight(
    val name: String,
    val ratio: Float,
    val color: Color
)