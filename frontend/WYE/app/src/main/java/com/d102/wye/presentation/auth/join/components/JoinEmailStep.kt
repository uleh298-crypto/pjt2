package com.d102.wye.presentation.auth.join.components

import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.d102.wye.presentation.designsystem.WyeEmailTextField
import com.d102.wye.presentation.designsystem.WyePrimaryButton
import com.d102.wye.presentation.designsystem.WyeTextFieldStyle
import com.d102.wye.presentation.theme.TextSecondary

@Composable
fun ColumnScope.JoinEmailStep(
    email: String,
    errorMessage: String?,
    canProceed: Boolean,
    onEmailChanged: (String) -> Unit,
    onNextClick: () -> Unit
) {
    JoinStepHeader(title = "가입하실 이메일을\n입력해 주세요.")
    Spacer(modifier = Modifier.height(48.dp))
    WyeEmailTextField(
        value = email,
        onValueChange = onEmailChanged,
        style = WyeTextFieldStyle.Underlined
    )
    Spacer(modifier = Modifier.height(12.dp))
    Text(
        text = errorMessage ?: "가입하실 이메일 주소를 정확히 입력해 주세요.",
        style = MaterialTheme.typography.bodySmall,
        color = if (errorMessage == null) TextSecondary else MaterialTheme.colorScheme.error
    )
    Spacer(modifier = Modifier.weight(1f))
    WyePrimaryButton(
        text = "다음",
        onClick = onNextClick,
        enabled = canProceed
    )
}
