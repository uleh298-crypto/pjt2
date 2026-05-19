package com.d102.wye.presentation.mypage.support

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material.icons.filled.KeyboardArrowUp
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.d102.wye.presentation.designsystem.WyeSearchBar
import com.d102.wye.presentation.designsystem.WyeTopBar
import com.d102.wye.presentation.theme.BackGroundLightGreen2
import com.d102.wye.presentation.theme.Border
import com.d102.wye.presentation.theme.PrimaryGreen
import com.d102.wye.presentation.theme.SurfaceWhite
import com.d102.wye.presentation.theme.TextOnColored
import com.d102.wye.presentation.theme.TextPrimary
import com.d102.wye.presentation.theme.TextSecondary

@Composable
fun FaqScreen(
    onBackClick: () -> Unit
) {
    var query by rememberSaveable { mutableStateOf("") }
    var selectedCategoryKey by rememberSaveable { mutableStateOf(faqCategories.first().key) }
    var expandedItemId by rememberSaveable { mutableStateOf(faqItems.firstOrNull()?.id) }

    val filteredItems = remember(query, selectedCategoryKey) {
        faqItems.filter { item ->
            val matchesCategory = selectedCategoryKey == "all" || item.categoryKey == selectedCategoryKey
            val matchesQuery = query.isBlank() ||
                item.question.contains(query, ignoreCase = true) ||
                item.answer.contains(query, ignoreCase = true)
            matchesCategory && matchesQuery
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
    ) {
        WyeTopBar(
            title = "자주 묻는 질문",
            onBackClick = onBackClick
        )

        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            contentPadding = PaddingValues(horizontal = 16.dp, vertical = 14.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            item {
                WyeSearchBar(
                    query = query,
                    onQueryChange = { query = it },
                    placeholder = "궁금한 내용을 검색해 보세요",
                    modifier = Modifier.fillMaxWidth()
                )
            }

            item {
                Row(
                    modifier = Modifier.horizontalScroll(rememberScrollState()),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    faqCategories.forEach { category ->
                        FaqCategoryChip(
                            label = category.label,
                            selected = selectedCategoryKey == category.key,
                            onClick = { selectedCategoryKey = category.key }
                        )
                    }
                }
            }

            if (filteredItems.isEmpty()) {
                item {
                    EmptyFaqResult()
                }
            } else {
                items(filteredItems, key = { it.id }) { item ->
                    FaqCard(
                        item = item,
                        expanded = expandedItemId == item.id,
                        onClick = {
                            expandedItemId = if (expandedItemId == item.id) null else item.id
                        }
                    )
                }
            }
        }
    }
}

@Composable
private fun FaqCategoryChip(
    label: String,
    selected: Boolean,
    onClick: () -> Unit
) {
    Text(
        text = label,
        color = if (selected) TextOnColored else TextSecondary,
        style = MaterialTheme.typography.bodySmall.copy(
            fontWeight = if (selected) FontWeight.SemiBold else FontWeight.Medium
        ),
        modifier = Modifier
            .clip(RoundedCornerShape(20.dp))
            .border(
                width = 1.dp,
                color = if (selected) PrimaryGreen else Border,
                shape = RoundedCornerShape(20.dp)
            )
            .background(if (selected) PrimaryGreen else SurfaceWhite)
            .clickable(onClick = onClick)
            .padding(horizontal = 18.dp, vertical = 10.dp)
    )
}

@Composable
private fun FaqCard(
    item: FaqItem,
    expanded: Boolean,
    onClick: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(18.dp))
            .background(SurfaceWhite)
            .clickable(onClick = onClick)
            .padding(horizontal = 18.dp, vertical = 18.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.Top
        ) {
            Text(
                text = item.question,
                color = TextPrimary,
                style = MaterialTheme.typography.bodyLarge.copy(fontWeight = FontWeight.SemiBold),
                modifier = Modifier.weight(1f)
            )

            Icon(
                imageVector = if (expanded) Icons.Default.KeyboardArrowUp else Icons.Default.KeyboardArrowDown,
                contentDescription = null,
                tint = TextSecondary,
                modifier = Modifier
                    .padding(start = 12.dp, top = 2.dp)
                    .size(20.dp)
            )
        }

        if (expanded) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(14.dp))
                    .background(BackGroundLightGreen2)
                    .padding(16.dp)
            ) {
                Text(
                    text = item.answer,
                    color = TextSecondary,
                    style = MaterialTheme.typography.bodyMedium,
                    lineHeight = MaterialTheme.typography.bodyMedium.lineHeight
                )
            }
        }
    }
}

@Composable
private fun EmptyFaqResult() {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(18.dp))
            .background(SurfaceWhite)
            .padding(horizontal = 20.dp, vertical = 28.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        Text(
            text = "검색 결과가 없습니다.",
            color = TextPrimary,
            style = MaterialTheme.typography.bodyLarge.copy(fontWeight = FontWeight.SemiBold)
        )
        Text(
            text = "다른 키워드로 검색하거나 카테고리를 변경해 보세요.",
            color = TextSecondary,
            style = MaterialTheme.typography.bodySmall
        )
    }
}
