package com.d102.wye.presentation.auth.login.components

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.d102.wye.presentation.designsystem.WyeTextButton
import com.d102.wye.presentation.theme.TextPrimary
import androidx.compose.material3.MaterialTheme

@Composable
fun LoginFooterLinks(
    onJoinClick: () -> Unit,
    onForgotPasswordClick: () -> Unit
) {
    Column(
        modifier = Modifier.fillMaxWidth(),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        FooterLinkRow(
            prefix = "계정이 없으신가요?",
            action = "가입하기",
            onClick = onJoinClick
        )

        Spacer(modifier = Modifier.height(0.dp))

        FooterLinkRow(
            prefix = "비밀번호를 잊으셨나요?",
            action = "비밀번호 찾기",
            onClick = onForgotPasswordClick
        )
    }
}

@Composable
private fun FooterLinkRow(
    prefix: String,
    action: String,
    onClick: () -> Unit
) {
    Row {
        Text(
            text = prefix,
            color = TextPrimary,
            style = MaterialTheme.typography.bodySmall,
            modifier = Modifier.alignByBaseline()
        )

        WyeTextButton(
            text = action,
            onClick = onClick,
            modifier = Modifier.alignByBaseline()
        )
    }
}
