package com.d102.wye.presentation.model

/**
 * presentation/model/ 에는 UI 전용 모델을 작성한다
 */
sealed class UiState<out T> {
    object Idle : UiState<Nothing>()
    object Loading : UiState<Nothing>()
    data class Success<T>(val data: T) : UiState<T>()
    data class Error(val message: String) : UiState<Nothing>()
}