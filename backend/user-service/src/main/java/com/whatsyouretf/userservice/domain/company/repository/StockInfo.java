package com.whatsyouretf.userservice.domain.company.repository;

import java.math.BigDecimal;

public record StockInfo(
    String ticker,
    String stockName,
    BigDecimal currentPrice,
    BigDecimal previousPrice,
    BigDecimal dailyFluctuation,
    BigDecimal dailyReturn,
    BigDecimal marketCapitalization,
    String description
) {
}
