package com.whatsyouretf.userservice.domain.etf.repository;

import com.whatsyouretf.userservice.domain.etf.entity.EtfStockComposition;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

/**
 * ETF 주식 구성종목 Repository
 */
public interface EtfStockCompositionRepository extends JpaRepository<EtfStockComposition, Long> {

    /**
     * ETF의 최신 구성종목 조회 (비중 높은 순)
     */
    @Query("""
        SELECT esc FROM EtfStockComposition esc
        JOIN FETCH esc.stock s
        JOIN FETCH s.company
        WHERE esc.etf.id = :etfId
          AND esc.baseDate = (
              SELECT MAX(e.baseDate) FROM EtfStockComposition e
              WHERE e.etf.id = :etfId
          )
        ORDER BY esc.weightPct DESC
        """)
    List<EtfStockComposition> findLatestByEtfId(@Param("etfId") Long etfId);

    /**
     * ETF의 상위 N개 구성종목 조회
     */
    @Query("""
        SELECT esc FROM EtfStockComposition esc
        JOIN FETCH esc.stock s
        JOIN FETCH s.company
        WHERE esc.etf.id = :etfId
          AND esc.baseDate = (
              SELECT MAX(e.baseDate) FROM EtfStockComposition e
              WHERE e.etf.id = :etfId
          )
        ORDER BY esc.weightPct DESC
        LIMIT :limit
        """)
    List<EtfStockComposition> findTopByEtfId(@Param("etfId") Long etfId, @Param("limit") int limit);

    /**
     * ETF의 특정 그룹코드 종목들 조회 (섹터별 종목)
     */
    @Query("""
        SELECT esc FROM EtfStockComposition esc
        JOIN FETCH esc.stock s
        JOIN FETCH s.company c
        WHERE esc.etf.stockCode = :ticker
          AND c.industryGroup = :groupCode
          AND esc.baseDate = (
              SELECT MAX(e.baseDate) FROM EtfStockComposition e
              WHERE e.etf.stockCode = :ticker
          )
        ORDER BY esc.weightPct DESC
        """)
    List<EtfStockComposition> findByEtfTickerAndGroupCode(
            @Param("ticker") String ticker,
            @Param("groupCode") String groupCode
    );

    /**
     * 특정 종목이 포함된 ETF 목록 조회
     * - 각 ETF의 최신 baseDate 데이터만 조회
     * - 활성 ETF만 조회
     * - 비중 높은 순 정렬
     */
    @Query("""
        SELECT esc FROM EtfStockComposition esc
        JOIN FETCH esc.stock s
        JOIN FETCH esc.etf e
        WHERE esc.stock.ticker = :ticker
          AND e.isActive = true
          AND NOT EXISTS (
              SELECT 1 FROM EtfStockComposition e2
              WHERE e2.etf.id = esc.etf.id AND e2.baseDate > esc.baseDate
          )
        ORDER BY esc.weightPct DESC
    """)
    List<EtfStockComposition> getEtfStockCompositionByStockTicker(@Param("ticker") String ticker);

    /**
     * ETF 구성종목 ticker 목록 조회 (24시간 캐시)
     */
    @Cacheable(value = "etfTickers", key = "#etfId")
    @Query("""
        SELECT s.ticker FROM EtfStockComposition esc
        JOIN esc.stock s
        WHERE esc.etf.id = :etfId
          AND esc.baseDate = (
              SELECT MAX(e.baseDate) FROM EtfStockComposition e
              WHERE e.etf.id = :etfId
          )
        """)
    List<String> findTickersByEtfId(@Param("etfId") Long etfId);
}
