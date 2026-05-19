package com.d102.wye.presentation.auth.passwordreset

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.VerifiedUser
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.d102.wye.presentation.auth.join.components.JoinStepHeader
import com.d102.wye.presentation.auth.join.components.JoinVerificationSection
import com.d102.wye.presentation.designsystem.WyeEmailTextField
import com.d102.wye.presentation.designsystem.WyePasswordConfirmTextField
import com.d102.wye.presentation.designsystem.WyePasswordTextField
import com.d102.wye.presentation.designsystem.WyePrimaryButton
import com.d102.wye.presentation.designsystem.WyeTextFieldStyle
import com.d102.wye.presentation.designsystem.WyeTopBar
import com.d102.wye.presentation.designsystem.WyeVerificationCodeTextField
import com.d102.wye.presentation.theme.BadgeConservative
import com.d102.wye.presentation.theme.PrimaryGreen
import com.d102.wye.presentation.theme.TextSecondary
import kotlinx.coroutines.flow.collect

@Composable
fun PasswordResetScreen(
    onBackClick: () -> Unit,
    onCloseClick: () -> Unit,
    onLoginClick: () -> Unit,
    viewModel: PasswordResetViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()

    LaunchedEffect(viewModel) {
        viewModel.passwordResetEvent.collect { event ->
            when (event) {
                PasswordResetEvent.NavigateToLogin -> onLoginClick()
            }
        }
    }

    PasswordResetScreenContent(
        uiState = uiState,
        onBackClick = {
            if (uiState.currentStep == PasswordResetStep.EMAIL) onBackClick() else viewModel.onBackClick()
        },
        onCloseClick = onCloseClick,
        onLoginClick = onLoginClick,
        onEmailChanged = { viewModel.onEmailChanged(it) },
        onVerificationCodeChanged = { viewModel.onVerificationCodeChanged(it) },
        onPasswordChanged = { viewModel.onPasswordChanged(it) },
        onPasswordConfirmChanged = { viewModel.onPasswordConfirmChanged(it) },
        onPasswordVisibilityToggle = { viewModel.onPasswordVisibilityToggle() },
        onPasswordConfirmVisibilityToggle = { viewModel.onPasswordConfirmVisibilityToggle() },
        onNextClick = { viewModel.onNextClick() },
        onResendCodeClick = { viewModel.onResendCodeClick() },
        onLoginButtonClick = { viewModel.onLoginClick() }
    )
}

@Composable
private fun PasswordResetScreenContent(
    uiState: PasswordResetUiState,
    onBackClick: () -> Unit,
    onCloseClick: () -> Unit,
    onLoginClick: () -> Unit,
    onEmailChanged: (String) -> Unit,
    onVerificationCodeChanged: (String) -> Unit,
    onPasswordChanged: (String) -> Unit,
    onPasswordConfirmChanged: (String) -> Unit,
    onPasswordVisibilityToggle: () -> Unit,
    onPasswordConfirmVisibilityToggle: () -> Unit,
    onNextClick: () -> Unit,
    onResendCodeClick: () -> Unit,
    onLoginButtonClick: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
    ) {
        if (uiState.currentStep == PasswordResetStep.SUCCESS) {
            IconButton(onClick = onCloseClick, modifier = Modifier.padding(start = 4.dp, top = 4.dp)) {
                Icon(
                    imageVector = Icons.Default.Close,
                    contentDescription = "닫기",
                    tint = TextSecondary
                )
            }
        } else {
            WyeTopBar(
                title = "",
                onBackClick = onBackClick
            )
        }

        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 16.dp, vertical = 16.dp)
        ) {
            when (uiState.currentStep) {
                PasswordResetStep.EMAIL -> {
                    JoinStepHeader(title = "가입하셨던 이메일을\n입력해 주세요.")
                    Spacer(modifier = Modifier.height(48.dp))
                    WyeEmailTextField(
                        value = uiState.email,
                        onValueChange = onEmailChanged,
                        style = WyeTextFieldStyle.Underlined
                    )
                    uiState.errorMessage?.let { message ->
                        Spacer(modifier = Modifier.height(12.dp))
                        Text(
                            text = message,
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.error
                        )
                    }
                    Spacer(modifier = Modifier.weight(1f))
                    WyePrimaryButton(
                        text = "인증번호 발송",
                        onClick = onNextClick,
                        enabled = uiState.canProceed
                    )
                }

                PasswordResetStep.VERIFICATION -> {
                    JoinStepHeader(title = "이메일로 보낸\n인증번호를 입력해 주세요.")
                    Spacer(modifier = Modifier.height(48.dp))
                    WyeVerificationCodeTextField(
                        value = uiState.verificationCode,
                        onValueChange = onVerificationCodeChanged,
                        style = WyeTextFieldStyle.Underlined
                    )
                    Spacer(modifier = Modifier.height(12.dp))
                    JoinVerificationSection(
                        errorText = uiState.errorMessage,
                        helperText = uiState.helperMessage,
                        onResendClick = onResendCodeClick
                    )
                    Spacer(modifier = Modifier.weight(1f))
                    WyePrimaryButton(
                        text = "다음",
                        onClick = onNextClick,
                        enabled = uiState.canProceed
                    )
                }

                PasswordResetStep.NEW_PASSWORD -> {
                    JoinStepHeader(title = "비밀번호를\n변경해 주세요.")
                    Spacer(modifier = Modifier.height(48.dp))
                    WyePasswordTextField(
                        value = uiState.password,
                        onValueChange = onPasswordChanged,
                        isVisible = uiState.isPasswordVisible,
                        onVisibilityToggle = onPasswordVisibilityToggle,
                        style = WyeTextFieldStyle.Underlined
                    )
                    Spacer(modifier = Modifier.height(16.dp))
                    WyePasswordConfirmTextField(
                        value = uiState.passwordConfirm,
                        onValueChange = onPasswordConfirmChanged,
                        isVisible = uiState.isPasswordConfirmVisible,
                        onVisibilityToggle = onPasswordConfirmVisibilityToggle,
                        style = WyeTextFieldStyle.Underlined
                    )
                    Spacer(modifier = Modifier.height(12.dp))
                    Text(
                        text = uiState.errorMessage
                            ?: "영문, 숫자, 특수문자를 포함하여 안전하게 설정해 주세요.",
                        style = MaterialTheme.typography.bodySmall,
                        color = if (uiState.errorMessage == null) {
                            TextSecondary
                        } else {
                            MaterialTheme.colorScheme.error
                        }
                    )
                    Spacer(modifier = Modifier.weight(1f))
                    WyePrimaryButton(
                        text = "비밀번호 변경 완료",
                        onClick = onNextClick,
                        enabled = uiState.canProceed
                    )
                }

                PasswordResetStep.SUCCESS -> {
                    Spacer(modifier = Modifier.height(52.dp))
                    Column(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Box(
                            modifier = Modifier
                                .background(BadgeConservative, CircleShape)
                                .padding(24.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Default.VerifiedUser,
                                contentDescription = null,
                                tint = PrimaryGreen
                            )
                        }
                        Spacer(modifier = Modifier.height(48.dp))
                        Text(
                            text = "비밀번호 변경 완료",
                            style = MaterialTheme.typography.titleLarge
                        )
                        Spacer(modifier = Modifier.height(20.dp))
                        Text(
                            text = "변경된 비밀번호로 다시\n로그인해 주세요.",
                            style = MaterialTheme.typography.bodyMedium,
                            color = TextSecondary,
                            textAlign = TextAlign.Center
                        )
                    }
                    Spacer(modifier = Modifier.weight(1f))
                    WyePrimaryButton(
                        text = "로그인 하러 가기",
                        onClick = onLoginButtonClick
                    )
                }
            }
        }
    }
}

