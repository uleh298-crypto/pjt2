package com.whatsyouretf.userservice.domain.alert.dto;

import com.whatsyouretf.userservice.domain.alert.entity.AlertCategory;
import com.whatsyouretf.userservice.domain.alert.entity.ReferenceType;
import com.whatsyouretf.userservice.domain.alert.entity.UserAlert;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

/**
 * 알림 목록 응답 DTO
 */
@Getter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class AlertListResponse {

    /** 알림 목록 */
    private List<AlertItem> alerts;

    /** 읽지 않은 알림 수 */
    private long unreadCount;

    @Getter
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class AlertItem {
        /** 알림 ID */
        private Long id;

        /** 알림 유형 코드 */
        private String alertTypeCode;

        /** 알림 유형명 */
        private String alertTypeName;

        /** 카테고리 */
        private AlertCategory category;

        /** 참조 대상 유형 */
        private ReferenceType referenceType;

        /** 참조 대상 ID */
        private Long referenceId;

        /** 참조 대상 티커 (ETF인 경우에만 사용) */
        private String referenceTicker;

        /** 알림 제목 */
        private String title;

        /** 알림 메시지 */
        private String message;

        /** 읽음 여부 */
        private Boolean isRead;

        /** 생성일시 */
        private LocalDateTime createdAt;

        /**
         * Entity -> DTO 변환
         */
        public static AlertItem from(UserAlert alert) {
            return from(alert, null);
        }

        /**
         * Entity -> DTO 변환 (ETF ticker 포함)
         *
         * @param alert 알림 엔티티
         * @param etfTickerMap ETF ID -> stockCode 매핑 (ETF 알림인 경우 ticker 조회용)
         */
        public static AlertItem from(UserAlert alert, Map<Long, String> etfTickerMap) {
            String ticker = null;
            if (alert.getReferenceType() == ReferenceType.ETF
                    && alert.getReferenceId() != null
                    && etfTickerMap != null) {
                ticker = etfTickerMap.get(alert.getReferenceId());
            }

            return AlertItem.builder()
                    .id(alert.getId())
                    .alertTypeCode(alert.getAlertType().getCode())
                    .alertTypeName(alert.getAlertType().getName())
                    .category(alert.getAlertType().getCategory())
                    .referenceType(alert.getReferenceType())
                    .referenceId(alert.getReferenceId())
                    .referenceTicker(ticker)
                    .title(alert.getTitle())
                    .message(alert.getMessage())
                    .isRead(alert.getIsRead())
                    .createdAt(alert.getCreatedAt())
                    .build();
        }
    }
}
