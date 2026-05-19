package com.whatsyouretf.userservice.domain.alert.dto;

import com.whatsyouretf.userservice.domain.alert.entity.DeviceType;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

/**
 * FCM 토큰 등록/삭제 요청 DTO
 */
@Getter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class FcmTokenRequest {

    /** FCM 토큰 */
    @NotBlank(message = "FCM 토큰은 필수입니다.")
    @Size(max = 500, message = "FCM 토큰은 최대 500자입니다.")
    private String token;

    /** 기기 유형 (등록 시 필수) */
    private DeviceType deviceType;
}
