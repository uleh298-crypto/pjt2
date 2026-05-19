package com.whatsyouretf.userservice.domain.etf.repository;

import com.whatsyouretf.userservice.domain.etf.entity.EtfOtherComposition;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

/**
 * ETF 비주식 구성종목 Repository (선물, 채권, 현금 등)
 */
public interface EtfOtherCompositionRepository extends JpaRepository<EtfOtherComposition, Long> {

    /**
     * ETF ID로 비주식 구성종목 조회 (비중 높은 순)
     */
    @Query("""
        SELECT eoc FROM EtfOtherComposition eoc
        WHERE eoc.etf.id = :etfId
        ORDER BY eoc.weight DESC
        """)
    List<EtfOtherComposition> findByEtfId(@Param("etfId") Long etfId);

    /**
     * ETF ID와 자산 유형으로 조회
     */
    @Query("""
        SELECT eoc FROM EtfOtherComposition eoc
        WHERE eoc.etf.id = :etfId
          AND eoc.assetType = :assetType
        ORDER BY eoc.weight DESC
        """)
    List<EtfOtherComposition> findByEtfIdAndAssetType(
            @Param("etfId") Long etfId,
            @Param("assetType") String assetType
    );

    /**
     * ETF 티커로 비주식 구성종목 조회
     */
    @Query("""
        SELECT eoc FROM EtfOtherComposition eoc
        WHERE eoc.etf.stockCode = :ticker
        ORDER BY eoc.weight DESC
        """)
    List<EtfOtherComposition> findByEtfTicker(@Param("ticker") String ticker);
}
