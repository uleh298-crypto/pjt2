package com.whatsyouretf.userservice.domain.auth.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
@Schema(description = "카카오 모바일 로그인 요청")
public class KakaoMobileLoginRequest {

    @NotBlank(message = "Access Token은 필수입니다.")
    @Schema(description = "카카오 SDK에서 발급받은 Access Token", example = "xxxxxx")
    private String accessToken;
}
