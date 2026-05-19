package com.d102.wye.presentation.navigation

/**
 * 앱 내 모든 화면의 라우트 정의
 *
 * 두 가지 종류:
 * - object : 파라미터 없는 화면 (목록, 설정 등)
 * - data class : 파라미터 있는 화면 (상세 조회 등)
 *
 * 실제 경로 문자열은 route 프로퍼티로 통일해서 사용
 * NavHost/navigate() 양쪽에서 항상 .route로 접근할 것
 */
sealed class Route(val route: String) {

    // ─────────────────────────────────────────
    // Bottom Nav (BottomNavBar가 보이는 화면)
    // ─────────────────────────────────────────

    object Home : Route("home")
    object Explore : Route("explore")

    // ─────────────────────────────────────────
    // 시뮬레이션 화면
    // ─────────────────────────────────────────

    object Simulation : Route("simulation")
    object SimulationEntry : Route("simulation_entry")
    object SimulationAddStock : Route("simulation_add_stock")

    object Strategy : Route("strategy")
    object MyPage : Route("mypage")

    // ─────────────────────────────────────────
    // Auth
    // ─────────────────────────────────────────

    object Login : Route("login")
    object Join : Route("join")
    object PasswordReset : Route("password_reset")
    object LikedEtfList : Route("liked_etf_list")
    object HoldingEtfList : Route("holding_etf_list")
    object Faq : Route("faq")
    object Terms : Route("terms")

    // ─────────────────────────────────────────
    // ETF 상세 (ticker로 단건 조회)
    // ─────────────────────────────────────────

    data class EtfDetail(val ticker: String, val riskLevel: Int = 0) : Route("etf_detail/$ticker/$riskLevel") {
        companion object {
            const val ROUTE_PATTERN = "etf_detail/{ticker}/{riskLevel}"
            const val ARG_TICKER = "ticker"
            const val ARG_RISK_LEVEL = "riskLevel"
        }
    }

    // ─────────────────────────────────────────
    // 뉴스 상세 (id로 단건 조회)
    // ─────────────────────────────────────────

    data class NewsDetail(val newsId: Long) : Route("news_detail/$newsId") {
        companion object {
            const val ROUTE_PATTERN = "news_detail/{newsId}"
            const val ARG_NEWS_ID = "newsId"
        }
    }



    // ─────────────────────────────────────────
    // 나의전략 서브 화면
    // ─────────────────────────────────────────

    data class StrategyDetail(val strategyId: Long) : Route("strategy_detail/$strategyId") {
        companion object {
            const val ROUTE_PATTERN = "strategy_detail/{strategyId}"
            const val ARG_STRATEGY_ID = "strategyId"
        }
    }

    object StrategyCompare : Route("strategy_compare")

    // ─────────────────────────────────────────
    // 종목 상세 (ticker로 단건 조회)
    // ─────────────────────────────────────────

    data class StockDetail(val ticker: String) : Route("stock_detail/$ticker") {
        companion object {
            const val ROUTE_PATTERN = "stock_detail/{ticker}"
            const val ARG_TICKER = "ticker"
        }
    }

    // 종목에 포함된 ETF 전체 목록
    data class StockEtfList(val ticker: String) : Route("stock_etf_list/$ticker") {
        companion object {
            const val ROUTE_PATTERN = "stock_etf_list/{ticker}"
            const val ARG_TICKER = "ticker"
        }
    }

    // ─────────────────────────────────────────
    // 뉴스 목록
    // ─────────────────────────────────────────

    object NewsList : Route("news_list")

    // ─────────────────────────────────────────
    // 알림
    // ─────────────────────────────────────────

    object AlertList : Route("alerts")
    object AlertSettings : Route("alert_settings")
}
