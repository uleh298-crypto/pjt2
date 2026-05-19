package com.d102.wye.data.local.datastore

import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import com.d102.wye.core.app.Constants
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject
import javax.inject.Singleton

/**
 * JWT 토큰 및 로그인 상태를 DataStore에 저장/조회하는 클래스
 *
 * AuthTokenInterceptor, TokenRefreshInterceptor, AuthRepositoryImpl에서 주입받아 사용
 *
 * Flow를 반환하는 이유:
 * DataStore는 기본적으로 Flow 기반이라 값이 바뀌면 자동으로 새 값을 emit함
 * → 로그인 상태 변화를 ViewModel에서 실시간으로 감지 가능
 */
@Singleton
class AuthTokenDataStore @Inject constructor(
    private val dataStore: DataStore<Preferences>
) {

    // ─────────────────────────────────────────
    // Keys (Constants에서 문자열 관리)
    // ─────────────────────────────────────────

    private val accessTokenKey = stringPreferencesKey(Constants.KEY_ACCESS_TOKEN)
    private val refreshTokenKey = stringPreferencesKey(Constants.KEY_REFRESH_TOKEN)
    private val fcmTokenKey = stringPreferencesKey(Constants.KEY_FCM_TOKEN)
    private val isLoggedInKey = booleanPreferencesKey(Constants.KEY_IS_LOGGED_IN)
    private val sessionExpiredKey = booleanPreferencesKey(Constants.KEY_SESSION_EXPIRED)

    // ─────────────────────────────────────────
    // Read (Flow)
    // ─────────────────────────────────────────

    /** Access Token Flow — AuthTokenInterceptor에서 runBlocking으로 수집 */
    val accessToken: Flow<String?> = dataStore.data.map { it[accessTokenKey] }

    /** Refresh Token Flow — TokenRefreshInterceptor에서 runBlocking으로 수집 */
    val refreshToken: Flow<String?> = dataStore.data.map { it[refreshTokenKey] }

    /** 마지막으로 서버에 등록한 FCM 토큰 */
    val fcmToken: Flow<String?> = dataStore.data.map { it[fcmTokenKey] }

    /** 로그인 상태 Flow — MainActivity/AuthViewModel에서 startDestination 분기에 사용 */
    val isLoggedIn: Flow<Boolean> = dataStore.data.map { it[isLoggedInKey] ?: false }

    /** 토큰 만료 등으로 강제 로그아웃됐는지 여부 */
    val sessionExpired: Flow<Boolean> = dataStore.data.map { it[sessionExpiredKey] ?: false }

    // ─────────────────────────────────────────
    // Write
    // ─────────────────────────────────────────

    /**
     * 로그인 성공 시 토큰 저장
     * AuthRepositoryImpl.login() 성공 콜백에서 호출
     */
    suspend fun saveTokens(accessToken: String, refreshToken: String) {
        dataStore.edit { preferences ->
            preferences[accessTokenKey] = accessToken
            preferences[refreshTokenKey] = refreshToken
            preferences[isLoggedInKey] = true
            preferences[sessionExpiredKey] = false
        }
    }

    /**
     * 토큰 재발급 시 Access Token만 갱신
     * TokenRefreshInterceptor에서 호출
     */
    suspend fun updateAccessToken(accessToken: String) {
        dataStore.edit { preferences ->
            preferences[accessTokenKey] = accessToken
        }
    }

    /**
     * 토큰 재발급 시 양쪽 모두 갱신
     * TokenRefreshInterceptor에서 호출
     */
    suspend fun updateTokens(accessToken: String, refreshToken: String) {
        dataStore.edit { preferences ->
            preferences[accessTokenKey] = accessToken
            preferences[refreshTokenKey] = refreshToken
        }
    }

    suspend fun saveFcmToken(token: String) {
        dataStore.edit { preferences ->
            preferences[fcmTokenKey] = token
        }
    }

    /**
     * 로그아웃 / 토큰 만료 시 전체 삭제
     * AuthRepositoryImpl.logout() 또는 TokenRefreshInterceptor 재발급 실패 시 호출
     */
    suspend fun clearTokens() {
        dataStore.edit { preferences ->
            preferences.remove(accessTokenKey)
            preferences.remove(refreshTokenKey)
            preferences.remove(fcmTokenKey)
            preferences[isLoggedInKey] = false
            preferences[sessionExpiredKey] = false
        }
    }

    /** 세션 만료로 인한 강제 로그아웃 처리 */
    suspend fun clearTokensBySessionExpired() {
        dataStore.edit { preferences ->
            preferences.remove(accessTokenKey)
            preferences.remove(refreshTokenKey)
            preferences.remove(fcmTokenKey)
            preferences[isLoggedInKey] = false
            preferences[sessionExpiredKey] = true
        }
    }

    /** 세션 만료 안내를 한 번 보여준 뒤 소비 처리 */
    suspend fun consumeSessionExpired() {
        dataStore.edit { preferences ->
            preferences[sessionExpiredKey] = false
        }
    }
}
