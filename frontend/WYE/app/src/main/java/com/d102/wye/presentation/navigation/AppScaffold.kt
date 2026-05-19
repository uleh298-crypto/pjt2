package com.d102.wye.presentation.navigation

import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController

@Composable
fun AppScaffold(
    startDestination: String = Route.Login.route
) {
    val navController = rememberNavController()
    val navBackStackEntry by navController.currentBackStackEntryAsState()
    val currentRoute = navBackStackEntry?.destination?.route

    val showBottomBar = currentRoute in BottomNavTab.routes
    val selectedTab = BottomNavTab.fromRoute(currentRoute)

    Scaffold(
        modifier = Modifier.fillMaxSize(),
        bottomBar = {
            if (showBottomBar) {
                BottomNavBar(
                    selectedTab = selectedTab,
                    onTabSelected = { tab ->
                        navController.navigate(tab.route) {
                            popUpTo(navController.graph.findStartDestination().id) {
                                saveState = true
                            }
                            launchSingleTop = true

                            restoreState = tab.route != Route.SimulationEntry.route
                        }
                    }
                )
            }
        }
    ) { innerPadding ->
        AppNavGraph(
            navController = navController,
            contentPadding = innerPadding,
            startDestination = startDestination
        )
    }
}