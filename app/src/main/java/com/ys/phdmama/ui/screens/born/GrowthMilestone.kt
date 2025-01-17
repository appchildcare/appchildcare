package com.ys.phdmama.ui.screens.born

import android.util.Log
import androidx.compose.foundation.layout.*
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavHostController
import com.ys.phdmama.viewmodel.BabyDataViewModel
import com.ys.phdmama.viewmodel.GrowthMilestonesViewModel
import java.util.UUID

@Composable
fun GrowthMilestonesScreen(
    navController: NavHostController,
    viewModel: GrowthMilestonesViewModel = viewModel()
) {
    // Variables para los campos de texto
    var weight by remember { mutableStateOf("") }
    var height by remember { mutableStateOf("") }
    var headCircumference by remember { mutableStateOf("") }

    // Estado para controlar la visibilidad de la alerta
    var showAlert by remember { mutableStateOf(false) }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(text = "Registrar Hitos del Crecimiento", style = MaterialTheme.typography.titleLarge)

        Spacer(modifier = Modifier.height(16.dp))

        OutlinedTextField(
            value = weight,
            onValueChange = { weight = it },
            label = { Text("Peso (kg)") },
            modifier = Modifier.fillMaxWidth()
        )

        Spacer(modifier = Modifier.height(16.dp))

        OutlinedTextField(
            value = height,
            onValueChange = { height = it },
            label = { Text("Talla (cm)") },
            modifier = Modifier.fillMaxWidth()
        )

        Spacer(modifier = Modifier.height(16.dp))

        OutlinedTextField(
            value = headCircumference,
            onValueChange = { headCircumference = it },
            label = { Text("Perímetro Cefálico (cm)") },
            modifier = Modifier.fillMaxWidth()
        )

        Spacer(modifier = Modifier.height(16.dp))

        Button(onClick = {
            if (weight.isNotEmpty() && height.isNotEmpty() && headCircumference.isNotEmpty()) {
                val milestoneData = mapOf(
                    "weight" to weight,
                    "height" to height,
                    "headCircumference" to headCircumference,
                    "timestamp" to System.currentTimeMillis()
                )
                viewModel.saveGrowthMilestone(
                    milestoneData = milestoneData,
                    onSuccess = {
                        showAlert = true // Muestra la alerta al guardar
                    },
                    onError = { errorMessage ->
                        Log.e("GrowthMilestone", "Error al guardar: $errorMessage")
                    }
                )
            } else {
                viewModel.clearErrorMessage()
            }
        }) {
            Text(text = "Guardar")
        }
    }

    // Mostrar alerta al guardar con éxito
    if (showAlert) {
        AlertDialog(
            onDismissRequest = { showAlert = false },
            title = { Text(text = "Confirmación") },
            text = { Text(text = "Datos registrados") },
            confirmButton = {
                Button(onClick = {
                    showAlert = false
                    navController.popBackStack() // Navega hacia atrás
                }) {
                    Text(text = "Aceptar")
                }
            }
        )
    }
}