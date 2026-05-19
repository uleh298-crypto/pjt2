package com.d102.wye.presentation.auth.login.components

import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.d102.wye.presentation.designsystem.WyeKakaoButton

@Composable
fun LoginSocialSection(
    onKakaoLoginClick: () -> Unit,
    kakaoLoginEnabled: Boolean,
) {
    WyeKakaoButton(
        onClick = onKakaoLoginClick,
        enabled = kakaoLoginEnabled,
        modifier = Modifier
            .fillMaxWidth()
            .height(52.dp)
    )
}
