package com.whatsyouretf.userservice.domain.ai.entity;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

/**
 * AI 프롬프트 관리 엔티티
 * <p>
 * LLM 프롬프트를 버전 관리합니다.
 */
@Entity
@Table(name = "ai_prompt", uniqueConstraints = {
        @UniqueConstraint(name = "uk_prompt_version", columnNames = {"name", "version"})
})
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
@Builder
public class AiPrompt {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /** 프롬프트 이름 (portfolio_feedback, etf_analysis 등) */
    @Column(nullable = false, length = 50)
    private String name;

    /** 버전 (v1.0, v1.1 등) */
    @Column(nullable = false, length = 20)
    private String version;

    /** 프롬프트 템플릿 내용 */
    @Column(name = "prompt_template", nullable = false, columnDefinition = "TEXT")
    private String promptTemplate;

    /** 변경 사항 메모 */
    @Column(length = 200)
    private String description;

    /** 현재 활성 버전 여부 */
    @Column(name = "is_active")
    @Builder.Default
    private Boolean isActive = false;

    @Column(name = "created_at")
    @Builder.Default
    private LocalDateTime createdAt = LocalDateTime.now();
}
