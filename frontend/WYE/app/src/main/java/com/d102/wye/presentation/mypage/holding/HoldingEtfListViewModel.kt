package com.d102.wye.presentation.mypage.holding

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.d102.wye.domain.common.BaseResult
import com.d102.wye.domain.repository.EtfRepository
import com.d102.wye.domain.repository.UserRepository
import com.d102.wye.presentation.model.UiState
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class HoldingEtfListViewModel @Inject constructor(
    private val userRepository: UserRepository,
    private val etfRepository: EtfRepository,
) : ViewModel() {

    private val _uiState = MutableStateFlow<UiState<HoldingEtfListData>>(UiState.Idle)
    val uiState: StateFlow<UiState<HoldingEtfListData>> = _uiState.asStateFlow()

    init {
        loadHoldingEtfs()
    }

    fun loadHoldingEtfs() {
        viewModelScope.launch {
            _uiState.update { UiState.Loading }

            when (val acceptedResult = userRepository.getMyDataAccepted()) {
                is BaseResult.Error -> _uiState.update { UiState.Error(acceptedResult.error.message) }
                is BaseResult.Success -> {
                    if (!acceptedResult.data) {
                        _uiState.update {
                            UiState.Success(
                                HoldingEtfListData(
                                    holdings = emptyList(),
                                    isConnected = false
                                )
                            )
                        }
                        return@launch
                    }

                    when (val holdingsResult = userRepository.getMyDataHoldings()) {
                        is BaseResult.Error -> _uiState.update { UiState.Error(holdingsResult.error.message) }
                        is BaseResult.Success -> {
                            val items = holdingsResult.data.mapNotNull { holding ->
                                when (val etfResult = etfRepository.getEtfDetail(holding.ticker)) {
                                    is BaseResult.Error -> null
                                    is BaseResult.Success -> HoldingEtfListItemUiModel(
                                        ticker = holding.ticker,
                                        name = etfResult.data.name,
                                        currentPrice = etfResult.data.currentPrice,
                                        changeRate = etfResult.data.dailyFluctuationRatio,
                                        changeAmount = etfResult.data.dailyFluctuation,
                                        riskType = etfResult.data.riskType,
                                        quantityText = "${holding.counts}주 보유"
                                    )
                                }
                            }

                            _uiState.update {
                                UiState.Success(
                                    HoldingEtfListData(
                                        holdings = items,
                                        isConnected = true
                                    )
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}

data class HoldingEtfListData(
    val holdings: List<HoldingEtfListItemUiModel>,
    val isConnected: Boolean,
)

data class HoldingEtfListItemUiModel(
    val ticker: String,
    val name: String,
    val currentPrice: Long,
    val changeRate: Double,
    val changeAmount: Long,
    val riskType: String,
    val quantityText: String,
)
