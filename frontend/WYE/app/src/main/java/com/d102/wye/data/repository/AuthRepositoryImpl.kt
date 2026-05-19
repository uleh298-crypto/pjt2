package com.d102.wye.data.repository

import com.d102.wye.data.local.datastore.AuthTokenDataStore
import com.d102.wye.data.mapper.toDomain
import com.d102.wye.data.remote.api.AuthApiService
import com.d102.wye.data.remote.dto.request.FcmTokenRequest
import com.d102.wye.data.remote.dto.request.KakaoLoginRequest
import com.d102.wye.data.remote.dto.request.LoginRequest
import com.d102.wye.data.remote.dto.request.LogoutRequest
import com.d102.wye.data.remote.dto.request.PasswordResetConfirmRequest
import com.d102.wye.data.remote.dto.request.PasswordResetRequest
import com.d102.wye.data.remote.dto.request.PasswordResetVerifyRequest
import com.d102.wye.data.remote.dto.request.SignupEmailRequest
import com.d102.wye.data.remote.dto.request.SignupRequest
import com.d102.wye.data.remote.dto.request.SignupResendRequest
import com.d102.wye.data.remote.dto.request.SignupVerifyRequest
import com.d102.wye.domain.common.BaseResult
import com.d102.wye.domain.model.TokenPair
import kotlinx.coroutines.flow.first
import com.d102.wye.domain.repository.AuthRepository
import kotlinx.coroutines.flow.Flow
import timber.log.Timber
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class AuthRepositoryImpl @Inject constructor(
    private val authApiService: AuthApiService,
    private val authTokenDataStore: AuthTokenDataStore  // 토큰 저장/조회
) : BaseRepository(), AuthRepository {

    // ─────────────────────────────────────────
    // 로그인
    // ─────────────────────────────────────────

    override suspend fun checkEmailAvailability(email: String): BaseResult<Boolean> {
        return when (
            val result = safeApiCall {
                authApiService.checkEmailAvailability(email = email)
            }
        ) {
            is BaseResult.Success -> BaseResult.Success(result.data.available)
            is BaseResult.Error -> result
        }
    }

    override suspend fun checkEmailExists(email: String): BaseResult<Boolean> {
        return when (
            val result = safeApiCall {
                authApiService.checkEmailAvailability(email = email)
            }
        ) {
            is BaseResult.Success -> BaseResult.Success(result.data.exists)
            is BaseResult.Error -> result
        }
    }

    /** 비밀번호 재설정 이메일 발송 요청을 보낸다. */
    override suspend fun requestPasswordReset(email: String): BaseResult<Unit> {
        return safeApiCallWithoutData {
            authApiService.requestPasswordReset(PasswordResetRequest(email = email))
        }
    }

    /** 비밀번호 재설정 인증 코드를 검증하고 유효 여부를 반환한다. */
    override suspend fun verifyPasswordResetCode(email: String, token: String): BaseResult<Boolean> {
        return when (
            val result = safeApiCall {
                authApiService.verifyPasswordResetCode(
                    PasswordResetVerifyRequest(
                        email = email,
                        token = token
                    )
                )
            }
        ) {
            is BaseResult.Success -> BaseResult.Success(result.data.valid)
            is BaseResult.Error -> result
        }
    }

    /** 검증된 인증 코드로 새 비밀번호를 재설정한다. */
    override suspend fun resetPassword(
        email: String,
        token: String,
        newPassword: String,
        newPasswordConfirm: String
    ): BaseResult<Unit> {
        return safeApiCallWithoutData {
            authApiService.resetPassword(
                PasswordResetConfirmRequest(
                    email = email,
                    token = token,
                    newPassword = newPassword,
                    newPasswordConfirm = newPasswordConfirm
                )
            )
        }
    }

    /** 회원가입 1단계: 이메일로 인증 메일을 발송한다. */
    override suspend fun sendSignupEmail(email: String): BaseResult<Unit> {
        return safeApiCallWithoutData {
            authApiService.sendSignupEmail(SignupEmailRequest(email = email))
        }
    }

    /** 회원가입 2단계: 이메일 인증 코드를 확인한다. */
    override suspend fun verifySignup(email: String, token: String): BaseResult<Unit> {
        return safeApiCallWithoutData {
            authApiService.verifySignup(
                SignupVerifyRequest(email = email, token = token)
            )
        }
    }

    /** 회원가입 3단계: 비밀번호·닉네임을 입력해 가입을 완료하고 토큰을 반환한다. */
    override suspend fun signupComplete(
        email: String,
        password: String,
        passwordConfirm: String,
        nickname: String
    ): BaseResult<TokenPair> {
        return when (
            val result = safeApiCall {
                authApiService.signupComplete(
                    SignupRequest(
                        email = email,
                        password = password,
                        passwordConfirm = passwordConfirm,
                        nickname = nickname
                    )
                )
            }
        ) {
            is BaseResult.Success -> BaseResult.Success(result.data.toDomain())
            is BaseResult.Error -> result
        }
    }

    /** 같은 이메일로 회원가입 인증 메일을 다시 발송한다. */
    override suspend fun resendSignupCode(email: String): BaseResult<Unit> {
        return safeApiCallWithoutData {
            authApiService.resendSignupCode(SignupResendRequest(email = email))
        }
    }

    override suspend fun saveAuthTokens(tokenPair: TokenPair) {
        authTokenDataStore.saveTokens(
            accessToken = tokenPair.accessToken,
            refreshToken = tokenPair.refreshToken
        )
    }

    override suspend fun clearLocalAuthState() {
        authTokenDataStore.clearTokens()
    }

    override suspend fun login(email: String, password: String): BaseResult<TokenPair> {
        return when (val result = safeApiCall { 
            authApiService.login(LoginRequest(email, password))
        }) {
            is BaseResult.Success -> {
                val tokenPair = result.data.toDomain()
                authTokenDataStore.saveTokens(
                    accessToken = tokenPair.accessToken,
                    refreshToken = tokenPair.refreshToken
                )
                BaseResult.Success(tokenPair)
            }
            is BaseResult.Error -> result
        }
    }

    /** 카카오 SDK access token으로 서버 로그인 후 JWT를 저장한다. */
    override suspend fun loginWithKakao(accessToken: String): BaseResult<TokenPair> {
        return when (val result = safeApiCall {
            authApiService.loginWithKakao(KakaoLoginRequest(accessToken = accessToken))
        }) {
            is BaseResult.Success -> {
                val tokenPair = result.data.toDomain()
                authTokenDataStore.saveTokens(
                    accessToken = tokenPair.accessToken,
                    refreshToken = tokenPair.refreshToken
                )
                BaseResult.Success(tokenPair)
            }
            is BaseResult.Error -> result
        }
    }

    // ─────────────────────────────────────────
    // 로그아웃
    // ─────────────────────────────────────────

    override suspend fun logout(): BaseResult<Unit> {
        val currentFcmToken = authTokenDataStore.fcmToken.first()
        Timber.d(
            "[Logout] request | hasFcmToken=%s | fcmToken=%s",
            !currentFcmToken.isNullOrBlank(),
            currentFcmToken
        )
        return safeApiCallWithoutData(
            // 서버 로그아웃 성공/실패 무관하게 로컬 토큰 삭제
            onSuccess = {
                Timber.d("[Logout] success | clearing local auth state")
                authTokenDataStore.clearTokens()
            }
        ) {
            authApiService.logout(
                LogoutRequest(
                    fcmToken = currentFcmToken
                )
            )
        }.also {
            // 서버 오류여도 로컬은 무조건 삭제
            if (it is BaseResult.Error) {
                Timber.e(
                    "[Logout] failed | code=%s | message=%s",
                    it.error.code,
                    it.error.message
                )
                authTokenDataStore.clearTokens()
            }
        }
    }

    // ─────────────────────────────────────────
    // FCM 토큰 등록
    // ─────────────────────────────────────────

    override suspend fun registerFcmToken(token: String): BaseResult<Unit> {
        return safeApiCallWithoutData(
            onSuccess = {
                Timber.d("[FCM] register success | token=%s", token)
                authTokenDataStore.saveFcmToken(token)
            }
        ) {
            authApiService.registerFcmToken(
                FcmTokenRequest(
                    token = token,
                    deviceType = "ANDROID"
                )
            )
        }.also {
            if (it is BaseResult.Error) {
                Timber.e(
                    "[FCM] register failed | code=%s | message=%s",
                    it.error.code,
                    it.error.message
                )
            }
        }
    }

    // ─────────────────────────────────────────
    // 토큰 상태 Flow (DataStore에서 직접 위임)
    // ─────────────────────────────────────────

    override val isLoggedIn: Flow<Boolean> = authTokenDataStore.isLoggedIn

    override val accessToken: Flow<String?> = authTokenDataStore.accessToken

    override val sessionExpired: Flow<Boolean> = authTokenDataStore.sessionExpired

    override suspend fun consumeSessionExpired() {
        authTokenDataStore.consumeSessionExpired()
    }
}
