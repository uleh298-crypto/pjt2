package com.d102.wye.presentation.home.news

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.scaleIn
import androidx.compose.animation.scaleOut
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import coil.compose.AsyncImage
import com.d102.wye.presentation.designsystem.WyeTopBar
import com.d102.wye.presentation.model.UiState
import com.d102.wye.presentation.theme.*

// ── 뉴스 목록 화면 ──────────────────────────────────────────────

@Composable
fun NewsListScreen(
    onBack: () -> Unit,
    onNewsClick: (Long) -> Unit,
    viewModel: NewsListViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val searchQuery by viewModel.searchQuery.collectAsStateWithLifecycle()
    var isSearchActive by remember { mutableStateOf(false) }
    val focusRequester = remember { FocusRequester() }
    val snackbarHostState = remember { SnackbarHostState() }
    val categoryScrollState = rememberScrollState()

    LaunchedEffect(isSearchActive) {
        if (isSearchActive) focusRequester.requestFocus()
    }

    LaunchedEffect(uiState) {
        if (uiState is UiState.Error) {
            snackbarHostState.showSnackbar((uiState as UiState.Error).message)
        }
    }

    Scaffold(
        containerColor = Background,
        snackbarHost = { SnackbarHost(hostState = snackbarHostState) },
        topBar = {
            AnimatedContent(
                targetState = isSearchActive,
                transitionSpec = {
                    val enter = fadeIn(tween(250, easing = FastOutSlowInEasing)) +
                            scaleIn(initialScale = 0.96f, animationSpec = tween(250, easing = FastOutSlowInEasing))
                    val exit = fadeOut(tween(150)) +
                            scaleOut(targetScale = 0.96f, animationSpec = tween(150))
                    enter togetherWith exit
                },
                label = "search_topbar",
            ) { searchMode ->
                if (searchMode) {
                    // 검색 모드 TopBar
                    Surface(
                        modifier = Modifier.fillMaxWidth(),
                        color = SurfaceWhite,
                        shadowElevation = 2.dp,
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .statusBarsPadding()
                                .height(64.dp)
                                .padding(horizontal = 4.dp),
                            verticalAlignment = Alignment.CenterVertically,
                        ) {
                            IconButton(onClick = {
                                isSearchActive = false
                                viewModel.onSearchQueryChanged("")
                            }) {
                                Icon(
                                    imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                                    contentDescription = "검색 닫기",
                                    tint = TextPrimary,
                                )
                            }
                            TextField(
                                value = searchQuery,
                                onValueChange = { viewModel.onSearchQueryChanged(it) },
                                modifier = Modifier
                                    .weight(1f)
                                    .focusRequester(focusRequester),
                                placeholder = {
                                    Text(
                                        text = "뉴스 검색",
                                        style = MaterialTheme.typography.bodyMedium.copy(fontSize = 15.sp),
                                        color = TextSecondary,
                                    )
                                },
                                singleLine = true,
                                keyboardOptions = KeyboardOptions(imeAction = ImeAction.Search),
                                keyboardActions = KeyboardActions(onSearch = { /* 검색 실행 */ }),
                                colors = TextFieldDefaults.colors(
                                    focusedContainerColor = SurfaceWhite,
                                    unfocusedContainerColor = SurfaceWhite,
                                    focusedIndicatorColor = androidx.compose.ui.graphics.Color.Transparent,
                                    unfocusedIndicatorColor = androidx.compose.ui.graphics.Color.Transparent,
                                    cursorColor = PrimaryGreen,
                                ),
                                textStyle = MaterialTheme.typography.bodyMedium.copy(
                                    fontSize = 15.sp,
                                    color = TextPrimary,
                                ),
                            )
                            if (searchQuery.isNotEmpty()) {
                                IconButton(onClick = { viewModel.onSearchQueryChanged("") }) {
                                    Icon(
                                        imageVector = Icons.Default.Close,
                                        contentDescription = "지우기",
                                        tint = TextSecondary,
                                        modifier = Modifier.size(18.dp),
                                    )
                                }
                            }
                        }
                    }
                } else {
                    // 일반 TopBar
                    WyeTopBar(
                        title = "뉴스",
                        onBackClick = onBack,
                        actions = {
                            IconButton(onClick = { isSearchActive = true }) {
                                Icon(
                                    imageVector = Icons.Default.Search,
                                    contentDescription = "검색",
                                    tint = TextPrimary,
                                )
                            }
                        }
                    )
                }
            }
        }
    ) { innerPadding ->
        val state = uiState as? UiState.Success
        val categories = state?.data?.categories.orEmpty()
        val selectedCategoryCode = state?.data?.selectedCategoryCode
        val allNews = state?.data?.newsList.orEmpty()
        val isLast = state?.data?.isLast == true
        val isLoadingMore = state?.data?.isLoadingMore == true
        val isRefreshing = state?.data?.isRefreshing == true
        val displayNews = allNews
        val featured = if (searchQuery.isBlank()) displayNews.firstOrNull() else null
        val restNews = if (searchQuery.isBlank() && displayNews.size > 1) displayNews.drop(1) else displayNews

        if (uiState is UiState.Loading) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(top = innerPadding.calculateTopPadding()),
                contentAlignment = Alignment.Center
            ) {
                CircularProgressIndicator()
            }
            return@Scaffold
        }

        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(top = innerPadding.calculateTopPadding()),
        ) {
        LazyColumn(
            modifier = Modifier.weight(1f),
        ) {
            // 카테고리 칩 (고정)
            stickyHeader {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(Background),
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .horizontalScroll(categoryScrollState)
                            .padding(horizontal = 16.dp, vertical = 12.dp),
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                    ) {
                        categories.forEach { cat ->
                            val selected = cat.code == selectedCategoryCode
                            Text(
                                text = cat.label,
                                style = MaterialTheme.typography.labelLarge.copy(
                                    fontWeight = if (selected) FontWeight.SemiBold else FontWeight.Normal,
                                ),
                                color = if (selected) TextOnColored else TextPrimary,
                                modifier = Modifier
                                    .clip(RoundedCornerShape(20.dp))
                                    .background(if (selected) PrimaryGreen else SurfaceVariant)
                                    .clickable { viewModel.onCategorySelected(cat.code) }
                                    .padding(horizontal = 16.dp, vertical = 8.dp),
                            )
                        }
                    }
                    if (isRefreshing) {
                        LinearProgressIndicator(
                            modifier = Modifier.fillMaxWidth(),
                            color = PrimaryGreen,
                            trackColor = SurfaceVariant,
                        )
                    }
                }
            }

            // 검색 결과 없음
            if (displayNews.isEmpty()) {
                item {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(top = 80.dp),
                        contentAlignment = Alignment.Center,
                    ) {
                        Column(
                            horizontalAlignment = Alignment.CenterHorizontally,
                            verticalArrangement = Arrangement.spacedBy(8.dp),
                        ) {
                            Icon(
                                imageVector = Icons.Default.Search,
                                contentDescription = null,
                                tint = TextSecondary,
                                modifier = Modifier.size(40.dp),
                            )
                            val emptyMessage = if (searchQuery.isBlank()) "뉴스가 없습니다" else "검색 결과가 없습니다"
                            Text(
                                text = emptyMessage,
                                style = MaterialTheme.typography.bodyMedium.copy(fontSize = 15.sp),
                                color = TextSecondary,
                            )
                        }
                    }
                }
            }

            // 피처드 뉴스 (첫 번째) — 검색어 입력 중에는 숨김
            if (featured != null && searchQuery.isBlank()) {
                item(key = "featured_${featured.id}") {
                    FeaturedNewsCard(
                        news = featured,
                        onClick = { onNewsClick(featured.id) },
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 16.dp)
                            .padding(bottom = 20.dp),
                    )
                }
            }

            // 최신 뉴스 헤더
            if (restNews.isNotEmpty()) {
                item {
                    Text(
                        text = if (isSearchActive && searchQuery.isNotBlank()) "검색 결과" else "최신 뉴스",
                        style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.SemiBold),
                        color = TextPrimary,
                        modifier = Modifier.padding(horizontal = 16.dp, vertical = 4.dp),
                    )
                }
            }

            // 나머지 뉴스 목록
            itemsIndexed(restNews, key = { _, it -> it.id }) { index, news ->
                // 마지막에서 3번째 아이템 도달 시 다음 페이지 로드
                if (index == restNews.lastIndex - 2 && searchQuery.isBlank()) {
                    LaunchedEffect(index) { viewModel.loadMore() }
                }
                NewsListItem(
                    news = news,
                    onClick = { onNewsClick(news.id) },
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 16.dp),
                )
                HorizontalDivider(
                    modifier = Modifier.padding(horizontal = 16.dp),
                    color = Divider,
                )
            }

            // 더 불러오는 중 인디케이터
            if (isLoadingMore) {
                item {
                    Box(
                        modifier = Modifier.fillMaxWidth().padding(16.dp),
                        contentAlignment = Alignment.Center,
                    ) {
                        CircularProgressIndicator(
                            modifier = Modifier.size(24.dp),
                            strokeWidth = 2.dp,
                            color = PrimaryGreen,
                        )
                    }
                }
            }

            item { Spacer(Modifier.height(20.dp)) }
        }
        } // Column 닫기
    }
}

// ── 피처드 카드 ─────────────────────────────────────────────────

@Composable
private fun FeaturedNewsCard(
    news: NewsListItemUiModel,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Box(
        modifier = modifier
            .height(200.dp)
            .clip(RoundedCornerShape(16.dp))
            .background(PrimaryGreenDark)
            .clickable(onClick = onClick),
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
                style = MaterialTheme.typography.bodyLarge.copy(
                    fontWeight = FontWeight.SemiBold,
                    lineHeight = 24.sp,
                ),
                color = TextOnColored,
                maxLines = 2,
                overflow = TextOverflow.Ellipsis,
            )
            Spacer(Modifier.height(6.dp))
            Text(
                text = "${news.timeAgo} · ${news.source}",
                style = MaterialTheme.typography.bodySmall,
                color = TextOnColored.copy(alpha = 0.8f),
            )
        }
    }
}

// ── 목록 아이템 ─────────────────────────────────────────────────

@Composable
private fun NewsListItem(
    news: NewsListItemUiModel,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Row(
        modifier = modifier.clickable(onClick = onClick),
        horizontalArrangement = Arrangement.spacedBy(12.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Column(modifier = Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(4.dp)) {
            Text(
                text = news.category,
                style = MaterialTheme.typography.labelSmall,
                color = PrimaryGreen,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
            )
            Text(
                text = news.title,
                style = MaterialTheme.typography.bodyMedium.copy(
                    fontSize = 15.sp,
                    fontWeight = FontWeight.SemiBold,
                    lineHeight = 21.sp,
                ),
                color = TextPrimary,
                maxLines = 2,
                overflow = TextOverflow.Ellipsis,
            )
            Text(
                text = "${news.timeAgo} · ${news.source}",
                style = MaterialTheme.typography.labelSmall,
                color = TextSecondary,
            )
        }
        if (news.thumbnailUrl != null) {
            AsyncImage(
                model = news.thumbnailUrl,
                contentDescription = null,
                contentScale = ContentScale.Crop,
                modifier = Modifier
                    .size(72.dp)
                    .clip(RoundedCornerShape(8.dp))
                    .background(SurfaceVariant),
            )
        } else {
            Box(
                modifier = Modifier
                    .size(72.dp)
                    .clip(RoundedCornerShape(8.dp))
                    .background(SurfaceVariant),
            )
        }
    }
}
