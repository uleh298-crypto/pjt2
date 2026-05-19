package com.whatsyouretf.userservice.domain.ai.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.util.List;

/**
 * GMS API 요청 DTO (Anthropic Messages API format)
 */
@Getter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class GmsRequest {

    private String model;

    @JsonProperty("max_tokens")
    private Integer maxTokens;

    /** 시스템 프롬프트 (Anthropic API는 system을 별도 필드로 받음) */
    private String system;

    /** 대화 메시지 목록 */
    private List<Message> messages;

    @Getter
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class Message {
        private String role;  // "user", "assistant"
        private String content;
    }

    /**
     * 포트폴리오 분석용 요청 생성
     */
    public static GmsRequest forPortfolioAnalysis(String model, String systemPrompt, String userMessage, int maxTokens) {
        return GmsRequest.builder()
                .model(model)
                .maxTokens(maxTokens)
                .system(systemPrompt)
                .messages(List.of(
                        Message.builder().role("user").content(userMessage).build()
                ))
                .build();
    }
}
