package com.d102.wye.presentation.explore

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.expandVertically
import androidx.compose.animation.shrinkVertically
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material.icons.filled.KeyboardArrowUp
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import com.d102.wye.domain.state.EtfFilterState
import com.d102.wye.presentation.designsystem.WyePrimaryButton
import com.d102.wye.presentation.designsystem.WyeSelectableChip
import com.d102.wye.presentation.theme.BackGroundLightGreen
import com.d102.wye.presentation.theme.Background
import com.d102.wye.presentation.theme.TextPrimary
import com.d102.wye.presentation.theme.TextSecondary
import com.d102.wye.presentation.theme.WYETheme

private fun <T> Set<T>.toggle(item: T): Set<T> = if (item in this) this - item else this + item

@Composable
fun FilterDialog(
    filter: EtfFilterState,
    resultCount: Int,
    onFilterChanged: (EtfFilterState) -> Unit,
    onApply: () -> Unit,
    onDismiss: () -> Unit,
    expandedSections: Set<String> = emptySet(),
    onToggleSection: (String) -> Unit = {},
) {
    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(usePlatformDefaultWidth = false),
    ) {
        Surface(
            modifier = Modifier
                .fillMaxWidth()
                .fillMaxHeight(0.9f)
                .padding(horizontal = 16.dp),
            shape = MaterialTheme.shapes.large,
            color = Background,
        ) {
            Column {
                // 헤더
                FilterDialogHeader(
                    onReset = { onFilterChanged(EtfFilterState()) },
                    onDismiss = onDismiss
                )

                // 필터 섹션들 (스크롤)
                Column(
                    modifier = Modifier
                        .weight(1f)
                        .verticalScroll(rememberScrollState())
                        .padding(horizontal = 16.dp),
                    verticalArrangement = Arrangement.spacedBy(20.dp),
                ) {
                    Spacer(Modifier.height(4.dp))

                    // 위험 분류
                    FilterSection(title = "위험 분류", expanded = "위험 분류" in expandedSections, onToggle = { onToggleSection("위험 분류") }) {
                        val riskItems = listOf(
                            Triple("CONSERVATIVE", "안정형", "예금 보호 확정\n금리 추구형"),
                            Triple("STABLE", "안정추구형", "투자원금 손실\n최소화"),
                            Triple("MODERATE", "위험중립형", "투자에 따른\n수익·손실 인지"),
                            Triple("ACTIVE", "적극투자형", "높은 수익 위해\n위험 감수"),
                            Triple("AGGRESSIVE", "공격투자형", "원금 손실 감수\n고위험 투자"),
                        )
                        // 1행: 안정형 안정추구형 위험중립형
                        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            riskItems.take(3).forEach { (type, label, desc) ->
                                WyeSelectableChip(
                                    label = label,
                                    description = desc,
                                    selected = filter.riskType == type,
                                    onClick = {
                                        onFilterChanged(
                                            filter.copy(riskType = if (filter.riskType == type) null else type)
                                        )
                                    },
                                    modifier = Modifier.weight(1f),
                                    unselectedColor = BackGroundLightGreen,
                                    showBorder = false,
                                )
                            }
                        }
                        // 2행: 적극투자형 공격투자형
                        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            riskItems.drop(3).forEach { (type, label, desc) ->
                                WyeSelectableChip(
                                    label = label,
                                    description = desc,
                                    selected = filter.riskType == type,
                                    onClick = {
                                        onFilterChanged(
                                            filter.copy(riskType = if (filter.riskType == type) null else type)
                                        )
                                    },
                                    modifier = Modifier.weight(1f),
                                    unselectedColor = BackGroundLightGreen,
                                    showBorder = false,
                                )
                            }
                            Spacer(Modifier.weight(1f))
                        }
                    }

                    // 투자 전략
                    FilterSection(title = "투자 전략", expanded = "투자 전략" in expandedSections, onToggle = { onToggleSection("투자 전략") }) {
                        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            listOf("시장 대표", "테마형", "배당형", "채권형").forEach { s ->
                                WyeSelectableChip(
                                    label = s,
                                    selected = filter.strategy == s,
                                    onClick = { onFilterChanged(filter.copy(strategy = if (filter.strategy == s) null else s)) },
                                    unselectedColor = BackGroundLightGreen,
                                    showBorder = false,
                                )
                            }
                        }
                    }

                    // 투자 테마 (테마형 선택 시 활성화)
                    ThemeFilterSection(
                        filter = filter,
                        onFilterChanged = onFilterChanged,
                    )

                    // 배당률
                    FilterSection(title = "배당률", expanded = "배당률" in expandedSections, onToggle = { onToggleSection("배당률") }) {
                        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            listOf(
                                "3% 이상" to "3",
                                "5% 이상" to "5",
                                "7% 이상" to "7",
                                "10% 이상" to "10",
                            ).forEach { (label, value) ->
                                WyeSelectableChip(
                                    label = label,
                                    selected = filter.dividendRateRange == value,
                                    onClick = { onFilterChanged(filter.copy(dividendRateRange = if (filter.dividendRateRange == value) null else value)) },
                                    unselectedColor = BackGroundLightGreen,
                                    showBorder = false,
                                )
                            }
                        }
                    }

                    // 배당주기
                    FilterSection(title = "배당주기", expanded = "배당주기" in expandedSections, onToggle = { onToggleSection("배당주기") }) {
                        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            listOf("월", "반기", "분기", "년").forEach { cycle ->
                                WyeSelectableChip(
                                    label = cycle,
                                    selected = filter.dividendCycle == cycle,
                                    onClick = { onFilterChanged(filter.copy(dividendCycle = if (filter.dividendCycle == cycle) null else cycle)) },
                                    unselectedColor = BackGroundLightGreen,
                                    showBorder = false,
                                )
                            }
                        }
                    }

                    // 파생상품
                    FilterSection(title = "파생상품", expanded = "파생상품" in expandedSections, onToggle = { onToggleSection("파생상품") }) {
                        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            WyeSelectableChip(
                                label = "O",
                                selected = filter.hasDerivative == true,
                                onClick = { onFilterChanged(filter.copy(hasDerivative = if (filter.hasDerivative == true) null else true)) },
                                unselectedColor = BackGroundLightGreen,
                                    showBorder = false,
                            )
                            WyeSelectableChip(
                                label = "X",
                                selected = filter.hasDerivative == false,
                                onClick = { onFilterChanged(filter.copy(hasDerivative = if (filter.hasDerivative == false) null else false)) },
                                unselectedColor = BackGroundLightGreen,
                                    showBorder = false,
                            )
                        }
                    }

                    // 레버리지 (파생상품 O 선택 시 활성화)
                    LockedFilterSection(
                        title = "레버리지",
                        locked = filter.hasDerivative != true,
                        selected = filter.hasLeverage,
                        onChanged = { onFilterChanged(filter.copy(hasLeverage = it)) },
                    )

                    // 인버스 (파생상품 O 선택 시 활성화)
                    LockedFilterSection(
                        title = "인버스",
                        locked = filter.hasDerivative != true,
                        selected = filter.hasInverse,
                        onChanged = { onFilterChanged(filter.copy(hasInverse = it)) },
                    )

                    // P/E
                    FilterSection(title = "P/E", expanded = "P/E" in expandedSections, onToggle = { onToggleSection("P/E") }) {
                        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            listOf(
                                "10배 이하" to "under10",
                                "10 - 20배" to "10-20",
                                "20배 이상" to "over20"
                            ).forEach { (label, value) ->
                                WyeSelectableChip(
                                    label = label,
                                    selected = filter.peRange == value,
                                    onClick = { onFilterChanged(filter.copy(peRange = if (filter.peRange == value) null else value)) },
                                    unselectedColor = BackGroundLightGreen,
                                    showBorder = false,
                                )
                            }
                        }
                    }

                    // P/B
                    FilterSection(title = "P/B", expanded = "P/B" in expandedSections, onToggle = { onToggleSection("P/B") }) {
                        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            listOf(
                                "1배 미만" to "under1",
                                "1 - 3배" to "1-3",
                                "3배 이상" to "over3"
                            ).forEach { (label, value) ->
                                WyeSelectableChip(
                                    label = label,
                                    selected = filter.pbRange == value,
                                    onClick = { onFilterChanged(filter.copy(pbRange = if (filter.pbRange == value) null else value)) },
                                    unselectedColor = BackGroundLightGreen,
                                    showBorder = false,
                                )
                            }
                        }
                    }

                    // ROE
                    FilterSection(title = "ROE", expanded = "ROE" in expandedSections, onToggle = { onToggleSection("ROE") }) {
                                        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                            listOf(
                                                "5% 미만" to "under5",
                                                "5 - 15%" to "5-15",
                                                "15% 이상" to "over15"
                                            ).forEach { (label, value) ->
                                                WyeSelectableChip(
                                                    label = label,
                                                    selected = filter.roeRange == value,
                                                    onClick = { onFilterChanged(filter.copy(roeRange = if (filter.roeRange == value) null else value)) },
                                    unselectedColor = BackGroundLightGreen,
                                    showBorder = false,
                                )
                            }
                        }
                    }

                    // 운용보수
                    FilterSection(title = "운용보수(수수료)", expanded = "운용보수(수수료)" in expandedSections, onToggle = { onToggleSection("운용보수(수수료)") }) {
                        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            listOf(
                                "0.05% 미만" to "under0.05",
                                "0.05 - 0.5%" to "0.05-0.5",
                                "0.5% 이상" to "over0.5"
                            ).forEach { (label, value) ->
                                WyeSelectableChip(
                                    label = label,
                                    selected = filter.expenseRatioRange == value,
                                    onClick = { onFilterChanged(filter.copy(expenseRatioRange = if (filter.expenseRatioRange == value) null else value)) },
                                    unselectedColor = BackGroundLightGreen,
                                    showBorder = false,
                                )
                            }
                        }
                    }

                    // 순자산액
                    FilterSection(title = "순자산액", expanded = "순자산액" in expandedSections, onToggle = { onToggleSection("순자산액") }) {
                        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            listOf(
                                "100억 미만" to "under100",
                                "100 - 1000억" to "100-1000",
                                "1000억 이상" to "over1000"
                            ).forEach { (label, value) ->
                                WyeSelectableChip(
                                    label = label,
                                    selected = filter.netAssetRange == value,
                                    onClick = { onFilterChanged(filter.copy(netAssetRange = if (filter.netAssetRange == value) null else value)) },
                                    unselectedColor = BackGroundLightGreen,
                                    showBorder = false,
                                )
                            }
                        }
                    }

                    Spacer(Modifier.height(8.dp))
                }

                // 하단 버튼
                WyePrimaryButton(
                    text = "${resultCount}개의 결과 보기",
                    onClick = onApply,
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                )
            }
        }
    }
}

@Composable
private fun FilterDialogHeader(onReset: () -> Unit, onDismiss: () -> Unit) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 12.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        TextButton(onClick = onReset) {
            Text("초기화", color = TextSecondary, style = MaterialTheme.typography.bodyMedium)
        }
        Text(
            text = "상세 필터",
            modifier = Modifier.weight(1f),
            textAlign = TextAlign.Center,
            style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.SemiBold),
        )
        IconButton(onClick = onDismiss) {
            Icon(Icons.Default.Close, contentDescription = "닫기")
        }
    }
}

@Composable
private fun ThemeFilterSection(
    filter: EtfFilterState,
    onFilterChanged: (EtfFilterState) -> Unit,
) {
    val enabled = filter.strategy == "테마형"
    var expanded by remember { mutableStateOf(false) }
    val themes = listOf(
        "반도체", "IT/전자", "바이오/의약",
        "자동차", "화학/소재", "에너지",
        "금융", "건설/부동산", "소비재",
        "통신/미디어", "운송/물류", "산업재", "지주회사", "기타",
    )

    LaunchedEffect(enabled) {
        if (enabled) expanded = true else expanded = false
    }

    AnimatedVisibility(
        visible = enabled,
        enter = expandVertically(),
        exit = shrinkVertically(),
    ) {
        Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
            Text(
                "투자 테마",
                style = MaterialTheme.typography.titleSmall,
                color = TextPrimary,
                modifier = Modifier.fillMaxWidth(),
            )
            AnimatedVisibility(
                visible = expanded,
                enter = expandVertically(),
                exit = shrinkVertically(),
            ) {
                FlowRow(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp),
                ) {
                    themes.forEach { theme ->
                        WyeSelectableChip(
                            label = theme,
                            selected = theme in filter.themes,
                            onClick = {
                                onFilterChanged(
                                    filter.copy(
                                        themes = filter.themes.toggle(
                                            theme
                                        )
                                    )
                                )
                            },
                            unselectedColor = BackGroundLightGreen,
                                    showBorder = false,
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun FilterSection(
    title: String,
    expanded: Boolean,
    onToggle: () -> Unit,
    content: @Composable ColumnScope.() -> Unit,
) {
    Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .clickable { onToggle() },
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Text(title, style = MaterialTheme.typography.titleSmall, color = TextPrimary)
            Icon(
                imageVector = if (expanded) Icons.Default.KeyboardArrowUp else Icons.Default.KeyboardArrowDown,
                contentDescription = if (expanded) "접기" else "펼치기",
                tint = TextSecondary,
            )
        }
        if (expanded) {
            content()
        }
    }
}

@Composable
private fun LockedFilterSection(
    title: String,
    locked: Boolean,
    selected: Boolean?,
    onChanged: (Boolean?) -> Unit,
) {
    var expanded by remember { mutableStateOf(false) }

    LaunchedEffect(locked) {
        if (!locked) expanded = true else expanded = false
    }

    AnimatedVisibility(
        visible = !locked,
        enter = expandVertically(),
        exit = shrinkVertically(),
    ) {
        Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
            Text(
                title,
                style = MaterialTheme.typography.titleSmall,
                color = TextPrimary,
                modifier = Modifier.fillMaxWidth(),
            )
            AnimatedVisibility(
                visible = expanded,
                enter = expandVertically(),
                exit = shrinkVertically(),
            ) {
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    WyeSelectableChip(
                        label = "O",
                        selected = selected == true,
                        onClick = { onChanged(if (selected == true) null else true) },
                        unselectedColor = BackGroundLightGreen,
                                    showBorder = false,
                    )
                    WyeSelectableChip(
                        label = "X",
                        selected = selected == false,
                        onClick = { onChanged(if (selected == false) null else false) },
                        unselectedColor = BackGroundLightGreen,
                                    showBorder = false,
                    )
                }
            }
        }
    }
}


