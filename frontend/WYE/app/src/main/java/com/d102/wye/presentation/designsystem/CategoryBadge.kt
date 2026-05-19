package com.d102.wye.presentation.designsystem

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.d102.wye.presentation.theme.*

/**
 * 카테고리 뱃지
 *
 * 뉴스 카드나 ETF 목록에서 카테고리를 표시하는 작은 pill 형태 뱃지.
 *
 * @param label           표시할 텍스트
 * @param backgroundColor 뱃지 배경색
 * @param textColor       텍스트 색상 (기본 흰색)
 */
@Composable
fun CategoryBadge(
    label: String,
    modifier: Modifier = Modifier,
    backgroundColor: Color = BadgeConservative,
    textColor: Color = BadgeConservativeFont,
    isPill: Boolean = false,
) {
    Text(
        text = label,
        color = textColor,
        fontSize = 11.sp,
        lineHeight = if (isPill) 11.sp else 14.sp,
        fontWeight = if (isPill) FontWeight.Normal else FontWeight.SemiBold,
        modifier = modifier
            .clip(if (isPill) RoundedCornerShape(20.dp) else RoundedCornerShape(4.dp))
            .background(backgroundColor)
            .padding(
                horizontal = if (isPill) 12.dp else 8.dp,
                vertical = if (isPill) 1.dp else 3.dp,
            ),
    )
}

@Preview(showBackground = true, backgroundColor = 0xFFF5F0E8)
@Composable
private fun CategoryBadgePreview() {
    androidx.compose.foundation.layout.Row(
        horizontalArrangement = androidx.compose.foundation.layout.Arrangement.spacedBy(8.dp),
        modifier = Modifier.padding(16.dp),
    ) {
        CategoryBadge("안정형",     backgroundColor = BadgeConservative,       textColor = BadgeConservativeFont)
        CategoryBadge("안정추구형", backgroundColor = BadgeConservativeGrowth,  textColor = BadgeConservativeGrowthFont)
        CategoryBadge("위험중립형", backgroundColor = BadgeNeutral,             textColor = BadgeNeutralFont)
        CategoryBadge("적극투자형", backgroundColor = BadgeActive,              textColor = BadgeActiveFont)
        CategoryBadge("공격투자형", backgroundColor = BadgeAggressive,          textColor = BadgeAggressiveFont)
    }
}
