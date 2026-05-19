package com.d102.wye.presentation.mypage.holding

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.d102.wye.presentation.designsystem.EtfListItem
import com.d102.wye.presentation.designsystem.WyeTopBar
import com.d102.wye.presentation.model.UiState
import com.d102.wye.presentation.theme.TextSecondary

@Composable
fun HoldingEtfListScreen(
    onBackClick: () -> Unit,
    onEtfClick: (ticker: String) -> Unit,
    viewModel: HoldingEtfListViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()

    Box(modifier = Modifier.fillMaxSize()) {
        androidx.compose.foundation.layout.Column(modifier = Modifier.fillMaxSize()) {
            WyeTopBar(
                title = "보유 ETF",
                onBackClick = onBackClick
            )

            when (val state = uiState) {
                is UiState.Loading -> Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    CircularProgressIndicator()
                }

                is UiState.Success -> {
                    when {
                        !state.data.isConnected -> EmptyHoldingState("마이데이터가 연동되어 있지 않습니다.")
                        state.data.holdings.isEmpty() -> EmptyHoldingState("보유하고 있는 ETF가 없습니다.")
                        else -> {
                            LazyColumn(modifier = Modifier.fillMaxSize()) {
                                items(state.data.holdings, key = { it.ticker }) { etf ->
                                    EtfListItem(
                                        name = etf.name,
                                        ticker = etf.ticker,
                                        currentPrice = etf.currentPrice,
                                        changeRate = etf.changeRate,
                                        changeAmount = etf.changeAmount,
                                        riskType = etf.riskType,
                                        supportingText = etf.quantityText,
                                        isLiked = false,
                                        showLikeButton = false,
                                        onClick = { onEtfClick(etf.ticker) }
                                    )
                                }
                            }
                        }
                    }
                }

                is UiState.Error -> EmptyHoldingState(state.message)
                UiState.Idle -> Unit
            }
        }
    }
}

@Composable
private fun EmptyHoldingState(message: String) {
    Box(
        modifier = Modifier.fillMaxSize(),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = message,
            color = TextSecondary,
            style = MaterialTheme.typography.bodyMedium
        )
    }
}
