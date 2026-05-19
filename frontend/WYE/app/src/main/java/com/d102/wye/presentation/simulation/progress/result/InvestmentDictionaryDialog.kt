package com.d102.wye.presentation.simulation.progress.result

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import com.d102.wye.R
import com.d102.wye.presentation.designsystem.WyePrimaryButton
import com.d102.wye.presentation.theme.PrimaryGreen
import com.d102.wye.presentation.theme.SurfaceCard
import com.d102.wye.presentation.theme.TextPrimary
import com.d102.wye.presentation.theme.TextSecondary

@Composable
fun InvestmentDictionaryDialog(
    onDismiss: () -> Unit
) {
    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(usePlatformDefaultWidth = false)
    ) {
        Surface(
            modifier = Modifier
                .fillMaxWidth(0.9f)
                .padding(vertical = 32.dp),
            shape = RoundedCornerShape(24.dp),
            color = Color.White
        ) {
            Column(
                modifier = Modifier.padding(28.dp)
            ) {
                // 1. 헤더 영역 (타이틀 & 닫기 버튼)
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "WHAT'S YOUR ETF",
                        style = MaterialTheme.typography.labelMedium,
                        color = PrimaryGreen
                    )
                    IconButton(
                        onClick = onDismiss,
                        modifier = Modifier.size(24.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Close,
                            contentDescription = "닫기",
                            tint = TextSecondary
                        )
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                Text(
                    text = "투자 용어 사전",
                    style = MaterialTheme.typography.titleMedium,
                    color = TextPrimary
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = "초보 투자자를 위한 필수 금융 용어 정리",
                    style = MaterialTheme.typography.bodyMedium,
                    color = TextSecondary
                )

                Spacer(modifier = Modifier.height(32.dp))

                // 2. 용어 리스트
                Column(verticalArrangement = Arrangement.spacedBy(28.dp)) {
                    DictionaryItem(
                        iconRes = R.drawable.ic_per,
                        title = "PER (주가수익비율)",
                        description = "기업이 벌어들이는 이익 대비 주가의 비율을 나타냅니다. 보통 이 수치가 낮을수록 저평가된 것으로 봅니다."
                    )
                    DictionaryItem(
                        iconRes = R.drawable.ic_pbr,
                        title = "PBR (주가순자산비율)",
                        description = "주식의 시장가격과 재무제표 상 주당 가치를 비교하기 위해 사용하는 비율입니다. 1보다 낮으면 재무제표 상 주당 가치 대비 시장 가격이 낮다는 의미입니다."
                    )
                    DictionaryItem(
                        iconRes = R.drawable.ic_roe,
                        title = "ROE (자기자본이익률)",
                        description = "내가 투자한 돈으로 회사가 얼마나 효율적으로 돈을 벌었는지 보여줍니다. 높을수록 장사를 잘 하는 회사입니다."
                    )
                }

                Spacer(modifier = Modifier.height(24.dp))

                // 3. 하단 안내 박스
                Surface(
                    shape = RoundedCornerShape(12.dp),
                    color = SurfaceCard,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Row(
                        modifier = Modifier.padding(16.dp),
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        verticalAlignment = Alignment.Top
                    ) {
                        Icon(
                            painter = painterResource(id = R.drawable.ic_info),
                            contentDescription = null,
                            tint = PrimaryGreen,
                            modifier = Modifier.size(12.dp).padding(top = 2.dp)
                        )
                        Text(
                            text = "위 지표들은 절대적인 기준이 아니며, 업종별 평균 수치와 함께 비교하는 것이 중요합니다.",
                            style = MaterialTheme.typography.bodySmall,
                            color = TextSecondary,
                        )
                    }
                }

                Spacer(modifier = Modifier.height(24.dp))

                // 4. 확인 버튼
                WyePrimaryButton(
                    text = "확인했습니다",
                    modifier = Modifier.fillMaxWidth(),
                    onClick = onDismiss
                )
            }
        }
    }
}

@Composable
private fun DictionaryItem(
    iconRes: Int,
    title: String,
    description: String
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // 아이콘 배경 박스
        Box(
            modifier = Modifier
                .size(50.dp)
                .background(PrimaryGreen.copy(alpha = 0.1f), RoundedCornerShape(12.dp)), // 연한 회색 배경
            contentAlignment = Alignment.Center
        ) {
            Icon(
                painter = painterResource(id = iconRes),
                contentDescription = null,
                tint = PrimaryGreen,
                modifier = Modifier.size(20.dp)
            )
        }

        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = title,
                style = MaterialTheme.typography.titleSmall.copy(fontSize = 15.sp),
                color = TextPrimary
            )
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = description,
                style = MaterialTheme.typography.bodySmall,
                color = TextSecondary,
                lineHeight = 18.sp
            )
        }
    }
}