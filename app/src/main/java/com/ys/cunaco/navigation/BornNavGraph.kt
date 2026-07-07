package com.ys.cunaco.navigation

import androidx.compose.runtime.Composable
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import com.ys.cunaco.viewmodel.BabyDataViewModel

// TODO: remove unused nav graph
@Composable
fun BornNavGraph(navController: NavHostController, babyDataViewModel: BabyDataViewModel,
                 openDrawer: () -> Unit,) {
    NavHost(navController = navController, startDestination = NavRoutes.BORN_DASHBOARD) {
        composable(NavRoutes.BORN_DASHBOARD) {
//            BornDashboardScreen(navController = navController, openDrawer = openDrawer)
        }
        composable(NavRoutes.BORN_MENU) {
//            BabyMenuScreen(navController = navController)
        }
        composable(NavRoutes.BORN_GROWTHMILESTONES) {
//            GrowthMilestonesScreen(navController = navController, openDrawer = openDrawer)
        }
    }
}
