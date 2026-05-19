package com.whatsyouretf.userservice.domain.alert.entity;

import com.whatsyouretf.userservice.domain.user.entity.User;
import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

/**
 * 사용자별 알림 설정 엔티티
 * <p>
 * 사용자별로 알림 유형별 ON/OFF 설정을 관리합니다.
 */
@Entity
@Table(name = "user_notification_setting", indexes = {
        @Index(name = "idx_notification_setting_user", columnList = "user_id")
}, uniqueConstraints = {
        @UniqueConstraint(name = "uk_user_alert_type", columnNames = {"user_id", "alert_type_code"})
})
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
@Builder
public class UserNotificationSetting {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /** 사용자 */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    /** 알림 유형 */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "alert_type_code", nullable = false)
    private AlertType alertType;

    /** 활성화 여부 */
    @Column(name = "is_enabled")
    @Builder.Default
    private Boolean isEnabled = true;

    @Column(name = "created_at")
    @Builder.Default
    private LocalDateTime createdAt = LocalDateTime.now();

    @Column(name = "updated_at")
    @Builder.Default
    private LocalDateTime updatedAt = LocalDateTime.now();

    /**
     * 활성화 상태 변경
     */
    public void setEnabled(boolean enabled) {
        this.isEnabled = enabled;
        this.updatedAt = LocalDateTime.now();
    }
}
