package com.whatsyouretf.userservice.domain.common.entity;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

/**
 * 공통 카테고리 엔티티
 * <p>
 * 뉴스, 포트폴리오 등에서 공통으로 사용하는 카테고리 정보를 저장합니다.
 */
@Entity
@Table(name = "category")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
@Builder
public class Category {

    /** 카테고리 코드 (PK) - NEWS_MACRO, NEWS_SEMI, PORTFOLIO_DIVIDEND 등 */
    @Id
    @Column(length = 30)
    private String code;

    /** 카테고리 유형 (NEWS / PORTFOLIO / ETF) */
    @Column(nullable = false, length = 20)
    private String type;

    /** 카테고리명 ("금리/거시경제", "배당형") */
    @Column(nullable = false, length = 50)
    private String name;

    /** 설명 */
    @Column(length = 200)
    private String description;

    /** 노출 순서 */
    @Column(name = "display_order")
    @Builder.Default
    private Integer displayOrder = 0;

    /** 활성 여부 */
    @Column(name = "is_active")
    @Builder.Default
    private Boolean isActive = true;

    @Column(name = "created_at")
    @Builder.Default
    private LocalDateTime createdAt = LocalDateTime.now();
}
