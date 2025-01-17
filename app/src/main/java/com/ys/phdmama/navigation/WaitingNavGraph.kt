package com.ys.phdmama.navigation

import androidx.compose.runtime.Composable
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import com.ys.phdmama.ui.screens.waiting.WaitingDashboardScreen

@Composable
fun WaitingNavGraph(navController: NavHostController) {
    NavHost(navController = navController, startDestination = NavRoutes.WAITING_DASHBOARD) {
        composable(NavRoutes.WAITING_DASHBOARD) {
            WaitingDashboardScreen(navController = navController)
        }
    }
}
