package com.ys.phdmama.ui.screens.wizard.alreadyborn

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.Alignment
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavHostController
import com.ys.phdmama.navigation.NavRoutes
import com.ys.phdmama.viewmodel.BabyDataViewModel
import com.ys.phdmama.viewmodel.WizardViewModel
import com.ys.phdmama.viewmodel.WizardViewModelFactory

@Composable
fun BabySummary(navController: NavHostController,
                   viewModel: BabyDataViewModel = viewModel(),
                   wizardViewModel: WizardViewModel = viewModel(factory = WizardViewModelFactory(LocalContext.current))) {

    val babyName by viewModel.babyName.collectAsState()
    val babyAPGAR by viewModel.babyAPGAR.collectAsState()
    val babyHeight by viewModel.babyHeight .collectAsState()
    val babyWeight by viewModel.babyWeight.collectAsState()
    val babyBloodType by viewModel.babyBloodType.collectAsState()
    val babySex by viewModel.babySex.collectAsState()

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(text = "Resumen de Datos del Bebé", style = MaterialTheme.typography.headlineSmall)

        Spacer(modifier = Modifier.height(16.dp))

        Text(text = "Name: $babyName", style = MaterialTheme.typography.bodyLarge)
        Text(text = "APGAR: $babyAPGAR", style = MaterialTheme.typography.bodyLarge)
        Text(text = "babyHeight: $babyHeight", style = MaterialTheme.typography.bodyLarge)
        Text(text = "babyWeight: $babyWeight", style = MaterialTheme.typography.bodyLarge)
        Text(text = "Tipo de Sangre: $babyBloodType", style = MaterialTheme.typography.bodyLarge)
        Text(text = "Sexo: $babySex", style = MaterialTheme.typography.bodyLarge)

        Spacer(modifier = Modifier.height(16.dp))

        Button(onClick = {
//                wizardViewModel.setWizardFinished(true)
                navController.navigate(NavRoutes.MAIN) {
                    popUpTo(0) { inclusive = true }
            }
        }) {
            Text(text = "Confirmar y Guardar")
        }

        Button(onClick = {
            navController.navigate(NavRoutes.BABY_SEX) {}
        }) {
            Text(text = "Revisar")
        }
    }
}