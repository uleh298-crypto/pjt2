package com.whatsyouretf.userservice.domain.index.controller;

import com.whatsyouretf.userservice.common.response.ApiResponse;
import com.whatsyouretf.userservice.common.response.PaginatedResponse;
import com.whatsyouretf.userservice.domain.index.entity.MarketType;
import com.whatsyouretf.userservice.domain.index.repository.IndexSummary;
import com.whatsyouretf.userservice.domain.index.service.IndexService;
import io.swagger.v3.oas.annotations.Operation;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/index")
@RequiredArgsConstructor
public class IndexController {
        private final IndexService indexService;

        @GetMapping
        @Operation(summary = "인덱스 이력 조회", description = "26년 3월 2일부터 현재일까지 기준으로 페이징하여 응답합니다.")
        public ResponseEntity<ApiResponse<PaginatedResponse<IndexSummary>>> getIndex(
                @RequestParam MarketType marketType,
                Pageable pageable
        ) {
                return ResponseEntity
                        .status(HttpStatus.OK)
                        .body(ApiResponse.success(PaginatedResponse.createPaginatedResponse(indexService.getIndexHistory(marketType, pageable))));
        }
}
