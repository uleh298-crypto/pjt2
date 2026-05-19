package com.d102.wye.presentation.explore.stock

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowRight
import androidx.compose.material.icons.outlined.AccountBalance
import androidx.compose.material.icons.outlined.Description
import androidx.compose.material.icons.outlined.Hub
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.d102.wye.domain.model.RelatedStock
import com.d102.wye.domain.model.Stock
import com.d102.wye.domain.model.StockEtf
import com.d102.wye.presentation.designsystem.WyeTopBar
import com.d102.wye.presentation.model.UiState
import com.d102.wye.presentation.theme.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun StockDetailScreen(
    onBack: () -> Unit,
    onEtfListClick: (String) -> Unit,
    onEtfClick: (String) -> Unit,
    onRelatedStockClick: (String) -> Unit,
    viewModel: StockDetailViewModel = hiltViewModel(),
) {
    val stockState by viewModel.stockState.collectAsStateWithLifecycle()
    val relatedStocksState by viewModel.relatedStocksState.collectAsStateWithLifecycle()
    val tagsState by viewModel.tagsState.collectAsStateWithLifecycle()

    Scaffold(
        containerColor = MaterialTheme.colorScheme.background,
        topBar = {
            WyeTopBar(title = "종목 상세 분석", onBackClick = onBack)
        },
    ) { innerPadding ->
        when (val state = stockState) {
            is UiState.Loading -> Box(
                Modifier.fillMaxSize().padding(innerPadding),
                contentAlignment = Alignment.Center,
            ) { CircularProgressIndicator(color = PrimaryGreen) }

            is UiState.Success -> StockDetailContent(
                stock = state.data,
                tags = (tagsState as? UiState.Success)?.data ?: emptyList(),
                relatedStocks = (relatedStocksState as? UiState.Success)?.data ?: emptyList(),
                onEtfListClick = { onEtfListClick(state.data.ticker) },
                onEtfClick = onEtfClick,
                onRelatedStockClick = onRelatedStockClick,
                modifier = Modifier.padding(top = innerPadding.calculateTopPadding()),
            )

            is UiState.Error -> Box(
                Modifier.fillMaxSize().padding(innerPadding),
                contentAlignment = Alignment.Center,
            ) {
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(16.dp),
                ) {
                    Text(
                        "데이터를 불러올 수 없습니다",
                        style = MaterialTheme.typography.bodyMedium,
                        color = TextSecondary,
                    )
                    Button(
                        onClick = { viewModel.loadStock() },
                        colors = ButtonDefaults.buttonColors(containerColor = PrimaryGreen),
                    ) {
                        Text("다시 시도")
                    }
                }
            }

            UiState.Idle -> Unit
        }
    }
}

@Composable
private fun StockDetailContent(
    stock: Stock,
    tags: List<String>,
    relatedStocks: List<RelatedStock>,
    onEtfListClick: () -> Unit,
    onEtfClick: (String) -> Unit,
    onRelatedStockClick: (String) -> Unit,
    modifier: Modifier = Modifier,
) {
    Column(
        modifier = modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState()),
    ) {
        // ── 종목명 + 태그 + 카드 ──────────────────────────────────
        Column(modifier = Modifier.padding(horizontal = 20.dp)) {
            Spacer(Modifier.height(8.dp))
            Row(verticalAlignment = Alignment.Bottom, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                Text(stock.name, style = MaterialTheme.typography.titleLarge.copy(fontSize = 28.sp, fontWeight = FontWeight.SemiBold), color = TextPrimary)
                Text(stock.ticker, style = MaterialTheme.typography.bodyMedium, color = TextSecondary, modifier = Modifier.padding(bottom = 4.dp))
            }
            Spacer(Modifier.height(16.dp))
            Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                tags.forEach { tag -> TagChip(tag) }
            }
            Spacer(Modifier.height(16.dp))
            Row(
                modifier = Modifier.fillMaxWidth().height(IntrinsicSize.Max),
                horizontalArrangement = Arrangement.spacedBy(12.dp),
            ) {
                MarketCapCard(marketCap = stock.marketCap, modifier = Modifier.weight(1f).fillMaxHeight())
                CurrentPriceCard(price = stock.currentPrice, changeAmount = stock.changeAmount, modifier = Modifier.weight(1f).fillMaxHeight())
            }
            Spacer(Modifier.height(32.dp))
        }

        SectionDivider()

        // ── 회사 개요 ─────────────────────────────────────────────
        Column(modifier = Modifier.padding(horizontal = 20.dp)) {
            Spacer(Modifier.height(32.dp))
            SectionHeader(icon = { Icon(Icons.Outlined.Description, contentDescription = null, tint = TextPrimary, modifier = Modifier.size(18.dp)) }, title = "회사 개요")
            Spacer(Modifier.height(20.dp))
            Box(
                modifier = Modifier.fillMaxWidth().clip(RoundedCornerShape(12.dp)).background(SurfaceVariant).padding(16.dp),
            ) {
                Text(stock.description, style = MaterialTheme.typography.bodyMedium.copy(lineHeight = 22.sp), color = TextSecondary)
            }
            Spacer(Modifier.height(32.dp))
        }

        SectionDivider()

        // ── 포함된 ETF ────────────────────────────────────────────
        Column(modifier = Modifier.padding(horizontal = 20.dp)) {
            Spacer(Modifier.height(32.dp))
            SectionHeader(icon = { Icon(Icons.Outlined.AccountBalance, contentDescription = null, tint = TextPrimary, modifier = Modifier.size(18.dp)) }, title = "이 종목이 포함되어있는 ETF")
            Spacer(Modifier.height(20.dp))
            stock.containedEtfs.take(3).forEach { etf ->
                EtfWeightItem(etf = etf, onClick = { onEtfClick(etf.ticker) })
                Spacer(Modifier.height(36.dp))
            }
            Box(
                modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp),
                contentAlignment = Alignment.Center,
            ) {
                Text(
                    text = "포함된 ETF 전체보기",
                    style = MaterialTheme.typography.bodyMedium,
                    color = TextSecondary,
                    modifier = Modifier.clickable(onClick = onEtfListClick),
                )
            }
            Spacer(Modifier.height(32.dp))
        }

        SectionDivider()

        // ── 함께 등장하는 종목 ────────────────────────────────────
        Column(modifier = Modifier.padding(horizontal = 20.dp)) {
            Spacer(Modifier.height(32.dp))
            SectionHeader(icon = { Icon(Icons.Outlined.Hub, contentDescription = null, tint = TextPrimary, modifier = Modifier.size(18.dp)) }, title = "이 종목과 함께 등장하는 종목")
            Spacer(Modifier.height(20.dp))
            relatedStocks.forEach { related ->
                RelatedStockItem(stock = related, onClick = { onRelatedStockClick(related.ticker) })
                HorizontalDivider(color = Divider)
            }
            Spacer(Modifier.height(32.dp))
        }
    }
}

@Composable
private fun TagChip(label: String) {
    Box(
        modifier = Modifier
            .clip(RoundedCornerShape(6.dp))
            .background(SurfaceCard)
            .padding(horizontal = 10.dp, vertical = 4.dp),
    ) {
        Text(label, style = MaterialTheme.typography.labelMedium, color = TextSecondary)
    }
}

@Composable
private fun MarketCapCard(marketCap: Long, modifier: Modifier = Modifier) {
    Column(
        modifier = modifier
            .clip(RoundedCornerShape(12.dp))
            .background(SurfaceCard)
            .padding(14.dp),
        verticalArrangement = Arrangement.spacedBy(6.dp),
    ) {
        Text("시가총액", style = MaterialTheme.typography.bodySmall, color = TextSecondary)
        Text(
            formatMarketCap(marketCap),
            style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.SemiBold),
            color = TextPrimary,
        )
    }
}

@Composable
private fun CurrentPriceCard(price: Long, changeAmount: Long, modifier: Modifier = Modifier) {
    val isRise = changeAmount >= 0
    val changeColor = if (isRise) EtfRise else EtfFall
    val sign = if (isRise) "▲" else "▼"

    Column(
        modifier = modifier
            .clip(RoundedCornerShape(12.dp))
            .background(SurfaceCard)
            .padding(14.dp),
        verticalArrangement = Arrangement.spacedBy(6.dp),
    ) {
        Text("현재가", style = MaterialTheme.typography.bodySmall, color = TextSecondary)
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(6.dp),
        ) {
            Text(
                "%,d원".format(price),
                style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.SemiBold),
                color = TextPrimary,
            )
            Text(
                "$sign%,d".format(kotlin.math.abs(changeAmount)),
                style = MaterialTheme.typography.labelMedium,
                color = changeColor,
            )
        }
    }
}

@Composable
private fun SectionHeader(icon: @Composable () -> Unit, title: String) {
    Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
        icon()
        Text(title, style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.SemiBold), color = TextPrimary)
    }
}

@Composable
private fun EtfWeightItem(etf: StockEtf, onClick: () -> Unit) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick),
        verticalArrangement = Arrangement.spacedBy(6.dp),
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.Top,
        ) {
            Text(etf.name, style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.SemiBold), color = TextPrimary)
            Text("비중", style = MaterialTheme.typography.bodySmall.copy(fontSize = 11.sp), color = TextSecondary)
        }
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Text(
                "${etf.manager} · ${etf.ticker}",
                style = MaterialTheme.typography.bodySmall,
                color = TextSecondary,
            )
            Text(
                "${"%.1f".format(etf.weight)}%",
                style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.SemiBold),
                color = TextPrimary,
            )
        }
        // 프로그레스바
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(4.dp)
                .clip(RoundedCornerShape(2.dp))
                .background(SurfaceVariant),
        ) {
            Box(
                modifier = Modifier
                    .fillMaxWidth((etf.weight / 100.0).toFloat().coerceIn(0f, 1f))
                    .fillMaxHeight()
                    .clip(RoundedCornerShape(2.dp))
                    .background(PrimaryGreen),
            )
        }
    }
}

@Composable
private fun RelatedStockItem(stock: RelatedStock, onClick: () -> Unit) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
            .padding(vertical = 18.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(14.dp),
    ) {
        // 2글자 약어 아바타
        Box(
            contentAlignment = Alignment.Center,
            modifier = Modifier
                .size(44.dp)
                .clip(RoundedCornerShape(10.dp))
                .background(SurfaceVariant),
        ) {
            Text(
                stock.name.take(2),
                style = MaterialTheme.typography.bodySmall.copy(fontSize = 13.sp, fontWeight = FontWeight.SemiBold),
                color = TextPrimary,
            )
        }

        Column(modifier = Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(3.dp)) {
            Text(stock.name, style = MaterialTheme.typography.titleSmall.copy(fontSize = 15.sp, fontWeight = FontWeight.SemiBold), color = TextPrimary)
            Text(stock.description, style = MaterialTheme.typography.bodySmall, color = TextSecondary)
        }

        Icon(
            imageVector = Icons.AutoMirrored.Filled.KeyboardArrowRight,
            contentDescription = null,
            tint = TextSecondary,
            modifier = Modifier.size(20.dp),
        )
    }
}

@Composable
private fun SectionDivider() {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(8.dp)
            .background(Color(0xFFF4F6F8))
    )
}

private fun formatMarketCap(marketCap: Long): String {
    // marketCap 단위: 억원
    val jo  = marketCap / 10_000L
    val eok = marketCap % 10_000L
    return when {
        jo > 0 && eok > 0 -> "${jo}조 ${"%,d".format(eok)}억원"
        jo > 0             -> "${jo}조원"
        eok > 0            -> "${"%,d".format(eok)}억원"
        else               -> "%,d억원".format(marketCap)
    }
}
