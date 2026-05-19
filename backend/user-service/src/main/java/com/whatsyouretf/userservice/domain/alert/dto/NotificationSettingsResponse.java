package com.whatsyouretf.userservice.domain.alert.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.util.List;

/**
 * 알림 설정 응답 DTO (설정 그룹 단위)
 */
@Getter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class NotificationSettingsResponse {

    /** 알림 설정 목록 */
    private List<SettingItem> settings;

    @Getter
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class SettingItem {
        /** 설정 그룹 코드 */
        private String settingGroup;

        /** 그룹 표시명 */
        private String groupName;

        /** 설명 */
        private String description;

        /** 활성화 여부 */
        private Boolean isEnabled;
    }
}
