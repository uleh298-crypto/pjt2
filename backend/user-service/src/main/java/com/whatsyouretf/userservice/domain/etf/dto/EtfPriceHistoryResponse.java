package com.whatsyouretf.userservice.domain.etf.dto;

import com.whatsyouretf.userservice.domain.etf.entity.EtfPrice;
import lombok.Builder;
import lombok.Getter;

import java.math.BigDecimal;
import java.time.LocalDate;

@Getter
@Builder
public class EtfPriceHistoryResponse {
    private LocalDate date;
    private BigDecimal stockPrice;
    private BigDecimal dailyReturn;
    private BigDecimal nav;

    public static EtfPriceHistoryResponse from(EtfPrice etfPrice) {
        return EtfPriceHistoryResponse.builder()
                .date(etfPrice.getTradeDate())
                .stockPrice(etfPrice.getClose())
                .dailyReturn(etfPrice.getChangeRate())
                .nav(etfPrice.getNav())
                .build();
    }
}
