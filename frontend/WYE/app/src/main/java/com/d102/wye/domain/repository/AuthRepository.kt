package com.d102.wye.domain.repository

import com.d102.wye.domain.common.BaseResult
import com.d102.wye.domain.model.TokenPair
import kotlinx.coroutines.flow.Flow

/**
 * 인증 관련 Repository 인터페이스
 */
interface AuthRepository {

    /** 회원가입 전 이메일 사용 가능 여부를 확인한다. */
    suspend fun checkEmailAvailability(email: String): BaseResult<Boolean>

    /** 가입된 이메일인지 확인한다. */
    suspend fun checkEmailExists(email: String): BaseResult<Boolean>

    /** 비밀번호 재설정 메일 발송을 요청한다. */
    suspend fun requestPasswordReset(email: String): BaseResult<Unit>

    /** 비밀번호 재설정 인증 코드를 검증한다. */
    suspend fun verifyPasswordResetCode(email: String, token: String): BaseResult<Boolean>

    /** 검증된 인증 코드로 새 비밀번호를 저장한다. */
    suspend fun resetPassword(
        email: String,
        token: String,
        newPassword: String,
        newPasswordConfirm: String
    ): BaseResult<Unit>

    /** 회원가입 1단계: 이메일로 인증 메일을 발송한다. */
    suspend fun sendSignupEmail(email: String): BaseResult<Unit>

    /** 회원가입 2단계: 이메일 인증 코드를 확인한다. */
    suspend fun verifySignup(email: String, token: String): BaseResult<Unit>

    /** 회원가입 3단계: 비밀번호·닉네임을 입력해 가입을 완료하고 토큰을 반환한다. */
    suspend fun signupComplete(
        email: String,
        password: String,
        passwordConfirm: String,
        nickname: String
    ): BaseResult<TokenPair>

    /** 회원가입 인증 메일을 재발송한다. */
    suspend fun resendSignupCode(email: String): BaseResult<Unit>

    /** 발급된 토큰을 로컬에 저장해 로그인 상태를 확정한다. */
    suspend fun saveAuthTokens(tokenPair: TokenPair)

    /** 서버 호출 없이 로컬 인증 상태만 정리한다. */
    suspend fun clearLocalAuthState()

    /** 로그인 */
    suspend fun login(email: String, password: String): BaseResult<TokenPair>

    /** 카카오 SDK access token으로 서버 로그인 후 JWT를 발급받는다. */
    suspend fun loginWithKakao(accessToken: String): BaseResult<TokenPair>

    /** 로그아웃 (토큰 삭제) */
    suspend fun logout(): BaseResult<Unit>

    /** FCM 토큰 서버 등록 */
    suspend fun registerFcmToken(token: String): BaseResult<Unit>

    /** 로그인 상태 Flow — MainActivity startDestination 분기에 사용 */
    val isLoggedIn: Flow<Boolean>

    /** Access Token Flow — 필요한 경우 직접 참조 */
    val accessToken: Flow<String?>

    /** 세션 만료로 강제 로그아웃됐는지 여부 */
    val sessionExpired: Flow<Boolean>

    /** 세션 만료 안내를 소비 처리한다. */
    suspend fun consumeSessionExpired()
}
