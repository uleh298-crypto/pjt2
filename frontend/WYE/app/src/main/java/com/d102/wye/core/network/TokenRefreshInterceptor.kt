package com.d102.wye.core.network

import com.d102.wye.core.app.Constants
import com.d102.wye.data.local.datastore.AuthTokenDataStore
import com.d102.wye.data.remote.dto.request.RefreshTokenRequest
import com.d102.wye.data.remote.dto.response.BaseResponse
import com.d102.wye.data.remote.dto.response.TokenResponse
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.runBlocking
import okhttp3.Interceptor
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody
import okhttp3.Response
import timber.log.Timber
import javax.inject.Inject

/**
 * Access Token 만료 시 Refresh Token으로 재발급을 시도하는 Interceptor
 */
class TokenRefreshInterceptor @Inject constructor(
    private val authTokenDataStore: AuthTokenDataStore,  // DataStore 직접 대신 DataStore 래퍼 주입
    private val gson: Gson
) : Interceptor {

    override fun intercept(chain: Interceptor.Chain): Response {
        val originalRequest = chain.request()
        val response = chain.proceed(originalRequest)

        if (response.code != 401 && response.code != 403) return response

        Timber.w("Auth failure (${response.code}), attempting token refresh")

        // AuthTokenDataStore에서 Refresh Token 읽기
        val refreshToken = runBlocking {
            authTokenDataStore.refreshToken.first()
        }

        if (refreshToken.isNullOrEmpty()) {
            Timber.e("No refresh token found, skipping session expired (not logged in)")
            return response
        }

        // Refresh Token으로 새 토큰 요청
        val newTokenPair = refreshAccessToken(refreshToken)

        if (newTokenPair == null) {
            Timber.e("Token refresh failed, clearing auth data")
            runBlocking { authTokenDataStore.clearTokensBySessionExpired() }
            return response
        }

        // AuthTokenDataStore에 새 토큰 저장
        runBlocking {
            authTokenDataStore.updateTokens(
                accessToken = newTokenPair.first,
                refreshToken = newTokenPair.second
            )
        }

        Timber.d("Token refreshed successfully, retrying original request")

        response.close()
        val retryRequest = originalRequest.newBuilder()
            .removeHeader("Authorization")
            .addHeader("Authorization", "Bearer ${newTokenPair.first}")
            .build()

        return chain.proceed(retryRequest)
    }

    /**
     * 인터셉터가 없는 순수 OkHttpClient로 토큰 재발급 요청
     * 앱 OkHttpClient를 그대로 쓰면 401 → 재발급 → 401 무한루프 발생
     */
    private fun refreshAccessToken(refreshToken: String): Pair<String, String>? {
        return try {
            val client = OkHttpClient()

            val body = gson.toJson(
                RefreshTokenRequest(refreshToken = refreshToken)
            )
                .toRequestBody("application/json".toMediaType())

            val request = Request.Builder()
                .url("${Constants.BASE_URL}auth/token/refresh")
                .post(body)
                .build()

            val response = client.newCall(request).execute()

            if (!response.isSuccessful) {
                Timber.w("Token refresh failed with code: ${response.code}")
                return null
            }

            val responseBody = response.body?.string() ?: return null
            val responseType = object : TypeToken<BaseResponse<TokenResponse>>() {}.type
            val parsedResponse: BaseResponse<TokenResponse> =
                gson.fromJson(responseBody, responseType) ?: return null

            val tokenData = parsedResponse.data ?: return null
            val newAccessToken = tokenData.accessToken.takeIf { it.isNotEmpty() } ?: return null
            // refresh API는 accessToken만 재발급하고 refreshToken은 생략할 수 있다.
            val newRefreshToken = tokenData.refreshToken.takeIf { it.isNotEmpty() } ?: refreshToken

            Pair(newAccessToken, newRefreshToken)
        } catch (e: Exception) {
            Timber.e(e, "Exception during token refresh")
            null
        }
    }
}
