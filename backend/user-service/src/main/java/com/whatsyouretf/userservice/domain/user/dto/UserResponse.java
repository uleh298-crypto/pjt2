package com.whatsyouretf.userservice.domain.user.dto;

import com.whatsyouretf.userservice.domain.user.entity.User;
import com.whatsyouretf.userservice.domain.user.entity.UserSocialAccount;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Getter
@Builder
@AllArgsConstructor
public class UserResponse {

    private Long id;
    private String email;
    private String nickname;
    private String profileImage;
    private String role;
    private Boolean isActive;
    private LocalDateTime lastLoginAt;
    private LocalDateTime createdAt;
    private List<SocialAccountInfo> socialAccounts;

    @Getter
    @Builder
    @AllArgsConstructor
    public static class SocialAccountInfo {
        private String provider;
        private String email;
        private Boolean isPrimary;
        private LocalDateTime linkedAt;

        public static SocialAccountInfo from(UserSocialAccount account) {
            return SocialAccountInfo.builder()
                    .provider(account.getProvider().name())
                    .email(account.getEmail())
                    .isPrimary(account.getIsPrimary())
                    .linkedAt(account.getLinkedAt())
                    .build();
        }
    }

    public static UserResponse from(User user) {
        return UserResponse.builder()
                .id(user.getId())
                .email(user.getEmail())
                .nickname(user.getNickname())
                .profileImage(user.getProfileImage())
                .role(user.getRole().name())
                .isActive(user.getIsActive())
                .lastLoginAt(user.getLastLoginAt())
                .createdAt(user.getCreatedAt())
                .socialAccounts(user.getSocialAccounts().stream()
                        .map(SocialAccountInfo::from)
                        .collect(Collectors.toList()))
                .build();
    }

    public static UserResponse fromWithoutSocialAccounts(User user) {
        return UserResponse.builder()
                .id(user.getId())
                .email(user.getEmail())
                .nickname(user.getNickname())
                .profileImage(user.getProfileImage())
                .role(user.getRole().name())
                .isActive(user.getIsActive())
                .lastLoginAt(user.getLastLoginAt())
                .createdAt(user.getCreatedAt())
                .build();
    }
}
