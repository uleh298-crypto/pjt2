package com.whatsyouretf.userservice.domain.auth.dto;

import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
public class LogoutRequest {

    /**
     * 현재 기기의 FCM 토큰 (선택)
     * 전달 시 해당 기기의 푸시 알림만 해제됩니다.
     */
    private String fcmToken;
}
