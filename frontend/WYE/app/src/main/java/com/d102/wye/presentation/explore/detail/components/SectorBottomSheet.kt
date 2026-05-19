package com.d102.wye.presentation.explore.detail.components

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.d102.wye.domain.model.EtfCluster
import com.d102.wye.domain.model.EtfClusterStock
import com.d102.wye.presentation.theme.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SectorBottomSheet(
    cluster: EtfCluster,
    onDismiss: () -> Unit,
    onStockClick: (String) -> Unit = {},
) {
    ModalBottomSheet(onDismissRequest = onDismiss, containerColor = Background) {
        SectorBottomSheetContent(cluster = cluster, onStockClick = onStockClick)
    }
}

@Composable
fun SectorBottomSheetContent(
    cluster: EtfCluster,
    onStockClick: (String) -> Unit = {},
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .verticalScroll(rememberScrollState())
            .padding(horizontal = 20.dp)
            .padding(bottom = 32.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp),
    ) {
        Text("${cluster.name} 산업", style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.SemiBold), color = TextPrimary)
        HorizontalDivider(color = Divider)
        Text("주요 구성 종목", style = MaterialTheme.typography.labelLarge.copy(fontWeight = FontWeight.SemiBold), color = TextSecondary)
        cluster.stocks.forEach { stock ->
            StockProgressItem(
                stock = stock,
                onClick = if (cluster.assetType == null && stock.ticker.isNotBlank()) { { onStockClick(stock.ticker) } } else null,
            )
        }
        if (!cluster.aiAnalysis.isNullOrBlank()) AiAnalysisBox(cluster.aiAnalysis)
        Spacer(Modifier.height(8.dp))
    }
}

@Composable
private fun StockProgressItem(stock: EtfClusterStock, onClick: (() -> Unit)? = null) {
    Column(
        verticalArrangement = Arrangement.spacedBy(6.dp),
        modifier = if (onClick != null) Modifier.clickable(onClick = onClick) else Modifier,
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(10.dp),
            ) {
                Box(
                    contentAlignment = Alignment.Center,
                    modifier = Modifier.size(36.dp).clip(RoundedCornerShape(10.dp)).background(BackGroundLightGreen),
                ) {
                    Text(stock.name.take(1), style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.SemiBold), color = PrimaryGreen)
                }
                Text(stock.name, style = MaterialTheme.typography.titleSmall.copy(fontSize = 15.sp, fontWeight = FontWeight.Medium), color = TextPrimary)
            }
            Text("${"%.1f".format(stock.percentage)}%", style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.SemiBold), color = TextSecondary)
        }
        Box(modifier = Modifier.fillMaxWidth().height(4.dp).clip(RoundedCornerShape(2.dp)).background(BackGroundLightGreen)) {
            Box(modifier = Modifier.fillMaxWidth((stock.percentage / 100.0).toFloat().coerceIn(0f, 1f)).fillMaxHeight().clip(RoundedCornerShape(2.dp)).background(PrimaryGreen))
        }
    }
}

@Composable
private fun AiAnalysisBox(analysis: String) {
    Row(
        modifier = Modifier.fillMaxWidth().clip(RoundedCornerShape(12.dp)).background(BackGroundLightGreen).padding(14.dp),
        horizontalArrangement = Arrangement.spacedBy(10.dp),
    ) {
        Icon(
            imageVector = Icons.Filled.AutoAwesome,
            contentDescription = "AI 분석",
            tint = PrimaryGreen,
            modifier = Modifier.size(18.dp).padding(top = 1.dp),
        )
        Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
            Text("AI 분석 결과", style = MaterialTheme.typography.bodySmall.copy(fontSize = 13.sp, fontWeight = FontWeight.SemiBold), color = PrimaryGreen)
            Text(analysis, style = MaterialTheme.typography.bodySmall.copy(fontSize = 13.sp, lineHeight = 20.sp), color = TextSecondary)
        }
    }
}
