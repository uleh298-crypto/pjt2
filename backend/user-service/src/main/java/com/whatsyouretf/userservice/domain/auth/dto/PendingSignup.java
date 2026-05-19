package com.whatsyouretf.userservice.domain.auth.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.io.Serializable;

/**
 * Redis에 임시 저장할 회원가입 정보
 */
@Getter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class PendingSignup implements Serializable {

    private String email;
    private String encodedPassword; // BCrypt 해시된 비밀번호
    private String nickname;
}
