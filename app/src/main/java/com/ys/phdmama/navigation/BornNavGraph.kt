package com.ys.phdmama.navigation

import androidx.compose.runtime.Composable
import androidx.navigation.NavGraphBuilder
import androidx.navigation.NavHostController
import androidx.navigation.compose.composable
import androidx.navigation.navigation
import com.ys.phdmama.ui.screens.born.BornDashboardScreen
import com.ys.phdmama.ui.screens.wizard.alreadyborn.*

@Composable
fun NavGraphBuilder.bornNavGraph(navController: NavHostController) {
    navigation(startDestination = NavRoutes.BORN_DASHBOARD, route = "born") {
        composable(NavRoutes.BORN_DASHBOARD) {
            BornDashboardScreen(navController = navController)
        }
        composable(NavRoutes.BABY_NAME) {
            BabyNameScreen(navController = navController)
        }
        composable(NavRoutes.BABY_APGAR) {
            BabyAPGARScreen(navController = navController)
        }
        composable(NavRoutes.BABY_BLOOD_TYPE) {
            BabyBloodTypeScreen(navController = navController)
        }
        // Continúa añadiendo más pantallas para el rol `born`
    }
}
