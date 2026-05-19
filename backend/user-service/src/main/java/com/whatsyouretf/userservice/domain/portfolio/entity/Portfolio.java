package com.whatsyouretf.userservice.domain.portfolio.entity;

import com.whatsyouretf.userservice.common.exception.BusinessException;
import com.whatsyouretf.userservice.common.exception.ErrorCode;
import com.whatsyouretf.userservice.domain.user.entity.User;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

/**
 * 포트폴리오 엔티티
 * <p>
 * 사용자의 포트폴리오 정보를 저장합니다.
 */
@Entity
@Table(name = "portfolio", indexes = {
        @Index(name = "idx_portfolio_user", columnList = "user_id")
})
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Portfolio {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /** 사용자 */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    /** 포트폴리오 이름 */
    @Column(nullable = false, length = 100)
    private String name;

    /** 포트폴리오 설명 */
    @Column(columnDefinition = "TEXT")
    private String description;

    /** 투자 금액 */
    @Column(name = "invest_amount", precision = 15, scale = 2)
    private BigDecimal investAmount;

    @Column(name = "invest_period")
    private Integer investPeriod;

    @Column(name = "portfolio_type")
    @Enumerated(EnumType.STRING)
    private PortfolioType portfolioType;

    /** 알림 허용 여부 */
    @Column(name = "is_alert_enabled")
    private Boolean isAlertEnabled = false;

    /** 전일 종가 (포트폴리오 평가액) */
    @Column(name = "prev_close_value", precision = 15, scale = 2)
    private BigDecimal prevCloseValue;

    /** 마이데이터 포트폴리오 여부 */
    @Column(name = "is_my_data")
    private Boolean isMyData = false;

    @Column(name = "created_at")
    private LocalDateTime createdAt = LocalDateTime.now();

    @Column(name = "updated_at")
    private LocalDateTime updatedAt = LocalDateTime.now();

    @OneToMany(mappedBy = "portfolio", cascade = CascadeType.ALL, orphanRemoval = true)
    List<PortfolioEtf> portfolioEtfs = new ArrayList<>();

    public static Portfolio createPortfolio(
        Long userId,
        String portfolioName,
        BigDecimal investAmount,
        Integer investPeriod,
        PortfolioType portfolioType
    ) {
        if (investPeriod > 36 || investPeriod < 1) {
            throw new BusinessException(ErrorCode.INVALID_PORTFOLIO_PERIOD);
        }

        Portfolio portfolio = new Portfolio();
        portfolio.user = User.of(userId);
        portfolio.name = portfolioName;
        portfolio.investAmount = investAmount;
        portfolio.investPeriod = investPeriod;
        portfolio.prevCloseValue = investAmount;
        portfolio.portfolioType = portfolioType;
        return portfolio;
    }

    public static Portfolio createMyDataPortfolio(Long userId, BigDecimal totalValue) {
        Portfolio portfolio = new Portfolio();
        portfolio.user = User.of(userId);
        portfolio.name = "마이데이터 포트폴리오";
        portfolio.investAmount = totalValue;
        portfolio.investPeriod = 1;
        portfolio.prevCloseValue = totalValue;
        portfolio.portfolioType = PortfolioType.LUMP_SUM;
        portfolio.isMyData = true;
        return portfolio;
    }

    public static Portfolio of(Long portfolioId) {
        Portfolio portfolio = new Portfolio();
        portfolio.id = portfolioId;
        return portfolio;
    }

    public void update(String updatedName) {
        name = updatedName;
    }
}
