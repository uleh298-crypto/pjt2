package com.d102.wye.presentation.auth.passwordreset

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.d102.wye.domain.common.BaseResult
import com.d102.wye.domain.repository.AuthRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

@HiltViewModel
class PasswordResetViewModel @Inject constructor(
    private val authRepository: AuthRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(PasswordResetUiState())
    val uiState: StateFlow<PasswordResetUiState> = _uiState.asStateFlow()

    private val _passwordResetEvent = MutableSharedFlow<PasswordResetEvent>()
    val passwordResetEvent: SharedFlow<PasswordResetEvent> = _passwordResetEvent

    fun onEmailChanged(value: String) {
        _uiState.update { it.copy(email = value, errorMessage = null) }
    }

    fun onVerificationCodeChanged(value: String) {
        _uiState.update {
            it.copy(
                verificationCode = value.take(6),
                errorMessage = null
            )
        }
    }

    fun onPasswordChanged(value: String) {
        _uiState.update { it.copy(password = value, errorMessage = null) }
    }

    fun onPasswordConfirmChanged(value: String) {
        _uiState.update { it.copy(passwordConfirm = value, errorMessage = null) }
    }

    fun onPasswordVisibilityToggle() {
        _uiState.update { it.copy(isPasswordVisible = !it.isPasswordVisible) }
    }

    fun onPasswordConfirmVisibilityToggle() {
        _uiState.update { it.copy(isPasswordConfirmVisible = !it.isPasswordConfirmVisible) }
    }

    /** 현재 단계의 입력값을 검증하고 해당 비밀번호 재설정 API를 호출한다. */
    fun onNextClick() {
        val current = _uiState.value
        when (current.currentStep) {
            PasswordResetStep.EMAIL -> {
                val email = current.email.trim()
                if (!EMAIL_REGEX.matches(email)) {
                    setError("올바른 이메일 형식을 입력해 주세요.")
                } else {
                    checkPasswordResetEmail(email)
                }
            }

            PasswordResetStep.VERIFICATION -> {
                if (current.verificationCode.length < 6) {
                    setError("인증번호 6자리를 입력해 주세요.")
                } else {
                    verifyPasswordResetCode()
                }
            }

            PasswordResetStep.NEW_PASSWORD -> {
                when {
                    current.password.length < 8 -> setError("비밀번호는 8자 이상이어야 합니다.")
                    !PASSWORD_REGEX.matches(current.password) -> setError("비밀번호는 영문, 숫자, 특수문자를 모두 포함해야 합니다.")
                    current.password != current.passwordConfirm -> setError("비밀번호가 일치하지 않습니다.")
                    else -> resetPassword()
                }
            }

            PasswordResetStep.SUCCESS -> Unit
        }
    }

    fun onBackClick() {
        _uiState.update {
            it.copy(
                currentStep = when (it.currentStep) {
                    PasswordResetStep.EMAIL -> PasswordResetStep.EMAIL
                    PasswordResetStep.VERIFICATION -> PasswordResetStep.EMAIL
                    PasswordResetStep.NEW_PASSWORD -> PasswordResetStep.VERIFICATION
                    PasswordResetStep.SUCCESS -> PasswordResetStep.NEW_PASSWORD
                },
                helperMessage = if (it.currentStep == PasswordResetStep.VERIFICATION) null else it.helperMessage,
                errorMessage = null
            )
        }
    }

    /** 같은 이메일로 인증 메일을 다시 요청한다. */
    fun onResendCodeClick() {
        val email = _uiState.value.email.trim()
        if (email.isBlank()) {
            setError("이메일을 다시 입력해 주세요.")
            return
        }

        requestPasswordReset(email, successMessage = "인증번호를 다시 보냈습니다.")
    }

    /** 완료 화면에서 로컬 인증 상태를 정리한 뒤 로그인 화면 이동 이벤트를 발행한다. */
    fun onLoginClick() {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, errorMessage = null) }
            authRepository.clearLocalAuthState()
            _uiState.update { it.copy(isLoading = false) }
            _passwordResetEvent.emit(PasswordResetEvent.NavigateToLogin)
        }
    }

    /** 화면에 표시할 에러 메시지를 저장하고 로딩 상태를 해제한다. */
    private fun setError(message: String) {
        _uiState.update { it.copy(isLoading = false, errorMessage = message) }
    }

    /** 가입된 이메일일 때만 비밀번호 재설정 메일 요청을 진행한다. */
    private fun checkPasswordResetEmail(email: String) {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, errorMessage = null) }
            when (val result = authRepository.checkEmailExists(email)) {
                is BaseResult.Success -> {
                    if (result.data) {
                        requestPasswordReset(email)
                    } else {
                        setError("아이디를 찾을 수 없습니다.")
                    }
                }
                is BaseResult.Error -> setError(result.error.message)
            }
        }
    }

    /** 비밀번호 재설정 메일을 요청하고 인증번호 입력 단계로 이동한다. */
    private fun requestPasswordReset(email: String, successMessage: String = "인증번호 재전송") {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, errorMessage = null) }
            when (val result = authRepository.requestPasswordReset(email)) {
                is BaseResult.Success -> {
                    _uiState.update {
                        it.copy(
                            currentStep = PasswordResetStep.VERIFICATION,
                            email = email,
                            helperMessage = successMessage,
                            isLoading = false,
                            errorMessage = null
                        )
                    }
                }
                is BaseResult.Error -> setError(result.error.message)
            }
        }
    }

    /** 입력된 인증 코드를 검증하고 새 비밀번호 입력 단계로 진행한다. */
    private fun verifyPasswordResetCode() {
        val current = _uiState.value
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, errorMessage = null) }
            when (
                val result = authRepository.verifyPasswordResetCode(
                    email = current.email.trim(),
                    token = current.verificationCode
                )
            ) {
                is BaseResult.Success -> {
                    if (result.data) {
                        _uiState.update {
                            it.copy(
                                currentStep = PasswordResetStep.NEW_PASSWORD,
                                isLoading = false,
                                errorMessage = null
                            )
                        }
                    } else {
                        setError("인증번호를 다시 확인해 주세요.")
                    }
                }
                is BaseResult.Error -> setError(result.error.message)
            }
        }
    }

    /** 검증된 인증 코드로 새 비밀번호를 저장하고 완료 화면으로 이동한다. */
    private fun resetPassword() {
        val current = _uiState.value
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, errorMessage = null) }
            when (
                val result = authRepository.resetPassword(
                    email = current.email.trim(),
                    token = current.verificationCode,
                    newPassword = current.password,
                    newPasswordConfirm = current.passwordConfirm
                )
            ) {
                is BaseResult.Success -> {
                    _uiState.update {
                        it.copy(
                            currentStep = PasswordResetStep.SUCCESS,
                            isLoading = false,
                            errorMessage = null
                        )
                    }
                }
                is BaseResult.Error -> setError(result.error.message)
            }
        }
    }

    companion object {
        private val EMAIL_REGEX = Regex("^[A-Za-z0-9+_.-]+@[A-Za-z0-9.-]+$")
        private val PASSWORD_REGEX =
            Regex("^(?=.*[A-Za-z])(?=.*\\d)(?=.*[^A-Za-z\\d]).{8,72}$")
    }
}

sealed interface PasswordResetEvent {
    data object NavigateToLogin : PasswordResetEvent
}
