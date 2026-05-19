package com.d102.wye.presentation.simulation.progress.result

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import com.d102.wye.R
import com.d102.wye.domain.model.AiReviewResult
import com.d102.wye.presentation.designsystem.WyeBadge
import com.d102.wye.presentation.designsystem.WyeCard
import com.d102.wye.presentation.designsystem.WyePrimaryButton
import com.d102.wye.presentation.model.UiState
import com.d102.wye.presentation.theme.Divider
import com.d102.wye.presentation.theme.PrimaryGreen
import com.d102.wye.presentation.theme.SurfaceVariant
import com.d102.wye.presentation.theme.TextDetail
import com.d102.wye.presentation.theme.TextPrimary
import com.d102.wye.presentation.theme.TextSecondary
import com.d102.wye.presentation.theme.TextTertiary

@Composable
fun AiReviewDialog(
    uiState: UiState<AiReviewResult>,
    onDismiss: () -> Unit
) {

    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(usePlatformDefaultWidth = false)
    ) {
        Surface(
            modifier = Modifier
                .fillMaxWidth(0.9f),
            shape = RoundedCornerShape(24.dp),
            color = Color.White
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .heightIn(max = 600.dp)
                    .padding(horizontal = 24.dp, vertical = 32.dp)
                    .verticalScroll(rememberScrollState()),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                // 1. 다이얼로그 공통 타이틀
                Text(
                    text = "AI 포트폴리오 진단",
                    style = MaterialTheme.typography.titleMedium,
                    color = TextPrimary
                )

                Spacer(modifier = Modifier.height(24.dp))

                // 2. 상태별 화면 분기
                when (uiState) {
                    is UiState.Loading -> {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(200.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                CircularProgressIndicator(color = PrimaryGreen)
                                Spacer(modifier = Modifier.height(16.dp))
                                Text(
                                    text = "AI가 포트폴리오를 분석 중입니다...",
                                    style = MaterialTheme.typography.bodyMedium,
                                    color = TextSecondary
                                )
                            }
                        }
                    }

                    is UiState.Success -> {
                        AiReviewSuccessContent(data = uiState.data)
                    }

                    is UiState.Error -> {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(150.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = "진단 결과를 불러오지 못했습니다.\n다시 시도해주세요.",
                                color = Color.Red,
                                textAlign = TextAlign.Center
                            )
                        }
                    }

                    else -> Unit
                }

                Spacer(modifier = Modifier.height(32.dp))

                // 3. 닫기 버튼
                WyePrimaryButton(
                    text = "닫기",
                    modifier = Modifier.fillMaxWidth(),
                    onClick = onDismiss
                )
            }
        }
    }
}


@Composable
private fun AiReviewSuccessContent(data: AiReviewResult) {
    Column(modifier = Modifier.fillMaxWidth()) {

        // 1. 요약 박스
        WyeCard(
            modifier = Modifier.fillMaxWidth(),
            border = BorderStroke(1.dp, PrimaryGreen.copy(alpha = 0.2f)),
            containerColor = PrimaryGreen.copy(alpha = 0.05f),
            elevation = 0.dp
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                Icon(
                    painter = painterResource(id = R.drawable.ic_ai_robot),
                    contentDescription = null,
                    tint = PrimaryGreen,
                    modifier = Modifier.size(36.dp)
                )
                Column {
                    Text(
                        text = data.mainTitle,
                        style = MaterialTheme.typography.titleSmall,
                        color = PrimaryGreen
                    )
                    Spacer(modifier = Modifier.height(6.dp))
                    Text(
                        text = data.subTitle,
                        style = MaterialTheme.typography.bodySmall,
                        color = TextSecondary
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(12.dp))

        FlowRow(
            horizontalArrangement = Arrangement.spacedBy(6.dp),
            verticalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            data.tags.forEach { tag ->
                WyeBadge(
                    label = "#$tag",
                    textStyle = MaterialTheme.typography.labelMedium,
                    color = SurfaceVariant,
                    textColor = TextTertiary,
                    textHorizontalPadding = 10,
                    textVerticalPadding = 6
                )
            }
        }

        Spacer(modifier = Modifier.height(36.dp))

        // 3. 요약 상세 섹션 타이틀
        Row(
            modifier = Modifier.padding(start = 8.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            Icon(
                painter = painterResource(id = R.drawable.ic_ai_diagnosis),
                contentDescription = null,
                tint = PrimaryGreen,
                modifier = Modifier.size(12.dp)
            )
            Text(
                text = "요약 상세",
                style = MaterialTheme.typography.titleSmall.copy(fontSize = 14.sp),
                color = TextSecondary
            )
        }

        Spacer(modifier = Modifier.height(8.dp))

        // 4. 상세 내용 텍스트 박스
        WyeCard(
            modifier = Modifier.fillMaxWidth(),
            border = BorderStroke(1.dp, Divider),
            elevation = 0.dp
        ) {
            Text(
                modifier = Modifier.padding(2.dp),
                text = data.feedback,
                style = MaterialTheme.typography.bodyMedium,
                lineHeight = 22.sp,
                color = TextDetail,
            )
        }
    }
}