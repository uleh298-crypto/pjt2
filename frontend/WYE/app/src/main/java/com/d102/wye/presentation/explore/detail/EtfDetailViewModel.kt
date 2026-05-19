package com.d102.wye.presentation.explore.detail

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.d102.wye.domain.common.BaseResult
import com.d102.wye.domain.model.*
import com.d102.wye.domain.repository.EtfRepository
import com.d102.wye.domain.repository.IndexRepository
import com.d102.wye.domain.repository.UserRepository
import timber.log.Timber
import com.d102.wye.presentation.model.UiState
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import java.time.DayOfWeek
import java.time.LocalTime
import java.time.ZoneId
import java.time.ZonedDateTime
import java.time.format.DateTimeFormatter
import javax.inject.Inject

@HiltViewModel
class EtfDetailViewModel @Inject constructor(
    private val etfRepository: EtfRepository,
    private val indexRepository: IndexRepository,
    private val userRepository: UserRepository,
    savedStateHandle: SavedStateHandle,
) : ViewModel() {

    val ticker: String = checkNotNull(savedStateHandle["ticker"])

    private val _detailState = MutableStateFlow<UiState<EtfDetail>>(UiState.Loading)
    val detailState: StateFlow<UiState<EtfDetail>> = _detailState.asStateFlow()

    private val _clusterState = MutableStateFlow<UiState<EtfClusterData>>(UiState.Loading)
    val clusterState: StateFlow<UiState<EtfClusterData>> = _clusterState.asStateFlow()

    private val _chartState = MutableStateFlow<UiState<EtfReturnChart>>(UiState.Idle)
    val chartState: StateFlow<UiState<EtfReturnChart>> = _chartState.asStateFlow()

    private val _periodReturn = MutableStateFlow<UiState<EtfPeriodReturn>>(UiState.Idle)
    val periodReturn: StateFlow<UiState<EtfPeriodReturn>> = _periodReturn.asStateFlow()

    private val _selectedPeriod = MutableStateFlow("ALL")
    val selectedPeriod: StateFlow<String> = _selectedPeriod.asStateFlow()

    private val _startDateMs        = MutableStateFlow<Long?>(null)
    private val _endDateMs          = MutableStateFlow<Long?>(null)
    private val _periodDurationDays = MutableStateFlow<Int?>(null)
    val startDateMs:        StateFlow<Long?> = _startDateMs.asStateFlow()
    val endDateMs:          StateFlow<Long?> = _endDateMs.asStateFlow()
    val periodDurationDays: StateFlow<Int?>  = _periodDurationDays.asStateFlow()

    private val _showNav    = MutableStateFlow(true)
    private val _showPrice  = MutableStateFlow(true)
    private val _showKospi  = MutableStateFlow(true)
    private val _showSp500  = MutableStateFlow(true)
    val showNav:   StateFlow<Boolean> = _showNav.asStateFlow()
    val showPrice: StateFlow<Boolean> = _showPrice.asStateFlow()
    val showKospi: StateFlow<Boolean> = _showKospi.asStateFlow()
    val showSp500: StateFlow<Boolean> = _showSp500.asStateFlow()

    private val _marketData = MutableStateFlow<EtfMarketData?>(null)
    val marketData: StateFlow<EtfMarketData?> = _marketData.asStateFlow()

    private val _marketStatusLabel = MutableStateFlow("")
    val marketStatusLabel: StateFlow<String> = _marketStatusLabel.asStateFlow()

    private val _isLiked = MutableStateFlow(false)
    val isLiked: StateFlow<Boolean> = _isLiked.asStateFlow()

    private var pollingJob: Job? = null

    init {
        loadDetail()
        loadCluster()
        loadPeriodReturn()
        loadLikeState()
        startPolling()
    }

    fun startPolling() {
        pollingJob?.cancel()
        pollingJob = viewModelScope.launch {
            while (true) {
                when (val result = etfRepository.getMarketData(ticker)) {
                    is BaseResult.Success -> {
                        _marketData.update { result.data }
                        _detailState.update { state ->
                            if (state is UiState.Success) {
                                UiState.Success(state.data.copy(
                                    currentPrice = result.data.currentPrice,
                                    volume = result.data.volume,
                                    dailyFluctuationRatio = result.data.dailyReturn,
                                ))
                            } else state
                        }
                    }
                    is BaseResult.Error -> Unit
                }
                _marketStatusLabel.update { marketStatusLabel() }
                delay(60_000L)
            }
        }
    }

    fun stopPolling() { pollingJob?.cancel() }

    private fun loadLikeState() {
        viewModelScope.launch {
            when (val result = userRepository.checkFavoriteEtf(ticker)) {
                is BaseResult.Success -> _isLiked.update { result.data }
                is BaseResult.Error   -> Unit
            }
        }
    }

    fun onLikeToggled() {
        viewModelScope.launch {
            val result = if (_isLiked.value) {
                userRepository.deleteFavoriteEtf(ticker)
            } else {
                userRepository.addFavoriteEtf(ticker)
            }
            if (result is BaseResult.Success) {
                _isLiked.update { !it }
            }
        }
    }

    private fun isMarketOpen(): Boolean {
        val now = ZonedDateTime.now(ZoneId.of("Asia/Seoul"))
        if (now.dayOfWeek == DayOfWeek.SATURDAY || now.dayOfWeek == DayOfWeek.SUNDAY) return false
        val t = now.toLocalTime()
        return t >= LocalTime.of(9, 0) && t <= LocalTime.of(15, 30)
    }

    private fun marketStatusLabel(): String {
        val now = ZonedDateTime.now(ZoneId.of("Asia/Seoul"))
        return if (isMarketOpen()) {
            now.format(DateTimeFormatter.ofPattern("yy.MM.dd HH:mm")) + " 기준"
        } else {
            var date = now.toLocalDate()
            if (now.toLocalTime() < LocalTime.of(9, 0)) date = date.minusDays(1)
            while (date.dayOfWeek == DayOfWeek.SATURDAY || date.dayOfWeek == DayOfWeek.SUNDAY) {
                date = date.minusDays(1)
            }
            date.format(DateTimeFormatter.ofPattern("yy.MM.dd")) + " 종가 기준"
        }
    }

    fun loadDetail() {
        viewModelScope.launch {
            _detailState.update { UiState.Loading }
            when (val result = etfRepository.getEtfDetail(ticker)) {
                is BaseResult.Success -> {
                    _detailState.update { UiState.Success(result.data) }
                    loadChart()
                }
                is BaseResult.Error   -> _detailState.update { UiState.Error(result.error.message) }
            }
        }
    }

    fun loadCluster() {
        viewModelScope.launch {
            _clusterState.update { UiState.Loading }
            when (val result = etfRepository.getEtfCluster(ticker)) {
                is BaseResult.Success -> _clusterState.update { UiState.Success(result.data) }
                is BaseResult.Error   -> _clusterState.update { UiState.Error(result.error.message) }
            }
        }
    }

    fun onPeriodSelected(period: String) {
        _selectedPeriod.update { period }
        val today = System.currentTimeMillis()
        val durationDays = when (period) {
            "1W" -> 7; "1M" -> 30; "3M" -> 90; "1Y" -> 365; "3Y" -> 3 * 365; else -> null
        }
        _periodDurationDays.update { durationDays }
        // 오늘 기준 → 과거로 기간 설정
        _endDateMs.update { today }
        _startDateMs.update {
            if (durationDays != null) {
                today - durationDays.toLong() * 86_400_000L
            } else {
                // ALL: 상장일부터 오늘까지
                (_detailState.value as? UiState.Success)?.data?.listingDate?.let { dateStringToMs(it) }
            }
        }
        loadChart()
    }

    fun onDateRangeSelected(start: Long, end: Long) {
        _startDateMs.update { start }
        _endDateMs.update { end }
        _selectedPeriod.update { "" }
        _periodDurationDays.update { null }
    }

    fun loadChart() {
        viewModelScope.launch {
            _chartState.update { UiState.Loading }
            val startMs = _startDateMs.value ?: run {
                if (_selectedPeriod.value == "ALL") {
                    val listingDate = (_detailState.value as? UiState.Success)?.data?.listingDate
                    if (listingDate != null) dateStringToMs(listingDate)
                    else System.currentTimeMillis() - 20L * 365 * 86_400_000L
                } else return@launch
            }
            val endMs     = _endDateMs.value   ?: System.currentTimeMillis()
            val startDate = msToDateString(startMs)
            val endDate   = msToDateString(endMs)
            val calDays   = ((endMs - startMs) / 86_400_000).toInt().coerceAtLeast(1)
            val size      = (calDays * 1.5 + 20).toInt().coerceIn(10, 5000)

            // ETF 가격 이력 (NAV + 종가)
            val etfResult = etfRepository.getEtfPriceHistory(ticker, startDate, endDate, size)
            if (etfResult is BaseResult.Error) {
                _chartState.update { UiState.Error(etfResult.error.message) }
                return@launch
            }
            val points = (etfResult as BaseResult.Success).data.sortedBy { it.date }
            if (points.isEmpty()) {
                _chartState.update { UiState.Error("해당 기간의 데이터가 없습니다.") }
                return@launch
            }
            val baseNav   = points.first().nav.takeIf { it > 0 } ?: 1.0
            val basePrice = points.first().stockPrice.toDouble().takeIf { it > 0 } ?: 1.0

            // 체크된 지수 순차 요청
            val kospiData = if (_showKospi.value) {
                fetchIndexChartPoints("KOSPI", startDate, endDate)
            } else emptyList()

            val nasdaqData = if (_showSp500.value) {
                fetchIndexChartPoints("NASDAQ", startDate, endDate)
            } else emptyList()

            _chartState.update {
                UiState.Success(EtfReturnChart(
                    navData   = points.map { ChartPoint(it.date, (it.nav - baseNav) / baseNav * 100.0) },
                    priceData = points.map { ChartPoint(it.date, (it.stockPrice.toDouble() - basePrice) / basePrice * 100.0) },
                    kospiData = kospiData,
                    sp500Data = nasdaqData,
                ))
            }
        }
    }

    private suspend fun fetchIndexChartPoints(
        marketType: String,
        startDate:  String,
        endDate:    String,
    ): List<ChartPoint> {
        return when (val result = indexRepository.getIndex(marketType, startDate, endDate)) {
            is BaseResult.Success -> {
                val pts = result.data
                Timber.d("$marketType 데이터: ${pts.size}개, 첫날=${pts.firstOrNull()?.date}, 마지막=${pts.lastOrNull()?.date}")
                val sorted = pts.sortedBy { it.date }
                val base = sorted.firstOrNull()?.close?.takeIf { it > 0 } ?: return emptyList()
                sorted.map { ChartPoint(it.date, (it.close - base) / base * 100.0) }
            }
            is BaseResult.Error -> {
                Timber.e("$marketType 인덱스 로드 실패: ${result.error.message}")
                emptyList()
            }
        }
    }

    fun toggleNav()   { _showNav.update   { !it }; loadChart() }
    fun togglePrice() { _showPrice.update { !it }; loadChart() }
    fun toggleKospi() { _showKospi.update { !it }; loadChart() }
    fun toggleSp500() { _showSp500.update { !it }; loadChart() }

    private fun loadPeriodReturn() {
        viewModelScope.launch {
            val startDate = dateStringMonthsAgo(6)
            val today = todayString()
            when (val result = etfRepository.getEtfPriceHistory(ticker, startDate, today, size = 200)) {
                is BaseResult.Success -> {
                    val points = result.data.sortedBy { it.date }
                    if (points.isEmpty()) return@launch
                    _periodReturn.update {
                        UiState.Success(EtfPeriodReturn(
                            asOfDate = points.last().date,
                            nav1M   = calcReturn(points, 30,  isNav = true),
                            nav3M   = calcReturn(points, 90,  isNav = true),
                            nav6M   = calcReturn(points, 180, isNav = true),
                            index1M = 0.0, index3M = 0.0, index6M = 0.0,
                            price1M = calcReturn(points, 30,  isNav = false),
                            price3M = calcReturn(points, 90,  isNav = false),
                            price6M = calcReturn(points, 180, isNav = false),
                        ))
                    }
                }
                is BaseResult.Error -> Unit
            }
        }
    }

    private fun todayString(): String {
        val c = java.util.Calendar.getInstance()
        return "%04d-%02d-%02d".format(c.get(java.util.Calendar.YEAR), c.get(java.util.Calendar.MONTH) + 1, c.get(java.util.Calendar.DAY_OF_MONTH))
    }

    private fun dateStringToMs(date: String): Long {
        val parts = date.split("-")
        return java.util.Calendar.getInstance().apply {
            set(parts[0].toInt(), parts[1].toInt() - 1, parts[2].toInt(), 0, 0, 0)
            set(java.util.Calendar.MILLISECOND, 0)
        }.timeInMillis
    }

    private fun msToDateString(ms: Long): String {
        val c = java.util.Calendar.getInstance().apply { timeInMillis = ms }
        return "%04d-%02d-%02d".format(c.get(java.util.Calendar.YEAR), c.get(java.util.Calendar.MONTH) + 1, c.get(java.util.Calendar.DAY_OF_MONTH))
    }

    private fun dateStringMonthsAgo(months: Int): String {
        val c = java.util.Calendar.getInstance().apply { add(java.util.Calendar.MONTH, -months) }
        return "%04d-%02d-%02d".format(c.get(java.util.Calendar.YEAR), c.get(java.util.Calendar.MONTH) + 1, c.get(java.util.Calendar.DAY_OF_MONTH))
    }

    private fun periodStartDate(period: String): String {
        val c = java.util.Calendar.getInstance()
        when (period) {
            "1W"  -> c.add(java.util.Calendar.WEEK_OF_YEAR, -1)
            "1M"  -> c.add(java.util.Calendar.MONTH, -1)
            "3M"  -> c.add(java.util.Calendar.MONTH, -3)
            "1Y"  -> c.add(java.util.Calendar.YEAR, -1)
            "3Y"  -> c.add(java.util.Calendar.YEAR, -3)
            "ALL" -> {
                val listingDate = (_detailState.value as? UiState.Success)?.data?.listingDate
                if (listingDate != null) return listingDate
                c.add(java.util.Calendar.YEAR, -20)
            }
        }
        return "%04d-%02d-%02d".format(c.get(java.util.Calendar.YEAR), c.get(java.util.Calendar.MONTH) + 1, c.get(java.util.Calendar.DAY_OF_MONTH))
    }

    private fun periodSize(period: String) = when (period) {
        "1W" -> 10; "1M" -> 35; "3M" -> 100; "1Y" -> 300; "3Y" -> 1000; else -> 5000
    }

    private fun calcReturn(points: List<EtfPriceData>, daysAgo: Int, isNav: Boolean): Double {
        if (points.size < 2) return 0.0
        val targetMs = System.currentTimeMillis() - daysAgo.toLong() * 86_400_000L
        val past = points.minByOrNull { kotlin.math.abs(dateStringToMs(it.date) - targetMs) } ?: return 0.0
        val last = points.last()
        val pastVal = if (isNav) past.nav else past.stockPrice.toDouble()
        val lastVal  = if (isNav) last.nav else last.stockPrice.toDouble()
        if (pastVal == 0.0) return 0.0
        return (lastVal - pastVal) / pastVal * 100.0
    }
}
