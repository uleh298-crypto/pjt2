package com.whatsyouretf.userservice.domain.user.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

/**
 * 비밀번호 변경 요청 DTO
 * <p>
 * 이메일 로그인 사용자 또는 비밀번호를 설정한 소셜 로그인 사용자가 사용합니다.
 */
@Getter
@NoArgsConstructor
@AllArgsConstructor
public class PasswordChangeRequest {

    @NotBlank(message = "현재 비밀번호는 필수입니다.")
    @Size(max = 72, message = "비밀번호는 72자 이하여야 합니다.")
    private String currentPassword;

    @NotBlank(message = "새 비밀번호는 필수입니다.")
    @Size(min = 8, max = 72, message = "비밀번호는 8~72자여야 합니다.")
    @Pattern(
        regexp = "^(?=.*[A-Za-z])(?=.*\\d)(?=.*[!@#$%^&*()_+\\-=\\[\\]{}|;':\",./<>?]).{8,72}$",
        message = "비밀번호는 영문, 숫자, 특수문자를 포함해야 합니다."
    )
    private String newPassword;

    @NotBlank(message = "새 비밀번호 확인은 필수입니다.")
    private String newPasswordConfirm;
}
