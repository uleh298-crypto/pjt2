package com.whatsyouretf.userservice.domain.etf.entity;

import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

/**
 * ETF 일별 시세 엔티티
 */
@Entity
@Table(name = "etf_prices", uniqueConstraints = {
    @UniqueConstraint(columnNames = {"etf_id", "trade_date"})
})
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
@Builder
public class EtfPrice {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "etf_id", nullable = false)
    private Etf etf;

    /** 거래일 */
    @Column(name = "trade_date", nullable = false)
    private LocalDate tradeDate;

    /** 종가 */
    @Column(precision = 14, scale = 2)
    private BigDecimal close;

    /** NAV */
    @Column(precision = 14, scale = 2)
    private BigDecimal nav;

    /** 거래량 */
    private Long volume;

    /** 등락률 (%) */
    @Column(name = "change_rate", precision = 8, scale = 4)
    private BigDecimal changeRate;

    @Column(name = "created_at")
    @Builder.Default
    private LocalDateTime createdAt = LocalDateTime.now();
}
