package com.d102.wye.presentation.designsystem

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.d102.wye.presentation.theme.NavActive
import com.d102.wye.presentation.theme.NavInactive
import com.d102.wye.presentation.theme.NavInactiveLabel
import com.d102.wye.presentation.theme.SurfaceWhite

/** 하단 탭 목록 */
enum class BottomNavTab(
    val label: String,
    val selectedIcon: ImageVector,
    val unselectedIcon: ImageVector,
) {
    HOME(
        label = "홈",
        selectedIcon = Icons.Filled.Home,
        unselectedIcon = Icons.Outlined.Home,
    ),
    EXPLORE(
        label = "탐색",
        selectedIcon = Icons.Filled.TrendingUp,
        unselectedIcon = Icons.Outlined.TrendingUp,
    ),
    SIMULATION(
        label = "시뮬레이션",
        selectedIcon = Icons.Filled.PieChart,
        unselectedIcon = Icons.Outlined.PieChart,
    ),
    STRATEGY(
        label = "나의 전략",
        selectedIcon = Icons.Filled.LibraryBooks,
        unselectedIcon = Icons.Outlined.LibraryBooks,
    ),
    MYPAGE(
        label = "마이페이지",
        selectedIcon = Icons.Filled.Person,
        unselectedIcon = Icons.Outlined.Person,
    ),
}

/**
 * What's Your ETF 하단 내비게이션 바
 *
 * @param selectedTab  현재 선택된 탭
 * @param onTabSelected  탭 클릭 콜백
 */
@Composable
fun WyeBottomNavBar(
    selectedTab: BottomNavTab = BottomNavTab.HOME,
    onTabSelected: (BottomNavTab) -> Unit = {},
) {
    NavigationBar(
        containerColor = SurfaceWhite,
        tonalElevation = 0.dp,
    ) {
        BottomNavTab.entries.forEach { tab ->
            val selected = tab == selectedTab
            NavigationBarItem(
                selected = selected,
                onClick = { onTabSelected(tab) },
                icon = {
                    Icon(
                        imageVector = if (selected) tab.selectedIcon else tab.unselectedIcon,
                        contentDescription = tab.label,
                    )
                },
                label = {
                    Text(
                        text = tab.label,
                        fontSize = 10.sp,
                    )
                },
                colors = NavigationBarItemDefaults.colors(
                    selectedIconColor = NavActive,
                    selectedTextColor = NavActive,
                    unselectedIconColor = NavInactive,
                    unselectedTextColor = NavInactiveLabel,
                    indicatorColor = SurfaceWhite,
                ),
            )
        }
    }
}

@Preview(showBackground = true)
@Composable
private fun WyeBottomNavBarPreview() {
    WyeBottomNavBar(selectedTab = BottomNavTab.HOME)
}
