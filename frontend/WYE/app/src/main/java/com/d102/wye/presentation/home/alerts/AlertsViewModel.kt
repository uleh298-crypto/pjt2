package com.d102.wye.presentation.home.alerts

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.d102.wye.domain.common.BaseResult
import com.d102.wye.domain.model.Alert
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
class AlertsViewModel @Inject constructor(
    private val alertRepository: AlertRepository,
) : ViewModel() {

    private val _uiState = MutableStateFlow<UiState<List<Alert>>>(UiState.Idle)
    val uiState: StateFlow<UiState<List<Alert>>> = _uiState.asStateFlow()

    init {
        loadAlerts()
    }

    fun loadAlerts(category: String = "all") {
        viewModelScope.launch {
            _uiState.update { UiState.Loading }
            when (val result = alertRepository.getAlerts(category)) {
                is BaseResult.Success -> _uiState.update { UiState.Success(result.data) }
                is BaseResult.Error -> _uiState.update { UiState.Error(result.error.message) }
            }
        }
    }

    fun markAsRead(id: Long) {
        viewModelScope.launch {
            alertRepository.markAsRead(id)
            val current = (_uiState.value as? UiState.Success)?.data ?: return@launch
            _uiState.update {
                UiState.Success(current.map { if (it.id == id) it.copy(isRead = true) else it })
            }
        }
    }
}
