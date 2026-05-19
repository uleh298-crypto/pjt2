package com.d102.wye.presentation.strategy.compare

import androidx.compose.ui.graphics.Color
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.d102.wye.domain.common.BaseResult
import com.d102.wye.domain.model.BacktestPoint
import com.d102.wye.domain.repository.PortfolioRepository
import com.d102.wye.domain.repository.SimulationRepository
import com.d102.wye.domain.usecase.portfolio.CalculatePortfolioChartUseCase
import com.d102.wye.presentation.model.UiState
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import timber.log.Timber
import java.time.LocalDate
import javax.inject.Inject
import kotlin.math.sqrt

enum class ComparePeriod(val label: String, val months: Long) {
    THREE_MONTHS("3개월", 3),
    ONE_YEAR("1년", 12),
    THREE_YEARS("3년", 36)
}

@HiltViewModel
class StrategyCompareViewModel @Inject constructor(
    private val portfolioRepository: PortfolioRepository,
    private val simulationRepository: SimulationRepository,
    private val calculatePortfolioChart: CalculatePortfolioChartUseCase
) : ViewModel() {

    private val palette = listOf(
//        Color(0xFF3A6E45), // 1. 기본 초록 (Primary)
//        Color(0xFFE8924A), // 2. 부드러운 오렌지
//        Color(0xFF5B8FA8), // 3. 차분한 블루
        Color(0xFFE56B6F), // 4. 인디 핑크
        Color(0xFF6D597A), // 5. 뮤트 퍼플
//        Color(0xFF8AB17D), // 6. 라이트 그린
//        Color(0xFFE1B07E), // 7. 샌드 옐로우
        Color(0xFF457B9D), // 8. 스틸 블루
//        Color(0xFF9E2A2B), // 9. 다크 레드
//        Color(0xFF2A9D8F)  // 10. 틸(Teal)
    )

    private val _uiState = MutableStateFlow<UiState<CompareData>>(UiState.Idle)
    val uiState: StateFlow<UiState<CompareData>> = _uiState.asStateFlow()

    private val _compareResultState = MutableStateFlow<UiState<CompareResultData>>(UiState.Idle)
    val compareResultState: StateFlow<UiState<CompareResultData>> =
        _compareResultState.asStateFlow()

    private val _selectedPeriod = MutableStateFlow(ComparePeriod.ONE_YEAR)
    val selectedPeriod: StateFlow<ComparePeriod> = _selectedPeriod.asStateFlow()

    private val _snackbarEvent = MutableSharedFlow<String>()
    val snackbarEvent: SharedFlow<String> = _snackbarEvent.asSharedFlow()

    init {
        loadPortfolioList()
    }

    private fun loadPortfolioList() {
        viewModelScope.launch {
            _uiState.value = UiState.Loading
            when (val result = portfolioRepository.getPortfolioList()) {
                is BaseResult.Success -> {
                    Timber.d("[Compare] 포트폴리오 목록 조회 성공 | count=${result.data.size}")

                    val sortedList = result.data.sortedByDescending { it.isMyData }

                    _uiState.value = UiState.Success(
                        CompareData(strategyList = sortedList.map {
                            CompareStrategyItem(
                                id = it.portfolioId,
                                name = it.title,
                                isMyData = it.isMyData,
                                etfNames = it.etfList.map { etf -> etf.name }
                            )
                        })
                    )
                }

                is BaseResult.Error -> {
                    Timber.e("[Compare] 포트폴리오 목록 조회 실패 | ${result.error.message}")
                    _uiState.value = UiState.Error(result.error.message)
                }
            }
        }
    }

    fun toggleSelection(id: Long) {
        val current = (_uiState.value as? UiState.Success)?.data ?: return

        val list = current.strategyList
        val selectedCount = list.count { it.isSelected }
        val target = list.find { it.id == id } ?: return

        if (!target.isSelected && selectedCount >= 3) {
            viewModelScope.launch {
                _snackbarEvent.emit("최대 3개까지만 비교할 수 있습니다.")
            }
            return // 상태 업데이트 하지 않고 종료
        }

        // 정상적으로 선택/해제 상태 업데이트
        _uiState.update {
            val updated = list.map { item ->
                if (item.id == id) item.copy(isSelected = !item.isSelected) else item
            }.let { updatedList ->
                var colorIndex = 0
                updatedList.map {
                    if (it.isSelected) it.copy(color = palette[colorIndex++ % palette.size])
                    else it.copy(color = Color.Transparent)
                }
            }
            UiState.Success(current.copy(strategyList = updated))
        }

        // 데이터 갱신 로직 (기존과 동일)
        val selectedItems = (_uiState.value as? UiState.Success)?.data
            ?.strategyList?.filter { it.isSelected } ?: return

        if (selectedItems.size >= 2) fetchCompareResult(selectedItems, _selectedPeriod.value)
        else _compareResultState.value = UiState.Idle
    }

    fun onPeriodSelected(period: ComparePeriod) {
        _selectedPeriod.value = period
        val selectedItems = (_uiState.value as? UiState.Success)?.data
            ?.strategyList?.filter { it.isSelected } ?: return
        if (selectedItems.size >= 2) fetchCompareResult(selectedItems, period)
    }

    private fun fetchCompareResult(
        selectedItems: List<CompareStrategyItem>,
        period: ComparePeriod
    ) {
        viewModelScope.launch {
            _compareResultState.value = UiState.Loading

            val today = LocalDate.now()
            val endDate = today.toString()
            val periodStartDate = today.minusMonths(period.months).toString()

            Timber.d("[Compare] period=${period.label} | $periodStartDate ~ $endDate")

            val results = selectedItems.map { item ->
                async {
                    // 1. 포트폴리오 상세
                    val detail = when (val r = portfolioRepository.getPortfolioDetail(item.id)) {
                        is BaseResult.Success -> r.data
                        is BaseResult.Error -> {
                            Timber.e("[Compare] 상세 조회 실패 | id=${item.id}")
                            return@async null
                        }
                    }

                    val tickers = detail.counts.map { it.ticker }

                    // 2. 가격이력 증분 업데이트
                    tickers.forEach { ticker ->
                        val lastCachedDate = simulationRepository.getLastCachedDate(ticker)
                        val needsFetch = lastCachedDate == null || lastCachedDate < endDate

                        if (needsFetch) {
                            val fetchStart = if (lastCachedDate != null) {
                                LocalDate.parse(lastCachedDate).plusDays(1).toString()
                            } else {
                                today.minusYears(3).toString()
                            }
                            Timber.d("[Compare] 증분 조회 | ticker=$ticker | $fetchStart ~ $endDate")

                            when (val r = simulationRepository.getEtfPriceHistories(
                                tickers = listOf(ticker),
                                startDate = fetchStart,
                                endDate = endDate
                            )) {
                                is BaseResult.Success -> simulationRepository.savePriceHistories(r.data)
                                is BaseResult.Error -> Timber.e("[Compare] 가격이력 실패 | ticker=$ticker")
                            }
                        }
                    }

                    val priceHistories = simulationRepository.getCachedPriceHistories(tickers)

                    // 3. 차트 계산 (createdAt 자리에 기간 시작일 넣기)
                    val chartResult = calculatePortfolioChart(
                        counts = detail.counts,
                        priceHistories = priceHistories,
                        createdAt = periodStartDate
                    )

                    Timber.d("[Compare] id=${item.id} | recentPoints=${chartResult.recentPoints.size}개")

                    // 4. 지표 계산
                    val points = chartResult.recentPoints
                    val totalReturnRate = chartResult.recentReturn
                    val volatility = calcVolatility(points)
                    val sharpeRatio = calcSharpeRatio(points)

                    CompareCalculatedResult(
                        item,
                        chartResult,
                        totalReturnRate,
                        volatility,
                        sharpeRatio
                    )
                }
            }.awaitAll().filterNotNull()

            if (results.isEmpty()) {
                _compareResultState.value = UiState.Error("계산 실패")
                return@launch
            }

            val ranked = results.sortedByDescending { it.totalReturnRate }
                .mapIndexed { index, r -> r to (index + 1) }

            _compareResultState.value = UiState.Success(
                CompareResultData(
                    chartLines = ranked.map { (r, _) ->
                        CompareChartLine(
                            id = r.item.id,
                            name = r.item.name,
                            color = r.item.color,
                            recentPoints = r.chartResult.recentPoints,
                            pastPoints = r.chartResult.pastPoints
                        )
                    },
                    tableItems = ranked.map { (r, rank) ->
                        CompareDetailStat(
                            id = r.item.id,
                            name = r.item.name,
                            color = r.item.color,
                            totalReturnRate = "${if (r.totalReturnRate >= 0) "+" else ""}${
                                "%.2f".format(
                                    r.totalReturnRate
                                )
                            }%",
                            volatility = "${"%.2f".format(r.volatility)}%",
                            sharpeRatio = "%.2f".format(r.sharpeRatio),
                            rank = rank
                        )
                    }
                )
            )
        }
    }

    private fun calcVolatility(points: List<BacktestPoint>): Double {
        if (points.size < 2) return 0.0
        val n = points.size - 1
        val dailyReturns = (1 until points.size).map { i ->
            val prev = points[i - 1].value
            val curr = points[i].value
            (curr - prev) / (100.0 + prev)
        }
        if (n < 2) return 0.0
        val mean = dailyReturns.average()
        val sigma = sqrt(dailyReturns.sumOf { (it - mean) * (it - mean) } / (n - 1))
        return sigma * sqrt(n.toDouble()) * 100.0  // 기간 연율화 %
    }

    private fun calcSharpeRatio(points: List<BacktestPoint>): Double {
        if (points.size < 2) return 0.0
        val n = points.size - 1
        val dailyReturns = (1 until points.size).map { i ->
            val prev = points[i - 1].value
            val curr = points[i].value
            (curr - prev) / (100.0 + prev)
        }
        if (n < 2) return 0.0
        val mean = dailyReturns.average()
        val rfDaily = 0.0299 / 252
        val sigma = sqrt(dailyReturns.sumOf { (it - mean) * (it - mean) } / (n - 1))
        if (sigma == 0.0) return 0.0
        val dailySharpe = (mean - rfDaily) / sigma
        return dailySharpe * sqrt(n.toDouble())
    }
}

private data class CompareCalculatedResult(
    val item: CompareStrategyItem,
    val chartResult: CalculatePortfolioChartUseCase.Result,
    val totalReturnRate: Double,
    val volatility: Double,
    val sharpeRatio: Double
)

data class CompareData(val strategyList: List<CompareStrategyItem>)

data class CompareStrategyItem(
    val id: Long,
    val name: String,
    val isSelected: Boolean = false,
    val color: Color = Color.Transparent,
    val isMyData: Boolean = false,
    val etfNames: List<String> = emptyList()
)

data class CompareResultData(
    val chartLines: List<CompareChartLine>,
    val tableItems: List<CompareDetailStat>
)

data class CompareChartLine(
    val id: Long,
    val name: String,
    val color: Color,
    val recentPoints: List<BacktestPoint>,
    val pastPoints: List<BacktestPoint>
)

data class CompareDetailStat(
    val id: Long,
    val name: String,
    val color: Color,
    val totalReturnRate: String,
    val volatility: String,
    val sharpeRatio: String,
    val rank: Int
)