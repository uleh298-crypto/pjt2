package com.whatsyouretf.userservice.domain.etf.dto;

import java.math.BigDecimal;

public record EtfTopTenListResponse(
        String ticker,
        String name,
        BigDecimal dailyReturn,
        Long volume
) {
        public static EtfTopTenListResponse from(EtfCurrentInfo currentInfo) {
                return new EtfTopTenListResponse(
                        currentInfo.ticker(),
                        currentInfo.name(),
                        currentInfo.dailyReturn(),
                        currentInfo.volume()
                );
        }
}
