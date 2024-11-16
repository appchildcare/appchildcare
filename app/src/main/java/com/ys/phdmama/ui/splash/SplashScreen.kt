package com.ys.phdmama.ui.splash

import android.util.Log
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.size
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
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
    val context = LocalContext.current
    val wizardFinished by wizardViewModel.wizardFinished.collectAsState()

    LaunchedEffect(Unit) {
        delay(1000L)
        val isUserLoggedIn = loginViewModel.checkUserAuthState()
        wizardViewModel.checkWizardFinished()

        if (isUserLoggedIn) {
            val userRole = loginViewModel.getUserRoleFromFirestore()
            if (wizardFinished) {
                when (userRole) {
                    "born" -> navController.navigate(NavRoutes.BORN_DASHBOARD) {
                        popUpTo(0) { inclusive = true }
                    }
                    "waiting" -> navController.navigate(NavRoutes.WAITING_DASHBOARD) {
                        popUpTo(0) { inclusive = true }
                    }
                    else -> navController.navigate(NavRoutes.BABY_STATUS) {
                        popUpTo(NavRoutes.SPLASH) { inclusive = true }
                    }
                }
            } else {
                navController.navigate(NavRoutes.BABY_STATUS) {
                    popUpTo(NavRoutes.SPLASH) { inclusive = true }
                }
            }
        } else {
            navController.navigate(NavRoutes.LOGIN) {
                popUpTo(NavRoutes.SPLASH) { inclusive = true }
            }
        }
    }

    Box(
        modifier = Modifier.fillMaxSize(),
        contentAlignment = Alignment.Center
    ) {
        CircularProgressIndicator(modifier = Modifier.size(50.dp))
    }
}
