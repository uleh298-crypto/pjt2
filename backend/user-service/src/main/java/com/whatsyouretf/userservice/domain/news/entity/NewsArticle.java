package com.whatsyouretf.userservice.domain.news.entity;

import com.whatsyouretf.userservice.domain.common.entity.Category;
import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

/**
 * 뉴스 기사 엔티티
 * <p>
 * 네이버 증권에서 수집한 종목 뉴스 정보를 저장합니다.
 */
@Entity
@Table(name = "news_article", indexes = {
        @Index(name = "idx_news_published_at", columnList = "published_at DESC"),
        @Index(name = "idx_news_category", columnList = "category_code")
})
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
@Builder
public class NewsArticle {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /** 뉴스 제목 */
    @Column(nullable = false, length = 500)
    private String title;

    /** 뉴스 본문 */
    @Column(columnDefinition = "TEXT")
    private String content;

    /** AI 요약 (JSON: {"bullets": ["요약1", "요약2", "요약3"]}) */
    @JdbcTypeCode(SqlTypes.JSON)
    @Column(name = "content_summary", columnDefinition = "jsonb")
    private String contentSummary;

    /** 언론사명 */
    @Column(length = 100)
    private String source;

    /** 원본 URL (unique) */
    @Column(name = "source_url", nullable = false, unique = true, length = 1000)
    private String sourceUrl;

    /** 썸네일 이미지 URL */
    @Column(name = "thumbnail_url", length = 1000)
    private String thumbnailUrl;

    /** 뉴스 카테고리 (FK -> category) */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "category_code")
    private Category category;

    /** 검색 키워드 (JSON 배열) */
    @JdbcTypeCode(SqlTypes.JSON)
    @Column(columnDefinition = "jsonb")
    private String keywords;

    /** 발행일시 */
    @Column(name = "published_at")
    private LocalDateTime publishedAt;

    /** 조회수 */
    @Column(name = "view_count")
    @Builder.Default
    private Integer viewCount = 0;

    /** 활성 상태 */
    @Column(name = "is_active")
    @Builder.Default
    private Boolean isActive = true;

    @Column(name = "created_at")
    @Builder.Default
    private LocalDateTime createdAt = LocalDateTime.now();

    /** 뉴스-종목 매핑 (양방향 관계) */
    @OneToMany(mappedBy = "newsArticle", cascade = CascadeType.ALL, orphanRemoval = true)
    @Builder.Default
    private List<NewsStockMapping> stockMappings = new ArrayList<>();

    /**
     * 조회수 증가
     */
    public void incrementViewCount() {
        this.viewCount++;
    }
}
