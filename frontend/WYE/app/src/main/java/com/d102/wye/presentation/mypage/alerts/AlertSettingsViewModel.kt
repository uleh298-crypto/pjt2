package com.d102.wye.presentation.mypage.alerts

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.d102.wye.domain.common.BaseResult
import com.d102.wye.domain.model.AlertSetting
import com.d102.wye.domain.repository.AlertRepository
import com.d102.wye.presentation.model.UiState
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class AlertSettingsViewModel @Inject constructor(
    private val alertRepository: AlertRepository,
) : ViewModel() {

    private val _uiState = MutableStateFlow<UiState<AlertSettingsUiState>>(UiState.Idle)
    val uiState: StateFlow<UiState<AlertSettingsUiState>> = _uiState.asStateFlow()

    private var allSettings: List<AlertSetting> = emptyList()

    init {
        loadAlertSettings()
    }

    fun loadAlertSettings() {
        viewModelScope.launch {
            _uiState.update { UiState.Loading }
            when (val result = alertRepository.getSettings()) {
                is BaseResult.Success -> {
                    allSettings = result.data
                    _uiState.update { UiState.Success(result.data.toUiState()) }
                }
                is BaseResult.Error -> _uiState.update { UiState.Error(result.error.message) }
            }
        }
    }

    fun onEtfListingChanged(enabled: Boolean) = toggleSetting("ETF_LISTING", enabled) {
        copy(etfListingEnabled = enabled)
    }

    fun onEtfDelistingChanged(enabled: Boolean) = toggleSetting("ETF_DELISTING", enabled) {
        copy(etfDelistingEnabled = enabled)
    }

    fun onPortfolioRebalancingChanged(enabled: Boolean) = toggleSetting("PORTFOLIO_REBALANCING", enabled) {
        copy(portfolioRebalancingEnabled = enabled)
    }

    fun onPortfolioProfitChanged(enabled: Boolean) = toggleSetting("PORTFOLIO_RETURN", enabled) {
        copy(portfolioProfitEnabled = enabled)
    }

    fun onNewsChanged(enabled: Boolean) = toggleSetting("NEWS_NOTIFICATION", enabled) {
        copy(newsEnabled = enabled)
    }

    fun enableAll() {
        _uiState.update { UiState.Success(AlertSettingsUiState(true, true, true, true, true, true)) }
        if (allSettings.isEmpty()) return
        allSettings = allSettings.map { it.copy(isEnabled = true) }
        viewModelScope.launch { alertRepository.updateSettings(allSettings) }
    }

    fun disableAll() {
        _uiState.update { UiState.Success(AlertSettingsUiState(false, false, false, false, false, false)) }
        if (allSettings.isEmpty()) return
        allSettings = allSettings.map { it.copy(isEnabled = false) }
        viewModelScope.launch { alertRepository.updateSettings(allSettings) }
    }

    private fun toggleSetting(
        code: String,
        enabled: Boolean,
        uiTransform: AlertSettingsUiState.() -> AlertSettingsUiState,
    ) {
        val current = (_uiState.value as? UiState.Success)?.data ?: return
        _uiState.update { UiState.Success(current.uiTransform()) }

        allSettings = allSettings.map {
            if (it.settingGroup == code) it.copy(isEnabled = enabled) else it
        }

        viewModelScope.launch {
            alertRepository.updateSettings(allSettings)
        }
    }
}

data class AlertSettingsUiState(
    val appNoticeEnabled: Boolean = false,
    val etfListingEnabled: Boolean = false,
    val etfDelistingEnabled: Boolean = false,
    val portfolioRebalancingEnabled: Boolean = false,
    val portfolioProfitEnabled: Boolean = false,
    val newsEnabled: Boolean = false,
)

private fun List<AlertSetting>.toUiState() = AlertSettingsUiState(
    appNoticeEnabled            = find { it.settingGroup == "APP_NOTIFICATION" }?.isEnabled ?: false,
    etfListingEnabled           = find { it.settingGroup == "ETF_LISTING" }?.isEnabled ?: false,
    etfDelistingEnabled         = find { it.settingGroup == "ETF_DELISTING" }?.isEnabled ?: false,
    portfolioRebalancingEnabled = find { it.settingGroup == "PORTFOLIO_REBALANCING" }?.isEnabled ?: false,
    portfolioProfitEnabled      = find { it.settingGroup == "PORTFOLIO_RETURN" }?.isEnabled ?: false,
    newsEnabled                 = find { it.settingGroup == "NEWS_NOTIFICATION" }?.isEnabled ?: false,
)
