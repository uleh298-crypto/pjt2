package com.whatsyouretf.userservice.domain.ai.service;

import com.whatsyouretf.userservice.domain.ai.dto.PortfolioReviewRequest;
import com.whatsyouretf.userservice.domain.ai.dto.PortfolioReviewResponse;

/**
 * AI 피드백 서비스 인터페이스
 */
public interface AiFeedbackService {

    /**
     * 포트폴리오 AI 리뷰 요청
     *
     * @param userId  사용자 ID
     * @param request 리뷰 요청 정보
     * @return 리뷰 응답
     */
    PortfolioReviewResponse requestReview(Long userId, PortfolioReviewRequest request);
}
