package com.d102.wye.presentation.theme

import androidx.compose.ui.graphics.Color

// ── 브랜드 색상 ──────────────────────────────────
val PrimaryGreen      = Color(0xFF3A6E45)   // 짙은 숲 초록       — 앱바 타이틀, 활성 탭, 버튼, 포인트 컬러
val PrimaryGreenDark  = Color(0xFF2D4B3A)   // 더 어두운 올리브 초록 — 버튼 누름 상태, 오버레이
val KakaoYellow       = Color(0xFFFEE500)   // 카카오 노랑         — 카카오 로그인 버튼

// ── 배경 ──────────────────────────────────────────────────────
val Background        = Color(0xFFFFFFFF)   // 흰색               — 앱 전체 배경
val SurfaceWhite      = Color(0xFFFFFFFF)   // 흰색               — 카드/시트 배경
val BackGroundLightGreen      = Color(0xFFECF1ED)   // 연한 초록색               — 카드/시트 배경
val BackGroundLightGreen2      = Color(0xFFF6F7F7)   // 더 연한 초록색
val BackGroundLightGreen3     = Color(0xFFFAFAF5)   // 더 더 연한 초록색
val SurfaceVariant    = Color(0xFFF1F5F9)   // 연한 파란빛 회색   — 선택된 항목 배경, 입력 필드 배경
val SurfaceCard       = Color(0xFFF8FAFC)   // 거의 흰색 연회색   — 뉴스 카드 등 연한 카드 배경

// ── 텍스트 ────────────────────────────────────────────────────
val TextPrimary       = Color(0xFF0F172A)   // 거의 검정 남색     — 메인 헤드라인, 주요 텍스트
val TextSecondary     = Color(0xFF64748B)   // 중간 회청색        — 보조 텍스트, 레이블
val TextTertiary      = Color(0xFF475569)   // 약간 짙은 회청색   — 본문, 뉴스 카테고리
val TextHint          = Color(0xFFBDBDBD)   // 연한 회색          — 플레이스홀더
val TextOnColored     = Color(0xFFFFFFFF)   // 흰색               — 컬러 배경 위 텍스트

val TextDetail       = Color(0xFF334155)

// ── 아이콘 / 탭 상태 ──────────────────────────────────────────
val IconInactive      = Color(0xFF94A3B8)   // 연한 회청색        — 비활성 탭 아이콘
val NavInactiveLabel  = Color(0xFFBBBBBB)   // 연한 회색          — 하단 내비 비활성 레이블 (홈, 탐색 등)

val IconBackGroundOrange = Color(0xFFFFF7ED)
val IconBackGroundBlue = Color(0xFFEFF6FF)
val IconBackGroundGreen  = Color(0xFFF0FDF4)  // BALANCED_PORTFOLIO - 연한 민트
val IconBackGroundRed    = Color(0xFFFFF1F2)  // AGGRESSIVE_PLAY - 연한 핑크레드
val IconBackGroundPurple = Color(0xFFF5F3FF)  // LONG_TERM_INVESTING - 연한 라벤더
val IconBackGroundGray   = Color(0xFFF8FAFC)  // UNKNOWN - 연한 회색

// ── 테두리 / 구분선 ───────────────────────────────────────────
val Border            = Color(0xFFCBD5E1)   // 연한 회청색        — 입력 필드 테두리, 비활성 경계선
val Divider           = Color(0xFFE2E8F0)   // 아주 연한 회색     — 리스트 구분선, 토글 구분

// ── ETF 등락 색상 ──────────────────────────────────────────────
val EtfRise           = Color(0xFFEF4444)   // 선명한 빨강        — 상승
val EtfFall           = Color(0xFF2D5BE0)   // 선명한 파랑        — 하락
val EtfNeutral        = Color(0xFFBDBDBD)   // 연한 회색          — 보합

// ── 투자 성향 뱃지 (위험도 낮음 → 높음) ──────────────────────
val BadgeConservative       = Color(0xFFE3F2FD)   // 파랑     — 안정형
val BadgeConservativeGrowth = Color(0xFFE3F2FD)   // 파랑    — 안정추구형
val BadgeNeutral            = Color(0xFFFFF4E0)   // 노랑  — 위험중립형
val BadgeActive             = Color(0xFFFFE9E2)   // 빨강  — 적극투자형
val BadgeAggressive         = Color(0xFFFFE9E2)   // 빨강  — 공격투자형

// ── 뱃지 글꼴 (위험도 낮음 → 높음) ──────────────────────
val BadgeConservativeFont      = Color(0xFF0D47A1)   // 파랑     — 안정형
val BadgeConservativeGrowthFont = Color(0xFF01579B)   // 파랑    — 안정추구형
val BadgeNeutralFont            = Color(0xFF8A5B00)   // 노랑  — 위험중립형
val BadgeActiveFont             = Color(0xFF8B3A2A)   // 빨강  — 적극투자형
val BadgeAggressiveFont         = Color(0xFF7A1F2A)   // 빨강  — 공격투자형

// ── 하단 내비게이션 ────────────────────────────────────────────
val NavActive         = PrimaryGreen
val NavInactive       = IconInactive

// ── 구분선 (하위 호환) ────────────────────────────────────────
val SurfaceDivider    = Divider

// ── 수익률 차트 라인 ──────────────────────────────
val ChartColorNav    = Color(0xFF4ADE80)   // 연초록  — NAV
val ChartColorPrice  = Color(0xFF60A5FA)   // 하늘파랑 — 종가
val ChartColorKospi  = Color(0xFFF472B6)   // 로즈핑크 — KOSPI
val ChartColorNasdaq = Color(0xFF2DD4BF)   // 민트청록 — 나스닥

// ── 기타 ────────────────────────────────────────
val TipBackground = Color(0xFFECFDF5)
val TipBorder = Color(0xFFD1FAE5)

val MyDataYellow = Color(0xFFFFB300)

// ── 섹터 그래프 색상 ────────────────────────────────────────
val SectorColor1 = Color(0xFF3A6E45)  // PrimaryGreen (진한)
val SectorColor2 = Color(0xFF5A8F6A)  // 중간 초록
val SectorColor3 = Color(0xFF8AB89A)  // 연한 초록
val SectorColor4 = Color(0xFFBDD9C5)  // 아주 연한 민트
val SectorColor5 = Color(0xFFD1E8D9)  // 거의 흰색에 가까운 초록
