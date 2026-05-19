package com.d102.wye.presentation.designsystem

import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.ErrorOutline
import androidx.compose.material.icons.outlined.SearchOff
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.d102.wye.presentation.theme.NavInactive
import com.d102.wye.presentation.theme.TextPrimary
import com.d102.wye.presentation.theme.TextSecondary

/**
 * 빈 화면 / 에러 상태
 *
 * 검색 결과 없음, 데이터 없음, 에러 등 다양한 빈 상태에 사용.
 *
 * @param message       주 메시지
 * @param description   보조 설명 (null 이면 숨김)
 * @param icon          아이콘 (기본: SearchOff)
 * @param actionLabel   하단 버튼 텍스트 (null 이면 버튼 숨김)
 * @param onAction      버튼 클릭 콜백
 */
@Composable
fun WyeEmptyState(
    message: String,
    modifier: Modifier = Modifier,
    description: String? = null,
    icon: ImageVector = Icons.Outlined.SearchOff,
    actionLabel: String? = null,
    onAction: (() -> Unit)? = null,
) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center,
        modifier = modifier
            .fillMaxWidth()
            .padding(32.dp),
    ) {
        Icon(
            imageVector = icon,
            contentDescription = null,
            tint = NavInactive,
            modifier = Modifier.size(64.dp),
        )
        Spacer(Modifier.height(16.dp))
        Text(
            text = message,
            color = TextPrimary,
            fontSize = 16.sp,
            fontWeight = FontWeight.SemiBold,
            textAlign = TextAlign.Center,
        )
        if (description != null) {
            Spacer(Modifier.height(8.dp))
            Text(
                text = description,
                color = TextSecondary,
                fontSize = 13.sp,
                textAlign = TextAlign.Center,
                lineHeight = 18.sp,
            )
        }
        if (actionLabel != null && onAction != null) {
            Spacer(Modifier.height(24.dp))
            WyeOutlinedButton(
                text = actionLabel,
                onClick = onAction,
                modifier = Modifier.wrapContentWidth(),
            )
        }
    }
}

// ── Preview ─────────────────────────────────────────────────────

@Preview(showBackground = true, backgroundColor = 0xFFF5F0E8, name = "검색 결과 없음")
@Composable
private fun WyeEmptyStateSearchPreview() {
    WyeEmptyState(
        message = "검색 결과가 없어요",
        description = "다른 키워드로 검색해보세요",
        icon = Icons.Outlined.SearchOff,
        actionLabel = "다시 검색",
        onAction = {},
    )
}

@Preview(showBackground = true, backgroundColor = 0xFFF5F0E8, name = "에러 상태")
@Composable
private fun WyeEmptyStateErrorPreview() {
    WyeEmptyState(
        message = "데이터를 불러오지 못했어요",
        description = "네트워크 연결을 확인하고\n다시 시도해주세요",
        icon = Icons.Outlined.ErrorOutline,
        actionLabel = "다시 시도",
        onAction = {},
    )
}
