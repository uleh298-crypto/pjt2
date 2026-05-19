package com.whatsyouretf.userservice.domain.simulation.entity;

import com.whatsyouretf.userservice.domain.etf.entity.Etf;
import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal;

/**
 * 시뮬레이션 ETF별 성과 엔티티
 */
@Entity
@Table(name = "simulation_etf_performance", indexes = {
        @Index(name = "idx_etf_performance_simulation_id", columnList = "simulation_id")
})
@Getter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class SimulationEtfPerformance {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Setter
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "simulation_id", nullable = false)
    private Simulation simulation;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "etf_id", nullable = false)
    private Etf etf;

    @Column(name = "weight_pct", nullable = false, precision = 5, scale = 2)
    private BigDecimal weightPct;

    @Column(name = "return_rate", precision = 10, scale = 4)
    private BigDecimal returnRate;

    @Column(name = "contribution", precision = 10, scale = 4)
    private BigDecimal contribution;
}
