package com.whatsyouretf.userservice.domain.simulation.entity;

import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal;

/**
 * 시뮬레이션 월별 수익률 엔티티
 */
@Entity
@Table(name = "simulation_monthly_return", indexes = {
        @Index(name = "idx_monthly_return_simulation_id", columnList = "simulation_id")
})
@Getter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class SimulationMonthlyReturn {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Setter
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "simulation_id", nullable = false)
    private Simulation simulation;

    @Column(name = "month", nullable = false, length = 7)
    private String month;

    @Column(name = "value", nullable = false, precision = 18, scale = 2)
    private BigDecimal value;

    @Column(name = "return_rate", precision = 10, scale = 4)
    private BigDecimal returnRate;
}
