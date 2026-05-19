package com.d102.wye.presentation.strategy.list.components

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.d102.wye.R
import com.d102.wye.presentation.designsystem.WyePrimaryButton
import com.d102.wye.presentation.theme.PrimaryGreen
import com.d102.wye.presentation.theme.SurfaceVariant
import com.d102.wye.presentation.theme.TextPrimary
import com.d102.wye.presentation.theme.TextSecondary


// ─────────────────────────────────────────
// 처음 온 유저를 위한 화면
// ─────────────────────────────────────────
@Composable
fun StrategyEmptyView(
    onCreateClick: () -> Unit,
    onConnectMyDataClick: () -> Unit = {}
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 24.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        Text(
            text = "What's your ETF를\n100% 활용해 보세요!",
            style = MaterialTheme.typography.titleLarge,
            textAlign = TextAlign.Center,
            color = TextPrimary
        )

        Spacer(modifier = Modifier.height(24.dp))

        // 1. 내 실제 자산 연결 카드
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .border(BorderStroke(1.dp, SurfaceVariant), RoundedCornerShape(16.dp))
                .padding(20.dp),
        ) {
            Row(
                verticalAlignment = Alignment.Top
            ) {
                Box(
                    modifier = Modifier
                        .size(48.dp)
                        .background(PrimaryGreen.copy(alpha = 0.1f), RoundedCornerShape(12.dp)),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        painter = painterResource(id = R.drawable.ic_nav_strategy),
                        contentDescription = null,
                        tint = PrimaryGreen
                    )
                }

                Column(
                    modifier = Modifier.padding(start = 16.dp)
                ) {
                    Text(
                        text = "내 실제 자산 연결하기",
                        style = MaterialTheme.typography.bodyLarge.copy(fontWeight = FontWeight.SemiBold),
                        color = TextPrimary
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = "흩어져 있는 내 ETF 자산을 한눈에 모아보고\n분석받으세요.",
                        style = MaterialTheme.typography.bodyMedium,
                        color = TextSecondary,
                    )
                    Spacer(modifier = Modifier.height(48.dp))
                }
            }
            // 자산 연결 버튼 (라인 버튼)
            Surface(
                modifier = Modifier.align(Alignment.BottomEnd),
                shape = RoundedCornerShape(20.dp),
                border = BorderStroke(1.dp, SurfaceVariant),
                color = Color.White,
                onClick = { onConnectMyDataClick() }
            ) {
                Text(
                    text = "\uD83D\uDD17 자산 연결하러 가기",
                    modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp),
                    style = MaterialTheme.typography.labelMedium,
                    color = TextSecondary
                )
            }
        }

        // OR 구분선
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 24.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            HorizontalDivider(
                modifier = Modifier.weight(1f),
                color = SurfaceVariant
            )
            Text(
                text = "OR",
                modifier = Modifier.padding(horizontal = 16.dp),
                style = MaterialTheme.typography.labelMedium,
                color = TextSecondary
            )
            HorizontalDivider(
                modifier = Modifier.weight(1f),
                color = SurfaceVariant
            )
        }

        // 2. 상상 속 전략 첫 생성 카드
        Surface(
            shape = RoundedCornerShape(16.dp),
            color = Color.White,
            border = BorderStroke(1.dp, SurfaceVariant),
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(
                modifier = Modifier.padding(24.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Box(
                    modifier = Modifier
                        .size(100.dp)
                        .background(PrimaryGreen.copy(alpha = 0.1f), CircleShape),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        painter = painterResource(id = R.drawable.ic_plant),
                        contentDescription = null,
                        tint = PrimaryGreen
                    ) // 임시 식물 아이콘
                }
                Spacer(modifier = Modifier.height(16.dp))
                Text(
                    text = "상상 속의 전략, 현실이 될까요?",
                    style = MaterialTheme.typography.bodyLarge.copy(fontWeight = FontWeight.SemiBold),
                    color = TextPrimary
                )
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = "다양한 ETF를 조합해 필승 전략을 설계해 보세요.",
                    style = MaterialTheme.typography.bodyMedium,
                    color = TextSecondary,
                    textAlign = TextAlign.Center
                )
                Spacer(modifier = Modifier.height(24.dp))
                WyePrimaryButton(
                    text = "첫번째 전략 설계하기",
                    onClick = onCreateClick,
                    modifier = Modifier.fillMaxWidth()
                )
            }
        }
    }
}