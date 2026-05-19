package com.whatsyouretf.userservice.domain.user.dto;

import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
public class UserUpdateRequest {

    @Size(min = 2, max = 20, message = "닉네임은 2~20자여야 합니다.")
    @Pattern(regexp = "^[가-힣a-zA-Z0-9]{2,20}$", message = "닉네임은 한글, 영문, 숫자만 허용됩니다.")
    private String nickname;
}
