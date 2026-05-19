package com.d102.wye.domain.state

data class EtfFilterState(
    // 검색
    val query: String = "",

    // 즐겨찾기만 보기
    val onlyLiked: Boolean = false,

    // 검색 범위: null = 전체 / "etf" = ETF 종목명 / "stock" = 주식명
    val searchScope: String? = null,

    // 위험 분류
    val riskType: String? = null,  // "CONSERVATIVE" / "STABLE" / "MODERATE" / "ACTIVE" / "AGGRESSIVE"

    // 기초자산 (탐색 화면 상단 드롭다운)
    val assetClass: String? = null,

    // 투자 전략
    val strategy: String? = null,       // "시장대표" / "테마형" / "배당형" / "채권형"

    // 투자 테마
    val themes: Set<String> = emptySet(),

    // 배당률 범위
    val dividendRateRange: String? = null,  // "3" / "5" / "7" / "10"

    // 배당주기
    val dividendCycle: String? = null,      // "월" / "반기" / "분기" / "년"

    // 파생상품
    val hasDerivative: Boolean? = null,

    // 레버리지 / 인버스 (파생상품 O 선택 시 활성화)
    val hasLeverage: Boolean? = null,
    val hasInverse: Boolean? = null,

    // P/E 범위
    val peRange: String? = null,        // "under10" / "10-20" / "over20"

    // P/B 범위
    val pbRange: String? = null,        // "under1" / "1-3" / "over3"

    // ROE 범위
    val roeRange: String? = null,       // "under5" / "5-15" / "over15"

    // 운용보수
    val expenseRatioRange: String? = null,  // "under0.05" / "0.05-0.5" / "over0.5"

    // 순자산액
    val netAssetRange: String? = null,  // "under100" / "100-1000" / "over1000"
)
