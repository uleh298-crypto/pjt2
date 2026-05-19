package com.whatsyouretf.userservice.domain.news.dto;

import com.whatsyouretf.userservice.domain.news.entity.NewsArticle;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * 뉴스 목록 응답 DTO
 */
@Getter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class NewsListResponse {

    /** 뉴스 ID */
    private Long id;

    /** 뉴스 제목 */
    private String title;

    /** 언론사명 */
    private String source;

    /** 썸네일 URL */
    private String thumbnailUrl;

    /** 카테고리 코드 */
    private String categoryCode;

    /** 카테고리명 */
    private String categoryName;

    /** 발행일시 */
    private LocalDateTime publishedAt;

    /**
     * Entity -> DTO 변환
     */
    public static NewsListResponse from(NewsArticle article) {
        return NewsListResponse.builder()
                .id(article.getId())
                .title(article.getTitle())
                .source(article.getSource())
                .thumbnailUrl(article.getThumbnailUrl())
                .categoryCode(article.getCategory() != null ? article.getCategory().getCode() : null)
                .categoryName(article.getCategory() != null ? article.getCategory().getName() : null)
                .publishedAt(article.getPublishedAt())
                .build();
    }
}
