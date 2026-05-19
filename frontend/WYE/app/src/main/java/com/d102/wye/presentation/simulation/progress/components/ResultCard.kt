package com.d102.wye.presentation.simulation.progress.components

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import com.d102.wye.presentation.designsystem.WyeCard

@Composable
fun ResultCard(
    modifier: Modifier = Modifier,
    borderColor: Color = Color.Transparent,
    backgroundColor: Color = Color.White,
    content: @Composable () -> Unit
) {
    WyeCard(
        modifier = modifier
            .fillMaxWidth(),
        border = BorderStroke(1.dp, borderColor),
        containerColor = backgroundColor,
        elevation = 0.dp
    ) {
        content()
    }
}