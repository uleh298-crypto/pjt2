package com.whatsyouretf.userservice.domain.etf.controller;

import com.whatsyouretf.userservice.common.auth.CustomUserDetails;
import com.whatsyouretf.userservice.common.response.ApiResponse;
import com.whatsyouretf.userservice.common.response.PaginatedResponse;
import com.whatsyouretf.userservice.domain.etf.dto.*;
import com.whatsyouretf.userservice.domain.etf.service.EtfService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * ETF 관련 API Controller
 */
@Tag(name = "ETF", description = "ETF API")
@RestController
@RequestMapping("/api/v1/etfs")
@RequiredArgsConstructor
public class EtfController {

    private final EtfService etfService;

    @Operation(summary = "etf 가격 이력 조회", description = "시작일부터 종료일을 기준으로 페이징하여 응답합니다.")
    @GetMapping("/{ticker}/price-history")
    public ResponseEntity<ApiResponse<PaginatedResponse<EtfPriceHistoryResponse>>> getEtfPriceHistories(
        @Valid EtfPriceHistoryRequest request,
        @Parameter(description = "etf 종목 코드") @PathVariable String ticker,
        Pageable pageable
    ) {
            return ResponseEntity
                    .status(HttpStatus.OK)
                    .body(ApiResponse.success(
                        PaginatedResponse.createPaginatedResponse(
                            etfService.getEtfHistory(ticker, request.getStartDate(), request.getEndDate(), pageable)
                                .map(EtfPriceHistoryResponse::from))
                    ));
    }

    @GetMapping("/{ticker}")
    @Operation(summary = "etf 단건 조회", description = "etf의 종목 코드를 기준으로 etf 상세 조회를 응답합니다")
    public ResponseEntity<ApiResponse<EtfDetailResponse>> getEtfDetail(@Parameter(description = "etf 종목 코드") @PathVariable String ticker) {
            return ResponseEntity
                    .status(HttpStatus.OK)
                    .body(ApiResponse.success(
                            EtfDetailResponse.from(
                                etfService.getEtfDetail(ticker),
                                etfService.getEtfCurrentInfo(ticker))));
    }

    @GetMapping("/{ticker}/clusters")
    @Operation(summary = "etf 클러스터 조회", description = "클러스터 탭 응답")
    public ResponseEntity<ApiResponse<EtfClusterResponse>> getEtfCluster(@Parameter(description = "etf 종목 코드") @PathVariable String ticker) {
        return ResponseEntity
            .status(HttpStatus.OK)
            .body(ApiResponse.success(
                    etfService.getClusterData(ticker)));
    }

    @PostMapping
    @Operation(summary = "etf 목록 조회", description = "etf 조건에 맞는 etf 목록을 페이징하여 응답합니다")
    public ResponseEntity<ApiResponse<PaginatedResponse<EtfListResponse>>> getEtfList(
            @Parameter(description = "etf 검색 조건") @RequestBody EtfListRequest request,
            @AuthenticationPrincipal CustomUserDetails userDetails,
            Pageable pageable
    ) {
            Long userId = userDetails != null ? userDetails.getUserId() : null;
            return ResponseEntity
                    .status(HttpStatus.OK)
                    .body(ApiResponse.success(PaginatedResponse.createPaginatedResponse(
                                    etfService.getEtfList(request.toQuery(userId), pageable)
                                            .map(etfSummary -> EtfListResponse.of(
                                                    etfSummary,
                                                    etfService.getEtfCurrentInfo(etfSummary.ticker()))))));
    }

    @GetMapping
    @Operation(summary = "etf top10 조회", description = "etf 실시간 거래량 top10 을 조회합니다.")
    public ResponseEntity<ApiResponse<List<EtfTopTenListResponse>>> getTopTenEtfList() {
        return ResponseEntity
                .status(HttpStatus.OK)
                .body(ApiResponse.success(
                        etfService.getTopTenList()
                                .stream()
                                .map(EtfTopTenListResponse::from)
                                .toList()
                ));
    }

    @GetMapping("/{ticker}/market-data")
    @Operation(summary = "etf 시장 데이터 조회", description = "캐시에서 현재가 / 등락률 / 거래량을 조회합니다")
    public ResponseEntity<ApiResponse<EtfMarketDataResponse>> getEtfMarketData(@Parameter(description = "etf 종목 코드") @PathVariable String ticker) {
        return ResponseEntity
                .status(HttpStatus.OK)
                .body(ApiResponse.success(
                        EtfMarketDataResponse.from(etfService.getEtfCurrentInfo(ticker))));
    }

    @GetMapping("/{ticker}/dividends")
    @Operation(summary = "etf 배당이력 조회", description = "etf의 종목 코드를 기준으로 etf 배당 이력을 응답합니다")
    public ResponseEntity<ApiResponse<List<EtfDividendsResponse>>> getEtfDividends(@Parameter(description = "etf 종목 코드") @PathVariable String ticker) {
        return ResponseEntity
            .status(HttpStatus.OK)
            .body(ApiResponse.success(etfService.getEtfDividends(ticker).stream()
                .map(EtfDividendsResponse::of)
                .toList()));
    }
}
