package com.whatsyouretf.userservice.domain.company.entity;

import com.whatsyouretf.userservice.domain.etf.entity.Fundamental;
import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

/**
 * 주식 정보 엔티티
 * <p>
 * 상장 주식 정보를 저장합니다. 회사 정보(company_info)와 분리된 테이블입니다.
 */
@Entity
@Table(name = "stock")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
@Builder
public class Stock {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /** 회사 정보 (FK) */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "company_id")
    private CompanyInfo company;

    /** 종목코드 (티커) */
    @Column(nullable = false, length = 20, unique = true)
    private String ticker;

    @Column
    private String description;

    /** 종가 */
    @Column(precision = 14, scale = 2)
    private BigDecimal close;

    /** 상장일 */
    @Column(name = "listing_date")
    private LocalDate listingDate;

    /** 액면가 */
    @Column(name = "face_value")
    private Integer faceValue;

    @Embedded
    private Fundamental fundamental;

    /** 상장주식수 */
    @Column(name = "listed_shares")
    private Long listedShares;

    /** 시장 유형 (KOSPI / KOSDAQ / NYSE / NASDAQ 등) */
    @Column(name = "market_type", length = 20)
    private String marketType;

    /** 활성 여부 */
    @Column(name = "is_active")
    @Builder.Default
    private Boolean isActive = true;

    @Column(name = "created_at")
    @Builder.Default
    private LocalDateTime createdAt = LocalDateTime.now();
}
