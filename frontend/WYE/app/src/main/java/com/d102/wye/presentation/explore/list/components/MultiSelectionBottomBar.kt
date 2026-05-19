package com.d102.wye.presentation.explore.list.components

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.tween
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.unit.dp
import com.d102.wye.presentation.designsystem.WyePrimaryButton
import com.d102.wye.presentation.explore.list.SelectedEtf
import com.d102.wye.presentation.theme.PrimaryGreen
import com.d102.wye.presentation.theme.SurfaceWhite

@Composable
fun MultiSelectionBottomBar(
    selectedItems: List<SelectedEtf>,
    onRemove: (String) -> Unit,
    onComplete: () -> Unit
) {
    AnimatedVisibility(
        visible = selectedItems.isNotEmpty(),
        enter = slideInVertically(animationSpec = tween(300)) { it },
        exit = slideOutVertically(animationSpec = tween(300)) { it }
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .shadow(
                    elevation = 16.dp,
                    shape = RoundedCornerShape(topStart = 20.dp, topEnd = 20.dp)
                )
                .clip(RoundedCornerShape(topStart = 20.dp, topEnd = 20.dp))
                .background(SurfaceWhite)
        ) {
            FlowRow(
                modifier = Modifier
                    .fillMaxWidth()
                    .heightIn(max = 120.dp)
                    .verticalScroll(rememberScrollState())
                    .padding(16.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                selectedItems.forEach { item ->
                    SelectedEtfChip(
                        name = item.name,
                        onRemove = { onRemove(item.ticker) }
                    )
                }
            }

            WyePrimaryButton(
                text = "${selectedItems.size}개 종목 추가하기",
                onClick = onComplete,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp)
                    .padding(bottom = 16.dp)
            )
        }
    }
}

@Composable
private fun SelectedEtfChip(
    name: String,
    onRemove: () -> Unit
) {
    Row(
        modifier = Modifier
            .clip(RoundedCornerShape(12.dp))
            .background(PrimaryGreen.copy(alpha = 0.1f))
            .padding(horizontal = 12.dp, vertical = 8.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = name,
            color = PrimaryGreen,
            style = MaterialTheme.typography.labelMedium
        )
        Spacer(modifier = Modifier.width(6.dp))
        Icon(
            imageVector = Icons.Default.Close,
            contentDescription = "삭제",
            tint = PrimaryGreen,
            modifier = Modifier
                .size(16.dp)
                .clickable { onRemove() }
        )
    }
}