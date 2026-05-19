package com.whatsyouretf.userservice.domain.ai.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.util.List;

/**
 * GMS API 응답 DTO (Anthropic Messages API format)
 */
@Getter
@NoArgsConstructor
@AllArgsConstructor
@JsonIgnoreProperties(ignoreUnknown = true)
public class GmsResponse {

    private String id;

    private String type;

    private String role;

    private String model;

    private List<ContentBlock> content;

    @JsonProperty("stop_reason")
    private String stopReason;

    private Usage usage;

    @Getter
    @NoArgsConstructor
    @AllArgsConstructor
    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class ContentBlock {
        private String type;  // "text"
        private String text;
    }

    @Getter
    @NoArgsConstructor
    @AllArgsConstructor
    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class Usage {
        @JsonProperty("input_tokens")
        private Integer inputTokens;
        @JsonProperty("output_tokens")
        private Integer outputTokens;
    }

    /**
     * 첫 번째 텍스트 응답 내용 반환
     */
    public String getTextContent() {
        if (content == null || content.isEmpty()) {
            return null;
        }
        return content.stream()
                .filter(block -> "text".equals(block.getType()))
                .map(ContentBlock::getText)
                .findFirst()
                .orElse(null);
    }
}
