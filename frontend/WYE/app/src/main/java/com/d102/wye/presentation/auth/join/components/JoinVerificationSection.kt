package com.d102.wye.presentation.auth.join.components

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import com.d102.wye.presentation.designsystem.WyeTextButton
import com.d102.wye.presentation.theme.TextSecondary

@Composable
fun JoinVerificationSection(
    errorText: String?,
    helperText: String?,
    onResendClick: () -> Unit
) {
    Column(modifier = Modifier.fillMaxWidth()) {
        if (errorText != null) {
            Text(
                text = errorText,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.error
            )
        }

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.Start
        ) {
            if (helperText != null) {
                WyeTextButton(
                    text = helperText,
                    onClick = onResendClick
                )
            } else {
                Text(
                    text = "",
                    style = MaterialTheme.typography.bodySmall,
                    color = TextSecondary
                )
            }
        }
    }
}
