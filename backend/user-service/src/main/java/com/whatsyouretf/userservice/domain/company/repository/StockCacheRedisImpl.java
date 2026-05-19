package com.whatsyouretf.userservice.domain.company.repository;

import com.whatsyouretf.userservice.domain.company.service.StockCache;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

/**
 * Redis 기반 Stock 현재 정보 캐시 구현
 * data-service에서 주기적으로 업데이트하는 캐시 데이터를 조회합니다.
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class StockCacheRedisImpl implements StockCache {
    private final RedisTemplate<String, String> redisTemplate;

    private static final String HASH_PREFIX = "StockInfo:";

    @Override
    public StockInfo get(String ticker, String description) {
        try {
            String key = HASH_PREFIX + ticker;
            var hashOps = redisTemplate.opsForHash();

            // Redis에서 Hash 데이터 모두 조회
            var data = hashOps.entries(key);

            if (data == null || data.isEmpty()) {
                log.debug("[{}] Redis에 캐시 데이터 없음", ticker);
                return null;
            }

            // Hash 필드값들을 StockInfo로 변환
            String tickerStr = (String) data.get("ticker");
            if (tickerStr == null) {
                tickerStr = ticker;  // 폴백
            }

            String stockName = (String) data.get("stockName");
            if (stockName == null) {
                stockName = "";
            }

            String currentPriceStr = (String) data.get("currentPrice");
            String previousPriceStr = (String) data.get("previousPrice");
            String dailyFluctuationStr = (String) data.get("dailyFluctuation");
            String dailyReturnStr = (String) data.get("dailyReturn");
            String marketCapStr = (String) data.get("marketCapitalization");

            if (currentPriceStr == null || previousPriceStr == null) {
                log.warn("[{}] 필수 필드 부족", ticker);
                return null;
            }

            // StockInfo 생성자로 직접 객체 생성
            return new StockInfo(
                    tickerStr,
                    stockName,
                    new BigDecimal(currentPriceStr),
                    new BigDecimal(previousPriceStr),
                    new BigDecimal(dailyFluctuationStr != null ? dailyFluctuationStr : "0"),
                    new BigDecimal(dailyReturnStr != null ? dailyReturnStr : "0"),
                    new BigDecimal(marketCapStr != null ? marketCapStr : "0"),
                    description
            );
        } catch (Exception e) {
            log.error("[{}] Redis 조회 실패: {}", ticker, e.getMessage());
            return null;
        }
    }

    @Override
    public Map<String, StockInfo> getAll(Set<String> tickers) {
        Map<String, StockInfo> result = new HashMap<>();

        if (tickers == null || tickers.isEmpty()) {
            return result;
        }

        try {
            var hashOps = redisTemplate.opsForHash();

            for (String ticker : tickers) {
                String key = HASH_PREFIX + ticker;
                var data = hashOps.entries(key);

                if (data == null || data.isEmpty()) {
                    continue;
                }

                String currentPriceStr = (String) data.get("currentPrice");
                String previousPriceStr = (String) data.get("previousPrice");

                if (currentPriceStr == null || previousPriceStr == null) {
                    continue;
                }

                String dailyReturnStr = (String) data.get("dailyReturn");

                StockInfo stockInfo = new StockInfo(
                        ticker,
                        (String) data.getOrDefault("stockName", ""),
                        new BigDecimal(currentPriceStr),
                        new BigDecimal(previousPriceStr),
                        new BigDecimal((String) data.getOrDefault("dailyFluctuation", "0")),
                        new BigDecimal(dailyReturnStr != null ? dailyReturnStr : "0"),
                        new BigDecimal((String) data.getOrDefault("marketCapitalization", "0")),
                        null
                );

                result.put(ticker, stockInfo);
            }
        } catch (Exception e) {
            log.error("Redis 배치 조회 실패: {}", e.getMessage());
        }

        return result;
    }
}
