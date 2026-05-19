package com.d102.wye.presentation.mypage.support

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.d102.wye.presentation.designsystem.WyeTopBar
import com.d102.wye.presentation.theme.Background
import com.d102.wye.presentation.theme.PrimaryGreen
import com.d102.wye.presentation.theme.TextPrimary
import com.d102.wye.presentation.theme.TextSecondary

@Composable
fun TermsScreen(
    onBackClick: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Background)
    ) {
        WyeTopBar(
            title = "이용약관",
            onBackClick = onBackClick
        )

        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 20.dp, vertical = 16.dp),
            verticalArrangement = Arrangement.spacedBy(24.dp)
        ) {
            Text(
                text = "ETF 투자 분석 서비스\n이용약관",
                color = TextPrimary,
                style = MaterialTheme.typography.headlineSmall.copy(fontWeight = FontWeight.Bold)
            )

            Text(
                text = "시행일: 2024년 5월 20일",
                color = TextSecondary,
                style = MaterialTheme.typography.bodySmall.copy(fontWeight = FontWeight.Medium)
            )

            termsSections.forEach { section ->
                TermsSectionBlock(section = section)
            }
        }
    }
}

@Composable
private fun TermsSectionBlock(section: TermsSection) {
    Column(
        modifier = Modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        Text(
            text = section.title,
            color = PrimaryGreen,
            style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold)
        )

        Text(
            text = section.body,
            color = TextSecondary,
            style = MaterialTheme.typography.bodyMedium,
            lineHeight = MaterialTheme.typography.bodyMedium.lineHeight
        )
    }
}
