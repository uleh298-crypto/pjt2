package com.d102.wye.presentation.explore.list

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.d102.wye.domain.common.BaseResult
import com.d102.wye.domain.model.Etf
import com.d102.wye.domain.model.EtfFilter
import com.d102.wye.domain.repository.EtfRepository
import com.d102.wye.domain.repository.UserRepository
import com.d102.wye.domain.state.EtfFilterState
import com.d102.wye.presentation.model.EtfListItemUiModel
import com.d102.wye.presentation.model.UiState
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import timber.log.Timber
import java.time.DayOfWeek
import java.time.LocalTime
import java.time.ZoneId
import java.time.ZonedDateTime
import java.time.format.DateTimeFormatter
import javax.inject.Inject

@HiltViewModel
class ExploreViewModel @Inject constructor(
    private val etfRepository: EtfRepository,
    private val userRepository: UserRepository,
) : ViewModel() {

    private val _uiState = MutableStateFlow<UiState<ExploreData>>(UiState.Idle)
    val uiState: StateFlow<UiState<ExploreData>> = _uiState.asStateFlow()

    private val _filterState = MutableStateFlow(EtfFilterState())
    val filterState: StateFlow<EtfFilterState> = _filterState.asStateFlow()

    private val _isLoadingMore = MutableStateFlow(false)
    val isLoadingMore: StateFlow<Boolean> = _isLoadingMore.asStateFlow()

    private val _sortedBy = MutableStateFlow<String?>("volume")
    val sortedBy: StateFlow<String?> = _sortedBy.asStateFlow()

    private var rawEtfList: List<EtfListItemUiModel> = emptyList()
    private var currentPage = 0
    private var isLastPage = false
    private var isDataInitialized = false

    private val _selectedTickers = MutableStateFlow<Set<SelectedEtf>>(emptySet())
    val selectedTickers = _selectedTickers.asStateFlow()

    private val _expandedFilterSections = MutableStateFlow<Set<String>>(emptySet())
    val expandedFilterSections: StateFlow<Set<String>> = _expandedFilterSections.asStateFlow()

    private val _marketStatusLabel = MutableStateFlow("")
    val marketStatusLabel: StateFlow<String> = _marketStatusLabel.asStateFlow()

    private var pollingJob: Job? = null
    private var searchDebounceJob: Job? = null

    fun toggleFilterSection(title: String) {
        _expandedFilterSections.update { if (title in it) it - title else it + title }
    }

    init {
        observeFavoriteChanges()
        loadEtfList()
        startPolling()
    }

    fun startPolling() {
        pollingJob?.cancel()
        pollingJob = viewModelScope.launch {
            delay(60_000L) // 첫 로드는 loadEtfList()가 처리
            while (true) {
                refreshMarketData()
                delay(60_000L)
            }
        }
    }

    fun setInitialSelections(selections: List<SelectedEtf>) {
        if (_selectedTickers.value.isNotEmpty()) return

        viewModelScope.launch {
            _selectedTickers.update { selections.toSet() }
        }
    }

    private suspend fun refreshMarketData() {
        val current = (_uiState.value as? UiState.Success)?.data ?: return
        val updatedList = current.etfList.map { item ->
            when (val result = etfRepository.getMarketData(item.ticker)) {
                is BaseResult.Success -> item.copy(
                    currentPrice = result.data.currentPrice,
                    changeRate = result.data.dailyReturn,
                )
                is BaseResult.Error -> item
            }
        }
        _uiState.update { UiState.Success(current.copy(etfList = updatedList)) }
        _marketStatusLabel.update { marketStatusLabel() }
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

    private fun observeFavoriteChanges() {
        viewModelScope.launch {
            userRepository.favoriteEtfChanged.collectLatest {
                if (_filterState.value.onlyLiked) {
                    loadFavorites()
                } else if (rawEtfList.isNotEmpty()) {
                    loadEtfList(_filterState.value)
                }
            }
        }
    }

    fun loadEtfList(filter: EtfFilterState = _filterState.value) {
        viewModelScope.launch {
            _uiState.update { UiState.Loading }
            currentPage = 0
            isLastPage = false
            rawEtfList = emptyList()
            isDataInitialized = false

            when (val result = etfRepository.getEtfList(filter.toFilter(_sortedBy.value), page = 0)) {
                is BaseResult.Success -> {
                    isLastPage = result.data.isLast
                    Timber.d(
                        "[FavoriteEtf] /etfs page=0 loaded | sample=%s",
                        result.data.items.take(10).joinToString { "${it.ticker}:${it.isFavorite}" }
                    )
                    rawEtfList = result.data.items.map { it.toUiModel() }
                    isDataInitialized = true
                    applyFilter()
                    _marketStatusLabel.update { marketStatusLabel() }
                }
                is BaseResult.Error -> _uiState.update { UiState.Error(result.error.message) }
            }
        }
    }

    fun loadMore() {
        if (isLastPage || _isLoadingMore.value || _filterState.value.onlyLiked) return
        viewModelScope.launch {
            _isLoadingMore.update { true }
            val nextPage = currentPage + 1
            when (val result = etfRepository.getEtfList(_filterState.value.toFilter(_sortedBy.value), page = nextPage)) {
                is BaseResult.Success -> {
                    currentPage = nextPage
                    isLastPage = result.data.isLast
                    Timber.d(
                        "[FavoriteEtf] /etfs page=%d loaded | sample=%s",
                        nextPage,
                        result.data.items.take(10).joinToString { "${it.ticker}:${it.isFavorite}" }
                    )
                    rawEtfList = (rawEtfList + result.data.items.map { it.toUiModel() })
                        .distinctBy { it.ticker }
                    applyFilter()
                }
                is BaseResult.Error -> _uiState.update { UiState.Error(result.error.message) }
            }
            _isLoadingMore.update { false }
        }
    }

    fun onQueryChanged(query: String) {
        _filterState.update { it.copy(query = query) }
        searchDebounceJob?.cancel()
        searchDebounceJob = viewModelScope.launch {
            delay(300L)
            loadEtfList()
        }
    }

    fun onSearchScopeSelected(scope: String?) {
        _filterState.update { it.copy(searchScope = scope) }
        applyFilter()
    }

    fun onAssetClassSelected(assetClass: String?) {
        _filterState.update { it.copy(assetClass = assetClass) }
        applyFilter()
    }

    fun onSortChanged(sortedBy: String?) {
        _sortedBy.update { sortedBy }
        loadEtfList()
    }

    fun onFilterChanged(filter: EtfFilterState) {
        val prev = _filterState.value
        _filterState.update { filter }
        when {
            filter.onlyLiked && !prev.onlyLiked -> loadFavorites()
            !filter.onlyLiked && prev.onlyLiked -> loadEtfList(filter)
            filter.onlyLiked -> loadFavorites()
            else -> loadEtfList(filter)
        }
    }

    fun onLikeToggled(ticker: String) {
        val target = rawEtfList.firstOrNull { it.ticker == ticker } ?: return
        viewModelScope.launch {
            Timber.d(
                "[FavoriteEtf] toggle requested | etfId=${target.etfId} | ticker=${target.ticker} | isLiked=${target.isLiked}"
            )
            val result = if (target.isLiked) {
                userRepository.deleteFavoriteEtf(target.ticker)
            } else {
                userRepository.addFavoriteEtf(target.ticker)
            }

            when (result) {
                is BaseResult.Success -> {
                    Timber.d(
                        "[FavoriteEtf] toggle succeeded | etfId=${target.etfId} | ticker=${target.ticker} | newIsLiked=${!target.isLiked}"
                    )
                    rawEtfList = rawEtfList.map { item ->
                        if (item.ticker == ticker) item.copy(isLiked = !item.isLiked) else item
                    }
                    if (_filterState.value.onlyLiked) loadFavorites() else applyFilter()
                }
                is BaseResult.Error -> {
                    Timber.e(
                        "[FavoriteEtf] toggle failed | etfId=${target.etfId} | ticker=${target.ticker} | message=${result.error.message}"
                    )
                    _uiState.update { UiState.Error(result.error.message) }
                }
            }
        }
    }

    fun toggleSelection(ticker: String, name: String) {
        _selectedTickers.update { currentSet ->
            val existingItem = currentSet.find { it.ticker == ticker }
            if (existingItem != null) {
                currentSet - existingItem // 있으면 제거
            } else {
                currentSet + SelectedEtf(ticker, name) // 없으면 추가
            }
        }
    }

    fun removeSelection(ticker: String) {
        _selectedTickers.update { currentSet ->
            currentSet.filterNot { it.ticker == ticker }.toSet()
        }
    }

    fun clearSelection() {
        _selectedTickers.update { emptySet() }
    }

    private fun applyFilter() {
        if (!isDataInitialized) return
        val filter = _filterState.value
        _uiState.update {
            UiState.Success(ExploreData(etfList = rawEtfList, filteredList = rawEtfList, filter = filter))
        }
    }

    private fun loadFavorites() {
        viewModelScope.launch {
            _uiState.update { UiState.Loading }
            val filter = _filterState.value.toFilter(_sortedBy.value).copy(isFavorite = true)
            when (val result = etfRepository.getEtfList(filter, page = 0)) {
                is BaseResult.Success -> {
                    val favorites = result.data.items.map { it.toUiModel() }
                    _uiState.update {
                        UiState.Success(
                            ExploreData(
                                etfList = rawEtfList,
                                filteredList = favorites,
                                filter = _filterState.value
                            )
                        )
                    }
                }
                is BaseResult.Error -> _uiState.update { UiState.Error(result.error.message) }
            }
        }
    }
}

// ─── EtfFilterState → EtfFilter 변환 ────────────────────────────
private fun EtfFilterState.toFilter(sortedBy: String? = null) = EtfFilter(
    riskType = riskType,
    strategy = strategy,
    isDerivatives = hasDerivative,
    isLeverage = hasLeverage,
    isInverse = hasInverse,
    dividendYield = dividendRateRange?.toDoubleOrNull(),
    searchName = query.takeIf { it.isNotBlank() },
    sortedBy = sortedBy,
)

private fun Etf.toUiModel() = EtfListItemUiModel(
    etfId = etfId,
    ticker = ticker,
    name = name,
    currentPrice = currentPrice,
    changeRate = changeRate,
    changeAmount = changeAmount,
    riskType = riskType,
    isLiked = isFavorite,
)

data class ExploreData(
    val etfList: List<EtfListItemUiModel>,
    val filteredList: List<EtfListItemUiModel>,
    val filter: EtfFilterState,
)

data class SelectedEtf(
    val ticker: String,
    val name: String
)