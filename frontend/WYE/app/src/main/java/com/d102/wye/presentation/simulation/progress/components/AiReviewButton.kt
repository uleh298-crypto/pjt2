package com.d102.wye.presentation.simulation.progress.components

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.expandHorizontally
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkHorizontally
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.d102.wye.R
import com.d102.wye.presentation.theme.Divider
import com.d102.wye.presentation.theme.IconInactive
import com.d102.wye.presentation.theme.PrimaryGreen
import kotlinx.coroutines.delay

@Composable
fun AiReviewButton(
    modifier: Modifier = Modifier,
    isEmpty: Boolean,
    onClick: () -> Unit
) {
    var isExpanded by remember { mutableStateOf(true) }

    // 2초 후 자동 축소
    LaunchedEffect(Unit) {
        delay(2000)
        isExpanded = false
    }

    Surface(
        onClick = { if (!isEmpty) onClick() },
        modifier = modifier,
        shape = RoundedCornerShape(16.dp),
        color = Color.White,
        border = BorderStroke(1.dp, if (isEmpty) Divider else PrimaryGreen.copy(alpha = 0.3f)),
        shadowElevation = 2.dp
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 10.dp, vertical = 8.dp),
            horizontalArrangement = Arrangement.spacedBy(4.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                painter = painterResource(R.drawable.ic_shining),
                contentDescription = null,
                tint = if (isEmpty) IconInactive else PrimaryGreen
            )
            AnimatedVisibility(
                visible = isExpanded,
                enter = fadeIn() + expandHorizontally(),
                exit = fadeOut() + shrinkHorizontally()
            ) {
                Text(
                    text = "AI 진단",
                    style = MaterialTheme.typography.bodySmall.copy(fontWeight = FontWeight.SemiBold),
                    color = if (isEmpty) IconInactive else PrimaryGreen
                )
            }
        }
    }
}