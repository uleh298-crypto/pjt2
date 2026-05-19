package com.whatsyouretf.userservice.domain.user.dto;

/**
 * 관심 ETF 정렬 타입
 */
public enum FavoriteSortType {
    /** 최근 등록순 (기본값) */
    RECENT,
    /** 등락률 높은순 */
    CHANGE_RATE_DESC,
    /** 등락률 낮은순 */
    CHANGE_RATE_ASC,
    /** 이름순 (가나다) */
    NAME_ASC
}
