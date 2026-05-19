package com.whatsyouretf.userservice.domain.news.repository;

import com.whatsyouretf.userservice.domain.news.entity.NewsArticle;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

/**
 * 뉴스 기사 Repository
 */
@Repository
public interface NewsArticleRepository extends JpaRepository<NewsArticle, Long> {

    /**
     * 최신 뉴스 목록 조회 (활성 상태 + AI 분석 완료)
     */
    @Query("SELECT n FROM NewsArticle n LEFT JOIN FETCH n.category " +
           "WHERE n.isActive = true AND n.contentSummary IS NOT NULL " +
           "ORDER BY n.publishedAt DESC")
    Page<NewsArticle> findLatestNews(Pageable pageable);

    /**
     * 카테고리 코드별 최신 뉴스 목록 조회
     */
    @Query("SELECT n FROM NewsArticle n LEFT JOIN FETCH n.category " +
           "WHERE n.category.code = :categoryCode AND n.isActive = true AND n.contentSummary IS NOT NULL " +
           "ORDER BY n.publishedAt DESC")
    Page<NewsArticle> findByCategoryCode(@Param("categoryCode") String categoryCode, Pageable pageable);

    /**
     * 키워드 검색 (제목, 전체 카테고리)
     */
    @Query("SELECT n FROM NewsArticle n LEFT JOIN FETCH n.category WHERE n.isActive = true AND n.contentSummary IS NOT NULL " +
           "AND n.title LIKE %:keyword% " +
           "ORDER BY n.publishedAt DESC")
    Page<NewsArticle> searchByKeyword(@Param("keyword") String keyword, Pageable pageable);

    /**
     * 키워드 검색 (제목, 특정 카테고리)
     */
    @Query("SELECT n FROM NewsArticle n LEFT JOIN FETCH n.category WHERE n.isActive = true AND n.contentSummary IS NOT NULL " +
           "AND n.category.code = :categoryCode AND n.title LIKE %:keyword% " +
           "ORDER BY n.publishedAt DESC")
    Page<NewsArticle> searchByKeywordAndCategory(@Param("keyword") String keyword, @Param("categoryCode") String categoryCode, Pageable pageable);

    /**
     * 커서 기반 최신 뉴스 목록 조회 (lastPublishedAt, lastId 이후)
     */
    @Query("SELECT n FROM NewsArticle n LEFT JOIN FETCH n.category " +
           "WHERE n.isActive = true AND n.contentSummary IS NOT NULL " +
           "AND (n.publishedAt < :lastPublishedAt OR (n.publishedAt = :lastPublishedAt AND n.id < :lastId)) " +
           "ORDER BY n.publishedAt DESC, n.id DESC")
    List<NewsArticle> findLatestNewsByCursor(@Param("lastPublishedAt") java.time.LocalDateTime lastPublishedAt, @Param("lastId") Long lastId, Pageable pageable);

    /**
     * 커서 기반 최신 뉴스 목록 조회 (첫 페이지)
     */
    @Query("SELECT n FROM NewsArticle n LEFT JOIN FETCH n.category " +
           "WHERE n.isActive = true AND n.contentSummary IS NOT NULL " +
           "ORDER BY n.publishedAt DESC, n.id DESC")
    List<NewsArticle> findLatestNewsFirstPage(Pageable pageable);

    /**
     * 커서 기반 카테고리별 뉴스 목록 조회 (lastPublishedAt, lastId 이후)
     */
    @Query("SELECT n FROM NewsArticle n LEFT JOIN FETCH n.category " +
           "WHERE n.category.code = :categoryCode AND n.isActive = true AND n.contentSummary IS NOT NULL " +
           "AND (n.publishedAt < :lastPublishedAt OR (n.publishedAt = :lastPublishedAt AND n.id < :lastId)) " +
           "ORDER BY n.publishedAt DESC, n.id DESC")
    List<NewsArticle> findByCategoryCodeByCursor(@Param("categoryCode") String categoryCode, @Param("lastPublishedAt") java.time.LocalDateTime lastPublishedAt, @Param("lastId") Long lastId, Pageable pageable);

    /**
     * 커서 기반 카테고리별 뉴스 목록 조회 (첫 페이지)
     */
    @Query("SELECT n FROM NewsArticle n LEFT JOIN FETCH n.category " +
           "WHERE n.category.code = :categoryCode AND n.isActive = true AND n.contentSummary IS NOT NULL " +
           "ORDER BY n.publishedAt DESC, n.id DESC")
    List<NewsArticle> findByCategoryCodeFirstPage(@Param("categoryCode") String categoryCode, Pageable pageable);

    /**
     * ETF 관련 뉴스 조회 (ETF 구성종목의 뉴스)
     * - etf_stock_composition JOIN stock JOIN news_stock_mapping
     */
    @Query("SELECT DISTINCT n FROM NewsArticle n " +
           "LEFT JOIN FETCH n.category " +
           "JOIN n.stockMappings nsm " +
           "JOIN EtfStockComposition ec ON ec.stock.company.id = nsm.companyInfo.id " +
           "WHERE ec.etf.id = :etfId AND n.isActive = true AND n.contentSummary IS NOT NULL " +
           "ORDER BY n.publishedAt DESC")
    List<NewsArticle> findByEtfId(@Param("etfId") Long etfId, Pageable pageable);

    /**
     * 회사 ID로 종목 관련 뉴스 조회
     */
    @Query("SELECT n FROM NewsArticle n " +
           "LEFT JOIN FETCH n.category " +
           "JOIN n.stockMappings nsm " +
           "WHERE nsm.companyInfo.id = :companyId AND n.isActive = true AND n.contentSummary IS NOT NULL " +
           "ORDER BY n.publishedAt DESC")
    List<NewsArticle> findByCompanyId(@Param("companyId") Long companyId, Pageable pageable);

    /**
     * 뉴스 단건 조회 (category 포함)
     */
    @Query("SELECT n FROM NewsArticle n LEFT JOIN FETCH n.category WHERE n.id = :id")
    java.util.Optional<NewsArticle> findByIdWithCategory(@Param("id") Long id);

    /**
     * 회사 ID로 종목 관련 뉴스 개수 조회
     */
    @Query("SELECT COUNT(DISTINCT n) FROM NewsArticle n " +
           "JOIN n.stockMappings nsm " +
           "WHERE nsm.companyInfo.id = :companyId AND n.isActive = true AND n.contentSummary IS NOT NULL")
    long countByCompanyId(@Param("companyId") Long companyId);

    /**
     * ETF 관련 뉴스 개수 조회
     */
    @Query("SELECT COUNT(DISTINCT n) FROM NewsArticle n " +
           "JOIN n.stockMappings nsm " +
           "JOIN EtfStockComposition ec ON ec.stock.company.id = nsm.companyInfo.id " +
           "WHERE ec.etf.id = :etfId AND n.isActive = true AND n.contentSummary IS NOT NULL")
    long countByEtfId(@Param("etfId") Long etfId);

    /**
     * 원본 URL 존재 여부 확인
     */
    boolean existsBySourceUrl(String sourceUrl);

    /**
     * 포트폴리오 관련 뉴스 조회 (투자금액 × 종목비중 × 최신성으로 점수 계산)
     * - etf_prices의 최신 종가 사용
     * - 최신성: 1 / (1 + 경과일수 × 0.3)
     * - 점수 높은 순으로 상위 N개 반환
     */
    @Query(value = """
            WITH latest_prices AS (
                SELECT DISTINCT ON (etf_id) etf_id, close
                FROM etf_prices
                ORDER BY etf_id, trade_date DESC
            )
            SELECT n.id as newsId,
                   MAX(pe.etf_count * lp.close * ec.weight_pct * (1.0 / (1 + EXTRACT(EPOCH FROM (NOW() - n.published_at)) / 86400 * 0.3))) as relevanceScore
            FROM news_article n
            JOIN news_stock_mapping nsm ON nsm.news_id = n.id
            JOIN stock s ON s.company_id = nsm.company_id
            JOIN etf_stock_composition ec ON ec.stock_id = s.id
            JOIN portfolio_etf pe ON pe.etf_id = ec.etf_id
            JOIN latest_prices lp ON lp.etf_id = pe.etf_id
            WHERE pe.portfolio_id = :portfolioId AND n.is_active = true AND n.content_summary IS NOT NULL
            GROUP BY n.id
            ORDER BY relevanceScore DESC
            LIMIT :limit
            """, nativeQuery = true)
    List<PortfolioNewsProjection> findPortfolioNewsByRelevance(@Param("portfolioId") Long portfolioId, @Param("limit") int limit);

    /**
     * 포트폴리오 뉴스 Projection
     */
    interface PortfolioNewsProjection {
        Long getNewsId();
        java.math.BigDecimal getRelevanceScore();
    }

    /**
     * 포트폴리오 관련 뉴스 전체 조회 (투자금액 × 종목비중 × 최신성으로 점수 계산)
     * - 중복 조회 방지: 한 번에 전체 뉴스 엔티티 반환
     * - 점수 높은 순으로 상위 N개 반환 후 발행일 역순 정렬
     */
    @Query(value = """
            WITH latest_prices AS (
                SELECT DISTINCT ON (etf_id) etf_id, close
                FROM etf_prices
                ORDER BY etf_id, trade_date DESC
            ),
            scored_news AS (
                SELECT n.id,
                       MAX(pe.etf_count * lp.close * ec.weight_pct * (1.0 / (1 + EXTRACT(EPOCH FROM (NOW() - n.published_at)) / 86400 * 0.3))) as relevance_score
                FROM news_article n
                JOIN news_stock_mapping nsm ON nsm.news_id = n.id
                JOIN stock s ON s.company_id = nsm.company_id
                JOIN etf_stock_composition ec ON ec.stock_id = s.id
                JOIN portfolio_etf pe ON pe.etf_id = ec.etf_id
                JOIN latest_prices lp ON lp.etf_id = pe.etf_id
                WHERE pe.portfolio_id = :portfolioId AND n.is_active = true AND n.content_summary IS NOT NULL
                GROUP BY n.id
                ORDER BY relevance_score DESC
                LIMIT :limit
            )
            SELECT n.*
            FROM news_article n
            JOIN scored_news sn ON sn.id = n.id
            ORDER BY n.published_at DESC
            """, nativeQuery = true)
    List<NewsArticle> findPortfolioNewsWithFullData(@Param("portfolioId") Long portfolioId, @Param("limit") int limit);
}
