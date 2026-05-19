package com.d102.wye.presentation.mypage.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import com.d102.wye.presentation.designsystem.WyePrimaryButton
import com.d102.wye.presentation.theme.Divider
import com.d102.wye.presentation.theme.EtfRise
import com.d102.wye.presentation.theme.IconInactive
import com.d102.wye.presentation.theme.PrimaryGreen
import com.d102.wye.presentation.theme.SurfaceWhite
import com.d102.wye.presentation.theme.SurfaceVariant
import com.d102.wye.presentation.theme.TextPrimary
import com.d102.wye.presentation.theme.TextSecondary

@Composable
fun NicknameEditDialog(
    currentNickname: String,
    nicknameDraft: String,
    validationMessage: String?,
    isSaving: Boolean,
    onNicknameChange: (String) -> Unit,
    onDismiss: () -> Unit,
    onSave: () -> Unit
) {
    val canSave = nicknameDraft.trim().isNotEmpty() && !isSaving

    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(usePlatformDefaultWidth = false)
    ) {
        Surface(
            modifier = Modifier.fillMaxWidth(0.9f),
            shape = RoundedCornerShape(24.dp),
            color = SurfaceWhite
        ) {
            Column(
                modifier = Modifier.padding(24.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(
                    text = "닉네임 변경",
                    style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.SemiBold),
                    color = TextPrimary
                )

                Spacer(modifier = Modifier.height(12.dp))

                Text(
                    text = "마이페이지에서 보여지는 이름을 수정할 수 있습니다.",
                    style = MaterialTheme.typography.bodyMedium,
                    color = TextSecondary
                )

                Spacer(modifier = Modifier.height(24.dp))

                Column(modifier = Modifier.fillMaxWidth()) {
                    Text(
                        text = "현재 닉네임",
                        style = MaterialTheme.typography.labelMedium,
                        color = PrimaryGreen
                    )

                    Spacer(modifier = Modifier.height(6.dp))

                    Text(
                        text = currentNickname,
                        style = MaterialTheme.typography.bodyMedium,
                        color = TextSecondary
                    )

                    Spacer(modifier = Modifier.height(18.dp))

                    Text(
                        text = "새 닉네임",
                        style = MaterialTheme.typography.labelMedium,
                        color = PrimaryGreen
                    )

                    Spacer(modifier = Modifier.height(8.dp))

                    OutlinedTextField(
                        value = nicknameDraft,
                        onValueChange = onNicknameChange,
                        modifier = Modifier.fillMaxWidth(),
                        singleLine = true,
                        shape = RoundedCornerShape(12.dp),
                        textStyle = MaterialTheme.typography.bodyMedium.copy(color = TextPrimary),
                        placeholder = {
                            Text(
                                text = "새 닉네임을 입력하세요",
                                style = MaterialTheme.typography.bodyMedium,
                                color = IconInactive
                            )
                        },
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = PrimaryGreen,
                            unfocusedBorderColor = Divider,
                            focusedContainerColor = SurfaceWhite,
                            unfocusedContainerColor = SurfaceWhite,
                            cursorColor = TextPrimary
                        )
                    )

                    Spacer(modifier = Modifier.height(8.dp))

                    Text(
                        text = validationMessage
                            ?: "닉네임은 2자 이상 20자 이하, 한글/영문/숫자만 사용할 수 있습니다.",
                        modifier = Modifier.fillMaxWidth(),
                        style = MaterialTheme.typography.labelSmall,
                        color = if (validationMessage != null) EtfRise else TextSecondary,
                        textAlign = TextAlign.Center,
                    )
                }

                Spacer(modifier = Modifier.height(28.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    WyePrimaryButton(
                        modifier = Modifier.weight(1f),
                        text = "취소",
                        backgroundColor = SurfaceVariant,
                        textColor = TextSecondary,
                        onClick = onDismiss
                    )

                    WyePrimaryButton(
                        modifier = Modifier.weight(1f),
                        text = if (isSaving) "저장 중..." else "저장",
                        enabled = canSave,
                        onClick = onSave
                    )
                }
            }
        }
    }
}
