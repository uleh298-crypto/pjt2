package com.whatsyouretf.userservice.domain.user.service.impl;

import com.whatsyouretf.userservice.common.exception.BusinessException;
import com.whatsyouretf.userservice.common.exception.ErrorCode;
import com.whatsyouretf.userservice.common.service.FileStorageService;
import com.whatsyouretf.userservice.domain.ai.repository.PortfolioAiFeedbackRepository;
import com.whatsyouretf.userservice.domain.alert.repository.FcmTokenRepository;
import com.whatsyouretf.userservice.domain.alert.repository.UserAlertRepository;
import com.whatsyouretf.userservice.domain.alert.repository.UserNotificationSettingRepository;
import com.whatsyouretf.userservice.domain.etf.dto.EtfCurrentInfo;
import com.whatsyouretf.userservice.domain.simulation.repository.SimulationRepository;
import com.whatsyouretf.userservice.domain.etf.entity.Etf;
import com.whatsyouretf.userservice.domain.etf.repository.EtfRepository;
import com.whatsyouretf.userservice.domain.etf.service.EtfReader;
import com.whatsyouretf.userservice.domain.etf.service.EtfService;
import com.whatsyouretf.userservice.domain.portfolio.entity.Portfolio;
import com.whatsyouretf.userservice.domain.portfolio.entity.PortfolioEtf;
import com.whatsyouretf.userservice.domain.portfolio.repository.PortfolioEtfRepository;
import com.whatsyouretf.userservice.domain.portfolio.repository.PortfolioRepository;
import com.whatsyouretf.userservice.domain.user.dto.*;
import com.whatsyouretf.userservice.domain.user.entity.User;
import com.whatsyouretf.userservice.domain.user.entity.UserFavoriteEtf;
import com.whatsyouretf.userservice.domain.user.repository.LoginHistoryRepository;
import com.whatsyouretf.userservice.domain.user.repository.PasswordResetTokenRepository;
import com.whatsyouretf.userservice.domain.user.repository.RefreshTokenRepository;
import com.whatsyouretf.userservice.domain.user.repository.UserFavoriteEtfRepository;
import com.whatsyouretf.userservice.domain.user.repository.UserHoldingEtfRepository;
import com.whatsyouretf.userservice.domain.user.repository.UserRepository;
import com.whatsyouretf.userservice.domain.user.service.MyDataApi;
import com.whatsyouretf.userservice.domain.user.service.UserService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class UserServiceImpl implements UserService {

    private static final String PROFILE_IMAGE_DIRECTORY = "profiles";

    private final UserRepository userRepository;
    private final UserFavoriteEtfRepository userFavoriteEtfRepository;
    private final UserHoldingEtfRepository userHoldingEtfRepository;
    private final EtfRepository etfRepository;
    private final EtfReader etfReader;
    private final FileStorageService fileStorageService;
    private final PortfolioRepository portfolioRepository;
    private final PortfolioEtfRepository portfolioEtfRepository;
    private final EtfService etfService;
    private final UserAlertRepository userAlertRepository;
    private final UserNotificationSettingRepository userNotificationSettingRepository;
    private final FcmTokenRepository fcmTokenRepository;
    private final LoginHistoryRepository loginHistoryRepository;
    private final PasswordResetTokenRepository passwordResetTokenRepository;
    private final RefreshTokenRepository refreshTokenRepository;
    private final PortfolioAiFeedbackRepository portfolioAiFeedbackRepository;
    private final SimulationRepository simulationRepository;
    private final MyDataApi myDataApi;

    @Override
    public UserResponse getUserById(Long userId) {
        User user = userRepository.findByIdWithSocialAccounts(userId)
                .orElseThrow(() -> new BusinessException(ErrorCode.USER_NOT_FOUND));
        return UserResponse.from(user);
    }

    @Override
    public UserResponse getMyInfo(Long userId) {
        return getUserById(userId);
    }

    // ==================== 프로필 이미지 ====================

    @Override
    @Transactional
    public String uploadProfileImage(Long userId, MultipartFile file) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new BusinessException(ErrorCode.USER_NOT_FOUND));

        // 기존 프로필 이미지가 있으면 삭제
        String oldProfileImage = user.getProfileImage();
        if (oldProfileImage != null && !oldProfileImage.isBlank()) {
            fileStorageService.delete(oldProfileImage);
            log.info("기존 프로필 이미지 삭제: {}", oldProfileImage);
        }

        // 새 이미지 업로드
        String newImageUrl = fileStorageService.upload(file, PROFILE_IMAGE_DIRECTORY);

        // 사용자 프로필 이미지 URL 업데이트
        user.updateProfile(user.getNickname(), newImageUrl);

        log.info("프로필 이미지 업로드 완료: userId={}, url={}", userId, newImageUrl);
        return newImageUrl;
    }

    @Override
    @Transactional
    public void deleteProfileImage(Long userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new BusinessException(ErrorCode.USER_NOT_FOUND));

        String profileImage = user.getProfileImage();
        if (profileImage == null || profileImage.isBlank()) {
            log.info("삭제할 프로필 이미지 없음: userId={}", userId);
            return;
        }

        // 파일 삭제
        boolean deleted = fileStorageService.delete(profileImage);
        if (!deleted) {
            log.warn("프로필 이미지 파일 삭제 실패 (이미 삭제되었거나 존재하지 않음): {}", profileImage);
        }

        // 사용자 프로필 이미지 URL 제거
        user.clearProfileImage();
        log.info("프로필 이미지 삭제 완료: userId={}", userId);
    }

    @Override
    @Transactional
    public UserResponse updateProfile(Long userId, UserUpdateRequest request) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new BusinessException(ErrorCode.USER_NOT_FOUND));

        // 닉네임 중복 체크
        if (request.getNickname() != null && !request.getNickname().equals(user.getNickname())) {
            if (userRepository.existsByNickname(request.getNickname())) {
                throw new BusinessException(ErrorCode.DUPLICATE_NICKNAME);
            }
        }

        user.updateProfile(request.getNickname(), null);

        return UserResponse.fromWithoutSocialAccounts(user);
    }

    @Override
    public boolean checkNicknameDuplicate(String nickname) {
        return userRepository.existsByNickname(nickname);
    }

    @Override
    @Transactional
    public void deleteUser(Long userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new BusinessException(ErrorCode.USER_NOT_FOUND));

        // 프로필 이미지 삭제
        String profileImage = user.getProfileImage();
        if (profileImage != null && !profileImage.isBlank()) {
            fileStorageService.delete(profileImage);
            log.info("프로필 이미지 삭제: {}", profileImage);
        }

        // 관련 데이터 삭제 (순서 중요: FK 제약조건 고려)
        userAlertRepository.deleteAllByUserId(userId);
        userNotificationSettingRepository.deleteAllByUserId(userId);
        fcmTokenRepository.deleteAllByUserId(userId);
        loginHistoryRepository.deleteAllByUserId(userId);
        passwordResetTokenRepository.deleteAllByUserId(userId);
        refreshTokenRepository.deleteAllByUserId(userId);
        portfolioAiFeedbackRepository.deleteAllByUserId(userId);
        simulationRepository.deleteAllByUserId(userId);
        portfolioRepository.deleteAllByUserId(userId);
        userFavoriteEtfRepository.deleteAllByUserId(userId);
        userHoldingEtfRepository.deleteAllByUserId(userId);
        // socialAccounts는 User 엔티티에서 cascade로 자동 삭제됨

        // 사용자 삭제 (hard delete)
        userRepository.delete(user);
        log.info("User deleted (hard delete): {}", userId);
    }

    // ==================== 관심 ETF ====================

    @Override
    public FavoriteEtfListResponse getFavoriteEtfs(Long userId, FavoriteSortType sortType) {
        // 사용자 존재 확인
        if (!userRepository.existsById(userId)) {
            throw new BusinessException(ErrorCode.USER_NOT_FOUND);
        }

        // 관심 ETF 목록 조회
        List<UserFavoriteEtf> favorites = userFavoriteEtfRepository.findAllByUserIdWithEtf(userId);

        if (favorites.isEmpty()) {
            return FavoriteEtfListResponse.of(List.of());
        }

        // ETF ticker 목록 추출
        Set<String> tickers = favorites.stream()
                .map(f -> f.getEtf().getStockCode())
                .collect(Collectors.toSet());

        // Redis 캐시에서 최신 시세 조회
        Map<String, EtfCurrentInfo> priceInfoMap = etfReader.getInfosMap(tickers);

        // DTO 변환
        List<FavoriteEtfResponse> responses = favorites.stream()
                .map(f -> FavoriteEtfResponse.from(f, priceInfoMap.get(f.getEtf().getStockCode())))
                .collect(Collectors.toList());

        // 정렬 적용
        sortFavorites(responses, sortType);

        return FavoriteEtfListResponse.of(responses);
    }

    /**
     * 관심 ETF 정렬
     */
    private void sortFavorites(List<FavoriteEtfResponse> favorites, FavoriteSortType sortType) {
        if (sortType == null) {
            sortType = FavoriteSortType.RECENT;
        }

        switch (sortType) {
            case CHANGE_RATE_DESC -> favorites.sort((a, b) -> {
                if (a.getChangeRate() == null && b.getChangeRate() == null) return 0;
                if (a.getChangeRate() == null) return 1;
                if (b.getChangeRate() == null) return -1;
                return b.getChangeRate().compareTo(a.getChangeRate());
            });
            case CHANGE_RATE_ASC -> favorites.sort((a, b) -> {
                if (a.getChangeRate() == null && b.getChangeRate() == null) return 0;
                if (a.getChangeRate() == null) return 1;
                if (b.getChangeRate() == null) return -1;
                return a.getChangeRate().compareTo(b.getChangeRate());
            });
            case NAME_ASC -> favorites.sort((a, b) -> {
                if (a.getName() == null && b.getName() == null) return 0;
                if (a.getName() == null) return 1;
                if (b.getName() == null) return -1;
                return a.getName().compareTo(b.getName());
            });
            case RECENT -> favorites.sort((a, b) -> {
                if (a.getFavoritedAt() == null && b.getFavoritedAt() == null) return 0;
                if (a.getFavoritedAt() == null) return 1;
                if (b.getFavoritedAt() == null) return -1;
                return b.getFavoritedAt().compareTo(a.getFavoritedAt());
            });
        }
    }

    @Override
    @Transactional
    public void addFavoriteEtf(Long userId, String ticker) {
        // 사용자 조회
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new BusinessException(ErrorCode.USER_NOT_FOUND));

        // ETF 존재 확인
        Etf etf = etfRepository.findByStockCode(ticker)
                .orElseThrow(() -> new BusinessException(ErrorCode.ETF_NOT_FOUND));

        // 이미 관심 등록 여부 확인
        if (userFavoriteEtfRepository.existsByUserIdAndTicker(userId, ticker)) {
            throw new BusinessException(ErrorCode.ALREADY_FAVORITE);
        }

        // 관심 ETF 추가
        UserFavoriteEtf favorite = UserFavoriteEtf.create(user, etf);
        userFavoriteEtfRepository.save(favorite);

        log.info("관심 ETF 추가: userId={}, ticker={}", userId, ticker);
    }

    @Override
    @Transactional
    public void removeFavoriteEtf(Long userId, String ticker) {
        // 관심 ETF 조회
        UserFavoriteEtf favorite = userFavoriteEtfRepository.findByUserIdAndTicker(userId, ticker)
                .orElseThrow(() -> new BusinessException(ErrorCode.FAVORITE_NOT_FOUND));

        // 삭제
        userFavoriteEtfRepository.delete(favorite);

        log.info("관심 ETF 삭제: userId={}, ticker={}", userId, ticker);
    }

    @Override
    public boolean isFavoriteEtf(Long userId, String ticker) {
        return userFavoriteEtfRepository.existsByUserIdAndTicker(userId, ticker);
    }

    @Override
    @Transactional
    public List<MyDataEtfCount> getMyData(Long userId) {
        User user = userRepository.findById(userId).orElseThrow(() -> new BusinessException(ErrorCode.USER_NOT_FOUND));
        user.checkMyDataAccepted();

        List<MyDataEtfCount> myDataList = myDataApi.getMyData(userId);

        // 실시간 가격 × 수량으로 총 평가액 계산
        Set<String> tickers = myDataList.stream().map(MyDataEtfCount::ticker).collect(Collectors.toSet());
        Map<String, EtfCurrentInfo> currentInfoMap = etfService.getEtfCurrentInfoMap(tickers);
        BigDecimal totalValue = myDataList.stream()
                .map(c -> {
                    EtfCurrentInfo info = currentInfoMap.get(c.ticker());
                    BigDecimal price = (info != null && info.currentPrice() != null) ? info.currentPrice() : BigDecimal.ZERO;
                    return price.multiply(c.counts());
                })
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        // 기존 마이데이터 포트폴리오 삭제 후 재생성
        portfolioRepository.findByUserIdAndIsMyDataTrue(userId)
                .ifPresent(portfolioRepository::delete);

        Portfolio portfolio = Portfolio.createMyDataPortfolio(userId, totalValue);
        portfolioRepository.save(portfolio);

        Map<String, Etf> etfMap = etfRepository.findEtfsByStockCodeInTickers(tickers.stream().toList()).stream()
                .collect(Collectors.toMap(Etf::getStockCode, e -> e));

        List<PortfolioEtf> portfolioEtfs = myDataList.stream()
                .filter(c -> etfMap.containsKey(c.ticker()))
                .map(c -> PortfolioEtf.createPortfolioEtf(portfolio, etfMap.get(c.ticker()), c.counts()))
                .toList();
        portfolioEtfRepository.saveAll(portfolioEtfs);

        return myDataList;
    }

    @Override
    public Boolean checkUserAcceptedMyData(Long userId) {
        return userRepository.findById(userId).orElseThrow(() -> new BusinessException(ErrorCode.USER_NOT_FOUND)).getIsMyDataAccepted();
    }

    @Override
    @Transactional
    public void acceptMyData(Long userId) {
        User user = userRepository.findById(userId).orElseThrow(() -> new BusinessException(ErrorCode.USER_NOT_FOUND));
        user.acceptMyData();
    }
}
