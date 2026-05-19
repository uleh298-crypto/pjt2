package com.d102.wye.presentation.home.alerts

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.d102.wye.domain.model.Alert
import com.d102.wye.presentation.designsystem.WyeBadge
import com.d102.wye.presentation.designsystem.WyeBadgeStyle
import com.d102.wye.presentation.designsystem.WyeTabs
import com.d102.wye.presentation.designsystem.WyeTopBar
import com.d102.wye.presentation.model.UiState
import com.d102.wye.presentation.theme.*
import java.time.LocalDate
import java.time.format.DateTimeFormatter

// ── 카테고리 표시 속성 ───────────────────────────────────────────

private data class CategoryStyle(
    val label: String,
    val color: Color,
    val textColor: Color,
)

private fun String.toCategoryStyle() = when (this) {
    "ETF"       -> CategoryStyle("ETF",     Color(0xFFDCEDD8), Color(0xFF2E6B3E))
    "PORTFOLIO" -> CategoryStyle("포트폴리오", Color(0xFFFFF3E0), Color(0xFF8A5B00))
    "NEWS"      -> CategoryStyle("뉴스",     Color(0xFFE3F0FF), Color(0xFF1A56A0))
    "SYSTEM"    -> CategoryStyle("공지",     Color(0xFFF1F5F9), Color(0xFF475569))
    else        -> CategoryStyle(this,      Color(0xFFF1F5F9), Color(0xFF475569))
}

// ── 날짜 그룹 추출 ───────────────────────────────────────────────

private fun String.toDateGroup(): String {
    return try {
        val date = LocalDate.parse(substring(0, 10), DateTimeFormatter.ISO_LOCAL_DATE)
        val today = LocalDate.now()
        when {
            date == today -> "오늘"
            date == today.minusDays(1) -> "어제"
            else -> "${date.monthValue}월 ${date.dayOfMonth}일"
        }
    } catch (e: Exception) {
        substring(0, 10)
    }
}

// ── 화면 ────────────────────────────────────────────────────────

@Composable
fun AlertsScreen(
    onBack: () -> Unit,
    onEtfClick: (String) -> Unit,
    onNewsClick: (Long) -> Unit,
    onPortfolioClick: (Long) -> Unit,
    viewModel: AlertsViewModel = hiltViewModel(),
) {
    val tabs = listOf("전체", "미확인")
    var selectedTab by remember { mutableIntStateOf(0) }
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()

    Scaffold(
        containerColor = Background,
        topBar = { WyeTopBar(title = "알림 목록", onBackClick = onBack) },
    ) { innerPadding ->
        Column(modifier = Modifier.padding(top = innerPadding.calculateTopPadding())) {
            WyeTabs(
                titles = tabs,
                selectedIndex = selectedTab,
                onTabSelected = { selectedTab = it },
                containerColor = Background,
            )

            when (val state = uiState) {
                is UiState.Loading -> Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center,
                ) {
                    CircularProgressIndicator()
                }

                is UiState.Success -> {
                    val displayItems = if (selectedTab == 0) state.data
                    else state.data.filter { !it.isRead }
                    val grouped = displayItems.groupBy { it.createdAt.toDateGroup() }

                    if (displayItems.isEmpty()) {
                        Box(
                            modifier = Modifier.fillMaxSize(),
                            contentAlignment = Alignment.Center,
                        ) {
                            Text(
                                text = "알림이 없습니다.",
                                style = MaterialTheme.typography.bodyMedium,
                                color = TextSecondary,
                            )
                        }
                    } else {
                        LazyColumn(
                            modifier = Modifier.fillMaxSize(),
                            contentPadding = PaddingValues(bottom = 20.dp),
                        ) {
                            grouped.forEach { (dateGroup, groupItems) ->
                                item(key = "header_$dateGroup") {
                                    Text(
                                        text = dateGroup,
                                        style = MaterialTheme.typography.bodySmall,
                                        color = TextSecondary,
                                        modifier = Modifier.padding(horizontal = 20.dp, vertical = 12.dp),
                                    )
                                }
                                items(groupItems, key = { it.id }) { alert ->
                                    AlertItemRow(
                                        alert = alert,
                                        onTap = {
                                            viewModel.markAsRead(alert.id)
                                            when (alert.referenceType) {
                                                "ETF" -> alert.referenceTicker?.let { onEtfClick(it) }
                                                "PORTFOLIO" -> alert.referenceId?.let { onPortfolioClick(it) }
                                                "NEWS" -> alert.referenceId?.let { onNewsClick(it) }
                                                else -> Unit
                                            }
                                        },
                                    )
                                    HorizontalDivider(color = Divider)
                                }
                            }
                        }
                    }
                }

                is UiState.Error -> Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center,
                ) {
                    Text(
                        text = state.message,
                        style = MaterialTheme.typography.bodyMedium,
                        color = TextSecondary,
                    )
                }

                UiState.Idle -> Unit
            }
        }
    }
}

// ── 아이템 ──────────────────────────────────────────────────────

@Composable
private fun AlertItemRow(alert: Alert, onTap: () -> Unit) {
    val style = alert.category.toCategoryStyle()
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .background(if (alert.isRead) SurfaceCard else Background)
            .clickable(onClick = onTap)
            .padding(horizontal = 20.dp, vertical = 16.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Column(modifier = Modifier.weight(1f)) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp),
            ) {
                WyeBadge(
                    label = style.label,
                    color = style.color,
                    textColor = style.textColor,
                    style = WyeBadgeStyle.FILLED,
                )
                Text(
                    text = alert.createdAt.substring(11, 16),
                    style = MaterialTheme.typography.bodySmall,
                    color = TextSecondary,
                )
            }
            Spacer(Modifier.height(6.dp))
            Text(
                text = alert.title,
                style = MaterialTheme.typography.bodyMedium.copy(
                    fontSize = 15.sp,
                    fontWeight = FontWeight.SemiBold,
                    lineHeight = 22.sp,
                ),
                color = TextPrimary,
            )
            Spacer(Modifier.height(2.dp))
            Text(
                text = alert.message,
                style = MaterialTheme.typography.bodySmall.copy(lineHeight = 18.sp),
                color = TextSecondary,
            )
        }
        if (!alert.isRead) {
            Spacer(Modifier.width(12.dp))
            Box(
                modifier = Modifier
                    .size(8.dp)
                    .background(PrimaryGreen, shape = androidx.compose.foundation.shape.CircleShape),
            )
        }
    }
}
