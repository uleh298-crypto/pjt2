package com.d102.wye.presentation.explore.list.components

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.ui.draw.clip
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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.d102.wye.presentation.theme.PrimaryGreen
import com.d102.wye.presentation.theme.TextPrimary
import com.d102.wye.presentation.theme.TextSecondary

private val sortOptions = listOf("거래량 순", "등락률 순", "시가총액 순")
private val sortValues  = listOf("volume", "dailyReturn", "aum")

fun sortLabelOf(value: String?) = sortOptions[sortValues.indexOf(value).coerceAtLeast(0)]

// ── 정렬 ───────────────────────────────────────────────────────
@Composable
fun SortRow(
    selectedSort: String?,
    onSortChanged: (String) -> Unit,
    modifier: Modifier = Modifier,
    marketStatusLabel: String = "",
) {
    var expanded by remember { mutableStateOf(false) }
    val selected = sortLabelOf(selectedSort)

    Row(
        modifier = modifier,
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically,
    ) {
        if (marketStatusLabel.isNotBlank()) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(4.dp),
            ) {
                if (marketStatusLabel.contains("기준")) {
                    Box(
                        modifier = Modifier
                            .size(6.dp)
                            .clip(CircleShape)
                            .background(PrimaryGreen)
                    )
                }
                Text(
                    text = marketStatusLabel,
                    style = MaterialTheme.typography.labelSmall,
                    color = if (marketStatusLabel.contains("기준")) PrimaryGreen else TextSecondary,
                )
            }
        } else {
            Box(modifier = Modifier)
        }
        Box {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(3.dp),
                modifier = Modifier
                    .clickable { expanded = true }
                    .padding(horizontal = 4.dp, vertical = 7.dp),
            ) {
                Text(
                    text = selected,
                    style = MaterialTheme.typography.bodySmall.copy(fontWeight = FontWeight.Medium),
                    color = TextPrimary,
                )
                Icon(
                    imageVector = Icons.Default.KeyboardArrowDown,
                    contentDescription = null,
                    modifier = Modifier.size(14.dp),
                    tint = TextSecondary,
                )
            }
            DropdownMenu(
                expanded = expanded,
                onDismissRequest = { expanded = false },
                containerColor = Color.White,
                shape = RoundedCornerShape(16.dp),
                shadowElevation = 8.dp,
            ) {
                sortOptions.forEach { option ->
                    val isSelected = selected == option
                    DropdownMenuItem(
                        text = {
                            Text(
                                text = option,
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
                        onClick = { onSortChanged(sortValues[sortOptions.indexOf(option)]); expanded = false },
                    )
                }
            }
        }
    }
}
