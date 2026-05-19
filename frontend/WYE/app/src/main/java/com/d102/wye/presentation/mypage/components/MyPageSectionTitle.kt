package com.d102.wye.presentation.mypage.components

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.d102.wye.presentation.theme.Divider
import com.d102.wye.presentation.theme.TextSecondary

@Composable
fun MyPageSectionTitle(title: String) {
    Column {
        HorizontalDivider(
            color = Divider,
            thickness = 1.dp,
            modifier = Modifier.padding(16.dp)
        )
        Text(
            text = title,
            style = MaterialTheme.typography.labelSmall,
            color = TextSecondary,
            modifier = Modifier.padding(horizontal = 16.dp)
        )
    }
}
