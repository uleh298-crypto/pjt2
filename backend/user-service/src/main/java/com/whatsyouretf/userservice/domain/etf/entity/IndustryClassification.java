package com.whatsyouretf.userservice.domain.etf.entity;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

/**
 * 산업 분류 엔티티
 * <p>
 * Level 1~3: KSIC 표준 (미사용)
 * Level 4: 세분류 (네이버 테마 기반 커스텀)
 */
@Entity
@Table(name = "industry_classification")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
@Builder
public class IndustryClassification {

    @Id
    @Column(length = 10)
    private String code;

    @Column(nullable = false, length = 100)
    private String name;

    /** 분류 레벨 (1=대, 2=중, 3=소, 4=세분류) */
    @Column(nullable = false)
    private Integer level;

    /** 상위 분류 코드 */
    @Column(name = "parent_code", length = 10)
    private String parentCode;

    /** 그룹 코드 (IT_SEMI, ENERGY 등) */
    @Column(name = "group_code", length = 10)
    private String groupCode;

    /** 그룹명 (반도체, 에너지 등) */
    @Column(name = "group_name", length = 50)
    private String groupName;

    @Column(name = "created_at")
    @Builder.Default
    private LocalDateTime createdAt = LocalDateTime.now();

    @Column(name = "updated_at")
    @Builder.Default
    private LocalDateTime updatedAt = LocalDateTime.now();
}
