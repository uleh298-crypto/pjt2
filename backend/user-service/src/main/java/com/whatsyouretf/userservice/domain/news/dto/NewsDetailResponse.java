package com.whatsyouretf.userservice.domain.news.dto;

import com.whatsyouretf.userservice.domain.news.entity.NewsArticle;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.List;

/**
 * 뉴스 상세 응답 DTO
 */
@Getter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class NewsDetailResponse {

    /** 뉴스 ID */
    private Long id;

    /** 뉴스 제목 */
    private String title;

    /** 뉴스 본문 */
    private String content;

    /** 언론사명 */
    private String source;

    /** 원본 URL */
    private String sourceUrl;

    /** 썸네일 URL */
    private String thumbnailUrl;

    /** 카테고리 코드 */
    private String categoryCode;

    /** 카테고리명 */
    private String categoryName;

    /** 발행일시 */
    private LocalDateTime publishedAt;

    /** AI 핵심 요약 (bullet points) */
    private List<String> aiSummary;

    /** 키워드 태그 */
    private List<String> keywords;

    /** 관련 ETF 목록 */
    private List<RelatedEtfResponse> relatedEtfs;

    /**
     * Entity -> DTO 변환
     */
    public static NewsDetailResponse from(
            NewsArticle article,
            List<String> aiSummary,
            List<String> keywords,
            List<RelatedEtfResponse> relatedEtfs
    ) {
        return NewsDetailResponse.builder()
                .id(article.getId())
                .title(article.getTitle())
                .content(article.getContent())
                .source(article.getSource())
                .sourceUrl(article.getSourceUrl())
                .thumbnailUrl(article.getThumbnailUrl())
                .categoryCode(article.getCategory() != null ? article.getCategory().getCode() : null)
                .categoryName(article.getCategory() != null ? article.getCategory().getName() : null)
                .publishedAt(article.getPublishedAt())
                .aiSummary(aiSummary)
                .keywords(keywords)
                .relatedEtfs(relatedEtfs)
                .build();
    }
}
