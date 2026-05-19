package com.d102.wye.presentation.designsystem

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.d102.wye.presentation.theme.Divider
import com.d102.wye.presentation.theme.SurfaceWhite
import com.d102.wye.presentation.theme.TextPrimary
import com.d102.wye.presentation.theme.TextSecondary

/**
 * 범용 카드 컨테이너
 *
 * 흰 배경 + 그림자/테두리 스타일을 통일.
 * 내부 콘텐츠는 [content] 슬롯으로 자유롭게 구성.
 *
 * @param onClick     null 이면 클릭 불가(non-interactive)
 * @param elevation   그림자 높이 (기본 2dp)
 * @param border      테두리 (기본 없음, 필요 시 BorderStroke 전달)
 * @param cornerRadius 모서리 반경
 * @param innerPadding 내부 패딩
 */
@Composable
fun WyeCard(
    modifier: Modifier = Modifier,
    onClick: (() -> Unit)? = null,
    containerColor: Color = SurfaceWhite,
    elevation: Dp = 2.dp,
    border: BorderStroke? = null,
    cornerRadius: Dp = 12.dp,
    innerPadding: PaddingValues = PaddingValues(16.dp),
    content: @Composable ColumnScope.() -> Unit,
) {
    val shape = RoundedCornerShape(cornerRadius)
    val colors = CardDefaults.cardColors(containerColor = containerColor)
    val cardElevation = CardDefaults.cardElevation(defaultElevation = elevation)

    if (onClick != null) {
        Card(
            onClick = onClick,
            shape = shape,
            colors = colors,
            elevation = cardElevation,
            border = border,
            modifier = modifier,
        ) {
            Column(modifier = Modifier.padding(innerPadding)) { content() }
        }
    } else {
        Card(
            shape = shape,
            colors = colors,
            elevation = cardElevation,
            border = border,
            modifier = modifier,
        ) {
            Column(modifier = Modifier.padding(innerPadding)) { content() }
        }
    }
}

/**
 * 테두리 스타일 카드 (elevation 없이 테두리만)
 */
@Composable
fun WyeOutlinedCard(
    modifier: Modifier = Modifier,
    onClick: (() -> Unit)? = null,
    cornerRadius: Dp = 12.dp,
    innerPadding: PaddingValues = PaddingValues(16.dp),
    content: @Composable ColumnScope.() -> Unit,
) {
    WyeCard(
        modifier = modifier,
        onClick = onClick,
        elevation = 0.dp,
        border = BorderStroke(1.dp, Divider),
        cornerRadius = cornerRadius,
        innerPadding = innerPadding,
        content = content,
    )
}

// ── Preview ─────────────────────────────────────────────────────

@Preview(showBackground = true, backgroundColor = 0xFFF5F0E8)
@Composable
private fun WyeCardPreview() {
    Column(
        modifier = Modifier.padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        WyeCard {
            Text("카드 제목", fontSize = 16.sp, fontWeight = FontWeight.Bold, color = TextPrimary)
            Spacer(Modifier.height(4.dp))
            Text("카드 본문 내용입니다.", fontSize = 14.sp, color = TextSecondary)
        }
        WyeOutlinedCard {
            Text("아웃라인 카드", fontSize = 16.sp, fontWeight = FontWeight.Bold, color = TextPrimary)
            Spacer(Modifier.height(4.dp))
            Text("테두리 스타일 카드입니다.", fontSize = 14.sp, color = TextSecondary)
        }
    }
}
