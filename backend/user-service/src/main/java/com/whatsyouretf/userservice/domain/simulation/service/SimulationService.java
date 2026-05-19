package com.whatsyouretf.userservice.domain.simulation.service;

import com.whatsyouretf.userservice.domain.simulation.dto.*;

/**
 * 시뮬레이션 서비스 인터페이스
 */
public interface SimulationService {

    /**
     * 내 시뮬레이션 목록 조회
     */
    SimulationListResponse getSimulations(Long userId, int page, int size);

    /**
     * 시뮬레이션 결과 저장
     */
    SimulationSaveResponse saveSimulation(Long userId, SimulationSaveRequest request);

    /**
     * 시뮬레이션 상세 조회
     */
    SimulationDetailResponse getSimulationDetail(Long userId, Long simulationId);

    /**
     * 시뮬레이션 삭제
     */
    void deleteSimulation(Long userId, Long simulationId);

    /**
     * 포트폴리오 비교
     */
    SimulationCompareResponse comparePortfolios(Long userId, SimulationCompareRequest request);
}
