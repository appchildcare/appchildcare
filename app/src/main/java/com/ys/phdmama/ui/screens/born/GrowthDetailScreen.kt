package com.ys.phdmama.ui.screens.born

import android.annotation.SuppressLint
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavHostController
import com.ys.phdmama.viewmodel.BabyDataViewModel
import com.ys.phdmama.viewmodel.GrowthMilestonesViewModel
import com.ys.phdmama.viewmodel.UserDataViewModel

@Composable
fun GrowthDetailScreen(navController: NavHostController,
                       growthMilestonesViewModel: GrowthMilestonesViewModel = viewModel(),
                       userViewModel: UserDataViewModel = viewModel(),
                       dashboardViewModel: BabyDataViewModel = viewModel(),
                       babyDataViewModel: BabyDataViewModel = viewModel(),
                       openDrawer: () -> Unit,
                       babyId: String?) {
    val records = growthMilestonesViewModel.growthRecords.value
    LaunchedEffect(babyId) {
        growthMilestonesViewModel.fetchBabyId(
            onSuccess = { baby ->
                if (!baby.isNullOrEmpty()) {
                    growthMilestonesViewModel.loadGrowthData(baby.first())
                }
            },
            onSkip = {
               // testId = ""
            },
            onError = {
                //testId = ""
            }
        )

    }



    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(20.dp)
    ) {
        Text("Reporte de Crecimiento", style = MaterialTheme.typography.titleLarge)
        Spacer(Modifier.height(16.dp))

        if (records.isNotEmpty()) {
            LazyColumn {
                items(records) { record ->
                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 4.dp),
                        elevation = CardDefaults.cardElevation(2.dp)
                    ) {
                        Column(modifier = Modifier.padding(12.dp)) {
                            Text("Mes: ${record.ageInMonths}")
                            Text("Peso: ${record.weight} kg")
                            Text("Talla: ${record.height} cm")
                            Text("Perímetro cefálico: ${record.headCircumference} cm")
                            Text("IMC: ${calcularIMC(record.weight, record.height)}")
                        }
                    }
                }
            }
        } else {
            Text("No hay datos disponibles.")
        }
    }
}


@SuppressLint("DefaultLocale")
fun calcularIMC(weight: Double, height: Double): Double {
    val tallaMetros = height / 100.0
    if (tallaMetros <= 0) return 0.0
    val imc = weight / (tallaMetros * tallaMetros)
    return String.format("%.2f", imc).toDouble()
}
