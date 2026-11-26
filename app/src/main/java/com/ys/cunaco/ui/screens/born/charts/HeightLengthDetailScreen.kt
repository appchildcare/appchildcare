package com.ys.cunaco.ui.screens.born.charts

import android.annotation.SuppressLint
import android.content.Context
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.LazyColumn
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
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavHostController
import com.ys.cunaco.R
import com.ys.cunaco.ui.components.PhdLayoutMenu
import com.ys.cunaco.util.LmsUtils
import com.ys.cunaco.viewmodel.BabyDataViewModel
import com.ys.cunaco.viewmodel.GrowthMilestonesViewModel
import androidx.compose.ui.platform.LocalContext
import androidx.hilt.navigation.compose.hiltViewModel
import com.ys.cunaco.model.LMSHeightLength
import com.ys.cunaco.model.LengthRange
import com.ys.cunaco.services.GraphicChartRenderer
import com.ys.cunaco.services.PdfGeneratorUtils
import com.ys.cunaco.viewmodel.GrowthRecord
import java.util.Locale
import kotlin.math.exp
import kotlin.math.ln
import kotlin.math.pow

@Composable
fun HeightLengthDetailScreen(
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

    val babyName = remember(selectedBabyProfile) {
        selectedBabyProfile?.name
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
        title = stringResource(id = R.string.height_report_title),
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
                if (records.isNotEmpty()) {
                    HeightLengthChart(
                        records = records,
                        sex = babySex,
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(300.dp)
                            .padding(16.dp)
                    )

                    Spacer(Modifier.height(16.dp))

                    Button(
                        onClick = {
                            generateHeightLengthPDF(
                                context = context,
                                records = records,
                                babyName = babyName ?: context.getString(R.string.unknown_baby),
                                sex = babySex
                            )
                        },
                        colors = ButtonDefaults.buttonColors(
                            containerColor = MaterialTheme.colorScheme.primary
                        ),
                        modifier = Modifier.padding(start = 8.dp)
                    ) {
                        Spacer(modifier = Modifier.padding(4.dp))
                        Text(
                            text = stringResource(id = R.string.download),
                            color = Color.White,
                            fontSize = 12.sp
                        )
                    }

                    val lmsTable = if (babySex.lowercase() == "girl") {
                        LmsUtils.lmdGirlsHeightLengthData
                    } else {
                        LmsUtils.lmsBoysHeightLengthData
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
                                    Text(text = stringResource(id = R.string.month_detail_label, record.ageInMonths))
                                    Text(text = stringResource(id = R.string.height_detail_label, String.format(Locale.getDefault(), "%.1f", record.height)))

                                    rango?.let {
                                        Text(text = stringResource(id = R.string.oms_range_label, String.format(Locale.getDefault(), "%.2f", it.min), String.format(Locale.getDefault(), "%.2f", it.max)))
                                    }
                                }
                            }
                        }
                    }
                } else {
                    Text(text = stringResource(id = R.string.no_data_available))
                }
            } else {
                CircularProgressIndicator()
            }

            Spacer(modifier = Modifier.height(8.dp))
        }
    }
}

@SuppressLint("RememberReturnType")
@Composable
fun HeightLengthChart(
    records: List<GrowthRecord>,
    sex: String,
    modifier: Modifier = Modifier
) {
    val chartRenderer = remember { GraphicChartRenderer() }

    Canvas(modifier = modifier) {
        chartRenderer.drawChart(
            drawScope = this,
            records = records,
            sex = sex,
            size = size
        )
    }
}

fun generateHeightLengthPDF(
    context: Context,
    records: List<GrowthRecord>,
    babyName: String,
    sex: String
) {
    val lmsTable = if (sex.lowercase() == "girl") {
        LmsUtils.lmdGirlsHeightLengthData
    } else {
        LmsUtils.lmsBoysHeightLengthData
    }

    PdfGeneratorUtils.generateAndSharePdf(
        context = context,
        fileName = context.getString(R.string.pdf_file_name_height),
        title = context.getString(R.string.pdf_report_title_height),
        subtitle = context.getString(R.string.pdf_report_baby_name, babyName),
        logoResId = R.drawable.app_child_care_logo
    ) {
        val headers = listOf(
            context.getString(R.string.table_header_age),
            context.getString(R.string.table_header_height),
            context.getString(R.string.table_header_zscore),
            context.getString(R.string.table_header_oms_range)
        )
        val columnPositions = floatArrayOf(60f, 135f, 230f, 330f)

        val tableData = records.map { record ->
            val zScore = calcularZScoreTallaEdad(
                talla = record.height,
                edadMeses = record.ageInMonths,
                lmsList = lmsTable
            )

            val rango = calcularRangoNormalTalla(record.ageInMonths, lmsTable)
            val rangoText = rango?.let {
                "${String.format(Locale.getDefault(), "%.1f", it.min)}-${String.format(Locale.getDefault(), "%.1f", it.max)}"
            } ?: "N/A"

            listOf(
                "${record.ageInMonths} m",
                "${record.height}",
                zScore?.let { String.format(Locale.getDefault(), "%.2f", it) } ?: "N/A",
                rangoText
            )
        }

        drawTable(headers, columnPositions, tableData)
        addSectionTitle(context.getString(R.string.summary_title))

        val totalMeasurements = records.size
        val lastRecord = records.lastOrNull()
        val lastHeight = lastRecord?.height?.let { String.format(Locale.getDefault(), "%.1f", it) } ?: "N/A"
        val lastAge = lastRecord?.ageInMonths ?: 0

        val normalCount = records.count { record ->
            val zScore = calcularZScoreTallaEdad(
                talla = record.height,
                edadMeses = record.ageInMonths,
                lmsList = lmsTable
            )
            zScore?.let { it >= -2 && it <= 2 } ?: false
        }

        addText(context.getString(R.string.summary_total_measurements, totalMeasurements), indent = 20f)
        addText(context.getString(R.string.summary_last_height_measurement, lastHeight, lastAge), indent = 20f)
        addText(context.getString(R.string.summary_normal_range_count, normalCount, totalMeasurements), indent = 20f)
    }
}

fun calcularZScoreTallaEdad(
    talla: Double?, 
    edadMeses: Int,
    lmsList: List<LMSHeightLength>
): Double? {
    if (talla == null) return null
    val lms = lmsList.find { it.month == edadMeses } ?: return null
    val (L, M, S) = Triple(lms.L, lms.M, lms.S)
    return if (L == 0.0) ln(talla / M) / S else (talla.pow(L) - M.pow(L)) / (L * S * M.pow(L - 1))
}

fun calcularRangoNormalTalla(
    edadMeses: Int,
    lmsList: List<LMSHeightLength>
): LengthRange? {
    val lms = lmsList.find { it.month == edadMeses } ?: return null
    val (L, M, S) = Triple(lms.L, lms.M, lms.S)
    val z = 2.0
    val min = if (L != 0.0) M * (1 + L * S * -z).pow(1 / L) else M * exp(-z * S)
    val max = if (L != 0.0) M * (1 + L * S * z).pow(1 / L) else M * exp(z * S)
    return LengthRange(min = String.format(Locale.getDefault(), "%.2f", min).toDouble(), max = String.format(Locale.getDefault(), "%.2f", max).toDouble())
}
