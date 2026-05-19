package com.whatsyouretf.userservice.domain.etf.entity;

import com.whatsyouretf.userservice.domain.ai.entity.AiPrompt;
import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

/**
 * ETF 섹터 버블 AI 분석 이력 엔티티
 * <p>
 * ETF 클러스터 뷰에서 섹터 버블 클릭 시 표시되는 AI 분석 결과를 저장합니다.
 * ETF 리밸런싱에 따른 분석 이력을 관리합니다.
 */
@Entity
@Table(name = "etf_sector_ai_history", indexes = {
        @Index(name = "idx_sector_ai_etf_group", columnList = "etf_id, group_code"),
        @Index(name = "idx_sector_ai_date", columnList = "etf_id, base_date DESC")
})
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
@Builder
public class EtfSectorAiHistory {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /** ETF */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "etf_id", nullable = false)
    private Etf etf;

    /** 그룹코드 */
    @Column(name = "group_code", nullable = false, length = 20)
    private String groupCode;

    /** 그룹명 */
    @Column(name = "group_name", length = 50)
    private String groupName;

    /** 비중 (%) */
    @Column(name = "weight_pct", precision = 6, scale = 3)
    private BigDecimal weightPct;

    /** 해당 섹터 종목 수 */
    @Column(name = "stock_count")
    private Integer stockCount;

    /** 상위 종목 목록 (JSON) */
    @Column(name = "top_stocks", columnDefinition = "TEXT")
    private String topStocks;

    /** AI 분석 결과 */
    @Column(name = "ai_analysis", nullable = false, columnDefinition = "TEXT")
    private String aiAnalysis;

    /** 사용된 프롬프트 (FK) */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "prompt_id")
    private AiPrompt prompt;

    /** 기준일 */
    @Column(name = "base_date", nullable = false)
    private LocalDate baseDate;

    @Column(name = "created_at")
    @Builder.Default
    private LocalDateTime createdAt = LocalDateTime.now();
}
