package com.d102.wye.presentation.navigation

import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.compose.animation.EnterTransition
import androidx.compose.animation.ExitTransition
import androidx.navigation.navArgument
import com.d102.wye.presentation.auth.join.JoinScreen
import com.d102.wye.presentation.auth.login.LoginScreen
import com.d102.wye.presentation.auth.passwordreset.PasswordResetScreen
import com.d102.wye.presentation.explore.detail.EtfDetailScreen
import com.d102.wye.presentation.explore.list.ExploreScreen
import com.d102.wye.presentation.explore.list.SelectedEtf
import com.d102.wye.presentation.explore.stock.StockDetailScreen
import com.d102.wye.presentation.explore.stock.StockEtfListScreen
import com.d102.wye.presentation.home.HomeScreen
import com.d102.wye.presentation.home.alerts.AlertsScreen
import com.d102.wye.presentation.home.news.NewsDetailScreen
import com.d102.wye.presentation.home.news.NewsListScreen
import com.d102.wye.presentation.mypage.MyPageScreen
import com.d102.wye.presentation.mypage.alerts.AlertSettingsScreen
import com.d102.wye.presentation.mypage.holding.HoldingEtfListScreen
import com.d102.wye.presentation.mypage.liked.LikedEtfListScreen
import com.d102.wye.presentation.mypage.support.FaqScreen
import com.d102.wye.presentation.mypage.support.TermsScreen
import com.d102.wye.presentation.simulation.entry.SimulationEntryScreen
import com.d102.wye.presentation.simulation.progress.SimulationScreen
import com.d102.wye.presentation.simulation.progress.SimulationViewModel
import com.d102.wye.presentation.strategy.compare.StrategyCompareScreen
import com.d102.wye.presentation.strategy.detail.StrategyDetailScreen
import com.d102.wye.presentation.strategy.list.StrategyScreen

/**
 * 앱 전체 NavGraph
 */
@Composable
fun AppNavGraph(
    navController: NavHostController,
    contentPadding: PaddingValues,
    startDestination: String = Route.Login.route
) {
    NavHost(
        navController = navController,
        startDestination = startDestination,
        modifier = Modifier
            .fillMaxSize()
            .padding(bottom = contentPadding.calculateBottomPadding()),
        enterTransition = { EnterTransition.None },
        exitTransition = { ExitTransition.None },
        popEnterTransition = { EnterTransition.None },
        popExitTransition = { ExitTransition.None },
    ) {

        // ─────────────────────────────────────────
        // Auth
        // ─────────────────────────────────────────

        composable(Route.Login.route) {
            LoginScreen(
                onLoginSuccess = {
                    navController.navigate(Route.Home.route) {
                        popUpTo(Route.Login.route) { inclusive = true }
                    }
                },
                onJoinClick = { navController.navigate(Route.Join.route) },
                onForgotPasswordClick = { navController.navigate(Route.PasswordReset.route) }
            )
        }

        composable(Route.Join.route) {
            JoinScreen(
                onBackClick = { navController.popBackStack() },
                onStartClick = {}
            )
        }

        composable(Route.PasswordReset.route) {
            PasswordResetScreen(
                onBackClick = { navController.popBackStack() },
                onCloseClick = { navController.popBackStack() },
                onLoginClick = {
                    navController.navigate(Route.Login.route) {
                        popUpTo(0) { inclusive = true }
                        launchSingleTop = true
                    }
                }
            )
        }

        // ─────────────────────────────────────────
        // Bottom Nav 화면
        // ─────────────────────────────────────────

        composable(Route.Home.route) {
            HomeScreen(
                onNewsClick = { newsId -> navController.navigate(Route.NewsDetail(newsId).route) },
                onEtfClick = { ticker -> navController.navigate(Route.EtfDetail(ticker).route) },
                onBookmarkClick = { navController.navigate(Route.LikedEtfList.route) },
                onAlertClick = { navController.navigate(Route.AlertList.route) },
                onNewsMoreClick = { navController.navigate(Route.NewsList.route) }
            )
        }

        composable(Route.Explore.route) {
            ExploreScreen(
                onEtfClick = { ticker, riskLevel ->
                    navController.navigate(
                        Route.EtfDetail(ticker, riskLevel).route
                    )
                }
            )
        }

        composable(Route.SimulationEntry.route) { entry ->
            SimulationEntryScreen(
                onMakePortfolioClick = { tickers ->
                    navController.navigate(Route.Simulation.route)
                    // navigate 후 Simulation backStackEntry에 세팅
                    navController.getBackStackEntry(Route.Simulation.route)
                        .savedStateHandle["selected_tickers"] = tickers.toTypedArray()
                }
            )
        }

        composable(Route.Strategy.route) {
            StrategyScreen(
                onStrategyClick = { id -> navController.navigate(Route.StrategyDetail(id).route) },
                onCompareClick = { navController.navigate(Route.StrategyCompare.route) },
                onCreateFirstStrategyClick = { navController.navigate(Route.Simulation.route) }
            )
        }

        composable(Route.MyPage.route) {
            MyPageScreen(
                onLikedEtfClick = { ticker -> navController.navigate(Route.EtfDetail(ticker).route) },
                onHoldingEtfMoreClick = { navController.navigate(Route.HoldingEtfList.route) },
                onLikedEtfListClick = { navController.navigate(Route.LikedEtfList.route) },
                onPasswordChangeClick = { navController.navigate(Route.PasswordReset.route) },
                onAlertSettingClick = { navController.navigate(Route.AlertSettings.route) },
                onFaqClick = { navController.navigate(Route.Faq.route) },
                onTermsClick = { navController.navigate(Route.Terms.route) },
                onLogoutClick = {
                    navController.navigate(Route.Login.route) {
                        popUpTo(0) { inclusive = true }
                    }
                }
            )
        }

        composable(Route.LikedEtfList.route) {
            LikedEtfListScreen(
                onBackClick = { navController.popBackStack() },
                onEtfClick = { ticker -> navController.navigate(Route.EtfDetail(ticker).route) }
            )
        }

        composable(Route.HoldingEtfList.route) {
            HoldingEtfListScreen(
                onBackClick = { navController.popBackStack() },
                onEtfClick = { ticker -> navController.navigate(Route.EtfDetail(ticker).route) }
            )
        }

        composable(Route.AlertSettings.route) {
            AlertSettingsScreen(
                onBackClick = { navController.popBackStack() }
            )
        }

        composable(Route.Faq.route) {
            FaqScreen(
                onBackClick = { navController.popBackStack() }
            )
        }

        composable(Route.Terms.route) {
            TermsScreen(
                onBackClick = { navController.popBackStack() }
            )
        }

        // ─────────────────────────────────────────
        // ETF 상세 (ticker 파라미터)
        // ─────────────────────────────────────────

        composable(
            route = Route.EtfDetail.ROUTE_PATTERN,
            arguments = listOf(
                navArgument(Route.EtfDetail.ARG_TICKER) { type = NavType.StringType },
                navArgument(Route.EtfDetail.ARG_RISK_LEVEL) {
                    type = NavType.IntType; defaultValue = 0
                },
            )
        ) {
            EtfDetailScreen(
                onBack = { navController.popBackStack() },
                onStockClick = { ticker -> navController.navigate(Route.StockDetail(ticker).route) },
            )
        }

        // ─────────────────────────────────────────
        // 종목 상세 (ticker 파라미터)
        // ─────────────────────────────────────────

        composable(
            route = Route.StockDetail.ROUTE_PATTERN,
            arguments = listOf(
                navArgument(Route.StockDetail.ARG_TICKER) { type = NavType.StringType }
            )
        ) {
            StockDetailScreen(
                onBack = { navController.popBackStack() },
                onEtfListClick = { ticker -> navController.navigate(Route.StockEtfList(ticker).route) },
                onEtfClick = { ticker -> navController.navigate(Route.EtfDetail(ticker).route) },
                onRelatedStockClick = { ticker -> navController.navigate(Route.StockDetail(ticker).route) },
            )
        }

        // 종목에 포함된 ETF 전체 목록
        composable(
            route = Route.StockEtfList.ROUTE_PATTERN,
            arguments = listOf(
                navArgument(Route.StockEtfList.ARG_TICKER) { type = NavType.StringType }
            )
        ) {
            StockEtfListScreen(
                onBack = { navController.popBackStack() },
                onEtfClick = { ticker -> navController.navigate(Route.EtfDetail(ticker).route) },
            )
        }

        // ─────────────────────────────────────────
        // 뉴스 상세 (newsId 파라미터)
        // ─────────────────────────────────────────

        // ─────────────────────────────────────────
        // 뉴스 목록
        // ─────────────────────────────────────────

        composable(Route.NewsList.route) {
            NewsListScreen(
                onBack = { navController.popBackStack() },
                onNewsClick = { newsId -> navController.navigate(Route.NewsDetail(newsId).route) },
            )
        }

        // ─────────────────────────────────────────
        // 뉴스 상세 (newsId 파라미터)
        // ─────────────────────────────────────────

        composable(
            route = Route.NewsDetail.ROUTE_PATTERN,
            arguments = listOf(
                navArgument(Route.NewsDetail.ARG_NEWS_ID) { type = NavType.LongType }
            )
        ) { backStackEntry ->
            val newsId =
                backStackEntry.arguments?.getLong(Route.NewsDetail.ARG_NEWS_ID) ?: return@composable
            NewsDetailScreen(
                newsId = newsId,
                onBack = { navController.popBackStack() },
                onEtfClick = { ticker -> navController.navigate(Route.EtfDetail(ticker).route) },
            )
        }

        // ─────────────────────────────────────────
        // 시뮬레이션 진행 화면
        // ─────────────────────────────────────────
        composable(Route.Simulation.route) { backStackEntry ->
            val viewModel: SimulationViewModel = hiltViewModel()

            val selectedTickers by backStackEntry.savedStateHandle
                .getStateFlow<Array<String>?>("selected_tickers", null)
                .collectAsStateWithLifecycle()

            LaunchedEffect(selectedTickers) {
                selectedTickers?.let { tickers ->
                    viewModel.addPortfolioItems(tickers.toList())
                    backStackEntry.savedStateHandle.remove<Array<String>>("selected_tickers")
                }
            }

            SimulationScreen(
                onBackClick = { navController.popBackStack() },
                onAddEtfClick = { tickers, names ->
                    navController.currentBackStackEntry?.savedStateHandle?.apply {
                        set("current_tickers", tickers)
                        set("current_names", names)
                    }
                    navController.navigate(Route.SimulationAddStock.route)
                },
                onSaveClick = {
                    navController.navigate(Route.Strategy.route) {
                        // 위로 쌓인 스택을 모두 정리
                        popUpTo(Route.Home.route) { saveState = false }
                        launchSingleTop = true
                        restoreState = true
                    }
                },
                viewModel = viewModel
            )
        }

        // ─────────────────────────────────────────
        // 시뮬레이션에서 '추가하기'를 눌렀을 때 띄울 탐색 화면
        // ─────────────────────────────────────────
        composable(Route.SimulationAddStock.route) { backStackEntry ->
            val savedState = navController.previousBackStackEntry?.savedStateHandle

            val tickers = savedState?.get<Array<String>>("current_tickers") ?: emptyArray()
            val names = savedState?.get<Array<String>>("current_names") ?: emptyArray()

            val currentSelections = tickers.zip(names).map { (ticker, name) ->
                SelectedEtf(ticker = ticker, name = name)
            }

            ExploreScreen(
                title = "종목 추가",
                isSelectionMode = true,
                initialSelectedTickers = currentSelections,
                onBackClick = { navController.popBackStack() },
                onEtfClick = { ticker, riskLevel ->
                    navController.navigate(Route.EtfDetail(ticker, riskLevel).route)
                },
                onSelectionComplete = { tickers ->
                    // 전체 선택 목록(기존 + 새로 추가) 반환
                    navController.previousBackStackEntry
                        ?.savedStateHandle
                        ?.set("selected_tickers", tickers.toTypedArray())
                    navController.popBackStack()
                }
            )
        }

        // ─────────────────────────────────────────
        // 알림 목록
        // ─────────────────────────────────────────

        composable(Route.AlertList.route) {
            AlertsScreen(
                onBack = { navController.popBackStack() },
                onEtfClick = { ticker -> navController.navigate(Route.EtfDetail(ticker).route) },
                onNewsClick = { newsId -> navController.navigate(Route.NewsDetail(newsId).route) },
                onPortfolioClick = { portfolioId ->
                    navController.navigate(
                        Route.StrategyDetail(
                            portfolioId
                        ).route
                    )
                },
            )
        }


        // ─────────────────────────────────────────
        // 나의전략 서브 화면 (strategyId 파라미터)
        // ─────────────────────────────────────────

        composable(
            route = Route.StrategyDetail.ROUTE_PATTERN,
            arguments = listOf(
                navArgument(Route.StrategyDetail.ARG_STRATEGY_ID) { type = NavType.LongType }
            )
        ) { backStackEntry ->
            StrategyDetailScreen(
                onBackClick = { navController.popBackStack() },
                onNewsClick = { newsId -> navController.navigate(Route.NewsDetail(newsId).route) }
            )
        }
        composable(
            route = Route.StrategyCompare.route
        ) { backStackEntry ->
            StrategyCompareScreen(
                onBackClick = { navController.popBackStack() }
            )
        }

    }
}
