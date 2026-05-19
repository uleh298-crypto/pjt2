package com.d102.wye.presentation.auth.login

import android.app.Activity
import android.content.Context
import android.content.ContextWrapper
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.ui.Alignment
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.d102.wye.core.auth.KakaoLoginManager
import com.d102.wye.presentation.auth.login.components.LoginFooterLinks
import com.d102.wye.presentation.auth.login.components.LoginFormSection
import com.d102.wye.presentation.auth.login.components.LoginHeader
import com.d102.wye.presentation.auth.login.components.LoginSocialSection
import com.d102.wye.presentation.theme.SurfaceWhite
import kotlinx.coroutines.flow.collect

@Composable
fun LoginScreen(
    onLoginSuccess: () -> Unit,
    onJoinClick: () -> Unit,
    onForgotPasswordClick: () -> Unit,
    viewModel: LoginViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val context = LocalContext.current
    val kakaoLoginManager = remember { KakaoLoginManager() }

    LaunchedEffect(viewModel) {
        viewModel.loginEvent.collect { event ->
            when (event) {
                LoginEvent.LoginSuccess -> onLoginSuccess()
            }
        }
    }

    LoginScreenContent(
        uiState = uiState,
        onEmailChanged = { viewModel.onEmailChanged(it) },
        onPasswordChanged = { viewModel.onPasswordChanged(it) },
        onPasswordVisibilityToggle = { viewModel.onPasswordVisibilityToggle() },
        onLoginClick = { viewModel.onLoginClick() },
        onKakaoLoginClick = {
            val activity = context.findActivity()
            if (activity == null) {
                viewModel.onKakaoLoginFailure("카카오 로그인을 시작할 수 없습니다.")
            } else {
                viewModel.onKakaoLoginStart()
                kakaoLoginManager.login(
                    activity = activity,
                    onSuccess = viewModel::onKakaoLoginSuccess,
                    onError = viewModel::onKakaoLoginFailure
                )
            }
        },
        onJoinClick = onJoinClick,
        onForgotPasswordClick = onForgotPasswordClick
    )
}

@Composable
private fun LoginScreenContent(
    uiState: LoginUiState,
    onEmailChanged: (String) -> Unit,
    onPasswordChanged: (String) -> Unit,
    onPasswordVisibilityToggle: () -> Unit,
    onLoginClick: () -> Unit,
    onKakaoLoginClick: () -> Unit,
    onJoinClick: () -> Unit,
    onForgotPasswordClick: () -> Unit
) {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(SurfaceWhite)
            .verticalScroll(rememberScrollState()),
        contentAlignment = Alignment.Center
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 24.dp)
                .padding(top = 60.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            LoginHeader()

            Spacer(modifier = Modifier.height(48.dp))

            LoginFormSection(
                uiState = uiState,
                onEmailChanged = onEmailChanged,
                onPasswordChanged = onPasswordChanged,
                onPasswordVisibilityToggle = onPasswordVisibilityToggle,
                onLoginClick = onLoginClick
            )

            Spacer(modifier = Modifier.height(20.dp))

            LoginSocialSection(
                onKakaoLoginClick = onKakaoLoginClick,
                kakaoLoginEnabled = !uiState.isLoading && !uiState.isKakaoLoading
            )

            Spacer(modifier = Modifier.height(18.dp))

            LoginFooterLinks(
                onJoinClick = onJoinClick,
                onForgotPasswordClick = onForgotPasswordClick
            )
        }
    }
}

/** LocalContext에서 실제 Activity를 찾아 카카오 SDK 호출에 사용한다. */
private fun Context.findActivity(): Activity? = when (this) {
    is Activity -> this
    is ContextWrapper -> baseContext.findActivity()
    else -> null
}
