package com.d102.wye.presentation.auth.login

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
class LoginViewModel @Inject constructor(
    private val authRepository: AuthRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(LoginUiState())
    val uiState: StateFlow<LoginUiState> = _uiState.asStateFlow()

    private val _loginEvent = MutableSharedFlow<LoginEvent>()
    val loginEvent: SharedFlow<LoginEvent> = _loginEvent

    /** 이메일 입력값을 상태에 반영한다. */
    fun onEmailChanged(email: String) {
        _uiState.update {
            it.copy(
                email = email,
                errorMessage = null
            )
        }
    }

    /** 비밀번호 입력값을 상태에 반영한다. */
    fun onPasswordChanged(password: String) {
        _uiState.update {
            it.copy(
                password = password,
                errorMessage = null
            )
        }
    }

    /** 비밀번호 표시 여부를 토글한다. */
    fun onPasswordVisibilityToggle() {
        _uiState.update { it.copy(isPasswordVisible = !it.isPasswordVisible) }
    }

    /** 이메일 로그인 버튼을 눌렀을 때 입력값 검증과 로그인 요청을 처리한다. */
    fun onLoginClick() {
        val currentState = _uiState.value
        val email = currentState.email.trim()
        val password = currentState.password

        when {
            email.isBlank() -> setError("이메일을 입력해 주세요.")
            !EMAIL_REGEX.matches(email) -> setError("올바른 이메일 형식을 입력해 주세요.")
            password.isBlank() -> setError("비밀번호를 입력해 주세요.")
            else -> {
                _uiState.update {
                    it.copy(
                        email = email,
                        isLoading = true,
                        errorMessage = null
                    )
                }

                viewModelScope.launch {
                    when (val result = authRepository.login(email, password)) {
                        is BaseResult.Success -> emitLoginSuccess()
                        is BaseResult.Error -> setError(result.error.message)
                    }
                }
            }
        }
    }

    /** 카카오 로그인 시작 전 버튼 로딩 상태를 켠다. */
    fun onKakaoLoginStart() {
        _uiState.update { it.copy(isKakaoLoading = true, errorMessage = null) }
    }

    /** 카카오 SDK access token으로 서버 로그인 요청을 보낸다. */
    fun onKakaoLoginSuccess(accessToken: String) {
        viewModelScope.launch {
            when (val result = authRepository.loginWithKakao(accessToken = accessToken)) {
                is BaseResult.Success -> emitLoginSuccess()
                is BaseResult.Error -> setError(result.error.message)
            }
        }
    }

    /** 카카오 로그인 실패 시 로딩을 해제하고 에러 메시지를 노출한다. */
    fun onKakaoLoginFailure(message: String) {
        setError(message)
    }

    /** 화면에 표시할 에러 메시지를 저장하고 로딩 상태를 해제한다. */
    private fun setError(message: String) {
        _uiState.update { it.copy(isLoading = false, isKakaoLoading = false, errorMessage = message) }
    }

    /** 로그인 성공 시 로딩을 해제하고 화면 전환 이벤트를 발행한다. */
    private fun emitLoginSuccess() {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = false, isKakaoLoading = false) }
            _loginEvent.emit(LoginEvent.LoginSuccess)
        }
    }

    companion object {
        private val EMAIL_REGEX = Regex("^[A-Za-z0-9+_.-]+@[A-Za-z0-9.-]+$")
    }
}

sealed interface LoginEvent {
    data object LoginSuccess : LoginEvent
}
