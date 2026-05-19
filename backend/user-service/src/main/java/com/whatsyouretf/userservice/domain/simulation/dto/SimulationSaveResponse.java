package com.whatsyouretf.userservice.domain.simulation.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

/**
 * 시뮬레이션 저장 응답 DTO
 */
@Getter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class SimulationSaveResponse {

    private Long simulationId;

    public static SimulationSaveResponse of(Long simulationId) {
        return SimulationSaveResponse.builder()
                .simulationId(simulationId)
                .build();
    }
}
