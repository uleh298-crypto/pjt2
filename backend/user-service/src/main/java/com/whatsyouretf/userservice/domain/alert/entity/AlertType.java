package com.whatsyouretf.userservice.domain.alert.entity;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

/**
 * 알림 유형 코드 엔티티
 * <p>
 * 알림 유형을 정의하는 코드 테이블입니다.
 */
@Entity
@Table(name = "alert_type")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
@Builder
public class AlertType {

    /** 알림 유형 코드 (PK) */
    @Id
    @Column(length = 50)
    private String code;

    /** 알림 유형명 */
    @Column(nullable = false, length = 100)
    private String name;

    /** 카테고리 */
    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 30)
    private AlertCategory category;

    /** 설정 그룹 (사용자 설정 화면 단위) */
    @Column(name = "setting_group", nullable = false, length = 30)
    private String settingGroup;

    /** 설명 */
    @Column(length = 200)
    private String description;

    /** 활성 상태 */
    @Column(name = "is_active")
    @Builder.Default
    private Boolean isActive = true;

    /** 노출 순서 */
    @Column(name = "display_order")
    @Builder.Default
    private Integer displayOrder = 0;

    @Column(name = "created_at")
    @Builder.Default
    private LocalDateTime createdAt = LocalDateTime.now();
}
