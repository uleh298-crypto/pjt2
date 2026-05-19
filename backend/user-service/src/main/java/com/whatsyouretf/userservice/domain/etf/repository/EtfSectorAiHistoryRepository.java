package com.whatsyouretf.userservice.domain.etf.repository;

import com.whatsyouretf.userservice.domain.etf.entity.EtfSectorAiHistory;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

/**
 * ETF 섹터 AI 분석 이력 Repository
 */
public interface EtfSectorAiHistoryRepository extends JpaRepository<EtfSectorAiHistory, Long> {

    /**
     * ETF의 특정 그룹 최신 AI 분석 조회
     */
    @Query("""
        SELECT esah FROM EtfSectorAiHistory esah
        WHERE esah.etf.id = :etfId
          AND esah.groupCode = :groupCode
        ORDER BY esah.baseDate DESC
        LIMIT 1
        """)
    Optional<EtfSectorAiHistory> findLatestByEtfIdAndGroupCode(
            @Param("etfId") Long etfId,
            @Param("groupCode") String groupCode
    );

    /**
     * ETF의 모든 그룹 최신 AI 분석 조회
     */
    @Query(value = """
        SELECT DISTINCT ON (group_code) h.*
        FROM etf_sector_ai_history AS h
                JOIN etf AS e ON h.etf_id = e.id
        WHERE e.stock_code = :ticker
        ORDER BY group_code, base_date DESC
        """, nativeQuery = true)
    List<EtfSectorAiHistory> findLatestAllByEtfTicker(@Param("ticker") String ticker);
}
