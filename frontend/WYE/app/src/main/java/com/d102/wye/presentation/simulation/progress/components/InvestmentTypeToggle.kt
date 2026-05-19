package com.d102.wye.presentation.simulation.progress.components

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.d102.wye.domain.state.InvestmentType
import com.d102.wye.presentation.theme.SurfaceDivider
import com.d102.wye.presentation.theme.TextPrimary
import com.d102.wye.presentation.theme.TextSecondary


@Composable
fun InvestmentTypeToggle(
    selectedType: InvestmentType,
    onTypeSelected: (InvestmentType) -> Unit,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .background(SurfaceDivider, RoundedCornerShape(12.dp)),
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        val isREGULARSAVING = selectedType == InvestmentType.REGULAR_SAVING

        // 적립형 버튼
        ToggleItem(
            text = "적립형",
            isSelected = isREGULARSAVING,
            modifier = Modifier.weight(1f),
            onClick = { onTypeSelected(InvestmentType.REGULAR_SAVING) }
        )

        // 거치형 버튼
        ToggleItem(
            text = "거치형",
            isSelected = !isREGULARSAVING,
            modifier = Modifier.weight(1f),
            onClick = { onTypeSelected(InvestmentType.LUMP_SUM) }
        )
    }
}

@Composable
private fun ToggleItem(
    text: String,
    isSelected: Boolean,
    modifier: Modifier = Modifier,
    onClick: () -> Unit
) {
    Surface(
        modifier = modifier
            .height(40.dp)
            .clip(RoundedCornerShape(12.dp))
            .clickable { onClick() },
        color = if (isSelected) Color.White else Color.Transparent,
        shadowElevation = if (isSelected) 2.dp else 0.dp
    ) {
        Box(contentAlignment = Alignment.Center) {
            Text(
                text = text,
                style = MaterialTheme.typography.bodyMedium,
                fontWeight = if (isSelected) FontWeight.SemiBold else FontWeight.Medium,
                color = if (isSelected) TextPrimary else TextSecondary
            )
        }
    }
}