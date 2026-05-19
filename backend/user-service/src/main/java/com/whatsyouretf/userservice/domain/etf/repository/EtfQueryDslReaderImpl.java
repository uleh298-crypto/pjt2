package com.whatsyouretf.userservice.domain.etf.repository;

import com.querydsl.core.types.OrderSpecifier;
import com.querydsl.core.types.Projections;
import com.querydsl.core.types.dsl.BooleanExpression;
import com.querydsl.core.types.dsl.Expressions;
import com.querydsl.jpa.JPAExpressions;
import com.querydsl.jpa.impl.JPAQueryFactory;
import com.whatsyouretf.userservice.domain.etf.dto.EtfSummary;
import com.whatsyouretf.userservice.domain.etf.entity.EtfSector;
import com.whatsyouretf.userservice.domain.etf.entity.QEtf;
import com.whatsyouretf.userservice.domain.etf.entity.QEtfStockComposition;
import com.whatsyouretf.userservice.domain.etf.entity.RiskType;
import com.whatsyouretf.userservice.domain.etf.service.EtfQuery;
import com.whatsyouretf.userservice.domain.user.entity.QUserFavoriteEtf;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.util.List;

@Component
@RequiredArgsConstructor
public class EtfQueryDslReaderImpl implements EtfQueryDslReader {

    private static final QEtf etf = QEtf.etf;
    private static final QEtfStockComposition comp = QEtfStockComposition.etfStockComposition;
    private static final QUserFavoriteEtf fav = QUserFavoriteEtf.userFavoriteEtf;

    private final JPAQueryFactory queryFactory;

    @Override
    public Page<EtfSummary> readEtfList(EtfQuery query, Pageable pageable) {
        List<EtfSummary> content = queryFactory
                .select(Projections.constructor(EtfSummary.class,
                        etf.id,
                        etf.stockCode,
                        etf.name,
                        fav.id.isNotNull(),
                        etf.riskType.stringValue(),
                        etf.nav
                ))
                .from(etf)
                .leftJoin(fav).on(fav.etf.eq(etf).and(isLikedCondition(query.userId())))
                .where(buildWhere(query))
                .orderBy(orderBy(query.sortedBy()))
                .offset(pageable.getOffset())
                .limit(pageable.getPageSize())
                .fetch();

        Long total = queryFactory
                .select(etf.count())
                .from(etf)
                .leftJoin(fav).on(fav.etf.eq(etf).and(isLikedCondition(query.userId())))
                .where(buildWhere(query))
                .fetchOne();

        return new PageImpl<>(content, pageable, total != null ? total : 0);
    }

    @Override
    public List<EtfSummary> readAllEtfList(EtfQuery query) {
        return queryFactory
                .select(Projections.constructor(EtfSummary.class,
                        etf.id,
                        etf.stockCode,
                        etf.name,
                        fav.id.isNotNull(),
                        etf.riskType.stringValue(),
                        etf.nav
                ))
                .from(etf)
                .leftJoin(fav).on(fav.etf.eq(etf).and(isLikedCondition(query.userId())))
                .where(buildWhere(query))
                .orderBy(etf.aum.desc().nullsLast())
                .fetch();
    }

    private BooleanExpression isLikedCondition(Long userId) {
        if (userId == null) return Expressions.FALSE;
        return fav.user.id.eq(userId);
    }

    private BooleanExpression isFavoriteEq(Boolean isFavorite, Long userId) {
        if (isFavorite == null) return null;
        if (userId == null) return Expressions.FALSE;
        if (isFavorite) {
            // 찜한 ETF만: fav JOIN이 매칭된 것만
            return fav.id.isNotNull();
        } else {
            // 찜 안 한 ETF만: fav JOIN이 없는 것만
            return fav.id.isNull();
        }
    }

    private BooleanExpression[] buildWhere(EtfQuery query) {
        return new BooleanExpression[]{
                etf.isActive.isTrue(),
                riskTypeEq(query.riskType()),
                strategyEq(query.strategy()),
                sectorEq(query.sector()),
                dividendYieldGoe(query.dividendYield()),
                dividendFreqEq(query.dividendFrequency()),
                isDerivativesEq(query.isDerivatives()),
                isLeverageEq(query.isLeverage()),
                isInverseEq(query.isInverse()),
                perBetween(query.perLow(), query.perHigh()),
                pbrBetween(query.pbrLow(), query.pbrHigh()),
                roeBetween(query.roeLow(), query.roeHigh()),
                commissionLoe(query.commission()),
                aumGoe(query.aum()),
                searchNameContains(query.searchName()),
                isFavoriteEq(query.isFavorite(), query.userId())
        };
    }

    private BooleanExpression searchNameContains(String searchName) {
        if (searchName == null || searchName.isBlank()) return null;

        // ETF 이름에 포함
        BooleanExpression etfNameMatch = etf.name.containsIgnoreCase(searchName);

        // 구성종목의 회사명에 포함 (etf_stock_composition → stock → company_info)
        // 명시적 join 대신 경로 탐색으로 JPQL implicit join 사용 → alias 충돌 방지
        BooleanExpression stockNameMatch = JPAExpressions
                .selectOne()
                .from(comp)
                .where(
                        comp.etf.eq(etf),
                        comp.stock.isNotNull(),
                        comp.stock.company.isNotNull(),
                        comp.stock.company.companyName.containsIgnoreCase(searchName)
                )
                .exists();

        return etfNameMatch.or(stockNameMatch);
    }

    private BooleanExpression riskTypeEq(String riskType) {
        if (riskType == null) return null;
        try {
            RiskType rt = RiskType.valueOf(riskType.toUpperCase());
            return etf.riskType.isNotNull().and(etf.riskType.eq(rt));
        } catch (IllegalArgumentException e) {
            return Expressions.FALSE;
        }
    }

    private BooleanExpression strategyEq(String strategy) {
        if (strategy == null) return null;
        if (strategy.isBlank()) return Expressions.FALSE;
        return etf.strategyType.isNotNull().and(etf.strategyType.eq(strategy));
    }

    private BooleanExpression sectorEq(String sector) {
        if (sector == null) return null;
        // enum 상수명(SEMI) 또는 tag(SEMI) 로 매칭, 대소문자 무시
        for (EtfSector s : EtfSector.values()) {
            if (s.name().equalsIgnoreCase(sector) || s.getTag().equalsIgnoreCase(sector)) {
                return etf.sector.isNotNull().and(etf.sector.eq(s));
            }
        }
        return Expressions.FALSE;  // 알 수 없는 값 → 결과 없음
    }

    private BooleanExpression dividendYieldGoe(BigDecimal dividendYield) {
        return dividendYield != null ? etf.dividendYield.isNotNull().and(etf.dividendYield.goe(dividendYield)) : null;
    }

    private BooleanExpression dividendFreqEq(String dividendFrequency) {
        if (dividendFrequency == null) return null;
        return etf.dividendFreq.isNotNull().and(etf.dividendFreq.eq(dividendFrequency));
    }

    private BooleanExpression isDerivativesEq(Boolean isDerivatives) {
        return isDerivatives != null ? etf.isDerivatives.isNotNull().and(etf.isDerivatives.eq(isDerivatives)) : null;
    }

    private BooleanExpression isLeverageEq(Boolean isLeverage) {
        return isLeverage != null ? etf.isLeveraged.isNotNull().and(etf.isLeveraged.eq(isLeverage)) : null;
    }

    private BooleanExpression isInverseEq(Boolean isInverse) {
        return isInverse != null ? etf.isInverse.isNotNull().and(etf.isInverse.eq(isInverse)) : null;
    }

    private BooleanExpression perBetween(BigDecimal perLow, BigDecimal perHigh) {
        if (perLow == null && perHigh == null) return null;
        BooleanExpression notNull = etf.fundamental.per.isNotNull();
        if (perLow != null && perHigh != null) return notNull.and(etf.fundamental.per.between(perLow.doubleValue(), perHigh.doubleValue()));
        if (perLow != null) return notNull.and(etf.fundamental.per.goe(perLow.doubleValue()));
        return notNull.and(etf.fundamental.per.loe(perHigh.doubleValue()));
    }

    private BooleanExpression pbrBetween(BigDecimal pbrLow, BigDecimal pbrHigh) {
        if (pbrLow == null && pbrHigh == null) return null;
        BooleanExpression notNull = etf.fundamental.pbr.isNotNull();
        if (pbrLow != null && pbrHigh != null) return notNull.and(etf.fundamental.pbr.between(pbrLow.doubleValue(), pbrHigh.doubleValue()));
        if (pbrLow != null) return notNull.and(etf.fundamental.pbr.goe(pbrLow.doubleValue()));
        return notNull.and(etf.fundamental.pbr.loe(pbrHigh.doubleValue()));
    }

    private BooleanExpression roeBetween(BigDecimal roeLow, BigDecimal roeHigh) {
        if (roeLow == null && roeHigh == null) return null;
        BooleanExpression notNull = etf.fundamental.roe.isNotNull();
        if (roeLow != null && roeHigh != null) return notNull.and(etf.fundamental.roe.between(roeLow.doubleValue(), roeHigh.doubleValue()));
        if (roeLow != null) return notNull.and(etf.fundamental.roe.goe(roeLow.doubleValue()));
        return notNull.and(etf.fundamental.roe.loe(roeHigh.doubleValue()));
    }

    private BooleanExpression commissionLoe(BigDecimal commission) {
        return commission != null ? etf.expenseRatio.isNotNull().and(etf.expenseRatio.loe(commission)) : null;
    }

    private BooleanExpression aumGoe(BigDecimal aum) {
        return aum != null ? etf.aum.isNotNull().and(etf.aum.goe(aum.longValue())) : null;
    }

    private OrderSpecifier<?> orderBy(String sortedBy) {
        if (sortedBy == null) return etf.aum.desc().nullsLast();
        return switch (sortedBy) {
            case "aum" -> etf.aum.desc().nullsLast();
            case "dividend_yield" -> etf.dividendYield.desc();
            case "per" -> etf.fundamental.per.asc();
            case "expense_ratio" -> etf.expenseRatio.asc();
            // dailyReturn / volume 은 서비스 레이어에서 캐시 기반 정렬 → DB 기본 정렬만 적용
            default -> etf.aum.desc().nullsLast();
        };
    }
}
