package com.d102.wye.presentation.simulation.entry

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.d102.wye.domain.model.EtfBundle
import com.d102.wye.presentation.designsystem.WyeBadge
import com.d102.wye.presentation.designsystem.WyeOutlinedButton
import com.d102.wye.presentation.designsystem.WyeOutlinedCard
import com.d102.wye.presentation.theme.PrimaryGreen
import com.d102.wye.presentation.theme.SurfaceVariant
import com.d102.wye.presentation.theme.TextPrimary
import com.d102.wye.presentation.theme.TextTertiary


@Composable
fun BundleCard(bundle: EtfBundle, onClick: () -> Unit) {
    WyeOutlinedCard(
        modifier = Modifier
            .width(230.dp)
            .clickable(onClick = onClick)
    ) {
        // 이모지 아이콘
        Box(
            modifier = Modifier
                .size(50.dp)
                .background(
                    bundle.bundleType.toBackgroundColor(),
                    RoundedCornerShape(12.dp)
                ),
            contentAlignment = Alignment.Center
        ) {
            Image(
                painter = painterResource(bundle.bundleType.toDrawable()),
                contentDescription = null,
                modifier = Modifier.size(36.dp)
            )
        }

        Spacer(modifier = Modifier.height(12.dp))

        // 제목
        Text(
            text = bundle.name,
            fontSize = 16.sp,
            fontWeight = FontWeight.Bold,
            color = TextPrimary
        )

        Spacer(modifier = Modifier.height(4.dp))

        // 설명
        Text(
            modifier = Modifier.heightIn(min = 50.dp),
            text = bundle.summary,
            fontSize = 13.sp,
            color = TextTertiary,
            maxLines = 2
        )

        Spacer(modifier = Modifier.height(12.dp))

        FlowRow(
            horizontalArrangement = Arrangement.spacedBy(6.dp),
            verticalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            bundle.tags.forEach { tag ->
                WyeBadge(
                    label = "#$tag",
                    textStyle = MaterialTheme.typography.labelSmall,
                    color = SurfaceVariant,
                    textColor = TextTertiary
                )
            }
        }

        Spacer(modifier = Modifier.height(12.dp))

        WyeOutlinedButton(
            text = "상세보기",
            style = MaterialTheme.typography.labelMedium,
            verticalPaddingValues = 4.dp,
            borderColor = PrimaryGreen.copy(alpha = 0.3f),
            onClick = onClick
        )
    }
}