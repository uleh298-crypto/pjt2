package com.whatsyouretf.userservice.domain.ai.service.impl;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.whatsyouretf.userservice.common.exception.BusinessException;
import com.whatsyouretf.userservice.common.exception.ErrorCode;
import com.whatsyouretf.userservice.domain.ai.dto.PortfolioReviewRequest;
import com.whatsyouretf.userservice.domain.ai.dto.PortfolioReviewResponse;
import com.whatsyouretf.userservice.domain.ai.entity.AiPrompt;
import com.whatsyouretf.userservice.domain.ai.entity.PortfolioAiFeedback;
import com.whatsyouretf.userservice.domain.ai.repository.AiPromptRepository;
import com.whatsyouretf.userservice.domain.ai.repository.PortfolioAiFeedbackRepository;
import com.whatsyouretf.userservice.domain.ai.service.AiFeedbackService;
import com.whatsyouretf.userservice.domain.ai.service.LlmService;
import com.whatsyouretf.userservice.domain.user.entity.User;
import com.whatsyouretf.userservice.domain.user.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

/**
 * AI 피드백 서비스 구현체
 * <p>
 * LLM 연동을 통해 포트폴리오 분석을 수행합니다.
 */
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
@Slf4j
public class AiFeedbackServiceImpl implements AiFeedbackService {

    private final PortfolioAiFeedbackRepository feedbackRepository;
    private final AiPromptRepository promptRepository;
    private final UserRepository userRepository;
    private final LlmService llmService;
    private final ObjectMapper objectMapper;

    @Override
    @Transactional
    public PortfolioReviewResponse requestReview(Long userId, PortfolioReviewRequest request) {
        // 사용자 조회
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new BusinessException(ErrorCode.USER_NOT_FOUND));

        // 포트폴리오 비중 합계 검증
        int totalWeight = request.getPortfolio().getEtfs().stream()
                .mapToInt(PortfolioReviewRequest.EtfInfo::getWeight)
                .sum();
        if (totalWeight != 100) {
            throw new BusinessException(ErrorCode.INVALID_WEIGHT_SUM);
        }

        // 활성 프롬프트 조회
        AiPrompt prompt = promptRepository.findByNameAndIsActiveTrue("portfolio_feedback")
                .orElse(null);

        // 피드백 엔티티 생성 및 저장
        PortfolioAiFeedback feedback = PortfolioAiFeedback.builder()
                .user(user)
                .prompt(prompt)
                .build();

        feedbackRepository.save(feedback);

        // LLM 호출하여 분석 수행 (별도 트랜잭션에서 엔티티 업데이트)
        String promptTemplate = prompt != null ? prompt.getPromptTemplate() : null;
        llmService.analyzePortfolio(feedback.getId(), promptTemplate, request.getPortfolio());

        // LLM 서비스가 별도로 업데이트하므로 재조회 필요
        PortfolioAiFeedback result = feedbackRepository.findById(feedback.getId())
                .orElseThrow(() -> new BusinessException(ErrorCode.REVIEW_NOT_FOUND));

        return PortfolioReviewResponse.from(result, parseKeywords(result.getKeywords()));
    }

    /**
     * 키워드 JSON 파싱
     */
    private List<String> parseKeywords(String json) {
        if (json == null || json.isEmpty()) {
            return List.of();
        }
        try {
            return objectMapper.readValue(json, new TypeReference<>() {});
        } catch (JsonProcessingException e) {
            log.error("키워드 파싱 실패: {}", json, e);
            return List.of();
        }
    }
}
