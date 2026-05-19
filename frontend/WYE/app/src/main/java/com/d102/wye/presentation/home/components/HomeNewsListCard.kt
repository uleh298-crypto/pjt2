package com.d102.wye.presentation.home.components

import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.d102.wye.presentation.designsystem.NewsCard
import com.d102.wye.presentation.home.HomeNewsUiModel

@Composable
fun HomeNewsListCard(
    news: HomeNewsUiModel,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    NewsCard(
        category = news.category,
        title = news.title,
        timeAgo = news.timeAgo,
        source = news.source,
        thumbnailUrl = news.thumbnailUrl,
        onClick = onClick
    )
}
