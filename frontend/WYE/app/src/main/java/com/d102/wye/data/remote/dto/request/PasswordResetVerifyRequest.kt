package com.d102.wye.data.remote.dto.request

data class PasswordResetVerifyRequest(
    val email: String,
    val token: String
)
