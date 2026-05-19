package com.d102.wye.presentation.home.components

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.d102.wye.presentation.designsystem.SectionHeader
import com.d102.wye.presentation.home.HomeNewsUiModel
import com.d102.wye.presentation.home.Top10EtfUiModel
import com.d102.wye.presentation.theme.TextPrimary
import com.d102.wye.presentation.theme.TextSecondary

@Composable
fun HomeTop10Tab(
    top10Etfs: List<Top10EtfUiModel>,
    top10UpdatedText: String,
    isRefreshing: Boolean,
    newsList: List<HomeNewsUiModel>,
    onRefreshClick: () -> Unit,
    onEtfClick: (ticker: String) -> Unit,
    onNewsClick: (newsId: Long) -> Unit,
    onNewsMoreClick: () -> Unit
) {
    val primaryCardHeight = 400.dp

    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(horizontal = 16.dp, vertical = 16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.Top
            ) {
                Text(
                    text = "거래량 TOP 10 ETF",
                    color = TextPrimary,
                    style = MaterialTheme.typography.titleMedium
                )
                Row(
                    modifier = Modifier.offset(x = 0.dp, y = 8.dp),
                    horizontalArrangement = Arrangement.spacedBy(4.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "$top10UpdatedText 기준",
                        color = TextSecondary,
                        style = MaterialTheme.typography.bodySmall
                    )
                    if (isRefreshing) {
                        CircularProgressIndicator(
                            modifier = Modifier.size(14.dp),
                            strokeWidth = 2.dp
                        )
                    } else {
                        Icon(
                            imageVector = Icons.Default.Refresh,
                            contentDescription = "새로고침",
                            tint = TextSecondary,
                            modifier = Modifier
                                .size(16.dp)
                                .clickable(onClick = onRefreshClick)
                        )
                    }
                }
            }
        }

        item {
            HomeTop10HeatmapCard(
                modifier = Modifier.height(primaryCardHeight),
                top10Etfs = top10Etfs,
                onEtfClick = onEtfClick
            )
        }

        item {
            Spacer(modifier = Modifier.height(4.dp))
        }

        item {
            SectionHeader(
                title = "실시간 ETF 뉴스",
                onActionClick = onNewsMoreClick
            )
        }

        items(items = newsList, key = { it.id }) { news ->
            HomeNewsListCard(
                news = news,
                onClick = { onNewsClick(news.id) }
            )
        }
    }
}
