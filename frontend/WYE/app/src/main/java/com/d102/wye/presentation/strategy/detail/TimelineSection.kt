package com.d102.wye.presentation.strategy.detail

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.verticalScroll
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
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.d102.wye.presentation.theme.Border
import com.d102.wye.presentation.theme.Divider
import com.d102.wye.presentation.theme.IconInactive
import com.d102.wye.presentation.theme.PrimaryGreen
import com.d102.wye.presentation.theme.TextPrimary
import com.d102.wye.presentation.theme.TextSecondary

private const val PREVIEW_COUNT = 2

@Composable
fun TimelineSection(timelines: List<TimelineItem>) {
    var expanded by remember { mutableStateOf(false) }
    val hasMore = timelines.size > PREVIEW_COUNT

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .background(Color.White)
            .padding(24.dp)
    ) {
        Text(
            text = "주요 이슈 타임라인",
            style = MaterialTheme.typography.labelLarge.copy(fontSize = 16.sp),
            color = TextPrimary
        )
        Spacer(modifier = Modifier.height(20.dp))

        if (expanded) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .heightIn(max = 300.dp)
                    .verticalScroll(rememberScrollState())
            ) {
                timelines.forEachIndexed { index, item ->
                    TimelineItemRow(index = index, item = item)
                }
            }
        } else {
            timelines.take(PREVIEW_COUNT).forEachIndexed { index, item ->
                TimelineItemRow(index = index, item = item)
            }
        }

        if (hasMore) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable { expanded = !expanded }
                    .padding(top = 4.dp),
                horizontalArrangement = Arrangement.Center,
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Text(
                    text = if (expanded) "접기" else "타임라인 더보기",
                    style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Medium),
                    color = TextSecondary,
                )
                Icon(
                    imageVector = if (expanded) Icons.Filled.KeyboardArrowUp else Icons.Filled.KeyboardArrowDown,
                    contentDescription = null,
                    tint = TextSecondary,
                    modifier = Modifier.size(22.dp),
                )
            }
        }
    }
}

@Composable
private fun TimelineItemRow(index: Int, item: TimelineItem) {
    Row(modifier = Modifier.fillMaxWidth()) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier = Modifier.width(20.dp)
        ) {
            Box(
                modifier = Modifier
                    .size(10.dp)
                    .background(
                        if (index == 0) PrimaryGreen else Border,
                        CircleShape
                    )
            )
            Box(
                modifier = Modifier
                    .width(1.dp)
                    .height(72.dp)
                    .background(Divider)
            )
        }

        Column(
            modifier = Modifier.padding(start = 12.dp, bottom = 24.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Text(
                text = item.date,
                style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.SemiBold),
                color = if (index == 0) PrimaryGreen else IconInactive
            )
            Text(
                text = item.title,
                style = MaterialTheme.typography.labelLarge.copy(fontSize = 16.sp),
                color = TextPrimary
            )
            Text(
                text = item.content,
                style = MaterialTheme.typography.labelLarge,
                color = TextSecondary,
            )
        }
    }
}
