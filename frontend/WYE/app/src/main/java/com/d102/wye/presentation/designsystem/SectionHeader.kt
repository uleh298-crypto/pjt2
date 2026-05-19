package com.d102.wye.presentation.designsystem

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.d102.wye.presentation.theme.PrimaryGreen
import com.d102.wye.presentation.theme.TextPrimary

/**
 * 섹션 헤더
 *
 * 홈 화면의 "실시간 ETF 뉴스 | 전체보기" 처럼
 * 섹션 타이틀과 선택적 "전체보기" 링크를 가로로 배치합니다.
 *
 * @param title         섹션 제목
 * @param actionLabel   우측 링크 텍스트 (null 이면 표시 안 함)
 * @param onActionClick 링크 클릭 콜백
 */
@Composable
fun SectionHeader(
    title: String,
    modifier: Modifier = Modifier,
    actionLabel: String? = "전체보기",
    onActionClick: (() -> Unit)? = null,
) {
    Row(
        modifier = modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Text(
            text = title,
            color = TextPrimary,
            fontSize = 16.sp,
            fontWeight = FontWeight.SemiBold,
        )

        if (actionLabel != null && onActionClick != null) {
            Text(
                text = actionLabel,
                color = PrimaryGreen,
                fontSize = 13.sp,
                fontWeight = FontWeight.Medium,
                modifier = Modifier.clickable(onClick = onActionClick),
            )
        }
    }
}