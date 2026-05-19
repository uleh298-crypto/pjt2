package com.whatsyouretf.userservice.domain.simulation.service.impl;

import com.whatsyouretf.userservice.common.exception.BusinessException;
import com.whatsyouretf.userservice.common.exception.ErrorCode;
import com.whatsyouretf.userservice.domain.simulation.dto.*;
import com.whatsyouretf.userservice.domain.simulation.entity.*;
import com.whatsyouretf.userservice.domain.simulation.repository.SimulationEtfPerformanceRepository;
import com.whatsyouretf.userservice.domain.simulation.repository.SimulationMonthlyReturnRepository;
import com.whatsyouretf.userservice.domain.simulation.repository.SimulationRepository;
import com.whatsyouretf.userservice.domain.simulation.service.SimulationService;
import com.whatsyouretf.userservice.domain.user.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.*;

/**
 * 시뮬레이션 서비스 구현체
 */
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class SimulationServiceImpl implements SimulationService {

    private final SimulationRepository simulationRepository;
    private final SimulationMonthlyReturnRepository monthlyReturnRepository;
    private final SimulationEtfPerformanceRepository etfPerformanceRepository;
    private final UserRepository userRepository;

    private static final int MAX_SIMULATIONS = 50;

    @Override
    public SimulationListResponse getSimulations(Long userId, int page, int size) {
        Pageable pageable = PageRequest.of(page, Math.min(size, 50));
        Page<Simulation> simulationPage = simulationRepository.findByUserIdOrderByCreatedAtDesc(userId, pageable);

        List<SimulationListResponse.SimulationSummary> summaries = simulationPage.getContent().stream()
                .map(SimulationListResponse.SimulationSummary::from)
                .toList();

        return SimulationListResponse.builder()
                .simulations(summaries)
                .page(page)
                .totalPages(simulationPage.getTotalPages())
                .totalElements(simulationPage.getTotalElements())
                .build();
    }

    // TODO: 팀원이 etf/portfolio repository 구현 후 활성화
    @Override
    @Transactional
    public SimulationSaveResponse saveSimulation(Long userId, SimulationSaveRequest request) {
        throw new BusinessException(ErrorCode.AI_SERVICE_UNAVAILABLE);
    }

    @Override
    public SimulationDetailResponse getSimulationDetail(Long userId, Long simulationId) {
        Simulation simulation = simulationRepository.findByIdAndUserId(simulationId, userId)
                .orElseThrow(() -> new BusinessException(ErrorCode.SIMULATION_NOT_FOUND));

        List<SimulationMonthlyReturn> monthlyReturns =
                monthlyReturnRepository.findBySimulationIdOrderByMonth(simulationId);
        List<SimulationEtfPerformance> etfPerformances =
                etfPerformanceRepository.findBySimulationIdWithEtf(simulationId);

        return SimulationDetailResponse.from(simulation, monthlyReturns, etfPerformances);
    }

    @Override
    @Transactional
    public void deleteSimulation(Long userId, Long simulationId) {
        Simulation simulation = simulationRepository.findByIdAndUserId(simulationId, userId)
                .orElseThrow(() -> new BusinessException(ErrorCode.SIMULATION_NOT_FOUND));

        simulationRepository.delete(simulation);
    }

    // TODO: 팀원이 etf/portfolio repository 구현 후 활성화
    @Override
    public SimulationCompareResponse comparePortfolios(Long userId, SimulationCompareRequest request) {
        throw new BusinessException(ErrorCode.AI_SERVICE_UNAVAILABLE);
    }

    /**
     * 기간 검증
     */
    private void validatePeriod(LocalDate startDate, LocalDate endDate) {
        if (startDate.isAfter(endDate)) {
            throw new BusinessException(ErrorCode.INVALID_SIMULATION_PERIOD);
        }
        if (endDate.isAfter(LocalDate.now())) {
            throw new BusinessException(ErrorCode.INVALID_SIMULATION_PERIOD);
        }
        if (startDate.isBefore(LocalDate.of(2010, 1, 1))) {
            throw new BusinessException(ErrorCode.INVALID_SIMULATION_PERIOD);
        }
    }
}
