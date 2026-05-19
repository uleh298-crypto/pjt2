package com.d102.wye.presentation.mypage.alerts

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import android.content.Intent
import android.provider.Settings
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.app.NotificationManagerCompat
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.lifecycle.compose.LocalLifecycleOwner
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.d102.wye.presentation.designsystem.WyeTopBar
import com.d102.wye.presentation.model.UiState
import com.d102.wye.presentation.theme.Background
import com.d102.wye.presentation.theme.PrimaryGreen
import com.d102.wye.presentation.theme.SurfaceDivider
import com.d102.wye.presentation.theme.TextPrimary
import com.d102.wye.presentation.theme.TextSecondary

@Composable
fun AlertSettingsScreen(
    onBackClick: () -> Unit,
    viewModel: AlertSettingsViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val snackbarHostState = remember { SnackbarHostState() }
    val context = LocalContext.current
    val lifecycleOwner = LocalLifecycleOwner.current

    var notificationsEnabled by remember {
        mutableStateOf(NotificationManagerCompat.from(context).areNotificationsEnabled())
    }

    // ON_RESUME 시 알림 권한 변화 감지 → enable/disableAll 즉시 호출
    DisposableEffect(lifecycleOwner) {
        val observer = LifecycleEventObserver { _, event ->
            if (event == Lifecycle.Event.ON_RESUME) {
                val newEnabled = NotificationManagerCompat.from(context).areNotificationsEnabled()
                if (newEnabled != notificationsEnabled) {
                    notificationsEnabled = newEnabled
                    if (newEnabled) viewModel.enableAll() else viewModel.disableAll()
                }
            }
        }
        lifecycleOwner.lifecycle.addObserver(observer)
        onDispose { lifecycleOwner.lifecycle.removeObserver(observer) }
    }

    val openNotificationSettings = {
        val intent = Intent(Settings.ACTION_APP_NOTIFICATION_SETTINGS).apply {
            putExtra(Settings.EXTRA_APP_PACKAGE, context.packageName)
        }
        context.startActivity(intent)
    }

        // 초기 로드 시 권한 OFF이면 백엔드 동기화 (ON이면 서버 값 유지) / 에러 스낵바
    var initialSyncDone by remember { mutableStateOf(false) }
    LaunchedEffect(uiState) {
        if (!initialSyncDone && uiState is UiState.Success) {
            initialSyncDone = true
            if (!notificationsEnabled) viewModel.disableAll()
        }
        if (uiState is UiState.Error) {
            snackbarHostState.showSnackbar((uiState as UiState.Error).message)
        }
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Background)
    ) {
        Column(modifier = Modifier.fillMaxSize()) {
            WyeTopBar(
                title = "알림 설정",
                onBackClick = onBackClick
            )

            when (val state = uiState) {
                is UiState.Loading -> Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    CircularProgressIndicator()
                }

                is UiState.Success -> AlertSettingsContent(
                    state = state.data.copy(
                        appNoticeEnabled = notificationsEnabled,
                        etfListingEnabled = notificationsEnabled && state.data.etfListingEnabled,
                        etfDelistingEnabled = notificationsEnabled && state.data.etfDelistingEnabled,
                        portfolioRebalancingEnabled = notificationsEnabled && state.data.portfolioRebalancingEnabled,
                        portfolioProfitEnabled = notificationsEnabled && state.data.portfolioProfitEnabled,
                        newsEnabled = notificationsEnabled && state.data.newsEnabled,
                    ),
                    onAppNoticeChanged = { openNotificationSettings() },
                    onEtfListingChanged = { if (!notificationsEnabled) openNotificationSettings() else viewModel.onEtfListingChanged(it) },
                    onEtfDelistingChanged = { if (!notificationsEnabled) openNotificationSettings() else viewModel.onEtfDelistingChanged(it) },
                    onPortfolioRebalancingChanged = { if (!notificationsEnabled) openNotificationSettings() else viewModel.onPortfolioRebalancingChanged(it) },
                    onPortfolioProfitChanged = { if (!notificationsEnabled) openNotificationSettings() else viewModel.onPortfolioProfitChanged(it) },
                    onNewsChanged = { if (!notificationsEnabled) openNotificationSettings() else viewModel.onNewsChanged(it) },
                )

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

@Composable
private fun AlertSettingsContent(
    state: AlertSettingsUiState,
    onAppNoticeChanged: (Boolean) -> Unit,
    onEtfListingChanged: (Boolean) -> Unit,
    onEtfDelistingChanged: (Boolean) -> Unit,
    onPortfolioRebalancingChanged: (Boolean) -> Unit,
    onPortfolioProfitChanged: (Boolean) -> Unit,
    onNewsChanged: (Boolean) -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 20.dp, vertical = 24.dp),
        verticalArrangement = Arrangement.spacedBy(28.dp)
    ) {
        AlertGroup(
            title = "앱",
            items = listOf(
                AlertToggleItem(
                    title = "앱 알림",
                    description = "중요한 공지사항과 소식을 놓치지 마세요.",
                    checked = state.appNoticeEnabled,
                    onCheckedChange = onAppNoticeChanged
                )
            )
        )

        AlertGroup(
            title = "ETF",
            items = listOf(
                AlertToggleItem(
                    title = "ETF 상장 알림",
                    description = "관심 있는 ETF의 신규 상장 소식을 받아보세요.",
                    checked = state.etfListingEnabled,
                    onCheckedChange = onEtfListingChanged
                ),
                AlertToggleItem(
                    title = "ETF 상장 폐지 알림",
                    description = "관심 있는 ETF의 상장 폐지 소식을 받아보세요.",
                    checked = state.etfDelistingEnabled,
                    onCheckedChange = onEtfDelistingChanged
                )
            )
        )

        AlertGroup(
            title = "포트폴리오",
            items = listOf(
                AlertToggleItem(
                    title = "포트폴리오 리밸런싱 알림",
                    description = "포트폴리오 리밸런싱 될 때 알려드려요.",
                    checked = state.portfolioRebalancingEnabled,
                    onCheckedChange = onPortfolioRebalancingChanged
                ),
                AlertToggleItem(
                    title = "포트폴리오 수익률 알림",
                    description = "수익률에 큰 변화가 생기면 즉시 알려드려요.",
                    checked = state.portfolioProfitEnabled,
                    onCheckedChange = onPortfolioProfitChanged
                )
            )
        )

        AlertGroup(
            title = "뉴스",
            items = listOf(
                AlertToggleItem(
                    title = "뉴스 수신 알림",
                    description = "관심있는 ETF와 관련된 뉴스를 받아보세요.",
                    checked = state.newsEnabled,
                    onCheckedChange = onNewsChanged
                )
            )
        )
    }
}

@Composable
private fun AlertGroup(
    title: String,
    items: List<AlertToggleItem>
) {
    Column(verticalArrangement = Arrangement.spacedBy(18.dp)) {
        Text(
            text = title,
            color = TextSecondary.copy(alpha = 0.55f),
            style = MaterialTheme.typography.labelSmall
        )

        items.forEach { item ->
            AlertToggleRow(item = item)
        }
    }
}

@Composable
private fun AlertToggleRow(item: AlertToggleItem) {
    androidx.compose.foundation.layout.Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Column(
            modifier = Modifier
                .weight(1f)
                .padding(end = 16.dp),
            verticalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            Text(
                text = item.title,
                color = TextPrimary,
                style = MaterialTheme.typography.bodyMedium.copy(
                    fontWeight = FontWeight.Medium,
                    fontSize = 15.sp
                )
            )
            Text(
                text = item.description,
                color = TextSecondary,
                style = MaterialTheme.typography.bodySmall.copy(fontSize = 12.sp)
            )
        }

        Switch(
            checked = item.checked,
            onCheckedChange = item.onCheckedChange,
            modifier = Modifier.scale(0.88f),
            colors = SwitchDefaults.colors(
                checkedThumbColor = Color.White,
                checkedTrackColor = PrimaryGreen,
                checkedBorderColor = PrimaryGreen,
                uncheckedThumbColor = Color.White,
                uncheckedTrackColor = SurfaceDivider,
                uncheckedBorderColor = SurfaceDivider
            )
        )
    }
}

private data class AlertToggleItem(
    val title: String,
    val description: String,
    val checked: Boolean,
    val onCheckedChange: (Boolean) -> Unit
)
