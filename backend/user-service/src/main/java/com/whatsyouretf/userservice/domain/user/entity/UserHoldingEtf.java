package com.whatsyouretf.userservice.domain.user.entity;

import com.whatsyouretf.userservice.domain.etf.entity.Etf;
import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * 사용자 보유 ETF 엔티티 (마이데이터 연동)
 */
@Entity
@Table(name = "user_holding_etf", uniqueConstraints = {
    @UniqueConstraint(columnNames = {"user_id", "etf_id"})
})
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
@Builder
public class UserHoldingEtf {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "etf_id", nullable = false)
    private Etf etf;

    /** 보유 수량 */
    @Column(nullable = false)
    private Integer quantity;

    /** 평균 매입가 */
    @Column(name = "avg_price", precision = 15, scale = 2)
    private BigDecimal avgPrice;

    /** 마이데이터 동기화 시점 */
    @Column(name = "synced_at")
    private LocalDateTime syncedAt;

    @Column(name = "created_at")
    @Builder.Default
    private LocalDateTime createdAt = LocalDateTime.now();

    @Column(name = "updated_at")
    @Builder.Default
    private LocalDateTime updatedAt = LocalDateTime.now();

    /**
     * 보유 정보 업데이트
     */
    public void update(Integer quantity, BigDecimal avgPrice) {
        this.quantity = quantity;
        this.avgPrice = avgPrice;
        this.syncedAt = LocalDateTime.now();
        this.updatedAt = LocalDateTime.now();
    }
}
