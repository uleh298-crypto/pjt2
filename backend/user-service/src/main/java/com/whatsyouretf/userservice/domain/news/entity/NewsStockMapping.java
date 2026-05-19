package com.whatsyouretf.userservice.domain.news.entity;

import com.whatsyouretf.userservice.domain.company.entity.CompanyInfo;
import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

/**
 * 뉴스-종목 매핑 엔티티
 * <p>
 * 네이버 증권 종목뉴스 크롤링 결과로,
 * 하나의 뉴스가 여러 종목과 연결될 수 있습니다.
 */
@Entity
@Table(name = "news_stock_mapping", indexes = {
        @Index(name = "idx_news_stock_news", columnList = "news_id"),
        @Index(name = "idx_news_stock_company", columnList = "company_id")
}, uniqueConstraints = {
        @UniqueConstraint(name = "uk_news_stock", columnNames = {"news_id", "company_id"})
})
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
@Builder
public class NewsStockMapping {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /** 뉴스 기사 */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "news_id", nullable = false)
    private NewsArticle newsArticle;

    /** 관련 회사 */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "company_id", nullable = false)
    private CompanyInfo companyInfo;

    @Column(name = "created_at")
    @Builder.Default
    private LocalDateTime createdAt = LocalDateTime.now();
}
