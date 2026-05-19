package com.d102.wye.presentation.strategy.detail.components

import androidx.compose.animation.animateContentSize
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.d102.wye.presentation.strategy.detail.StrategyDetailData
import com.d102.wye.presentation.theme.EtfFall
import com.d102.wye.presentation.theme.EtfRise
import com.d102.wye.presentation.theme.PrimaryGreen
import com.d102.wye.presentation.theme.SurfaceVariant
import com.d102.wye.presentation.theme.TextPrimary
import com.d102.wye.presentation.theme.TextSecondary

@Composable
fun SummaryMetricsRow(data: StrategyDetailData) {
    var isTagsExpanded by remember { mutableStateOf(false) }
    val maxVisibleTags = 3

    RoundedSurface(horizontalPaddingValue = 24.dp) {
        Column(
            modifier = Modifier
                .padding(20.dp)
                .animateContentSize()
        ) {

            Box(
                modifier = Modifier.fillMaxWidth()
            ) {
                Text(
                    modifier = Modifier.align(Alignment.TopStart),
                    text = "•  ${data.investmentType}",
                    style = MaterialTheme.typography.labelMedium,
                    color = PrimaryGreen
                )

                Text(
                    modifier = Modifier.align(Alignment.TopEnd),
                    text = "생성일 ${data.saveDate.take(10)}",
                    style = MaterialTheme.typography.labelSmall,
                    color = TextSecondary
                )
            }
            Spacer(modifier = Modifier.height(6.dp))
            Text(
                text = data.title,
                style = MaterialTheme.typography.headlineSmall.copy(fontWeight = FontWeight.Bold),
                color = TextPrimary
            )
            Spacer(modifier = Modifier.height(12.dp))

            val displayTags = if (isTagsExpanded || data.etfNames.size <= maxVisibleTags) {
                data.etfNames
            } else {
                data.etfNames.take(maxVisibleTags)
            }

            FlowRow(
                horizontalArrangement = Arrangement.spacedBy(6.dp),
                verticalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                displayTags.forEach { etfName ->
                    Text(
                        text = "#$etfName",
                        style = MaterialTheme.typography.labelMedium,
                        color = TextSecondary,
                        modifier = Modifier
                            .background(SurfaceVariant, RoundedCornerShape(4.dp))
                            .padding(horizontal = 6.dp, vertical = 4.dp)
                    )
                }

                // 태그가 많고 접혀있는 상태라면 "+ N개" 뱃지 표시
                if (!isTagsExpanded && data.etfNames.size > maxVisibleTags) {
                    val hiddenCount = data.etfNames.size - maxVisibleTags
                    Text(
                        text = "+ $hiddenCount",
                        style = MaterialTheme.typography.labelMedium,
                        color = PrimaryGreen, // 클릭 유도를 위해 색상 다르게
                        modifier = Modifier
                            .background(PrimaryGreen.copy(alpha = 0.1f), RoundedCornerShape(4.dp))
                            .clickable { isTagsExpanded = true }
                            .padding(horizontal = 8.dp, vertical = 4.dp)
                    )
                }
            }

            // 다 펼쳐졌을 때 다시 접는 버튼 (선택 사항)
            if (isTagsExpanded && data.etfNames.size > maxVisibleTags) {
                Text(
                    text = "접기",
                    style = MaterialTheme.typography.labelSmall,
                    color = TextSecondary,
                    modifier = Modifier
                        .padding(top = 8.dp)
                        .clickable { isTagsExpanded = false }
                )
            }

            Spacer(modifier = Modifier.height(20.dp))

            // 2. 하단 지표 (수익률, 자산 등)
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceEvenly,
                verticalAlignment = Alignment.CenterVertically
            ) {
                data.summaryMetrics.forEachIndexed { index, (label, value) ->
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        modifier = Modifier.weight(1f) // 균등 분할
                    ) {
                        Text(
                            text = label,
                            style = MaterialTheme.typography.labelSmall,
                            color = TextSecondary
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            text = value.replace(" ", "\n"),
                            style = MaterialTheme.typography.titleMedium.copy(
                                fontWeight = FontWeight.Bold,
                                fontSize = 15.sp,
                                lineHeight = 20.sp
                            ),
                            color = when {
                                value.startsWith("+") -> EtfRise
                                value.startsWith("-") -> EtfFall
                                else -> TextPrimary
                            },
                            textAlign = TextAlign.Center
                        )
                    }

                    // 지표 사이 세로 구분선
                    if (index < data.summaryMetrics.size - 1) {
                        Box(
                            modifier = Modifier
                                .height(20.dp)
                                .width(1.dp)
                                .background(SurfaceVariant)
                        )
                    }
                }
            }
        }
    }
}