package com.whatsyouretf.userservice.domain.alert.entity;

import com.whatsyouretf.userservice.domain.user.entity.User;
import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

/**
 * FCM 토큰 엔티티
 * <p>
 * 푸시 알림을 위한 FCM 토큰을 저장합니다.
 */
@Entity
@Table(name = "fcm_token", indexes = {
        @Index(name = "idx_fcm_user", columnList = "user_id"),
        @Index(name = "idx_fcm_token", columnList = "token")
}, uniqueConstraints = {
        @UniqueConstraint(name = "uq_fcm_token", columnNames = {"token"})
})
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
@Builder
public class FcmToken {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /** 사용자 */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    /** FCM 토큰 */
    @Column(nullable = false, length = 500)
    private String token;

    /** 기기 유형 */
    @Enumerated(EnumType.STRING)
    @Column(name = "device_type", nullable = false, length = 20)
    private DeviceType deviceType;

    /** 활성 상태 */
    @Column(name = "is_active")
    @Builder.Default
    private Boolean isActive = true;

    @Column(name = "created_at")
    @Builder.Default
    private LocalDateTime createdAt = LocalDateTime.now();

    @Column(name = "updated_at")
    @Builder.Default
    private LocalDateTime updatedAt = LocalDateTime.now();

    /**
     * 토큰 업데이트
     */
    public void updateToken(String token) {
        this.token = token;
        this.isActive = true;
        this.updatedAt = LocalDateTime.now();
    }

    /**
     * 비활성화
     */
    public void deactivate() {
        this.isActive = false;
        this.updatedAt = LocalDateTime.now();
    }
}
