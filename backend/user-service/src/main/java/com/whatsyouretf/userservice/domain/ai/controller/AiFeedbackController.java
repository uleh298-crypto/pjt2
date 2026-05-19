package com.whatsyouretf.userservice.domain.ai.controller;

import com.whatsyouretf.userservice.common.auth.CustomUserDetails;
import com.whatsyouretf.userservice.common.response.ApiResponse;
import com.whatsyouretf.userservice.domain.ai.dto.PortfolioReviewRequest;
import com.whatsyouretf.userservice.domain.ai.dto.PortfolioReviewResponse;
import com.whatsyouretf.userservice.domain.ai.service.AiFeedbackService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

/**
 * AI 피드백 API 컨트롤러
 * <p>
 * 포트폴리오 AI 리뷰 관련 API를 제공합니다.
 */
@Tag(name = "AI Feedback", description = "포트폴리오 AI 피드백 API")
@RestController
@RequestMapping("/api/v1/ai")
@RequiredArgsConstructor
public class AiFeedbackController {

    private final AiFeedbackService aiFeedbackService;

    /**
     * 포트폴리오 AI 리뷰 요청
     */
    @Operation(summary = "포트폴리오 AI 리뷰 요청",
            description = "사용자가 구성한 포트폴리오에 대해 AI 분석을 요청합니다.")
    @PostMapping("/portfolio/review")
    public ResponseEntity<ApiResponse<PortfolioReviewResponse>> requestReview(
            @AuthenticationPrincipal CustomUserDetails userDetails,
            @Valid @RequestBody PortfolioReviewRequest request
    ) {
        PortfolioReviewResponse response = aiFeedbackService.requestReview(
                userDetails.getUserId(), request);
        return ResponseEntity.status(org.springframework.http.HttpStatus.CREATED)
                .body(ApiResponse.success(response));
    }

}
