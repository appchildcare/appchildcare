package com.ys.phdmama.navigation

import androidx.compose.runtime.Composable
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import com.ys.phdmama.ui.login.LoginScreen
import com.ys.phdmama.ui.main.MainScreen
import com.ys.phdmama.ui.register.RegisterScreen
import com.ys.phdmama.ui.splash.SplashScreen


object NavRoutes {
    const val SPLASH = "splash"
    const val LOGIN = "login"
    const val REGISTER = "register"
    const val MAIN = "main"
    // Add other routes here
}

@Composable
fun NavGraph(navController: NavHostController, startDestination: String = NavRoutes.SPLASH) {
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
        // Add other composable screens here
    }
}