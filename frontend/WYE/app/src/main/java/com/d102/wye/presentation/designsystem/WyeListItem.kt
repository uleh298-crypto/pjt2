package com.d102.wye.presentation.designsystem

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowRight
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.d102.wye.presentation.theme.*

/**
 * 범용 리스트 아이템
 *
 * ETF 목록, 설정 메뉴, 전략 목록 등 다양한 화면에서 사용.
 *
 * @param title         주 텍스트 (왼쪽)
 * @param subtitle      보조 텍스트 (title 아래, null 이면 숨김)
 * @param trailingText  우측 주 텍스트 (값, 등락률 등)
 * @param trailingSubText 우측 보조 텍스트
 * @param trailingTextColor 우측 텍스트 색상 (등락 색 등)
 * @param showArrow     우측 화살표 표시 여부
 * @param showDivider   하단 구분선 표시 여부
 * @param leadingContent 왼쪽 선행 슬롯 (아이콘, 순위 번호 등)
 * @param onClick       클릭 콜백 (null 이면 클릭 불가)
 */
@Composable
fun WyeListItem(
    title: String,
    modifier: Modifier = Modifier,
    subtitle: String? = null,
    trailingText: String? = null,
    trailingSubText: String? = null,
    trailingTextColor: Color = TextPrimary,
    showArrow: Boolean = false,
    showDivider: Boolean = true,
    leadingContent: (@Composable () -> Unit)? = null,
    onClick: (() -> Unit)? = null,
) {
    Column {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = modifier
                .fillMaxWidth()
                .then(if (onClick != null) Modifier.clickable(onClick = onClick) else Modifier)
                .padding(horizontal = 16.dp, vertical = 14.dp),
        ) {
            // Leading 슬롯
            if (leadingContent != null) {
                leadingContent()
                Spacer(Modifier.width(12.dp))
            }

            // 제목 + 부제목
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = title,
                    color = TextPrimary,
                    fontSize = 15.sp,
                    fontWeight = FontWeight.Medium,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                )
                if (subtitle != null) {
                    Spacer(Modifier.height(2.dp))
                    Text(
                        text = subtitle,
                        color = TextSecondary,
                        fontSize = 12.sp,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                    )
                }
            }

            // Trailing 텍스트
            if (trailingText != null || trailingSubText != null) {
                Spacer(Modifier.width(8.dp))
                Column(horizontalAlignment = Alignment.End) {
                    if (trailingText != null) {
                        Text(
                            text = trailingText,
                            color = trailingTextColor,
                            fontSize = 15.sp,
                            fontWeight = FontWeight.SemiBold,
                        )
                    }
                    if (trailingSubText != null) {
                        Text(
                            text = trailingSubText,
                            color = TextSecondary,
                            fontSize = 12.sp,
                        )
                    }
                }
            }

            // 화살표
            if (showArrow) {
                Spacer(Modifier.width(4.dp))
                Icon(
                    imageVector = Icons.AutoMirrored.Filled.KeyboardArrowRight,
                    contentDescription = null,
                    tint = NavInactive,
                    modifier = Modifier.size(20.dp),
                )
            }
        }

        if (showDivider) {
            HorizontalDivider(
                color = Divider,
                thickness = 1.dp,
                modifier = Modifier.padding(horizontal = 16.dp),
            )
        }
    }
}

// ── Preview ─────────────────────────────────────────────────────

@Preview(showBackground = true, backgroundColor = 0xFFF5F0E8)
@Composable
private fun WyeListItemPreview() {
    Column {
        // ETF 목록 스타일
        WyeListItem(
            title = "KODEX 200",
            subtitle = "삼성자산운용",
            trailingText = "+1.24%",
            trailingSubText = "41,350원",
            trailingTextColor = EtfRise,
            onClick = {},
        )
        WyeListItem(
            title = "TIGER 미국 S&P500",
            subtitle = "미래에셋자산운용",
            trailingText = "-0.98%",
            trailingSubText = "17,820원",
            trailingTextColor = EtfFall,
            onClick = {},
        )
        // 설정 메뉴 스타일
        WyeListItem(
            title = "알림 설정",
            showArrow = true,
            onClick = {},
        )
        WyeListItem(
            title = "공지사항",
            showArrow = true,
            showDivider = false,
            onClick = {},
        )
    }
}
