package com.d102.wye.presentation.designsystem

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material3.Icon
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.unit.dp
import com.d102.wye.presentation.theme.Border
import com.d102.wye.presentation.theme.PrimaryGreen
import com.d102.wye.presentation.theme.SurfaceWhite

@Composable
fun WyeRoundCheckbox(
    checked: Boolean,
    modifier: Modifier = Modifier
) {
    // 체크 여부에 따라 배경색이 스르륵 바뀌는 애니메이션
    val checkboxColor by animateColorAsState(
        targetValue = if (checked) PrimaryGreen else SurfaceWhite,
        animationSpec = tween(durationMillis = 200),
        label = "checkboxColor"
    )

    val borderColor by animateColorAsState(
        targetValue = if (checked) PrimaryGreen else Border,
        animationSpec = tween(durationMillis = 200),
        label = "borderColor"
    )

    Box(
        modifier = modifier
            .size(24.dp)
            .clip(RoundedCornerShape(6.dp))
            .background(checkboxColor)
            .border(width = 1.5.dp, color = borderColor, shape = RoundedCornerShape(6.dp))
            .padding(2.dp),
        contentAlignment = Alignment.Center
    ) {
        if (checked) {
            Icon(
                imageVector = Icons.Default.Check,
                contentDescription = null,
                tint = SurfaceWhite,
                modifier = Modifier.size(16.dp)
            )
        }
    }
}