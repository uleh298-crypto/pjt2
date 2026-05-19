package com.whatsyouretf.userservice.domain.user.entity;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

@Entity
@Table(name = "email_verification_token")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
@Builder
public class EmailVerificationToken {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, length = 100)
    private String email;

    @Column(nullable = false, unique = true, length = 255)
    private String token;

    @Column(name = "expires_at", nullable = false)
    private LocalDateTime expiresAt;

    @Column(name = "is_verified")
    @Builder.Default
    private Boolean isVerified = false;

    @Column(name = "created_at")
    @Builder.Default
    private LocalDateTime createdAt = LocalDateTime.now();

    // 임시 저장용 필드 (DB 저장 X)
    @Transient
    private String password;

    @Transient
    private String nickname;

    public boolean isExpired() {
        return LocalDateTime.now().isAfter(expiresAt);
    }

    public void verify() {
        this.isVerified = true;
    }

    public void setTempUserInfo(String password, String nickname) {
        this.password = password;
        this.nickname = nickname;
    }
}
