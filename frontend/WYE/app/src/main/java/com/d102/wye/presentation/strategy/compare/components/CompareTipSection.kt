package com.d102.wye.presentation.strategy.compare.components

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import com.d102.wye.R
import com.d102.wye.presentation.strategy.detail.components.RoundedSurface
import com.d102.wye.presentation.theme.PrimaryGreen
import com.d102.wye.presentation.theme.TextSecondary
import com.d102.wye.presentation.theme.TipBackground
import com.d102.wye.presentation.theme.TipBorder


// ─────────────────────────────────────────
// 하단 비교 팁 섹션
// ─────────────────────────────────────────
@Composable
fun CompareTipSection() {
    Box(
        modifier = Modifier.fillMaxWidth().padding(vertical = 16.dp)
    ){
        RoundedSurface(
            horizontalPaddingValue = 0.dp,
            backgroundColor = TipBackground,
            borderColor = TipBorder
        ) {
            Row(
                modifier = Modifier.padding(16.dp),
                verticalAlignment = Alignment.Top
            ) {
                Icon(
                    painter = painterResource(id = R.drawable.ic_tip),
                    contentDescription = null,
                    tint = PrimaryGreen,
                    modifier = Modifier.size(20.dp)
                )
                Spacer(modifier = Modifier.width(12.dp))
                Column {
                    Text(
                        text = "비교 팁",
                        style = MaterialTheme.typography.labelMedium,
                        color = PrimaryGreen
                    )
                    Spacer(modifier = Modifier.height(6.dp))
                    Text(
                        text = "두 개 이상의 전략을 선택하여 기간별 성과와 위험 지표를 한 눈에 비교할 수 있습니다.",
                        style = MaterialTheme.typography.bodySmall,
                        color = TextSecondary,
                    )
                }
            }
        }

    }
}