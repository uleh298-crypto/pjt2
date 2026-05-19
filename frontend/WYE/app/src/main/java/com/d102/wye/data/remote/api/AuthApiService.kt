package com.d102.wye.data.remote.api

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
import com.d102.wye.data.remote.dto.response.BaseResponse
import com.d102.wye.data.remote.dto.response.EmailCheckResponse
import com.d102.wye.data.remote.dto.response.PasswordResetVerifyResponse
import com.d102.wye.data.remote.dto.response.SignupResponse
import com.d102.wye.data.remote.dto.response.TokenResponse
import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.POST
import retrofit2.http.Query

/**
 * 인증 관련 Retrofit API 인터페이스
 */
interface AuthApiService {

    /** 회원가입 전 이메일 중복 여부를 확인한다. */
    @GET("auth/check/email")
    suspend fun checkEmailAvailability(
        @Query("email") email: String
    ): Response<BaseResponse<EmailCheckResponse>>

    /**
     * 이메일 로그인
     */
    @POST("auth/login")
    suspend fun login(
        @Body request: LoginRequest
    ): Response<BaseResponse<TokenResponse>>

    /** 회원가입 1단계: 이메일로 인증 메일을 발송한다. */
    @POST("auth/signup")
    suspend fun sendSignupEmail(
        @Body request: SignupEmailRequest
    ): Response<BaseResponse<Unit>>

    /** 회원가입 3단계: 이메일 인증 후 비밀번호·닉네임으로 가입을 완료한다. */
    @POST("auth/signup/complete")
    suspend fun signupComplete(
        @Body request: SignupRequest
    ): Response<BaseResponse<TokenResponse>>

    /** 비밀번호 재설정 인증 메일을 요청한다. */
    @POST("auth/password/reset/request")
    suspend fun requestPasswordReset(
        @Body request: PasswordResetRequest
    ): Response<BaseResponse<Unit>>

    /** 비밀번호 재설정 인증 코드를 검증한다. */
    @POST("auth/password/reset/verify")
    suspend fun verifyPasswordResetCode(
        @Body request: PasswordResetVerifyRequest
    ): Response<BaseResponse<PasswordResetVerifyResponse>>

    /** 인증 코드 확인 후 새 비밀번호로 재설정한다. */
    @POST("auth/password/reset")
    suspend fun resetPassword(
        @Body request: PasswordResetConfirmRequest
    ): Response<BaseResponse<Unit>>

    /** 회원가입 2단계: 이메일로 발송된 인증 코드를 확인한다. */
    @POST("auth/signup/verify")
    suspend fun verifySignup(
        @Body request: SignupVerifyRequest
    ): Response<BaseResponse<Unit>>

    /** 회원가입 인증 메일을 같은 이메일로 재발송한다. */
    @POST("auth/signup/resend")
    suspend fun resendSignupCode(
        @Body request: SignupResendRequest
    ): Response<BaseResponse<Unit>>

    /**
     * 카카오 로그인
     */
    @POST("auth/oauth/kakao")
    suspend fun loginWithKakao(
        @Body request: KakaoLoginRequest
    ): Response<BaseResponse<TokenResponse>>


    /**
     * 로그아웃
     * 서버에서 Refresh Token 무효화
     */
    @POST("auth/logout")
    suspend fun logout(
        @Body request: LogoutRequest
    ): Response<BaseResponse<Unit>>

    /**
     * FCM 토큰 등록
     * 로그인 후 또는 토큰 갱신 시 서버에 저장
     */
    @POST("auth/fcm/token")
    suspend fun registerFcmToken(
        @Body request: FcmTokenRequest,
    ): Response<BaseResponse<Unit>>
}
