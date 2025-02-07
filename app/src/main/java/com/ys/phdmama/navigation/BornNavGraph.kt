package com.ys.phdmama.navigation

import androidx.compose.runtime.Composable
import androidx.navigation.NavGraphBuilder
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.navigation
import androidx.navigation.compose.composable
import com.ys.phdmama.ui.screens.born.BabyMenuScreen
import com.ys.phdmama.ui.screens.born.BornDashboardScreen
import com.ys.phdmama.ui.screens.born.GrowthMilestonesScreen
import com.ys.phdmama.ui.screens.wizard.alreadyborn.*
import com.ys.phdmama.viewmodel.BabyDataViewModel


@Composable
fun BornNavGraph(navController: NavHostController, babyDataViewModel: BabyDataViewModel,
                 openDrawer: () -> Unit,) {
    NavHost(navController = navController, startDestination = NavRoutes.BORN_DASHBOARD) {
        composable(NavRoutes.BORN_DASHBOARD) {
            BornDashboardScreen(navController = navController, openDrawer = openDrawer)
        }
        composable(NavRoutes.BORN_MENU) {
            BabyMenuScreen(navController = navController)
        }
        composable(NavRoutes.BORN_GROWTHMILESTONES) {
            GrowthMilestonesScreen(navController = navController)
        }
    }
}
