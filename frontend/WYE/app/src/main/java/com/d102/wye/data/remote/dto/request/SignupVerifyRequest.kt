package com.d102.wye.data.remote.dto.request

data class SignupVerifyRequest(
    val email: String,
    val token: String
)
