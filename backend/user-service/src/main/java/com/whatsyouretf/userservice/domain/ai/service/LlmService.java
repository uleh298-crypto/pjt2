package com.whatsyouretf.userservice.domain.ai.service;

import com.whatsyouretf.userservice.domain.ai.dto.PortfolioReviewRequest;

/**
 * LLM 서비스 인터페이스
 */
public interface LlmService {

    /**
     * 포트폴리오 AI 분석 요청 (동기)
     *
     * @param feedbackId     피드백 ID (결과 저장용)
     * @param promptTemplate 프롬프트 템플릿
     * @param portfolio      포트폴리오 정보
     */
    void analyzePortfolio(Long feedbackId, String promptTemplate, PortfolioReviewRequest.PortfolioInfo portfolio);
}
