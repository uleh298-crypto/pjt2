package com.whatsyouretf.userservice.domain.ai.service.impl;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.whatsyouretf.userservice.common.exception.BusinessException;
import com.whatsyouretf.userservice.common.exception.ErrorCode;
import com.whatsyouretf.userservice.domain.ai.dto.GmsRequest;
import com.whatsyouretf.userservice.domain.ai.dto.GmsResponse;
import com.whatsyouretf.userservice.domain.ai.dto.PortfolioReviewRequest;
import com.whatsyouretf.userservice.domain.ai.entity.PortfolioAiFeedback;
import com.whatsyouretf.userservice.domain.ai.repository.PortfolioAiFeedbackRepository;
import com.whatsyouretf.userservice.domain.ai.service.LlmService;
import com.whatsyouretf.userservice.domain.etf.entity.Etf;
import com.whatsyouretf.userservice.domain.etf.repository.EtfRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.reactive.function.client.WebClientResponseException;

import java.util.List;
import java.util.Map;

/**
 * LLM 서비스 구현체 (GMS API 연동)
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class LlmServiceImpl implements LlmService {

    private final WebClient gmsWebClient;
    private final PortfolioAiFeedbackRepository feedbackRepository;
    private final EtfRepository etfRepository;
    private final ObjectMapper objectMapper;

    @Value("${anthropic.model.name:${gms.model.name}}")
    private String modelName;

    @Value("${anthropic.model.max-tokens:${gms.model.max-tokens}}")
    private int maxTokens;

    private static final int MAX_RETRIES = 3;
    private static final long INITIAL_DELAY_MS = 1000; // 1초

    private static final String SYSTEM_PROMPT = """
        당신은 ETF 포트폴리오 분석 전문가입니다. 사용자의 포트폴리오를 분석하여 투자 성향과 특징을 진단해주세요.

        반드시 아래 JSON 형식으로만 응답해주세요. 다른 텍스트 없이 JSON만 반환하세요.

        ```json
        {
          "headline": "포트폴리오 특성 한 문장 (15자 내외)",
          "sub_headline": "부제목 구체적 설명 (25자 내외)",
          "keywords": ["키워드1", "키워드2", "키워드3"],
          "analysis": "종합 분석 200~300자"
        }
        ```

        - headline: 포트폴리오의 핵심 특성을 한 문장으로 표현
        - sub_headline: 헤드라인을 보완하는 구체적 설명
        - keywords: 포트폴리오 특성 키워드 3~5개
        - analysis: 포트폴리오 분석 결과 상세 설명
        """;

    @Override
    @Transactional
    public void analyzePortfolio(Long feedbackId, String promptTemplate, PortfolioReviewRequest.PortfolioInfo portfolio) {
        PortfolioAiFeedback feedback = feedbackRepository.findById(feedbackId).orElse(null);
        if (feedback == null) {
            log.error("피드백을 찾을 수 없음: feedbackId={}", feedbackId);
            return;
        }

        try {
            String userMessage = buildUserMessage(portfolio);

            GmsRequest request = GmsRequest.forPortfolioAnalysis(
                    modelName,
                    promptTemplate != null ? promptTemplate : SYSTEM_PROMPT,
                    userMessage,
                    maxTokens
            );

            GmsResponse response = callGmsApiWithRetry(request);

            if (response == null || response.getTextContent() == null) {
                throw new BusinessException(ErrorCode.AI_SERVICE_UNAVAILABLE);
            }

            String llmResponse = response.getTextContent();
            log.debug("LLM 응답: {}", llmResponse);

            // JSON 파싱 및 저장
            parseLlmResponseAndUpdate(feedback, llmResponse);

        } catch (BusinessException e) {
            throw e;
        } catch (Exception e) {
            log.error("LLM 분석 실패: feedbackId={}", feedbackId, e);
            throw new BusinessException(ErrorCode.REVIEW_GENERATION_FAILED);
        }
    }

    /**
     * 사용자 메시지 생성
     * DB에서 ETF 정보를 조회하여 프롬프트 형식에 맞게 구성
     */
    private String buildUserMessage(PortfolioReviewRequest.PortfolioInfo portfolio) {
        // ticker로 ETF 정보 조회
        List<String> tickers = portfolio.getEtfs().stream()
                .map(PortfolioReviewRequest.EtfInfo::getTicker)
                .toList();

        Map<String, Etf> etfMap = etfRepository.findEtfsByStockCodeInTickers(tickers).stream()
                .collect(java.util.stream.Collectors.toMap(Etf::getStockCode, e -> e));

        StringBuilder sb = new StringBuilder();
        sb.append("[포트폴리오 정보]\n");
        sb.append("투자금액: ").append(String.format("%,d", portfolio.getTotalAmount())).append("원\n\n");

        sb.append("[ETF 구성]\n");
        for (PortfolioReviewRequest.EtfInfo etfInfo : portfolio.getEtfs()) {
            Etf etf = etfMap.get(etfInfo.getTicker());

            sb.append("- ETF명: ").append(etfInfo.getName()).append("\n");
            sb.append("  - 비중: ").append(etfInfo.getWeight()).append("%\n");

            if (etf != null) {
                sb.append("  - 섹터: ").append(etf.getSector() != null ? etf.getSector().name() : "N/A").append("\n");
                sb.append("  - 전략: ").append(etf.getStrategyType() != null ? etf.getStrategyType() : "N/A").append("\n");
                sb.append("  - 위험등급: ").append(etf.getRiskType() != null ? etf.getRiskType().getTypeName() : "N/A").append("\n");
                sb.append("  - 배당주기: ").append(etf.getDividendFreq() != null ? etf.getDividendFreq() : "N/A").append("\n");
            }
            sb.append("\n");
        }

        return sb.toString();
    }

    /**
     * GMS API 호출 (재시도 로직 포함)
     * - 529 (Overloaded), 503 (Service Unavailable), 429 (Rate Limit) 에러 시 재시도
     * - 지수 백오프: 1초 → 2초 → 4초
     */
    private GmsResponse callGmsApiWithRetry(GmsRequest request) {
        int attempt = 0;
        long delayMs = INITIAL_DELAY_MS;

        while (attempt < MAX_RETRIES) {
            try {
                return gmsWebClient.post()
                        .uri("/v1/messages")
                        .bodyValue(request)
                        .retrieve()
                        .bodyToMono(GmsResponse.class)
                        .block();

            } catch (WebClientResponseException e) {
                int statusCode = e.getStatusCode().value();

                // 재시도 가능한 에러인지 확인 (529, 503, 429)
                if (isRetryableError(statusCode) && attempt < MAX_RETRIES - 1) {
                    attempt++;
                    log.warn("GMS API 일시적 오류 (status={}), {}ms 후 재시도 ({}/{})",
                            statusCode, delayMs, attempt, MAX_RETRIES);

                    try {
                        Thread.sleep(delayMs);
                    } catch (InterruptedException ie) {
                        Thread.currentThread().interrupt();
                        throw new BusinessException(ErrorCode.AI_SERVICE_UNAVAILABLE);
                    }

                    delayMs *= 2; // 지수 백오프
                } else {
                    log.error("GMS API 호출 실패: status={}, body={}", statusCode, e.getResponseBodyAsString());
                    throw new BusinessException(ErrorCode.AI_SERVICE_UNAVAILABLE);
                }
            }
        }

        throw new BusinessException(ErrorCode.AI_SERVICE_UNAVAILABLE);
    }

    /**
     * 재시도 가능한 HTTP 상태 코드 확인
     */
    private boolean isRetryableError(int statusCode) {
        return statusCode == 529    // Overloaded (Claude)
                || statusCode == 503 // Service Unavailable
                || statusCode == 429 // Too Many Requests
                || statusCode == 500; // Internal Server Error
    }

    /**
     * LLM 응답 파싱 및 피드백 엔티티 업데이트
     */
    private void parseLlmResponseAndUpdate(PortfolioAiFeedback feedback, String llmResponse) {
        try {
            // JSON 블록 추출 (```json ... ``` 형식 처리)
            String jsonContent = extractJsonFromResponse(llmResponse);
            JsonNode root = objectMapper.readTree(jsonContent);

            // 헤드라인, 서브헤드라인
            String headline = getTextOrNull(root, "headline");
            String subHeadline = getTextOrNull(root, "sub_headline");

            // 키워드
            String keywords = null;
            if (root.has("keywords")) {
                keywords = objectMapper.writeValueAsString(root.get("keywords"));
            }

            // 분석
            String analysis = getTextOrNull(root, "analysis");

            // 엔티티 업데이트
            feedback.complete(headline, subHeadline, keywords, analysis, modelName);
            feedbackRepository.save(feedback);

            log.info("포트폴리오 AI 분석 완료: feedbackId={}", feedback.getId());

        } catch (JsonProcessingException e) {
            log.error("LLM 응답 JSON 파싱 실패: {}", llmResponse, e);
            throw new BusinessException(ErrorCode.REVIEW_GENERATION_FAILED);
        }
    }

    /**
     * JSON 블록 추출 및 정제
     */
    private String extractJsonFromResponse(String response) {
        if (response == null) return "{}";

        String jsonContent;

        // ```json ... ``` 형식 처리
        if (response.contains("```json")) {
            int start = response.indexOf("```json") + 7;
            int end = response.indexOf("```", start);
            if (end > start) {
                jsonContent = response.substring(start, end).trim();
            } else {
                jsonContent = response.trim();
            }
        }
        // ``` ... ``` 형식 처리
        else if (response.contains("```")) {
            int start = response.indexOf("```") + 3;
            int end = response.indexOf("```", start);
            if (end > start) {
                jsonContent = response.substring(start, end).trim();
            } else {
                jsonContent = response.trim();
            }
        } else {
            jsonContent = response.trim();
        }

        // JSON 문자열 내 줄바꿈 이스케이프 처리
        // LLM이 문자열 값 내에 줄바꿈을 넣는 경우 파싱 에러 방지
        return sanitizeJsonNewlines(jsonContent);
    }

    /**
     * JSON 문자열 값 내의 줄바꿈을 이스케이프 처리
     * - JSON 구조의 줄바꿈(키 사이)은 유지
     * - 문자열 값 내의 줄바꿈만 \n으로 변환
     */
    private String sanitizeJsonNewlines(String json) {
        if (json == null) return "{}";

        StringBuilder result = new StringBuilder();
        boolean inString = false;
        boolean escaped = false;

        for (int i = 0; i < json.length(); i++) {
            char c = json.charAt(i);

            if (escaped) {
                result.append(c);
                escaped = false;
                continue;
            }

            if (c == '\\') {
                result.append(c);
                escaped = true;
                continue;
            }

            if (c == '"') {
                inString = !inString;
                result.append(c);
                continue;
            }

            // 문자열 내부의 줄바꿈은 이스케이프 처리
            if (inString && (c == '\n' || c == '\r')) {
                if (c == '\r' && i + 1 < json.length() && json.charAt(i + 1) == '\n') {
                    // \r\n은 \n 하나로 처리
                    continue;
                }
                result.append("\\n");
                continue;
            }

            result.append(c);
        }

        return result.toString();
    }

    private String getTextOrNull(JsonNode node, String fieldName) {
        if (node.has(fieldName) && !node.get(fieldName).isNull()) {
            return node.get(fieldName).asText();
        }
        return null;
    }
}
