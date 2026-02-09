package com.ys.phdmama.ui.screens.born.charts

import android.annotation.SuppressLint
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavHostController
import com.ys.phdmama.R
import com.ys.phdmama.model.LMS
import com.ys.phdmama.model.LengthRange
import com.ys.phdmama.services.GraphicChartRenderer
import com.ys.phdmama.services.GraphicWeightChartRenderer
import com.ys.phdmama.ui.components.PhdLayoutMenu
import com.ys.phdmama.util.LmsUtils
import com.ys.phdmama.viewmodel.BabyDataViewModel
import com.ys.phdmama.viewmodel.GrowthMilestonesViewModel
import com.ys.phdmama.viewmodel.GrowthRecord
import kotlin.math.exp
import kotlin.math.ln
import kotlin.math.pow

@Composable
fun WeightDetailScreen(
    navController: NavHostController,
    growthMilestonesViewModel: GrowthMilestonesViewModel = hiltViewModel(),
    babyDataViewModel: BabyDataViewModel = hiltViewModel(),
    openDrawer: () -> Unit,
    babyId: String?
) {
    val records = growthMilestonesViewModel.growthRecords.value
    val context = LocalContext.current
    val selectedBabyProfile by babyDataViewModel.selectedBaby.collectAsState()

    val babySex = remember(selectedBabyProfile) {
        when (selectedBabyProfile?.sex) {
            "Masculino" -> "boy"
            "Femenino" -> "girl"
            else -> ""
        }
    }

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
        title = "Reporte de Peso",
        navController = navController,
        openDrawer = openDrawer
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(20.dp)
        ) {
            Spacer(Modifier.height(16.dp))

            if (babySex.isNotEmpty()) {
                WeightChart( // TODO : cambiar por WeightChart
                    records = records,
                    sex = babySex,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(300.dp)
                        .padding(16.dp)
                )

                if (records.isNotEmpty()) {
                    Button(
                        onClick = {
                            generateHeightLengthDF(
                                context = context,
                                records = records,
                                babyId = babyId ?: "unknown"
                            )
                        },
                        colors = ButtonDefaults.buttonColors(
                            containerColor = MaterialTheme.colorScheme.primary
                        ),
                        modifier = Modifier.padding(start = 8.dp)
                    ) {
                        Spacer(modifier = Modifier.padding(4.dp))
                        Text(
                            text = "Descargar",
                            color = Color.White,
                            fontSize = 12.sp
                        )
                    }

                    val lmsTable = if (babySex.lowercase() == "girl") {
                        LmsUtils.lmsWeightGirlsData
                    } else {
                        LmsUtils.lmsWeightBoysData
                    }

                    LazyColumn {
                        items(records) { record ->
                            val zScore = calcularZScoreTallaEdad(
                                talla = record.height,
                                edadMeses = record.ageInMonths,
                                lmsList = lmsTable
                            )


                            val rango = calcularRangoNormalTalla(record.ageInMonths, lmsTable)

                            Card(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(vertical = 4.dp),
                                elevation = CardDefaults.cardElevation(2.dp)
                            ) {
                                Column(modifier = Modifier.padding(12.dp)) {
                                    Text("Mes: ${record.ageInMonths}")
                                    Text("Peso: ${record.weight} kg")

                                    rango?.let {
                                        Text("Rango OMS: ${it.min} kg - ${it.max} kg")
                                    }
                                }
                            }
                        }
                    }

                } else {
                    Text("No hay datos disponibles.")
                }
            } else {
                CircularProgressIndicator()
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

@SuppressLint("RememberReturnType")
@Composable
fun WeightChart(
    records: List<GrowthRecord>,
    sex: String,
    modifier: Modifier = Modifier
) {
    val chartRenderer = remember { GraphicWeightChartRenderer() }

    Canvas(modifier = modifier) {
        chartRenderer.drawChart(
            drawScope = this,
            records = records,
            sex = sex,
            size = size
        )
    }
}

fun monthsToWeek(months: Int): Int {
    return kotlin.math.round(months * 4.345).toInt()
}


fun calcularZScoreTallaEdad(
    talla: Double?, // valor medido en cm
    edadMeses: Int,
    lmsList: List<LMS>
): Double? {
    if (talla == null) return null

    val weeks = monthsToWeek(edadMeses)
    val lms = lmsList.find { it.week == weeks } ?: return null
    val (L, M, S) = Triple(lms.L, lms.M, lms.S)

    return if (L == 0.0) {
        ln(talla / M) / S
    } else {
        (talla.pow(L) - M.pow(L)) / (L * S * M.pow(L - 1))
    }
}

//data class LengthRange(val min: Double, val max: Double)

fun calcularRangoNormalTalla(
    edadMeses: Int,
    lmsList: List<LMS>
): LengthRange? {
    val weeks = monthsToWeek(edadMeses)
    val lms = lmsList.find { it.week == weeks } ?: return null
    val (L, M, S) = Triple(lms.L, lms.M, lms.S)
    val z = 2.0

    val min = if (L != 0.0) M * (1 + L * S * -z).pow(1 / L) else M * exp(-z * S)
    val max = if (L != 0.0) M * (1 + L * S * z).pow(1 / L) else M * exp(z * S)

    return LengthRange(
        min = String.format("%.2f", min).toDouble(),
        max = String.format("%.2f", max).toDouble()
    )
}

