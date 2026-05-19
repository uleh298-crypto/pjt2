package com.whatsyouretf.userservice.domain.etf.entity;

import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

/**
 * ETF 엔티티
 * <p>
 * 국내 상장 ETF 정보를 저장합니다.
 * 이 테이블의 데이터는 팀원이 담당하며, user-service에서는 조회만 합니다.
 */
@Entity
@Table(name = "etf")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
@Builder
public class Etf {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /** ETF 종목코드 (6자리, 예: 069500) */
    @Column(name = "stock_code", nullable = false, unique = true, length = 20)
    private String stockCode;

    /** ETF 명칭 (예: KODEX 200) */
    @Column(nullable = false, length = 200)
    private String name;

    /** ETF 영문명 (예: KOSPI 200 Index Tracking Fund) */
    @Column(name = "english_name", length = 200)
    private String englishName;

    /** 전략 유형 (MARKET/THEME/DIVIDEND/BOND/DERIVATIVE) */
    @Column(name = "strategy_type", length = 30)
    private String strategyType;

    /** 섹터 (반도체/2차전지/AI/배당 등) */
    @Column(length = 50)
    @Enumerated(value = EnumType.STRING)
    private EtfSector sector;

    /** 자산운용사 (KODEX/TIGER/KBSTAR 등) */
    @Column(name = "asset_manager", length = 50)
    private String assetManager;

    @Column(name = "is_krx_only")
    private Boolean isKrxOnly;

    @Column(name = "is_derivatives")
    private Boolean isDerivatives;

    /** 레버리지 ETF 여부 */
    @Column(name = "is_leveraged")
    @Builder.Default
    private Boolean isLeveraged = false;

    /** 인버스 ETF 여부 */
    @Column(name = "is_inverse")
    @Builder.Default
    private Boolean isInverse = false;

    /** 환헤지 여부 */
    @Column(name = "is_hedged")
    private Boolean isHedged;

    /** 총보수율 (%) */
    @Column(name = "expense_ratio", precision = 6, scale = 4)
    private BigDecimal expenseRatio;

    /** NAV (순자산가치) */
    @Column(precision = 14, scale = 2)
    private BigDecimal nav;

    /** AUM (순자산총액, 원) */
    private Long aum;

    /** 배당률 (%) */
    @Column(name = "dividend_yield", precision = 6, scale = 3)
    private BigDecimal dividendYield;

    /** 배당 주기 (MONTHLY/QUARTERLY/SEMI_ANNUAL/ANNUAL/NONE) */
    @Column(name = "dividend_freq", length = 10)
    private String dividendFreq;

    /** 위험등급 (HIGH_RISK/MODERATE/STABLE) */
    @Enumerated(EnumType.STRING)
    private RiskType riskType;

    /** 상장일 */
    @Column(name = "listing_date")
    private LocalDate listingDate;

    /** 상장폐지일 */
    @Column(name = "delisted_date")
    private LocalDate delistedDate;

    /** 재무 지표 */
    @Embedded
    private Fundamental fundamental;
    /** 활성 상태 */
    @Column(name = "is_active")
    @Builder.Default
    private Boolean isActive = true;

    @Column(name = "created_at")
    @Builder.Default
    private LocalDateTime createdAt = LocalDateTime.now();

    @Column(name = "updated_at")
    @Builder.Default
    private LocalDateTime updatedAt = LocalDateTime.now();

    public static Etf of(Long etfId) {
        Etf etf = new Etf();
        etf.id = etfId;
        return etf;
    }
}
