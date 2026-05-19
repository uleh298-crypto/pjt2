package com.d102.wye.presentation.auth.login

data class LoginUiState(
    val email: String = "",
    val password: String = "",
    val isLoading: Boolean = false,
    val isKakaoLoading: Boolean = false,
    val isPasswordVisible: Boolean = false,
    val errorMessage: String? = null
) {
    val canLogin: Boolean
        get() = email.isNotBlank() && password.isNotBlank() && !isLoading && !isKakaoLoading
}
