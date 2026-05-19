package com.whatsyouretf.userservice.domain.user.controller;

import com.whatsyouretf.userservice.common.auth.CustomUserDetails;
import com.whatsyouretf.userservice.common.response.ApiResponse;
import com.whatsyouretf.userservice.domain.user.dto.*;
import com.whatsyouretf.userservice.domain.user.service.impl.MyDataEtfCount;
import com.whatsyouretf.userservice.domain.user.service.UserService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

/**
 * 사용자 관련 API 컨트롤러
 * <p>
 * 사용자 정보 조회/수정, 관심 ETF, 보유 ETF(마이데이터) 기능을 제공합니다.
 */
@Tag(name = "User", description = "사용자 API")
@RestController
@RequestMapping("/api/v1/users")
@RequiredArgsConstructor
@SecurityRequirement(name = "bearerAuth")
public class UserController {

    private final UserService userService;

    // ==================== 사용자 정보 ====================

    /**
     * 내 정보 조회
     *
     * @param userDetails 인증된 사용자 정보
     * @return 사용자 정보
     */
    @Operation(summary = "내 정보 조회", description = "로그인한 사용자의 정보를 조회합니다.")
    @GetMapping("/me")
    public ResponseEntity<ApiResponse<UserResponse>> getMyInfo(
            @AuthenticationPrincipal CustomUserDetails userDetails
    ) {
        UserResponse response = userService.getMyInfo(userDetails.getUserId());
        return ResponseEntity.ok(ApiResponse.success(response));
    }

    /**
     * 프로필 수정
     *
     * @param userDetails 인증된 사용자 정보
     * @param request     수정할 프로필 정보
     * @return 수정된 사용자 정보
     */
    @Operation(summary = "프로필 수정", description = "사용자 프로필을 수정합니다.")
    @PatchMapping("/me")
    public ResponseEntity<ApiResponse<UserResponse>> updateProfile(
            @AuthenticationPrincipal CustomUserDetails userDetails,
            @Valid @RequestBody UserUpdateRequest request
    ) {
        UserResponse response = userService.updateProfile(userDetails.getUserId(), request);
        return ResponseEntity.ok(ApiResponse.success("프로필 수정 성공", response));
    }

    /**
     * 닉네임 중복 체크
     *
     * @param nickname 확인할 닉네임
     * @return true = 사용 가능, false = 중복
     */
    @Operation(summary = "닉네임 중복 체크", description = "닉네임 사용 가능 여부를 확인합니다.")
    @GetMapping("/check-nickname")
    public ResponseEntity<ApiResponse<Boolean>> checkNickname(@RequestParam String nickname) {
        boolean isDuplicate = userService.checkNicknameDuplicate(nickname);
        return ResponseEntity.ok(ApiResponse.success(!isDuplicate));
    }

    /**
     * 특정 사용자 조회
     *
     * @param userId 사용자 ID
     * @return 사용자 공개 정보
     */
    @Operation(summary = "사용자 조회", description = "특정 사용자의 공개 정보를 조회합니다.")
    @GetMapping("/{userId}")
    public ResponseEntity<ApiResponse<UserResponse>> getUserById(@PathVariable Long userId) {
        UserResponse response = userService.getUserById(userId);
        return ResponseEntity.ok(ApiResponse.success(response));
    }

    /**
     * 회원 탈퇴 (hard delete)
     *
     * @param userDetails 인증된 사용자 정보
     * @return 성공 응답
     */
    @Operation(summary = "회원 탈퇴", description = "회원 탈퇴를 진행합니다. 모든 데이터가 영구 삭제됩니다.")
    @DeleteMapping("/me")
    public ResponseEntity<ApiResponse<Void>> deleteAccount(
            @AuthenticationPrincipal CustomUserDetails userDetails
    ) {
        userService.deleteUser(userDetails.getUserId());
        return ResponseEntity.ok(ApiResponse.success("회원 탈퇴 완료"));
    }

    // ==================== 프로필 이미지 ====================

    /**
     * 프로필 이미지 업로드
     *
     * @param userDetails 인증된 사용자 정보
     * @param file        업로드할 이미지 파일
     * @return 업로드된 이미지 URL
     */
    @Operation(summary = "프로필 이미지 업로드", description = "프로필 이미지를 업로드합니다. 기존 이미지가 있으면 교체됩니다.")
    @PostMapping(value = "/me/profile-image", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<ApiResponse<String>> uploadProfileImage(
            @AuthenticationPrincipal CustomUserDetails userDetails,
            @Parameter(description = "이미지 파일 (jpg, jpeg, png, gif, webp / 최대 5MB)")
            @RequestParam("file") MultipartFile file
    ) {
        String imageUrl = userService.uploadProfileImage(userDetails.getUserId(), file);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success("프로필 이미지 업로드 완료", imageUrl));
    }

    /**
     * 프로필 이미지 삭제
     *
     * @param userDetails 인증된 사용자 정보
     * @return 성공 응답
     */
    @Operation(summary = "프로필 이미지 삭제", description = "프로필 이미지를 삭제합니다.")
    @DeleteMapping("/me/profile-image")
    public ResponseEntity<ApiResponse<Void>> deleteProfileImage(
            @AuthenticationPrincipal CustomUserDetails userDetails
    ) {
        userService.deleteProfileImage(userDetails.getUserId());
        return ResponseEntity.ok(ApiResponse.success("프로필 이미지 삭제 완료"));
    }

    // ==================== 관심 ETF ====================

    /**
     * 관심 ETF 목록 조회
     *
     * @param userDetails 인증된 사용자 정보
     * @param sort        정렬 기준 (RECENT, CHANGE_RATE_DESC, CHANGE_RATE_ASC, NAME_ASC)
     * @return 관심 ETF 목록 (최신 시세 포함)
     */
    @Operation(summary = "관심 ETF 목록 조회", description = "로그인한 사용자의 관심 ETF 목록을 조회합니다.")
    @GetMapping("/me/favorites/etfs")
    public ResponseEntity<ApiResponse<FavoriteEtfListResponse>> getFavoriteEtfs(
            @AuthenticationPrincipal CustomUserDetails userDetails,
            @Parameter(description = "정렬 기준: RECENT(최근등록순), CHANGE_RATE_DESC(등락률높은순), CHANGE_RATE_ASC(등락률낮은순), NAME_ASC(이름순)")
            @RequestParam(defaultValue = "RECENT") FavoriteSortType sort
    ) {
        FavoriteEtfListResponse response = userService.getFavoriteEtfs(userDetails.getUserId(), sort);
        return ResponseEntity.ok(ApiResponse.success(response));
    }

    /**
     * 관심 ETF 추가
     *
     * @param userDetails 인증된 사용자 정보
     * @param ticker      ETF 종목코드
     * @return 성공 응답
     */
    @Operation(summary = "관심 ETF 추가", description = "ETF를 관심 목록에 추가합니다.")
    @PostMapping("/me/favorites/etfs/{ticker}")
    public ResponseEntity<ApiResponse<Void>> addFavoriteEtf(
            @AuthenticationPrincipal CustomUserDetails userDetails,
            @Parameter(description = "ETF 종목코드") @PathVariable String ticker
    ) {
        userService.addFavoriteEtf(userDetails.getUserId(), ticker);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success("관심 ETF 추가 완료"));
    }

    /**
     * 관심 ETF 삭제
     *
     * @param userDetails 인증된 사용자 정보
     * @param ticker      ETF 종목코드
     * @return 성공 응답
     */
    @Operation(summary = "관심 ETF 삭제", description = "ETF를 관심 목록에서 삭제합니다.")
    @DeleteMapping("/me/favorites/etfs/{ticker}")
    public ResponseEntity<ApiResponse<Void>> removeFavoriteEtf(
            @AuthenticationPrincipal CustomUserDetails userDetails,
            @Parameter(description = "ETF 종목코드") @PathVariable String ticker
    ) {
        userService.removeFavoriteEtf(userDetails.getUserId(), ticker);
        return ResponseEntity.ok(ApiResponse.success("관심 ETF 삭제 완료"));
    }

    /**
     * 관심 ETF 여부 확인
     *
     * @param userDetails 인증된 사용자 정보
     * @param ticker      ETF 종목코드
     * @return true = 관심 등록됨
     */
    @Operation(summary = "관심 ETF 여부 확인", description = "특정 ETF가 관심 목록에 있는지 확인합니다.")
    @GetMapping("/me/favorites/etfs/{ticker}/check")
    public ResponseEntity<ApiResponse<Boolean>> checkFavoriteEtf(
            @AuthenticationPrincipal CustomUserDetails userDetails,
            @Parameter(description = "ETF 종목코드") @PathVariable String ticker
    ) {
        boolean isFavorite = userService.isFavoriteEtf(userDetails.getUserId(), ticker);
        return ResponseEntity.ok(ApiResponse.success(isFavorite));
    }


    /**
     * 관심 ETF 여부 확인
     *
     * @param userDetails 인증된 사용자 정보
     * @return true = 관심 등록됨
     */
    @Operation(summary = "마이데이터 정보 조회", description = "호출 시점에 보유 중인 마이데이터의 etf 포트폴리오를 조회합니다.")
    @GetMapping("/me/my-data")
    public ResponseEntity<ApiResponse<List<MyDataEtfCount>>> getEtfPortfolio(
        @AuthenticationPrincipal CustomUserDetails userDetails
    ) {
        return ResponseEntity.ok(ApiResponse.success(userService.getMyData(userDetails.getUserId())));
    }

    @Operation(summary = "마이데이터 동의 여부 확인", description = "마이데이터 동의 여부를 확인합니다")
    @GetMapping("/me/my-data/accepted")
    public ResponseEntity<ApiResponse<Boolean>> checkMyDataAccepted(
        @AuthenticationPrincipal CustomUserDetails userDetails
    ) {
        return ResponseEntity.ok(ApiResponse.success(userService.checkUserAcceptedMyData(userDetails.getUserId())));
    }

    @Operation(summary = "마이데이터 동의", description = "마이데이터 수집을 동의합니다")
    @PostMapping("/me/my-data")
    public ResponseEntity<ApiResponse<Void>> acceptMyData(
        @AuthenticationPrincipal CustomUserDetails userDetails
    ) {
        userService.acceptMyData(userDetails.getUserId());
        return ResponseEntity.ok(ApiResponse.success(null));
    }
}
