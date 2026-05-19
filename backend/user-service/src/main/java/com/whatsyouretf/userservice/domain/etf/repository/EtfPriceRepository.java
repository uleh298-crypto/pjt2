package com.whatsyouretf.userservice.domain.etf.repository;

import com.whatsyouretf.userservice.domain.etf.entity.EtfPrice;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

/**
 * ETF 시세 Repository
 */
@Repository
public interface EtfPriceRepository extends JpaRepository<EtfPrice, Long> {
    /**
     * 여러 ETF의 최신 시세 조회
     */
    @Query("""
        SELECT ep FROM EtfPrice ep
        JOIN FETCH ep.etf
        WHERE ep.etf.id IN :etfIds
          AND ep.tradeDate = (
              SELECT MAX(ep2.tradeDate) FROM EtfPrice ep2 WHERE ep2.etf = ep.etf
          )
        """)
    List<EtfPrice> findLatestByEtfIds(@Param("etfIds") List<Long> etfIds);

    /**
     * 최신 거래일 기준 volume 상위 10개 ETF 시세 조회 (Redis 캐시 비어있을 때 fallback용)
     */
    @Query("""
        SELECT ep FROM EtfPrice ep
        JOIN FETCH ep.etf e
        WHERE e.isActive = true
        AND ep.volume IS NOT NULL
        AND ep.tradeDate = (
            SELECT MAX(ep2.tradeDate) FROM EtfPrice ep2 WHERE ep2.etf = ep.etf
        )
        ORDER BY ep.volume DESC
        LIMIT 10
        """)
    List<EtfPrice> findTop10ByLatestVolume();
}
