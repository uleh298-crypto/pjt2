package com.d102.wye.presentation.strategy.detail

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import com.d102.wye.presentation.theme.SurfaceVariant
import com.d102.wye.presentation.theme.TextPrimary
import com.d102.wye.presentation.theme.TextSecondary
import java.time.Duration
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter

@Composable
fun NewsSection(news: List<NewsItem>, onNewsClick: (Long) -> Unit = {}) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .background(Color.White)
            .padding(24.dp)
    ) {
        Text(
            text = "관련 뉴스",
            style = MaterialTheme.typography.labelLarge.copy(fontSize = 15.sp),
            color = TextPrimary
        )

        news.forEach { item ->
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable { onNewsClick(item.id) }
                    .padding(top = 20.dp),
                verticalAlignment = Alignment.Top
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .weight(0.8f)
                ) {
                    Text(
                        text = item.title,
                        style = MaterialTheme.typography.labelLarge.copy(fontSize = 16.sp),
                        color = TextPrimary,
                        maxLines = 2
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = "${item.publishedAt.toTimeAgo()} · ${item.source}",
                        style = MaterialTheme.typography.bodySmall,
                        color = TextSecondary,
                        maxLines = 1
                    )
                }
                Spacer(modifier = Modifier.width(16.dp))
                // 뉴스 썸네일
                AsyncImage(
                    model = item.thumbnailUrl,
                    contentDescription = item.title,
                    contentScale = ContentScale.Crop,
                    modifier = Modifier
                        .size(64.dp)
                        .clip(RoundedCornerShape(12.dp))
                        .background(SurfaceVariant),
                )
            }
        }
    }
}

private fun String.toTimeAgo(): String {
    return runCatching {
        val publishedAt = LocalDateTime.parse(this, DateTimeFormatter.ISO_DATE_TIME)
        val duration = Duration.between(publishedAt, LocalDateTime.now())
        when {
            duration.toMinutes() < 1 -> "방금 전"
            duration.toHours() < 1   -> "${duration.toMinutes()}분 전"
            duration.toDays() < 1    -> "${duration.toHours()}시간 전"
            duration.toDays() < 7    -> "${duration.toDays()}일 전"
            else -> publishedAt.toLocalDate().toString()
        }
    }.getOrDefault(this)
}