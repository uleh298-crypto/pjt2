package com.whatsyouretf.userservice.domain.auth.service;

import com.whatsyouretf.userservice.common.exception.BusinessException;
import com.whatsyouretf.userservice.common.exception.ErrorCode;
import com.whatsyouretf.userservice.common.service.EmailService;
import com.whatsyouretf.userservice.common.service.RedisService;
import com.whatsyouretf.userservice.common.util.JwtTokenUtil;
import com.whatsyouretf.userservice.domain.alert.repository.FcmTokenRepository;
import com.whatsyouretf.userservice.domain.auth.dto.*;
import com.whatsyouretf.userservice.domain.auth.service.impl.AuthServiceImpl;
import com.whatsyouretf.userservice.domain.user.entity.*;
import com.whatsyouretf.userservice.domain.user.repository.*;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.util.ReflectionTestUtils;

import java.time.LocalDateTime;
import java.util.Optional;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.BDDMockito.*;

/**
 * AuthService 단위 테스트
 * <p>
 * 테스트 범위:
 * - 이메일 회원가입 (signup, verify, resend)
 * - 이메일 로그인 (login)
 * - 토큰 관리 (refreshToken, logout)
 * - 비밀번호 재설정 (request, verify, reset)
 */
@ExtendWith(MockitoExtension.class)
@DisplayName("AuthService 단위 테스트")
class AuthServiceTest {

    @InjectMocks
    private AuthServiceImpl authService;

    @Mock
    private UserRepository userRepository;

    @Mock
    private UserSocialAccountRepository socialAccountRepository;

    @Mock
    private RefreshTokenRepository refreshTokenRepository;

    @Mock
    private EmailVerificationTokenRepository emailVerificationTokenRepository;

    @Mock
    private PasswordResetTokenRepository passwordResetTokenRepository;

    @Mock
    private LoginHistoryRepository loginHistoryRepository;

    @Mock
    private FcmTokenRepository fcmTokenRepository;

    @Mock
    private JwtTokenUtil jwtTokenUtil;

    @Mock
    private PasswordEncoder passwordEncoder;

    @Mock
    private EmailService emailService;

    @Mock
    private RedisService redisService;

    private static final String TEST_EMAIL = "test@example.com";
    private static final String TEST_PASSWORD = "Password123!";
    private static final String TEST_NICKNAME = "테스트유저";
    private static final String TEST_TOKEN = "123456";

    @BeforeEach
    void setUp() {
        // JWT 토큰 유효기간 설정
        ReflectionTestUtils.setField(authService, "accessTokenValidity", 3600000L);
    }

    // ========== 회원가입 테스트 ==========

    @Nested
    @DisplayName("회원가입 테스트")
    class SignupTest {

        @Test
        @DisplayName("정상적인 회원가입 요청 시 인증 이메일이 발송된다")
        void signup_Success() {
            // given
            SignupRequest request = new SignupRequest(TEST_EMAIL, TEST_PASSWORD, TEST_PASSWORD, TEST_NICKNAME);

            given(userRepository.existsByEmail(TEST_EMAIL)).willReturn(false);
            given(userRepository.existsByNickname(TEST_NICKNAME)).willReturn(false);
            given(passwordEncoder.encode(TEST_PASSWORD)).willReturn("encodedPassword");

            // when
            authService.signup(request);

            // then
            then(emailVerificationTokenRepository).should().deleteByEmail(TEST_EMAIL);
            then(emailVerificationTokenRepository).should().save(any(EmailVerificationToken.class));
            then(redisService).should().savePendingSignup(eq(TEST_EMAIL), any(PendingSignup.class), anyLong());
            then(emailService).should().sendVerificationEmail(eq(TEST_EMAIL), anyString());
        }

        @Test
        @DisplayName("비밀번호와 비밀번호 확인이 일치하지 않으면 예외가 발생한다")
        void signup_PasswordMismatch_ThrowsException() {
            // given
            SignupRequest request = new SignupRequest(TEST_EMAIL, TEST_PASSWORD, "DifferentPassword!", TEST_NICKNAME);

            // when & then
            assertThatThrownBy(() -> authService.signup(request))
                    .isInstanceOf(BusinessException.class)
                    .hasFieldOrPropertyWithValue("code", ErrorCode.PASSWORD_MISMATCH);
        }

        @Test
        @DisplayName("이미 존재하는 이메일로 가입 시 예외가 발생한다")
        void signup_DuplicateEmail_ThrowsException() {
            // given
            SignupRequest request = new SignupRequest(TEST_EMAIL, TEST_PASSWORD, TEST_PASSWORD, TEST_NICKNAME);
            given(userRepository.existsByEmail(TEST_EMAIL)).willReturn(true);

            // when & then
            assertThatThrownBy(() -> authService.signup(request))
                    .isInstanceOf(BusinessException.class)
                    .hasFieldOrPropertyWithValue("code", ErrorCode.DUPLICATE_EMAIL);
        }

        @Test
        @DisplayName("이미 존재하는 닉네임으로 가입 시 예외가 발생한다")
        void signup_DuplicateNickname_ThrowsException() {
            // given
            SignupRequest request = new SignupRequest(TEST_EMAIL, TEST_PASSWORD, TEST_PASSWORD, TEST_NICKNAME);
            given(userRepository.existsByEmail(TEST_EMAIL)).willReturn(false);
            given(userRepository.existsByNickname(TEST_NICKNAME)).willReturn(true);

            // when & then
            assertThatThrownBy(() -> authService.signup(request))
                    .isInstanceOf(BusinessException.class)
                    .hasFieldOrPropertyWithValue("code", ErrorCode.DUPLICATE_NICKNAME);
        }
    }

    // ========== 이메일 인증 테스트 ==========

    @Nested
    @DisplayName("이메일 인증 테스트")
    class VerifyEmailTest {

        @Test
        @DisplayName("유효한 인증 코드로 인증 시 사용자가 생성되고 토큰이 발급된다")
        void verifyEmail_Success() {
            // given
            EmailVerifyRequest request = new EmailVerifyRequest(TEST_EMAIL, TEST_TOKEN);

            EmailVerificationToken verificationToken = EmailVerificationToken.builder()
                    .email(TEST_EMAIL)
                    .token(TEST_TOKEN)
                    .expiresAt(LocalDateTime.now().plusMinutes(10))
                    .isVerified(false)
                    .build();

            PendingSignup pendingSignup = PendingSignup.builder()
                    .email(TEST_EMAIL)
                    .encodedPassword("encodedPassword")
                    .nickname(TEST_NICKNAME)
                    .build();

            User savedUser = User.builder()
                    .id(1L)
                    .email(TEST_EMAIL)
                    .nickname(TEST_NICKNAME)
                    .password("encodedPassword")
                    .build();

            given(emailVerificationTokenRepository.findByEmailAndToken(TEST_EMAIL, TEST_TOKEN))
                    .willReturn(Optional.of(verificationToken));
            given(redisService.getPendingSignup(TEST_EMAIL, PendingSignup.class))
                    .willReturn(Optional.of(pendingSignup));
            given(userRepository.existsByEmail(TEST_EMAIL)).willReturn(false);
            given(userRepository.save(any(User.class))).willReturn(savedUser);
            given(jwtTokenUtil.createAccessToken(1L)).willReturn("accessToken");
            given(jwtTokenUtil.createRefreshToken(1L)).willReturn("refreshToken");
            given(jwtTokenUtil.getExpirationFromToken("refreshToken"))
                    .willReturn(java.util.Date.from(LocalDateTime.now().plusDays(7)
                            .atZone(java.time.ZoneId.systemDefault()).toInstant()));

            // when
            AuthResponse response = authService.verifyEmail(request);

            // then
            assertThat(response).isNotNull();
            assertThat(response.getAccessToken()).isEqualTo("accessToken");
            assertThat(response.getRefreshToken()).isEqualTo("refreshToken");
            assertThat(response.getIsNewUser()).isTrue();
            then(redisService).should().deletePendingSignup(TEST_EMAIL);
        }

        @Test
        @DisplayName("잘못된 인증 코드로 인증 시 예외가 발생한다")
        void verifyEmail_InvalidToken_ThrowsException() {
            // given
            EmailVerifyRequest request = new EmailVerifyRequest(TEST_EMAIL, "wrongToken");
            given(emailVerificationTokenRepository.findByEmailAndToken(TEST_EMAIL, "wrongToken"))
                    .willReturn(Optional.empty());

            // when & then
            assertThatThrownBy(() -> authService.verifyEmail(request))
                    .isInstanceOf(BusinessException.class)
                    .hasFieldOrPropertyWithValue("code", ErrorCode.INVALID_TOKEN);
        }

        @Test
        @DisplayName("만료된 인증 코드로 인증 시 예외가 발생한다")
        void verifyEmail_ExpiredToken_ThrowsException() {
            // given
            EmailVerifyRequest request = new EmailVerifyRequest(TEST_EMAIL, TEST_TOKEN);

            EmailVerificationToken expiredToken = EmailVerificationToken.builder()
                    .email(TEST_EMAIL)
                    .token(TEST_TOKEN)
                    .expiresAt(LocalDateTime.now().minusMinutes(1)) // 만료됨
                    .isVerified(false)
                    .build();

            given(emailVerificationTokenRepository.findByEmailAndToken(TEST_EMAIL, TEST_TOKEN))
                    .willReturn(Optional.of(expiredToken));

            // when & then
            assertThatThrownBy(() -> authService.verifyEmail(request))
                    .isInstanceOf(BusinessException.class)
                    .hasFieldOrPropertyWithValue("code", ErrorCode.EXPIRED_TOKEN);
        }
    }

    // ========== 로그인 테스트 ==========

    @Nested
    @DisplayName("로그인 테스트")
    class LoginTest {

        @Test
        @DisplayName("올바른 이메일과 비밀번호로 로그인 시 토큰이 발급된다")
        void login_Success() {
            // given
            LoginRequest request = new LoginRequest(TEST_EMAIL, TEST_PASSWORD);

            User user = User.builder()
                    .id(1L)
                    .email(TEST_EMAIL)
                    .password("encodedPassword")
                    .nickname(TEST_NICKNAME)
                    .isActive(true)
                    .build();

            given(userRepository.findByEmail(TEST_EMAIL)).willReturn(Optional.of(user));
            given(passwordEncoder.matches(TEST_PASSWORD, "encodedPassword")).willReturn(true);
            given(jwtTokenUtil.createAccessToken(1L)).willReturn("accessToken");
            given(jwtTokenUtil.createRefreshToken(1L)).willReturn("refreshToken");
            given(jwtTokenUtil.getExpirationFromToken("refreshToken"))
                    .willReturn(java.util.Date.from(LocalDateTime.now().plusDays(7)
                            .atZone(java.time.ZoneId.systemDefault()).toInstant()));

            // when
            AuthResponse response = authService.login(request);

            // then
            assertThat(response).isNotNull();
            assertThat(response.getAccessToken()).isEqualTo("accessToken");
            assertThat(response.getIsNewUser()).isFalse();
            then(loginHistoryRepository).should().save(any(LoginHistory.class));
        }

        @Test
        @DisplayName("존재하지 않는 이메일로 로그인 시 예외가 발생한다")
        void login_UserNotFound_ThrowsException() {
            // given
            LoginRequest request = new LoginRequest(TEST_EMAIL, TEST_PASSWORD);
            given(userRepository.findByEmail(TEST_EMAIL)).willReturn(Optional.empty());

            // when & then
            assertThatThrownBy(() -> authService.login(request))
                    .isInstanceOf(BusinessException.class)
                    .hasFieldOrPropertyWithValue("code", ErrorCode.INVALID_CREDENTIALS);
        }

        @Test
        @DisplayName("잘못된 비밀번호로 로그인 시 예외가 발생한다")
        void login_WrongPassword_ThrowsException() {
            // given
            LoginRequest request = new LoginRequest(TEST_EMAIL, "wrongPassword");

            User user = User.builder()
                    .id(1L)
                    .email(TEST_EMAIL)
                    .password("encodedPassword")
                    .build();

            given(userRepository.findByEmail(TEST_EMAIL)).willReturn(Optional.of(user));
            given(passwordEncoder.matches("wrongPassword", "encodedPassword")).willReturn(false);

            // when & then
            assertThatThrownBy(() -> authService.login(request))
                    .isInstanceOf(BusinessException.class)
                    .hasFieldOrPropertyWithValue("code", ErrorCode.INVALID_CREDENTIALS);
        }

        @Test
        @DisplayName("소셜 로그인만 한 사용자가 이메일 로그인 시 예외가 발생한다")
        void login_SocialOnlyUser_ThrowsException() {
            // given
            LoginRequest request = new LoginRequest(TEST_EMAIL, TEST_PASSWORD);

            User socialOnlyUser = User.builder()
                    .id(1L)
                    .email(TEST_EMAIL)
                    .password(null) // 소셜 로그인만 한 경우 비밀번호 없음
                    .build();

            given(userRepository.findByEmail(TEST_EMAIL)).willReturn(Optional.of(socialOnlyUser));

            // when & then
            assertThatThrownBy(() -> authService.login(request))
                    .isInstanceOf(BusinessException.class)
                    .hasFieldOrPropertyWithValue("code", ErrorCode.INVALID_CREDENTIALS);
        }

        @Test
        @DisplayName("비활성화된 계정으로 로그인 시 예외가 발생한다")
        void login_InactiveUser_ThrowsException() {
            // given
            LoginRequest request = new LoginRequest(TEST_EMAIL, TEST_PASSWORD);

            User inactiveUser = User.builder()
                    .id(1L)
                    .email(TEST_EMAIL)
                    .password("encodedPassword")
                    .isActive(false)
                    .build();

            given(userRepository.findByEmail(TEST_EMAIL)).willReturn(Optional.of(inactiveUser));
            given(passwordEncoder.matches(TEST_PASSWORD, "encodedPassword")).willReturn(true);

            // when & then
            assertThatThrownBy(() -> authService.login(request))
                    .isInstanceOf(BusinessException.class)
                    .hasFieldOrPropertyWithValue("code", ErrorCode.USER_NOT_FOUND);
        }
    }

    // ========== 비밀번호 재설정 테스트 ==========

    @Nested
    @DisplayName("비밀번호 재설정 테스트")
    class PasswordResetTest {

        @Test
        @DisplayName("존재하는 이메일로 비밀번호 재설정 요청 시 이메일이 발송된다")
        void requestPasswordReset_Success() {
            // given
            User user = User.builder()
                    .id(1L)
                    .email(TEST_EMAIL)
                    .password("encodedPassword")
                    .build();

            given(userRepository.findByEmail(TEST_EMAIL)).willReturn(Optional.of(user));

            // when
            authService.requestPasswordReset(TEST_EMAIL);

            // then
            then(passwordResetTokenRepository).should().invalidateAllByUserId(1L);
            then(passwordResetTokenRepository).should().save(any(PasswordResetToken.class));
            then(emailService).should().sendPasswordResetEmail(eq(TEST_EMAIL), anyString());
        }

        @Test
        @DisplayName("존재하지 않는 이메일로 비밀번호 재설정 요청 시 예외가 발생하지 않는다 (보안상)")
        void requestPasswordReset_NonExistentEmail_NoException() {
            // given
            given(userRepository.findByEmail(TEST_EMAIL)).willReturn(Optional.empty());

            // when & then - 예외가 발생하지 않음
            assertThatCode(() -> authService.requestPasswordReset(TEST_EMAIL))
                    .doesNotThrowAnyException();

            // 이메일은 발송되지 않음
            then(emailService).should(never()).sendPasswordResetEmail(anyString(), anyString());
        }

        @Test
        @DisplayName("유효한 토큰으로 비밀번호 재설정 시 비밀번호가 변경된다")
        void resetPassword_Success() {
            // given
            PasswordResetConfirmRequest request = new PasswordResetConfirmRequest(
                    TEST_EMAIL, TEST_TOKEN, "NewPassword123!", "NewPassword123!"
            );

            User user = User.builder()
                    .id(1L)
                    .email(TEST_EMAIL)
                    .password("oldEncodedPassword")
                    .build();

            PasswordResetToken resetToken = PasswordResetToken.builder()
                    .user(user)
                    .token(TEST_TOKEN)
                    .expiresAt(LocalDateTime.now().plusMinutes(30))
                    .isUsed(false)
                    .build();

            given(passwordResetTokenRepository.findValidToken(eq(TEST_TOKEN), any(LocalDateTime.class)))
                    .willReturn(Optional.of(resetToken));
            given(passwordEncoder.encode("NewPassword123!")).willReturn("newEncodedPassword");

            // when
            authService.resetPassword(request);

            // then
            then(refreshTokenRepository).should().revokeAllByUserId(1L);
        }

        @Test
        @DisplayName("비밀번호와 비밀번호 확인이 일치하지 않으면 예외가 발생한다")
        void resetPassword_PasswordMismatch_ThrowsException() {
            // given
            PasswordResetConfirmRequest request = new PasswordResetConfirmRequest(
                    TEST_EMAIL, TEST_TOKEN, "NewPassword123!", "DifferentPassword!"
            );

            // when & then
            assertThatThrownBy(() -> authService.resetPassword(request))
                    .isInstanceOf(BusinessException.class)
                    .hasFieldOrPropertyWithValue("code", ErrorCode.PASSWORD_MISMATCH);
        }
    }

    // ========== 로그아웃 테스트 ==========

    @Nested
    @DisplayName("로그아웃 테스트")
    class LogoutTest {

        @Test
        @DisplayName("로그아웃 시 Refresh Token이 폐기된다")
        void logout_WithoutFcmToken_Success() {
            // given
            Long userId = 1L;

            // when
            authService.logout(userId, null);

            // then
            then(refreshTokenRepository).should().revokeAllByUserId(userId);
        }

        @Test
        @DisplayName("FCM 토큰과 함께 로그아웃 시 해당 기기의 FCM 토큰도 삭제된다")
        void logout_WithFcmToken_Success() {
            // given
            Long userId = 1L;
            String fcmToken = "test_fcm_token";

            // when
            authService.logout(userId, fcmToken);

            // then
            then(refreshTokenRepository).should().revokeAllByUserId(userId);
            then(fcmTokenRepository).should().deleteByUserIdAndToken(userId, fcmToken);
        }
    }
}
