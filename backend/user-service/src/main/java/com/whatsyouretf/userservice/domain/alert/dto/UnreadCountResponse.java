package com.whatsyouretf.userservice.domain.alert.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

/**
 * 읽지 않은 알림 수 응답 DTO
 */
@Getter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class UnreadCountResponse {

    /** 읽지 않은 알림 수 */
    private long unreadCount;
}
