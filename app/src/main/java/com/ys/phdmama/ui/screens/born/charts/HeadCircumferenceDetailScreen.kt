package com.ys.phdmama.ui.screens.born.charts

import androidx.compose.foundation.Image
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
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavHostController
import com.ys.phdmama.R
import com.ys.phdmama.model.LMSHeadCircumference
import com.ys.phdmama.ui.components.PhdBoldText
import com.ys.phdmama.ui.components.PhdLayoutMenu
import com.ys.phdmama.util.LmsJsonUtil
import com.ys.phdmama.viewmodel.BabyDataViewModel
import com.ys.phdmama.viewmodel.GrowthMilestonesViewModel
import com.ys.phdmama.viewmodel.UserDataViewModel
import kotlin.math.ln
import kotlin.math.pow
@Composable
fun HeadCircumferenceDetailScreen(
    navController: NavHostController,
    growthMilestonesViewModel: GrowthMilestonesViewModel = viewModel(),
    userViewModel: UserDataViewModel = viewModel(),
    dashboardViewModel: BabyDataViewModel = viewModel(),
    babyDataViewModel: BabyDataViewModel = viewModel(),
    openDrawer: () -> Unit,
    babyId: String?
) {
    val records = growthMilestonesViewModel.growthRecords.value

    LaunchedEffect(babyId) {
        growthMilestonesViewModel.fetchBabyId(
            onSuccess = { baby ->
                if (!baby.isNullOrEmpty()) {
                    growthMilestonesViewModel.loadGrowthData(baby.first())
                }
            },
            onSkip = {},
            onError = {}
        )
    }

    PhdLayoutMenu(
        title = "Reporte de Perímetro Cefálico",
        navController = navController,
        openDrawer = openDrawer
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(20.dp)
        ) {
            PhdBoldText("Perímetro Cefálico para la Edad")
            Spacer(Modifier.height(16.dp))

            if (records.isNotEmpty()) {
                val sexo = "niño" // O usa el valor real desde el ViewModel si está disponible
                val lmsTable = LmsJsonUtil.lmsDataGirls

                LazyColumn {
                    items(records) { record ->
                        val zScore = record.headCircumference?.let { medida ->
                            calcularZScorePerimetroCefalico(
                                headCircumference  = record.headCircumference,
                                edadMeses = record.ageInMonths,
                                sexo = "girl",
                                lmsList = lmsTable,
                            )
                        }

                        val diagnostico = zScore?.let {
                            when {
                                it < -2 -> "Microcefalia"
                                it <= 2 -> "Normal"
                                else -> "Macrocefalia"
                            }
                        }

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
                              //  Text("IMC: ${calcularIMC(record.weight, record.height)}")

                                diagnostico?.let {
                                    Text("Diagnóstico perímetro cefálico (OMS): $it")
                                }
                                val rango = calcularRangoNormalPerimetroCefalico(record.ageInMonths, "girl", lmsTable)
                                rango?.let {
                                    Text("Rango normal: ${it.min} cm - ${it.max} cm")
                                }
                            }
                        }
                    }
                }
            } else {
                Text("No hay datos disponibles.")
            }

            Spacer(modifier = Modifier.height(8.dp))
            Image(
                painter = painterResource(id = R.mipmap.ilustraciones_baby),
                contentDescription = "Auth image",
                modifier = Modifier
                    .fillMaxWidth()
                    .height(180.dp)
            )
        }
    }
}

fun calcularZScorePerimetroCefalico(
    headCircumference: Double?, // valor medido en cm
    edadMeses: Int,
    sexo: String,
    lmsList: List<LMSHeadCircumference>,
): Double? {
    if (headCircumference == null) return null

    val lms = lmsList.find { it.week == edadMeses && it.sex.lowercase() == sexo.lowercase() }
    return lms?.let {
        val (L, M, S) = Triple(it.L, it.M, it.S)
        if (L == 0.0) {
            ln(headCircumference / M) / S
        } else {
            (headCircumference.pow(L) - M.pow(L)) / (L * S * M.pow(L - 1))
        }
    }
}

data class HeadCircumferenceRange(
    val min: Double,
    val max: Double
)

fun calcularRangoNormalPerimetroCefalico(
    edadMeses: Int,
    sexo: String,
    lmsList: List<LMSHeadCircumference>
): HeadCircumferenceRange? {
    val lms = lmsList.find { it.week == edadMeses && it.sex.equals(sexo, ignoreCase = true) } ?: return null
    val (L, M, S) = Triple(lms.L, lms.M, lms.S)
    val z = 2.0 // ±2 Z-scores (percentil 2.3 a 97.7)

    val pcMin = if (L != 0.0) M * Math.pow(1 + L * S * -z, 1 / L) else M * Math.exp(-z * S)
    val pcMax = if (L != 0.0) M * Math.pow(1 + L * S * z, 1 / L) else M * Math.exp(z * S)

    return HeadCircumferenceRange(
        min = String.format("%.2f", pcMin).toDouble(),
        max = String.format("%.2f", pcMax).toDouble()
    )
}

