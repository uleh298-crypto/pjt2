package com.whatsyouretf.userservice.domain.etf.entity;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

/**
 * ETF 주식 클러스터 매핑 엔티티
 * <p>
 * 클러스터 태그 → 회사 목록 조회용 매핑 정보를 저장합니다.
 */
@Entity
@Table(name = "etf_stock_cluster_mapping", uniqueConstraints = {
        @UniqueConstraint(columnNames = {"etf_id", "composition_id"})
})
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
@Builder
public class EtfStockClusterMapping {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /** ETF */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "etf_id", nullable = false)
    private Etf etf;

    /** ETF 주식 구성종목 (FK) */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "composition_id", nullable = false)
    private EtfStockComposition composition;

    /** 섹터 (industry_classification FK, Level 4) */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "sector_code", nullable = false)
    private IndustryClassification sector;

    /** 매핑 출처 (MANUAL / AI) */
    @Column(length = 20)
    @Builder.Default
    private String source = "MANUAL";

    @Column(name = "created_at")
    @Builder.Default
    private LocalDateTime createdAt = LocalDateTime.now();
}
