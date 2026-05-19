package com.d102.wye.presentation.designsystem

import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Tab
import androidx.compose.material3.TabRow
import androidx.compose.material3.TabRowDefaults
import androidx.compose.material3.TabRowDefaults.tabIndicatorOffset
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.sp
import com.d102.wye.presentation.theme.PrimaryGreen
import com.d102.wye.presentation.theme.TextSecondary

@Composable
fun WyeTabs(
    titles: List<String>,
    selectedIndex: Int,
    onTabSelected: (Int) -> Unit,
    modifier: Modifier = Modifier,
    containerColor: Color = Color.White,
    selectedColor: Color = PrimaryGreen,
    unselectedColor: Color = TextSecondary,
    textStyle: TextStyle = MaterialTheme.typography.titleSmall.copy(fontSize = 15.sp)
) {
    TabRow(
        selectedTabIndex = selectedIndex,
        modifier = modifier.fillMaxWidth(),
        containerColor = containerColor,
        contentColor = selectedColor,
        indicator = { tabPositions ->
            TabRowDefaults.SecondaryIndicator(
                modifier = Modifier.tabIndicatorOffset(tabPositions[selectedIndex]),
                color = selectedColor
            )
        }
    ) {
        titles.forEachIndexed { index, title ->
            Tab(
                selected = selectedIndex == index,
                onClick = { onTabSelected(index) },
                text = {
                    Text(
                        text = title,
                        style = textStyle,
                        color = if (selectedIndex == index) selectedColor else unselectedColor
                    )
                }
            )
        }
    }
}

@Preview(showBackground = true, backgroundColor = 0xFFFFFFFF)
@Composable
private fun WyeTabsPreview() {
    WyeTabs(
        titles = listOf("수익률 추이", "포트폴리오 분석"),
        selectedIndex = 0,
        onTabSelected = {}
    )
}
