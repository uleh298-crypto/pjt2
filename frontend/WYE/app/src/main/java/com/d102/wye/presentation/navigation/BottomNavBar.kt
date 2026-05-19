package com.d102.wye.presentation.navigation

import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.size
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationBarItemDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.d102.wye.presentation.theme.NavActive
import com.d102.wye.presentation.theme.NavInactive
import com.d102.wye.presentation.theme.NavInactiveLabel
import com.d102.wye.presentation.theme.SurfaceWhite

/**
 * What's Your ETF 하단 내비게이션 바
 *
 * 아이콘 selected/unselected 구분 없이 단일 아이콘 사용
 * 선택 여부는 NavActive/NavInactive 색상으로만 구분
 *
 * @param selectedTab    현재 선택된 탭
 * @param onTabSelected  탭 클릭 콜백
 */
@Composable
fun BottomNavBar(
    selectedTab: BottomNavTab = BottomNavTab.HOME,
    onTabSelected: (BottomNavTab) -> Unit = {}
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
                        painter = painterResource(tab.iconRes),
                        contentDescription = tab.label,
                        modifier = Modifier.size(20.dp)
                    )
                },
                label = {
                    Text(
                        text = tab.label,
                        style = MaterialTheme.typography.labelSmall
                    )
                },
                colors = NavigationBarItemDefaults.colors(
                    selectedIconColor = NavActive,
                    selectedTextColor = NavActive,
                    unselectedIconColor = NavInactive,
                    unselectedTextColor = NavInactiveLabel,
                    indicatorColor = SurfaceWhite
                )
            )
        }
    }
}
