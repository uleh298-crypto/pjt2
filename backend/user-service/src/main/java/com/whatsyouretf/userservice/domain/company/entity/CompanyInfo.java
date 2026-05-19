package com.whatsyouretf.userservice.domain.company.entity;

import com.whatsyouretf.userservice.domain.etf.entity.IndustryClassification;
import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

/**
 * 상장 회사 정보 엔티티
 * <p>
 * 국내 상장 회사 정보를 저장합니다.
 * 이 테이블의 데이터는 팀원이 담당하며, user-service에서는 조회만 합니다.
 */
@Entity
@Table(name = "company_info")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
@Builder
public class CompanyInfo {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /** 회사명 */
    @Column(name = "company_name", length = 100)
    private String companyName;

    /** 산업분류 (세분류: SEMI_HBM 등) */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "industry_code")
    private IndustryClassification industry;

    /** 산업분류명 (WICS 소분류: 반도체와반도체장비 등) */
    @Column(name = "industry_name", length = 100)
    private String industryName;

    /** 투자테마 그룹 (대분류: IT_SEMI, BIO 등) */
    @Column(name = "industry_group", length = 50)
    private String industryGroup;

    /** 대표자명 */
    @Column(name = "ceo_name", length = 100)
    private String ceoName;

    /** 홈페이지 URL */
    @Column(length = 200)
    private String homepage;

    /** 지역 */
    @Column(length = 100)
    private String region;

    /** 활성 여부 */
    @Column(name = "is_active")
    @Builder.Default
    private Boolean isActive = true;

    @Column(name = "corporation_number", length = 50)
    private String corporationNumber;

    @Column(name = "created_at")
    @Builder.Default
    private LocalDateTime createdAt = LocalDateTime.now();

    @Column(name = "updated_at")
    @Builder.Default
    private LocalDateTime updatedAt = LocalDateTime.now();
}
