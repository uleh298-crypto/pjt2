package com.whatsyouretf.userservice.domain.company.controller;

import com.whatsyouretf.userservice.common.response.ApiResponse;
import com.whatsyouretf.userservice.domain.company.dto.RelatedStockResponse;
import com.whatsyouretf.userservice.domain.company.repository.StockInfo;
import com.whatsyouretf.userservice.domain.company.service.EtfIncludesStock;
import com.whatsyouretf.userservice.domain.company.service.StockFacade;
import com.whatsyouretf.userservice.domain.company.service.StockService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

/**
 * 종목 API 컨트롤러
 */
@Tag(name = "Stock", description = "종목 API")
@RestController
@RequestMapping("/api/v1/stocks")
@RequiredArgsConstructor
public class StockController {

    private final StockService stockService;
    private final StockFacade stockFacade;

    /**
     * 종목 태그 조회
     */
    @Operation(summary = "종목 태그 조회", description = "종목의 태그(시장유형, 산업그룹, 산업분류)를 조회합니다.")
    @GetMapping("/{ticker}/tags")
    public ResponseEntity<ApiResponse<List<String>>> getStockTags(
            @Parameter(description = "종목 티커 (6자리)") @PathVariable String ticker
    ) {
        List<String> tags = stockService.getStockTags(ticker);
        return ResponseEntity.ok(ApiResponse.success(tags));
    }

    /**
     * 관련 종목 조회
     */
    @Operation(summary = "관련 종목 조회", description = "같은 산업분류의 관련 종목 3개를 조회합니다.")
    @GetMapping("/{ticker}/related")
    public ResponseEntity<ApiResponse<List<RelatedStockResponse>>> getRelatedStocks(
            @Parameter(description = "종목 티커 (6자리)") @PathVariable String ticker
    ) {
        List<RelatedStockResponse> response = stockService.getRelatedStocks(ticker);
        return ResponseEntity.ok(ApiResponse.success(response));
    }

    @Operation(summary = "주식 시세 조회", description = "주식의 시세를 조회합니다.")
    @GetMapping("/{ticker}")
    public ResponseEntity<ApiResponse<StockInfo>> getStockInfo(
        @Parameter(description = "종목 티커 (6자리)") @PathVariable String ticker
    ) {
        return ResponseEntity.ok(
            ApiResponse.success(stockService.getStockInfo(ticker)));
    }

    @Operation(summary = "주식 종목 포함 etf 조회", description = "주식이 포함된 etf를 조회합니다.")
    @GetMapping("/{ticker}/etfs")
    public ResponseEntity<ApiResponse<List<EtfIncludesStock>>> getStockEtfs(
        @Parameter(description = "종목 티커 (6자리)") @PathVariable String ticker
    ) {
        return ResponseEntity
                .ok(ApiResponse.success(stockFacade.getIncludingStock(ticker)));
    }
}
