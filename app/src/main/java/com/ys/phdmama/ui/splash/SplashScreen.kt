package com.ys.phdmama.ui.splash

import android.util.Log
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.size
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavHostController
import com.ys.phdmama.navigation.NavRoutes
import com.ys.phdmama.viewmodel.LoginViewModel
import com.ys.phdmama.viewmodel.WizardViewModel
import kotlinx.coroutines.delay

@Composable
fun SplashScreen(
    navController: NavHostController,
    loginViewModel: LoginViewModel = viewModel(),
    wizardViewModel: WizardViewModel = viewModel()
) {
    LaunchedEffect(Unit) {
//        delay(1000L) // Simula un tiempo de carga inicial
        handleNavigation(navController, loginViewModel, wizardViewModel)
    }

    Box(
        modifier = Modifier.fillMaxSize(),
        contentAlignment = Alignment.Center
    ) {
        CircularProgressIndicator(modifier = Modifier.size(50.dp))
    }
}

fun handleNavigation(
    navController: NavHostController,
    loginViewModel: LoginViewModel,
    wizardViewModel: WizardViewModel
) {
    val isUserLoggedIn = loginViewModel.checkUserAuthState()
    val wizardFinished = try {
        wizardViewModel.checkWizardFinished()
        wizardViewModel.wizardFinished.value
    } catch (e: Exception) {
        Log.e("SplashScreen", "Failed to check wizard status: ${e.localizedMessage}")
        false
    }

    if (isUserLoggedIn) {
        loginViewModel.fetchUserDetails(
            onSuccess = { role ->
                when {
                    wizardFinished && role == "born" -> navigateSafely(navController, "born")
                    wizardFinished && role == "waiting" -> navigateSafely(navController, "waiting")
                    else -> navigateSafely(navController, NavRoutes.BABY_STATUS)
                }
            },
            onSkip = {
                navigateSafely(navController, NavRoutes.BABY_STATUS)
            },
            onError = { errorMessage ->
                Log.e("SplashScreen", "Error fetching user details: $errorMessage")
                navigateSafely(navController, NavRoutes.LOGIN)
            }
        )
    } else {
        navigateSafely(navController, NavRoutes.LOGIN)
    }
}

fun navigateSafely(navController: NavHostController, route: String) {
    try {
        navController.navigate(route) {
            popUpTo(0) { inclusive = true }
        }
    } catch (e: Exception) {
        Log.e("Navigation", "Failed to navigate to $route: ${e.localizedMessage}")
    }
}