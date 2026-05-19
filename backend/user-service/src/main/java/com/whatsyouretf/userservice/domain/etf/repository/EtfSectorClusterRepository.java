package com.whatsyouretf.userservice.domain.etf.repository;

import com.whatsyouretf.userservice.domain.etf.entity.EtfSectorCluster;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

/**
 * ETF 섹터 클러스터 Repository
 */
public interface EtfSectorClusterRepository extends JpaRepository<EtfSectorCluster, Long> {

    /**
     * ETF의 최신 섹터 클러스터 조회 (GROUP_CODE 기준) - 시장형 ETF용
     */
    @Query("""
        SELECT esc FROM EtfSectorCluster esc
        WHERE esc.etf.stockCode = :ticker
          AND esc.clusterType = 'GROUP_CODE'
          AND esc.baseDate = (
              SELECT MAX(e.baseDate) FROM EtfSectorCluster e
              WHERE e.etf.stockCode = :ticker AND e.clusterType = 'GROUP_CODE'
          )
        ORDER BY esc.weightPct DESC
        """)
    List<EtfSectorCluster> findLatestByEtfTicker(@Param("ticker") String ticker);

    /**
     * ETF의 최신 섹터 클러스터 조회 (클러스터 타입 지정)
     * - 테마형: SUB_SECTOR
     * - 시장형: GROUP_CODE
     */
    @Query("""
        SELECT esc FROM EtfSectorCluster esc
        WHERE esc.etf.stockCode = :ticker
          AND esc.clusterType = :clusterType
          AND esc.baseDate = (
              SELECT MAX(e.baseDate) FROM EtfSectorCluster e
              WHERE e.etf.stockCode = :ticker AND e.clusterType = :clusterType
          )
        ORDER BY esc.weightPct DESC
        """)
    List<EtfSectorCluster> findLatestByEtfTickerAndClusterType(
            @Param("ticker") String ticker,
            @Param("clusterType") String clusterType
    );
}
