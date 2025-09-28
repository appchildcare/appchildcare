package com.ys.phdmama.ui.screens.carbonfootprint

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.ui.unit.sp
import androidx.compose.ui.unit.dp
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.navigation.NavController
import com.ys.phdmama.ui.components.PhdLayoutMenu
import com.ys.phdmama.ui.components.PhdSubtitle
import com.ys.phdmama.ui.theme.primaryYellow
import com.ys.phdmama.viewmodel.CarbonFootprintViewModel

@Composable
fun CarbonFootprintScreen(viewModel: CarbonFootprintViewModel, navController: NavController,
                          openDrawer: () -> Unit) {
    PhdLayoutMenu(
        title = "Huella de carbono",
        navController = navController,
        openDrawer = openDrawer
    ) {
        DataEntryScreen(viewModel)
    }
}

@Composable
fun DataEntryScreen(viewModel: CarbonFootprintViewModel) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
            .verticalScroll(rememberScrollState()),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
//        Text(
//            "Datos diarios del cuidado del bebé",
//            fontSize = 24.sp,
//            fontWeight = FontWeight.Bold,
//            color = Color(0xFF2E7D32)
//        )
        PhdSubtitle("Datos diarios del cuidado del bebé")

        // Input Fields
        InputField(
            label = "Pañales desechables usados",
            value = viewModel.disposableDiapers,
            onValueChange = { viewModel.disposableDiapers = it },
            icon = "🍼"
        )

        InputField(
            label = "Pañales de tela usados",
            value = viewModel.clothDiapers,
            onValueChange = { viewModel.clothDiapers = it },
            icon = "🌿"
        )

        InputField(
            label = "Toallitas húmedas usadas",
            value = viewModel.wetWipes,
            onValueChange = { viewModel.wetWipes = it },
            icon = "🧻"
        )

        InputField(
            label = "Alimentación con fórmula",
            value = viewModel.formulaFeedings,
            onValueChange = { viewModel.formulaFeedings = it },
            icon = "🍼"
        )

        InputField(
            label = "Lavados de botellas",
            value = viewModel.bottleWashes,
            onValueChange = { viewModel.bottleWashes = it },
            icon = "🧽"
        )

        InputField(
            label = "Baños Dados",
            value = viewModel.baths,
            onValueChange = { viewModel.baths = it },
            icon = "🛁"
        )

        if (viewModel.successMessage.isNotEmpty()) {
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(containerColor = Color(0xFFE8F5E8))
            ) {
                Text(
                    text = viewModel.successMessage,
                    modifier = Modifier.padding(16.dp),
                    color = Color(0xFF2E7D32)
                )
            }
        }

        // Calculate Button
        Button(
            onClick = { viewModel.calculateFootprint() },
            modifier = Modifier
                .fillMaxWidth()
                .height(56.dp),
            enabled = !viewModel.isLoading,
            colors = ButtonDefaults.buttonColors(
                containerColor = primaryYellow
            )
        ) {
            if (viewModel.isLoading) {
                CircularProgressIndicator(
                    modifier = Modifier
                        .width(20.dp)
                        .height(20.dp),
                    color = Color.White
                )
            } else {
                Text("Calcular Huella de carbono", fontSize = 16.sp)
            }
        }

        // Clear Button
        Button(
            onClick = { viewModel.clearForm() },
            modifier = Modifier
                .fillMaxWidth()
                .height(56.dp),
            colors = ButtonDefaults.buttonColors(
                containerColor = Color.Gray
            )
        ) {
            Text("Limpiar formulario", fontSize = 16.sp, color = Color.White)
        }

        // Messages
        if (viewModel.errorMessage.isNotEmpty()) {
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(containerColor = Color(0xFFFFEBEE))
            ) {
                Text(
                    text = viewModel.errorMessage,
                    modifier = Modifier.padding(16.dp),
                    color = Color(0xFFD32F2F)
                )
            }
        }
    }
}

@Composable
fun InputField(
    label: String,
    value: String,
    onValueChange: (String) -> Unit,
    icon: String
) {
    OutlinedTextField(
        value = value,
        onValueChange = onValueChange,
        label = { Text("$icon $label") },
        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
        modifier = Modifier.fillMaxWidth(),
        singleLine = true
    )
}
