package com.d102.wye.presentation.simulation.entry

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.d102.wye.presentation.designsystem.WyeCard
import com.d102.wye.presentation.designsystem.WyeCircleIcon
import com.d102.wye.presentation.theme.PrimaryGreen
import com.d102.wye.presentation.theme.TextTertiary

@Composable
fun EtfItemRow(item: String) {
    WyeCard(
        modifier = Modifier.fillMaxWidth(),
        border = BorderStroke(1.dp, PrimaryGreen.copy(alpha = 0.1f)),
        containerColor = PrimaryGreen.copy(alpha = 0.05f), elevation = 0.dp
    ) {
        Row(
            horizontalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            WyeCircleIcon(tag = item)

            Column(
                modifier = Modifier.fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                Text(
                    text = item,
                    style = MaterialTheme.typography.titleSmall.copy(fontSize = 14.sp)
                )
                Text(
                    text = "ETF Full Name (Mock)",
                    style = MaterialTheme.typography.bodySmall,
                    color = TextTertiary
                )
            }
        }
    }

}