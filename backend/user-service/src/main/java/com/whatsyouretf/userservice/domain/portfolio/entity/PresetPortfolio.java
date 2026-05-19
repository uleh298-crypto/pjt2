package com.whatsyouretf.userservice.domain.portfolio.entity;

import jakarta.persistence.*;
import lombok.*;

/**
 * 꾸러미 (시스템 제공 포트폴리오) 엔티티
 */
@Entity
@Table(name = "preset_portfolios")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
@Builder
public class PresetPortfolio {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, length = 100)
    private String name;

    @Column(length = 200)
    private String description;

    /** 카드 이미지/아이콘 식별자 (STABLE_INCOME, HIGH_GROWTH 등) */
    @Column(name = "image_tag", length = 50)
    private String imageTag;

    /** 카드 태그 목록 (콤마 구분, 예: "배당,저변동성") */
    @Column(length = 200)
    private String tags;

    @Column(name = "display_order")
    @Builder.Default
    private Integer displayOrder = 0;
}
