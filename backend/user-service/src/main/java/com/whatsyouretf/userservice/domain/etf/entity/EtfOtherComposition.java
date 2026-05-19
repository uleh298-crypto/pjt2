package com.whatsyouretf.userservice.domain.etf.entity;

import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * ETF 비주식 구성종목 엔티티
 * <p>
 * ETF의 비주식 구성종목(선물, 채권, 현금, 원자재 등) 정보를 저장합니다.
 */
@Entity
@Table(name = "etf_other_composition")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
@Builder
public class EtfOtherComposition {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /** ETF */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "etf_id", nullable = false)
    private Etf etf;

    /** 자산 유형 (FUTURES / BOND / CASH / COMMODITY) */
    @Column(name = "asset_type", length = 20)
    private String assetType;

    /** 자산명 ("KOSPI200 선물", "국고채 3년") */
    @Column(name = "asset_name", length = 50)
    private String assetName;

    /** 식별자 유형 (ISIN / TICKER / CUSTOM) */
    @Column(name = "identifier_type", length = 20)
    private String identifierType;

    /** 식별자 값 */
    @Column(name = "identifier_value", length = 30)
    private String identifierValue;

    /** 표시명 (패턴 기반 변환) */
    @Column(name = "display_name", length = 100)
    private String displayName;

    /** 비중 (%) */
    @Column(precision = 6, scale = 3)
    private BigDecimal weight;

    /** 시가 */
    @Column(name = "market_value")
    private Long marketValue;

    @Column(name = "created_at")
    @Builder.Default
    private LocalDateTime createdAt = LocalDateTime.now();
}
