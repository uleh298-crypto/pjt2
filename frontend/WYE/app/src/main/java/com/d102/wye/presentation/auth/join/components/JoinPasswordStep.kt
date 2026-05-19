package com.d102.wye.presentation.auth.join.components

import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.d102.wye.presentation.designsystem.WyePasswordConfirmTextField
import com.d102.wye.presentation.designsystem.WyePasswordTextField
import com.d102.wye.presentation.designsystem.WyePrimaryButton
import com.d102.wye.presentation.designsystem.WyeTextFieldStyle
import com.d102.wye.presentation.theme.TextSecondary

@Composable
fun ColumnScope.JoinPasswordStep(
    email: String,
    password: String,
    passwordConfirm: String,
    isPasswordVisible: Boolean,
    isPasswordConfirmVisible: Boolean,
    errorMessage: String?,
    canProceed: Boolean,
    onPasswordChanged: (String) -> Unit,
    onPasswordConfirmChanged: (String) -> Unit,
    onPasswordVisibilityToggle: () -> Unit,
    onPasswordConfirmVisibilityToggle: () -> Unit,
    onNextClick: () -> Unit
) {
    JoinStepHeader(
        title = "비밀번호를\n설정해 주세요.",
        subtitle = "$email 계정으로 가입합니다."
    )
    Spacer(modifier = Modifier.height(48.dp))
    WyePasswordTextField(
        value = password,
        onValueChange = onPasswordChanged,
        isVisible = isPasswordVisible,
        onVisibilityToggle = onPasswordVisibilityToggle,
        style = WyeTextFieldStyle.Underlined
    )
    Spacer(modifier = Modifier.height(16.dp))
    WyePasswordConfirmTextField(
        value = passwordConfirm,
        onValueChange = onPasswordConfirmChanged,
        isVisible = isPasswordConfirmVisible,
        onVisibilityToggle = onPasswordConfirmVisibilityToggle,
        style = WyeTextFieldStyle.Underlined
    )
    Spacer(modifier = Modifier.height(12.dp))
    Text(
        text = errorMessage ?: "영문, 숫자, 특수문자를 포함하여 안전하게 설정해 주세요.",
        style = MaterialTheme.typography.bodySmall,
        color = if (errorMessage == null) {
            TextSecondary
        } else {
            MaterialTheme.colorScheme.error
        }
    )
    Spacer(modifier = Modifier.weight(1f))
    WyePrimaryButton(
        text = "가입 완료",
        onClick = onNextClick,
        enabled = canProceed
    )
}
