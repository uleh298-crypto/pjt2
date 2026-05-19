package com.d102.wye.presentation.designsystem

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.d102.wye.presentation.theme.*

/**
 * 뱃지 스타일: 채워진(Filled) vs 테두리만(Outlined)
 */
enum class WyeBadgeStyle { FILLED, OUTLINED }

/**
 * 범용 뱃지
 *
 * 카테고리 태그, 상태 표시, 라벨 등 다양한 용도로 사용.
 *
 * @param label     표시할 텍스트
 * @param color     뱃지 주 색상 (배경 or 테두리에 사용)
 * @param style     FILLED(채운 배경) / OUTLINED(테두리만)
 * @param textColor 텍스트 색상 (FILLED 기본: 흰색, OUTLINED 기본: color와 동일)
 */
@Composable
fun WyeBadge(
    modifier: Modifier = Modifier,
    label: String,
    shape: Shape = RoundedCornerShape(4.dp),
    textStyle: TextStyle = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.SemiBold),
    textHorizontalPadding: Int = 8,
    textVerticalPadding: Int = 3,
    color: Color = BadgeConservative,
    style: WyeBadgeStyle = WyeBadgeStyle.FILLED,
    textColor: Color = if (style == WyeBadgeStyle.FILLED) TextOnColored else color,
) {
    val bgModifier = when (style) {
        WyeBadgeStyle.FILLED -> Modifier.background(color)
        WyeBadgeStyle.OUTLINED -> Modifier.border(1.dp, color, shape)
    }

    Text(
        text = label,
        color = textColor,
        style = textStyle,
        modifier = modifier
            .clip(shape)
            .then(bgModifier)
            .padding(horizontal = textHorizontalPadding.dp, vertical = textVerticalPadding.dp),
    )
}

/**
 * 수치 뱃지 (알림 카운트 등)
 *
 * @param count  표시할 숫자 (99 초과 시 "99+" 표시)
 */
@Composable
fun WyeCountBadge(
    count: Int,
    modifier: Modifier = Modifier,
    color: Color = BadgeConservative,
) {
    val text = if (count > 99) "99+" else count.toString()
    Box(
        contentAlignment = Alignment.Center,
        modifier = modifier
            .defaultMinSize(minWidth = 18.dp, minHeight = 18.dp)
            .clip(RoundedCornerShape(50))
            .background(color)
            .padding(horizontal = 4.dp),
    ) {
        Text(text = text, color = TextOnColored, fontSize = 10.sp, fontWeight = FontWeight.Bold)
    }
}

// ────────────────────────────────────────────────────────────────
//  선택형 칩 (투자 성향 선택 등)
// ────────────────────────────────────────────────────────────────

/**
 * 선택형 칩
 * - 미선택: 흰 배경 + 회색 테두리
 * - 선택됨: 초록 배경 + 흰 텍스트
 *
 * @param label       칩 제목
 * @param description 칩 설명 (null 이면 숨김)
 * @param selected    선택 상태
 * @param onClick     클릭 콜백
 */
@Composable
fun WyeSelectableChip(
    modifier: Modifier = Modifier,
    label: String,
    textStyle: TextStyle = MaterialTheme.typography.labelLarge,
    selected: Boolean,
    onClick: () -> Unit,
    description: String? = null,
    unselectedColor: Color = SurfaceWhite,
    showBorder: Boolean = true,
) {
    val shape = RoundedCornerShape(12.dp)
    val bgColor   = if (selected) PrimaryGreen else unselectedColor
    val border    = if (selected) PrimaryGreen else Divider
    val titleColor = if (selected) TextOnColored else TextPrimary
    val descColor  = if (selected) TextOnColored.copy(alpha = 0.8f) else TextSecondary

    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center,
        modifier = modifier
            .clip(shape)
            .background(bgColor)
            .then(if (showBorder) Modifier.border(1.dp, border, shape) else Modifier)
            .clickable(onClick = onClick)
            .padding(horizontal = 12.dp, vertical = 14.dp),
    ) {
        Text(
            text = label,
            style = textStyle.copy(fontWeight = FontWeight.SemiBold),
            color = titleColor,
            textAlign = TextAlign.Center,
        )
        if (description != null) {
            Spacer(Modifier.height(4.dp))
            Text(
                text = description,
                color = descColor,
                style = textStyle.copy(fontSize = 11.sp),
            )
        }
    }
}

// ── Preview ─────────────────────────────────────────────────────

@Preview(showBackground = true, backgroundColor = 0xFFF5F0E8)
@Composable
private fun WyeBadgePreview() {
    Column(
        modifier = Modifier.padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        // Filled 스타일
        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            WyeBadge(label = "안정형",     color = InvestmentTypeColor.CONSERVATIVE,        textColor = InvestmentTypeColor.CONSERVATIVE_FONT)
            WyeBadge(label = "안정추구형", color = InvestmentTypeColor.CONSERVATIVE_GROWTH,  textColor = InvestmentTypeColor.CONSERVATIVE_GROWTH_FONT)
            WyeBadge(label = "위험중립형", color = InvestmentTypeColor.NEUTRAL,              textColor = InvestmentTypeColor.NEUTRAL_FONT)
            WyeBadge(label = "적극투자형", color = InvestmentTypeColor.ACTIVE,               textColor = InvestmentTypeColor.ACTIVE_FONT)
            WyeBadge(label = "공격투자형", color = InvestmentTypeColor.AGGRESSIVE,           textColor = InvestmentTypeColor.AGGRESSIVE_FONT)
        }
    }
}

@Preview(showBackground = true, backgroundColor = 0xFFF5F0E8, name = "SelectableChip")
@Composable
private fun WyeSelectableChipPreview() {
    val items = listOf(
        "안정형"    to "예금 보호 확정\n금리 추구형",
        "안정추구형" to "투자원금 손실\n최소화",
        "위험중립형" to "투자에 따른 수\n익과 손실 인지",
        "적극투자형" to "투자 원금 대비\n손실 위험 감내",
        "공격투자형" to "고수익을 위한\n고위험 감수",
    )
    var selected by remember { mutableStateOf("안정형") }

    Column(
        modifier = Modifier.padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp),
    ) {
        // 3열 첫 줄
        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            items.take(3).forEach { (label, desc) ->
                WyeSelectableChip(
                    label = label,
                    description = desc,
                    selected = selected == label,
                    onClick = { selected = label },
                    modifier = Modifier.weight(1f),
                )
            }
        }
        // 2열 둘째 줄
        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            items.drop(3).forEach { (label, desc) ->
                WyeSelectableChip(
                    label = label,
                    description = desc,
                    selected = selected == label,
                    onClick = { selected = label },
                    modifier = Modifier.weight(1f),
                )
            }
            // 빈 자리 채우기
            Spacer(Modifier.weight(1f))
        }
    }
}


// ── 미리 정의된 카테고리 뱃지 ─────────────────────────────────────

object InvestmentTypeColor {
    val CONSERVATIVE        = BadgeConservative        // 안정형
    val CONSERVATIVE_GROWTH = BadgeConservativeGrowth  // 안정추구형
    val NEUTRAL             = BadgeNeutral             // 위험중립형
    val ACTIVE              = BadgeActive              // 적극투자형
    val AGGRESSIVE          = BadgeAggressive          // 공격투자형

    val CONSERVATIVE_FONT        = BadgeConservativeFont        // 안정형 글꼴
    val CONSERVATIVE_GROWTH_FONT = BadgeConservativeGrowthFont  // 안정추구형 글꼴
    val NEUTRAL_FONT             = BadgeNeutralFont             // 위험중립형 글꼴
    val ACTIVE_FONT              = BadgeActiveFont              // 적극투자형 글꼴
    val AGGRESSIVE_FONT          = BadgeAggressiveFont          // 공격투자형 글꼴
}
