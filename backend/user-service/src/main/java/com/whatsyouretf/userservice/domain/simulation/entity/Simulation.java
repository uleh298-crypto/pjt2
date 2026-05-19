package com.whatsyouretf.userservice.domain.simulation.entity;

import com.whatsyouretf.userservice.domain.portfolio.entity.Portfolio;
import com.whatsyouretf.userservice.domain.user.entity.User;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.CreationTimestamp;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

/**
 * 시뮬레이션 엔티티
 * 사용자가 실행한 시뮬레이션 결과를 저장
 */
@Entity
@Table(name = "simulation", indexes = {
        @Index(name = "idx_simulation_user_id", columnList = "user_id"),
        @Index(name = "idx_simulation_portfolio_id", columnList = "portfolio_id"),
        @Index(name = "idx_simulation_created_at", columnList = "created_at")
})
@Getter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Simulation {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "portfolio_id", nullable = false)
    private Portfolio portfolio;

    @Column(name = "start_date", nullable = false)
    private LocalDate startDate;

    @Column(name = "end_date", nullable = false)
    private LocalDate endDate;

    @Column(name = "initial_amount", nullable = false, precision = 18, scale = 2)
    private BigDecimal initialAmount;

    @Column(name = "final_amount", nullable = false, precision = 18, scale = 2)
    private BigDecimal finalAmount;

    @Enumerated(EnumType.STRING)
    @Column(name = "rebalancing_cycle", length = 20)
    @Builder.Default
    private RebalancingCycle rebalancingCycle = RebalancingCycle.NONE;

    @Column(name = "total_return", precision = 18, scale = 2)
    private BigDecimal totalReturn;

    @Column(name = "total_return_rate", precision = 10, scale = 4)
    private BigDecimal totalReturnRate;

    @Column(name = "annualized_return", precision = 10, scale = 4)
    private BigDecimal annualizedReturn;

    @Column(name = "max_drawdown", precision = 10, scale = 4)
    private BigDecimal maxDrawdown;

    @Column(name = "sharpe_ratio", precision = 10, scale = 4)
    private BigDecimal sharpeRatio;

    @Column(name = "volatility", precision = 10, scale = 4)
    private BigDecimal volatility;

    @OneToMany(mappedBy = "simulation", cascade = CascadeType.ALL, orphanRemoval = true)
    @Builder.Default
    private List<SimulationMonthlyReturn> monthlyReturns = new ArrayList<>();

    @OneToMany(mappedBy = "simulation", cascade = CascadeType.ALL, orphanRemoval = true)
    @Builder.Default
    private List<SimulationEtfPerformance> etfPerformances = new ArrayList<>();

    @CreationTimestamp
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    /**
     * 월별 수익률 추가
     */
    public void addMonthlyReturn(SimulationMonthlyReturn monthlyReturn) {
        monthlyReturns.add(monthlyReturn);
        monthlyReturn.setSimulation(this);
    }

    /**
     * ETF 성과 추가
     */
    public void addEtfPerformance(SimulationEtfPerformance etfPerformance) {
        etfPerformances.add(etfPerformance);
        etfPerformance.setSimulation(this);
    }
}
