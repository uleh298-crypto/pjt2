package com.whatsyouretf.userservice.domain.alert.event;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.util.List;

/**
 * 뉴스 분석 완료 이벤트
 * data-service에서 발행, user-service에서 수신
 */
@Getter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class NewsAnalyzedEvent {

    /** 이벤트 타입 */
    private String eventType;

    /** 뉴스 ID */
    private Long newsId;

    /** 뉴스 제목 */
    private String newsTitle;

    /** 뉴스 요약 */
    private String newsSummary;

    /** 영향받는 ETF ID 목록 */
    private List<Long> etfIds;

    /** 영향 유형 (POSITIVE/NEGATIVE/NEUTRAL) */
    private String influenceType;
}
