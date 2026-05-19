package com.d102.wye.domain.common

sealed class BaseResult<out T> {
    data class Success<T>(val data: T) : BaseResult<T>()
    data class Error(val error: ApiError) : BaseResult<Nothing>()
}

fun <T, R> BaseResult<T>.map(transform: (T) -> R): BaseResult<R> = when (this) {
    is BaseResult.Success -> BaseResult.Success(transform(data))
    is BaseResult.Error   -> this
}