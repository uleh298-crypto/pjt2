package com.whatsyouretf.userservice.domain.auth.service.impl;

import com.whatsyouretf.userservice.common.exception.BusinessException;
import com.whatsyouretf.userservice.common.exception.ErrorCode;
import com.whatsyouretf.userservice.common.service.EmailService;
import com.whatsyouretf.userservice.common.util.JwtTokenUtil;
import com.whatsyouretf.userservice.domain.alert.repository.FcmTokenRepository;
import com.whatsyouretf.userservice.domain.auth.dto.*;
import com.whatsyouretf.userservice.domain.auth.service.AuthService;
import com.whatsyouretf.userservice.domain.user.entity.*;
import com.whatsyouretf.userservice.domain.user.entity.UserSocialAccount.SocialProvider;
import com.whatsyouretf.userservice.domain.user.repository.*;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.*;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.client.RestTemplate;

import java.security.SecureRandom;
import java.time.LocalDateTime;
import java.util.Map;
import java.util.concurrent.atomic.AtomicBoolean;

@Slf4j
@Service
@RequiredArgsConstructor
public class AuthServiceImpl implements AuthService {

    private final UserRepository userRepository;
    private final UserSocialAccountRepository socialAccountRepository;
    private final RefreshTokenRepository refreshTokenRepository;
    private final EmailVerificationTokenRepository emailVerificationTokenRepository;
    private final PasswordResetTokenRepository passwordResetTokenRepository;
    private final LoginHistoryRepository loginHistoryRepository;
    private final FcmTokenRepository fcmTokenRepository;
    private final JwtTokenUtil jwtTokenUtil;
    private final PasswordEncoder passwordEncoder;
    private final EmailService emailService;

    @Value("${jwt.access-token-validity}")
    private Long accessTokenValidity;

    private final RestTemplate restTemplate = new RestTemplate();
    private final SecureRandom secureRandom = new SecureRandom();

    private static final int VERIFICATION_TOKEN_LENGTH = 6;
    private static final int VERIFICATION_TOKEN_VALIDITY_MINUTES = 10;
    private static final int PASSWORD_RESET_TOKEN_VALIDITY_MINUTES = 30;

    // ========== OAuth (카카오) ==========

    @Override
    @Transactional
    public AuthResponse processKakaoLogin(String accessToken) {
        Map<String, Object> userInfo = getKakaoUserInfo(accessToken);

        AtomicBoolean isNewUser = new AtomicBoolean(false);
        User user = findOrCreateUser(userInfo, SocialProvider.KAKAO, isNewUser);

        user.updateLastLogin();
        saveLoginHistory(user, "KAKAO");

        return generateAuthResponse(user, isNewUser.get(), "KAKAO");
    }

    // ========== 중복 체크 ==========

    @Override
    public boolean existsByEmail(String email) {
        return userRepository.existsByEmail(email);
    }

    // ========== 이메일 회원가입 ==========

    /**
     * 회원가입 1단계: 이메일 인증 요청 (이메일만)
     */
    @Override
    @Transactional
    public void signup(SignupRequest request) {
        // 이메일 중복 확인
        if (userRepository.existsByEmail(request.getEmail())) {
            throw new BusinessException(ErrorCode.DUPLICATE_EMAIL);
        }

        // 기존 미인증 토큰 삭제
        emailVerificationTokenRepository.deleteByEmail(request.getEmail());

        // 인증 코드 생성 (6자리 숫자)
        String token = generateNumericToken(VERIFICATION_TOKEN_LENGTH);

        // 이메일 인증 토큰 저장
        EmailVerificationToken verificationToken = EmailVerificationToken.builder()
                .email(request.getEmail())
                .token(token)
                .expiresAt(LocalDateTime.now().plusMinutes(VERIFICATION_TOKEN_VALIDITY_MINUTES))
                .build();
        emailVerificationTokenRepository.save(verificationToken);

        // 인증 이메일 발송
        emailService.sendVerificationEmail(request.getEmail(), token);

        log.info("Signup verification email sent to: {}", request.getEmail());
    }

    /**
     * 회원가입 2단계: 이메일 인증 확인
     */
    @Override
    @Transactional
    public void verifyEmail(EmailVerifyRequest request) {
        // 인증 토큰 확인
        EmailVerificationToken verificationToken = emailVerificationTokenRepository
                .findByEmailAndToken(request.getEmail(), request.getToken())
                .orElseThrow(() -> new BusinessException(ErrorCode.INVALID_TOKEN));

        if (verificationToken.getIsVerified()) {
            throw new BusinessException(ErrorCode.INVALID_TOKEN);
        }

        if (verificationToken.isExpired()) {
            throw new BusinessException(ErrorCode.EXPIRED_TOKEN);
        }

        // 인증 토큰 검증 완료 처리
        verificationToken.verify();

        log.info("Email verified: {}", request.getEmail());
    }

    /**
     * 회원가입 3단계: 비밀번호/닉네임 입력하여 가입 완료
     */
    @Override
    @Transactional
    public AuthResponse completeSignup(SignupCompleteRequest request) {
        // 비밀번호 확인
        if (!request.getPassword().equals(request.getPasswordConfirm())) {
            throw new BusinessException(ErrorCode.PASSWORD_MISMATCH);
        }

        // 이메일 인증 여부 확인
        EmailVerificationToken verificationToken = emailVerificationTokenRepository
                .findVerifiedByEmail(request.getEmail())
                .orElseThrow(() -> new BusinessException(ErrorCode.EMAIL_NOT_VERIFIED));

        // 이메일 중복 확인 (동시 요청 대비)
        if (userRepository.existsByEmail(request.getEmail())) {
            throw new BusinessException(ErrorCode.DUPLICATE_EMAIL);
        }

        // 닉네임 중복 확인
        if (userRepository.existsByNickname(request.getNickname())) {
            throw new BusinessException(ErrorCode.DUPLICATE_NICKNAME);
        }

        // 사용자 생성
        User user = User.builder()
                .email(request.getEmail())
                .password(passwordEncoder.encode(request.getPassword()))
                .nickname(request.getNickname())
                .build();
        user = userRepository.save(user);

        // 인증 토큰 삭제 (더 이상 필요 없음)
        emailVerificationTokenRepository.delete(verificationToken);

        // 로그인 처리
        user.updateLastLogin();
        saveLoginHistory(user, "EMAIL");

        log.info("User signup completed: {}", user.getEmail());

        return generateAuthResponse(user, true, "EMAIL");
    }

    @Override
    @Transactional
    public void resendVerificationEmail(String email) {
        // 이미 가입된 사용자인지 확인
        if (userRepository.existsByEmail(email)) {
            throw new BusinessException(ErrorCode.DUPLICATE_EMAIL);
        }

        // 기존 토큰 삭제
        emailVerificationTokenRepository.deleteByEmail(email);

        // 새 인증 코드 생성
        String token = generateNumericToken(VERIFICATION_TOKEN_LENGTH);

        // 새 토큰 저장
        EmailVerificationToken verificationToken = EmailVerificationToken.builder()
                .email(email)
                .token(token)
                .expiresAt(LocalDateTime.now().plusMinutes(VERIFICATION_TOKEN_VALIDITY_MINUTES))
                .build();
        emailVerificationTokenRepository.save(verificationToken);

        // 이메일 발송
        emailService.sendVerificationEmail(email, token);

        log.info("Verification email resent to: {}", email);
    }

    // ========== 이메일 로그인 ==========

    @Override
    @Transactional
    public AuthResponse login(LoginRequest request) {
        // 사용자 조회
        User user = userRepository.findByEmail(request.getEmail())
                .orElseThrow(() -> new BusinessException(ErrorCode.INVALID_CREDENTIALS));

        // 비밀번호 확인 (소셜 로그인만 한 경우 password가 null)
        if (user.getPassword() == null) {
            throw new BusinessException(ErrorCode.INVALID_CREDENTIALS);
        }

        if (!passwordEncoder.matches(request.getPassword(), user.getPassword())) {
            throw new BusinessException(ErrorCode.INVALID_CREDENTIALS);
        }

        // 계정 활성화 확인
        if (!user.getIsActive()) {
            throw new BusinessException(ErrorCode.USER_NOT_FOUND);
        }

        // 로그인 처리
        user.updateLastLogin();
        saveLoginHistory(user, "EMAIL");

        return generateAuthResponse(user, false, "EMAIL");
    }

    // ========== 토큰 ==========

    @Override
    @Transactional
    public AuthResponse refreshToken(String refreshTokenValue) {
        if (!jwtTokenUtil.validateToken(refreshTokenValue) || !jwtTokenUtil.isRefreshToken(refreshTokenValue)) {
            throw new BusinessException(ErrorCode.INVALID_TOKEN);
        }

        RefreshToken refreshToken = refreshTokenRepository
                .findValidToken(refreshTokenValue, LocalDateTime.now())
                .orElseThrow(() -> new BusinessException(ErrorCode.INVALID_TOKEN));

        refreshToken.revoke();

        User user = refreshToken.getUser();
        String newAccessToken = jwtTokenUtil.createAccessToken(user.getId());

        return AuthResponse.ofRefresh(newAccessToken, accessTokenValidity / 1000);
    }

    @Override
    @Transactional
    public void logout(Long userId, String fcmToken) {
        // Refresh Token 폐기
        refreshTokenRepository.revokeAllByUserId(userId);

        // FCM 토큰이 전달된 경우 해당 기기의 푸시 알림 해제
        if (fcmToken != null && !fcmToken.isBlank()) {
            fcmTokenRepository.deleteByUserIdAndToken(userId, fcmToken);
            log.info("FCM token deleted for user: {}", userId);
        }

        log.info("User logged out: {}", userId);
    }

    // ========== 비밀번호 재설정 ==========

    @Override
    @Transactional
    public void requestPasswordReset(String email) {
        // 보안상 존재하지 않는 이메일이어도 동일한 응답
        User user = userRepository.findByEmail(email).orElse(null);

        if (user != null && user.getPassword() != null) {
            // 기존 토큰 무효화
            passwordResetTokenRepository.invalidateAllByUserId(user.getId());

            // 새 토큰 생성
            String token = generateNumericToken(VERIFICATION_TOKEN_LENGTH);

            PasswordResetToken resetToken = PasswordResetToken.builder()
                    .user(user)
                    .token(token)
                    .expiresAt(LocalDateTime.now().plusMinutes(PASSWORD_RESET_TOKEN_VALIDITY_MINUTES))
                    .build();
            passwordResetTokenRepository.save(resetToken);

            // 이메일 발송
            emailService.sendPasswordResetEmail(email, token);

            log.info("Password reset email sent to: {}", email);
        } else {
            log.info("Password reset requested for non-existent or social-only email: {}", email);
        }
    }

    @Override
    @Transactional(readOnly = true)
    public boolean verifyPasswordResetToken(String email, String token) {
        return passwordResetTokenRepository.findValidToken(token, LocalDateTime.now())
                .map(resetToken -> resetToken.getUser().getEmail().equals(email))
                .orElse(false);
    }

    @Override
    @Transactional
    public void resetPassword(PasswordResetConfirmRequest request) {
        // 비밀번호 확인
        if (!request.getNewPassword().equals(request.getNewPasswordConfirm())) {
            throw new BusinessException(ErrorCode.PASSWORD_MISMATCH);
        }

        // 토큰 검증
        PasswordResetToken resetToken = passwordResetTokenRepository
                .findValidToken(request.getToken(), LocalDateTime.now())
                .orElseThrow(() -> new BusinessException(ErrorCode.INVALID_TOKEN));

        if (!resetToken.getUser().getEmail().equals(request.getEmail())) {
            throw new BusinessException(ErrorCode.INVALID_TOKEN);
        }

        // 비밀번호 변경
        User user = resetToken.getUser();
        user.updatePassword(passwordEncoder.encode(request.getNewPassword()));

        // 토큰 사용 처리
        resetToken.markAsUsed();

        // 모든 refresh token 폐기 (강제 재로그인)
        refreshTokenRepository.revokeAllByUserId(user.getId());

        log.info("Password reset completed for user: {}", user.getEmail());
    }

    // ========== Private Methods ==========

    @SuppressWarnings("unchecked")
    private Map<String, Object> getKakaoUserInfo(String accessToken) {
        String userInfoUrl = "https://kapi.kakao.com/v2/user/me";

        HttpHeaders headers = new HttpHeaders();
        headers.setBearerAuth(accessToken);

        HttpEntity<String> request = new HttpEntity<>(headers);

        try {
            ResponseEntity<Map> response = restTemplate.exchange(
                    userInfoUrl, HttpMethod.GET, request, Map.class
            );
            return response.getBody();
        } catch (Exception e) {
            log.error("Failed to get Kakao user info: {}", e.getMessage());
            throw new BusinessException(ErrorCode.OAUTH_FAILED);
        }
    }

    @SuppressWarnings("unchecked")
    private User findOrCreateUser(Map<String, Object> userInfo, SocialProvider provider, AtomicBoolean isNewUser) {
        String providerUserId = String.valueOf(userInfo.get("id"));

        return socialAccountRepository
                .findByProviderAndProviderUserIdWithUser(provider, providerUserId)
                .map(UserSocialAccount::getUser)
                .orElseGet(() -> {
                    isNewUser.set(true);

                    Map<String, Object> kakaoAccount = (Map<String, Object>) userInfo.get("kakao_account");
                    Map<String, Object> profile = kakaoAccount != null
                            ? (Map<String, Object>) kakaoAccount.get("profile")
                            : null;

                    String email = kakaoAccount != null ? (String) kakaoAccount.get("email") : null;
                    String nickname = profile != null ? (String) profile.get("nickname") : null;
                    String profileImage = profile != null ? (String) profile.get("profile_image_url") : null;

                    User user = email != null
                            ? userRepository.findByEmail(email).orElse(null)
                            : null;

                    if (user == null) {
                        user = User.builder()
                                .email(email != null ? email : providerUserId + "@kakao.user")
                                .nickname(nickname != null ? nickname : "user_" + providerUserId.substring(0, 8))
                                .profileImage(profileImage)
                                .build();
                        user = userRepository.save(user);
                    }

                    UserSocialAccount socialAccount = UserSocialAccount.builder()
                            .provider(provider)
                            .providerUserId(providerUserId)
                            .email(email)
                            .isPrimary(true)
                            .build();
                    user.addSocialAccount(socialAccount);
                    socialAccountRepository.save(socialAccount);

                    return user;
                });
    }

    private AuthResponse generateAuthResponse(User user, boolean isNewUser, String provider) {
        String accessToken = jwtTokenUtil.createAccessToken(user.getId());
        String refreshTokenValue = jwtTokenUtil.createRefreshToken(user.getId());

        RefreshToken refreshToken = RefreshToken.builder()
                .user(user)
                .token(refreshTokenValue)
                .expiresAt(jwtTokenUtil.getExpirationFromToken(refreshTokenValue)
                        .toInstant()
                        .atZone(java.time.ZoneId.of("Asia/Seoul"))
                        .toLocalDateTime())
                .build();
        refreshTokenRepository.save(refreshToken);

        return AuthResponse.of(
                accessToken,
                refreshTokenValue,
                accessTokenValidity / 1000,
                isNewUser,
                user,
                provider
        );
    }

    private void saveLoginHistory(User user, String provider) {
        LoginHistory history = LoginHistory.builder()
                .user(user)
                .provider(provider)
                .build();
        loginHistoryRepository.save(history);
    }

    private String generateNumericToken(int length) {
        StringBuilder sb = new StringBuilder(length);
        for (int i = 0; i < length; i++) {
            sb.append(secureRandom.nextInt(10));
        }
        return sb.toString();
    }
}
