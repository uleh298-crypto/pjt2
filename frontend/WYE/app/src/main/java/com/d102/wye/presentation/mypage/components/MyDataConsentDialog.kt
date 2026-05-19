package com.d102.wye.presentation.mypage.components

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Checkbox
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import com.d102.wye.presentation.designsystem.WyeOutlinedButton
import com.d102.wye.presentation.designsystem.WyePrimaryButton
import com.d102.wye.presentation.theme.BackGroundLightGreen3
import com.d102.wye.presentation.theme.Divider
import com.d102.wye.presentation.theme.PrimaryGreen
import com.d102.wye.presentation.theme.TextPrimary
import com.d102.wye.presentation.theme.TextSecondary

@Composable
fun MyDataConsentDialog(
    isChecked: Boolean,
    onCheckedChange: (Boolean) -> Unit,
    onDismiss: () -> Unit,
    onConfirm: () -> Unit,
) {
    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(usePlatformDefaultWidth = false)
    ) {
        Surface(
            modifier = Modifier.fillMaxWidth(0.9f),
            shape = RoundedCornerShape(24.dp),
            color = Color.White
        ) {
            Column(
                modifier = Modifier.padding(24.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(
                    text = "마이데이터 연동",
                    style = MaterialTheme.typography.titleMedium,
                    color = TextPrimary
                )

                Spacer(modifier = Modifier.height(14.dp))

                Text(
                    text = "보유 ETF, 현재 가격, 등락률을 불러오기 위해 마이데이터 활용 동의가 필요합니다.",
                    style = MaterialTheme.typography.bodyMedium,
                    color = TextSecondary,
                    textAlign = TextAlign.Center
                )

                Spacer(modifier = Modifier.height(20.dp))

                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(BackGroundLightGreen3, RoundedCornerShape(16.dp))
                        .padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    Text(
                        text = "동의 항목",
                        style = MaterialTheme.typography.labelLarge,
                        color = PrimaryGreen
                    )
                    Text(
                        text = "보유 ETF 조회를 위한 마이데이터 활용에 동의합니다.",
                        style = MaterialTheme.typography.bodySmall,
                        color = TextSecondary
                    )
                }

                Spacer(modifier = Modifier.height(16.dp))

                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable { onCheckedChange(!isChecked) }
                        .padding(horizontal = 4.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Checkbox(
                        checked = isChecked,
                        onCheckedChange = onCheckedChange
                    )
                    Text(
                        text = "마이데이터 활용에 동의합니다.",
                        style = MaterialTheme.typography.bodyMedium,
                        color = TextPrimary
                    )
                }

                Spacer(modifier = Modifier.height(24.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    WyeOutlinedButton(
                        text = "취소",
                        onClick = onDismiss,
                        modifier = Modifier.weight(1f),
                        borderColor = Divider,
                        textColor = TextSecondary
                    )
                    WyePrimaryButton(
                        text = "연동하기",
                        onClick = onConfirm,
                        modifier = Modifier.weight(1f),
                        enabled = isChecked
                    )
                }
            }
        }
    }
}
