package com.whatsyouretf.userservice.domain.simulation.repository;

import com.whatsyouretf.userservice.domain.simulation.entity.SimulationEtfPerformance;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

/**
 * 시뮬레이션 ETF 성과 레포지토리
 */
@Repository
public interface SimulationEtfPerformanceRepository extends JpaRepository<SimulationEtfPerformance, Long> {

    /**
     * 시뮬레이션 ID로 ETF 성과 조회 (ETF 정보 페치 조인)
     */
    @Query("SELECT sep FROM SimulationEtfPerformance sep " +
            "JOIN FETCH sep.etf " +
            "WHERE sep.simulation.id = :simulationId")
    List<SimulationEtfPerformance> findBySimulationIdWithEtf(@Param("simulationId") Long simulationId);

    /**
     * 시뮬레이션 ID로 ETF 성과 삭제
     */
    void deleteBySimulationId(Long simulationId);
}
