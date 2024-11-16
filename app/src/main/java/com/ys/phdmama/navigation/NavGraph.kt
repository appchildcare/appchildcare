package com.ys.phdmama.navigation

import androidx.compose.runtime.*
import androidx.lifecycle.viewmodel.compose.LocalViewModelStoreOwner
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavGraphBuilder
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import com.ys.phdmama.ui.login.LoginScreen
import com.ys.phdmama.ui.main.MainScreen
import com.ys.phdmama.ui.register.RegisterScreen
import com.ys.phdmama.ui.screens.BabyProfileScreen
import com.ys.phdmama.ui.screens.born.BabyMenuScreen
import com.ys.phdmama.ui.screens.born.BornDashboardScreen
import com.ys.phdmama.ui.screens.born.GrowthMilestonesScreen
import com.ys.phdmama.ui.screens.waiting.WaitingDashboardScreen
import com.ys.phdmama.ui.screens.wizard.BabyStatusScreen
import com.ys.phdmama.ui.screens.wizard.alreadyborn.*
import com.ys.phdmama.ui.screens.wizard.prebirth.*
import com.ys.phdmama.ui.splash.SplashScreen
import com.ys.phdmama.viewmodel.BabyDataViewModel
import com.ys.phdmama.viewmodel.LoginViewModel
import com.ys.phdmama.viewmodel.WizardViewModel

object NavRoutes {
    const val BORN_DASHBOARD = "bornDashboard"
    const val BORN_COUNTERS = "bornCounters"
    const val BORN_RESOURCES = "bornResources"
    const val BORN_MENU = "bornMenu"
    const val BORN_VACCINES = "BornVaccines"
    const val BORN_GROWTHMILESTONES = "bornGrowthMilestones"
    const val WAITING_DASHBOARD = "waitingDashboard"
    const val SPLASH = "splash"
    const val LOGIN = "login"
    const val REGISTER = "register"
    const val MAIN = "main"
    const val BABY_PROFILE = "baby_profile"
    const val BABY_STATUS = "baby_status"
    const val BABY_ALREADY_BORN = "baby_already_born"
    const val BIRTH_WAITING = "baby_birth_waiting"
    const val ROUGHBIRTH = "roughbirth"
    const val HAPPY_WAITING = "happy_waiting"
    const val BABY_NAME = "baby_name"
    const val BABY_SUMMARY = "baby_summary"
    const val BABY_APGAR = "baby_apgar"
    const val BABY_WEIGHT = "baby_weight"
    const val BABY_HEIGHT = "baby_height"
    const val BABY_BLOOD_TYPE = "baby_blood_type"
    const val BABY_SEX = "baby_sex"
    const val BABY_PERIMETER = "baby_perimeter"
}

@Composable
fun NavGraph(navController: NavHostController, startDestination: String = NavRoutes.SPLASH) {
    val viewModelStoreOwner = LocalViewModelStoreOwner.current
    val babyDataViewModel: BabyDataViewModel = viewModel(viewModelStoreOwner!!)
    val loginViewModel: LoginViewModel = viewModel()
    val wizardViewModel: WizardViewModel = viewModel()

    var userRole by remember { mutableStateOf<String?>(null) }

    LaunchedEffect(Unit) {
        loginViewModel.fetchUserRole { role ->
            userRole = role
        }
    }

    if (userRole != null) {
        NavHost(navController = navController, startDestination = startDestination) {

            composable(NavRoutes.SPLASH) {
                SplashScreen(navController = navController, loginViewModel, wizardViewModel)
            }
            composable(NavRoutes.LOGIN) {
                LoginScreen(navController = navController)
            }
            composable(NavRoutes.REGISTER) {
                RegisterScreen(navController = navController)
            }
            composable(NavRoutes.MAIN) {
                MainScreen(navController = navController)
            }
            composable(NavRoutes.BABY_STATUS) {
                BabyStatusScreen(navController = navController)
            }
            composable(NavRoutes.BABY_PROFILE) {
                BabyProfileScreen(navController = navController)
            }
            composable(NavRoutes.BABY_ALREADY_BORN) {
                BabyAlreadyBornScreen(navController = navController)
            }
            composable(NavRoutes.BIRTH_WAITING) {
                BirthWaitingScreen(navController = navController)
            }
            composable(NavRoutes.HAPPY_WAITING) {
                HappyWaitingScreen(navController = navController)
            }
            composable(NavRoutes.BABY_NAME) {
                BabyNameScreen(navController = navController, viewModel = babyDataViewModel)
            }
            composable(NavRoutes.BABY_APGAR) {
                BabyAPGARScreen(navController = navController, viewModel = babyDataViewModel)
            }
            composable(NavRoutes.BABY_PERIMETER) {
                BabyPerimeterScreen(navController = navController, viewModel = babyDataViewModel)
            }
            composable(NavRoutes.BABY_WEIGHT) {
                BabyWeightScreen(navController = navController, viewModel = babyDataViewModel)
            }
            composable(NavRoutes.BABY_HEIGHT) {
                BabyHeightScreen(navController = navController, viewModel = babyDataViewModel)
            }
            composable(NavRoutes.BABY_BLOOD_TYPE) {
                BabyBloodTypeScreen(navController = navController, viewModel = babyDataViewModel)
            }
            composable(NavRoutes.BABY_SEX) {
                BabySexScreen(navController = navController, viewModel = babyDataViewModel)
            }
            composable(NavRoutes.BABY_SUMMARY) {
                BabySummary(navController = navController, viewModel = babyDataViewModel)
            }
            composable(NavRoutes.ROUGHBIRTH) {
                RoughDateOfBirthScreen(navController = navController)
            }

            when (userRole) {
                "born" -> bornNavGraph(navController, babyDataViewModel)
                "waiting" -> waitingNavGraph(navController, babyDataViewModel)
            }
        }
    }
}

fun NavGraphBuilder.bornNavGraph(navController: NavHostController, babyDataViewModel: BabyDataViewModel) {
    composable(NavRoutes.BORN_DASHBOARD) {
        BornDashboardScreen(navController = navController)
    }
    composable(NavRoutes.BORN_MENU) {
        BabyMenuScreen(navController = navController)
    }
    composable(NavRoutes.BORN_GROWTHMILESTONES) {
        GrowthMilestonesScreen(navController = navController)
    }
}

fun NavGraphBuilder.waitingNavGraph(navController: NavHostController, babyDataViewModel: BabyDataViewModel) {
    composable(NavRoutes.WAITING_DASHBOARD) {
        WaitingDashboardScreen(navController = navController)
    }
}
