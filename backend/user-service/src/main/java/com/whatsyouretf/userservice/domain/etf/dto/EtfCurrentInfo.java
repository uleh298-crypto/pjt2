package com.whatsyouretf.userservice.domain.etf.dto;

import java.math.BigDecimal;
import java.math.RoundingMode;

public record EtfCurrentInfo(
        String ticker,
        String name,
        BigDecimal currentPrice,
        BigDecimal previousPrice,
        Long volume,
        BigDecimal nav,
        BigDecimal dailyReturn,
        BigDecimal dailyFluctuation
) {
        public static EtfCurrentInfo update(
                String ticker,
                String name,
                BigDecimal currentPrice,
                BigDecimal previousPrice,
                Long volume,
                BigDecimal nav
        ) {
                return new EtfCurrentInfo(
                        ticker,
                        name,
                        currentPrice,
                        previousPrice,
                        volume,
                        nav,
                        currentPrice.subtract(previousPrice).multiply(BigDecimal.valueOf(100L)).divide(previousPrice, 2, RoundingMode.DOWN),
                        currentPrice.subtract(previousPrice)
                );
        }

        public static EtfCurrentInfo empty() {
                return new EtfCurrentInfo(null, "", BigDecimal.valueOf(0),null,null,null,null,null);
        }
}
