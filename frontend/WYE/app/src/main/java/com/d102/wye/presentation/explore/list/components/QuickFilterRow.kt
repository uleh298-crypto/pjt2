package com.d102.wye.presentation.explore.list.components

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.filled.Tune
import androidx.compose.material.icons.outlined.StarOutline
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.unit.dp
import com.d102.wye.domain.state.EtfFilterState
import com.d102.wye.presentation.theme.PrimaryGreen
import com.d102.wye.presentation.theme.SurfaceVariant
import com.d102.wye.presentation.theme.TextOnColored
import com.d102.wye.presentation.theme.TextSecondary

@Composable
fun QuickFilterRow(
    filterState: EtfFilterState,
    onFilterIconClick: () -> Unit,
    onFilterChanged: (EtfFilterState) -> Unit,
    modifier: Modifier = Modifier,
) {
    val filterCount = activeFilterCount(filterState)
    val activeChips = buildActiveChips(filterState)
    val hasNoFilters = activeChips.isEmpty()

    Row(
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(8.dp),
        modifier = modifier.horizontalScroll(rememberScrollState()),
    ) {
        Box(
            contentAlignment = Alignment.Center,
            modifier = Modifier
                .size(36.dp)
                .clip(RoundedCornerShape(10.dp))
                .background(SurfaceVariant)
                .clickable(onClick = onFilterIconClick),
        ) {
            Icon(
                imageVector = Icons.Default.Tune,
                contentDescription = "상세 필터",
                tint = TextSecondary,
                modifier = Modifier.size(20.dp),
            )
        }

        // 즐겨찾기 토글 칩
        Icon(
            imageVector = if (filterState.onlyLiked) Icons.Filled.Star else Icons.Outlined.StarOutline,
            contentDescription = "즐겨찾기만 보기",
            tint = if (filterState.onlyLiked) TextOnColored else TextSecondary,
            modifier = Modifier
                .clip(RoundedCornerShape(20.dp))
                .background(if (filterState.onlyLiked) PrimaryGreen else SurfaceVariant)
                .clickable { onFilterChanged(filterState.copy(onlyLiked = !filterState.onlyLiked)) }
                .padding(horizontal = 14.dp, vertical = 7.dp)
                .size(16.dp),
        )

        if (hasNoFilters && !filterState.onlyLiked) {
            QuickChip("전체", true, onClick = {})
        } else if (!filterState.onlyLiked) {
            if (filterCount > 0) {
                Text(
                    text = "초기화",
                    style = MaterialTheme.typography.bodySmall,
                    color = TextSecondary,
                    modifier = Modifier
                        .clip(RoundedCornerShape(20.dp))
                        .clickable { onFilterChanged(EtfFilterState()) }
                        .padding(horizontal = 10.dp, vertical = 7.dp),
                )
            }
            activeChips.forEach { (label, nextState) ->
                ActiveFilterChip(label = label, onRemove = { onFilterChanged(nextState) })
            }
        }
    }
}

// ── 빠른 필터 칩 ───────────────────────────────────────────────
fun activeFilterCount(f: EtfFilterState): Int =
    f.themes.size +
            listOfNotNull(
                f.riskType, f.strategy, f.dividendRateRange, f.dividendCycle,
                f.peRange, f.pbRange, f.roeRange, f.expenseRatioRange, f.netAssetRange,
                f.hasDerivative?.let { "" },
                f.hasLeverage?.let { "" },
                f.hasInverse?.let { "" }).size

private val riskLabels = mapOf(
    "CONSERVATIVE" to "안정형",
    "STABLE"       to "안정추구형",
    "MODERATE"     to "위험중립형",
    "ACTIVE"       to "적극투자형",
    "AGGRESSIVE"   to "공격투자형",
)

fun buildActiveChips(f: EtfFilterState): List<Pair<String, EtfFilterState>> {
    val chips = mutableListOf<Pair<String, EtfFilterState>>()

    f.riskType?.let { riskLabels[it]?.let { label -> chips += label to f.copy(riskType = null) } }
    f.strategy?.let { chips += it to f.copy(strategy = null, themes = emptySet()) }
    f.themes.forEach { theme -> chips += theme to f.copy(themes = f.themes - theme) }

    val dividendLabels = mapOf("3" to "배당률 3%이상", "5" to "배당률 5%이상", "7" to "배당률 7%이상", "10" to "배당률 10%이상")
    f.dividendRateRange?.let {
        chips += (dividendLabels[it] ?: it) to f.copy(dividendRateRange = null)
    }

    val cycleLabels = mapOf("월" to "배당 월", "분기" to "배당 분기", "반기" to "배당 반기", "년" to "배당 연")
    f.dividendCycle?.let { chips += (cycleLabels[it] ?: it) to f.copy(dividendCycle = null) }

    f.hasDerivative?.let { v ->
        chips += "파생상품 ${if (v) "O" else "X"}" to f.copy(
            hasDerivative = null,
            hasLeverage = null,
            hasInverse = null,
        )
    }
    f.hasLeverage?.let { v -> chips += "레버리지 ${if (v) "O" else "X"}" to f.copy(hasLeverage = null) }
    f.hasInverse?.let { v -> chips += "인버스 ${if (v) "O" else "X"}" to f.copy(hasInverse = null) }

    val peLabels = mapOf("under10" to "PER 10 미만", "10-20" to "PER 10~20", "over20" to "PER 20 초과")
    f.peRange?.let { chips += (peLabels[it] ?: it) to f.copy(peRange = null) }

    val pbLabels = mapOf("under1" to "PBR 1 미만", "1-3" to "PBR 1~3", "over3" to "PBR 3 초과")
    f.pbRange?.let { chips += (pbLabels[it] ?: it) to f.copy(pbRange = null) }

    val roeLabels = mapOf("under5" to "ROE 5% 미만", "5-15" to "ROE 5~15%", "over15" to "ROE 15% 초과")
    f.roeRange?.let { chips += (roeLabels[it] ?: it) to f.copy(roeRange = null) }

    val expLabels =
        mapOf("under0.05" to "보수 0.05% 미만", "0.05-0.5" to "보수 0.05~0.5%", "over0.5" to "보수 0.5% 초과")
    f.expenseRatioRange?.let { chips += (expLabels[it] ?: it) to f.copy(expenseRatioRange = null) }

    val netLabels = mapOf(
        "under100" to "순자산 100억 미만",
        "100-1000" to "순자산 100~1000억",
        "over1000" to "순자산 1000억 초과"
    )
    f.netAssetRange?.let { chips += (netLabels[it] ?: it) to f.copy(netAssetRange = null) }

    return chips
}