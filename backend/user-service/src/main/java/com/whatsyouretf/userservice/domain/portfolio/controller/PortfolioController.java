package com.whatsyouretf.userservice.domain.portfolio.controller;

import com.whatsyouretf.userservice.common.auth.CustomUserDetails;
import com.whatsyouretf.userservice.common.response.ApiResponse;
import com.whatsyouretf.userservice.domain.portfolio.repository.PortfolioInfo;
import com.whatsyouretf.userservice.domain.portfolio.service.PortfolioDetail;
import com.whatsyouretf.userservice.domain.portfolio.service.PortfolioFacade;
import com.whatsyouretf.userservice.domain.portfolio.repository.PortfolioIssues;
import com.whatsyouretf.userservice.domain.portfolio.service.PortfolioService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Tag(name = "Portfolio", description = "포트폴리오 API")
@RestController
@RequestMapping("/api/v1/portfolios")
@RequiredArgsConstructor
@SecurityRequirement(name = "bearerAuth")
public class PortfolioController {
        private final PortfolioFacade portfolioFacade;
        private final PortfolioService portfolioService;

        @Operation(summary = "포트폴리오 저장", description = "사용자가 커스텀한 포트폴리오를 저장합니다")
        @PostMapping
        public ResponseEntity<ApiResponse<Void>> savePortfolio(
                @RequestBody SavePortfolioRequest request,
                @AuthenticationPrincipal CustomUserDetails userDetails
        ) {
                portfolioFacade.savePortfolio(
                        request.etfs().stream()
                                .map(PortfolioEtfCount::toQuery)
                                .toList(),
                        userDetails.getUserId(),
                        request.portfolioName(),
                        request.investAmount(),
                        request.investPeriod(),
                        request.portfolioType()
                );

                return ResponseEntity
                        .status(HttpStatus.OK)
                        .body(ApiResponse.success("포트폴리오를 저장하였습니다"));
        }

        @Operation(summary = "포트폴리오 목록 조회", description = "사용자가 커스텀한 포트폴리오 목록을 조회합니다")
        @GetMapping
        public ResponseEntity<ApiResponse<List<PortfolioInfo>>> getPortfolios(
                @AuthenticationPrincipal CustomUserDetails userDetails
        ) {

                return ResponseEntity
                        .status(HttpStatus.OK)
                        .body(ApiResponse.success(portfolioFacade.getPortfolioList(userDetails.getUserId())));
        }

        @Operation(summary = "포트폴리오 상세 조회", description = "사용자가 커스텀한 포트폴리오를 상세 조회합니다")
        @GetMapping("/{portfolioId}")
        public ResponseEntity<ApiResponse<PortfolioDetail>> getPortfolioDetail(
                @AuthenticationPrincipal CustomUserDetails userDetails,
                @PathVariable Long portfolioId
        ) {
                return ResponseEntity
                        .status(HttpStatus.OK)
                        .body(ApiResponse.success(PortfolioDetail.of(portfolioService.getPortfolio(userDetails.getUserId(), portfolioId))));
        }

        @Operation(summary = "포트폴리오 수정", description = "사용자가 커스텀한 포트폴리오를 수정합니다")
        @PutMapping
        public ResponseEntity<ApiResponse<Void>> updatePortfolio(
            @RequestBody UpdatePortfolioRequest request
        ) {
                portfolioService.updatePortfolio(request.portfolioId(), request.name());

                return ResponseEntity
                    .status(HttpStatus.OK)
                    .body(ApiResponse.success("포트폴리오 이름을 수정하였습니다"));
        }


        @Operation(summary = "포트폴리오 삭제", description = "사용자가 커스텀한 포트폴리오를 삭제합니다")
        @DeleteMapping("/{portfolioId}")
        public ResponseEntity<ApiResponse<Void>> deletePortfolio(@PathVariable Long portfolioId) {
                portfolioService.deletePortfolio(portfolioId);

                return ResponseEntity
                    .status(HttpStatus.OK)
                    .body(ApiResponse.success("포트폴리오를 삭제하였습니다"));
        }

        @Operation(summary = "포트폴리오 이슈 조회", description = "포트폴리오의 주요 이슈 목록을 조회합니다/")
        @GetMapping("/{portfolioId}/issues")
        public ResponseEntity<ApiResponse<List<PortfolioIssues>>> getPortfolioIssues(@PathVariable Long portfolioId) {
                return ResponseEntity
                    .status(HttpStatus.OK)
                    .body(ApiResponse.success(portfolioService.getPortfolioIssues(portfolioId)));
        }
}
