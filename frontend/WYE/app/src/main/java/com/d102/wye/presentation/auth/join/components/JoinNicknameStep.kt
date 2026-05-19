package com.d102.wye.presentation.auth.join.components

import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.d102.wye.presentation.designsystem.WyeNicknameTextField
import com.d102.wye.presentation.designsystem.WyePrimaryButton
import com.d102.wye.presentation.designsystem.WyeTextFieldStyle
import com.d102.wye.presentation.theme.TextSecondary

@Composable
fun ColumnScope.JoinNicknameStep(
    nickname: String,
    errorMessage: String?,
    canProceed: Boolean,
    onNicknameChanged: (String) -> Unit,
    onNextClick: () -> Unit
) {
    JoinStepHeader(title = "사용하실 닉네임을\n입력해 주세요.")
    Spacer(modifier = Modifier.height(48.dp))
    WyeNicknameTextField(
        value = nickname,
        onValueChange = onNicknameChanged,
        style = WyeTextFieldStyle.Underlined
    )
    Spacer(modifier = Modifier.height(12.dp))
    Text(
        text = errorMessage ?: "닉네임은 2자 이상 20자 이하로 입력해 주세요.",
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
