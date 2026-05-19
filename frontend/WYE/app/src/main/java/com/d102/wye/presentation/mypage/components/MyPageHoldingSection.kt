package com.d102.wye.presentation.mypage.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AutoGraph
import androidx.compose.material.icons.filled.Link
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.d102.wye.presentation.designsystem.WyePrimaryButton
import com.d102.wye.presentation.designsystem.WyeOutlinedCard
import com.d102.wye.presentation.mypage.MyDataUiState
import com.d102.wye.presentation.mypage.MyPageHoldingEtfUiModel
import com.d102.wye.presentation.theme.BackGroundLightGreen2
import com.d102.wye.presentation.theme.BackGroundLightGreen3
import com.d102.wye.presentation.theme.Border
import com.d102.wye.presentation.theme.EtfFall
import com.d102.wye.presentation.theme.EtfRise
import com.d102.wye.presentation.theme.PrimaryGreen
import com.d102.wye.presentation.theme.SurfaceWhite
import com.d102.wye.presentation.theme.TextPrimary
import com.d102.wye.presentation.theme.TextSecondary
import androidx.compose.foundation.BorderStroke

@Composable
fun MyPageHoldingSection(
    myDataState: MyDataUiState,
    holdingEtfs: List<MyPageHoldingEtfUiModel>,
    onHoldingEtfClick: (ticker: String) -> Unit,
    onMyDataConnectClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    if (myDataState == MyDataUiState.Loading) {
        Box(
            modifier = modifier
                .fillMaxWidth()
                .height(142.dp),
            contentAlignment = Alignment.Center
        ) {
            androidx.compose.material3.CircularProgressIndicator(
                color = PrimaryGreen,
                modifier = Modifier.size(32.dp)
            )
        }
        return
    }

    if (myDataState == MyDataUiState.NotConnected) {
        WyeOutlinedCard(
            modifier = modifier.fillMaxWidth(),
            innerPadding = PaddingValues(0.dp)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(BackGroundLightGreen3)
                    .padding(horizontal = 18.dp, vertical = 18.dp),
                verticalArrangement = Arrangement.spacedBy(18.dp)
            ) {
                Row(
                    horizontalArrangement = Arrangement.spacedBy(14.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Box(
                        modifier = Modifier
                            .size(64.dp)
                            .background(
                                color = BackGroundLightGreen2,
                                shape = RoundedCornerShape(20.dp)
                            ),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Filled.Link,
                            contentDescription = null,
                            modifier = Modifier.size(28.dp),
                            tint = PrimaryGreen
                        )
                    }

                    Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                        Text(
                            text = "마이데이터가 연동되어 있지 않습니다.",
                            color = TextPrimary,
                            style = MaterialTheme.typography.titleSmall
                        )
                        Text(
                            text = "연동 후 보유 ETF, 현재 가격, 등락률을 여기서 바로 확인할 수 있어요.",
                            color = TextSecondary,
                            style = MaterialTheme.typography.bodySmall
                        )
                    }
                }

                WyePrimaryButton(
                    text = "마이데이터 연동하기",
                    onClick = onMyDataConnectClick,
                )
            }
        }
        return
    }

    if (holdingEtfs.isEmpty()) {
        WyeOutlinedCard(
            modifier = modifier.fillMaxWidth(),
            innerPadding = PaddingValues(0.dp)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(BackGroundLightGreen3)
                    .padding(horizontal = 18.dp, vertical = 18.dp),
                verticalArrangement = Arrangement.spacedBy(18.dp)
            ) {
                Row(
                    horizontalArrangement = Arrangement.spacedBy(14.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Box(
                        modifier = Modifier
                            .size(64.dp)
                            .background(
                                color = BackGroundLightGreen2,
                                shape = RoundedCornerShape(20.dp)
                            ),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Filled.AutoGraph,
                            contentDescription = null,
                            modifier = Modifier.size(30.dp),
                            tint = PrimaryGreen
                        )
                    }

                    Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                        Text(
                            text = "보유하고 있는 ETF가 없습니다.",
                            color = TextPrimary,
                            style = MaterialTheme.typography.titleSmall
                        )
                        Text(
                            text = "ETF를 보유하면 여기서 등락률과 흐름을 바로 확인할 수 있어요.",
                            color = TextSecondary,
                            style = MaterialTheme.typography.bodySmall
                        )
                    }
                }
            }
        }
        return
    }

    LazyRow(
        modifier = modifier,
        horizontalArrangement = Arrangement.spacedBy(10.dp),
        contentPadding = PaddingValues(horizontal = 0.dp)
    ) {
        items(items = holdingEtfs, key = { it.ticker }) { etf ->
            Card(
                modifier = Modifier
                    .fillParentMaxWidth(0.48f)
                    .height(142.dp),
                onClick = { onHoldingEtfClick(etf.ticker) },
                shape = RoundedCornerShape(12.dp),
                colors = CardDefaults.cardColors(containerColor = SurfaceWhite),
                elevation = CardDefaults.cardElevation(defaultElevation = 0.dp),
                border = BorderStroke(1.dp, Border),
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(14.dp)
                ) {
                    Text(
                        text = etf.name,
                        style = MaterialTheme.typography.labelLarge,
                        maxLines = 1
                    )
                    Spacer(modifier = Modifier.height(6.dp))
                    Text(
                        text = "현재가",
                        style = MaterialTheme.typography.bodySmall,
                        color = TextSecondary
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = etf.currentPriceText,
                        style = MaterialTheme.typography.titleMedium.copy(fontWeight = androidx.compose.ui.text.font.FontWeight.SemiBold),
                        color = TextPrimary
                    )
                    Spacer(modifier = Modifier.height(2.dp))
                    Spacer(modifier = Modifier.weight(1f))
                    Column(verticalArrangement = Arrangement.spacedBy(1.dp)) {
                        Text(
                            text = "${etf.changeDetailText} (${etf.changeRateText})",
                            style = MaterialTheme.typography.bodySmall,
                            color = if (etf.isPositive) EtfRise else EtfFall
                        )
                        Text(
                            text = etf.quantityText,
                            style = MaterialTheme.typography.bodySmall,
                            color = TextSecondary
                        )
                    }
                }
            }
        }
    }
}
