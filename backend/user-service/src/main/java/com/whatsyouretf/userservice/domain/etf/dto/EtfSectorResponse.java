package com.whatsyouretf.userservice.domain.etf.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.util.List;

/**
 * ETF 섹터 클러스터 응답 DTO (버블 차트용)
 */
@Getter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class EtfSectorResponse {

    /** 섹터명 */
    private String name;

    /** 비중 (%) */
    private BigDecimal percentage;

    /** 섹터 내 종목 목록 */
    private List<EtfSectorStockResponse> stocks;

    /** AI 분석 텍스트 (없으면 null) */
    private String aiAnalysis;

    /** 자산 유형 (FUTURES, ETF, BOND, CASH / 주식 섹터는 null) */
    private String assetType;
}
