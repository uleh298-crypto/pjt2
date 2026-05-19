package com.d102.wye.presentation.auth.join.components

import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.d102.wye.presentation.designsystem.WyePrimaryButton
import com.d102.wye.presentation.designsystem.WyeTextFieldStyle
import com.d102.wye.presentation.designsystem.WyeVerificationCodeTextField

@Composable
fun ColumnScope.JoinVerificationStep(
    verificationCode: String,
    helperMessage: String?,
    errorMessage: String?,
    canProceed: Boolean,
    onVerificationCodeChanged: (String) -> Unit,
    onResendCodeClick: () -> Unit,
    onNextClick: () -> Unit
) {
    JoinStepHeader(title = "이메일로 보낸\n인증번호를 입력해 주세요.")
    Spacer(modifier = Modifier.height(48.dp))
    WyeVerificationCodeTextField(
        value = verificationCode,
        onValueChange = onVerificationCodeChanged,
        style = WyeTextFieldStyle.Underlined
    )
    Spacer(modifier = Modifier.height(16.dp))
    JoinVerificationSection(
        errorText = errorMessage,
        helperText = helperMessage,
        onResendClick = onResendCodeClick
    )
    Spacer(modifier = Modifier.weight(1f))
    WyePrimaryButton(
        text = "확인",
        onClick = onNextClick,
        enabled = canProceed
    )
}
