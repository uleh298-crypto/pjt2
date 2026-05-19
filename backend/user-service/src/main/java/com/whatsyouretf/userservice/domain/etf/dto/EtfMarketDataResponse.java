package com.whatsyouretf.userservice.domain.etf.dto;

import java.math.BigDecimal;

public record EtfMarketDataResponse(
        String ticker,
        BigDecimal currentPrice,
        BigDecimal dailyReturn,
        Long volume
) {
    public static EtfMarketDataResponse from(EtfCurrentInfo info) {
        if (info == null) {
            return new EtfMarketDataResponse(null, null, null, null);
        }
        return new EtfMarketDataResponse(
                info.ticker(),
                info.currentPrice(),
                info.dailyReturn(),
                info.volume()
        );
    }
}
