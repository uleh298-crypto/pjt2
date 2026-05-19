package com.d102.wye.presentation.home.news

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.OpenInNew
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalUriHandler
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import coil.compose.AsyncImage
import com.d102.wye.R
import com.d102.wye.presentation.designsystem.WyeTopBar
import com.d102.wye.presentation.model.UiState
import com.d102.wye.presentation.theme.BackGroundLightGreen
import com.d102.wye.presentation.theme.BackGroundLightGreen2
import com.d102.wye.presentation.theme.Background
import com.d102.wye.presentation.theme.EtfFall
import com.d102.wye.presentation.theme.EtfNeutral
import com.d102.wye.presentation.theme.EtfRise
import com.d102.wye.presentation.theme.PrimaryGreen
import com.d102.wye.presentation.theme.PrimaryGreenDark
import com.d102.wye.presentation.theme.SurfaceWhite
import com.d102.wye.presentation.theme.TextOnColored
import com.d102.wye.presentation.theme.TextPrimary
import com.d102.wye.presentation.theme.TextSecondary

data class NewsDetailUiModel(
    val id: Long,
    val title: String,
    val date: String,
    val thumbnailUrl: String? = null,
    val tags: List<String>,
    val aiSummary: List<String>,
    val body: String,
    val sourceUrl: String,
    val source: String,
    val relatedEtfs: List<RelatedEtfUiModel>,
)

data class RelatedEtfUiModel(
    val ticker: String,
    val name: String,
    val manager: String,
    val changeRate: Double,
    val thumbnailUrl: String? = null,
)

@Composable
fun NewsDetailScreen(
    newsId: Long,
    onBack: () -> Unit,
    onEtfClick: (String) -> Unit = {},
    viewModel: NewsDetailViewModel = hiltViewModel(),
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val snackbarHostState = remember { SnackbarHostState() }
    val uriHandler = LocalUriHandler.current

    LaunchedEffect(newsId) {
        viewModel.loadNewsDetail(newsId)
    }

    LaunchedEffect(uiState) {
        if (uiState is UiState.Error) {
            snackbarHostState.showSnackbar((uiState as UiState.Error).message)
        }
    }

    Scaffold(
        containerColor = Background,
        topBar = { WyeTopBar(title = "뉴스 상세보기", onBackClick = onBack) },
        snackbarHost = { SnackbarHost(hostState = snackbarHostState) },
    ) { innerPadding ->
        when (val state = uiState) {
            is UiState.Loading -> {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(top = innerPadding.calculateTopPadding()),
                    contentAlignment = Alignment.Center,
                ) {
                    CircularProgressIndicator()
                }
            }

            is UiState.Error -> {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(top = innerPadding.calculateTopPadding()),
                    contentAlignment = Alignment.Center,
                ) {
                    Button(onClick = viewModel::refresh) {
                        Text("다시 시도")
                    }
                }
            }

            UiState.Idle -> Unit

            is UiState.Success -> {
                NewsDetailContent(
                    news = state.data,
                    contentPadding = innerPadding,
                    onOpenSource = { uriHandler.openUri(state.data.sourceUrl) },
                    onEtfClick = onEtfClick,
                )
            }
        }
    }
}

@Composable
private fun NewsDetailContent(
    news: NewsDetailUiModel,
    contentPadding: PaddingValues,
    onOpenSource: () -> Unit,
    onEtfClick: (String) -> Unit,
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(top = contentPadding.calculateTopPadding())
            .verticalScroll(rememberScrollState()),
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(220.dp)
                .background(PrimaryGreenDark),
        ) {
            if (news.thumbnailUrl != null) {
                AsyncImage(
                    model = news.thumbnailUrl,
                    contentDescription = null,
                    contentScale = ContentScale.Crop,
                    modifier = Modifier.fillMaxSize(),
                )
            }
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(PrimaryGreenDark.copy(alpha = 0.55f)),
            )
            Column(
                modifier = Modifier
                    .align(Alignment.BottomStart)
                    .padding(16.dp),
            ) {
                Text(
                    text = news.title,
                    style = MaterialTheme.typography.titleMedium.copy(
                        fontWeight = FontWeight.SemiBold,
                        lineHeight = 28.sp,
                    ),
                    color = TextOnColored,
                )
                Spacer(Modifier.height(6.dp))
                Text(
                    text = news.date,
                    style = MaterialTheme.typography.bodySmall,
                    color = TextOnColored.copy(alpha = 0.8f),
                )
            }
        }

        FlowRow(
            modifier = Modifier
                .fillMaxWidth()
                .horizontalScroll(rememberScrollState())
                .padding(horizontal = 16.dp, vertical = 16.dp),
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            verticalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            news.tags.forEach { tag ->
                Text(
                    text = "#$tag",
                    style = MaterialTheme.typography.bodySmall.copy(
                        fontSize = 13.sp,
                        fontWeight = FontWeight.Medium,
                    ),
                    color = PrimaryGreen,
                    modifier = Modifier
                        .clip(RoundedCornerShape(20.dp))
                        .background(BackGroundLightGreen)
                        .padding(horizontal = 12.dp, vertical = 6.dp),
                )
            }
        }

        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp)
                .clip(RoundedCornerShape(12.dp))
                .background(BackGroundLightGreen2)
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(6.dp),
            ) {
                Icon(
                    painter = androidx.compose.ui.res.painterResource(id = R.drawable.ic_shining),
                    contentDescription = null,
                    tint = PrimaryGreen,
                    modifier = Modifier.size(20.dp),
                )
                Text(
                    text = "AI 핵심 요약",
                    style = MaterialTheme.typography.labelLarge.copy(fontWeight = FontWeight.SemiBold),
                    color = PrimaryGreen,
                )
            }
            news.aiSummary.forEach { bullet ->
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text(
                        text = "•",
                        style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.SemiBold),
                        color = TextPrimary,
                    )
                    Text(
                        text = bullet,
                        style = MaterialTheme.typography.bodyMedium.copy(lineHeight = 21.sp),
                        color = TextPrimary,
                    )
                }
            }
        }

        Spacer(Modifier.height(16.dp))

        Text(
            text = news.body,
            style = MaterialTheme.typography.bodyMedium.copy(
                fontSize = 15.sp,
                lineHeight = 24.sp,
            ),
            color = TextPrimary,
            modifier = Modifier.padding(horizontal = 16.dp),
        )

        Spacer(Modifier.height(24.dp))

        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp)
                .clip(RoundedCornerShape(12.dp))
                .background(BackGroundLightGreen2)
                .clickable(onClick = onOpenSource)
                .padding(horizontal = 20.dp, vertical = 16.dp),
            horizontalArrangement = Arrangement.Center,
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Text(
                text = "원문 전체 보러가기",
                style = MaterialTheme.typography.labelLarge.copy(fontSize = 15.sp),
                color = TextPrimary,
            )
            Spacer(Modifier.width(8.dp))
            Icon(
                imageVector = Icons.Default.OpenInNew,
                contentDescription = null,
                tint = TextSecondary,
                modifier = Modifier.size(18.dp),
            )
        }
        Text(
            text = "출처: ${news.source}",
            style = MaterialTheme.typography.bodySmall,
            color = TextSecondary,
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 12.dp),
            textAlign = TextAlign.Center,
        )

        if (news.relatedEtfs.isNotEmpty()) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(BackGroundLightGreen2)
                    .padding(vertical = 16.dp),
            ) {
                Text(
                    text = "관련 ETF 추천",
                    style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.SemiBold),
                    color = TextPrimary,
                    modifier = Modifier.padding(horizontal = 16.dp, vertical = 4.dp),
                )
                Spacer(Modifier.height(8.dp))
                LazyRow(
                    contentPadding = PaddingValues(horizontal = 16.dp),
                    horizontalArrangement = Arrangement.spacedBy(12.dp),
                ) {
                    items(news.relatedEtfs, key = { it.ticker }) { etf ->
                        RelatedEtfCard(etf = etf, onClick = { onEtfClick(etf.ticker) })
                    }
                }
            }
        }
    }
}

@Composable
private fun RelatedEtfCard(etf: RelatedEtfUiModel, onClick: () -> Unit) {
    val rateColor = when {
        etf.changeRate > 0 -> EtfRise
        etf.changeRate < 0 -> EtfFall
        else -> EtfNeutral
    }
    val rateText = if (etf.changeRate > 0) "+${etf.changeRate}%" else "${etf.changeRate}%"

    Column(
        modifier = Modifier
            .width(150.dp)
            .clip(RoundedCornerShape(12.dp))
            .background(SurfaceWhite)
            .clickable(onClick = onClick)
            .padding(14.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp),
    ) {
        Box(
            modifier = Modifier
                .size(48.dp)
                .clip(CircleShape)
                .background(BackGroundLightGreen),
            contentAlignment = Alignment.Center,
        ) {
            if (etf.thumbnailUrl != null) {
                AsyncImage(
                    model = etf.thumbnailUrl,
                    contentDescription = null,
                    modifier = Modifier
                        .fillMaxSize()
                        .clip(CircleShape),
                )
            } else {
                Text(
                    text = etf.name.take(1),
                    style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.SemiBold),
                    color = PrimaryGreen,
                )
            }
        }
        Text(
            text = etf.name,
            style = MaterialTheme.typography.bodySmall.copy(
                fontSize = 13.sp,
                fontWeight = FontWeight.Medium,
            ),
            color = TextPrimary,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
        )
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Text(
                text = etf.manager,
                style = MaterialTheme.typography.bodySmall,
                color = TextSecondary,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
                modifier = Modifier.weight(1f),
            )
            Text(
                text = rateText,
                style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.SemiBold),
                color = rateColor,
            )
        }
    }
}
