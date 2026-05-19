package com.d102.wye.presentation.explore.list.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.d102.wye.presentation.designsystem.WyeSearchBar
import com.d102.wye.presentation.theme.PrimaryGreen
import com.d102.wye.presentation.theme.SurfaceVariant
import com.d102.wye.presentation.theme.TextPrimary
import com.d102.wye.presentation.theme.TextSecondary


// ── 검색 + 검색범위 드롭다운 ────────────────────────────────────

private data class SearchScopeOption(val label: String, val value: String?)

@Composable
fun SearchRow(
    query: String,
    searchScope: String?,
    onQueryChanged: (String) -> Unit,
    onSearchScopeSelected: (String?) -> Unit,
    modifier: Modifier = Modifier,
) {
    val scopeOptions = listOf(
        SearchScopeOption("전체", null),
        SearchScopeOption("ETF 종목명", "etf"),
        SearchScopeOption("주식명", "stock"),
    )
    val selectedLabel = scopeOptions.firstOrNull { it.value == searchScope }?.label ?: "전체"
    val placeholder = when (searchScope) {
        "etf" -> "ETF 종목명 검색"
        "stock" -> "주식명 검색"
        else -> "ETF 종목명 또는 주식명 검색"
    }
    var expanded by remember { mutableStateOf(false) }

    Row(
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(8.dp),
        modifier = modifier,
    ) {
        val scopeActive = searchScope != null
        Box {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(4.dp),
                modifier = Modifier
                    .clip(RoundedCornerShape(20.dp))
                    .background(if (scopeActive) PrimaryGreen.copy(alpha = 0.08f) else SurfaceVariant)
                    .then(
                        if (scopeActive) Modifier.border(
                            1.dp,
                            PrimaryGreen.copy(alpha = 0.5f),
                            RoundedCornerShape(20.dp)
                        ) else Modifier
                    )
                    .clickable { expanded = true }
                    .padding(horizontal = 14.dp, vertical = 9.dp),
            ) {
                Text(
                    text = selectedLabel,
                    style = MaterialTheme.typography.bodySmall.copy(
                        fontWeight = if (scopeActive) FontWeight.SemiBold else FontWeight.Normal,
                    ),
                    color = if (scopeActive) PrimaryGreen else TextPrimary,
                )
                Icon(
                    imageVector = Icons.Default.KeyboardArrowDown,
                    contentDescription = null,
                    modifier = Modifier.size(14.dp),
                    tint = if (scopeActive) PrimaryGreen else TextSecondary,
                )
            }
            DropdownMenu(
                expanded = expanded,
                onDismissRequest = { expanded = false },
                containerColor = Color.White,
                shape = RoundedCornerShape(16.dp),
                shadowElevation = 8.dp,
            ) {
                scopeOptions.forEach { option ->
                    val isSelected = option.value == searchScope
                    DropdownMenuItem(
                        text = {
                            Text(
                                text = option.label,
                                style = MaterialTheme.typography.bodyMedium.copy(
                                    fontWeight = if (isSelected) FontWeight.SemiBold else FontWeight.Normal,
                                ),
                                color = if (isSelected) PrimaryGreen else TextPrimary,
                            )
                        },
                        trailingIcon = {
                            if (isSelected) {
                                Icon(
                                    imageVector = Icons.Default.Check,
                                    contentDescription = null,
                                    tint = PrimaryGreen,
                                    modifier = Modifier.size(16.dp),
                                )
                            }
                        },
                        onClick = {
                            onSearchScopeSelected(option.value)
                            expanded = false
                        },
                    )
                }
            }
        }

        WyeSearchBar(
            query = query,
            onQueryChange = onQueryChanged,
            placeholder = placeholder,
            modifier = Modifier.weight(1f),
        )
    }
}

