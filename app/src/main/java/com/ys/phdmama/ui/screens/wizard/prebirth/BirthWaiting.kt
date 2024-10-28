package com.ys.phdmama.ui.screens.wizard.prebirth

import androidx.compose.foundation.layout.*
import androidx.compose.material3.Button
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavHostController
import com.ys.phdmama.navigation.NavRoutes
import com.ys.phdmama.viewmodel.WizardViewModel

@Composable
fun BirthWaitingScreen(
    navController: NavHostController,
    wizardViewModel: WizardViewModel = viewModel()
) {
    val context = LocalContext.current

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.Center
    ) {
        Text(text = "Pantalla: En la dulce espera")

        Button(
            onClick = {
//                wizardViewModel.setWizardFinished(finished = true)
                navController.navigate(NavRoutes.ROUGHBIRTH) {
                    popUpTo(NavRoutes.BABY_STATUS) { inclusive = true }
                }
            },
            modifier = Modifier
                .fillMaxWidth()
                .padding(8.dp)
        ) {
            Text(text = "Finalizar wizard")
        }
    }
}
