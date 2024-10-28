package com.ys.phdmama.navigation

import androidx.compose.runtime.Composable
import androidx.lifecycle.viewmodel.compose.LocalViewModelStoreOwner
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import com.ys.phdmama.ui.login.LoginScreen
import com.ys.phdmama.ui.main.MainScreen
import com.ys.phdmama.ui.register.RegisterScreen
import com.ys.phdmama.ui.screens.BabyProfileScreen
import com.ys.phdmama.ui.screens.wizard.BabyStatusScreen
import com.ys.phdmama.ui.screens.wizard.alreadyborn.BabyAPGARScreen
import com.ys.phdmama.ui.splash.SplashScreen
import com.ys.phdmama.ui.screens.wizard.alreadyborn.BabyAlreadyBornScreen
import com.ys.phdmama.ui.screens.wizard.alreadyborn.BabyBloodTypeScreen
import com.ys.phdmama.ui.screens.wizard.alreadyborn.BabyNameScreen
import com.ys.phdmama.ui.screens.wizard.alreadyborn.BabyWeightScreen
import com.ys.phdmama.ui.screens.wizard.alreadyborn.BabyHeightScreen
import com.ys.phdmama.ui.screens.wizard.alreadyborn.BabyPerimeterScreen
import com.ys.phdmama.ui.screens.wizard.alreadyborn.BabySexScreen
import com.ys.phdmama.ui.screens.wizard.alreadyborn.BabySummary
import com.ys.phdmama.ui.screens.wizard.prebirth.BirthWaitingScreen
import com.ys.phdmama.ui.screens.wizard.prebirth.HappyWaitingScreen
import com.ys.phdmama.ui.screens.wizard.prebirth.RoughDateOfBirthScreen
import com.ys.phdmama.viewmodel.BabyDataViewModel


object NavRoutes {
    const val SPLASH = "splash"
    const val LOGIN = "login"
    const val REGISTER = "register"
    const val MAIN = "main"
    const val BABY_PROFILE = "baby_profile"
//    const val CHECKLIST_PREBIRTH = "checklist_prebirth"
//    const val CHECKLIST_NEWBORN = ""
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

    // Add other routes here
}

@Composable
fun NavGraph(navController: NavHostController, startDestination: String = NavRoutes.SPLASH) {
    val viewModelStoreOwner = LocalViewModelStoreOwner.current
    val babyDataViewModel: BabyDataViewModel = viewModel(viewModelStoreOwner!!)

    NavHost(navController = navController, startDestination = startDestination) {

        composable(NavRoutes.SPLASH) {
            SplashScreen(navController = navController)
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
        composable(NavRoutes.ROUGHBIRTH) {
            RoughDateOfBirthScreen(navController = navController)
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
        // Add other composable screens here
    }
}