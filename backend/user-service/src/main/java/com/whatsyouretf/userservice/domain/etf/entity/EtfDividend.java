package com.whatsyouretf.userservice.domain.etf.entity;

import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

/**
 * ETF 분배금 이력 엔티티
 */
@Entity
@Table(
    name = "etf_dividend",
    uniqueConstraints = @UniqueConstraint(
        name = "uq_etf_dividend_etf_date",
        columnNames = {"etf_id", "payment_date"}
    )
)
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class EtfDividend {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "etf_id", nullable = false)
    private Etf etf;

    /** 지급일 */
    @Column(name = "payment_date", nullable = false)
    private LocalDate paymentDate;

    /** 주당 분배금 */
    @Column(name = "amount_per_unit", precision = 14, scale = 4)
    private BigDecimal amountPerUnit;

    @Column(name = "created_at", updatable = false)
    private LocalDateTime createdAt = LocalDateTime.now();
}
