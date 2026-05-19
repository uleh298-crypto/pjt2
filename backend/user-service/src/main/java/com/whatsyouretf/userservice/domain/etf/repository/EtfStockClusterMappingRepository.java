package com.whatsyouretf.userservice.domain.etf.repository;

import com.whatsyouretf.userservice.domain.etf.entity.EtfStockClusterMapping;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

/**
 * ETF 주식 클러스터 매핑 Repository
 * <p>
 * etf_stock_cluster_mapping 테이블을 활용하여 효율적인 섹터별 종목 조회
 */
public interface EtfStockClusterMappingRepository extends JpaRepository<EtfStockClusterMapping, Long> {

    /**
     * ETF의 특정 섹터코드(세분류) 종목들 조회
     * - 테마형 ETF용: sector_code로 직접 조회
     */
    @Query("""
        SELECT m FROM EtfStockClusterMapping m
        JOIN FETCH m.composition c
        JOIN FETCH c.stock s
        JOIN FETCH s.company
        JOIN FETCH m.sector
        WHERE m.etf.stockCode = :ticker
          AND m.sector.code = :sectorCode
        ORDER BY c.weightPct DESC
        """)
    List<EtfStockClusterMapping> findByEtfTickerAndSectorCode(
            @Param("ticker") String ticker,
            @Param("sectorCode") String sectorCode
    );

    /**
     * ETF의 특정 그룹코드 종목들 조회
     * - 시장형 ETF용: group_code로 필터
     */
    @Query("""
        SELECT m FROM EtfStockClusterMapping m
        JOIN FETCH m.composition c
        JOIN FETCH c.stock s
        JOIN FETCH s.company
        JOIN FETCH m.sector
        WHERE m.etf.stockCode = :ticker
          AND m.sector.groupCode = :groupCode
        ORDER BY c.weightPct DESC
        """)
    List<EtfStockClusterMapping> findByEtfTickerAndGroupCode(
            @Param("ticker") String ticker,
            @Param("groupCode") String groupCode
    );

    /**
     * ETF의 모든 클러스터 매핑 조회 (sector FK 포함)
     */
    @Query("""
        SELECT m FROM EtfStockClusterMapping m
        JOIN FETCH m.composition c
        JOIN FETCH c.stock s
        JOIN FETCH s.company
        JOIN FETCH m.sector
        WHERE m.etf.stockCode = :ticker
        ORDER BY c.weightPct DESC
        """)
    List<EtfStockClusterMapping> findAllByEtfTicker(@Param("ticker") String ticker);

    /**
     * 시장형 ETF: 그룹코드별 상위 N개 종목 조회
     */
    @Query(value = """
        SELECT sub.group_code AS groupCode,
               sub.code AS sectorCode,
               sub.ticker AS stockTicker,
               sub.company_name AS companyName,
               sub.weight_pct AS weightPct
        FROM (
            SELECT ic.group_code,
                   ic.code,
                   s.ticker,
                   ci.company_name,
                   esc.weight_pct,
                   ROW_NUMBER() OVER (PARTITION BY ic.group_code ORDER BY esc.weight_pct DESC) AS rn
            FROM etf_stock_cluster_mapping m
            JOIN etf_stock_composition esc ON m.composition_id = esc.id
            JOIN stock s ON esc.stock_id = s.id
            LEFT JOIN company_info ci ON s.company_id = ci.id
            JOIN industry_classification ic ON m.sector_code = ic.code
            JOIN etf e ON m.etf_id = e.id
            WHERE e.stock_code = :ticker
              AND ic.group_code IS NOT NULL
        ) sub
        WHERE sub.rn <= :limit
        ORDER BY sub.group_code, sub.weight_pct DESC
        """, nativeQuery = true)
    List<SectorStockProjection> findTopStocksByGroupCode(
            @Param("ticker") String ticker,
            @Param("limit") int limit
    );

    /**
     * 테마형 ETF: 섹터코드별 상위 N개 종목 조회
     */
    @Query(value = """
        SELECT sub.group_code AS groupCode,
               sub.code AS sectorCode,
               sub.ticker AS stockTicker,
               sub.company_name AS companyName,
               sub.weight_pct AS weightPct
        FROM (
            SELECT ic.group_code,
                   ic.code,
                   s.ticker,
                   ci.company_name,
                   esc.weight_pct,
                   ROW_NUMBER() OVER (PARTITION BY ic.code ORDER BY esc.weight_pct DESC) AS rn
            FROM etf_stock_cluster_mapping m
            JOIN etf_stock_composition esc ON m.composition_id = esc.id
            JOIN stock s ON esc.stock_id = s.id
            LEFT JOIN company_info ci ON s.company_id = ci.id
            JOIN industry_classification ic ON m.sector_code = ic.code
            JOIN etf e ON m.etf_id = e.id
            WHERE e.stock_code = :ticker
        ) sub
        WHERE sub.rn <= :limit
        ORDER BY sub.code, sub.weight_pct DESC
        """, nativeQuery = true)
    List<SectorStockProjection> findTopStocksBySectorCode(
            @Param("ticker") String ticker,
            @Param("limit") int limit
    );
}
