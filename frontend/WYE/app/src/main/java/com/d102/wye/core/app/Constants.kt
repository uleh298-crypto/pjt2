package com.d102.wye.core.app

object Constants {

    // API
    const val BASE_URL = "https://api.example.com/api/v1/"

    const val CONNECT_TIMEOUT = 30L // seconds
    const val READ_TIMEOUT = 30L
    const val WRITE_TIMEOUT = 30L

    // DataStore
    const val PREF_NAME = "wye_prefs"
    const val KEY_ACCESS_TOKEN = "access_token"
    const val KEY_REFRESH_TOKEN = "refresh_token"
    const val KEY_FCM_TOKEN = "fcm_token"
    const val KEY_USER_ID = "user_id"
    const val KEY_IS_LOGGED_IN = "is_logged_in"
    const val KEY_SESSION_EXPIRED = "session_expired"

    // Database
    const val DATABASE_NAME = "etf_database"
    const val DATABASE_VERSION = 4


    // Error Messages
    const val ERROR_NETWORK = "네트워크 연결을 확인해주세요"
    const val ERROR_SESSION_EXPIRED = "세션이 만료되었습니다. 다시 로그인해주세요"

}
