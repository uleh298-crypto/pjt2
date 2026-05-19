package com.whatsyouretf.userservice.domain.portfolio.entity;

import com.whatsyouretf.userservice.domain.etf.entity.Etf;
import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.Cascade;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * 포트폴리오 ETF 구성 엔티티
 * <p>
 * 포트폴리오 내 ETF 구성 및 비중 정보를 저장합니다.
 */
@Entity
@Table(name = "portfolio_etf", uniqueConstraints = {
        @UniqueConstraint(columnNames = {"portfolio_id", "etf_id"})
})
@Getter
@Setter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
@Builder
public class PortfolioEtf {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /** 포트폴리오 */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "portfolio_id", nullable = false)
    private Portfolio portfolio;

    /** ETF */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "etf_id", nullable = false)
    private Etf etf;

    /** etf 갯수 */
    @Column(name = "etf_count", scale = 3)
    private BigDecimal etfCount;

    @Column(name = "created_at")
    @Builder.Default
    private LocalDateTime createdAt = LocalDateTime.now();

    public static PortfolioEtf createPortfolioEtf(
            Portfolio portfolio,
            Etf etf,
            BigDecimal etfCount
    ) {
        PortfolioEtf portfolioEtf = new PortfolioEtf();
        portfolioEtf.portfolio = portfolio;
        portfolioEtf.etf = etf;
        portfolioEtf.etfCount = etfCount;
        return portfolioEtf;
    }
  }
