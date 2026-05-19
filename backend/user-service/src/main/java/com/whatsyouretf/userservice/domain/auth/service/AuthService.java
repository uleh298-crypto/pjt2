package com.whatsyouretf.userservice.domain.auth.service;

import com.whatsyouretf.userservice.domain.auth.dto.*;

public interface AuthService {

    // ========== OAuth ==========

    /**
     * 카카오 로그인 (모바일 SDK access_token 기반)
     */
    AuthResponse processKakaoLogin(String accessToken);

    // ========== 중복 체크 ==========

    /**
     * 이메일 중복 체크
     */
    boolean existsByEmail(String email);

    // ========== 이메일 회원가입 ==========

    /**
     * 회원가입 1단계: 이메일 인증 요청 (인증 이메일 발송)
     */
    void signup(SignupRequest request);

    /**
     * 회원가입 2단계: 이메일 인증 확인
     */
    void verifyEmail(EmailVerifyRequest request);

    /**
     * 회원가입 3단계: 비밀번호/닉네임 입력하여 가입 완료
     */
    AuthResponse completeSignup(SignupCompleteRequest request);

    /**
     * 인증 이메일 재발송
     */
    void resendVerificationEmail(String email);

    // ========== 이메일 로그인 ==========

    /**
     * 이메일 + 비밀번호 로그인
     */
    AuthResponse login(LoginRequest request);

    // ========== 토큰 ==========

    /**
     * Refresh Token으로 Access Token 재발급
     */
    AuthResponse refreshToken(String refreshToken);

    /**
     * 로그아웃 (Refresh Token 폐기, FCM 토큰 삭제)
     *
     * @param userId 사용자 ID
     * @param fcmToken 현재 기기의 FCM 토큰 (nullable)
     */
    void logout(Long userId, String fcmToken);

    // ========== 비밀번호 재설정 ==========

    /**
     * 비밀번호 재설정 요청 (이메일 발송)
     */
    void requestPasswordReset(String email);

    /**
     * 비밀번호 재설정 토큰 검증
     */
    boolean verifyPasswordResetToken(String email, String token);

    /**
     * 비밀번호 재설정
     */
    void resetPassword(PasswordResetConfirmRequest request);
}
