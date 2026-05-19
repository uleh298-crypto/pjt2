package com.d102.wye.presentation.simulation.progress.setup

import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.requiredWidthIn
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Close
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Slider
import androidx.compose.material3.SliderDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.TextRange
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.TextFieldValue
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.d102.wye.R
import com.d102.wye.presentation.designsystem.DashedContainer
import com.d102.wye.presentation.designsystem.WyeCard
import com.d102.wye.presentation.designsystem.WyeCircleIcon
import com.d102.wye.presentation.designsystem.WyePrimaryButton
import com.d102.wye.presentation.simulation.progress.PortfolioItem
import com.d102.wye.presentation.simulation.progress.SimulationFormState
import com.d102.wye.presentation.theme.BackGroundLightGreen2
import com.d102.wye.presentation.theme.BadgeNeutralFont
import com.d102.wye.presentation.theme.Border
import com.d102.wye.presentation.theme.IconInactive
import com.d102.wye.presentation.theme.PrimaryGreen
import com.d102.wye.presentation.theme.SurfaceVariant
import com.d102.wye.presentation.theme.TextPrimary
import com.d102.wye.presentation.theme.TextSecondary

@Composable
fun PortfolioSection(
    formState: SimulationFormState,
    onAddClick: () -> Unit,
    onRemoveClick: (String) -> Unit,
    onWeightChange: (String, Int) -> Unit,
    onConfirmClick: () -> Unit
) {
    val totalWeight = formState.portfolioItems.sumOf { it.weight }
    val isComplete = totalWeight == 100

    val badgeColor = when {
        totalWeight == 100 -> PrimaryGreen
        totalWeight > 100 -> BadgeNeutralFont
        else -> IconInactive
    }

    Column(
        modifier = Modifier
            .background(BackGroundLightGreen2)
            .padding(horizontal = 20.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(text = "포트폴리오 구성", style = MaterialTheme.typography.titleSmall)

            Box(
                modifier = Modifier.background(
                    color = badgeColor,
                    shape = RoundedCornerShape(50.dp)
                ),
                contentAlignment = Alignment.Center,
            ) {
                Text(
                    text = "합계: $totalWeight%",
                    color = Color.White,
                    textAlign = TextAlign.Center,
                    style = MaterialTheme.typography.labelSmall,
                    modifier = Modifier
                        .padding(vertical = 8.dp)
                        .requiredWidthIn(min = 78.dp)
                )
            }
        }

        Spacer(modifier = Modifier.height(16.dp))
// 1. 기존 선택된 리스트 (로딩 중이더라도 기존 아이템은 보여야 함)
        if (formState.portfolioItems.isNotEmpty()) {
            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                formState.portfolioItems.forEach { item ->
                    val otherWeights = formState.portfolioItems
                        .filter { it.ticker != item.ticker }
                        .sumOf { it.weight }
                    val maxWeight = (100 - otherWeights).coerceIn(0, 100)

                    PortfolioSliderItemRow(
                        item = item,
                        maxWeight = maxWeight,
                        onWeightChange = { newWeight -> onWeightChange(item.ticker, newWeight) },
                        onRemove = { onRemoveClick(item.ticker) }
                    )
                }
            }
            Spacer(modifier = Modifier.height(12.dp))
        }

        // 2. 하단 UI (로딩 바 OR 종목 추가 버튼)
        if (formState.isFetchingEtfInfo) {
            // ── 똥도동똥똥 애니메이션 설정 ──────────────────────────────────
            val infiniteTransition = rememberInfiniteTransition(label = "loading")
            val offsetY by infiniteTransition.animateFloat(
                initialValue = -5f, // 위로 5dp
                targetValue = 5f,   // 아래로 5dp
                animationSpec = infiniteRepeatable(
                    animation = tween(600, easing = FastOutSlowInEasing),
                    repeatMode = RepeatMode.Reverse // 왔다 갔다
                ),
                label = "offsetY"
            )

            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 24.dp), // 여백을 살짝 더 줌
                contentAlignment = Alignment.Center
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.Center
                ) {
                    CircularProgressIndicator(
                        modifier = Modifier.size(18.dp),
                        color = PrimaryGreen,
                        strokeWidth = 2.dp
                    )

                    Spacer(modifier = Modifier.width(12.dp))

                    Text(
                        text = "ETF 과거 데이터를 분석하고 있어요...",
                        style = MaterialTheme.typography.bodySmall.copy(
                            fontWeight = FontWeight.Medium,
                            letterSpacing = (-0.5).sp
                        ),
                        color = PrimaryGreen,
                        modifier = Modifier.graphicsLayer {
                            translationY = offsetY.dp.toPx()
                        }
                    )
                }
            }
        } else {
            // 로딩 중이 아닐 때만 추가 버튼 노출
            if (formState.portfolioItems.isEmpty()) {
                // 아무것도 없을 때는 텅 빈 대형 점선 버튼
                DashedContainer(
                    modifier = Modifier.clickable { onAddClick() },
                    strokeWidth = 2.dp,
                    borderColor = Border
                ) {
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        Icon(
                            painter = painterResource(R.drawable.ic_plus),
                            contentDescription = null,
                            tint = IconInactive
                        )
                        Text(
                            text = "ETF 종목 추가하기",
                            style = MaterialTheme.typography.titleSmall,
                            color = IconInactive
                        )
                    }
                }
            } else if (formState.portfolioItems.size < 10) {
                // 1개~9개 있을 때는 얇은 초록색 추가 버튼
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(
                            color = PrimaryGreen.copy(alpha = 0.1f),
                            shape = RoundedCornerShape(16.dp)
                        )
                        .clickable { onAddClick() }
                        .padding(vertical = 14.dp),
                    horizontalArrangement = Arrangement.Center,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        imageVector = Icons.Default.Add,
                        contentDescription = "추가",
                        tint = PrimaryGreen,
                        modifier = Modifier.size(20.dp)
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = "ETF 종목 추가하기",
                        style = MaterialTheme.typography.titleSmall,
                        color = PrimaryGreen
                    )
                }
            }
        }


        Spacer(modifier = Modifier.height(16.dp))

        WyePrimaryButton(
            text = if (isComplete) "시뮬레이션 확인" else "비율을 100%로 맞춰주세요",
            modifier = Modifier.fillMaxWidth(),
            enabled = isComplete,
            onClick = { if (isComplete) onConfirmClick() }
        )

        Spacer(modifier = Modifier.height(40.dp))
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun PortfolioSliderItemRow(
    item: PortfolioItem,
    maxWeight: Int,
    onWeightChange: (Int) -> Unit,
    onRemove: () -> Unit
) {
    var weightTextFieldValue by remember(item.weight) {
        mutableStateOf(
            TextFieldValue(
                text = item.weight.toString(),
                selection = TextRange(item.weight.toString().length)
            )
        )
    }

    WyeCard(
        modifier = Modifier.fillMaxWidth(),
        cornerRadius = 16.dp,
        innerPadding = PaddingValues(start = 20.dp, top = 20.dp, end = 20.dp),
        border = BorderStroke(1.dp, SurfaceVariant),
        containerColor = Color.White,
        elevation = 0.dp
    ) {
        Column(modifier = Modifier.fillMaxWidth()) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.Top
            ) {
                WyeCircleIcon(
                    tag = item.name,
                    count = 2,
                    size = 36.dp,
                    backgroundColor = SurfaceVariant,
                    contentColor = TextSecondary
                )

                Spacer(modifier = Modifier.width(16.dp))

                Column(
                    modifier = Modifier.weight(1f),
                    verticalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    Text(
                        text = item.name,
                        color = TextPrimary,
                        style = MaterialTheme.typography.titleSmall.copy(fontSize = 14.sp),
                    )
                    Text(
                        text = item.ticker,
                        color = TextSecondary,
                        style = MaterialTheme.typography.bodySmall,
                        maxLines = 1,
                    )
                }

                Spacer(modifier = Modifier.width(12.dp))

                BasicTextField(
                    value = weightTextFieldValue,
                    onValueChange = { newValue ->
                        val filteredText = newValue.text.filter { it.isDigit() }
                        if (filteredText.isNotEmpty()) {
                            val newWeight = filteredText.toInt().coerceIn(0, maxWeight)
                            weightTextFieldValue = newValue.copy(
                                text = newWeight.toString(),
                                selection = TextRange(newWeight.toString().length)
                            )
                            onWeightChange(newWeight)
                        } else {
                            weightTextFieldValue =
                                newValue.copy(text = "", selection = TextRange(0))
                            onWeightChange(0)
                        }
                    },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    textStyle = MaterialTheme.typography.titleMedium.copy(
                        color = PrimaryGreen,
                        textAlign = TextAlign.End
                    ),
                    modifier = Modifier.width(44.dp)
                )

                Text(
                    text = "%",
                    color = PrimaryGreen,
                    style = MaterialTheme.typography.titleMedium,
                )

                Spacer(modifier = Modifier.width(12.dp))

                IconButton(
                    onClick = onRemove,
                    modifier = Modifier.size(24.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.Close,
                        contentDescription = "삭제",
                        tint = TextSecondary,
                        modifier = Modifier.size(20.dp)
                    )
                }
            }

            Slider(
                value = item.weight.toFloat(),
                onValueChange = { newValue ->
                    val newIntValue = newValue.toInt().coerceAtMost(maxWeight)
                    weightTextFieldValue = TextFieldValue(
                        text = newIntValue.toString(),
                        selection = TextRange(newIntValue.toString().length)
                    )
                    onWeightChange(newIntValue)
                },
                valueRange = 0f..100f,
                modifier = Modifier.fillMaxWidth(),
                thumb = {
                    Box(
                        modifier = Modifier
                            .size(14.dp)
                            .background(color = PrimaryGreen, shape = CircleShape)
                    )
                },
                track = { sliderState ->
                    SliderDefaults.Track(
                        colors = SliderDefaults.colors(
                            activeTrackColor = PrimaryGreen.copy(alpha = 0.3f),
                            inactiveTrackColor = SurfaceVariant
                        ),
                        sliderState = sliderState,
                        thumbTrackGapSize = 0.dp,
                        modifier = Modifier.height(6.dp)
                    )
                }
            )
        }
    }
}