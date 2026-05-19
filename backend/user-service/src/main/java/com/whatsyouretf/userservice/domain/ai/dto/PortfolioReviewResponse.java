package com.whatsyouretf.userservice.domain.ai.dto;

import com.whatsyouretf.userservice.domain.ai.entity.PortfolioAiFeedback;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.List;

/**
 * 포트폴리오 AI 리뷰 응답 DTO
 */
@Getter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class PortfolioReviewResponse {

    /** 리뷰 ID */
    private Long reviewId;

    /** 진단 결과 헤드라인 */
    private String headline;

    /** 서브 헤드라인 */
    private String subHeadline;

    /** 분석 키워드 */
    private List<String> keywords;

    /** 종합 분석 결과 */
    private String analysis;

    /** 사용된 LLM 모델 */
    private String llmModel;

    /** 생성일시 */
    private LocalDateTime createdAt;

    /**
     * 처리 중 응답 생성 (분석이 아직 완료되지 않은 경우)
     */
    public static PortfolioReviewResponse processing(Long reviewId) {
        return PortfolioReviewResponse.builder()
                .reviewId(reviewId)
                .build();
    }

    /**
     * Entity -> DTO 변환
     */
    public static PortfolioReviewResponse from(PortfolioAiFeedback feedback, List<String> keywords) {
        return PortfolioReviewResponse.builder()
                .reviewId(feedback.getId())
                .headline(feedback.getHeadline())
                .subHeadline(feedback.getSubHeadline())
                .keywords(keywords)
                .analysis(feedback.getAnalysis())
                .llmModel(feedback.getLlmModel())
                .createdAt(feedback.getCreatedAt())
                .build();
    }
}
