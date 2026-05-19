package com.whatsyouretf.userservice.domain.news.entity;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

/**
 * 뉴스 카테고리 Enum
 * <p>
 * 14개의 산업별 뉴스 카테고리를 정의합니다.
 */
@Getter
@RequiredArgsConstructor
public enum NewsCategory {

    NEWS_SEMI("반도체"),
    NEWS_IT("IT/전자"),
    NEWS_BIO("바이오/의약"),
    NEWS_AUTO("자동차"),
    NEWS_CHEM("화학/소재"),
    NEWS_ENERGY("에너지"),
    NEWS_FINANCE("금융"),
    NEWS_CONSTRUCT("건설/부동산"),
    NEWS_CONSUMER("소비재"),
    NEWS_TELECOM("통신/미디어"),
    NEWS_TRANSPORT("운송/물류"),
    NEWS_INDUSTRY("산업재"),
    NEWS_ETC("기타"),
    NEWS_MARKET("시장/경제");

    private final String displayName;
}
