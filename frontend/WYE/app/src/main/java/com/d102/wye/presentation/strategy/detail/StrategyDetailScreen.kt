package com.d102.wye.presentation.strategy.detail

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.d102.wye.presentation.designsystem.WyePrimaryButton
import com.d102.wye.presentation.designsystem.WyeTopBar
import com.d102.wye.presentation.model.UiState
import com.d102.wye.presentation.strategy.detail.components.SummaryMetricsRow
import com.d102.wye.presentation.theme.BackGroundLightGreen2
import com.d102.wye.presentation.theme.PrimaryGreen

@Composable
fun StrategyDetailScreen(
    onBackClick: () -> Unit,
    onNewsClick: (Long) -> Unit = {},
    viewModel: StrategyDetailViewModel = hiltViewModel()
) {
    val detailState by viewModel.detailState.collectAsStateWithLifecycle()
    val snackbarHostState = remember { SnackbarHostState() }

    LaunchedEffect(detailState) {
        if (detailState is UiState.Error) {
            snackbarHostState.showSnackbar(
                message = (detailState as UiState.Error).message
            )
        }
    }

    StrategyDetailScreenContent(
        detailState = detailState,
        snackbarHostState = snackbarHostState,
        onBackClick = onBackClick,
        onNewsClick = onNewsClick
    )
}

@Composable
private fun StrategyDetailScreenContent(
    detailState: UiState<StrategyDetailData>,
    snackbarHostState: SnackbarHostState,
    onBackClick: () -> Unit,
    onNewsClick: (Long) -> Unit = {}
) {
    val scrollState = rememberScrollState()

    val isScrolled by remember { derivedStateOf { scrollState.value > 150 } }

    val topBarTitle = (detailState as? UiState.Success)?.data?.title ?: ""

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(BackGroundLightGreen2)
    ) {
        WyeTopBar(
            title = if (isScrolled) topBarTitle else "전략 상세 보기",
            onBackClick = onBackClick,
            backgroundColor = BackGroundLightGreen2
        )

        Box(
            modifier = Modifier.fillMaxSize()
        ) {
            when (val state = detailState) {
                is UiState.Success -> {
                    Column(
                        modifier = Modifier
                            .fillMaxSize()
                            .verticalScroll(scrollState),
                        verticalArrangement = Arrangement.spacedBy(16.dp)
                    ) {
                        SummaryMetricsRow(state.data)

                        PerformanceSection(state.data.recentPerformance, isMain = true)

                        PerformanceSection(state.data.pastPerformance, isMain = false)

                        TimelineSection(state.data.timelines)

                        NewsSection(state.data.relatedNews, onNewsClick = onNewsClick)

                        WyePrimaryButton(
                            text = "닫기",
                            onClick = onBackClick,
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = 24.dp)
                                .padding(bottom = 24.dp)
                        )
                    }
                }

                is UiState.Loading -> {
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                    ) {
                        CircularProgressIndicator(
                            color = PrimaryGreen,
                            modifier = Modifier.align(Alignment.Center)
                        )
                    }
                }

                else -> Unit
            }

            SnackbarHost(
                hostState = snackbarHostState,
                modifier = Modifier.align(Alignment.BottomCenter)
            )
        }
    }
}