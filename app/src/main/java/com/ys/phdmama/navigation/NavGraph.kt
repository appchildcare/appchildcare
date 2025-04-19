package com.ys.phdmama.navigation

import android.os.Build
import android.util.Log
import androidx.annotation.RequiresApi
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.lifecycle.viewmodel.compose.LocalViewModelStoreOwner
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavGraphBuilder
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.navigation
import com.ys.phdmama.ui.login.LoginScreen
import com.ys.phdmama.ui.main.MainScreen
import com.ys.phdmama.ui.register.RegisterScreen
import com.ys.phdmama.ui.screens.BabyProfileScreen
import com.ys.phdmama.ui.screens.MotherProfileScreen
import com.ys.phdmama.ui.screens.Resources
import com.ys.phdmama.ui.screens.born.AddBabyDataScreen
import com.ys.phdmama.ui.screens.born.BabyMenuScreen
import com.ys.phdmama.ui.screens.born.BabySelectionScreen
import com.ys.phdmama.ui.screens.born.BornDashboardScreen
import com.ys.phdmama.ui.screens.born.GrowthMilestonesScreen
import com.ys.phdmama.ui.screens.born.Vaccines
import com.ys.phdmama.ui.screens.counters.CounterHome
import com.ys.phdmama.ui.screens.pregnancy.PregnancyDashboardScreen
import com.ys.phdmama.ui.screens.pregnancy.PregnancyResourcesMenuScreen
import com.ys.phdmama.ui.screens.waiting.GynecologistScreen
import com.ys.phdmama.ui.screens.wizard.BabyStatusScreen
import com.ys.phdmama.ui.screens.wizard.alreadyborn.BabyAPGARScreen
import com.ys.phdmama.ui.screens.wizard.alreadyborn.BabyAlreadyBornScreen
import com.ys.phdmama.ui.screens.wizard.alreadyborn.BabyBloodTypeScreen
import com.ys.phdmama.ui.screens.wizard.alreadyborn.BabyHeightScreen
import com.ys.phdmama.ui.screens.wizard.alreadyborn.BabyNameScreen
import com.ys.phdmama.ui.screens.wizard.alreadyborn.BabyPerimeterScreen
import com.ys.phdmama.ui.screens.wizard.alreadyborn.BabySexScreen
import com.ys.phdmama.ui.screens.wizard.alreadyborn.BabySummary
import com.ys.phdmama.ui.screens.wizard.alreadyborn.BabyWeightScreen
import com.ys.phdmama.ui.screens.wizard.prebirth.BirthWaitingScreen
import com.ys.phdmama.ui.screens.wizard.prebirth.HappyWaitingScreen
import com.ys.phdmama.ui.screens.wizard.prebirth.RoughDateOfBirthScreen
import com.ys.phdmama.ui.splash.SplashScreen
import com.ys.phdmama.ui.welcome.WelcomeScreen
import com.ys.phdmama.viewmodel.BabyDataViewModel
import com.ys.phdmama.viewmodel.LoginViewModel
import com.ys.phdmama.viewmodel.MotherProfileViewModel
import com.ys.phdmama.viewmodel.WizardViewModel

object NavRoutes {
    const val WELCOME_SCREEN = "welcomeScreen"
    const val BORN_DASHBOARD = "bornDashboard"
    const val BORN_COUNTERS = "bornCounters"
    const val BORN_RESOURCES = "bornResources"
    const val BORN_MENU = "bornMenu"
    const val BORN_VACCINES = "BornVaccines"
    const val BORN_GROWTHMILESTONES = "bornGrowthMilestones"
    const val BORN_BABY_SELECTION = "bornBabySelection"
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
    const val WAITING_GYNECOLOGIST = "waiting_gynecologist"
    const val PREGNANCY_RESOURCES = "pregnancyResources"
    const val PREGNANCY_DASHBOARD = "pregnancyDashboard"
    const val SIDEBAR_BABY_PROFILE = "baby_profile"
    const val SIDEBAR_ADD_BABY = "add_baby"
    const val SIDEBAR_POLICIES = "policies"
    const val SIDEBAR_LINK1 = "link1"
    const val SIDEBAR_LINK2 = "link2"
    const val MOTHER_PROFILE = "mother_profile"
}

@RequiresApi(Build.VERSION_CODES.O)
@Composable
fun NavGraph(navController: NavHostController, startDestination: String = NavRoutes.SPLASH,
             openDrawer: () -> Unit) {
    val viewModelStoreOwner = LocalViewModelStoreOwner.current
    val babyDataViewModel: BabyDataViewModel = viewModel(viewModelStoreOwner!!)
    val loginViewModel: LoginViewModel = viewModel()
    val wizardViewModel: WizardViewModel = viewModel()
    val motherProfileViewModel: MotherProfileViewModel = viewModel()

    var userRole by rememberSaveable { mutableStateOf<String?>(null) }
    var isUserLoggedIn by remember { mutableStateOf(false) }
    var wizardFinished by remember { mutableStateOf(false) }

    Log.d("NavGraph", "userRole = $userRole")

    LaunchedEffect(Unit) {
        isUserLoggedIn = loginViewModel.checkUserAuthState()
        if (isUserLoggedIn) {
            loginViewModel.fetchUserDetails(
                onSuccess = { role ->
                    userRole = role
                },
                onSkip = {
                    userRole = null // Omite la validación del role
                },
                onError = {
                    userRole = null // Manejo de errores
                }
            )
            wizardViewModel.checkWizardFinished()
            wizardFinished = wizardViewModel.wizardFinished.value
        }
    }

    Log.d("NavGraph", "userRole = $userRole, isUserLoggedIn = $isUserLoggedIn, wizardFinished = $wizardFinished")

    // Construcción del NavHost
    NavHost(navController = navController, startDestination = if (isUserLoggedIn) startDestination else NavRoutes.WELCOME_SCREEN) {

        composable(NavRoutes.SPLASH) {
            SplashScreen(navController = navController, loginViewModel, wizardViewModel)
        }
        composable(NavRoutes.WELCOME_SCREEN) {
            WelcomeScreen(navController = navController)
        }
        composable(NavRoutes.LOGIN) {
            LoginScreen(navController = navController)
        }
        composable(NavRoutes.REGISTER) {
            RegisterScreen(navController = navController)
        }
        composable(NavRoutes.MAIN) {
            MainScreen(navController = navController, openDrawer = openDrawer)
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
        composable(NavRoutes.BORN_COUNTERS) {
            CounterHome(navController = navController, openDrawer = openDrawer)
        }
        composable(NavRoutes.BORN_RESOURCES) {
            Resources(navController = navController, openDrawer = openDrawer)
        }
        composable(NavRoutes.PREGNANCY_RESOURCES) {
            PregnancyResourcesMenuScreen(navController = navController, openDrawer = openDrawer)
        }
        composable(NavRoutes.WAITING_GYNECOLOGIST) {
            GynecologistScreen(navController = navController, loginViewModel, openDrawer = openDrawer)
        }

        composable(NavRoutes.PREGNANCY_DASHBOARD) {
            PregnancyDashboardScreen(navController = navController, openDrawer = openDrawer)
        }

        composable(NavRoutes.SIDEBAR_ADD_BABY) {
            AddBabyDataScreen(navController = navController, openDrawer = openDrawer)
        }

        composable(NavRoutes.MOTHER_PROFILE) {
            MotherProfileScreen(navController = navController, motherProfileViewModel, openDrawer = openDrawer)
        }

        composable(NavRoutes.BORN_VACCINES) {
            Vaccines (navController = navController, babyDataViewModel, openDrawer = openDrawer)
        }

        navigation(startDestination = NavRoutes.BORN_DASHBOARD, route = "born") {
            composable(NavRoutes.BORN_DASHBOARD) {
                BornDashboardScreen(navController = navController, openDrawer = openDrawer)
            }
            composable(NavRoutes.BORN_MENU) {
                BabyMenuScreen(navController = navController)
            }
            composable(NavRoutes.BORN_GROWTHMILESTONES) {
                GrowthMilestonesScreen(navController = navController)
            }
            composable(NavRoutes.BORN_BABY_SELECTION) {
                BabySelectionScreen(navController = navController, openDrawer = openDrawer)
            }
        }

        navigation(startDestination = NavRoutes.WAITING_DASHBOARD, route = "waiting") {
            composable(NavRoutes.WAITING_DASHBOARD) {
                PregnancyDashboardScreen(navController = navController, openDrawer = openDrawer)
            }
            composable(NavRoutes.ROUGHBIRTH) {
                RoughDateOfBirthScreen(navController = navController)
            }
            composable(NavRoutes.WAITING_GYNECOLOGIST) {
                GynecologistScreen(navController = navController, openDrawer = openDrawer)
            }
            composable(NavRoutes.PREGNANCY_RESOURCES) {
                PregnancyResourcesMenuScreen(navController = navController, openDrawer = openDrawer)
            }
        }

        // Definir bornNavGraph y waitingNavGraph
        when {
            userRole == "born" -> bornNavGraph(navController, babyDataViewModel, openDrawer)
            userRole == "waiting" -> waitingNavGraph(navController, babyDataViewModel, openDrawer)
        }
    }
}

fun NavGraphBuilder.bornNavGraph(navController: NavHostController, babyDataViewModel: BabyDataViewModel,  openDrawer: () -> Unit) {
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

@RequiresApi(Build.VERSION_CODES.O)
fun NavGraphBuilder.waitingNavGraph(navController: NavHostController, babyDataViewModel: BabyDataViewModel, openDrawer: () -> Unit) {
    composable(NavRoutes.WAITING_DASHBOARD) {
        PregnancyDashboardScreen(navController = navController, openDrawer = openDrawer)
    }
    composable(NavRoutes.ROUGHBIRTH) {
        RoughDateOfBirthScreen(navController = navController)
    }
    composable(NavRoutes.PREGNANCY_RESOURCES) {
        PregnancyResourcesMenuScreen(navController = navController, openDrawer = openDrawer)
    }
}
