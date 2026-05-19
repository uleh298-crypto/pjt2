package com.whatsyouretf.userservice.domain.etf.entity;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

/**
 * ETF 비주식 클러스터 매핑 엔티티
 * <p>
 * 비주식 구성종목(선물, 채권 등)의 클러스터 매핑 정보를 저장합니다.
 */
@Entity
@Table(name = "etf_other_cluster_mapping", uniqueConstraints = {
        @UniqueConstraint(columnNames = {"etf_id", "composition_id"})
})
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
@Builder
public class EtfOtherClusterMapping {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /** ETF */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "etf_id", nullable = false)
    private Etf etf;

    /** ETF 비주식 구성종목 (FK) */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "composition_id", nullable = false)
    private EtfOtherComposition composition;

    /** 섹터 (industry_classification FK) */
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
