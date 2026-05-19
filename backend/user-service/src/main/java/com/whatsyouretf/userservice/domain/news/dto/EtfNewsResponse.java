package com.whatsyouretf.userservice.domain.news.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

/**
 * ETF 관련 뉴스 응답 DTO
 */
@Getter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class EtfNewsResponse {

    /** ETF 정보 */
    private EtfInfo etf;

    /** 뉴스 목록 */
    private List<EtfNewsItem> news;

    /** 전체 뉴스 개수 */
    private long totalCount;

    @Getter
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class EtfInfo {
        private Long id;
        private String ticker;
        private String name;
    }

    @Getter
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class EtfNewsItem {
        /** 뉴스 ID */
        private Long id;

        /** 뉴스 제목 */
        private String title;

        /** 언론사명 */
        private String source;

        /** 발행일시 */
        private LocalDateTime publishedAt;

        /** 관련 종목 (ETF 구성종목 중) */
        private RelatedStockInfo relatedStock;
    }

    @Getter
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class RelatedStockInfo {
        /** 종목 코드 */
        private String stockCode;

        /** 종목명 */
        private String companyName;

        /** ETF 내 비중 */
        private BigDecimal weightPct;
    }
}
