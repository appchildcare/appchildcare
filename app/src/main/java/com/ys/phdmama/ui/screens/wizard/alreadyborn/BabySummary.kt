package com.ys.phdmama.ui.screens.wizard.alreadyborn

import android.util.Log
import android.widget.Toast
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.Alignment
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavHostController
import com.ys.phdmama.navigation.NavRoutes
import com.ys.phdmama.viewmodel.BabyDataViewModel
import com.ys.phdmama.viewmodel.BabyStatusViewModel
import com.ys.phdmama.viewmodel.WizardViewModel

@Composable
fun BabySummary(
    navController: NavHostController,
    viewModel: BabyDataViewModel = hiltViewModel(),
    babyStatusViewModel: BabyStatusViewModel = hiltViewModel(),
    wizardViewModel: WizardViewModel = hiltViewModel()
) {
    val context = LocalContext.current
    val isLoading by remember { mutableStateOf(false) }
    val isLoadingRoleUpdate by babyStatusViewModel.isLoadingRoleUpdate.collectAsState()

    // Recopilar los datos del bebé en un mapa
    val babyData = mapOf(
        "name" to (viewModel.getBabyAttribute("name") ?: ""),
        "apgar" to (viewModel.getBabyAttribute("apgar") ?: ""),
        "height" to (viewModel.getBabyAttribute("height") ?: ""),
        "weight" to (viewModel.getBabyAttribute("weight") ?: ""),
        "perimeter" to (viewModel.getBabyAttribute("perimeter") ?: ""),
        "bloodType" to (viewModel.getBabyAttribute("bloodType") ?: ""),
        "sex" to (viewModel.getBabyAttribute("sex") ?: "")
    )

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(text = "Resumen de Datos del Bebé", style = MaterialTheme.typography.headlineSmall)

        Spacer(modifier = Modifier.height(16.dp))

        // Mostrar detalles del bebé
        babyData.forEach { (label, value) ->
            Text(text = "$label: $value", style = MaterialTheme.typography.bodyLarge)
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Botón para guardar los datos del bebé y actualizar el rol
        Button(
            onClick = {
                wizardViewModel.setWizardFinished(true)
                viewModel.addBabyToUser(
                    babyData = babyData,
                    onError = { errorMessage ->
                        Log.e("BabySummary", "Failed to save baby data: $errorMessage")
                        babyStatusViewModel.setLoadingRoleUpdate(false)
                        Toast.makeText(context, errorMessage, Toast.LENGTH_SHORT).show()
                    }
                )
            },
            enabled = !isLoadingRoleUpdate,
            modifier = Modifier
                .fillMaxWidth()
                .padding(8.dp)
        ) {
            if (isLoadingRoleUpdate) {
                CircularProgressIndicator()
            } else {
                Text(text = "Iniciemos la aventura")
            }
        }

        Button(
            onClick = {
                navController.navigate(NavRoutes.BABY_SEX)
            },
            enabled = !isLoading,
            modifier = Modifier.fillMaxWidth().padding(8.dp)
        ) {
            Text(text = "Revisar")
        }
    }
}
