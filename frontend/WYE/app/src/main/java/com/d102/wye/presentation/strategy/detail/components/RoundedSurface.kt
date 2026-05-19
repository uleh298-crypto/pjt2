package com.d102.wye.presentation.strategy.detail.components

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.d102.wye.presentation.theme.PrimaryGreen

@Composable
fun RoundedSurface(
    modifier: Modifier = Modifier,
    horizontalPaddingValue: Dp = 24.dp,
    backgroundColor: Color = Color.White,
    borderColor: Color = PrimaryGreen.copy(alpha = 0.1f),
    content: @Composable () -> Unit
) {
    Surface(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = horizontalPaddingValue),
        shape = RoundedCornerShape(16.dp),
        color = backgroundColor,
        border = BorderStroke(1.dp, borderColor),
        shadowElevation = 1.dp
    ) {
        content()
    }
}