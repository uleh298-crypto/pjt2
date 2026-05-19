package com.d102.wye.presentation.explore.stock

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.AccountBalance
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.d102.wye.domain.model.StockEtf
import com.d102.wye.presentation.designsystem.WyeTopBar
import com.d102.wye.presentation.model.UiState
import com.d102.wye.presentation.theme.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun StockEtfListScreen(
    onBack: () -> Unit,
    onEtfClick: (String) -> Unit,
    viewModel: StockDetailViewModel = hiltViewModel(),
) {
    val stockState by viewModel.stockState.collectAsStateWithLifecycle()

    Scaffold(
        containerColor = MaterialTheme.colorScheme.background,
        topBar = {
            WyeTopBar(title = "포함된 ETF 목록", onBackClick = onBack)
        },
    ) { innerPadding ->
        when (val state = stockState) {
            is UiState.Loading -> Box(
                Modifier.fillMaxSize().padding(innerPadding),
                contentAlignment = Alignment.Center,
            ) { CircularProgressIndicator(color = PrimaryGreen) }

            is UiState.Success -> {
                val stock = state.data
                LazyColumn(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(top = innerPadding.calculateTopPadding()),
                    contentPadding = PaddingValues(horizontal = 20.dp, vertical = 20.dp),
                ) {
                    // 헤더
                    item {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(bottom = 20.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically,
                        ) {
                            Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                                Text(
                                    stock.name,
                                    style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.SemiBold),
                                    color = TextPrimary,
                                )
                                Text(
                                    "총 ${stock.containedEtfs.size}개의 ETF에 편입됨",
                                    style = MaterialTheme.typography.bodySmall.copy(fontSize = 13.sp),
                                    color = TextSecondary,
                                )
                            }
                            Box(
                                contentAlignment = Alignment.Center,
                                modifier = Modifier
                                    .size(44.dp)
                                    .clip(CircleShape)
                                    .background(SurfaceVariant),
                            ) {
                                Icon(
                                    Icons.Outlined.AccountBalance,
                                    contentDescription = null,
                                    tint = TextSecondary,
                                    modifier = Modifier.size(22.dp),
                                )
                            }
                        }
                    }

                    // ETF 목록
                    items(stock.containedEtfs) { etf ->
                        EtfListCard(etf = etf, onClick = { onEtfClick(etf.ticker) })
                        HorizontalDivider(color = Divider)
                    }
                }
            }

            is UiState.Error -> Box(
                Modifier.fillMaxSize().padding(innerPadding),
                contentAlignment = Alignment.Center,
            ) { Text(state.message, color = TextSecondary) }

            UiState.Idle -> Unit
        }
    }
}

@Composable
private fun EtfListCard(etf: StockEtf, onClick: () -> Unit) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
            .padding(vertical = 20.dp),
        verticalArrangement = Arrangement.spacedBy(6.dp),
    ) {
        // 이름 행
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.Top,
        ) {
            Text(etf.name, style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.SemiBold), color = TextPrimary)
            Text("비중", style = MaterialTheme.typography.bodySmall.copy(fontSize = 11.sp), color = TextSecondary)
        }
        // 운용사·티커 + 비중 숫자 행
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Text("${etf.manager} · ${etf.ticker}", style = MaterialTheme.typography.bodySmall, color = TextSecondary)
            Text(
                "${"%.1f".format(etf.weight)}%",
                style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.SemiBold),
                color = TextPrimary,
            )
        }
        // 프로그레스바
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(4.dp)
                .clip(RoundedCornerShape(2.dp))
                .background(SurfaceVariant),
        ) {
            Box(
                modifier = Modifier
                    .fillMaxWidth((etf.weight / 100.0).toFloat().coerceIn(0f, 1f))
                    .fillMaxHeight()
                    .clip(RoundedCornerShape(2.dp))
                    .background(PrimaryGreen),
            )
        }
    }
}
