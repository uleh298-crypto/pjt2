package com.whatsyouretf.userservice.domain.auth.dto;

import com.whatsyouretf.userservice.domain.user.entity.User;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
@AllArgsConstructor
public class AuthResponse {

    private String accessToken;
    private String refreshToken;
    private Long expiresIn;
    private Boolean isNewUser;
    private UserInfo user;

    @Getter
    @Builder
    @AllArgsConstructor
    public static class UserInfo {
        private Long id;
        private String email;
        private String nickname;
        private String profileImage;
        private String loginProvider;

        public static UserInfo from(User user, String provider) {
            return UserInfo.builder()
                    .id(user.getId())
                    .email(user.getEmail())
                    .nickname(user.getNickname())
                    .profileImage(user.getProfileImage())
                    .loginProvider(provider)
                    .build();
        }
    }

    public static AuthResponse of(String accessToken, String refreshToken, Long expiresIn, boolean isNewUser, User user, String provider) {
        return AuthResponse.builder()
                .accessToken(accessToken)
                .refreshToken(refreshToken)
                .expiresIn(expiresIn)
                .isNewUser(isNewUser)
                .user(UserInfo.from(user, provider))
                .build();
    }

    // 토큰 갱신용 (refreshToken 없이)
    public static AuthResponse ofRefresh(String accessToken, Long expiresIn) {
        return AuthResponse.builder()
                .accessToken(accessToken)
                .expiresIn(expiresIn)
                .build();
    }
}
