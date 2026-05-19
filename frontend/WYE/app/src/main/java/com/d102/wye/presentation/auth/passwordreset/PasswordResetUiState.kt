package com.d102.wye.presentation.auth.passwordreset

enum class PasswordResetStep {
    EMAIL,
    VERIFICATION,
    NEW_PASSWORD,
    SUCCESS
}

data class PasswordResetUiState(
    val currentStep: PasswordResetStep = PasswordResetStep.EMAIL,
    val email: String = "",
    val verificationCode: String = "",
    val password: String = "",
    val passwordConfirm: String = "",
    val isPasswordVisible: Boolean = false,
    val isPasswordConfirmVisible: Boolean = false,
    val helperMessage: String? = null,
    val errorMessage: String? = null,
    val isLoading: Boolean = false
) {
    val canProceed: Boolean
        get() = when (currentStep) {
            PasswordResetStep.EMAIL -> email.isNotBlank()
            PasswordResetStep.VERIFICATION -> verificationCode.length >= 6
            PasswordResetStep.NEW_PASSWORD -> password.isNotBlank() && passwordConfirm.isNotBlank()
            PasswordResetStep.SUCCESS -> true
        } && !isLoading
}


