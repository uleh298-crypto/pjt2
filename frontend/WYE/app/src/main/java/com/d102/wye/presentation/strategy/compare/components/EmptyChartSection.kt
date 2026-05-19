package com.d102.wye.presentation.strategy.compare.components

import androidx.compose.foundation.background
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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.d102.wye.R
import com.d102.wye.presentation.strategy.detail.components.RoundedSurface
import com.d102.wye.presentation.theme.SurfaceVariant
import com.d102.wye.presentation.theme.TextPrimary
import com.d102.wye.presentation.theme.TextSecondary


// ─────────────────────────────────────────
// 비어있는 차트 섹션 (Empty State)
// ─────────────────────────────────────────
@Composable
fun EmptyChartSection() {
    RoundedSurface(
        horizontalPaddingValue = 0.dp,
    ) {
        Column(
            modifier = Modifier.padding(vertical = 24.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Box(
                modifier = Modifier
                    .size(64.dp)
                    .background(SurfaceVariant, CircleShape),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    painter = painterResource(id = R.drawable.ic_chart),
                    contentDescription = null,
                    tint = Color.Unspecified
                )
            }
            Spacer(modifier = Modifier.height(16.dp))
            Text(
                text = "위에서 비교할 전략을 선택해 주세요",
                style = MaterialTheme.typography.labelLarge.copy(fontSize = 16.sp),
                color = TextPrimary
            )
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = "상단의 체크를 선택하여 수익률을 비교해 보세요",
                style = MaterialTheme.typography.labelMedium,
                color = TextSecondary
            )
        }
    }
}
