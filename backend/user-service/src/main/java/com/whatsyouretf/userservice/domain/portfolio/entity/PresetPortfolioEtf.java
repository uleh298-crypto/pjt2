package com.whatsyouretf.userservice.domain.portfolio.entity;

import com.whatsyouretf.userservice.domain.etf.entity.Etf;
import jakarta.persistence.*;
import lombok.*;

/**
 * 꾸러미 ETF 구성 엔티티
 */
@Entity
@Table(name = "preset_portfolio_etfs", uniqueConstraints = {
        @UniqueConstraint(columnNames = {"preset_portfolio_id", "etf_id"})
})
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
@Builder
public class PresetPortfolioEtf {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "preset_portfolio_id", nullable = false)
    private PresetPortfolio presetPortfolio;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "etf_id", nullable = false)
    private Etf etf;
}
