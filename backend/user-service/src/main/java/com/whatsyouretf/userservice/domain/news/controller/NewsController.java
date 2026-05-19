package com.whatsyouretf.userservice.domain.news.controller;

import com.whatsyouretf.userservice.common.response.ApiResponse;
import com.whatsyouretf.userservice.domain.news.dto.*;
import com.whatsyouretf.userservice.domain.news.service.NewsService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

/**
 * 뉴스 API 컨트롤러
 * <p>
 * 뉴스 조회 관련 API를 제공합니다.
 * 인증 없이 접근 가능합니다.
 */
@Tag(name = "News", description = "뉴스 API")
@RestController
@RequestMapping("/api/v1/news")
@RequiredArgsConstructor
public class NewsController {

    private final NewsService newsService;

    /**
     * 최신 뉴스 목록 조회 (커서 기반 페이징)
     */
    @Operation(summary = "최신 뉴스 목록 조회", description = "카테고리별 최신 뉴스를 커서 기반으로 조회합니다. 무한 스크롤에 적합합니다.")
    @GetMapping
    public ResponseEntity<ApiResponse<NewsPageResponse>> getLatestNews(
            @Parameter(description = "카테고리 코드 필터") @RequestParam(required = false) String category,
            @Parameter(description = "마지막 조회 뉴스 ID (첫 페이지는 생략)") @RequestParam(required = false) Long lastId,
            @Parameter(description = "페이지 크기 (기본 20, 최대 50)") @RequestParam(defaultValue = "20") int size
    ) {
        NewsPageResponse response = newsService.getLatestNews(category, lastId, size);
        return ResponseEntity.ok(ApiResponse.success(response));
    }

    /**
     * 뉴스 상세 조회
     */
    @Operation(summary = "뉴스 상세 조회", description = "뉴스 상세 정보를 조회합니다. 조회수가 증가합니다.")
    @GetMapping("/{newsId}")
    public ResponseEntity<ApiResponse<NewsDetailResponse>> getNewsDetail(
            @Parameter(description = "뉴스 ID") @PathVariable Long newsId
    ) {
        NewsDetailResponse response = newsService.getNewsDetail(newsId);
        return ResponseEntity.ok(ApiResponse.success(response));
    }

    /**
     * 뉴스 검색 (제목 검색, 최신 20개)
     */
    @Operation(summary = "뉴스 검색", description = "키워드로 뉴스 제목을 검색합니다. 카테고리 지정 시 해당 카테고리 내에서 검색합니다. 최신 20개를 반환합니다.")
    @GetMapping("/search")
    public ResponseEntity<ApiResponse<NewsPageResponse>> searchNews(
            @Parameter(description = "검색 키워드 (1~50자)") @RequestParam String keyword,
            @Parameter(description = "카테고리 코드 (선택)") @RequestParam(required = false) String category
    ) {
        NewsPageResponse response = newsService.searchNews(keyword, category);
        return ResponseEntity.ok(ApiResponse.success(response));
    }

    /**
     * ETF 관련 뉴스 조회
     */
    @Operation(summary = "ETF 관련 뉴스 조회", description = "ETF 구성종목들의 뉴스를 조회합니다.")
    @GetMapping("/etf/{etfId}")
    public ResponseEntity<ApiResponse<EtfNewsResponse>> getEtfNews(
            @Parameter(description = "ETF ID") @PathVariable Long etfId,
            @Parameter(description = "조회 개수 (최대 50)") @RequestParam(defaultValue = "10") int size
    ) {
        EtfNewsResponse response = newsService.getEtfNews(etfId, size);
        return ResponseEntity.ok(ApiResponse.success(response));
    }

    /**
     * 포트폴리오 관련 뉴스 조회
     */
    @Operation(summary = "포트폴리오 뉴스 조회", description = "포트폴리오 구성 ETF의 종목 관련 뉴스를 조회합니다. 매일 오전 9시 갱신됩니다.")
    @GetMapping("/portfolio/{portfolioId}")
    public ResponseEntity<ApiResponse<PortfolioNewsResponse>> getPortfolioNews(
            @Parameter(description = "포트폴리오 ID") @PathVariable Long portfolioId
    ) {
        PortfolioNewsResponse response = newsService.getPortfolioNews(portfolioId);
        return ResponseEntity.ok(ApiResponse.success(response));
    }
}
