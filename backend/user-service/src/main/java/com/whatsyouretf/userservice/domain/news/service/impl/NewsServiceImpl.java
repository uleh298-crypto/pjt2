package com.whatsyouretf.userservice.domain.news.service.impl;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.whatsyouretf.userservice.common.exception.BusinessException;
import com.whatsyouretf.userservice.common.exception.ErrorCode;
import com.whatsyouretf.userservice.domain.etf.dto.EtfCurrentInfo;
import com.whatsyouretf.userservice.domain.etf.entity.Etf;
import com.whatsyouretf.userservice.domain.etf.repository.EtfRepository;
import com.whatsyouretf.userservice.domain.etf.service.EtfReader;
import com.whatsyouretf.userservice.domain.news.dto.*;
import com.whatsyouretf.userservice.domain.news.entity.NewsArticle;
import com.whatsyouretf.userservice.domain.news.repository.NewsArticleRepository;
import com.whatsyouretf.userservice.domain.portfolio.entity.Portfolio;
import com.whatsyouretf.userservice.domain.portfolio.repository.PortfolioRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * 뉴스 서비스 구현체
 */
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
@Slf4j
public class NewsServiceImpl implements com.whatsyouretf.userservice.domain.news.service.NewsService {

    private final NewsArticleRepository newsArticleRepository;
    private final EtfRepository etfRepository;
    private final EtfReader etfReader;
    private final PortfolioRepository portfolioRepository;
    private final ObjectMapper objectMapper;

    private static final int DEFAULT_PAGE_SIZE = 20;
    private static final int MAX_PAGE_SIZE = 50;
    private static final int MAX_ETF_NEWS_SIZE = 50;
    private static final int MAX_RELATED_ETFS = 5;
    private static final int MAX_PORTFOLIO_NEWS = 5;

    @Override
    public NewsPageResponse getLatestNews(String categoryCode, Long lastId, int size) {
        // 페이지 크기 제한 (최대 50)
        int validSize = Math.min(Math.max(size, 1), MAX_PAGE_SIZE);
        // 1개 더 조회해서 hasMore 판단
        Pageable pageable = PageRequest.of(0, validSize + 1);

        List<NewsArticle> articles;

        // 커서 기반 페이징: lastId로 해당 뉴스의 publishedAt 조회
        LocalDateTime lastPublishedAt = null;
        if (lastId != null) {
            lastPublishedAt = newsArticleRepository.findById(lastId)
                    .map(NewsArticle::getPublishedAt)
                    .orElse(null);
        }

        if (categoryCode != null && !categoryCode.isBlank()) {
            // 카테고리 필터 적용
            if (lastId != null && lastPublishedAt != null) {
                articles = newsArticleRepository.findByCategoryCodeByCursor(categoryCode, lastPublishedAt, lastId, pageable);
            } else {
                articles = newsArticleRepository.findByCategoryCodeFirstPage(categoryCode, pageable);
            }
        } else {
            // 전체 조회
            if (lastId != null && lastPublishedAt != null) {
                articles = newsArticleRepository.findLatestNewsByCursor(lastPublishedAt, lastId, pageable);
            } else {
                articles = newsArticleRepository.findLatestNewsFirstPage(pageable);
            }
        }

        // hasMore 판단: 요청한 것보다 1개 더 조회되면 다음 페이지 존재
        boolean hasMore = articles.size() > validSize;

        // 실제 반환할 뉴스 목록 (요청한 크기만큼만)
        List<NewsArticle> resultArticles = hasMore ? articles.subList(0, validSize) : articles;

        List<NewsListResponse> newsList = resultArticles.stream()
                .map(NewsListResponse::from)
                .toList();

        // 다음 커서: 마지막 뉴스의 ID
        Long nextCursor = resultArticles.isEmpty() ? null : resultArticles.get(resultArticles.size() - 1).getId();

        return NewsPageResponse.builder()
                .news(newsList)
                .size(validSize)
                .hasMore(hasMore)
                .nextCursor(hasMore ? nextCursor : null)
                .build();
    }

    @Override
    @Transactional
    public NewsDetailResponse getNewsDetail(Long newsId) {
        NewsArticle article = newsArticleRepository.findByIdWithCategory(newsId)
                .orElseThrow(() -> new BusinessException(ErrorCode.NEWS_NOT_FOUND));

        // 조회수 증가
        article.incrementViewCount();

        // AI 요약 파싱 (JSON: {"bullets": ["요약1", "요약2", ...]} 형식)
        List<String> aiSummary = parseAiSummary(article.getContentSummary());

        // 키워드 파싱 (JSON 배열)
        List<String> keywords = parseKeywords(article.getKeywords());

        // 관련 ETF 목록 조회 (news_stock_mapping → stock → etf_stock_composition → etf)
        List<RelatedEtfResponse> relatedEtfs = getRelatedEtfs(newsId);

        return NewsDetailResponse.from(article, aiSummary, keywords, relatedEtfs);
    }

    /**
     * 뉴스와 관련된 ETF 목록 조회
     * <p>
     * news_stock_mapping의 종목이 포함된 ETF를 비중 높은 순으로 조회
     */
    private List<RelatedEtfResponse> getRelatedEtfs(Long newsId) {
        // 관련 ETF 조회
        List<Etf> etfs = etfRepository.findRelatedEtfsByNewsId(newsId, MAX_RELATED_ETFS);

        if (etfs.isEmpty()) {
            return List.of();
        }

        // ETF ticker 목록
        Set<String> tickers = etfs.stream().map(Etf::getStockCode).collect(Collectors.toSet());

        // Redis 캐시에서 최신 시세 조회 (ETF 목록 API와 동일한 데이터 소스)
        Map<String, EtfCurrentInfo> priceInfoMap = etfReader.getInfosMap(tickers);

        // DTO 변환
        return etfs.stream()
                .map(etf -> RelatedEtfResponse.from(etf, priceInfoMap.get(etf.getStockCode())))
                .toList();
    }

    /**
     * AI 요약 JSON 파싱
     * 형식: {"bullets": ["요약1", "요약2", "요약3"]}
     */
    private List<String> parseAiSummary(String json) {
        if (json == null || json.isBlank()) {
            return List.of();
        }
        try {
            JsonNode root = objectMapper.readTree(json);
            if (root.has("bullets") && root.get("bullets").isArray()) {
                return objectMapper.convertValue(
                        root.get("bullets"),
                        new TypeReference<List<String>>() {}
                );
            }
            return List.of();
        } catch (JsonProcessingException e) {
            log.error("AI 요약 파싱 실패: {}", json, e);
            return List.of();
        }
    }

    /**
     * 키워드 JSON 파싱
     * 형식: ["키워드1", "키워드2", ...]
     */
    private List<String> parseKeywords(String json) {
        if (json == null || json.isBlank()) {
            return List.of();
        }
        try {
            return objectMapper.readValue(json, new TypeReference<List<String>>() {});
        } catch (JsonProcessingException e) {
            log.error("키워드 파싱 실패: {}", json, e);
            return List.of();
        }
    }

    /**
     * 본문 내용 자르기
     */
    private String truncateContent(String content, int maxLength) {
        if (content == null || content.isBlank()) {
            return null;
        }
        if (content.length() <= maxLength) {
            return content;
        }
        return content.substring(0, maxLength) + "...";
    }

    @Override
    public NewsPageResponse searchNews(String keyword, String categoryCode) {
        // 키워드 검증 (1글자 이상 50글자 이하)
        if (keyword == null || keyword.trim().isEmpty() || keyword.trim().length() > 50) {
            throw new BusinessException(ErrorCode.INVALID_KEYWORD);
        }

        Pageable pageable = PageRequest.of(0, DEFAULT_PAGE_SIZE);

        Page<NewsArticle> articlePage;
        if (categoryCode != null && !categoryCode.trim().isEmpty()) {
            articlePage = newsArticleRepository.searchByKeywordAndCategory(keyword.trim(), categoryCode.trim(), pageable);
        } else {
            articlePage = newsArticleRepository.searchByKeyword(keyword.trim(), pageable);
        }

        List<NewsListResponse> newsList = articlePage.getContent().stream()
                .map(NewsListResponse::from)
                .toList();

        return NewsPageResponse.builder()
                .news(newsList)
                .keyword(keyword)
                .size(DEFAULT_PAGE_SIZE)
                .hasMore(false)
                .nextCursor(null)
                .build();
    }

    // TODO: 팀원이 etf/company repository 구현 후 활성화
    @Override
    public EtfNewsResponse getEtfNews(Long etfId, int size) {
        throw new BusinessException(ErrorCode.AI_SERVICE_UNAVAILABLE);
    }

    @Override
    @Cacheable(value = "portfolioNews", key = "#portfolioId")
    public PortfolioNewsResponse getPortfolioNews(Long portfolioId) {
        // 포트폴리오 조회
        Portfolio portfolio = portfolioRepository.findById(portfolioId)
                .orElseThrow(() -> new BusinessException(ErrorCode.PORTFOLIO_NOT_FOUND));

        // 관련성 높은 뉴스 조회
        List<NewsArticle> articles = newsArticleRepository.findPortfolioNewsWithFullData(portfolioId, MAX_PORTFOLIO_NEWS);

        if (articles.isEmpty()) {
            return PortfolioNewsResponse.builder()
                    .portfolioId(portfolio.getId())
                    .portfolioName(portfolio.getName())
                    .news(List.of())
                    .updatedAt(LocalDateTime.now())
                    .build();
        }

        // DTO 변환
        List<PortfolioNewsResponse.PortfolioNewsItem> newsItems = articles.stream()
                .map(article -> PortfolioNewsResponse.PortfolioNewsItem.builder()
                        .id(article.getId())
                        .title(article.getTitle())
                        .summary(truncateContent(article.getContent(), 100))
                        .source(article.getSource())
                        .thumbnailUrl(article.getThumbnailUrl())
                        .publishedAt(article.getPublishedAt())
                        .build())
                .toList();

        // 오늘 오전 9시 기준 시각 계산
        LocalDateTime now = LocalDateTime.now();
        LocalDateTime todayNineAm = now.toLocalDate().atTime(9, 0);
        LocalDateTime updatedAt = now.isBefore(todayNineAm)
                ? todayNineAm.minusDays(1)
                : todayNineAm;

        return PortfolioNewsResponse.builder()
                .portfolioId(portfolio.getId())
                .portfolioName(portfolio.getName())
                .news(newsItems)
                .updatedAt(updatedAt)
                .build();
    }

    /**
     * 매일 오전 9시(KST)에 포트폴리오 뉴스 캐시 초기화
     */
    @Scheduled(cron = "0 0 9 * * *", zone = "Asia/Seoul")
    @CacheEvict(value = "portfolioNews", allEntries = true)
    public void clearPortfolioNewsCache() {
        log.info("포트폴리오 뉴스 캐시 초기화 완료 (매일 오전 9시 KST)");
    }
}
