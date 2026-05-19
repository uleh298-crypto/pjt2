package com.d102.wye.core.network

import com.d102.wye.data.local.datastore.AuthTokenDataStore
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.runBlocking
import okhttp3.Interceptor
import okhttp3.Response
import timber.log.Timber
import javax.inject.Inject

/**
 * JWT Token을 요청 헤더에 추가하는 Interceptor
 */
class AuthTokenInterceptor @Inject constructor(
    private val authTokenDataStore: AuthTokenDataStore  // DataStore 직접 대신 DataStore 래퍼 주입
) : Interceptor {

    companion object {
        // 토큰이 필요 없는 URL 패턴 (로그인, 회원가입, 이메일 인증 등)
        // 서버 API 확정되면 실제 경로로 교체
        private val PUBLIC_URLS = listOf(
            "/auth/check/email",
            "/auth/login",
            "/auth/signup",
            "/auth/password"
        )
    }

    override fun intercept(chain: Interceptor.Chain): Response {
        val request = chain.request()
        val path = request.url.encodedPath

        // 공개 URL이면 토큰 추가하지 않고 바로 진행
        if (PUBLIC_URLS.any { path.contains(it) }) {
            Timber.d("Public URL, skipping token: $path")
            return chain.proceed(request)
        }

        // AuthTokenDataStore에서 토큰 읽기
        val token = runBlocking {
            authTokenDataStore.accessToken.first()
        }

        val newRequest = if (!token.isNullOrEmpty()) {
            Timber.d("Added JWT token to request: $path")
            request.newBuilder()
                .addHeader("Authorization", "Bearer $token")
                .build()
        } else {
            Timber.w("No token found for protected URL: $path")
            request
        }

        return chain.proceed(newRequest)
        // 401 처리는 TokenRefreshInterceptor가 담당
        // 여기서 중복으로 clearTokens() 하지 않음
    }
}
