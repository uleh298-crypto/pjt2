package com.whatsyouretf.userservice.domain.ai.entity;

import com.whatsyouretf.userservice.domain.user.entity.User;
import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;

import java.time.LocalDateTime;

/**
 * 포트폴리오 AI 피드백 엔티티
 * <p>
 * 사용자 포트폴리오에 대한 AI 분석 결과를 저장합니다.
 */
@Entity
@Table(name = "portfolio_ai_feedback", indexes = {
        @Index(name = "idx_ai_feedback_user", columnList = "user_id"),
        @Index(name = "idx_ai_feedback_created", columnList = "created_at DESC")
})
@Getter
@Setter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
@Builder
public class PortfolioAiFeedback {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /** 사용자 */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    /** 사용된 프롬프트 */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "prompt_id")
    private AiPrompt prompt;

    /** 진단 결과 헤드라인 */
    @Column(length = 100)
    private String headline;

    /** 서브 헤드라인 */
    @Column(name = "sub_headline", length = 200)
    private String subHeadline;

    /** 분석 키워드 (JSON 배열) */
    @JdbcTypeCode(SqlTypes.JSON)
    @Column(columnDefinition = "jsonb")
    private String keywords;

    /** 종합 분석 결과 */
    @Column(columnDefinition = "TEXT")
    private String analysis;

    /** 사용된 LLM 모델 */
    @Column(name = "llm_model", length = 50)
    private String llmModel;

    @Column(name = "created_at")
    @Builder.Default
    private LocalDateTime createdAt = LocalDateTime.now();

    /**
     * 분석 완료 처리
     */
    public void complete(String headline, String subHeadline, String keywords,
                         String analysis, String llmModel) {
        this.headline = headline;
        this.subHeadline = subHeadline;
        this.keywords = keywords;
        this.analysis = analysis;
        this.llmModel = llmModel;
    }
}
