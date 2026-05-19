package com.d102.wye.presentation.home

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.Image
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material3.CenterAlignedTopAppBar
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.d102.wye.R
import com.d102.wye.presentation.home.components.HomeTop10Tab
import com.d102.wye.presentation.model.UiState
import com.d102.wye.presentation.theme.PrimaryGreen

@Composable
fun HomeScreen(
    onNewsClick: (newsId: Long) -> Unit,
    onEtfClick: (ticker: String) -> Unit,
    onNewsMoreClick: () -> Unit = {},
    onBookmarkClick: () -> Unit = {},
    onAlertClick: () -> Unit = {},
    viewModel: HomeViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val snackbarHostState = remember { SnackbarHostState() }

    LaunchedEffect(uiState) {
        if (uiState is UiState.Error) {
            snackbarHostState.showSnackbar(
                message = (uiState as UiState.Error).message
            )
        }
    }

    HomeScreenContent(
        uiState = uiState,
        snackbarHostState = snackbarHostState,
        onNewsClick = onNewsClick,
        onEtfClick = onEtfClick,
        onNewsMoreClick = onNewsMoreClick,
        onRefreshTop10Click = viewModel::refreshTop10Etfs,
        onBookmarkClick = onBookmarkClick,
        onAlertClick = onAlertClick
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun HomeScreenContent(
    uiState: UiState<HomeData>,
    snackbarHostState: SnackbarHostState,
    onNewsClick: (newsId: Long) -> Unit,
    onEtfClick: (ticker: String) -> Unit,
    onNewsMoreClick: () -> Unit,
    onRefreshTop10Click: () -> Unit,
    onBookmarkClick: () -> Unit,
    onAlertClick: () -> Unit
) {
    Box(
        modifier = Modifier
            .fillMaxSize()
        .background(MaterialTheme.colorScheme.background)
    ) {
        Column(modifier = Modifier.fillMaxSize()) {
            CenterAlignedTopAppBar(
                title = {
                    Box(
                        modifier = Modifier.fillMaxWidth(),
                        contentAlignment = Alignment.CenterStart
                    ) {
                        Image(
                            painter = painterResource(R.drawable.ic_logo),
                            contentDescription = "로고",
                            modifier = Modifier
                                .padding(start = 8.dp)
                                .height(48.dp)
                        )
                    }
                },
                actions = {
                    IconButton(onClick = onBookmarkClick) {
                        Icon(
                            painter = painterResource(R.drawable.ic_star),
                            contentDescription = "북마크",
                            tint = PrimaryGreen,
                            modifier = Modifier.size(30.dp)
                        )
                    }
                    IconButton(onClick = onAlertClick) {
                        Icon(
                            imageVector = Icons.Filled.Notifications,
                            contentDescription = "알림",
                            tint = PrimaryGreen
                        )
                    }
                },
                colors = TopAppBarDefaults.centerAlignedTopAppBarColors(
                    containerColor = MaterialTheme.colorScheme.background
                )
            )

            when (uiState) {
                is UiState.Loading -> {
                    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                        CircularProgressIndicator()
                    }
                }

                is UiState.Success -> {
                    HomeTop10Tab(
                        top10Etfs = uiState.data.top10Etfs,
                        top10UpdatedText = uiState.data.top10UpdatedText,
                        isRefreshing = uiState.data.isTop10Refreshing,
                        newsList = uiState.data.newsList,
                        onRefreshClick = onRefreshTop10Click,
                        onEtfClick = onEtfClick,
                        onNewsClick = onNewsClick,
                        onNewsMoreClick = onNewsMoreClick
                    )
                }

                is UiState.Error -> Unit

                UiState.Idle -> Unit
            }
        }

        SnackbarHost(
            hostState = snackbarHostState,
            modifier = Modifier.align(Alignment.BottomCenter)
        )
    }
}
