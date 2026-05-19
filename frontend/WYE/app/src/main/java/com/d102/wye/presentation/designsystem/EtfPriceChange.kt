package com.d102.wye.presentation.designsystem

import androidx.compose.foundation.layout.Column
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.material3.MaterialTheme
import androidx.compose.ui.text.font.FontWeight
import com.d102.wye.presentation.theme.EtfFall
import com.d102.wye.presentation.theme.EtfNeutral
import com.d102.wye.presentation.theme.EtfRise


@Composable
fun EtfPriceChange(changeRate: Double, changeAmount: Long) {
    val changeColor = when {
        changeRate > 0 -> EtfRise
        changeRate < 0 -> EtfFall
        else           -> EtfNeutral
    }
    val sign  = if (changeRate > 0) "+" else ""
    val arrow = when {
        changeRate > 0 -> "▲"
        changeRate < 0 -> "▼"
        else           -> ""
    }

    Column(horizontalAlignment = Alignment.End) {
        Text(
            text = "$sign${"%.2f".format(changeRate)}%",
            color = changeColor,
            style = MaterialTheme.typography.labelLarge.copy(fontWeight = FontWeight.SemiBold),
        )
        Text(
            text = if (changeRate == 0.0) "-" else "$arrow ${"%,d".format(changeAmount)}",
            color = changeColor,
            style = MaterialTheme.typography.labelSmall,
        )
    }
}
