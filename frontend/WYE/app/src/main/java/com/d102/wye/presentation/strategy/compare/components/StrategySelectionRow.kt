package com.d102.wye.presentation.strategy.compare.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.d102.wye.presentation.strategy.compare.CompareStrategyItem
import com.d102.wye.presentation.theme.PrimaryGreen
import com.d102.wye.presentation.theme.SurfaceVariant
import com.d102.wye.presentation.theme.TextPrimary
import com.d102.wye.presentation.theme.TextSecondary

@Composable
fun StrategySelectionRow(
    item: CompareStrategyItem,
    onClick: () -> Unit,
    indicatorColor: Color
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
            .padding(vertical = 12.dp, horizontal = 16.dp)
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(16.dp)
                    .background(
                        color = if (item.isSelected) indicatorColor else Color.Transparent,
                        shape = CircleShape
                    )
                    .border(
                        width = 1.5.dp,
                        color = if (item.isSelected) indicatorColor else TextSecondary.copy(alpha = 0.5f),
                        shape = CircleShape
                    )
            )

            Spacer(modifier = Modifier.width(12.dp))

            // 포트폴리오 이름
            Text(
                text = item.name,
                style = MaterialTheme.typography.bodyMedium,
                color = TextPrimary
            )

            // 마이데이터 뱃지
            if (item.isMyData) {
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = "내 자산",
                    color = PrimaryGreen,
                    style = MaterialTheme.typography.labelSmall,
                    modifier = Modifier
                        .background(PrimaryGreen.copy(alpha = 0.1f), RoundedCornerShape(4.dp))
                        .padding(horizontal = 6.dp, vertical = 2.dp)
                )
            }
        }

        // 2. 하단 ETF 리스트 (FlowRow로 자동 줄바꿈)
        if (item.etfNames.isNotEmpty()) {
            Spacer(modifier = Modifier.height(8.dp))
            FlowRow(
                modifier = Modifier.padding(start = 28.dp),
                horizontalArrangement = Arrangement.spacedBy(6.dp),
                verticalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                item.etfNames.forEach { etfName ->
                    Text(
                        text = "#$etfName",
                        style = MaterialTheme.typography.labelSmall.copy(fontSize = 11.sp),
                        color = TextSecondary,
                        modifier = Modifier
                            .background(SurfaceVariant.copy(alpha = 0.5f), RoundedCornerShape(4.dp))
                            .padding(horizontal = 6.dp, vertical = 2.dp)
                    )
                }
            }
        }
    }
}