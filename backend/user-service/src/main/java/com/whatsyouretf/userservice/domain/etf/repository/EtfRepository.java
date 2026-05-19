package com.whatsyouretf.userservice.domain.etf.repository;

import com.whatsyouretf.userservice.domain.etf.entity.Etf;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

/**
 * ETF Repository
 */
public interface EtfRepository extends JpaRepository<Etf, Long> {

    /**
     * 종목코드(티커)로 ETF 조회
     */
    Optional<Etf> findByStockCode(String stockCode);

    /**
     * 뉴스와 관련된 ETF 목록 조회
     * <p>
     * news_stock_mapping → stock (company_id) → etf_stock_composition → etf
     * 비중(weight_pct)이 높은 순으로 정렬
     *
     * @param newsId 뉴스 ID
     * @param limit 조회 개수
     * @return 관련 ETF 목록
     */
    @Query(value = """
        SELECT e.* FROM etf e
        WHERE e.id IN (
            SELECT DISTINCT esc.etf_id
            FROM etf_stock_composition esc
            JOIN stock s ON esc.stock_id = s.id
            JOIN news_stock_mapping nsm ON s.company_id = nsm.company_id
            WHERE nsm.news_id = :newsId
        )
        AND e.is_active = true
        ORDER BY (
            SELECT MAX(esc2.weight_pct)
            FROM etf_stock_composition esc2
            JOIN stock s2 ON esc2.stock_id = s2.id
            JOIN news_stock_mapping nsm2 ON s2.company_id = nsm2.company_id
            WHERE esc2.etf_id = e.id AND nsm2.news_id = :newsId
        ) DESC
        LIMIT :limit
        """, nativeQuery = true)
    List<Etf> findRelatedEtfsByNewsId(@Param("newsId") Long newsId, @Param("limit") int limit);


    @Query("""
        SELECT e FROM Etf e
        WHERE e.stockCode IN :tickers
        and e.isActive = true
    """)
    List<Etf> findEtfsByStockCodeInTickers(@Param("tickers") List<String> tickers);
}
