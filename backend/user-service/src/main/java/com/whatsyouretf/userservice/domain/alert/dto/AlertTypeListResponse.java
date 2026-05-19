package com.whatsyouretf.userservice.domain.alert.dto;

import com.whatsyouretf.userservice.domain.alert.entity.AlertCategory;
import com.whatsyouretf.userservice.domain.alert.entity.AlertType;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.util.List;

/**
 * 알림 유형 목록 응답 DTO
 */
@Getter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class AlertTypeListResponse {

    /** 알림 유형 목록 */
    private List<AlertTypeItem> alertTypes;

    @Getter
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class AlertTypeItem {
        /** 알림 유형 코드 */
        private String code;

        /** 알림 유형명 */
        private String name;

        /** 카테고리 */
        private AlertCategory category;

        /** 설명 */
        private String description;

        /**
         * Entity -> DTO 변환
         */
        public static AlertTypeItem from(AlertType alertType) {
            return AlertTypeItem.builder()
                    .code(alertType.getCode())
                    .name(alertType.getName())
                    .category(alertType.getCategory())
                    .description(alertType.getDescription())
                    .build();
        }
    }
}
