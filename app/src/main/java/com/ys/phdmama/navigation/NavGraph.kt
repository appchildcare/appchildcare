package com.ys.phdmama.navigation

import BabyCounterSelectionScreen
import SleepDiaryScreen
import SleepDiaryViewModel
import android.os.Build
import android.util.Log
import androidx.annotation.RequiresApi
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavGraphBuilder
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.navigation
import com.ys.phdmama.ui.login.LoginScreen
import com.ys.phdmama.ui.main.MainScreen
import com.ys.phdmama.ui.register.RegisterScreen
import com.ys.phdmama.ui.screens.Resources
import com.ys.phdmama.ui.screens.TermsConditions
import com.ys.phdmama.ui.screens.born.AddBabyDataScreen
import com.ys.phdmama.ui.screens.born.BabyMenuScreen
import com.ys.phdmama.ui.screens.born.BornDashboardScreen
import com.ys.phdmama.ui.screens.born.GrowthMilestonesScreen
import com.ys.phdmama.ui.screens.born.VaccineScreen
import com.ys.phdmama.ui.screens.pregnancy.PregnancyDashboardScreen
import com.ys.phdmama.ui.screens.pregnancy.PregnancyResourcesMenuScreen
import com.ys.phdmama.ui.screens.born.BornResourcesLeaveHome
import com.ys.phdmama.ui.screens.born.GrowthDetailScreen
import com.ys.phdmama.ui.screens.born.charts.HeadCircumferenceDetailScreen
import com.ys.phdmama.ui.screens.born.charts.HeightLengthDetailScreen
import com.ys.phdmama.ui.screens.carbonfootprint.CarbonFootprintScreen
import com.ys.phdmama.ui.screens.counters.SleepingCounterScreen
import com.ys.phdmama.ui.screens.pediatrician.PediatricVisitScreen
import com.ys.phdmama.ui.screens.pediatrician.PediatricianQuestionsScreen
import com.ys.phdmama.ui.screens.poop.PoopRegistrationScreen
import com.ys.phdmama.ui.screens.counters.LactationCounterScreen
import com.ys.phdmama.ui.screens.counters.LactationDiaryScreen
import com.ys.phdmama.ui.screens.foodregistration.FoodRegistrationScreen
import com.ys.phdmama.ui.screens.medicine.MedicineRegistrationScreen
import com.ys.phdmama.ui.screens.poop.PoopDiaryScreen
import com.ys.phdmama.ui.screens.poop.PoopMainScreen
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
import com.ys.phdmama.ui.welcome.WelcomeSlider
import com.ys.phdmama.viewmodel.BabyDataViewModel
import com.ys.phdmama.viewmodel.CarbonFootprintViewModel
import com.ys.phdmama.viewmodel.CounterViewModel
import com.ys.phdmama.viewmodel.GrowthMilestonesViewModel
import com.ys.phdmama.viewmodel.LactancyDiaryViewModel
import com.ys.phdmama.viewmodel.LactationViewModel
import com.ys.phdmama.viewmodel.LoginViewModel
import com.ys.phdmama.viewmodel.PoopDiaryViewModel
import com.ys.phdmama.viewmodel.PoopRegistrationViewModel
import com.ys.phdmama.viewmodel.WizardViewModel

object NavRoutes {
    const val WELCOME_SCREEN = "welcomeScreen"
    const val BORN_DASHBOARD = "bornDashboard"
    const val BORN_COUNTERS = "bornCounters"
    const val BORN_RESOURCES = "bornResources"
    const val BORN_RESOURCES_CHECKLIST = "bornResourcesChecklist"
    const val BORN_RESOURCES_LEAVE_HOME = "bornResourcesLeaveHome"
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
    const val WAITING_GYNECOLOGIST = "waiting_gynecologist"
    const val PREGNANCY_RESOURCES = "pregnancyResources"
    const val PREGNANCY_DASHBOARD = "pregnancyDashboard"
    const val SIDEBAR_BABY_PROFILE = "baby_profile"
    const val SIDEBAR_ADD_BABY = "add_baby"
    const val SIDEBAR_POLICIES = "policies"
    const val SIDEBAR_LINK1 = "link1"
    const val SIDEBAR_LINK2 = "link2"
    const val PEDIATRICIAN_QUESTIONS = "pediatrician_questions"
    const val PEDIATRICIAN_VISITS = "pediatrician_visits"
    const val BORN_GROW_CHART_DETAILS = "born_grow_chart_details"
    const val BORN_HEAD_CIRCUMFERENCE_CHART_DETAILS = "born_head_circumference_chart_details"
    const val BORN_HEIGHT_WEIGHT_CHART_DETAILS = "born_height_weight_chart_details"
    const val BORN_SNAP_COUNTER_REPORTS = "born_snap_counter_reports"
    const val BORN_LACTATION_COUNTER_REPORTS = "born_lactation_counter_reports"
    const val POOP_REGISTER = "poop_register"
    const val SLEEP_TRACKING = "sleep_tracking"
    const val LACTATION_TRACKING = "lactation_tracking"
    const val POO_MAIN_SELECTION = "poop_main_screen"
    const val POOP_TRACKING = "poop_tracking"
    const val TERMS_CONDITIONS = "terms_conditions"
    const val CARBON_FOOTPRINT = "carbon_footprint"
    const val FOOD_REGISTRATION = "food_registration"
    const val MEDICINE_REGISTRATION = "medicine_registration"
}

@RequiresApi(Build.VERSION_CODES.O)
@Composable
fun NavGraph(navController: NavHostController, startDestination: String = NavRoutes.SPLASH,
             openDrawer: () -> Unit) {
    val babyDataViewModel: BabyDataViewModel = hiltViewModel()
    val loginViewModel: LoginViewModel = hiltViewModel()
    val wizardViewModel: WizardViewModel = hiltViewModel()
    val growthMilestonesViewModel: GrowthMilestonesViewModel = hiltViewModel()
    val poopRegisterViewModel: PoopRegistrationViewModel = hiltViewModel()
    val sleepDiaryViewModel: SleepDiaryViewModel = viewModel()
    val lactationDiaryViewModel: LactancyDiaryViewModel = hiltViewModel()
    val lactationViewModel: LactationViewModel = hiltViewModel()
    val counterViewModel: CounterViewModel = hiltViewModel()
    val poopDiaryViewModel: PoopDiaryViewModel = hiltViewModel()
    val carbonFootViewModel: CarbonFootprintViewModel = hiltViewModel()

    var userRole by rememberSaveable { mutableStateOf<String?>(null) }
    var babyId by rememberSaveable { mutableStateOf<String?>(null) }
    var isUserLoggedIn by remember { mutableStateOf(false) }
    var wizardFinished by remember { mutableStateOf(false) }
    Log.d("NavGraph", "userRole = $userRole")

    LaunchedEffect(Unit) {
        loginViewModel.getUserUid(
            onSuccess = { baby ->
                babyId = baby
            },
            onSkip = {
                babyId = null
            },
            onError = {
                babyId = null
            }
        )
    }

    Log.d("fetch babies", "babyId = $babyId")

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
            WelcomeSlider(navController = navController)
        }
        composable(NavRoutes.TERMS_CONDITIONS) {
            TermsConditions (navController = navController, openDrawer = openDrawer)
        }
        composable(NavRoutes.CARBON_FOOTPRINT) {
            CarbonFootprintScreen (viewModel = carbonFootViewModel, navController = navController, openDrawer = openDrawer)
        }
        composable(NavRoutes.FOOD_REGISTRATION) {
            FoodRegistrationScreen(navController = navController, openDrawer = openDrawer)
        }
        composable(NavRoutes.MEDICINE_REGISTRATION) {
            MedicineRegistrationScreen(navController = navController, openDrawer = openDrawer)
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
//            BabyDataScreen(navController = navController, openDrawer = openDrawer)
            AddBabyDataScreen(navController = navController, babyId = babyId, openDrawer = openDrawer,)
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
            BabyCounterSelectionScreen (navController = navController, openDrawer = openDrawer, babyId = babyId)
        }
        composable(NavRoutes.BORN_RESOURCES) {
            Resources(navController = navController, openDrawer = openDrawer)
        }
        composable(NavRoutes.BORN_RESOURCES_CHECKLIST) {
            Resources(navController = navController, openDrawer = openDrawer)
        }
        composable(NavRoutes.BORN_RESOURCES_LEAVE_HOME) {
            BornResourcesLeaveHome(navController = navController, openDrawer = openDrawer)
        }
        composable(NavRoutes.PREGNANCY_RESOURCES) {
            PregnancyResourcesMenuScreen(navController = navController, openDrawer = openDrawer)
        }
        composable(NavRoutes.WAITING_GYNECOLOGIST) {
            GynecologistScreen(navController = navController, openDrawer = openDrawer)
        }

        composable(NavRoutes.PREGNANCY_DASHBOARD) {
            PregnancyDashboardScreen(navController = navController, openDrawer = openDrawer)
        }

        composable(NavRoutes.SIDEBAR_ADD_BABY) {
           // AddBabyDataScreen(navController = navController, openDrawer = openDrawer, babyId = babyId)
        }

        composable(NavRoutes.PEDIATRICIAN_QUESTIONS) {
            PediatricianQuestionsScreen(navController = navController, openDrawer = openDrawer)
        }

        composable(NavRoutes.PEDIATRICIAN_VISITS) {
            PediatricVisitScreen(navController = navController, openDrawer = openDrawer)
        }

        composable(NavRoutes.BORN_VACCINES) {
            VaccineScreen (navController = navController, babyDataViewModel, openDrawer = openDrawer)
        }

        navigation(startDestination = NavRoutes.BORN_DASHBOARD, route = "born") {
            composable(NavRoutes.BORN_DASHBOARD) {
                BornDashboardScreen(navController = navController, growthMilestonesViewModel, openDrawer = openDrawer)
            }
            composable(NavRoutes.BORN_MENU) {
                BabyMenuScreen(navController = navController, openDrawer = openDrawer)
            }
            composable(NavRoutes.BORN_GROWTHMILESTONES) {
                GrowthMilestonesScreen(navController = navController,  openDrawer = openDrawer, babyId = babyId)
            }
            composable(NavRoutes.BORN_GROW_CHART_DETAILS) {
                GrowthDetailScreen(navController = navController, growthMilestonesViewModel, openDrawer = openDrawer, babyId = babyId)
            }
            composable(NavRoutes.BORN_HEAD_CIRCUMFERENCE_CHART_DETAILS) {
                HeadCircumferenceDetailScreen(navController = navController, growthMilestonesViewModel, openDrawer = openDrawer, babyId = babyId)
            }
            composable(NavRoutes.BORN_HEIGHT_WEIGHT_CHART_DETAILS) {
                HeightLengthDetailScreen(navController = navController, growthMilestonesViewModel, openDrawer = openDrawer, babyId = babyId)
            }
            composable(NavRoutes.TERMS_CONDITIONS) {
                TermsConditions (navController = navController, openDrawer = openDrawer)
            }
            composable(NavRoutes.POOP_REGISTER) {
                babyId?.let { it1 ->
                    PoopRegistrationScreen(
                        navController = navController, openDrawer = openDrawer,
                        userId = it1,
                        babyId = it1,
                        babyName = "",
                        viewModel = poopRegisterViewModel
                    )
                }
            }
//            babyId: String?,
//            viewModel: PoopDiaryViewModel = hiltViewModel(),
//            navController: NavHostController,
//            openDrawer: () -> Unit
            composable(NavRoutes.POOP_TRACKING) {
                babyId?.let { it1 ->
                    PoopDiaryScreen (
                        babyId = it1,
                        viewModel = poopDiaryViewModel,
                        navController = navController,
                        openDrawer = openDrawer,
                    )
                }
            }
            composable(NavRoutes.POO_MAIN_SELECTION) {
                PoopMainScreen(
                    navController = navController, openDrawer = openDrawer
                )
            }
            composable(NavRoutes.BORN_SNAP_COUNTER_REPORTS) {
                SleepDiaryScreen(babyId = babyId, sleepDiaryViewModel, navController = navController, openDrawer = openDrawer)
            }
            composable(NavRoutes.BORN_LACTATION_COUNTER_REPORTS) {
                LactationDiaryScreen(babyId = babyId, lactationDiaryViewModel, navController = navController, openDrawer = openDrawer)
            }
            composable(NavRoutes.BORN_COUNTERS) {
                BabyCounterSelectionScreen(babyId = babyId, navController = navController, openDrawer = openDrawer)
            }
            composable(NavRoutes.LACTATION_TRACKING) {
                LactationCounterScreen(babyId = babyId, navController = navController, lactationViewModel, openDrawer = openDrawer)
            }
            composable(NavRoutes.SLEEP_TRACKING) {
                SleepingCounterScreen(babyId = babyId, navController = navController, counterViewModel, openDrawer = openDrawer)
            }
            composable(NavRoutes.POOP_REGISTER) {
                babyId?.let { it1 ->
                    PoopRegistrationScreen(
                        navController = navController, openDrawer = openDrawer,
                        userId = it1,
                        babyId = it1,
                        babyName = "",
                        viewModel = poopRegisterViewModel
                    )
                }
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
            userRole == "born" -> bornNavGraph(navController, babyDataViewModel, openDrawer, babyId, growthMilestonesViewModel)
            userRole == "waiting" -> waitingNavGraph(navController, babyDataViewModel, openDrawer)
        }
    }
}

@RequiresApi(Build.VERSION_CODES.O)
fun NavGraphBuilder.bornNavGraph(navController: NavHostController, babyDataViewModel: BabyDataViewModel,
                                 openDrawer: () -> Unit, babyId: String?, growthMilestonesViewModel: GrowthMilestonesViewModel) {
    composable(NavRoutes.BORN_DASHBOARD) {
        BornDashboardScreen(navController = navController, growthMilestonesViewModel, openDrawer = openDrawer)
    }
    composable(NavRoutes.BORN_MENU) {
        BabyMenuScreen(navController = navController, openDrawer = openDrawer)
    }
    composable(NavRoutes.BORN_GROWTHMILESTONES) {
        GrowthMilestonesScreen(navController = navController, openDrawer = openDrawer, babyId = babyId)
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
