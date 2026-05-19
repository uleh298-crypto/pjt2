package com.whatsyouretf.userservice.domain.etf.repository;

import com.querydsl.core.types.dsl.BooleanExpression;
import com.querydsl.jpa.impl.JPAQueryFactory;
import com.whatsyouretf.userservice.domain.etf.entity.EtfPrice;
import com.whatsyouretf.userservice.domain.etf.entity.QEtf;
import com.whatsyouretf.userservice.domain.etf.entity.QEtfPrice;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Component;

import java.time.LocalDate;
import java.util.List;

@Component
@RequiredArgsConstructor
public class EtfPriceQueryDslReaderImpl implements EtfPriceQueryDslReader {

    private static final QEtfPrice etfPrice = QEtfPrice.etfPrice;
    private static final QEtf etf = QEtf.etf;
    private final JPAQueryFactory queryFactory;

    @Override
    public Page<EtfPrice> findByTickerAndDateRange(String ticker, LocalDate startDate, LocalDate endDate, Pageable pageable) {
        List<EtfPrice> content = queryFactory
                .selectFrom(etfPrice)
                .join(etfPrice.etf, etf)
                .where(
                        etf.stockCode.eq(ticker),
                        startDateGoe(startDate),
                        endDateLoe(endDate)
                )
                .orderBy(etfPrice.tradeDate.desc())
                .offset(pageable.getOffset())
                .limit(pageable.getPageSize())
                .fetch();

        Long total = queryFactory
                .select(etfPrice.count())
                .from(etfPrice)
                .join(etfPrice.etf, etf)
                .where(
                        etf.stockCode.eq(ticker),
                        startDateGoe(startDate),
                        endDateLoe(endDate)
                )
                .fetchOne();

        return new PageImpl<>(content, pageable, total != null ? total : 0);
    }

    private BooleanExpression startDateGoe(LocalDate startDate) {
        return startDate != null ? etfPrice.tradeDate.goe(startDate) : null;
    }

    private BooleanExpression endDateLoe(LocalDate endDate) {
        return endDate != null ? etfPrice.tradeDate.loe(endDate) : null;
    }
}
