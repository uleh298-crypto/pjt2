package com.d102.wye.domain.model

// GET /api/v1/etfs/{ticker} 단건 조회 API 응답
data class EtfDetail(
    val ticker: String,
    val name: String,
    val currentPrice: Long,
    val dailyFluctuation: Long,
    val dailyFluctuationRatio: Double,
    val volume: Long,
    val company: String,
    val riskGrade: Int,
    val riskType: String,
    val expenseRatio: Double,
    val per: Double,
    val pbr: Double,
    val roe: Double,
    val aum: Long,
    val listingDate: String,
    val inav: Long,
    val inavChangeAmount: Long,
    val inavChangeRate: Double,
)

// GET /api/v1/etfs/{ticker}/market-data
data class EtfMarketData(
    val ticker: String,
    val currentPrice: Long,
    val dailyReturn: Double,
    val volume: Long,
)

// GET /api/v1/etfs/{ticker}/price-history 응답 항목
data class EtfPriceData(
    val date: String,
    val stockPrice: Long,
    val dailyReturn: Double,
    val nav: Double,
)

// 차트 렌더링용 (price-history → ViewModel에서 변환, kospi/sp500은 별도 API 연결 예정)
data class EtfReturnChart(
    val navData: List<ChartPoint>,
    val priceData: List<ChartPoint>,
    val kospiData: List<ChartPoint>,
    val sp500Data: List<ChartPoint>,
)

data class ChartPoint(
    val date: String,
    val value: Double,
)

// 영향을 많이 끼치는 종목 (GET /api/v1/etfs/{ticker}/clusters → influentialStocks)
data class InfluentialStock(
    val ticker: String,
    val name: String,
    val weight: Double,
    val currentPrice: Long,
    val changeRate: Double,
)

// 지수 데이터 포인트 (GET /api/v1/index)
data class IndexPoint(
    val date:       String,  // tradingDate
    val close:      Double,
    val marketType: String,
)

// 기간별 수익률 (price-history → ViewModel에서 계산, index는 별도 API 연결 예정)
data class EtfPeriodReturn(
    val asOfDate: String,
    val nav1M: Double,   val nav3M: Double,   val nav6M: Double,
    val index1M: Double, val index3M: Double, val index6M: Double,
    val price1M: Double, val price3M: Double, val price6M: Double,
)
