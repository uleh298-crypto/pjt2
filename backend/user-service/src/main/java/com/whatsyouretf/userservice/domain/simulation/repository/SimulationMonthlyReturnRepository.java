package com.whatsyouretf.userservice.domain.simulation.repository;

import com.whatsyouretf.userservice.domain.simulation.entity.SimulationMonthlyReturn;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

/**
 * 시뮬레이션 월별 수익률 레포지토리
 */
@Repository
public interface SimulationMonthlyReturnRepository extends JpaRepository<SimulationMonthlyReturn, Long> {

    /**
     * 시뮬레이션 ID로 월별 수익률 조회
     */
    List<SimulationMonthlyReturn> findBySimulationIdOrderByMonth(Long simulationId);

    /**
     * 시뮬레이션 ID로 월별 수익률 삭제
     */
    void deleteBySimulationId(Long simulationId);
}
