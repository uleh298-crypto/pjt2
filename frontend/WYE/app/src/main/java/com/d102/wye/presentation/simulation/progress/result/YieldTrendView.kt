package com.d102.wye.presentation.simulation.progress.result

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.d102.wye.domain.model.BacktestPoint
import com.d102.wye.presentation.designsystem.DashedContainer
import com.d102.wye.presentation.designsystem.WyeCard
import com.d102.wye.presentation.model.UiState
import com.d102.wye.presentation.simulation.model.SimulationUiModel
import com.d102.wye.presentation.simulation.progress.SimulationFormState
import com.d102.wye.presentation.simulation.progress.components.ResultCard
import com.d102.wye.presentation.theme.IconInactive
import com.d102.wye.presentation.theme.MyDataYellow
import com.d102.wye.presentation.theme.PrimaryGreen
import com.d102.wye.presentation.theme.SurfaceDivider
import com.d102.wye.presentation.theme.SurfaceVariant
import com.d102.wye.presentation.theme.TextPrimary
import com.d102.wye.presentation.theme.TextSecondary

private val PositiveColor = Color(0xFFE53935)  // 상승 → 빨강
private val NegativeColor = Color(0xFF1565C0)  // 하락 → 파랑

@Composable
fun YieldTrendView(
    formState: SimulationFormState,
    simulationState: UiState<SimulationUiModel>,
    onOverlayToggled: (Boolean) -> Unit,
    overlayPoints: List<BacktestPoint>?,
    idleGuideMessage: String
) {
    Column {
        // ── 내 보유 자산 겹쳐보기 토글 ──────────────────────────────────────
        WyeCard(
            modifier = Modifier.fillMaxWidth(),
            innerPadding = PaddingValues(horizontal = 20.dp, vertical = 12.dp),
            border = BorderStroke(1.dp, SurfaceVariant),
            containerColor = MyDataYellow.copy(alpha = 0.03f),
            elevation = 0.dp
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                    Text(
                        text = "내 보유 자산 겹쳐보기",
                        style = MaterialTheme.typography.titleSmall.copy(fontSize = 14.sp),
                        color = if (simulationState is UiState.Success) TextPrimary else TextSecondary
                    )
                    Text(
                        text = if (simulationState is UiState.Success) "실제 자산과 시뮬레이션을 비교해보세요" else "포트폴리오를 구성하고 실제 자산과 비교해보세요",
                        style = MaterialTheme.typography.bodySmall,
                        color = TextSecondary,
                    )
                }
                Switch(
                    checked = formState.isOverlayEnabled,
                    onCheckedChange = onOverlayToggled,
                    enabled = simulationState is UiState.Success,
                    colors = SwitchDefaults.colors(
                        checkedThumbColor = Color.White,
                        checkedTrackColor = MyDataYellow,
                        checkedBorderColor = MyDataYellow,
                        uncheckedThumbColor = Color.White,
                        uncheckedTrackColor = SurfaceDivider,
                        uncheckedBorderColor = SurfaceDivider,
                        disabledCheckedThumbColor = Color.White,
                        disabledCheckedTrackColor = MyDataYellow.copy(alpha = 0.5f),
                        disabledUncheckedThumbColor = Color.White,
                        disabledUncheckedTrackColor = SurfaceDivider.copy(alpha = 0.5f),
                        disabledUncheckedBorderColor = SurfaceDivider.copy(alpha = 0.5f)
                    )
                )
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // ── 요약 카드 3개 ────────────────────────────────────────────────────
        val uiModel = (simulationState as? UiState.Success)?.data

        val cardItems = listOf(
            Triple(
                "예상 총 자산",
                uiModel?.estimatedFinalAsset ?: "-",
                uiModel?.netProfit ?: "-"
            ) to (uiModel?.let {
                if (it.isPositiveReturn) PositiveColor else NegativeColor
            } ?: TextSecondary),
            Triple(
                "수익률",
                uiModel?.yieldRate ?: "-",
                if (uiModel?.isPositiveReturn == true) "▲ 상승"
                else if (uiModel != null) "▼ 하락"
                else "-"
            ) to (uiModel?.let {
                if (it.isPositiveReturn) PositiveColor else NegativeColor
            } ?: TextSecondary),
            Triple(
                "총 투자금",
                uiModel?.totalInvestment ?: "-",
                "변동없음"
            ) to TextSecondary  // 항상 회색
        )

        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            cardItems.forEach { (card, subColor) ->
                val (title, value, subText) = card
                ResultCard(
                    modifier = Modifier
                        .weight(1f)
                        .height(110.dp),
                    borderColor = PrimaryGreen.copy(alpha = 0.1f),
                    backgroundColor = PrimaryGreen.copy(alpha = 0.05f)
                ) {
                    Column(modifier = Modifier.fillMaxWidth()) {
                        Text(
                            text = title,
                            style = MaterialTheme.typography.labelLarge,
                            color = TextSecondary
                        )
                        Spacer(modifier = Modifier.height(6.dp))
                        Text(
                            text = value,
                            style = MaterialTheme.typography.titleSmall.copy(fontSize = 14.sp),
                            color = TextPrimary
                        )
                        Spacer(modifier = Modifier.weight(1f))
                        if (subText != "-") {
                            Text(
                                text = subText,
                                style = MaterialTheme.typography.labelSmall.copy(fontSize = 9.sp),
                                color = subColor,
                                modifier = Modifier.fillMaxWidth(),
                                textAlign = TextAlign.End
                            )
                        }
                    }
                }
            }
        }


        Spacer(modifier = Modifier.height(18.dp))

        // ── 차트 영역 ────────────────────────────────────────────────────────
        when (simulationState) {
            is UiState.Idle -> {
                DashedContainer(height = 180.dp, strokeWidth = 2.dp) {
                    Text(
                        text = idleGuideMessage,
                        style = MaterialTheme.typography.bodyMedium,
                        color = IconInactive,
                        textAlign = TextAlign.Center
                    )
                }
            }

            is UiState.Loading -> {
                val totalWeight = formState.portfolioItems.sumOf { it.weight }
                DashedContainer(height = 180.dp, strokeWidth = 2.dp) {
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        if (totalWeight != 100) {
                            Text(
                                text = "$totalWeight%",
                                style = MaterialTheme.typography.titleSmall,
                                color = if (totalWeight > 100) PositiveColor else TextPrimary
                            )
                            Text(
                                text = "비중 합계를 100%로 맞춰주세요",
                                style = MaterialTheme.typography.bodySmall,
                                color = TextSecondary,
                                textAlign = TextAlign.Center
                            )
                        } else {
                            CircularProgressIndicator(
                                color = PrimaryGreen,
                                strokeWidth = 3.dp
                            )
                            Text(
                                text = "계산 중...",
                                style = MaterialTheme.typography.bodySmall,
                                color = TextSecondary
                            )
                        }
                    }
                }
            }

            is UiState.Success -> {
                BacktestChart(
                    modifier = Modifier.fillMaxWidth(),
                    points = simulationState.data.backtestPoints,
                    overlayPoints = overlayPoints,
                    periodMonths = formState.investmentPeriod.toIntOrNull() ?: 0,
                )
            }

            is UiState.Error -> {
                DashedContainer(height = 180.dp, strokeWidth = 2.dp) {
                    Text(
                        text = simulationState.message,
                        style = MaterialTheme.typography.bodySmall,
                        color = PositiveColor,
                        textAlign = TextAlign.Center
                    )
                }
            }
        }
    }
}