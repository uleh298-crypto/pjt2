package com.whatsyouretf.userservice.domain.news.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.util.List;

/**
 * 뉴스 목록 응답 DTO (커서 기반 페이징)
 */
@Getter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class NewsPageResponse {

    /** 뉴스 목록 */
    private List<NewsListResponse> news;

    /** 검색 키워드 (검색 시에만) */
    private String keyword;

    /** 페이지 크기 */
    private int size;

    /** 다음 페이지 존재 여부 */
    private boolean hasMore;

    /** 다음 커서 (마지막 뉴스 ID) */
    private Long nextCursor;
}
