package com.d102.wye.presentation.simulation.progress.components

import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.unit.dp
import com.d102.wye.domain.state.InvestmentType
import com.d102.wye.presentation.theme.PrimaryGreen
import com.d102.wye.presentation.theme.TextTertiary
import java.text.DecimalFormat

@Composable
fun InvestmentGuideText(
    type: InvestmentType,
    amountStr: String,
    periodStr: String
) {
    // 만원 단위로 입력받아서 × 10,000
    val amountManwon = amountStr.replace(",", "").toLongOrNull() ?: 0L
    val amount = amountManwon * 10_000L
    val period = periodStr.replace(Regex("[^0-9]"), "").toIntOrNull() ?: 0

    if (amountManwon == 0L || period == 0) return

    val formatter = DecimalFormat("#,###")
    val formattedPeriod = formatter.format(period)

    // 만원 단위 포맷 함수
    fun formatWon(won: Long): String {
        val eok = won / 100_000_000L
        val man = (won % 100_000_000L) / 10_000L
        val remainder = won % 10_000L
        return when {
            eok > 0 && man > 0 -> "${formatter.format(eok)}억 ${formatter.format(man)}만원"
            eok > 0             -> "${formatter.format(eok)}억원"
            man > 0             -> "${formatter.format(man)}만원"
            else                -> "${formatter.format(remainder)}원"
        }
    }

    val highlightStyle = SpanStyle(color = PrimaryGreen, fontWeight = FontWeight.SemiBold)
    val defaultColor = TextTertiary

    val annotatedText = buildAnnotatedString {
        if (type == InvestmentType.REGULAR_SAVING) {
            val totalAmount = formatWon(amount * period)
            append("매월 ")
            withStyle(highlightStyle) { append(formatWon(amount)) }
            append("씩 ")
            withStyle(highlightStyle) { append("${formattedPeriod}개월") }
            append(" 동안 총 ")
            withStyle(highlightStyle) { append(totalAmount) }
            append("을 투자하게 됩니다.")
        } else {
            append("초기 ")
            withStyle(highlightStyle) { append(formatWon(amount)) }
            append("을 ")
            withStyle(highlightStyle) { append("${formattedPeriod}개월") }
            append(" 동안 투자하게 됩니다.")
        }
    }

    Text(
        text = annotatedText,
        style = MaterialTheme.typography.bodyMedium,
        color = defaultColor,
        modifier = Modifier.padding(vertical = 8.dp, horizontal = 4.dp)
    )
}