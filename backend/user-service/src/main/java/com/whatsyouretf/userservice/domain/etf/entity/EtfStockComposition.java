package com.whatsyouretf.userservice.domain.etf.entity;

import com.whatsyouretf.userservice.domain.company.entity.Stock;
import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

/**
 * ETF 주식 구성종목 엔티티
 * <p>
 * ETF의 주식 구성종목 및 비중 정보를 저장합니다.
 * 비주식 구성종목(선물, 채권, 현금 등)은 EtfOtherComposition에 저장됩니다.
 */
@Entity
@Table(name = "etf_stock_composition")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
@Builder
public class EtfStockComposition {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /** ETF */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "etf_id", nullable = false)
    private Etf etf;

    /** 구성종목 주식 (FK) */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "stock_id")
    private Stock stock;

    /** 비중 (%) */
    @Column(name = "weight_pct", precision = 6, scale = 3)
    private BigDecimal weightPct;

    /** 기준일 */
    @Column(name = "base_date", nullable = false)
    private LocalDate baseDate;

    @Column(name = "created_at")
    @Builder.Default
    private LocalDateTime createdAt = LocalDateTime.now();
}
