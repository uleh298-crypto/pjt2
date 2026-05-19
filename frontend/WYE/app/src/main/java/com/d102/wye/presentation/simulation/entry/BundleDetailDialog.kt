package com.d102.wye.presentation.simulation.entry

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.wrapContentHeight
import androidx.compose.foundation.lazy.LazyColumn
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
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import com.d102.wye.R
import com.d102.wye.domain.model.EtfBundleDetail
import com.d102.wye.presentation.designsystem.WyeCard
import com.d102.wye.presentation.designsystem.WyePrimaryButton
import com.d102.wye.presentation.theme.SurfaceVariant
import com.d102.wye.presentation.theme.TextTertiary

@Composable
fun BundleDetailDialog(
    bundle: EtfBundleDetail,
    isPortfolioFull: Boolean = false,
    onDismiss: () -> Unit,
    onStartSimulation: (EtfBundleDetail) -> Unit
) {
    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(usePlatformDefaultWidth = false) // 전체 너비 조절용
    ) {
        Surface(
            modifier = Modifier
                .fillMaxWidth(0.9f)
                .padding(vertical = 32.dp),
            shape = RoundedCornerShape(24.dp),
            color = Color.White
        ) {
            Box(modifier = Modifier.fillMaxWidth()) {

                // 1. 닫기 버튼 (X 아이콘)
                IconButton(
                    onClick = onDismiss,
                    modifier = Modifier
                        .align(Alignment.TopEnd)
                        .padding(8.dp) // 터치 영역 확보 및 여백
                ) {
                    Icon(
                        imageVector = Icons.Default.Close,
                        contentDescription = "닫기",
                        tint = TextTertiary
                    )
                }
                Column(
                    modifier = Modifier
                        .padding(24.dp)
                        .wrapContentHeight(),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    // 1. 상단 이모지 아이콘
                    Icon(
                        painter = painterResource(bundle.bundleType.toDrawable()),
                        contentDescription = null,
                        modifier = Modifier
                            .size(56.dp),
                        tint = Color.Unspecified
                    )
                    Spacer(modifier = Modifier.height(12.dp))

                    // 2. 제목 및 설명
                    Text(
                        text = bundle.name,
                        style = MaterialTheme.typography.titleLarge,
                        textAlign = TextAlign.Center
                    )

                    Spacer(modifier = Modifier.height(12.dp))

                    Text(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 24.dp),
                        text = bundle.description,
                        style = MaterialTheme.typography.bodyMedium,
                        textAlign = TextAlign.Center,
                        color = TextTertiary
                    )

                    Spacer(modifier = Modifier.height(32.dp))

                    // 3. 구성 리스트 타이틀
                    Text(
                        text = "이렇게 구성되어 있어요",
                        modifier = Modifier.fillMaxWidth(),
                        style = MaterialTheme.typography.titleSmall,
                        textAlign = TextAlign.Start
                    )

                    Spacer(modifier = Modifier.height(16.dp))

                    // 4. ETF 리스트
                    LazyColumn(
                        modifier = Modifier
                            .weight(weight = 1f, fill = false)
                            .heightIn(max = 300.dp),
                        verticalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        bundle.etfItems.forEach { etf ->
                            item {
                                EtfItemRow(item = etf.name)
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(16.dp))

                    WyeCard(containerColor = SurfaceVariant, elevation = 0.dp) {
                        Row(verticalAlignment = Alignment.Top) {
                            Text(
                                text = "ⓘ",
                                color = TextTertiary,
                                modifier = Modifier.padding(end = 8.dp)
                            )
                            Text(
                                text = "과거의 수익률이 미래의 수익을 보장하지 않습니다. 신중하게 검토 후 결정하세요.",
                                style = MaterialTheme.typography.labelMedium,
                                color = TextTertiary,
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(24.dp))

                    WyePrimaryButton(
                        text = "이 꾸러미로 시뮬레이션 시작하기",
                        modifier = Modifier.fillMaxWidth(),
                        enabled = !isPortfolioFull,
                        onClick = { onStartSimulation(bundle) }
                    )
                }
            }
        }
    }
}

