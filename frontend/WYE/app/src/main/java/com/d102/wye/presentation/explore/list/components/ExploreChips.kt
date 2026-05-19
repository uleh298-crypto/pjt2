package com.d102.wye.presentation.explore.list.components

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.d102.wye.presentation.theme.PrimaryGreen
import com.d102.wye.presentation.theme.SurfaceVariant
import com.d102.wye.presentation.theme.TextOnColored
import com.d102.wye.presentation.theme.TextPrimary


@Composable
fun QuickChip(label: String, selected: Boolean, onClick: () -> Unit) {
    Text(
        text = label,
        color = if (selected) TextOnColored else TextPrimary,
        style = MaterialTheme.typography.bodyMedium.copy(
            fontWeight = if (selected) FontWeight.SemiBold else FontWeight.Normal,
        ),
        modifier = Modifier
            .clip(RoundedCornerShape(20.dp))
            .background(if (selected) PrimaryGreen else SurfaceVariant)
            .clickable(onClick = onClick)
            .padding(horizontal = 14.dp, vertical = 7.dp),
    )
}

@Composable
fun ActiveFilterChip(label: String, onRemove: () -> Unit) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier = Modifier
            .clip(RoundedCornerShape(20.dp))
            .background(PrimaryGreen)
            .padding(start = 12.dp, end = 6.dp, top = 6.dp, bottom = 6.dp),
    ) {
        Text(
            text = label,
            color = TextOnColored,
            style = MaterialTheme.typography.bodySmall.copy(fontWeight = FontWeight.Medium),
        )
        Spacer(Modifier.width(2.dp))
        Icon(
            imageVector = Icons.Default.Close,
            contentDescription = "필터 제거",
            tint = TextOnColored,
            modifier = Modifier
                .size(16.dp)
                .clickable(onClick = onRemove),
        )
    }
}

