package com.ys.phdmama.navigation

import androidx.compose.runtime.Composable
import androidx.navigation.NavGraphBuilder
import androidx.navigation.NavHostController
import androidx.navigation.compose.composable
import androidx.navigation.navigation
import com.ys.phdmama.ui.screens.waiting.WaitingDashboardScreen
import com.ys.phdmama.ui.screens.wizard.prebirth.*

@Composable
fun NavGraphBuilder.waitingNavGraph(navController: NavHostController) {
    navigation(startDestination = NavRoutes.WAITING_DASHBOARD, route = "waiting") {
        composable(NavRoutes.WAITING_DASHBOARD) {
            WaitingDashboardScreen(navController = navController)
        }
        composable(NavRoutes.ROUGHBIRTH) {
            RoughDateOfBirthScreen(navController = navController)
        }
        composable(NavRoutes.BIRTH_WAITING) {
            BirthWaitingScreen(navController = navController)
        }
        // Añadir más pantallas para el rol `waiting`
    }
}
