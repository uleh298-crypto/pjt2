package com.d102.wye.presentation.simulation.entry

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.d102.wye.domain.common.BaseResult
import com.d102.wye.domain.model.EtfBundle
import com.d102.wye.domain.model.EtfBundleDetail
import com.d102.wye.domain.repository.PortfolioRepository
import com.d102.wye.domain.repository.SimulationRepository
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
class SimulationEntryViewModel @Inject constructor(
    private val simulationRepository: SimulationRepository,
    private val portfolioRepository: PortfolioRepository,
) : ViewModel() {

    private val _uiState = MutableStateFlow<UiState<SimulationEntryData>>(UiState.Idle)
    val uiState: StateFlow<UiState<SimulationEntryData>> = _uiState.asStateFlow()

    private val _selectedBundleDetail = MutableStateFlow<EtfBundleDetail?>(null)
    val selectedBundleDetail: StateFlow<EtfBundleDetail?> = _selectedBundleDetail.asStateFlow()

    init {
        loadBundles()
    }

    fun loadBundles() {
        viewModelScope.launch {
            _uiState.update { UiState.Loading }
            when (val result = simulationRepository.getPresetList()) {
                is BaseResult.Success -> {
                    Timber.d("[Preset] 목록 조회 성공 | count=${result.data.size}")
                    val portfolioCount = when (val r = portfolioRepository.getPortfolioList()) {
                        is BaseResult.Success -> r.data.count { !it.isMyData }
                        is BaseResult.Error -> 0
                    }
                    _uiState.update {
                        UiState.Success(
                            SimulationEntryData(
                                bundles = result.data,
                                isPortfolioFull = portfolioCount >= 10
                            )
                        )
                    }
                }
                is BaseResult.Error -> {
                    Timber.e("[Preset] 목록 조회 실패 | ${result.error.message}")
                    _uiState.update { UiState.Error(result.error.message) }
                }
            }
        }
    }

    fun onBundleClick(bundle: EtfBundle) {
        viewModelScope.launch {
            when (val result = simulationRepository.getPresetDetail(bundle.id)) {
                is BaseResult.Success -> {
                    Timber.d("[Preset] 상세 조회 성공 | id=${bundle.id}")
                    _selectedBundleDetail.update { result.data }
                }
                is BaseResult.Error -> {
                    Timber.e("[Preset] 상세 조회 실패 | ${result.error.message}")
                }
            }
        }
    }

    fun onBundleDialogDismiss() {
        _selectedBundleDetail.update { null }
    }
}

data class SimulationEntryData(
    val bundles: List<EtfBundle>,
    val isPortfolioFull: Boolean = false,
)
