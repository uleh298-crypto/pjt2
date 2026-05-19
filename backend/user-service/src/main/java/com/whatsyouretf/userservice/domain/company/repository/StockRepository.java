package com.whatsyouretf.userservice.domain.company.repository;

import com.whatsyouretf.userservice.domain.company.entity.Stock;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

/**
 * 주식 Repository
 */
@Repository
public interface StockRepository extends JpaRepository<Stock, Long> {

    /**
     * 티커로 주식 조회
     */
    Optional<Stock> findByTicker(String ticker);

    /**
     * 티커 목록으로 주식 목록 조회
     */
    List<Stock> findByTickerIn(List<String> tickers);

    /**
     * 회사 ID로 주식 조회
     */
    Optional<Stock> findByCompanyId(Long companyId);

    /**
     * 티커로 주식과 회사 정보 함께 조회 (industry 포함)
     */
    @Query("SELECT s FROM Stock s JOIN FETCH s.company c LEFT JOIN FETCH c.industry WHERE s.ticker = :ticker")
    Optional<Stock> findByTickerWithCompany(@Param("ticker") String ticker);

    /**
     * 활성화된 주식만 조회
     */
    List<Stock> findByIsActiveTrue();

    /**
     * 회사 ID 목록으로 주식 목록 조회 (배치)
     */
    List<Stock> findByCompanyIdIn(List<Long> companyIds);

    /**
     * 같은 산업 그룹의 관련 종목 조회 (자기 자신 제외, 상위 N개)
     * industry_classification 테이블의 group_name 기준
     */
    @Query("""
        SELECT s FROM Stock s
        JOIN FETCH s.company c
        JOIN FETCH c.industry ic
        JOIN IndustryClassification ic2 ON ic2.code = :industryCode
        WHERE ic.groupName = ic2.groupName
        AND s.ticker != :excludeTicker
        AND s.isActive = true
        ORDER BY s.listedShares DESC
        """)
    List<Stock> findRelatedStocksByIndustryCode(
            @Param("industryCode") String industryCode,
            @Param("excludeTicker") String excludeTicker,
            org.springframework.data.domain.Pageable pageable
    );
}
