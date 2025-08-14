package com.ys.phdmama.ui.screens.born

import android.annotation.SuppressLint
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavHostController
import com.ys.phdmama.R
import com.ys.phdmama.model.LMS
import com.ys.phdmama.ui.components.PhdBoldText
import com.ys.phdmama.ui.components.PhdLayoutMenu
import com.ys.phdmama.ui.components.custom.GrowthChartCard
import com.ys.phdmama.util.LmsJsonUtil
import com.ys.phdmama.viewmodel.BabyDataViewModel
import com.ys.phdmama.viewmodel.GrowthMilestonesViewModel
import com.ys.phdmama.viewmodel.UserDataViewModel
import kotlinx.serialization.json.Json

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

    PhdLayoutMenu(
        title = "Reporte de Crecimiento",
        navController = navController,
        openDrawer = openDrawer
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(20.dp)
        ) {
            PhdBoldText("Índice de masa corporal para la edad")
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
                                Text("IMC según edad bebé (OMS): ${calculateIMC()}")
                            }
                        }
                    }
                }
            } else {
                Text("No hay datos disponibles.")
            }

            Spacer(modifier = Modifier.height(8.dp))
            Image(
                painter = painterResource(id = R.mipmap.crecimiento),
                contentDescription = "Auth image",
                modifier = Modifier
                    .fillMaxWidth()
                    .height(180.dp)
            )
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

fun calculateIMC(): String {
    val peso = 5.0
    val talla = 60.0
    val edadMeses = 1
    val sexo = "niño"

    val imc = calcularIMC(peso, talla) // e.g. 13.89

    val lmsTable: List<LMS> = cargarLmsDesdeJson(LmsJsonUtil.jsonString)

    val zScore = calcularZScoreIMC(imc, edadMeses, sexo, lmsTable)

    println("IMC: %.2f, Z‑score: %.2f".format(imc, zScore ?: Double.NaN))

    return zScore?.let {
        when {
            it < -2 -> "Bajo peso"
            it <= 1 -> "Normal"
            it <= 2 -> "Sobrepeso"
            else -> "Obesidad"
        }
    } ?: "Sin datos suficientes"
}

fun cargarLmsDesdeJson(json: String): List<LMS> {
    return Json.decodeFromString(json)
}

fun calcularZScoreIMC(imc: Double?, edadMeses: Int, sexo: String, lmsList: List<LMS>): Double? {
    if (imc == null) return null
    val lms = lmsList.find {
        it.week == edadMeses
    }
    return lms?.let {
        val (L, M, S) = Triple(it.L, it.M, it.S)
        if (L == 0.0) {
            kotlin.math.ln(imc / M!!) / S!!
        } else {
            ((Math.pow(imc / M!!, L!!) - 1) / (L * S!!))
        }
    }
}
