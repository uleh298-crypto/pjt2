package com.d102.wye.presentation.designsystem

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.d102.wye.R
import com.d102.wye.presentation.theme.BadgeActive
import com.d102.wye.presentation.theme.BadgeActiveFont
import com.d102.wye.presentation.theme.BadgeAggressive
import com.d102.wye.presentation.theme.BadgeAggressiveFont
import com.d102.wye.presentation.theme.BadgeConservative
import com.d102.wye.presentation.theme.BadgeConservativeFont
import com.d102.wye.presentation.theme.BadgeConservativeGrowth
import com.d102.wye.presentation.theme.BadgeConservativeGrowthFont
import com.d102.wye.presentation.theme.BadgeNeutral
import com.d102.wye.presentation.theme.BadgeNeutralFont
import com.d102.wye.presentation.theme.Divider
import com.d102.wye.presentation.theme.NavInactive
import com.d102.wye.presentation.theme.PrimaryGreen
import com.d102.wye.presentation.theme.SurfaceVariant
import com.d102.wye.presentation.theme.TextSecondary

@Composable
fun EtfListItem(
    modifier: Modifier = Modifier,

    // 데이터 파라미터
    name: String,
    ticker: String,
    currentPrice: Long,
    changeRate: Double,
    changeAmount: Long,
    riskType: String,
    supportingText: String? = null,

    // 상태 파라미터
    isLiked: Boolean,
    isSelectionMode: Boolean = false,
    isSelected: Boolean = false,
    showLikeButton: Boolean = true,

    // 액션(이벤트) 파라미터
    onLikeToggled: () -> Unit = {},
    onToggleSelection: () -> Unit = {},
    onClick: () -> Unit,
) {
    Column {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = modifier
                .fillMaxWidth()
                .clickable {
                    if (isSelectionMode) onToggleSelection()
                    else onClick()
                }
                .padding(horizontal = 16.dp, vertical = 12.dp),
        ) {
            WyeCircleIcon(
                tag = name,
                count = 1,
                size = 44.dp,
                backgroundColor = SurfaceVariant,
                contentColor = TextSecondary,
                textStyle = MaterialTheme.typography.titleSmall.copy(fontSize = 12.sp),
            )
            Spacer(Modifier.width(12.dp))

            EtfInfoCard(
                name = name,
                currentPrice = currentPrice,
                riskType = riskType,
                modifier = Modifier.weight(1f),
                supportingText = supportingText
            )

            EtfPriceChange(changeRate, changeAmount)
            if (showLikeButton || isSelectionMode) {
                Spacer(Modifier.width(12.dp))
                EtfActionIcon(
                    isSelectionMode = isSelectionMode,
                    isSelected = isSelected,
                    isLiked = isLiked,
                    onToggleSelection = onToggleSelection,
                    onLikeToggled = onLikeToggled
                )
            }
        }
        HorizontalDivider(color = Divider, modifier = Modifier.padding(horizontal = 16.dp))
    }
}

@Composable
private fun EtfActionIcon(
    isSelectionMode: Boolean,
    isSelected: Boolean,
    isLiked: Boolean,
    onToggleSelection: () -> Unit,
    onLikeToggled: () -> Unit
) {
    if (isSelectionMode) {
        WyeRoundCheckbox(
            checked = isSelected,
        )
    } else {
        IconButton(onClick = onLikeToggled, modifier = Modifier.size(32.dp)) {
            Icon(
                painter = painterResource(
                    id = if (isLiked) R.drawable.ic_star else R.drawable.ic_staroutline
                ),
                contentDescription = "즐겨찾기",
                tint = if (isLiked) PrimaryGreen else NavInactive,
                modifier = Modifier.size(22.dp),
            )
        }
    }
}

// ─── 유틸리티 함수 ───────────────────────────────────────────────────
fun riskToBadge(level: Int): Triple<Color, Color, String> = when (level) {
    1 -> Triple(BadgeConservative, BadgeConservativeFont, "안정형")
    2 -> Triple(BadgeConservativeGrowth, BadgeConservativeGrowthFont, "안정추구형")
    3 -> Triple(BadgeNeutral, BadgeNeutralFont, "위험중립형")
    4 -> Triple(BadgeActive, BadgeActiveFont, "적극투자형")
    else -> Triple(BadgeAggressive, BadgeAggressiveFont, "공격투자형")
}

fun riskToBadge(type: String): Triple<Color, Color, String> = when (type) {
    "CONSERVATIVE", "안정형"    -> Triple(BadgeConservative, BadgeConservativeFont, "안정형")
    "STABLE", "안정추구형"       -> Triple(BadgeConservativeGrowth, BadgeConservativeGrowthFont, "안정추구형")
    "MODERATE", "위험중립형"     -> Triple(BadgeNeutral, BadgeNeutralFont, "위험중립형")
    "ACTIVE", "적극투자형"       -> Triple(BadgeActive, BadgeActiveFont, "적극투자형")
    "AGGRESSIVE", "공격투자형"   -> Triple(BadgeAggressive, BadgeAggressiveFont, "공격투자형")
    else                        -> Triple(BadgeAggressive, BadgeAggressiveFont, type)
}
