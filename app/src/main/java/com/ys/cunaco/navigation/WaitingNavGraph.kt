package com.ys.cunaco.navigation

import androidx.compose.runtime.Composable
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import com.ys.cunaco.ui.screens.waiting.WaitingDashboardScreen

@Composable
fun WaitingNavGraph(navController: NavHostController,  openDrawer: () -> Unit) {
    NavHost(navController = navController, startDestination = NavRoutes.WAITING_DASHBOARD) {
        composable(NavRoutes.WAITING_DASHBOARD) {
            WaitingDashboardScreen(navController = navController, openDrawer = openDrawer)
        }
    }
}
