package com.d102.wye.presentation.designsystem

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.d102.wye.presentation.theme.TextPrimary
import com.d102.wye.presentation.theme.TextSecondary

@Composable
fun EtfInfoCard(
    name: String,
    currentPrice: Long,
    riskType: String,
    modifier: Modifier = Modifier,
    supportingText: String? = null
) {
    val (badgeBg, badgeFg, badgeLabel) = riskToBadge(riskType)

    Column(modifier = modifier) {
        Text(text = name, color = TextPrimary, style = MaterialTheme.typography.labelLarge)
        Spacer(Modifier.height(4.dp))
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(6.dp),
        ) {
            Text(text = "%,d원".format(currentPrice), color = TextSecondary, style = MaterialTheme.typography.bodySmall)
            CategoryBadge(label = badgeLabel, backgroundColor = badgeBg, textColor = badgeFg)
        }
        if (!supportingText.isNullOrBlank()) {
            Spacer(Modifier.height(4.dp))
            Text(
                text = supportingText,
                color = TextSecondary,
                style = MaterialTheme.typography.bodySmall
            )
        }
    }
}
