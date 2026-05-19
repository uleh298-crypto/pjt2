package com.d102.wye.presentation.explore.stock

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.d102.wye.domain.common.BaseResult
import com.d102.wye.domain.model.RelatedStock
import com.d102.wye.domain.model.Stock

import com.d102.wye.domain.repository.StockRepository
import com.d102.wye.presentation.model.UiState
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class StockDetailViewModel @Inject constructor(
    private val stockRepository: StockRepository,
    savedStateHandle: SavedStateHandle,
) : ViewModel() {

    val ticker: String = checkNotNull(savedStateHandle["ticker"])

    private val _stockState = MutableStateFlow<UiState<Stock>>(UiState.Loading)
    val stockState: StateFlow<UiState<Stock>> = _stockState.asStateFlow()

    private val _relatedStocksState = MutableStateFlow<UiState<List<RelatedStock>>>(UiState.Loading)
    val relatedStocksState: StateFlow<UiState<List<RelatedStock>>> = _relatedStocksState.asStateFlow()

    private val _tagsState = MutableStateFlow<UiState<List<String>>>(UiState.Loading)
    val tagsState: StateFlow<UiState<List<String>>> = _tagsState.asStateFlow()

    init {
        loadStock()
        loadRelatedStocks()
        loadTags()
    }

    fun loadStock() {
        viewModelScope.launch {
            _stockState.update { UiState.Loading }
            when (val result = stockRepository.getStock(ticker)) {
                is BaseResult.Success -> _stockState.update { UiState.Success(result.data) }
                is BaseResult.Error   -> _stockState.update { UiState.Error(result.error.message) }
            }
        }
    }

    fun loadRelatedStocks() {
        viewModelScope.launch {
            _relatedStocksState.update { UiState.Loading }
            when (val result = stockRepository.getRelatedStocks(ticker)) {
                is BaseResult.Success -> _relatedStocksState.update { UiState.Success(result.data) }
                is BaseResult.Error   -> _relatedStocksState.update { UiState.Error(result.error.message) }
            }
        }
    }

    fun loadTags() {
        viewModelScope.launch {
            _tagsState.update { UiState.Loading }
            when (val result = stockRepository.getTags(ticker)) {
                is BaseResult.Success -> _tagsState.update { UiState.Success(result.data) }
                is BaseResult.Error   -> _tagsState.update { UiState.Error(result.error.message) }
            }
        }
    }
}
