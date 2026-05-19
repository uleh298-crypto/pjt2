package com.whatsyouretf.userservice.domain.user.dto;

import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

/**
 * 회원 탈퇴 요청 DTO
 */
@Getter
@NoArgsConstructor
@AllArgsConstructor
public class DeleteAccountRequest {

    /**
     * 비밀번호 (이메일 로그인 사용자는 필수)
     */
    @Size(max = 72, message = "비밀번호는 72자 이하여야 합니다.")
    private String password;

    /**
     * 탈퇴 사유 (선택)
     */
    @Size(max = 500, message = "탈퇴 사유는 500자 이하여야 합니다.")
    private String reason;
}
