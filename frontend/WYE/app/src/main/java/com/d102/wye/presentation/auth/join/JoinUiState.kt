package com.d102.wye.presentation.auth.join

import com.d102.wye.domain.model.TokenPair

enum class JoinStep {
    NICKNAME,
    EMAIL,
    VERIFICATION,
    PASSWORD,
    SUCCESS
}

data class JoinUiState(
    val currentStep: JoinStep = JoinStep.NICKNAME,
    val nickname: String = "",
    val email: String = "",
    val verificationCode: String = "",
    val password: String = "",
    val passwordConfirm: String = "",
    val isPasswordVisible: Boolean = false,
    val isPasswordConfirmVisible: Boolean = false,
    val pendingTokenPair: TokenPair? = null,
    val helperMessage: String? = null,
    val errorMessage: String? = null,
    val isLoading: Boolean = false
) {
    val canProceed: Boolean
        get() = when (currentStep) {
            JoinStep.NICKNAME -> nickname.isNotBlank()
            JoinStep.EMAIL -> email.isNotBlank()
            JoinStep.PASSWORD -> password.isNotBlank() && passwordConfirm.isNotBlank()
            JoinStep.VERIFICATION -> verificationCode.length >= 6
            JoinStep.SUCCESS -> true
        } && !isLoading
}
