package com.d102.wye.presentation.designsystem

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
import androidx.compose.foundation.clickable
import androidx.compose.ui.draw.clip
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.d102.wye.R
import com.d102.wye.presentation.theme.PrimaryGreen
import com.d102.wye.presentation.theme.TextOnColored

/**
 * Primary 버튼 — 채워진 녹색, 화면 하단 주요 CTA
 *
 * Figma: button/android/large/text/default
 * 기본 full-width. 작게 쓸 땐 modifier로 width 조절.
 */
@Composable
fun WyePrimaryButton(
    modifier: Modifier = Modifier,
    text: String,
    onClick: () -> Unit,
    backgroundColor: Color = PrimaryGreen,
    textColor: Color = TextOnColored,
    enabled: Boolean = true,
    style: TextStyle = MaterialTheme.typography.bodyMedium
) {
    Button(
        onClick = onClick,
        enabled = enabled,
        shape = RoundedCornerShape(12.dp),
        colors = ButtonDefaults.buttonColors(
            containerColor = backgroundColor,
            contentColor = textColor,
            disabledContainerColor = PrimaryGreen.copy(alpha = 0.4f),
            disabledContentColor = TextOnColored.copy(alpha = 0.6f),
        ),
        contentPadding = PaddingValues(horizontal = 20.dp, vertical = 16.dp),
        modifier = modifier.fillMaxWidth(),
    ) {
        Text(text = text, style = style)
    }
}

/**
 * Outlined 버튼 — 테두리만, 보조 액션/취소
 */
@Composable
fun WyeOutlinedButton(
    text: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    verticalPaddingValues: Dp = 14.dp,
    textColor: Color = PrimaryGreen,
    borderColor: Color = PrimaryGreen,
    enabled: Boolean = true,
    style: TextStyle = MaterialTheme.typography.bodyMedium
) {
    OutlinedButton(
        onClick = onClick,
        enabled = enabled,
        shape = RoundedCornerShape(12.dp),
        border = BorderStroke(width = 1.dp, color = borderColor),
        colors = ButtonDefaults.outlinedButtonColors(contentColor = PrimaryGreen),
        contentPadding = PaddingValues(horizontal = 20.dp, vertical = verticalPaddingValues),
        modifier = modifier.fillMaxWidth(),
    ) {
        Text(text = text, style = style)
    }
}

/**
 * Text 버튼 — "전체보기" 같은 링크성 텍스트 버튼
 */
@Composable
fun WyeTextButton(
    text: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    style: TextStyle = MaterialTheme.typography.bodySmall
) {
    TextButton(
        onClick = onClick,
        colors = ButtonDefaults.textButtonColors(contentColor = PrimaryGreen),
        modifier = modifier,
    ) {
        Text(text = text, style = style)
    }
}

/**
 * 카카오 버튼 — 카카오 로그인 전용 버튼
 */
@Composable
fun WyeKakaoButton(
    text: String = "카카오 로그인",
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
) {
    Image(
        painter = painterResource(id = R.drawable.kakao_login_medium_wide),
        contentDescription = text,
        contentScale = ContentScale.FillWidth,
        modifier = modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(12.dp))
            .graphicsLayer(alpha = if (enabled) 1f else 0.4f)
            .clickable(enabled = enabled, onClick = onClick),
    )
}

// ── Preview ─────────────────────────────────────────────────────

@Preview(showBackground = true, backgroundColor = 0xFFF5F0E8)
@Composable
private fun WyeButtonPreview() {
    Column(
        modifier = Modifier.padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(10.dp),
    ) {
        WyePrimaryButton(text = "시뮬레이션 시작", onClick = {})
        WyePrimaryButton(text = "비활성화 버튼", onClick = {}, enabled = false)
        WyeOutlinedButton(text = "나중에 하기", onClick = {})
        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            WyePrimaryButton(text = "저장", onClick = {}, modifier = Modifier.weight(1f))
            WyeOutlinedButton(text = "취소", onClick = {}, modifier = Modifier.weight(1f))
        }
        WyeKakaoButton(onClick = {})
        WyeTextButton(text = "전체보기", onClick = {})
    }
}
