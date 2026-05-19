package com.d102.wye.presentation.explore.list

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.runtime.snapshotFlow
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.d102.wye.presentation.designsystem.EtfListItem
import com.d102.wye.presentation.designsystem.WyeEmptyState
import com.d102.wye.presentation.designsystem.WyeTopBar
import com.d102.wye.presentation.explore.FilterDialog
import com.d102.wye.presentation.explore.list.components.MultiSelectionBottomBar
import com.d102.wye.presentation.explore.list.components.QuickFilterRow
import com.d102.wye.presentation.explore.list.components.SearchRow
import com.d102.wye.presentation.explore.list.components.SortRow
import com.d102.wye.presentation.explore.list.components.activeFilterCount
import com.d102.wye.presentation.model.UiState
import kotlinx.coroutines.launch

@Composable
fun ExploreScreen(
    title: String = "탐색",
    isSelectionMode: Boolean = false,
    initialSelectedTickers: List<SelectedEtf> = emptyList(),
    onEtfClick: (ticker: String, riskLevel: Int) -> Unit,
    onBackClick: (() -> Unit)? = null,
    onSelectionComplete: ((List<String>) -> Unit)? = null,
    viewModel: ExploreViewModel = hiltViewModel(),
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val filterState by viewModel.filterState.collectAsStateWithLifecycle()
    val sortedBy by viewModel.sortedBy.collectAsStateWithLifecycle()
    val isLoadingMore by viewModel.isLoadingMore.collectAsStateWithLifecycle()
    val snackbarHostState = remember { SnackbarHostState() }
    var showFilterDialog by remember { mutableStateOf(false) }
    val expandedFilterSections by viewModel.expandedFilterSections.collectAsStateWithLifecycle()
    val selectedTickers by viewModel.selectedTickers.collectAsStateWithLifecycle()
    val marketStatusLabel by viewModel.marketStatusLabel.collectAsStateWithLifecycle()
    val coroutineScope = rememberCoroutineScope()
    val listState = rememberLazyListState()

    LaunchedEffect(Unit) {
        if (isSelectionMode && initialSelectedTickers.isNotEmpty()) {
            viewModel.setInitialSelections(initialSelectedTickers)
        }
    }

    // 즐겨찾기 토글 시 맨 위로 스크롤
    LaunchedEffect(filterState.onlyLiked) {
        listState.scrollToItem(0)
    }

    // 스크롤 끝 감지 → 다음 페이지 로드
    LaunchedEffect(listState) {
        snapshotFlow {
            val lastVisible = listState.layoutInfo.visibleItemsInfo.lastOrNull()?.index ?: 0
            val total = listState.layoutInfo.totalItemsCount
            lastVisible >= total - 3
        }.collect { nearEnd ->
            if (nearEnd) viewModel.loadMore()
        }
    }

    LaunchedEffect(uiState) {
        if (uiState is UiState.Error) {
            snackbarHostState.showSnackbar((uiState as UiState.Error).message)
        }
    }

    if (showFilterDialog) {
        val resultCount = activeFilterCount(filterState)
        FilterDialog(
            filter = filterState,
            resultCount = resultCount,
            onFilterChanged = viewModel::onFilterChanged,
            onApply = { showFilterDialog = false },
            onDismiss = { showFilterDialog = false },
            expandedSections = expandedFilterSections,
            onToggleSection = viewModel::toggleFilterSection,
        )
    }

    Scaffold(
        containerColor = MaterialTheme.colorScheme.background,
        snackbarHost = { SnackbarHost(snackbarHostState) },
        topBar = {
            WyeTopBar(
                title = title,
                onBackClick = if (isSelectionMode) onBackClick else null
            )
        },
        bottomBar = {
            if (isSelectionMode) {
                MultiSelectionBottomBar(
                    selectedItems = selectedTickers.toList(),
                    onRemove = { ticker -> viewModel.removeSelection(ticker) },
                    onComplete = {
                        val tickerList = selectedTickers.map { it.ticker }
                        onSelectionComplete?.let { it(tickerList) }
                        viewModel.clearSelection()
                    }
                )
            }
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(top = innerPadding.calculateTopPadding()),
        ) {
            SearchRow(
                query = filterState.query,
                searchScope = filterState.searchScope,
                onQueryChanged = viewModel::onQueryChanged,
                onSearchScopeSelected = viewModel::onSearchScopeSelected,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 8.dp),
            )
            QuickFilterRow(
                filterState = filterState,
                onFilterIconClick = { showFilterDialog = true },
                onFilterChanged = viewModel::onFilterChanged,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp)
                    .padding(bottom = 4.dp),
            )
            SortRow(
                selectedSort = sortedBy,
                onSortChanged = viewModel::onSortChanged,
                marketStatusLabel = marketStatusLabel,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 4.dp),
            )

            when (val state = uiState) {
                is UiState.Loading -> Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center,
                ) { CircularProgressIndicator() }

                is UiState.Success -> if (state.data.filteredList.isEmpty()) {
                    WyeEmptyState(
                        message = if (filterState.onlyLiked) "관심 종목이 없어요" else "검색 결과가 없어요",
                        description = if (filterState.onlyLiked) "즐겨찾기한 ETF가 여기에 표시돼요" else "다른 키워드나 필터를 사용해보세요",
                        modifier = Modifier.fillMaxSize(),
                    )
                } else LazyColumn(
                    state = listState,
                    modifier = Modifier.fillMaxSize(),
                ) {
                    items(state.data.filteredList, key = { it.ticker }) { etf ->
                        val isSelected = selectedTickers.any { it.ticker == etf.ticker }
                        EtfListItem(
                            name = etf.name,
                            ticker = etf.ticker,
                            currentPrice = etf.currentPrice,
                            changeRate = etf.changeRate,
                            changeAmount = etf.changeAmount,
                            riskType = etf.riskType,
                            isLiked = etf.isLiked,
                            onLikeToggled = { viewModel.onLikeToggled(etf.ticker) },
                            isSelectionMode = isSelectionMode,
                            isSelected = isSelected,
                            onToggleSelection = {
                                if (!isSelected && selectedTickers.size >= 10) {
                                    coroutineScope.launch {
                                        snackbarHostState.showSnackbar("최대 10개까지만 선택할 수 있습니다.")
                                    }
                                } else {
                                    viewModel.toggleSelection(etf.ticker, etf.name)
                                }
                            },
                            onClick = { onEtfClick(etf.ticker, 0) },
                        )
                    }

                    if (isLoadingMore) {
                        item {
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(16.dp),
                                contentAlignment = Alignment.Center,
                            ) { CircularProgressIndicator() }
                        }
                    }
                }

                is UiState.Error -> Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center,
                ) {
                    Button(onClick = { viewModel.loadEtfList() }) {
                        Text("다시 시도")
                    }
                }
                UiState.Idle -> Unit
            }
        }
    }
}
