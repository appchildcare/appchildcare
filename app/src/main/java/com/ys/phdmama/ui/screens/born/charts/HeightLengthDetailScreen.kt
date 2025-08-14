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
import com.ys.phdmama.model.LMSHeightWeight
import com.ys.phdmama.ui.components.PhdLayoutMenu
import com.ys.phdmama.util.LmsJsonUtil
import com.ys.phdmama.viewmodel.BabyDataViewModel
import com.ys.phdmama.viewmodel.GrowthMilestonesViewModel
import com.ys.phdmama.viewmodel.UserDataViewModel
import kotlin.math.exp
import kotlin.math.ln
import kotlin.math.pow
@Composable
fun HeightLengthDetailScreen(
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
        title = "Reporte de Longitud/altura",
        navController = navController,
        openDrawer = openDrawer
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(20.dp)
        ) {
            Spacer(Modifier.height(16.dp))

            if (records.isNotEmpty()) {
                val sexo = "girl" // O usa el valor real desde el ViewModel si está disponible
                val lmsTable = LmsJsonUtil.lmsDataHeightWeightGirls

                LazyColumn {
                    items(records) { record ->
                        val zScore = calcularZScoreTallaEdad(
                            talla = record.height,
                            edadMeses = record.ageInMonths,
                            lmsList = lmsTable
                        )

                        val diagnostico = zScore?.let {
                            when {
                                it < -3 -> "Talla muy baja para la edad (desnutrición severa)"
                                it < -2 -> "Talla baja para la edad"
                                it <= 2 -> "Talla normal"
                                else -> "Talla alta para la edad"
                            }
                        }

                        val rango = calcularRangoNormalTalla(record.ageInMonths, lmsTable)

                        Card(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 4.dp),
                            elevation = CardDefaults.cardElevation(2.dp)
                        ) {
                            Column(modifier = Modifier.padding(12.dp)) {
                                Text("Mes: ${record.ageInMonths}")
                                Text("Talla: ${record.height} cm")

                                diagnostico?.let {
                                    Text("Diagnóstico OMS Talla para la Edad: $it")
                                }

                                rango?.let {
                                    Text("Rango OMS: ${it.min} cm - ${it.max} cm")
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



fun calcularZScoreTallaEdad(
    talla: Double?, // valor medido en cm
    edadMeses: Int,
    lmsList: List<LMSHeightWeight>
): Double? {
    if (talla == null) return null

    val lms = lmsList.find { it.week == edadMeses } ?: return null
    val (L, M, S) = Triple(lms.L, lms.M, lms.S)

    return if (L == 0.0) {
        ln(talla / M) / S
    } else {
        (talla.pow(L) - M.pow(L)) / (L * S * M.pow(L - 1))
    }
}

data class LengthRange(val min: Double, val max: Double)

fun calcularRangoNormalTalla(
    edadMeses: Int,
    lmsList: List<LMSHeightWeight>
): LengthRange? {
    val lms = lmsList.find { it.week == edadMeses } ?: return null
    val (L, M, S) = Triple(lms.L, lms.M, lms.S)
    val z = 2.0

    val min = if (L != 0.0) M * (1 + L * S * -z).pow(1 / L) else M * exp(-z * S)
    val max = if (L != 0.0) M * (1 + L * S * z).pow(1 / L) else M * exp(z * S)

    return LengthRange(
        min = String.format("%.2f", min).toDouble(),
        max = String.format("%.2f", max).toDouble()
    )
}
