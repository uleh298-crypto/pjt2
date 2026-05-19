package com.whatsyouretf.userservice.domain.etf.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

/**
 * ETF 영향력 종목 응답 DTO
 */
@Getter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class EtfInfluentialStockResponse {

    /** 종목 티커 */
    private String ticker;

    /** 종목명 */
    private String name;

    /** ETF 내 비중 (%) */
    private BigDecimal weight;

    /** 현재가 */
    private Long currentPrice;

    /** 등락률 (%) */
    private BigDecimal changeRate;
}
