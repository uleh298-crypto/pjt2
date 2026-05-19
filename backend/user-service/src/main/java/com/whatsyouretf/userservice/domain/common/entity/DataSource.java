package com.whatsyouretf.userservice.domain.common.entity;

import jakarta.persistence.*;
import lombok.*;

/**
 * 데이터 소스 엔티티
 * <p>
 * 데이터 수집 소스 정보를 관리합니다.
 */
@Entity
@Table(name = "data_source")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
@Builder
public class DataSource {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /** 소스명 */
    @Column(name = "source_name", length = 30)
    private String sourceName;

    /** URL */
    @Column(length = 200)
    private String url;

    /** 활성 여부 */
    @Column(name = "is_active")
    @Builder.Default
    private Boolean isActive = true;
}
