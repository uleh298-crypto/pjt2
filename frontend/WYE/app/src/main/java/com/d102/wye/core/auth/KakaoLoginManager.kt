package com.d102.wye.core.auth

import android.app.Activity
import com.d102.wye.BuildConfig
import com.kakao.sdk.auth.model.OAuthToken
import com.kakao.sdk.user.UserApiClient
import timber.log.Timber

/** 카카오 SDK 로그인과 access token 획득을 감싼다. */
class KakaoLoginManager {

    /** 카카오 SDK 로그인 흐름을 시작하고 성공 시 access token을 전달한다. */
    fun login(
        activity: Activity,
        onSuccess: (String) -> Unit,
        onError: (String) -> Unit,
    ) {
        if (BuildConfig.KAKAO_NATIVE_APP_KEY.startsWith("TODO_")) {
            onError("카카오 네이티브 앱 키를 먼저 설정해 주세요.")
            return
        }

        val callback: (OAuthToken?, Throwable?) -> Unit = { token, error ->
            when {
                error != null -> onError(error.message ?: "카카오 로그인 중 오류가 발생했습니다.")
                token?.accessToken.isNullOrBlank() -> onError("카카오 access token을 받지 못했습니다.")
                else -> {
                    Timber.d("Kakao access token: %s", token!!.accessToken)
                    onSuccess(token.accessToken)
                }
            }
        }

        if (UserApiClient.instance.isKakaoTalkLoginAvailable(activity)) {
            UserApiClient.instance.loginWithKakaoTalk(activity, callback = callback)
        } else {
            UserApiClient.instance.loginWithKakaoAccount(activity, callback = callback)
        }
    }
}
