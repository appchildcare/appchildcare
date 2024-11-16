package com.ys.phdmama.ui.screens.born

import androidx.compose.foundation.layout.*
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

@Composable
fun GrowthMilestonesScreen(
    navController: NavHostController,
    viewModel: BabyDataViewModel = viewModel()
) {
    // Variables para almacenar el peso, la talla y el perímetro cefálico ingresados
    var weight by remember { mutableStateOf("") }
    var height by remember { mutableStateOf("") }
    var headCircumference by remember { mutableStateOf("") }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(text = "Registrar Hitos del Crecimiento", style = MaterialTheme.typography.titleLarge)

        Spacer(modifier = Modifier.height(16.dp))

        // Campo de texto para ingresar el peso
        OutlinedTextField(
            value = weight,
            onValueChange = { weight = it },
            label = { Text("Peso (kg)") },
            modifier = Modifier.fillMaxWidth()
        )

        Spacer(modifier = Modifier.height(16.dp))

        // Campo de texto para ingresar la talla
        OutlinedTextField(
            value = height,
            onValueChange = { height = it },
            label = { Text("Talla (cm)") },
            modifier = Modifier.fillMaxWidth()
        )

        Spacer(modifier = Modifier.height(16.dp))

        // Campo de texto para ingresar el perímetro cefálico
        OutlinedTextField(
            value = headCircumference,
            onValueChange = { headCircumference = it },
            label = { Text("Perímetro Cefálico (cm)") },
            modifier = Modifier.fillMaxWidth()
        )

        Spacer(modifier = Modifier.height(16.dp))

        // Botón para guardar los valores
        Button(onClick = {
            viewModel.setBabyAttribute("weight", weight)
            viewModel.setBabyAttribute("height", height)
            viewModel.setBabyAttribute("headCircumference", headCircumference)
            navController.navigate("next_screen_route") // Ajusta la navegación según tu flujo
        }) {
            Text(text = "Guardar")
        }
    }
}
