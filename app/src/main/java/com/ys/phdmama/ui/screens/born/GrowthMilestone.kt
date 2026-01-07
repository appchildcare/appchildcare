package com.ys.phdmama.ui.screens.born

import android.util.Log
import android.widget.NumberPicker
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavHostController
import com.ys.phdmama.ui.components.PhdLayoutMenu
import com.ys.phdmama.viewmodel.GrowthMilestonesViewModel

@Composable
fun GrowthMilestonesScreen(
    navController: NavHostController,
    viewModel: GrowthMilestonesViewModel = hiltViewModel(),
    openDrawer: () -> Unit,
    babyId: String?
) {
    // Variables para los campos de texto
    var weight by remember { mutableStateOf("") }
    var height by remember { mutableStateOf("") }
    var headCircumference by remember { mutableStateOf("") }

    // Estado para controlar la visibilidad de la alerta
    var showAlert by remember { mutableStateOf(false) }

    var selectedNumber by remember { mutableStateOf(1) }
    if (babyId != null) {
        viewModel.setBabyId(babyId)
    }

    PhdLayoutMenu(
        title = "Hitos del crecimiento",
        navController = navController,
        openDrawer = openDrawer
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(16.dp)
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

            LabeledAndroidNumberPicker(
                label = "Seleccionar edad del bebé en meses:",
                value = selectedNumber,
                range = 1..60,
                onValueChange = { selectedNumber = it }
            )

            Spacer(modifier = Modifier.height(16.dp))

            Button(onClick = {
                if (weight.isNotEmpty() && height.isNotEmpty() && headCircumference.isNotEmpty()) {
                    val milestoneData = mapOf(
                        "weight" to weight,
                        "height" to height,
                        "headCircumference" to headCircumference,
                        "timestamp" to System.currentTimeMillis(),
                        "ageInMonths" to selectedNumber,
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
}

@Composable
fun LabeledAndroidNumberPicker(
    label: String,
    value: Int,
    range: IntRange,
    onValueChange: (Int) -> Unit
) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = Modifier.padding(16.dp)
    ) {
        Text(
            text = label,
            style = MaterialTheme.typography.bodyLarge,
            modifier = Modifier.padding(bottom = 10.dp)
        )

        AndroidView(
            modifier = Modifier.wrapContentSize(),
            factory = { context ->
                NumberPicker(context).apply {
                    minValue = range.first
                    maxValue = range.last
                    setOnValueChangedListener { _, _, newVal ->
                        onValueChange(newVal)
                    }
                }
            },
            update = { picker ->
                picker.value = value
            }
        )
    }
}
