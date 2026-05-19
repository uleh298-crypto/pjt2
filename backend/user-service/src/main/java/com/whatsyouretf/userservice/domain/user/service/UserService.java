package com.whatsyouretf.userservice.domain.user.service;

import com.whatsyouretf.userservice.domain.user.dto.*;

import com.whatsyouretf.userservice.domain.user.service.impl.MyDataEtfCount;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

public interface UserService {

    /**
     * 사용자 조회 (ID)
     */
    UserResponse getUserById(Long userId);

    // ==================== 프로필 이미지 ====================

    /**
     * 프로필 이미지 업로드
     */
    String uploadProfileImage(Long userId, MultipartFile file);

    /**
     * 프로필 이미지 삭제
     */
    void deleteProfileImage(Long userId);

    /**
     * 내 정보 조회
     */
    UserResponse getMyInfo(Long userId);

    /**
     * 프로필 수정
     */
    UserResponse updateProfile(Long userId, UserUpdateRequest request);

    /**
     * 닉네임 중복 체크
     */
    boolean checkNicknameDuplicate(String nickname);

    /**
     * 회원 탈퇴 (hard delete)
     */
    void deleteUser(Long userId);

    // ==================== 관심 ETF ====================

    /**
     * 관심 ETF 목록 조회
     */
    FavoriteEtfListResponse getFavoriteEtfs(Long userId, FavoriteSortType sortType);

    /**
     * 관심 ETF 추가
     */
    void addFavoriteEtf(Long userId, String ticker);

    /**
     * 관심 ETF 삭제
     */
    void removeFavoriteEtf(Long userId, String ticker);

    /**
     * 관심 ETF 여부 확인
     */
    boolean isFavoriteEtf(Long userId, String ticker);

    List<MyDataEtfCount> getMyData(Long userId);

    Boolean checkUserAcceptedMyData(Long userId);

    void acceptMyData(Long userId);
}
