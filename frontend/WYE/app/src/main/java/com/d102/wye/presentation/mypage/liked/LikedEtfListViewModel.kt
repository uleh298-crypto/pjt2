package com.d102.wye.presentation.mypage.liked

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.d102.wye.domain.common.BaseResult
import com.d102.wye.domain.model.EtfDetail
import com.d102.wye.domain.model.FavoriteEtf
import com.d102.wye.domain.model.FavoriteEtfSort
import com.d102.wye.domain.repository.EtfRepository
import com.d102.wye.domain.repository.UserRepository
import com.d102.wye.presentation.model.EtfListItemUiModel
import com.d102.wye.presentation.model.UiState
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

@HiltViewModel
class LikedEtfListViewModel @Inject constructor(
    private val userRepository: UserRepository,
    private val etfRepository: EtfRepository,
) : ViewModel() {

    private val _uiState = MutableStateFlow<UiState<LikedEtfListData>>(UiState.Idle)
    val uiState: StateFlow<UiState<LikedEtfListData>> = _uiState.asStateFlow()

    private val _event = MutableSharedFlow<LikedEtfListEvent>()
    val event: SharedFlow<LikedEtfListEvent> = _event.asSharedFlow()

    private var currentSort = FavoriteEtfSort.RECENT

    init {
        loadLikedEtfs()
    }

    fun loadLikedEtfs(sort: FavoriteEtfSort = currentSort) {
        viewModelScope.launch {
            currentSort = sort
            _uiState.update { UiState.Loading }
            when (val result = userRepository.getFavoriteEtfs(sort)) {
                is BaseResult.Success -> {
                    val enrichedFavorites = enrichFavorites(result.data.favorites)
                    when (enrichedFavorites) {
                        is BaseResult.Success -> {
                            _uiState.update {
                                UiState.Success(
                                    LikedEtfListData(
                                        likedEtfs = enrichedFavorites.data,
                                        selectedSort = sort,
                                        totalCount = result.data.totalCount
                                    )
                                )
                            }
                        }
                        is BaseResult.Error -> {
                            _uiState.update { UiState.Error(enrichedFavorites.error.message) }
                        }
                    }
                }
                is BaseResult.Error -> _uiState.update { UiState.Error(result.error.message) }
            }
        }
    }

    private suspend fun enrichFavorites(favorites: List<FavoriteEtf>): BaseResult<List<EtfListItemUiModel>> =
        coroutineScope {
            val results = favorites.map { favorite ->
                async {
                    favorite.ticker to etfRepository.getEtfDetail(favorite.ticker)
                }
            }.awaitAll()

            val failed = results.firstOrNull { it.second is BaseResult.Error }
            if (failed != null) {
                val error = (failed.second as BaseResult.Error).error
                return@coroutineScope BaseResult.Error(error)
            }

            val etfMap = results.associate { (ticker, result) ->
                ticker to (result as BaseResult.Success).data.toSummaryEtf()
            }

            BaseResult.Success(
                favorites.map { favorite ->
                    val etf = etfMap[favorite.ticker]
                    favorite.toUiModel(etf)
                }
            )
        }

    fun onSortChanged(sort: FavoriteEtfSort) {
        if (sort == currentSort) return
        loadLikedEtfs(sort)
    }

    fun onLikeToggled(ticker: String) {
        viewModelScope.launch {
            when (val result = userRepository.deleteFavoriteEtf(ticker)) {
                is BaseResult.Success -> loadLikedEtfs(currentSort)
                is BaseResult.Error -> _event.emit(LikedEtfListEvent.ShowMessage(result.error.message))
            }
        }
    }
}

data class LikedEtfListData(
    val likedEtfs: List<EtfListItemUiModel>,
    val selectedSort: FavoriteEtfSort,
    val totalCount: Int
)

private fun FavoriteEtf.toUiModel(etf: EtfSummary?) = EtfListItemUiModel(
    ticker = ticker,
    name = etf?.name ?: name,
    currentPrice = etf?.currentPrice ?: currentPrice,
    changeRate = etf?.changeRate ?: changeRate,
    changeAmount = etf?.changeAmount ?: 0L,
    riskType = etf?.riskType ?: (riskType ?: DEFAULT_RISK_TYPE),
    isLiked = true,
)

private fun EtfDetail.toSummaryEtf() = EtfSummary(
    name = name,
    currentPrice = currentPrice,
    changeRate = dailyFluctuationRatio,
    changeAmount = dailyFluctuation,
    riskType = riskType
)

sealed interface LikedEtfListEvent {
    data class ShowMessage(val message: String) : LikedEtfListEvent
}

private data class EtfSummary(
    val name: String,
    val currentPrice: Long,
    val changeRate: Double,
    val changeAmount: Long,
    val riskType: String
)

private const val DEFAULT_RISK_TYPE = "위험중립형"
