package com.d102.wye.presentation.explore.detail.components

import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.StartOffset
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AccountBalance
import androidx.compose.material.icons.filled.Bolt
import androidx.compose.material.icons.filled.Category
import androidx.compose.material.icons.filled.CellTower
import androidx.compose.material.icons.filled.Computer
import androidx.compose.material.icons.filled.DirectionsCar
import androidx.compose.material.icons.filled.Factory
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.LocalHospital
import androidx.compose.material.icons.filled.Memory
import androidx.compose.material.icons.filled.MoreHoriz
import androidx.compose.material.icons.filled.Science
import androidx.compose.material.icons.filled.ShoppingCart
import androidx.compose.material.icons.filled.WaterDrop
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.zIndex
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.d102.wye.domain.model.EtfCluster
import com.d102.wye.domain.model.EtfClusterData
import com.d102.wye.domain.model.EtfDetail
import com.d102.wye.domain.model.InfluentialStock
import com.d102.wye.presentation.designsystem.CategoryBadge
import com.d102.wye.presentation.explore.detail.EtfDetailViewModel
import com.d102.wye.presentation.theme.BackGroundLightGreen
import com.d102.wye.presentation.theme.BadgeActive
import com.d102.wye.presentation.theme.BadgeActiveFont
import com.d102.wye.presentation.theme.BadgeAggressive
import com.d102.wye.presentation.theme.BadgeAggressiveFont
import com.d102.wye.presentation.theme.BadgeConservative
import com.d102.wye.presentation.theme.BadgeConservativeFont
import com.d102.wye.presentation.theme.BadgeConservativeGrowth
import com.d102.wye.presentation.theme.BadgeConservativeGrowthFont
import com.d102.wye.presentation.theme.BadgeNeutral
import com.d102.wye.presentation.theme.BadgeNeutralFont
import com.d102.wye.presentation.theme.Divider
import com.d102.wye.presentation.theme.EtfFall
import com.d102.wye.presentation.theme.EtfRise
import com.d102.wye.presentation.theme.PrimaryGreen
import com.d102.wye.presentation.theme.SurfaceVariant
import com.d102.wye.presentation.theme.SurfaceWhite
import com.d102.wye.presentation.theme.TextPrimary
import com.d102.wye.presentation.theme.TextSecondary
import kotlinx.coroutines.delay
import kotlin.math.abs
import kotlin.math.cos
import kotlin.math.sin
import kotlin.random.Random

@Composable
fun ClusterTab(
    detail: EtfDetail,
    clusterData: EtfClusterData,
    viewModel: EtfDetailViewModel,
    onStockClick: (String) -> Unit = {},
) {
    var selectedCluster by remember { mutableStateOf<EtfCluster?>(null) }

    if (selectedCluster != null) {
        SectorBottomSheet(
            cluster = selectedCluster!!,
            onDismiss = { selectedCluster = null },
            onStockClick = onStockClick,
        )
    }

    BoxWithConstraints(modifier = Modifier.fillMaxSize()) {
        val screenH = maxHeight

        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState()),
        ) {
            Column(modifier = Modifier.height(screenH - 140.dp)) {
                Column(
                    modifier = Modifier
                        .weight(1f)
                        .padding(horizontal = 20.dp, vertical = 16.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(14.dp),
                ) {
                    EtfHeader(detail = detail, englishName = clusterData.englishName)

                    ClusterBubbleChart(
                        name = detail.name,
                        clusters = clusterData.sectors,
                        onClusterClick = { selectedCluster = it },
                        modifier = Modifier
                            .fillMaxWidth()
                            .weight(1f)
                            .padding(top = 20.dp),
                    )

                    val marketStatusLabel by viewModel.marketStatusLabel.collectAsStateWithLifecycle()
                    PriceVolumeRow(detail = detail, marketStatusLabel = marketStatusLabel)
                }
            }
            HorizontalDivider(
                color = Divider,
                modifier = Modifier
                    .padding(horizontal = 24.dp)
                    .padding(top = 2.dp, bottom = 10.dp)
            )
            InfluentialStocksSection(
                stocks = clusterData.influentialStocks,
                onStockClick = onStockClick
            )
        }
    }
}

// ─────────────────────────────────────────────────────────────────────────────
// 버블 클러스터 차트
// ─────────────────────────────────────────────────────────────────────────────

@Composable
private fun ClusterBubbleChart(
    name: String,
    clusters: List<EtfCluster>,
    onClusterClick: (EtfCluster) -> Unit,
    modifier: Modifier = Modifier,
) {
    val sorted = remember(clusters) { clusters.sortedByDescending { it.percentage } }
    val mainClusters = remember(sorted) { sorted.take(5) }
    val otherClusters = remember(sorted) { sorted.drop(5) }
    val hasOthers = otherClusters.isNotEmpty()

    // false = 메인 뷰(상위 5개+기타), true = 전체 확장 뷰
    var showAll by remember { mutableStateOf(false) }

    val othersLabel = "기타"

    val displayClusters = if (showAll) sorted else mainClusters
    val viewKey = if (showAll) "all" else "main"

    BubbleChartLayout(
        key = viewKey,
        centerLabel = if (showAll && hasOthers) othersLabel else name,
        centerColor = PrimaryGreen,
        isAllView = showAll,
        clusters = displayClusters,
        hasOthersSlot = !showAll && hasOthers,
        othersClusters = otherClusters,
        onClusterClick = onClusterClick,
        onCenterClick = { if (showAll) showAll = false },
        onOthersClick = { showAll = true },
        modifier = modifier,
    )
}

@Composable
private fun BubbleChartLayout(
    key: String,
    centerLabel: String,
    centerColor: Color,
    isAllView: Boolean,
    clusters: List<EtfCluster>,
    hasOthersSlot: Boolean,
    othersClusters: List<EtfCluster>,
    onClusterClick: (EtfCluster) -> Unit,
    onCenterClick: () -> Unit,
    onOthersClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    var visible by remember(key) { mutableStateOf(false) }
    LaunchedEffect(key) { visible = true }

    val centerScale by animateFloatAsState(
        targetValue = if (visible) 1f else 0f,
        animationSpec = spring(
            dampingRatio = Spring.DampingRatioMediumBouncy,
            stiffness = Spring.StiffnessMedium,
        ),
        label = "centerBubble",
    )

    val infiniteTransition = rememberInfiniteTransition(label = "pulse")
    val pulseProgress by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(
            animation = tween(2200, easing = LinearEasing),
            repeatMode = RepeatMode.Restart,
        ),
        label = "pulseProgress",
    )

    val maxPct = clusters.maxOfOrNull { it.percentage } ?: 1.0
    val minPct = clusters.minOfOrNull { it.percentage } ?: 0.0

    val totalSlots = clusters.size + if (hasOthersSlot) 1 else 0
    val angleStep = 360.0 / totalSlots.coerceAtLeast(1)

    // 별 위치 고정 (recomposition 마다 바뀌지 않게)
    val stars = remember {
        val rng = Random(seed = 42)
        List(60) { Triple(rng.nextFloat(), rng.nextFloat(), rng.nextFloat()) } // x, y, size
    }

    // 궤도 자전 애니메이션
    val orbitRotation by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 360f,
        animationSpec = infiniteRepeatable(
            animation = tween(28000, easing = LinearEasing),
            repeatMode = RepeatMode.Restart,
        ),
        label = "orbitRotation",
    )

    // 전체 뷰일 때 궤도 반경 확장 → 버블이 바깥으로 퍼짐
    val orbitFraction by animateFloatAsState(
        targetValue = if (isAllView) 0.60f else 0.39f,
        animationSpec = spring(
            dampingRatio = Spring.DampingRatioMediumBouncy,
            stiffness = Spring.StiffnessLow
        ),
        label = "orbitFraction",
    )
    val centerFraction by animateFloatAsState(
        targetValue = if (isAllView) 0.26f else 0.42f,
        animationSpec = spring(
            dampingRatio = Spring.DampingRatioMediumBouncy,
            stiffness = Spring.StiffnessLow
        ),
        label = "centerFraction",
    )
    val maxBubbleFraction by animateFloatAsState(
        targetValue = if (isAllView) 0.22f else 0.31f,
        animationSpec = spring(
            dampingRatio = Spring.DampingRatioMediumBouncy,
            stiffness = Spring.StiffnessLow
        ),
        label = "maxBubbleFraction",
    )
    val minBubbleFraction by animateFloatAsState(
        targetValue = if (isAllView) 0.15f else 0.22f,
        animationSpec = spring(
            dampingRatio = Spring.DampingRatioMediumBouncy,
            stiffness = Spring.StiffnessLow
        ),
        label = "minBubbleFraction",
    )

    BoxWithConstraints(
        modifier = modifier,
        contentAlignment = Alignment.Center,
    ) {
        val availableSize = minOf(maxWidth, maxHeight)
        val orbitRadius = availableSize * orbitFraction
        val centerBubble = availableSize * centerFraction
        val minBubbleSize = availableSize * minBubbleFraction
        val maxBubbleSize = availableSize * maxBubbleFraction

        // all-view: 황금각 방향 × 안→밖 반경 스캔 + 다방향 각도 폴백으로 뭉침 방지
        val allViewPositions: List<Pair<Float, Float>> = remember(clusters, maxWidth, maxHeight) {
            val wHalf = maxWidth.value / 2f
            val hHalf = maxHeight.value / 2f
            val availSz = minOf(wHalf, hHalf) * 2f
            val maxPctL = clusters.maxOfOrNull { it.percentage } ?: 1.0
            val minPctL = clusters.minOfOrNull { it.percentage } ?: 0.0
            val maxBR = availSz * 0.22f / 2f
            val minBR = availSz * 0.15f / 2f
            val centerR = availSz * 0.26f / 2f
            val gap = 12f
            val goldenAngle = 137.508

            val placed = mutableListOf(Triple(0f, 0f, centerR))

            clusters.mapIndexed { idx, cluster ->
                val norm = if (maxPctL > minPctL)
                    ((cluster.percentage - minPctL) / (maxPctL - minPctL)).toFloat() else 0.5f
                val bR = minBR + (maxBR - minBR) * norm

                // 황금각 기준 + 좌우 최대 ±160° 까지 20° 단위로 17방향 시도
                val baseAngleDeg = idx * goldenAngle + 45.0
                val angleOffsets = (-8..8).map { it * 20.0 }

                var bestCx = 0f;
                var bestCy = 0f

                outer@ for (ao in angleOffsets) {
                    val angle = Math.toRadians(baseAngleDeg + ao)
                    val cosA = abs(cos(angle)).toFloat().coerceAtLeast(0.001f)
                    val sinA = abs(sin(angle)).toFloat().coerceAtLeast(0.001f)

                    // 직사각형 내 최대 도달 반경
                    val maxSafeR = minOf((wHalf - bR) / cosA, (hHalf - bR) / sinA)
                    // 중심 버블 바깥에서 시작 (뭉침 방지: idx 의존 없이 항상 minR부터)
                    val minR = (centerR + bR + gap).coerceAtMost(maxSafeR)

                    var r = minR
                    while (r <= maxSafeR) {
                        val tcx = r * cos(angle).toFloat()
                        val tcy = r * sin(angle).toFloat()
                        val ok = placed.none { (px, py, pr) ->
                            val dx = tcx - px;
                            val dy = tcy - py
                            dx * dx + dy * dy < (bR + pr + gap) * (bR + pr + gap)
                        }
                        if (ok) {
                            bestCx = tcx; bestCy = tcy; break@outer
                        }
                        r += 3f
                    }
                }

                placed.add(Triple(bestCx, bestCy, bR))
                Pair(bestCx, bestCy)
            }
        }

        // 별 필드 + 궤도 링 + 펄스
        Canvas(modifier = Modifier.fillMaxSize()) {
            val cx = size.width / 2f
            val cy = size.height / 2f

            // 별 필드
            stars.forEach { (sx, sy, ss) ->
                drawCircle(
                    color = Color.White.copy(alpha = 0.25f + ss * 0.35f),
                    radius = 1.2f + ss * 2f,
                    center = Offset(sx * size.width, sy * size.height),
                )
            }

            // 궤도 링 (버블들이 도는 경로)
            val orbitR = orbitRadius.toPx()
            drawCircle(
                color = centerColor.copy(alpha = 0.12f),
                radius = orbitR,
                center = Offset(cx, cy),
                style = Stroke(width = 1.5f),
            )
            // 궤도 위 움직이는 광점
            val dotAngle = Math.toRadians(orbitRotation.toDouble())
            drawCircle(
                color = centerColor.copy(alpha = 0.55f),
                radius = 4f,
                center = Offset(
                    cx + orbitR * cos(dotAngle).toFloat(),
                    cy + orbitR * sin(dotAngle).toFloat(),
                ),
            )

            // 펄스 링
            val maxRingR = centerBubble.toPx() * 0.67f
            for (i in 0..2) {
                val progress = (pulseProgress + i / 3f) % 1f
                drawCircle(
                    color = centerColor,
                    radius = maxRingR * progress,
                    center = Offset(cx, cy),
                    alpha = (1f - progress) * 0.45f,
                    style = Stroke(width = 2.5f),
                )
            }
        }

        // 중심 버블
        Box(
            contentAlignment = Alignment.Center,
            modifier = Modifier
                .zIndex(10f)
                .size(centerBubble)
                .scale(centerScale)
                .shadow(elevation = 12.dp, shape = CircleShape, clip = false)
                .clip(CircleShape)
                .background(centerColor)
                .then(if (isAllView) Modifier.clickable { onCenterClick() } else Modifier),
        ) {
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(2.dp),
                modifier = Modifier.padding(horizontal = 12.dp),
            ) {
                if (isAllView) {
                    Text(
                        text = "← 접기",
                        fontSize = 9.sp,
                        color = Color.White.copy(alpha = 0.75f),
                        textAlign = TextAlign.Center,
                    )
                }
                val fontSize = (centerBubble.value * 0.13f).coerceIn(12f, 18f).sp
                Text(
                    text = if (isAllView) centerLabel else centerLabel.replace(" ", "\n"),
                    color = Color.White,
                    fontSize = fontSize,
                    fontWeight = FontWeight.ExtraBold,
                    textAlign = TextAlign.Center,
                    lineHeight = fontSize * 1.4f,
                )
            }
        }

        // 중심 → 버블 연결선 (인력선) — 메인 뷰에서만 표시
        if (!isAllView) {
            Canvas(modifier = Modifier.fillMaxSize()) {
                val cx = size.width / 2f
                val cy = size.height / 2f
                val orbitR = orbitRadius.toPx()
                val totalCount = clusters.size + if (hasOthersSlot) 1 else 0
                val step = 360.0 / totalCount.coerceAtLeast(1)
                repeat(totalCount) { idx ->
                    val rad = Math.toRadians(idx * step - 90.0)
                    val tx = cx + orbitR * cos(rad).toFloat()
                    val ty = cy + orbitR * sin(rad).toFloat()
                    drawLine(
                        color = centerColor.copy(alpha = 0.18f),
                        start = Offset(cx, cy),
                        end = Offset(tx, ty),
                        strokeWidth = 1f,
                    )
                }
            }
        }

        // 섹터 버블
        clusters.forEachIndexed { idx, cluster ->
            val normalized = if (maxPct > minPct)
                ((cluster.percentage - minPct) / (maxPct - minPct)).toFloat() else 0.5f
            val bubbleSize = minBubbleSize + (maxBubbleSize - minBubbleSize) * normalized

            val x: Dp
            val y: Dp
            if (isAllView) {
                val pos = allViewPositions.getOrNull(idx)
                x = (pos?.first ?: 0f).dp
                y = (pos?.second ?: 0f).dp
            } else {
                // 정육각형에 살짝 불규칙 부여 (각도 ±8°, 반경 ±5% 이내 고정 오프셋)
                val angleJitter = listOf(12.0, 4.0, -5.0, -8.0, -7.0, 6.0, -4.0)
                val radiusJitter = listOf(1.08f, 0.98f, 1.05f, 0.97f, 1.04f, 0.95f, 1.03f)
                val jAngle = angleJitter.getOrElse(idx) { 0.0 }
                val jRadius = radiusJitter.getOrElse(idx) { 1.0f }
                val rad = Math.toRadians(idx * angleStep - 90.0 + jAngle)
                x = (orbitRadius.value * jRadius * cos(rad)).dp
                y = (orbitRadius.value * jRadius * sin(rad)).dp
            }

            ClusterBubble(
                cluster = cluster,
                index = idx,
                visible = visible,
                bubbleSize = bubbleSize,
                onClick = { onClusterClick(cluster) },
                modifier = Modifier.offset(x = x, y = y),
            )
        }

        // 기타 버블 (메인 뷰에서만)
        if (hasOthersSlot) {
            val angleJitter = listOf(12.0, 4.0, -5.0, -8.0, -7.0, 6.0, -4.0)
            val radiusJitter = listOf(1.08f, 0.98f, 1.05f, 0.97f, 1.04f, 0.95f, 1.03f)
            val othersIdx = clusters.size
            val jAngle = angleJitter.getOrElse(othersIdx) { 0.0 }
            val jRadius = radiusJitter.getOrElse(othersIdx) { 1.0f }
            val rad = Math.toRadians(othersIdx * angleStep - 90.0 + jAngle)
            val x = (orbitRadius.value * jRadius * cos(rad)).dp
            val y = (orbitRadius.value * jRadius * sin(rad)).dp
            OthersBubble(
                index = clusters.size,
                visible = visible,
                count = othersClusters.size,
                percentage = othersClusters.sumOf { it.percentage },
                bubbleSize = minBubbleSize + (maxBubbleSize - minBubbleSize) * 0.5f,
                onClick = onOthersClick,
                modifier = Modifier.offset(x = x, y = y),
            )
        }
    }
}

@Composable
private fun ClusterBubble(
    cluster: EtfCluster,
    index: Int,
    visible: Boolean,
    bubbleSize: Dp,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    var triggered by remember { mutableStateOf(false) }
    LaunchedEffect(visible) {
        if (visible) {
            delay(index * 60L)
            triggered = true
        }
    }

    val scale by animateFloatAsState(
        targetValue = if (triggered) 1f else 0f,
        animationSpec = spring(
            dampingRatio = Spring.DampingRatioMediumBouncy,
            stiffness = Spring.StiffnessMediumLow,
        ),
        label = "clusterBubble$index",
    )

    val floatTransition = rememberInfiniteTransition(label = "float$index")
    val floatY by floatTransition.animateFloat(
        initialValue = -4f,
        targetValue = 4f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 1800 + index * 200),
            repeatMode = RepeatMode.Reverse,
            initialStartOffset = StartOffset(index * 300),
        ),
        label = "floatY$index",
    )

    Box(
        contentAlignment = Alignment.Center,
        modifier = modifier
            .offset(y = floatY.dp)
            .scale(scale),
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(0.dp, Alignment.CenterVertically),
            modifier = Modifier
                .size(bubbleSize)
                .shadow(elevation = 6.dp, shape = CircleShape, clip = false)
                .clip(CircleShape)
                .background(Color.White)
                .clickable(onClick = onClick),
        ) {
            val iconSize = bubbleSize * 0.26f
            val nameFontSz = (bubbleSize.value * 0.15f).sp
            val pctFontSz = (bubbleSize.value * 0.15f).sp
            val contentW = bubbleSize * 0.84f
            Icon(
                imageVector = sectorIcon(cluster.name),
                contentDescription = cluster.name,
                tint = PrimaryGreen,
                modifier = Modifier.size(iconSize),
            )
            Text(
                text = cluster.name,
                fontSize = nameFontSz,
                lineHeight = nameFontSz * 1.1f,
                fontWeight = FontWeight.SemiBold,
                color = TextPrimary,
                textAlign = TextAlign.Center,
                maxLines = 2,
                modifier = Modifier.width(contentW),
            )
            Text(
                text = "${"%.1f".format(cluster.percentage)}%",
                fontSize = pctFontSz,
                lineHeight = pctFontSz,
                color = TextSecondary,
                textAlign = TextAlign.Center,
                modifier = Modifier.width(contentW),
            )
        }
    }
}

@Composable
private fun OthersBubble(
    index: Int,
    visible: Boolean,
    count: Int,
    percentage: Double,
    bubbleSize: Dp,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    var triggered by remember { mutableStateOf(false) }
    LaunchedEffect(visible) {
        if (visible) {
            delay(index * 60L)
            triggered = true
        }
    }

    val scale by animateFloatAsState(
        targetValue = if (triggered) 1f else 0f,
        animationSpec = spring(
            dampingRatio = Spring.DampingRatioMediumBouncy,
            stiffness = Spring.StiffnessMediumLow,
        ),
        label = "othersBubble",
    )

    val floatTransition = rememberInfiniteTransition(label = "floatOthers")
    val floatY by floatTransition.animateFloat(
        initialValue = -4f,
        targetValue = 4f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 1800 + index * 200),
            repeatMode = RepeatMode.Reverse,
            initialStartOffset = StartOffset(index * 300),
        ),
        label = "floatYOthers",
    )

    Box(
        contentAlignment = Alignment.Center,
        modifier = modifier
            .offset(y = floatY.dp)
            .scale(scale),
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(0.dp, Alignment.CenterVertically),
            modifier = Modifier
                .size(bubbleSize)
                .shadow(elevation = 6.dp, shape = CircleShape, clip = false)
                .clip(CircleShape)
                .background(Color.White)
                .clickable(onClick = onClick),
        ) {
            val iconSize = bubbleSize * 0.26f
            val nameFontSz = (bubbleSize.value * 0.155f).coerceIn(10f, 14f).sp
            val pctFontSz = (bubbleSize.value * 0.125f).coerceIn(9f, 12f).sp
            val contentW = bubbleSize * 0.84f
            Icon(
                imageVector = Icons.Filled.MoreHoriz,
                contentDescription = "기타",
                tint = PrimaryGreen,
                modifier = Modifier.size(iconSize),
            )
            Text(
                text = "기타 ${count}개",
                fontSize = nameFontSz,
                lineHeight = nameFontSz * 1.1f,
                fontWeight = FontWeight.SemiBold,
                color = TextPrimary,
                textAlign = TextAlign.Center,
                maxLines = 2,
                modifier = Modifier.width(contentW),
            )
            Text(
                text = "${"%.1f".format(percentage)}%",
                fontSize = pctFontSz,
                lineHeight = pctFontSz,
                color = TextSecondary,
                textAlign = TextAlign.Center,
                modifier = Modifier.width(contentW),
            )
        }
    }
}

// ─────────────────────────────────────────────────────────────────────────────
// 영향력 있는 종목
// ─────────────────────────────────────────────────────────────────────────────

@Composable
private fun InfluentialStocksSection(
    stocks: List<InfluentialStock>,
    onStockClick: (String) -> Unit,
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 24.dp)
            .padding(top = 12.dp, bottom = 24.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        Text(
            "현재 이 ETF 수익률에 영향력을 미치는 종목은?",
            style = MaterialTheme.typography.titleMedium.copy(fontSize = 17.5.sp),
            color = TextPrimary,
        )

        if (stocks.isEmpty()) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 24.dp),
                contentAlignment = Alignment.Center,
            ) {
                Text(
                    "해당 ETF는 주식 외 자산으로 구성되어\n종목 정보를 제공하지 않습니다.",
                    style = MaterialTheme.typography.bodyMedium,
                    color = TextSecondary,
                    textAlign = TextAlign.Center,
                )
            }
        } else {
            stocks.forEach { stock ->
                InfluentialStockItem(stock = stock, onStockClick = onStockClick)
            }
        }
    }
}

@Composable
private fun InfluentialStockItem(
    stock: InfluentialStock,
    onStockClick: (String) -> Unit,
) {
    val changeColor = if (stock.changeRate >= 0) EtfRise else EtfFall
    val changeSign = if (stock.changeRate >= 0) "+" else ""

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(12.dp))
            .background(SurfaceWhite)
            .border(
                width = 0.5.dp,
                color = Divider, // 원하는 색
                shape = RoundedCornerShape(12.dp)
            )
            .clickable { onStockClick(stock.ticker) }
            .padding(horizontal = 14.dp, vertical = 12.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(10.dp),
        ) {
            Box(
                contentAlignment = Alignment.Center,
                modifier = Modifier
                    .size(36.dp)
                    .clip(RoundedCornerShape(10.dp))
                    .background(SurfaceVariant),
            ) {
                Text(
                    stock.name.take(1),
                    style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.SemiBold),
                    color = TextSecondary,
                )
            }
            Column(verticalArrangement = Arrangement.spacedBy(2.dp)) {
                Text(
                    stock.name,
                    style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Medium),
                    color = TextPrimary
                )
                Text(
                    stock.ticker,
                    style = MaterialTheme.typography.bodySmall,
                    color = TextSecondary
                )
            }
        }
        Column(
            horizontalAlignment = Alignment.End,
            verticalArrangement = Arrangement.spacedBy(2.dp),
        ) {
            Text(
                "비중 ${"%.1f".format(stock.weight)}%",
                style = MaterialTheme.typography.bodySmall.copy(fontWeight = FontWeight.SemiBold),
                color = PrimaryGreen,
            )
            Text(
                "%,d원".format(stock.currentPrice),
                style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.SemiBold),
                color = TextPrimary,
            )
            Text(
                "$changeSign${"%.2f".format(stock.changeRate)}%",
                style = MaterialTheme.typography.bodySmall,
                color = changeColor,
            )
        }
    }
}

// ─────────────────────────────────────────────────────────────────────────────
// 공통 서브 컴포넌트
// ─────────────────────────────────────────────────────────────────────────────

@Composable
private fun EtfHeader(detail: EtfDetail, englishName: String) {
    val (badgeBg, badgeFg, badgeLabel) = riskToBadge(detail.riskGrade)
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(4.dp),
    ) {
        CategoryBadge(
            label = badgeLabel,
            backgroundColor = badgeBg,
            textColor = badgeFg,
            isPill = true,
            modifier = Modifier
                .scale(1.3f)
                .padding(bottom = 4.dp)
        )
        Text(
            detail.name,
            style = MaterialTheme.typography.titleLarge.copy(
                fontSize = 28.sp,
                fontWeight = FontWeight.ExtraBold
            ),
            color = TextPrimary,
            textAlign = TextAlign.Center
        )
        if (englishName.isNotBlank()) {
            Text(
                englishName,
                style = MaterialTheme.typography.bodySmall.copy(fontSize = 13.sp),
                color = TextSecondary
            )
        }
    }
}

@Composable
private fun PriceVolumeRow(detail: EtfDetail, marketStatusLabel: String = "") {
    Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
        if (marketStatusLabel.isNotBlank()) {
            Text(
                text = marketStatusLabel,
                style = MaterialTheme.typography.labelSmall,
                color = if (marketStatusLabel.contains("기준")) PrimaryGreen else TextSecondary,
                modifier = Modifier.fillMaxWidth(),
                textAlign = androidx.compose.ui.text.style.TextAlign.End,
            )
        }
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            InfoCard(
                label = "현재 가격",
                value = "%,d원".format(detail.currentPrice),
                valueColor = PrimaryGreen,
                modifier = Modifier.weight(1f)
            )
            InfoCard(
                label = "거래량(주)",
                value = formatVolume(detail.volume),
                valueColor = PrimaryGreen,
                modifier = Modifier.weight(1f)
            )
        }
    }
}

@Composable
private fun InfoCard(
    label: String,
    value: String,
    modifier: Modifier = Modifier,
    valueColor: Color = TextPrimary,
) {
    Column(
        modifier = modifier
            .clip(RoundedCornerShape(12.dp))
            .background(BackGroundLightGreen)
            .padding(14.dp),
        verticalArrangement = Arrangement.spacedBy(6.dp),
    ) {
        Text(label, style = MaterialTheme.typography.bodySmall, color = TextSecondary)
        Text(
            value,
            style = MaterialTheme.typography.bodyLarge.copy(fontWeight = FontWeight.SemiBold),
            color = valueColor
        )
    }
}

private fun sectorIcon(name: String): ImageVector = when {
    name.contains("반도체") -> Icons.Filled.Memory
    name.contains("금융") || name.contains("은행") || name.contains("보험") -> Icons.Filled.AccountBalance
    name.contains("헬스케어") || name.contains("바이오") || name.contains("의료") || name.contains("제약") -> Icons.Filled.LocalHospital
    name.contains("에너지") || name.contains("정유") || name.contains("신재생") -> Icons.Filled.Bolt
    name.contains("IT") || name.contains("소프트웨어") || name.contains("인터넷") || name.contains("플랫폼") || name.contains(
        "기술"
    ) || name.contains("테크") -> Icons.Filled.Computer

    name.contains("소비재") || name.contains("유통") || name.contains("식품") || name.contains("화장품") -> Icons.Filled.ShoppingCart
    name.contains("산업재") || name.contains("기계") || name.contains("조선") || name.contains("방산") || name.contains(
        "건설"
    ) -> Icons.Filled.Factory

    name.contains("통신") || name.contains("미디어") || name.contains("방송") -> Icons.Filled.CellTower
    name.contains("유틸리티") || name.contains("전력") || name.contains("가스") || name.contains("수도") -> Icons.Filled.WaterDrop
    name.contains("부동산") || name.contains("리츠") -> Icons.Filled.Home
    name.contains("자동차") || name.contains("전기차") || name.contains("모빌리티") -> Icons.Filled.DirectionsCar
    name.contains("화학") || name.contains("소재") || name.contains("철강") -> Icons.Filled.Science
    else -> Icons.Filled.Category
}

private fun riskToBadge(grade: Int) = when (grade) {
    1 -> Triple(BadgeConservative, BadgeConservativeFont, "안정형")
    2 -> Triple(BadgeConservativeGrowth, BadgeConservativeGrowthFont, "안정추구형")
    3 -> Triple(BadgeNeutral, BadgeNeutralFont, "위험중립형")
    4 -> Triple(BadgeActive, BadgeActiveFont, "적극투자형")
    else -> Triple(BadgeAggressive, BadgeAggressiveFont, "공격투자형")
}

private fun formatVolume(volume: Long): String = when {
    volume >= 100_000_000 -> "${"%.1f".format(volume / 100_000_000.0)}억"
    volume >= 10_000 -> "${"%.1f".format(volume / 10_000.0)}만"
    else -> "%,d".format(volume)
}

