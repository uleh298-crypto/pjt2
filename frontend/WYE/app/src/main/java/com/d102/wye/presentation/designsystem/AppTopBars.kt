package com.d102.wye.presentation.designsystem

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material.icons.outlined.BookmarkBorder
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.sp
import com.d102.wye.presentation.theme.Background
import com.d102.wye.presentation.theme.PrimaryGreen
import com.d102.wye.presentation.theme.TextPrimary

// ────────────────────────────────────────────────────────────────
//  공통 기반 TopBar (내부 사용)
// ────────────────────────────────────────────────────────────────

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun BaseTopBar(
    title: String,
    modifier: Modifier = Modifier,
    isBrandTitle: Boolean = false,
    showBackButton: Boolean = false,
    onBackClick: () -> Unit = {},
    actions: @Composable () -> Unit = {},
) {
    TopAppBar(
        modifier = modifier,
        colors = TopAppBarDefaults.topAppBarColors(
            containerColor = Background,
            navigationIconContentColor = PrimaryGreen,
            actionIconContentColor = PrimaryGreen,
        ),
        navigationIcon = {
            if (showBackButton) {
                IconButton(onClick = onBackClick) {
                    Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "뒤로가기")
                }
            }
        },
        title = {
            Text(
                text = title,
                color = if (isBrandTitle) PrimaryGreen else TextPrimary,
                fontSize = if (isBrandTitle) 20.sp else 18.sp,
                fontWeight = if (isBrandTitle) FontWeight.Bold else FontWeight.SemiBold,
                letterSpacing = if (isBrandTitle) (-0.5).sp else 0.sp,
            )
        },
        actions = { actions() },
    )
}

// ────────────────────────────────────────────────────────────────
//  화면별 TopBar
// ────────────────────────────────────────────────────────────────

/** 홈 화면 — 브랜드 타이틀 + 북마크 + 알림 */
@Composable
fun HomeTopBar(
    onBookmarkClick: () -> Unit = {},
    onAlertClick: () -> Unit = {},
    modifier: Modifier = Modifier,
) {
    BaseTopBar(
        title = "What's Your ETF",
        isBrandTitle = true,
        modifier = modifier,
        actions = {
            IconButton(onClick = onBookmarkClick) {
                Icon(Icons.Outlined.BookmarkBorder, contentDescription = "북마크")
            }
            IconButton(onClick = onAlertClick) {
                Icon(Icons.Filled.Notifications, contentDescription = "알림")
            }
        },
    )
}

/** 탐색 화면 — "탐색" 타이틀 */
@Composable
fun ExploreTopBar(
    modifier: Modifier = Modifier,
) {
    BaseTopBar(title = "탐색", modifier = modifier)
}

/** 시뮬레이션 화면 — "시뮬레이션" 타이틀 */
@Composable
fun SimulationTopBar(
    modifier: Modifier = Modifier,
) {
    BaseTopBar(title = "시뮬레이션", modifier = modifier)
}

/** 나의 전략 화면 — "나의 전략" 타이틀 */
@Composable
fun StrategyTopBar(
    modifier: Modifier = Modifier,
) {
    BaseTopBar(title = "나의 전략", modifier = modifier)
}

/** 마이페이지 화면 — "마이페이지" 타이틀 */
@Composable
fun MyPageTopBar(
    modifier: Modifier = Modifier,
) {
    BaseTopBar(title = "마이페이지", modifier = modifier)
}

/** 상세/서브 화면 — 뒤로가기 + 커스텀 타이틀 */
@Composable
fun DetailTopBar(
    title: String,
    onBackClick: () -> Unit,
    modifier: Modifier = Modifier,
    actions: @Composable () -> Unit = {},
) {
    BaseTopBar(
        title = title,
        showBackButton = true,
        onBackClick = onBackClick,
        modifier = modifier,
        actions = actions,
    )
}

// ────────────────────────────────────────────────────────────────
//  Preview
// ────────────────────────────────────────────────────────────────

@Preview(showBackground = true, backgroundColor = 0xFFF5F0E8, name = "홈")
@Composable private fun HomeTopBarPreview() { HomeTopBar() }

@Preview(showBackground = true, backgroundColor = 0xFFF5F0E8, name = "탐색")
@Composable private fun ExploreTopBarPreview() { ExploreTopBar() }

@Preview(showBackground = true, backgroundColor = 0xFFF5F0E8, name = "시뮬레이션")
@Composable private fun SimulationTopBarPreview() { SimulationTopBar() }

@Preview(showBackground = true, backgroundColor = 0xFFF5F0E8, name = "나의 전략")
@Composable private fun StrategyTopBarPreview() { StrategyTopBar() }

@Preview(showBackground = true, backgroundColor = 0xFFF5F0E8, name = "마이페이지")
@Composable private fun MyPageTopBarPreview() { MyPageTopBar() }

@Preview(showBackground = true, backgroundColor = 0xFFF5F0E8, name = "상세")
@Composable private fun DetailTopBarPreview() { DetailTopBar(title = "ETF 상세", onBackClick = {}) }
