package com.d102.wye.data.remote.dto.request

data class PasswordResetConfirmRequest(
    val email: String,
    val token: String,
    val newPassword: String,
    val newPasswordConfirm: String
)
