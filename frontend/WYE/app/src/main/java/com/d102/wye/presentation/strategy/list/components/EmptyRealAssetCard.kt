package com.d102.wye.presentation.strategy.list.components

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import com.d102.wye.R
import com.d102.wye.presentation.designsystem.DashedContainer
import com.d102.wye.presentation.theme.PrimaryGreen
import com.d102.wye.presentation.theme.TextPrimary

@Composable
fun EmptyRealAssetCard(onConnectClick: () -> Unit = {}) {
    DashedContainer(strokeWidth = 2.dp) {
        Column(
            modifier = Modifier
                .clickable { onConnectClick() }
                .padding(32.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Box(
                modifier = Modifier
                    .size(50.dp)
                    .background(PrimaryGreen.copy(alpha = 0.1f), CircleShape),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    painter = painterResource(id = R.drawable.ic_link),
                    contentDescription = null,
                    tint = PrimaryGreen
                )
            }

            Spacer(modifier = Modifier.height(12.dp))

            Text(
                text = "연결된 실제 ETF 자산이 없습니다.",
                style = MaterialTheme.typography.titleSmall,
                color = TextPrimary
            )

            Spacer(modifier = Modifier.height(8.dp))

            Text(
                text = "∞ 내 자산 연결하기",
                style = MaterialTheme.typography.bodySmall,
                color = PrimaryGreen
            )
        }
    }
}