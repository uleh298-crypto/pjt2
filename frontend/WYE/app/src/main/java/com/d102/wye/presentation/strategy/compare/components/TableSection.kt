package com.d102.wye.presentation.strategy.compare.components

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.expandVertically
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Divider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.d102.wye.presentation.strategy.compare.CompareDetailStat
import com.d102.wye.presentation.strategy.detail.components.RoundedSurface
import com.d102.wye.presentation.theme.PrimaryGreen
import com.d102.wye.presentation.theme.SurfaceVariant
import com.d102.wye.presentation.theme.TextPrimary
import com.d102.wye.presentation.theme.TextSecondary

private data class MetricInfo(
    val name: String,
    val description: String,
    val tip: String
)

private val metricsInfo = listOf(
    MetricInfo(
        name = "수익률",
        description = "선택 기간 동안 포트폴리오의 총 수익률이에요.",
        tip = "높을수록 수익이 좋아요 📈"
    ),
    MetricInfo(
        name = "변동성",
        description = "수익률이 얼마나 들쭉날쭉한지 나타내요.\n연율화 표준편차로 계산해요.",
        tip = "낮을수록 안정적인 투자예요 🛡️"
    ),
    MetricInfo(
        name = "샤프비율",
        description = "위험 한 단위당 얼마나 수익을 냈는지 나타내요.\n(수익률 - 무위험수익률) ÷ 변동성으로 계산해요.",
        tip = "높을수록 효율적인 투자예요 ⚡"
    )
)

@Composable
fun CompareTableSection(items: List<CompareDetailStat>) {
    var showGuide by remember { mutableStateOf(false) }

    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {

        // ── 타이틀 + 툴팁 버튼
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "상세 비교",
                style = MaterialTheme.typography.labelLarge,
                color = TextPrimary
            )
            Text(
                text = if (showGuide) "지표 설명 닫기" else "지표 설명 보기",
                style = MaterialTheme.typography.bodySmall,
                color = PrimaryGreen,
                modifier = Modifier
                    .border(1.dp, PrimaryGreen.copy(alpha = 0.4f), RoundedCornerShape(12.dp))
                    .clickable { showGuide = !showGuide }
                    .padding(horizontal = 10.dp, vertical = 5.dp)
            )
        }

        // ── 지표 설명 가이드
        AnimatedVisibility(
            visible = showGuide,
            enter = fadeIn() + expandVertically(),
            exit = fadeOut() + shrinkVertically()
        ) {
            RoundedSurface(horizontalPaddingValue = 0.dp) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    metricsInfo.forEach { metric ->
                        Row(
                            verticalAlignment = Alignment.Top,
                            horizontalArrangement = Arrangement.spacedBy(10.dp)
                        ) {
                            Box(
                                modifier = Modifier
                                    .padding(top = 4.dp)
                                    .size(6.dp)
                                    .background(PrimaryGreen, CircleShape)
                            )
                            Column(verticalArrangement = Arrangement.spacedBy(3.dp)) {
                                Text(
                                    text = metric.name,
                                    style = MaterialTheme.typography.labelMedium,
                                    color = TextPrimary
                                )
                                Text(
                                    text = metric.description,
                                    style = MaterialTheme.typography.bodySmall,
                                    color = TextSecondary
                                )
                                Text(
                                    text = metric.tip,
                                    style = MaterialTheme.typography.bodySmall,
                                    color = PrimaryGreen
                                )
                            }
                        }
                    }
                }
            }
        }

        // ── 테이블
        RoundedSurface(horizontalPaddingValue = 0.dp) {
            Column {
                // 헤더
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 12.dp)
                ) {
                    Text(
                        "포트폴리오",
                        style = MaterialTheme.typography.labelSmall,
                        color = TextSecondary,
                        modifier = Modifier.weight(2f)
                    )
                    listOf("수익률", "변동성", "샤프비율").forEach { header ->
                        Text(
                            text = header,
                            style = MaterialTheme.typography.labelSmall,
                            color = TextSecondary,
                            modifier = Modifier.weight(1f),
                            textAlign = TextAlign.Center
                        )
                    }
                }

                Divider(color = SurfaceVariant)

                // 행
                items.forEachIndexed { index, item ->
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .background(if (index == 0) PrimaryGreen.copy(alpha = 0.05f) else Color.Transparent)
                            .padding(horizontal = 16.dp, vertical = 14.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(
                            modifier = Modifier.weight(2f),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Box(modifier = Modifier.size(8.dp).background(item.color, CircleShape))
                            Text(
                                item.name,
                                style = MaterialTheme.typography.bodySmall,
                                color = TextPrimary,
                                maxLines = 1
                            )
                        }
                        Text(
                            item.totalReturnRate,
                            style = MaterialTheme.typography.bodySmall.copy(fontSize = 11.sp),
                            color = when {
                                item.totalReturnRate.startsWith("+") -> Color(0xFF3A6E45)
                                item.totalReturnRate.startsWith("-") -> Color(0xFFE56A6A)
                                else -> TextPrimary
                            },
                            modifier = Modifier.weight(1f),
                            textAlign = TextAlign.Center
                        )
                        Text(
                            item.volatility,
                            style = MaterialTheme.typography.bodySmall.copy(fontSize = 11.sp),
                            color = TextSecondary,
                            modifier = Modifier.weight(1f),
                            textAlign = TextAlign.Center
                        )
                        Text(
                            item.sharpeRatio,
                            style = MaterialTheme.typography.bodySmall.copy(fontSize = 11.sp),
                            color = TextPrimary,
                            modifier = Modifier.weight(1f),
                            textAlign = TextAlign.Center
                        )
                    }
                    if (index < items.size - 1) Divider(
                        color = SurfaceVariant,
                        modifier = Modifier.padding(horizontal = 16.dp)
                    )
                }
            }
        }
    }
    Spacer(modifier = Modifier.height(12.dp))
}

@Composable
fun EmptyTableSection() {
    RoundedSurface(horizontalPaddingValue = 0.dp) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(170.dp),
            contentAlignment = Alignment.Center
        ) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(20.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Spacer(modifier = Modifier.weight(1.2f))
                    listOf("수익률", "변동성", "샤프비율").forEach { header ->
                        Text(
                            header,
                            style = MaterialTheme.typography.labelSmall,
                            color = TextSecondary,
                            modifier = Modifier.weight(1f),
                            textAlign = TextAlign.Center
                        )
                    }
                }
                Spacer(modifier = Modifier.height(20.dp))
                repeat(3) {
                    Row(
                        modifier = Modifier.fillMaxWidth().padding(vertical = 10.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Box(modifier = Modifier.weight(1.2f)) {
                            Box(modifier = Modifier.width(60.dp).height(12.dp).background(TextSecondary.copy(alpha = 0.2f), RoundedCornerShape(4.dp)))
                        }
                        repeat(3) {
                            Box(modifier = Modifier.weight(1f), contentAlignment = Alignment.Center) {
                                Box(modifier = Modifier.width(40.dp).height(12.dp).background(TextSecondary.copy(alpha = 0.2f), RoundedCornerShape(4.dp)))
                            }
                        }
                    }
                }
            }
            Surface(
                color = Color.White.copy(alpha = 0.4f),
                modifier = Modifier.fillMaxSize()
            ) {
                Box(contentAlignment = Alignment.Center) {
                    Text(
                        text = "상세 비교 데이터를 보려면 전략을 선택하세요.",
                        style = MaterialTheme.typography.bodyMedium,
                        color = TextSecondary
                    )
                }
            }
        }
    }
}