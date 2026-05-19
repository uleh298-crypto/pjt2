package com.d102.wye.presentation.simulation.progress.result

import DonutChart
import SectorWeight
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.defaultMinSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.d102.wye.presentation.designsystem.WyeCard
import com.d102.wye.presentation.model.UiState
import com.d102.wye.presentation.simulation.model.SimulationUiModel
import com.d102.wye.presentation.simulation.progress.components.ResultCard
import com.d102.wye.presentation.theme.BackGroundLightGreen3
import com.d102.wye.presentation.theme.Divider
import com.d102.wye.presentation.theme.IconInactive
import com.d102.wye.presentation.theme.SectorColor1
import com.d102.wye.presentation.theme.SectorColor2
import com.d102.wye.presentation.theme.SectorColor3
import com.d102.wye.presentation.theme.SectorColor4
import com.d102.wye.presentation.theme.SectorColor5
import com.d102.wye.presentation.theme.SurfaceCard
import com.d102.wye.presentation.theme.SurfaceVariant
import com.d102.wye.presentation.theme.TextPrimary
import com.d102.wye.presentation.theme.TextSecondary

@Composable
fun PortfolioAnalysisView(
    simulationState: UiState<SimulationUiModel>,
    onDictionaryClick: () -> Unit
) {
    Column(
        modifier = Modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "포트폴리오 주요 지표",
                style = MaterialTheme.typography.titleSmall.copy(fontSize = 14.sp),
                color = TextSecondary,
            )
            Text(
                text = "ⓘ 용어 가이드",
                textAlign = TextAlign.Center,
                color = TextSecondary,
                style = MaterialTheme.typography.bodySmall,
                modifier = Modifier
                    .border(1.dp, Divider, RoundedCornerShape(12.dp))
                    .clickable { onDictionaryClick() }
                    .padding(horizontal = 12.dp, vertical = 6.dp)
            )
        }

        // ── PER / PBR / ROE 카드 ──────────────────────────────────────────────
        val uiModel = (simulationState as? UiState.Success)?.data
        val sectorColors = listOf(
            SectorColor1, SectorColor2, SectorColor3,
            SectorColor4, SectorColor5
        )
        val sectorData = uiModel?.sectorWeights?.mapIndexed { index, s ->
            SectorWeight(
                name = s.name,
                ratio = s.ratio,
                color = sectorColors.getOrElse(index) { SectorColor4 }
            )
        } ?: emptyList()

        val isEmpty = sectorData.isEmpty()


        val cardItems = listOf(
            "PER" to (uiModel?.per ?: "-"),
            "PBR" to (uiModel?.pbr ?: "-"),
            "ROE" to (uiModel?.roe ?: "-")
        )

        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            cardItems.forEach { (title, value) ->
                ResultCard(
                    modifier = Modifier
                        .weight(1f)
                        .height(80.dp),
                    borderColor = SurfaceVariant,
                    backgroundColor = BackGroundLightGreen3
                ) {
                    Column(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Text(
                            text = title,
                            style = MaterialTheme.typography.labelLarge,
                            color = TextSecondary,
                            textAlign = TextAlign.Center
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            text = value,
                            style = MaterialTheme.typography.titleSmall,
                            color = TextPrimary,
                            textAlign = TextAlign.Center
                        )
                    }
                }
            }
        }

        // ── 섹터 비중 도넛 차트 ───────────────────────────────────────────────
        // ── 섹터 비중 도넛 차트 ───────────────────────────────────────────────
        WyeCard(
            modifier = Modifier.fillMaxWidth(),
            innerPadding = PaddingValues(20.dp), // (또는 16.dp)
            border = BorderStroke(1.dp, SurfaceVariant),
            containerColor = SurfaceCard,
            elevation = 0.dp
        ) {
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Text(
                    text = "섹터 비중",
                    style = MaterialTheme.typography.titleSmall.copy(fontSize = 14.sp),
                    color = TextSecondary,
                    modifier = Modifier.fillMaxWidth(),
                )

                DonutChart(items = sectorData)
                Spacer(modifier = Modifier.height(4.dp))

                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .defaultMinSize(minHeight = 40.dp),
                    contentAlignment = Alignment.Center
                ) {
                    if (!isEmpty) {
                        FlowRow(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(
                                12.dp,
                                Alignment.CenterHorizontally
                            ),
                            verticalArrangement = Arrangement.spacedBy(6.dp)
                        ) {
                            sectorData.forEach { sector ->
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.Center
                                ) {
                                    Box(
                                        modifier = Modifier
                                            .size(8.dp)
                                            .background(sector.color, CircleShape)
                                    )
                                    Spacer(modifier = Modifier.width(4.dp))
                                    Text(
                                        text = "${sector.name} ${sector.ratio.toInt()}%",
                                        style = MaterialTheme.typography.bodySmall.copy(fontSize = 11.sp),
                                        color = TextSecondary
                                    )
                                }
                            }
                        }
                    } else {
                        Text(
                            text = "ETF를 추가하고 섹터 비중을 확인해보세요",
                            style = MaterialTheme.typography.labelLarge,
                            color = IconInactive,
                            textAlign = TextAlign.Center
                        )
                    }
                }
            }
        }
    }
}