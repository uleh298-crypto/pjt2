package com.d102.wye.data.remote.dto.request

data class SignupRequest(
    val email: String,
    val password: String,
    val passwordConfirm: String,
    val nickname: String
)
