package com.whatsyouretf.userservice.domain.alert.entity;

import com.whatsyouretf.userservice.domain.user.entity.User;
import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

/**
 * 사용자 알림 엔티티
 * <p>
 * 사용자에게 발송된 알림 정보를 저장합니다.
 */
@Entity
@Table(name = "user_alert", indexes = {
        @Index(name = "idx_user_alert_user", columnList = "user_id"),
        @Index(name = "idx_user_alert_type", columnList = "alert_type_code"),
        @Index(name = "idx_user_alert_created", columnList = "created_at DESC"),
        @Index(name = "idx_user_alert_ref", columnList = "reference_type, reference_id")
})
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
@Builder
public class UserAlert {

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

    /** 참조 대상 유형 (nullable) */
    @Enumerated(EnumType.STRING)
    @Column(name = "reference_type", length = 30)
    private ReferenceType referenceType;

    /** 참조 대상 ID (nullable) */
    @Column(name = "reference_id")
    private Long referenceId;

    /** 알림 제목 */
    @Column(nullable = false, length = 200)
    private String title;

    /** 알림 메시지 */
    @Column(columnDefinition = "TEXT")
    private String message;

    /** 읽음 여부 */
    @Column(name = "is_read")
    @Builder.Default
    private Boolean isRead = false;

    /** 읽은 시간 */
    @Column(name = "read_at")
    private LocalDateTime readAt;

    @Column(name = "created_at")
    @Builder.Default
    private LocalDateTime createdAt = LocalDateTime.now();

    /**
     * 읽음 처리
     */
    public void markAsRead() {
        if (!this.isRead) {
            this.isRead = true;
            this.readAt = LocalDateTime.now();
        }
    }
}
