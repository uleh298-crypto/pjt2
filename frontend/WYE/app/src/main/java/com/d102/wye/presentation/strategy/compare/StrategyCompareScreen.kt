package com.d102.wye.presentation.strategy.compare

import MultiLineChart
import androidx.compose.animation.animateContentSize
import androidx.compose.foundation.background
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
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.d102.wye.presentation.designsystem.WyePrimaryButton
import com.d102.wye.presentation.designsystem.WyeTopBar
import com.d102.wye.presentation.model.UiState
import com.d102.wye.presentation.strategy.compare.components.CompareTableSection
import com.d102.wye.presentation.strategy.compare.components.CompareTipSection
import com.d102.wye.presentation.strategy.compare.components.EmptyChartSection
import com.d102.wye.presentation.strategy.compare.components.EmptyTableSection
import com.d102.wye.presentation.strategy.compare.components.StrategySelectionRow
import com.d102.wye.presentation.strategy.detail.components.RoundedSurface
import com.d102.wye.presentation.theme.BackGroundLightGreen2
import com.d102.wye.presentation.theme.PrimaryGreen
import com.d102.wye.presentation.theme.SurfaceVariant
import com.d102.wye.presentation.theme.TextPrimary
import com.d102.wye.presentation.theme.TextSecondary
import kotlinx.coroutines.flow.collectLatest

@Composable
fun StrategyCompareScreen(
    onBackClick: () -> Unit,
    viewModel: StrategyCompareViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val compareResultState by viewModel.compareResultState.collectAsStateWithLifecycle()
    val selectedPeriod by viewModel.selectedPeriod.collectAsStateWithLifecycle()
    val snackbarHostState = remember { SnackbarHostState() }

    LaunchedEffect(uiState) {
        if (uiState is UiState.Error)
            snackbarHostState.showSnackbar((uiState as UiState.Error).message)
    }
    LaunchedEffect(compareResultState) {
        if (compareResultState is UiState.Error)
            snackbarHostState.showSnackbar((compareResultState as UiState.Error).message)
    }

    LaunchedEffect(Unit) {
        viewModel.snackbarEvent.collectLatest { message ->
            snackbarHostState.showSnackbar(message)
        }
    }

    StrategyCompareScreenContent(
        uiState = uiState,
        compareResultState = compareResultState,
        selectedPeriod = selectedPeriod,
        snackbarHostState = snackbarHostState,
        onBackClick = onBackClick,
        onToggleSelection = viewModel::toggleSelection,
        onPeriodSelected = viewModel::onPeriodSelected
    )
}

@Composable
private fun StrategyCompareScreenContent(
    uiState: UiState<CompareData>,
    compareResultState: UiState<CompareResultData>,
    selectedPeriod: ComparePeriod,
    snackbarHostState: SnackbarHostState,
    onBackClick: () -> Unit,
    onToggleSelection: (Long) -> Unit,
    onPeriodSelected: (ComparePeriod) -> Unit
) {
    // D. 로딩 깜빡임 방지: 마지막 성공 데이터 유지
    var lastSuccessData by remember { mutableStateOf<CompareResultData?>(null) }
    LaunchedEffect(compareResultState) {
        if (compareResultState is UiState.Success) {
            lastSuccessData = compareResultState.data
        }
    }
    val isRefreshing = compareResultState is UiState.Loading && lastSuccessData != null

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(BackGroundLightGreen2)
    ) {
        WyeTopBar(
            title = "전략 비교하기",
            backgroundColor = BackGroundLightGreen2,
            onBackClick = onBackClick
        )

        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(BackGroundLightGreen2)
        ) {
            when (val state = uiState) {
                is UiState.Loading -> {
                    Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                        CircularProgressIndicator(color = PrimaryGreen)
                    }
                }

                is UiState.Success -> {
                    Box(modifier = Modifier.fillMaxSize()) {
                        Column(
                            modifier = Modifier
                                .fillMaxSize()
                                .verticalScroll(rememberScrollState())
                                .padding(horizontal = 24.dp)
                        ) {
                            Spacer(modifier = Modifier.height(4.dp))

                            Text(
                                text = "내 포트폴리오",
                                style = MaterialTheme.typography.labelLarge,
                                color = TextPrimary
                            )

                            Spacer(modifier = Modifier.height(10.dp))

                            // C. 선택 행에 색상 동그라미 표시 (접기/펼치기 적용)
                            var isListExpanded by remember { mutableStateOf(false) }
                            val maxVisibleCount = 3

                            RoundedSurface(horizontalPaddingValue = 0.dp) {
                                Column(
                                    modifier = Modifier.animateContentSize()
                                ) {
                                    val displayList = if (isListExpanded) {
                                        state.data.strategyList
                                    } else {
                                        state.data.strategyList.take(maxVisibleCount)
                                    }

                                    displayList.forEach { item ->
                                        StrategySelectionRow(
                                            item = item,
                                            onClick = { onToggleSelection(item.id) },
                                            indicatorColor = if (item.isSelected) item.color else Color.Transparent
                                        )
                                    }

                                    if (state.data.strategyList.size > maxVisibleCount) {
                                        Box(
                                            modifier = Modifier
                                                .fillMaxWidth()
                                                .clickable { isListExpanded = !isListExpanded }
                                                .padding(vertical = 14.dp),
                                            contentAlignment = Alignment.Center
                                        ) {
                                            Text(
                                                text = if (isListExpanded) "접기" else "포트폴리오 더보기",
                                                style = MaterialTheme.typography.labelMedium,
                                                color = TextSecondary
                                            )
                                        }
                                    }
                                }
                            }

                            Spacer(modifier = Modifier.height(12.dp))

                            // B. 기간 선택 + 차트 통합 섹션
                            val displayData = if (compareResultState is UiState.Success) {
                                compareResultState.data
                            } else lastSuccessData

                            when {
                                compareResultState is UiState.Idle -> EmptyChartSection()
                                compareResultState is UiState.Loading && lastSuccessData == null -> {
                                    // 첫 로딩만 로딩 UI
                                    RoundedSurface(horizontalPaddingValue = 0.dp) {
                                        Box(
                                            modifier = Modifier
                                                .fillMaxWidth()
                                                .height(240.dp),
                                            contentAlignment = Alignment.Center
                                        ) {
                                            CircularProgressIndicator(
                                                color = PrimaryGreen,
                                                modifier = Modifier.size(32.dp)
                                            )
                                        }
                                    }
                                }

                                displayData != null -> {
                                    CompareChartSection(
                                        chartLines = displayData.chartLines,
                                        selectedPeriod = selectedPeriod,
                                        isRefreshing = isRefreshing,
                                        onPeriodSelected = onPeriodSelected
                                    )
                                }

                                else -> EmptyChartSection()
                            }

                            Spacer(modifier = Modifier.height(24.dp))


                            // D. 테이블도 마지막 데이터 유지
                            val tableData = displayData?.tableItems

                            if (tableData != null) {
                                CompareTableSection(items = tableData)
                            } else {
                                EmptyTableSection()
                                CompareTipSection()
                            }

                            Spacer(modifier = Modifier.weight(1f))

                            WyePrimaryButton(
                                text = "닫기",
                                modifier = Modifier.fillMaxWidth(),
                                onClick = onBackClick
                            )
                            Spacer(modifier = Modifier.height(24.dp))
                        }
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

@Composable
private fun CompareChartSection(
    chartLines: List<CompareChartLine>,
    selectedPeriod: ComparePeriod,
    isRefreshing: Boolean,
    onPeriodSelected: (ComparePeriod) -> Unit
) {
    RoundedSurface(horizontalPaddingValue = 4.dp) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            // 헤더 + 기간 선택 한 Row에
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "수익률 추이",
                    style = MaterialTheme.typography.labelLarge,
                    color = TextPrimary
                )

                // 기간 선택 탭
                Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                    ComparePeriod.entries.forEach { period ->
                        val isSelected = selectedPeriod == period
                        Text(
                            text = period.label,
                            modifier = Modifier
                                .background(
                                    color = if (isSelected) PrimaryGreen else SurfaceVariant,
                                    shape = RoundedCornerShape(16.dp)
                                )
                                .clickable { onPeriodSelected(period) }
                                .padding(horizontal = 10.dp, vertical = 5.dp),
                            color = if (isSelected) Color.White else TextSecondary,
                            style = MaterialTheme.typography.labelSmall
                        )
                    }
                }
            }

            // y축 라벨
            Text(
                text = "수익률 (%)",
                style = MaterialTheme.typography.labelSmall.copy(fontSize = 9.sp),
                color = TextSecondary.copy(alpha = 0.6f)
            )

            // 차트 (D: 리프레시 중에도 기존 차트 표시)
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(180.dp)
            ) {
                MultiLineChart(
                    modifier = Modifier.fillMaxSize(),
                    lines = chartLines
                )
                // 리프레시 중 살짝 딤처리
                if (isRefreshing) {
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .background(Color.White.copy(alpha = 0.5f)),
                        contentAlignment = Alignment.Center
                    ) {
                        CircularProgressIndicator(
                            color = PrimaryGreen,
                            modifier = Modifier.size(24.dp),
                            strokeWidth = 2.dp
                        )
                    }
                }
            }
        }
    }
}
