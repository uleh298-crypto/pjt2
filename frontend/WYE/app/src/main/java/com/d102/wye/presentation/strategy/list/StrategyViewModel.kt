package com.d102.wye.presentation.strategy.list

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.d102.wye.domain.common.BaseResult
import com.d102.wye.domain.repository.PortfolioRepository
import com.d102.wye.domain.repository.UserRepository
import com.d102.wye.presentation.model.UiState
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import timber.log.Timber
import javax.inject.Inject

@HiltViewModel
class StrategyViewModel @Inject constructor(
    private val portfolioRepository: PortfolioRepository,
    private val userRepository: UserRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow<UiState<StrategyListData>>(UiState.Idle)
    val uiState: StateFlow<UiState<StrategyListData>> = _uiState.asStateFlow()

    private val _showEditDialog = MutableStateFlow<StrategyCardUiModel?>(null)
    val showEditDialog: StateFlow<StrategyCardUiModel?> = _showEditDialog.asStateFlow()

    private val _showMyDataDialog = MutableStateFlow(false)
    val showMyDataDialog: StateFlow<Boolean> = _showMyDataDialog.asStateFlow()

    private val _myDataConsentChecked = MutableStateFlow(false)
    val myDataConsentChecked: StateFlow<Boolean> = _myDataConsentChecked.asStateFlow()

    // ─────────────────────────────────────────────────────────────────────────
    // 마이데이터 연동 다이얼로그
    // ─────────────────────────────────────────────────────────────────────────

    fun onMyDataConnectClick() {
        _showMyDataDialog.value = true
    }

    fun onMyDataDialogDismiss() {
        _showMyDataDialog.value = false
        _myDataConsentChecked.value = false
    }

    fun onMyDataConsentChecked(checked: Boolean) {
        _myDataConsentChecked.value = checked
    }

    fun onMyDataConsentConfirm() {
        viewModelScope.launch {
            when (userRepository.acceptMyData()) {
                is BaseResult.Success -> {
                    _showMyDataDialog.value = false
                    _myDataConsentChecked.value = false
                    loadStrategies()  // 연동 완료 후 재조회
                }

                is BaseResult.Error -> Unit
            }
        }
    }

    // ─────────────────────────────────────────────────────────────────────────
    // 포트폴리오 편집
    // ─────────────────────────────────────────────────────────────────────────

    fun onEditClick(strategy: StrategyCardUiModel) {
        _showEditDialog.value = strategy
    }

    fun onEditDialogDismiss() {
        _showEditDialog.value = null
    }

    // ─────────────────────────────────────────────────────────────────────────
    // 데이터 로드
    // ─────────────────────────────────────────────────────────────────────────

    fun loadStrategies() {
        viewModelScope.launch {
            _uiState.update { UiState.Loading }

            when (val result = portfolioRepository.getPortfolioList()) {
                is BaseResult.Success -> {
                    val allItems = result.data

                    // isMyData == true → 실제 자산 카드
                    val myDataItem = allItems.find { it.isMyData }
                    val realAsset = myDataItem?.let { item ->
                        StrategyCardUiModel(
                            id = item.portfolioId.toString(),
                            title = item.title,
                            date = item.createdAt,
                            tags = item.etfList.map { "#${it.name}" },
                            totalReturn = item.totalReturn,
                            isMyData = true
                        )
                    }

                    // isMyData != true → 일반 전략 목록
                    val strategies = allItems
                        .filter { !it.isMyData }
                        .map { item ->
                            StrategyCardUiModel(
                                id = item.portfolioId.toString(),
                                title = item.title,
                                date = item.createdAt,
                                tags = item.etfList.map { "#${it.name}" },
                                totalReturn = item.totalReturn
                            )
                        }

                    Timber.d("[Strategy] 로드 완료 | 마이데이터=${myDataItem != null} | 전략=${strategies.size}개")

                    _uiState.update {
                        UiState.Success(
                            StrategyListData(
                                realAsset = realAsset,
                                isMyDataConnected = myDataItem != null,
                                strategies = strategies
                            )
                        )
                    }
                }

                is BaseResult.Error -> {
                    Timber.e("[Strategy] 포트폴리오 목록 조회 실패 | ${result.error.message}")
                    _uiState.update { UiState.Error(result.error.message) }
                }
            }
        }
    }

    fun onDeleteStrategy(strategyId: String) {
        viewModelScope.launch {
            when (portfolioRepository.deletePortfolio(strategyId.toLong())) {
                is BaseResult.Success -> {
                    val current = (_uiState.value as? UiState.Success)?.data ?: return@launch
                    _uiState.update {
                        UiState.Success(
                            current.copy(
                                strategies = current.strategies.filter { it.id != strategyId }
                            )
                        )
                    }
                }

                is BaseResult.Error -> Unit
            }
        }
    }

    fun onUpdateStrategy(portfolioId: Long, newName: String) {
        viewModelScope.launch {
            when (portfolioRepository.updatePortfolio(portfolioId, newName)) {
                is BaseResult.Success -> {
                    val current = (_uiState.value as? UiState.Success)?.data ?: return@launch
                    _uiState.update {
                        UiState.Success(
                            current.copy(
                                strategies = current.strategies.map { strategy ->
                                    if (strategy.id == portfolioId.toString())
                                        strategy.copy(title = newName)
                                    else strategy
                                }
                            )
                        )
                    }
                    _showEditDialog.value = null
                }

                is BaseResult.Error -> Unit
            }
        }
    }
}

// ─────────────────────────────────────────
// 화면 데이터 모델
// ─────────────────────────────────────────

data class StrategyCardUiModel(
    val id: String,
    val title: String,
    val date: String,
    val tags: List<String>,
    val totalReturn: Double = 0.0,
    val isMyData: Boolean = false,
    val holdingCounts: Map<String, Double> = emptyMap()
)

data class StrategyListData(
    val realAsset: StrategyCardUiModel? = null,
    val isMyDataConnected: Boolean = false,
    val strategies: List<StrategyCardUiModel> = emptyList()
) {
    val isCompletelyEmpty = realAsset == null && strategies.isEmpty()
}