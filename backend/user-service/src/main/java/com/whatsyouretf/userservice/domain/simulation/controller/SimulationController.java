package com.whatsyouretf.userservice.domain.simulation.controller;

import com.whatsyouretf.userservice.common.auth.CustomUserDetails;
import com.whatsyouretf.userservice.common.response.ApiResponse;
import com.whatsyouretf.userservice.domain.simulation.dto.*;
import com.whatsyouretf.userservice.domain.simulation.service.SimulationService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

/**
 * 시뮬레이션 API 컨트롤러
 */
@Tag(name = "Simulation", description = "시뮬레이션 API")
@RestController
@RequestMapping("/api/v1/simulations")
@RequiredArgsConstructor
public class SimulationController {

    private final SimulationService simulationService;

    /**
     * 내 시뮬레이션 목록 조회
     */
    @Operation(summary = "시뮬레이션 목록 조회", description = "내 시뮬레이션 목록을 조회합니다.")
    @GetMapping
    public ResponseEntity<ApiResponse<SimulationListResponse>> getSimulations(
            @AuthenticationPrincipal CustomUserDetails userDetails,
            @Parameter(description = "페이지 번호") @RequestParam(defaultValue = "0") int page,
            @Parameter(description = "페이지 크기") @RequestParam(defaultValue = "10") int size
    ) {
        SimulationListResponse response = simulationService.getSimulations(userDetails.getUserId(), page, size);
        return ResponseEntity.ok(ApiResponse.success(response));
    }

    /**
     * 시뮬레이션 결과 저장
     */
    @Operation(summary = "시뮬레이션 저장", description = "프론트엔드에서 계산한 시뮬레이션 결과를 저장합니다.")
    @PostMapping
    public ResponseEntity<ApiResponse<SimulationSaveResponse>> saveSimulation(
            @AuthenticationPrincipal CustomUserDetails userDetails,
            @Valid @RequestBody SimulationSaveRequest request
    ) {
        SimulationSaveResponse response = simulationService.saveSimulation(userDetails.getUserId(), request);
        return ResponseEntity.ok(ApiResponse.success("시뮬레이션이 저장되었습니다.", response));
    }

    /**
     * 시뮬레이션 상세 조회
     */
    @Operation(summary = "시뮬레이션 상세 조회", description = "저장된 시뮬레이션 상세 정보를 조회합니다.")
    @GetMapping("/{simulationId}")
    public ResponseEntity<ApiResponse<SimulationDetailResponse>> getSimulationDetail(
            @AuthenticationPrincipal CustomUserDetails userDetails,
            @Parameter(description = "시뮬레이션 ID") @PathVariable Long simulationId
    ) {
        SimulationDetailResponse response = simulationService.getSimulationDetail(userDetails.getUserId(), simulationId);
        return ResponseEntity.ok(ApiResponse.success(response));
    }

    /**
     * 시뮬레이션 삭제
     */
    @Operation(summary = "시뮬레이션 삭제", description = "저장된 시뮬레이션을 삭제합니다.")
    @DeleteMapping("/{simulationId}")
    public ResponseEntity<ApiResponse<Void>> deleteSimulation(
            @AuthenticationPrincipal CustomUserDetails userDetails,
            @Parameter(description = "시뮬레이션 ID") @PathVariable Long simulationId
    ) {
        simulationService.deleteSimulation(userDetails.getUserId(), simulationId);
        return ResponseEntity.ok(ApiResponse.success("시뮬레이션이 삭제되었습니다."));
    }

    /**
     * 포트폴리오 비교
     */
    @Operation(summary = "포트폴리오 비교", description = "여러 포트폴리오의 과거 수익률을 비교합니다. 결과는 저장되지 않습니다.")
    @PostMapping("/compare")
    public ResponseEntity<ApiResponse<SimulationCompareResponse>> comparePortfolios(
            @AuthenticationPrincipal CustomUserDetails userDetails,
            @Valid @RequestBody SimulationCompareRequest request
    ) {
        SimulationCompareResponse response = simulationService.comparePortfolios(userDetails.getUserId(), request);
        return ResponseEntity.ok(ApiResponse.success(response));
    }
}
