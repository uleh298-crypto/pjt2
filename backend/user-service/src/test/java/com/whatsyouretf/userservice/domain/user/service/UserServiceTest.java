package com.whatsyouretf.userservice.domain.user.service;

import com.whatsyouretf.userservice.common.exception.BusinessException;
import com.whatsyouretf.userservice.common.exception.ErrorCode;
import com.whatsyouretf.userservice.domain.etf.entity.Etf;
import com.whatsyouretf.userservice.domain.etf.entity.EtfPrice;
import com.whatsyouretf.userservice.domain.etf.repository.EtfPriceRepository;
import com.whatsyouretf.userservice.domain.etf.repository.EtfRepository;
import com.whatsyouretf.userservice.domain.user.dto.*;
import com.whatsyouretf.userservice.domain.user.entity.User;
import com.whatsyouretf.userservice.domain.user.entity.UserFavoriteEtf;
import com.whatsyouretf.userservice.domain.user.repository.UserFavoriteEtfRepository;
import com.whatsyouretf.userservice.domain.user.repository.UserHoldingEtfRepository;
import com.whatsyouretf.userservice.domain.user.repository.UserRepository;
import com.whatsyouretf.userservice.domain.user.service.impl.UserServiceImpl;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.BDDMockito.*;

/**
 * UserService 단위 테스트
 * <p>
 * 테스트 범위:
 * - 사용자 정보 조회/수정
 * - 관심 ETF (조회, 추가, 삭제)
 */
@ExtendWith(MockitoExtension.class)
@DisplayName("UserService 단위 테스트")
class UserServiceTest {

    @InjectMocks
    private UserServiceImpl userService;

    @Mock
    private UserRepository userRepository;

    @Mock
    private EtfRepository etfRepository;

    @Mock
    private EtfPriceRepository etfPriceRepository;

    @Mock
    private UserFavoriteEtfRepository userFavoriteEtfRepository;

    @Mock
    private UserHoldingEtfRepository userHoldingEtfRepository;

    // 테스트 데이터
    private User testUser;
    private Etf testEtf;
    private EtfPrice testEtfPrice;

    @BeforeEach
    void setUp() {
        // 테스트 사용자 생성
        testUser = User.builder()
                .id(1L)
                .email("test@example.com")
                .nickname("테스트유저")
                .profileImage(null)
                .isActive(true)
                .build();

        // 테스트 ETF 생성
        testEtf = Etf.builder()
                .id(100L)
                .stockCode("069500")
                .name("KODEX 200")
                .sector("국내주식형")
                .assetManager("삼성자산운용")
                .isActive(true)
                .build();

        // 테스트 ETF 시세 생성
        testEtfPrice = EtfPrice.builder()
                .id(1000L)
                .etf(testEtf)
                .tradeDate(LocalDate.now())
                .close(BigDecimal.valueOf(35000))
                .changeRate(BigDecimal.valueOf(1.25))
                .build();
    }

    // ========== 사용자 정보 테스트 ==========

    @Nested
    @DisplayName("사용자 정보 조회/수정 테스트")
    class UserInfoTest {

        @Test
        @DisplayName("닉네임 중복 체크 - 중복인 경우 true를 반환한다")
        void checkNicknameDuplicate_Duplicate_ReturnsTrue() {
            // given
            given(userRepository.existsByNickname("중복닉네임")).willReturn(true);

            // when
            boolean result = userService.checkNicknameDuplicate("중복닉네임");

            // then
            assertThat(result).isTrue();
        }

        @Test
        @DisplayName("닉네임 중복 체크 - 사용 가능한 경우 false를 반환한다")
        void checkNicknameDuplicate_Available_ReturnsFalse() {
            // given
            given(userRepository.existsByNickname("새닉네임")).willReturn(false);

            // when
            boolean result = userService.checkNicknameDuplicate("새닉네임");

            // then
            assertThat(result).isFalse();
        }

        @Test
        @DisplayName("회원 탈퇴 - 사용자가 비활성화된다")
        void deactivateUser_Success() {
            // given
            given(userRepository.findById(1L)).willReturn(Optional.of(testUser));

            // when
            userService.deactivateUser(1L);

            // then
            assertThat(testUser.getIsActive()).isFalse();
        }

        @Test
        @DisplayName("회원 탈퇴 - 존재하지 않는 사용자인 경우 예외가 발생한다")
        void deactivateUser_UserNotFound_ThrowsException() {
            // given
            given(userRepository.findById(999L)).willReturn(Optional.empty());

            // when & then
            assertThatThrownBy(() -> userService.deactivateUser(999L))
                    .isInstanceOf(BusinessException.class)
                    .hasFieldOrPropertyWithValue("code", ErrorCode.USER_NOT_FOUND);
        }
    }

    // ========== 관심 ETF 테스트 ==========

    @Nested
    @DisplayName("관심 ETF 테스트")
    class FavoriteEtfTest {

        @Test
        @DisplayName("관심 ETF 목록 조회 - 정상 조회")
        void getFavoriteEtfs_Success() {
            // given
            UserFavoriteEtf favorite = UserFavoriteEtf.builder()
                    .id(1L)
                    .user(testUser)
                    .etf(testEtf)
                    .createdAt(LocalDateTime.now())
                    .build();

            given(userRepository.existsById(1L)).willReturn(true);
            given(userFavoriteEtfRepository.findAllByUserIdWithEtf(1L)).willReturn(List.of(favorite));
            given(etfPriceRepository.findLatestByEtfIds(List.of(100L))).willReturn(List.of(testEtfPrice));

            // when
            FavoriteEtfListResponse response = userService.getFavoriteEtfs(1L, FavoriteSortType.RECENT);

            // then
            assertThat(response).isNotNull();
            assertThat(response.getTotalCount()).isEqualTo(1);
            assertThat(response.getFavorites()).hasSize(1);
            assertThat(response.getFavorites().get(0).getStockCode()).isEqualTo("069500");
            assertThat(response.getFavorites().get(0).getCurrentPrice()).isEqualTo(BigDecimal.valueOf(35000));
        }

        @Test
        @DisplayName("관심 ETF 목록 조회 - 사용자가 존재하지 않으면 예외가 발생한다")
        void getFavoriteEtfs_UserNotFound_ThrowsException() {
            // given
            given(userRepository.existsById(999L)).willReturn(false);

            // when & then
            assertThatThrownBy(() -> userService.getFavoriteEtfs(999L, FavoriteSortType.RECENT))
                    .isInstanceOf(BusinessException.class)
                    .hasFieldOrPropertyWithValue("code", ErrorCode.USER_NOT_FOUND);
        }

        @Test
        @DisplayName("관심 ETF 추가 - 정상 추가")
        void addFavoriteEtf_Success() {
            // given
            given(userRepository.findById(1L)).willReturn(Optional.of(testUser));
            given(etfRepository.findById(100L)).willReturn(Optional.of(testEtf));
            given(userFavoriteEtfRepository.existsByUserIdAndEtfId(1L, 100L)).willReturn(false);

            // when
            userService.addFavoriteEtf(1L, 100L);

            // then
            then(userFavoriteEtfRepository).should().save(any(UserFavoriteEtf.class));
        }

        @Test
        @DisplayName("관심 ETF 추가 - 사용자가 존재하지 않으면 예외가 발생한다")
        void addFavoriteEtf_UserNotFound_ThrowsException() {
            // given
            given(userRepository.findById(999L)).willReturn(Optional.empty());

            // when & then
            assertThatThrownBy(() -> userService.addFavoriteEtf(999L, 100L))
                    .isInstanceOf(BusinessException.class)
                    .hasFieldOrPropertyWithValue("code", ErrorCode.USER_NOT_FOUND);
        }

        @Test
        @DisplayName("관심 ETF 추가 - ETF가 존재하지 않으면 예외가 발생한다")
        void addFavoriteEtf_EtfNotFound_ThrowsException() {
            // given
            given(userRepository.findById(1L)).willReturn(Optional.of(testUser));
            given(etfRepository.findById(999L)).willReturn(Optional.empty());

            // when & then
            assertThatThrownBy(() -> userService.addFavoriteEtf(1L, 999L))
                    .isInstanceOf(BusinessException.class)
                    .hasFieldOrPropertyWithValue("code", ErrorCode.ETF_NOT_FOUND);
        }

        @Test
        @DisplayName("관심 ETF 추가 - 이미 추가된 경우 예외가 발생한다")
        void addFavoriteEtf_AlreadyFavorite_ThrowsException() {
            // given
            given(userRepository.findById(1L)).willReturn(Optional.of(testUser));
            given(etfRepository.findById(100L)).willReturn(Optional.of(testEtf));
            given(userFavoriteEtfRepository.existsByUserIdAndEtfId(1L, 100L)).willReturn(true);

            // when & then
            assertThatThrownBy(() -> userService.addFavoriteEtf(1L, 100L))
                    .isInstanceOf(BusinessException.class)
                    .hasFieldOrPropertyWithValue("code", ErrorCode.ALREADY_FAVORITE);
        }

        @Test
        @DisplayName("관심 ETF 삭제 - 정상 삭제")
        void removeFavoriteEtf_Success() {
            // given
            UserFavoriteEtf favorite = UserFavoriteEtf.builder()
                    .id(1L)
                    .user(testUser)
                    .etf(testEtf)
                    .build();

            given(userFavoriteEtfRepository.findByUserIdAndEtfId(1L, 100L))
                    .willReturn(Optional.of(favorite));

            // when
            userService.removeFavoriteEtf(1L, 100L);

            // then
            then(userFavoriteEtfRepository).should().delete(favorite);
        }

        @Test
        @DisplayName("관심 ETF 삭제 - 존재하지 않는 관심 ETF인 경우 예외가 발생한다")
        void removeFavoriteEtf_NotFound_ThrowsException() {
            // given
            given(userFavoriteEtfRepository.findByUserIdAndEtfId(1L, 999L))
                    .willReturn(Optional.empty());

            // when & then
            assertThatThrownBy(() -> userService.removeFavoriteEtf(1L, 999L))
                    .isInstanceOf(BusinessException.class)
                    .hasFieldOrPropertyWithValue("code", ErrorCode.FAVORITE_NOT_FOUND);
        }

        @Test
        @DisplayName("관심 ETF 여부 확인 - 등록된 경우 true를 반환한다")
        void isFavoriteEtf_Exists_ReturnsTrue() {
            // given
            given(userFavoriteEtfRepository.existsByUserIdAndEtfId(1L, 100L)).willReturn(true);

            // when
            boolean result = userService.isFavoriteEtf(1L, 100L);

            // then
            assertThat(result).isTrue();
        }

        @Test
        @DisplayName("관심 ETF 여부 확인 - 등록되지 않은 경우 false를 반환한다")
        void isFavoriteEtf_NotExists_ReturnsFalse() {
            // given
            given(userFavoriteEtfRepository.existsByUserIdAndEtfId(1L, 100L)).willReturn(false);

            // when
            boolean result = userService.isFavoriteEtf(1L, 100L);

            // then
            assertThat(result).isFalse();
        }
    }
}
