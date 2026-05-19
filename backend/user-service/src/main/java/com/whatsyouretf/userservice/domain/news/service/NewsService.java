package com.whatsyouretf.userservice.domain.news.service;

import com.whatsyouretf.userservice.domain.news.dto.*;

/**
 * 뉴스 서비스 인터페이스
 */
public interface NewsService {

    /**
     * 최신 뉴스 목록 조회 (커서 기반 페이징)
     *
     * @param categoryCode 카테고리 코드 필터 (nullable)
     * @param lastId 마지막 조회 뉴스 ID (첫 페이지는 null)
     * @param size 페이지 크기 (기본 10, 최대 50)
     * @return 뉴스 목록 응답
     */
    NewsPageResponse getLatestNews(String categoryCode, Long lastId, int size);

    /**
     * 뉴스 상세 조회
     *
     * @param newsId 뉴스 ID
     * @return 뉴스 상세 응답
     */
    NewsDetailResponse getNewsDetail(Long newsId);

    /**
     * 뉴스 검색 (최신 20개)
     *
     * @param keyword 검색 키워드
     * @param categoryCode 카테고리 코드 필터 (nullable)
     * @return 뉴스 목록 응답 (keyword 포함)
     */
    NewsPageResponse searchNews(String keyword, String categoryCode);

    /**
     * ETF 관련 뉴스 조회
     *
     * @param etfId ETF ID
     * @param size  조회 개수
     * @return ETF 뉴스 응답
     */
    EtfNewsResponse getEtfNews(Long etfId, int size);

    /**
     * 포트폴리오 관련 뉴스 조회
     * <p>
     * 포트폴리오 구성 ETF의 종목들과 관련된 최신 뉴스 5개를 반환합니다.
     * 비중이 높은 ETF의 종목 뉴스가 우선합니다.
     * 매일 오전 9시 기준으로 갱신됩니다.
     *
     * @param portfolioId 포트폴리오 ID
     * @return 포트폴리오 뉴스 응답
     */
    PortfolioNewsResponse getPortfolioNews(Long portfolioId);
}
