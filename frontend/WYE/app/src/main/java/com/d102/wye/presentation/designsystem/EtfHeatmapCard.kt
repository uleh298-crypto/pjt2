package com.d102.wye.presentation.designsystem

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.d102.wye.presentation.theme.*

/**
 * ETF 히트맵 카드
 *
 * 등락률에 따라 배경색이 변하는 카드 컴포넌트.
 * - 상승(+) → 빨강 계열 [EtfRise]
 * - 하락(-) → 파랑 계열 [EtfFall]
 * - 보합(0) → 회색 [EtfNeutral]
 *
 * @param etfName       ETF 이름 (예: "KODEX 200")
 * @param changeRate    등락률 (예: 1.24 → "+1.24%", -0.98 → "-0.98%")
 * @param modifier      사이즈/위치 조정용 Modifier (Modifier.weight 등)
 * @param onClick       카드 클릭 콜백
 */
@Composable
fun EtfHeatmapCard(
    etfName: String,
    changeRate: Double,
    modifier: Modifier = Modifier,
    onClick: () -> Unit = {},
) {
    val bgColor = when {
        changeRate > 0 -> EtfRise
        changeRate < 0 -> EtfFall
        else           -> EtfNeutral
    }
    val textColor = if (changeRate == 0.0) Color.Black else Color.White

    // 등락률 크기에 비례해 알파(채도) 조절 (0.4 ~ 1.0)
    val alpha = (0.4f + (kotlin.math.abs(changeRate) / 5.0).coerceAtMost(0.6)).toFloat()
    val cardColor = bgColor.copy(alpha = alpha)

    val rateText = if (changeRate >= 0) "+%.2f%%".format(changeRate)
                   else "%.2f%%".format(changeRate)

    Box(
        modifier = modifier
            .clip(RoundedCornerShape(8.dp))
            .background(cardColor)
            .clickable(onClick = onClick)
            .padding(12.dp),
    ) {
        // ETF 이름 (상단 왼쪽)
        Text(
            text = etfName,
            color = textColor,
            fontSize = 14.sp,
            fontWeight = FontWeight.Medium,
            maxLines = 2,
            overflow = TextOverflow.Ellipsis,
            modifier = Modifier.align(Alignment.TopStart),
        )

        // 등락률 (하단 왼쪽)
        Text(
            text = rateText,
            color = textColor,
            fontSize = 14.sp,
            fontWeight = FontWeight.Bold,
            modifier = Modifier.align(Alignment.BottomStart),
        )
    }
}

// ── 히트맵 그리드 (피그마 10개 트리맵 레이아웃) ───────────────────

/**
 * ETF 히트맵 그리드 (피그마 레이아웃 고정)
 *
 * Row 1 (tall): KODEX 200 (large) | [TIGER 미국S&P500 / [KODEX 인버스 | KODEX 반도체]]
 * Row 2 (small): 4등분 — TIGER 나스닥100 | KODEX 레버리지 | KODEX 2차전지 | SOL 미국배당
 * Row 3 (mid):  TIGER 차이나전기차SOL (넓음) | ACE 미국 S&P500
 *
 * @param items  (etfName, changeRate) 10개 리스트
 */
@Composable
fun EtfHeatmapGrid(
    items: List<Pair<String, Double>>,
    modifier: Modifier = Modifier,
    onCardClick: (String) -> Unit = {},
) {
    val safeItems = items + List(maxOf(0, 10 - items.size)) { "" to 0.0 }

    Column(modifier = modifier, verticalArrangement = Arrangement.spacedBy(4.dp)) {
        // ── Row 1: KODEX 200 (큰 카드) + 우측 S&P500 / [인버스|반도체] ──
        Row(
            horizontalArrangement = Arrangement.spacedBy(4.dp),
            modifier = Modifier.fillMaxWidth().height(200.dp),
        ) {
            // 왼쪽 큰 카드
            EtfHeatmapCard(
                etfName = safeItems[0].first,
                changeRate = safeItems[0].second,
                modifier = Modifier.weight(1f).fillMaxHeight(),
                onClick = { onCardClick(safeItems[0].first) },
            )
            // 오른쪽 열 — S&P500(위) + [인버스|반도체](아래)
            Column(
                modifier = Modifier.weight(1f).fillMaxHeight(),
                verticalArrangement = Arrangement.spacedBy(4.dp),
            ) {
                EtfHeatmapCard(
                    etfName = safeItems[1].first,
                    changeRate = safeItems[1].second,
                    modifier = Modifier.fillMaxWidth().weight(1f),
                    onClick = { onCardClick(safeItems[1].first) },
                )
                Row(
                    horizontalArrangement = Arrangement.spacedBy(4.dp),
                    modifier = Modifier.fillMaxWidth().weight(1f),
                ) {
                    EtfHeatmapCard(
                        etfName = safeItems[2].first,
                        changeRate = safeItems[2].second,
                        modifier = Modifier.weight(1f).fillMaxHeight(),
                        onClick = { onCardClick(safeItems[2].first) },
                    )
                    EtfHeatmapCard(
                        etfName = safeItems[3].first,
                        changeRate = safeItems[3].second,
                        modifier = Modifier.weight(1f).fillMaxHeight(),
                        onClick = { onCardClick(safeItems[3].first) },
                    )
                }
            }
        }

        // ── Row 2: 4등분 작은 카드 ────────────────────────────────────
        Row(
            horizontalArrangement = Arrangement.spacedBy(4.dp),
            modifier = Modifier.fillMaxWidth().height(100.dp),
        ) {
            listOf(safeItems[4], safeItems[5], safeItems[6], safeItems[7]).forEach { (name, rate) ->
                EtfHeatmapCard(
                    etfName = name,
                    changeRate = rate,
                    modifier = Modifier.weight(1f).fillMaxHeight(),
                    onClick = { onCardClick(name) },
                )
            }
        }

        // ── Row 3: 차이나전기차(넓음) | ACE S&P500 ────────────────────
        Row(
            horizontalArrangement = Arrangement.spacedBy(4.dp),
            modifier = Modifier.fillMaxWidth().height(100.dp),
        ) {
            EtfHeatmapCard(
                etfName = safeItems[8].first,
                changeRate = safeItems[8].second,
                modifier = Modifier.weight(1f).fillMaxHeight(),
                onClick = { onCardClick(safeItems[8].first) },
            )
            EtfHeatmapCard(
                etfName = safeItems[9].first,
                changeRate = safeItems[9].second,
                modifier = Modifier.weight(1f).fillMaxHeight(),
                onClick = { onCardClick(safeItems[9].first) },
            )
        }
    }
}

@Preview(showBackground = true, backgroundColor = 0xFFF5F0E8)
@Composable
private fun EtfHeatmapGridPreview() {
    EtfHeatmapGrid(
        items = listOf(
            "KODEX 200"             to  1.24,
            "TIGER 미국 S&P500"    to  0.85,
            "KODEX 인버스"         to -0.98,
            "KODEX 반도체"         to  1.10,
            "TIGER 나스닥100"      to  0.42,
            "KODEX 레버리지"       to -0.12,
            "KODEX 2차전지"        to  2.35,
            "SOL 미국배당"         to  0.68,
            "TIGER 차이나전기차SOL" to -1.45,
            "ACE 미국 S&P500"      to  0.91,
        ),
        modifier = Modifier.padding(16.dp),
    )
}
