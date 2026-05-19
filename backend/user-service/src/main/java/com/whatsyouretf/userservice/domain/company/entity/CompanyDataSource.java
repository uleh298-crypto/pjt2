package com.whatsyouretf.userservice.domain.company.entity;

import com.whatsyouretf.userservice.domain.common.entity.DataSource;
import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

/**
 * 회사 데이터 소스 매핑 엔티티
 * <p>
 * 회사 정보와 데이터 소스 간의 매핑 정보를 저장합니다.
 */
@Entity
@Table(name = "company_data_source")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
@Builder
public class CompanyDataSource {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /** 회사 정보 (FK) */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "company_info_id")
    private CompanyInfo companyInfo;

    /** 데이터 소스 (FK) */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "data_source_id", nullable = false)
    private DataSource dataSource;

    @Column(name = "created_at")
    @Builder.Default
    private LocalDateTime createdAt = LocalDateTime.now();

    @Column(name = "updated_at")
    @Builder.Default
    private LocalDateTime updatedAt = LocalDateTime.now();
}
