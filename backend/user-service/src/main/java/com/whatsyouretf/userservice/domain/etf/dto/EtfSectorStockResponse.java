package com.whatsyouretf.userservice.domain.etf.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

/**
 * 섹터 내 종목 응답 DTO
 */
@Getter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class EtfSectorStockResponse {

    /** 종목 티커 */
    private String ticker;

    /** 종목명 */
    private String name;

    /** 섹터 내 비중 (%) */
    private BigDecimal percentage;
}
