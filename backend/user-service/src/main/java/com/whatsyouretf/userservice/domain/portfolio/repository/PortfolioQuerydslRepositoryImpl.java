package com.whatsyouretf.userservice.domain.portfolio.repository;

import com.querydsl.core.Tuple;
import com.querydsl.core.types.Projections;
import com.querydsl.jpa.impl.JPAQueryFactory;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import static com.whatsyouretf.userservice.domain.etf.entity.QEtf.etf;
import static com.whatsyouretf.userservice.domain.portfolio.entity.QPortfolioEtf.portfolioEtf;

@Component
@RequiredArgsConstructor
public class PortfolioQuerydslRepositoryImpl implements PortfolioQuerydslRepository {
        private final JPAQueryFactory queryFactory;

        @Override
        public List<PortfolioEtfInfo> getPortfolioEtfs(Long portfolioId) {
                return queryFactory
                        .select(Projections.constructor(
                                PortfolioEtfInfo.class,
                                etf.stockCode,
                                etf.name
                        ))
                        .from(portfolioEtf)
                        .join(portfolioEtf.etf, etf)
                        .where(portfolioEtf.portfolio.id.eq(portfolioId))
                        .fetch();
        }

        @Override
        public Map<Long, List<PortfolioEtfInfo>> getPortfolioInfoMap(List<Long> portfolioList) {
                List<Tuple> rows = queryFactory
                        .select(
                                portfolioEtf.portfolio.id,
                                etf.stockCode,
                                etf.name
                        )
                        .from(portfolioEtf)
                        .join(portfolioEtf.etf, etf)
                        .where(portfolioEtf.portfolio.id.in(portfolioList))
                        .fetch();

                return rows.stream()
                        .collect(Collectors.groupingBy(
                                tuple -> tuple.get(portfolioEtf.portfolio.id),
                                Collectors.mapping(
                                        tuple -> new PortfolioEtfInfo(
                                                tuple.get(etf.stockCode),
                                                tuple.get(etf.name)
                                        ),
                                        Collectors.toList()
                                )
                        ));
        }
}
