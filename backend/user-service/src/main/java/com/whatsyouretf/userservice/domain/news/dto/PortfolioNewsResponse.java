package com.whatsyouretf.userservice.domain.news.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.List;

/**
 * 포트폴리오 관련 뉴스 응답 DTO
 */
@Getter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class PortfolioNewsResponse {

    /** 포트폴리오 ID */
    private Long portfolioId;

    /** 포트폴리오 이름 */
    private String portfolioName;

    /** 뉴스 목록 */
    private List<PortfolioNewsItem> news;

    /** 갱신 시각 (매일 오전 9시) */
    private LocalDateTime updatedAt;

    @Getter
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class PortfolioNewsItem {
        /** 뉴스 ID */
        private Long id;

        /** 뉴스 제목 */
        private String title;

        /** 요약 */
        private String summary;

        /** 언론사명 */
        private String source;

        /** 썸네일 URL */
        private String thumbnailUrl;

        /** 발행일시 */
        private LocalDateTime publishedAt;
    }
}
