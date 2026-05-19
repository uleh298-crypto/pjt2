package com.d102.wye.presentation.auth.login.components

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.size
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.VisibilityOff
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import com.d102.wye.presentation.auth.login.LoginUiState
import com.d102.wye.presentation.designsystem.WyePrimaryButton
import com.d102.wye.presentation.designsystem.WyeTextField
import com.d102.wye.presentation.designsystem.WyeTextFieldStyle
import com.d102.wye.presentation.theme.TextOnColored
import com.d102.wye.presentation.theme.TextSecondary

@Composable
fun LoginFormSection(
    uiState: LoginUiState,
    onEmailChanged: (String) -> Unit,
    onPasswordChanged: (String) -> Unit,
    onPasswordVisibilityToggle: () -> Unit,
    onLoginClick: () -> Unit
) {
    Column(modifier = Modifier.fillMaxWidth()) {
        Text(
            text = "이메일 주소",
            style = MaterialTheme.typography.labelMedium,
            color = TextSecondary
        )
        WyeTextField(
            value = uiState.email,
            onValueChange = onEmailChanged,
            placeholder = "user@example.com",
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Email),
            modifier = Modifier.fillMaxWidth(),
            style = WyeTextFieldStyle.Underlined
        )
    }

    Spacer(modifier = Modifier.height(16.dp))

    Column(modifier = Modifier.fillMaxWidth()) {
        Text(
            text = "비밀번호",
            style = MaterialTheme.typography.labelMedium,
            color = TextSecondary
        )
        WyeTextField(
            value = uiState.password,
            onValueChange = onPasswordChanged,
            placeholder = "8자 이상 입력하세요",
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password),
            visualTransformation = if (uiState.isPasswordVisible) VisualTransformation.None else PasswordVisualTransformation(),
            trailingIcon = if (uiState.isPasswordVisible) Icons.Filled.VisibilityOff else Icons.Filled.Visibility,
            trailingIconDescription = if (uiState.isPasswordVisible) "비밀번호 숨기기" else "비밀번호 보기",
            onTrailingIconClick = onPasswordVisibilityToggle,
            modifier = Modifier.fillMaxWidth(),
            style = WyeTextFieldStyle.Underlined
        )
    }

    uiState.errorMessage?.let { errorMessage ->
        Spacer(modifier = Modifier.height(12.dp))
        Text(
            text = errorMessage,
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.error,
            modifier = Modifier.fillMaxWidth()
        )
    }

    Spacer(modifier = Modifier.height(28.dp))

    Box(
        modifier = Modifier.fillMaxWidth(),
        contentAlignment = Alignment.Center
    ) {
        WyePrimaryButton(
            text = if (uiState.isLoading) "" else "로그인",
            onClick = onLoginClick,
            enabled = !uiState.isLoading,
            style = MaterialTheme.typography.titleSmall,
            modifier = Modifier
                .fillMaxWidth()
                .height(54.dp)
        )

        if (uiState.isLoading) {
            CircularProgressIndicator(
                modifier = Modifier.size(20.dp),
                strokeWidth = 2.dp,
                color = TextOnColored
            )
        }
    }
}
