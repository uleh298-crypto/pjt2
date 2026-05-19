package com.whatsyouretf.userservice.domain.user.entity;

import com.whatsyouretf.userservice.domain.etf.entity.Etf;
import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

/**
 * 사용자 관심 ETF 엔티티 (좋아요)
 */
@Entity
@Table(name = "user_favorite_etf", uniqueConstraints = {
    @UniqueConstraint(columnNames = {"user_id", "etf_id"})
})
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
@Builder
public class UserFavoriteEtf {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "etf_id", nullable = false)
    private Etf etf;

    @Column(name = "created_at")
    @Builder.Default
    private LocalDateTime createdAt = LocalDateTime.now();

    /**
     * 정적 팩토리 메서드
     */
    public static UserFavoriteEtf create(User user, Etf etf) {
        return UserFavoriteEtf.builder()
                .user(user)
                .etf(etf)
                .build();
    }
}
