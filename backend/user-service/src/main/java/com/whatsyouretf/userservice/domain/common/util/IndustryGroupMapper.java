package com.whatsyouretf.userservice.domain.common.util;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

import java.util.*;

/**
 * 산업 그룹 코드 매핑 유틸리티
 * <p>
 * 21개 group_code를 14개 모바일 UI 테마로 통합합니다.
 * ETF 목록, 뉴스 카테고리 등에서 공통으로 사용합니다.
 *
 * @see <a href="docs/planning/산업분류_체계_설계.md">산업분류 체계 설계 문서</a>
 */
public final class IndustryGroupMapper {

    private IndustryGroupMapper() {
        // Utility class
    }

    /**
     * 14개 모바일 UI 테마 그룹
     */
    @Getter
    @RequiredArgsConstructor
    public enum MobileTheme {
        SEMI("SEMI", "반도체"),
        IT("IT", "IT/전자"),
        BIO("BIO", "바이오/의약"),
        AUTO("AUTO", "자동차"),
        CHEM("CHEM", "화학/소재"),
        ENERGY("ENERGY", "에너지"),
        FINANCE("FINANCE", "금융"),
        CONSTRUCT("CONSTRUCT", "건설/부동산"),
        CONSUMER("CONSUMER", "소비재"),
        TELECOM("TELECOM", "통신/미디어"),
        TRANSPORT("TRANSPORT", "운송/물류"),
        INDUSTRY("INDUSTRY", "산업재"),
        HOLDING("HOLDING", "지주회사"),
        ETC("ETC", "기타");

        private final String code;
        private final String displayName;

        /**
         * 뉴스 카테고리 코드 반환 (NEWS_ 접두사)
         */
        public String toNewsCategoryCode() {
            return "NEWS_" + this.code;
        }
    }

    /**
     * 21개 group_code → 14개 MobileTheme 매핑
     */
    private static final Map<String, MobileTheme> GROUP_TO_THEME;

    static {
        Map<String, MobileTheme> map = new HashMap<>();

        // 반도체
        map.put("IT_SEMI", MobileTheme.SEMI);

        // IT/전자 (IT_ELEC + IT_SW 통합)
        map.put("IT_ELEC", MobileTheme.IT);
        map.put("IT_SW", MobileTheme.IT);

        // 바이오/의약
        map.put("BIO", MobileTheme.BIO);

        // 자동차
        map.put("AUTO", MobileTheme.AUTO);

        // 화학/소재 (CHEM + STEEL 통합)
        map.put("CHEM", MobileTheme.CHEM);
        map.put("STEEL", MobileTheme.CHEM);

        // 에너지
        map.put("ENERGY", MobileTheme.ENERGY);

        // 금융 (FINANCE + INSURANCE 통합)
        map.put("FINANCE", MobileTheme.FINANCE);
        map.put("INSURANCE", MobileTheme.FINANCE);

        // 건설/부동산
        map.put("CONSTRUCT", MobileTheme.CONSTRUCT);

        // 소비재 (CONSUMER + RETAIL + FOOD 통합)
        map.put("CONSUMER", MobileTheme.CONSUMER);
        map.put("RETAIL", MobileTheme.CONSUMER);
        map.put("FOOD", MobileTheme.CONSUMER);

        // 통신/미디어
        map.put("TELECOM", MobileTheme.TELECOM);

        // 운송/물류 (TRANSPORT + SHIPBUILD 통합)
        map.put("TRANSPORT", MobileTheme.TRANSPORT);
        map.put("SHIPBUILD", MobileTheme.TRANSPORT);

        // 산업재 (MACHINERY + DEFENSE 통합)
        map.put("MACHINERY", MobileTheme.INDUSTRY);
        map.put("DEFENSE", MobileTheme.INDUSTRY);

        // 지주회사
        map.put("HOLDING", MobileTheme.HOLDING);

        // 기타 (AGRI + MINING + EVENT + ETC + OTHER 통합)
        map.put("AGRI", MobileTheme.ETC);
        map.put("MINING", MobileTheme.ETC);
        map.put("EVENT", MobileTheme.ETC);
        map.put("ETC", MobileTheme.ETC);
        map.put("OTHER", MobileTheme.ETC);

        GROUP_TO_THEME = Collections.unmodifiableMap(map);
    }

    /**
     * 14개 MobileTheme → 해당하는 group_code 목록 (역매핑)
     */
    private static final Map<MobileTheme, List<String>> THEME_TO_GROUPS;

    static {
        Map<MobileTheme, List<String>> map = new EnumMap<>(MobileTheme.class);
        map.put(MobileTheme.SEMI, List.of("IT_SEMI"));
        map.put(MobileTheme.IT, List.of("IT_ELEC", "IT_SW"));
        map.put(MobileTheme.BIO, List.of("BIO"));
        map.put(MobileTheme.AUTO, List.of("AUTO"));
        map.put(MobileTheme.CHEM, List.of("CHEM", "STEEL"));
        map.put(MobileTheme.ENERGY, List.of("ENERGY"));
        map.put(MobileTheme.FINANCE, List.of("FINANCE", "INSURANCE"));
        map.put(MobileTheme.CONSTRUCT, List.of("CONSTRUCT"));
        map.put(MobileTheme.CONSUMER, List.of("CONSUMER", "RETAIL", "FOOD"));
        map.put(MobileTheme.TELECOM, List.of("TELECOM"));
        map.put(MobileTheme.TRANSPORT, List.of("TRANSPORT", "SHIPBUILD"));
        map.put(MobileTheme.INDUSTRY, List.of("MACHINERY", "DEFENSE"));
        map.put(MobileTheme.HOLDING, List.of("HOLDING"));
        map.put(MobileTheme.ETC, List.of("AGRI", "MINING", "EVENT", "ETC", "OTHER"));

        THEME_TO_GROUPS = Collections.unmodifiableMap(map);
    }

    // ==================== 공개 API ====================

    /**
     * group_code → MobileTheme 변환
     *
     * @param groupCode 21개 group_code 중 하나 (예: "IT_ELEC")
     * @return 해당하는 MobileTheme, 매핑 없으면 ETC
     */
    public static MobileTheme toMobileTheme(String groupCode) {
        if (groupCode == null || groupCode.isBlank()) {
            return MobileTheme.ETC;
        }
        return GROUP_TO_THEME.getOrDefault(groupCode.toUpperCase(), MobileTheme.ETC);
    }

    /**
     * group_code → 뉴스 카테고리 코드 변환
     *
     * @param groupCode 21개 group_code 중 하나 (예: "IT_ELEC")
     * @return 뉴스 카테고리 코드 (예: "NEWS_IT")
     */
    public static String toNewsCategoryCode(String groupCode) {
        return toMobileTheme(groupCode).toNewsCategoryCode();
    }

    /**
     * group_code → UI 표시명 변환
     *
     * @param groupCode 21개 group_code 중 하나 (예: "IT_ELEC")
     * @return UI 표시명 (예: "IT/전자")
     */
    public static String toDisplayName(String groupCode) {
        return toMobileTheme(groupCode).getDisplayName();
    }

    /**
     * MobileTheme → 해당하는 group_code 목록 반환
     * <p>
     * ETF 검색 시 theme으로 필터링할 때 사용합니다.
     * 예: theme="IT" → ["IT_ELEC", "IT_SW"] 반환
     *
     * @param theme 14개 MobileTheme 중 하나
     * @return 해당하는 group_code 목록
     */
    public static List<String> getGroupCodesForTheme(MobileTheme theme) {
        return THEME_TO_GROUPS.getOrDefault(theme, List.of());
    }

    /**
     * theme 코드 → 해당하는 group_code 목록 반환
     *
     * @param themeCode 테마 코드 (예: "IT", "SEMI", "BIO")
     * @return 해당하는 group_code 목록, 없으면 빈 리스트
     */
    public static List<String> getGroupCodesForTheme(String themeCode) {
        if (themeCode == null || themeCode.isBlank()) {
            return List.of();
        }

        try {
            MobileTheme theme = MobileTheme.valueOf(themeCode.toUpperCase());
            return getGroupCodesForTheme(theme);
        } catch (IllegalArgumentException e) {
            return List.of();
        }
    }

    /**
     * 모든 MobileTheme 목록 반환
     */
    public static List<MobileTheme> getAllThemes() {
        return List.of(MobileTheme.values());
    }

    /**
     * 유효한 theme 코드인지 확인
     */
    public static boolean isValidThemeCode(String themeCode) {
        if (themeCode == null || themeCode.isBlank()) {
            return false;
        }
        try {
            MobileTheme.valueOf(themeCode.toUpperCase());
            return true;
        } catch (IllegalArgumentException e) {
            return false;
        }
    }

    /**
     * 유효한 group_code인지 확인
     */
    public static boolean isValidGroupCode(String groupCode) {
        if (groupCode == null || groupCode.isBlank()) {
            return false;
        }
        return GROUP_TO_THEME.containsKey(groupCode.toUpperCase());
    }
}
