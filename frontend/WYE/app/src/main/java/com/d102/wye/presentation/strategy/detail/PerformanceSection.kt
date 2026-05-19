package com.d102.wye.presentation.strategy.detail

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.d102.wye.domain.state.InvestmentType
import com.d102.wye.presentation.designsystem.WyeBadge
import com.d102.wye.presentation.simulation.progress.result.BacktestChart
import com.d102.wye.presentation.strategy.detail.components.RoundedSurface
import com.d102.wye.presentation.theme.EtfFall
import com.d102.wye.presentation.theme.EtfRise
import com.d102.wye.presentation.theme.IconInactive
import com.d102.wye.presentation.theme.PrimaryGreen
import com.d102.wye.presentation.theme.SurfaceVariant
import com.d102.wye.presentation.theme.TextSecondary
import com.d102.wye.presentation.theme.TextTertiary
import java.time.LocalDate
import java.time.temporal.ChronoUnit

@Composable
fun PerformanceSection(data: PerformanceData, isMain: Boolean) {
    val isPositive = data.rate.contains("+")

    RoundedSurface {
        Column(modifier = Modifier.padding(20.dp)) {
            WyeBadge(
                shape = CircleShape,
                label = data.period,
                textStyle = MaterialTheme.typography.labelSmall,
                color = if (isMain) PrimaryGreen.copy(alpha = 0.1f) else SurfaceVariant,
                textColor = if (isMain) PrimaryGreen else TextTertiary
            )

            if (data.points.size < 2) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(150.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.Center
                    ) {
                        Text(
                            text = "📊",
                            style = MaterialTheme.typography.titleLarge
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            text = "저장 후 첫 거래일부터\n수익률 그래프가 표시돼요",
                            style = MaterialTheme.typography.bodyMedium,
                            color = TextSecondary,
                            textAlign = TextAlign.Center
                        )
                    }
                }
            } else {
                Spacer(modifier = Modifier.height(12.dp))

                Text(
                    text = "${data.period} 성과",
                    style = MaterialTheme.typography.bodyMedium,
                    color = TextSecondary
                )

                Spacer(modifier = Modifier.height(8.dp))

                Text(
                    text = data.rate,
                    style = MaterialTheme.typography.titleLarge.copy(fontSize = 30.sp),
                    color = when {
                        !isMain -> TextSecondary        // 과거 1년 → 회색
                        isPositive -> EtfRise
                        else -> EtfFall
                    }
                )

                Spacer(modifier = Modifier.height(4.dp))

                Text(
                    text = "(${data.dateRange})",
                    modifier = Modifier.padding(bottom = 6.dp),
                    style = MaterialTheme.typography.labelSmall,
                    color = IconInactive
                )

                Spacer(modifier = Modifier.height(12.dp))

                val periodMonths = remember(data.points) {
                    val startDate = LocalDate.parse(data.points.first().date)
                    val endDate = LocalDate.parse(data.points.last().date)
                    ChronoUnit.MONTHS.between(startDate, endDate).toInt()
                }

                BacktestChart(
                    points = data.points,
                    isDashed = !isMain,
                    periodMonths = periodMonths,
                    modifier = Modifier.fillMaxWidth()
                )
            }
        }

    }
}