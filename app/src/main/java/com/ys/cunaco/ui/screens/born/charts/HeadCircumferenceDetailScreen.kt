package com.ys.cunaco.ui.screens.born.charts

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
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Paint
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.PathEffect
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.nativeCanvas
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavHostController
import com.ys.cunaco.R
import com.ys.cunaco.model.LMSHeadCircumference
import com.ys.cunaco.services.PdfGeneratorUtils
import com.ys.cunaco.ui.components.PhdBoldText
import com.ys.cunaco.ui.components.PhdLayoutMenu
import com.ys.cunaco.util.LmsUtils
import com.ys.cunaco.viewmodel.BabyDataViewModel
import com.ys.cunaco.viewmodel.GrowthMilestonesViewModel
import com.ys.cunaco.viewmodel.GrowthRecord
import com.ys.cunaco.viewmodel.UserDataViewModel
import kotlin.math.ln
import kotlin.math.pow
import kotlin.math.exp

@Composable
fun HeadCircumferenceDetailScreen(
    navController: NavHostController,
    growthMilestonesViewModel: GrowthMilestonesViewModel = hiltViewModel(),
    userViewModel: UserDataViewModel = hiltViewModel(),
    dashboardViewModel: BabyDataViewModel = hiltViewModel(),
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
        return@remember selectedBabyProfile?.name
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

            Button(
                onClick = {
                    generateHeadCircumferencePDF(
                        context = context,
                        records = records,
                        babyName = babyName ?: "unknown",
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
                    text = "Descargar",
                    color = Color.White,
                    fontSize = 12.sp
                )
            }

            Spacer(Modifier.height(16.dp))

            // Add the chart here
            HeadCircumferenceChart(
                records = records,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(300.dp)
                    .padding(16.dp)
            )

            Spacer(Modifier.height(16.dp))

            if (records.isNotEmpty()) {
                val sexo = "girl" // O usa el valor real desde el ViewModel si está disponible
                val lmsTable = LmsUtils.lmsHeadCircumference

                LazyColumn {
                    items(records) { record ->
                        val zScore = record.headCircumference?.let { medida ->
                            selectedBabyProfile?.let { it1 ->
                                calcularZScorePerimetroCefalico(
                                    headCircumference = record.headCircumference,
                                    edadMeses = record.ageInMonths,
                                    sexo = it1.sex,
                                    lmsList = lmsTable,
                                )
                            }
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
                            }
                        }
                    }
                }
            } else {
                Text("No hay datos disponibles.")
            }

            Spacer(modifier = Modifier.height(8.dp))
            //Image(
              //  painter = painterResource(id = R.mipmap.ilustraciones_baby),
               // contentDescription = "Auth image",
                // modifier = Modifier
                   // .fillMaxWidth()
                   // .height(180.dp)
            //)
        }
    }
}

@Composable
fun HeadCircumferenceChart(
    records: List<Any>, // Replace with your actual record type
    modifier: Modifier = Modifier
) {
    // Reference data for girls (first 14 weeks) - WHO standards
    val referenceData = listOf(
        LMSHeadCircumference(0, "girl", 1.0, 33.8787, 0.03496, 1.1844, 30.3, 31.5, 32.7, 33.9, 35.1, 36.2, 37.4),
        LMSHeadCircumference(1, "girl", 1.0, 34.5529, 0.03374, 1.1658, 31.1, 32.2, 33.4, 34.6, 35.7, 36.9, 38.1),
        LMSHeadCircumference(2, "girl", 1.0, 35.2272, 0.03251, 1.1452, 31.8, 32.9, 34.1, 35.2, 36.4, 37.5, 38.7),
        LMSHeadCircumference(3, "girl", 1.0, 35.8430, 0.03231, 1.1581, 32.4, 33.5, 34.7, 35.8, 37.0, 38.2, 39.3),
        LMSHeadCircumference(4, "girl", 1.0, 36.3761, 0.03215, 1.1695, 32.9, 34.0, 35.2, 36.4, 37.5, 38.7, 39.9),
        LMSHeadCircumference(5, "girl", 1.0, 36.8472, 0.03202, 1.1799, 33.3, 34.5, 35.7, 36.8, 38.0, 39.2, 40.4),
        LMSHeadCircumference(6, "girl", 1.0, 37.2711, 0.03191, 1.1893, 33.7, 34.9, 36.1, 37.3, 38.5, 39.6, 40.8),
        LMSHeadCircumference(7, "girl", 1.0, 37.6584, 0.03182, 1.1983, 34.1, 35.3, 36.5, 37.7, 38.9, 40.1, 41.3),
        LMSHeadCircumference(8, "girl", 1.0, 38.0167, 0.03173, 1.2063, 34.4, 35.6, 36.8, 38.0, 39.2, 40.4, 41.6),
        LMSHeadCircumference(9, "girl", 1.0, 38.3516, 0.03166, 1.2142, 34.7, 35.9, 37.1, 38.4, 39.6, 40.8, 42.0),
        LMSHeadCircumference(10, "girl", 1.0, 38.6673, 0.03158, 1.2211, 35.0, 36.2, 37.4, 38.7, 39.9, 41.1, 42.3),
        LMSHeadCircumference(11, "girl", 1.0, 38.9661, 0.03152, 1.2282, 35.3, 36.5, 37.7, 39.0, 40.2, 41.4, 42.7),
        LMSHeadCircumference(12, "girl", 1.0, 39.2501, 0.03146, 1.2348, 35.5, 36.8, 38.0, 39.3, 40.5, 41.7, 43.0),
        LMSHeadCircumference(13, "girl", 1.0, 39.5210, 0.03140, 1.2410, 35.8, 37.0, 38.3, 39.5, 40.8, 42.0, 43.2)
    )

    Canvas(modifier = modifier) {
        val chartWidth = size.width - 120f
        val chartHeight = size.height - 120f
        val chartStartX = 80f
        val chartStartY = 40f

        // Chart bounds - matching WHO chart
        val maxWeeks = 13f
        val minHeadCircumference = 30f
        val maxHeadCircumference = 44f

        // Draw background with pink tint like WHO chart
        drawRect(
            color = Color(0xFFFCF0FC),
            topLeft = Offset(chartStartX, chartStartY),
            size = androidx.compose.ui.geometry.Size(chartWidth, chartHeight)
        )

        // Draw grid lines
        drawGrid(
            chartStartX = chartStartX,
            chartStartY = chartStartY,
            chartWidth = chartWidth,
            chartHeight = chartHeight,
            maxWeeks = maxWeeks,
            minHeadCircumference = minHeadCircumference,
            maxHeadCircumference = maxHeadCircumference
        )

        // Draw WHO standard deviation lines
        // +3 SD (top black line)
        drawPercentileLine(
            data = referenceData,
            chartStartX = chartStartX,
            chartStartY = chartStartY,
            chartWidth = chartWidth,
            chartHeight = chartHeight,
            maxWeeks = maxWeeks,
            minHeadCircumference = minHeadCircumference,
            maxHeadCircumference = maxHeadCircumference,
            color = Color.Black,
            strokeWidth = 3f,
            getValueFromLMS = { calculatePercentileFromLMS(it.L, it.M, it.S, 3.0) }
        )

        // Median (0 SD) - green line
        drawPercentileLine(
            data = referenceData,
            chartStartX = chartStartX,
            chartStartY = chartStartY,
            chartWidth = chartWidth,
            chartHeight = chartHeight,
            maxWeeks = maxWeeks,
            minHeadCircumference = minHeadCircumference,
            maxHeadCircumference = maxHeadCircumference,
            color = Color(0xFF4CAF50),
            strokeWidth = 3f,
            getValueFromLMS = { it.M }
        )

        // -3 SD (bottom black line)
        drawPercentileLine(
            data = referenceData,
            chartStartX = chartStartX,
            chartStartY = chartStartY,
            chartWidth = chartWidth,
            chartHeight = chartHeight,
            maxWeeks = maxWeeks,
            minHeadCircumference = minHeadCircumference,
            maxHeadCircumference = maxHeadCircumference,
            color = Color.Black,
            strokeWidth = 3f,
            getValueFromLMS = { calculatePercentileFromLMS(it.L, it.M, it.S, -3.0) }
        )

        // Draw Z-score labels on the right side
        drawZScoreLabels(
            chartStartX = chartStartX,
            chartStartY = chartStartY,
            chartWidth = chartWidth,
            chartHeight = chartHeight,
            referenceData = referenceData,
            maxWeeks = maxWeeks,
            minHeadCircumference = minHeadCircumference,
            maxHeadCircumference = maxHeadCircumference
        )

        // Draw axes
        drawAxes(
            chartStartX = chartStartX,
            chartStartY = chartStartY,
            chartWidth = chartWidth,
            chartHeight = chartHeight,
            maxWeeks = maxWeeks,
            minHeadCircumference = minHeadCircumference,
            maxHeadCircumference = maxHeadCircumference
        )

        // Plot patient data points if available
        plotPatientData(
            records = records,
            chartStartX = chartStartX,
            chartStartY = chartStartY,
            chartWidth = chartWidth,
            chartHeight = chartHeight,
            maxWeeks = maxWeeks,
            minHeadCircumference = minHeadCircumference,
            maxHeadCircumference = maxHeadCircumference
        )
    }
}

private fun DrawScope.drawPercentileLine(
    data: List<LMSHeadCircumference>,
    chartStartX: Float,
    chartStartY: Float,
    chartWidth: Float,
    chartHeight: Float,
    maxWeeks: Float,
    minHeadCircumference: Float,
    maxHeadCircumference: Float,
    color: Color,
    strokeWidth: Float = 2f,
    getValueFromLMS: (LMSHeadCircumference) -> Double
) {
    val path = Path()
    var isFirstPoint = true

    data.forEach { lms ->
        val x = chartStartX + (lms.week / maxWeeks) * chartWidth
        val value = getValueFromLMS(lms).toFloat()
        val y = chartStartY + chartHeight - ((value - minHeadCircumference) / (maxHeadCircumference - minHeadCircumference)) * chartHeight

        if (isFirstPoint) {
            path.moveTo(x, y)
            isFirstPoint = false
        } else {
            path.lineTo(x, y)
        }
    }

    drawPath(
        path = path,
        color = color,
        style = androidx.compose.ui.graphics.drawscope.Stroke(width = strokeWidth)
    )
}

private fun DrawScope.drawZScoreLabels(
    chartStartX: Float,
    chartStartY: Float,
    chartWidth: Float,
    chartHeight: Float,
    referenceData: List<LMSHeadCircumference>,
    maxWeeks: Float,
    minHeadCircumference: Float,
    maxHeadCircumference: Float
) {
    val textPaint = Paint().asFrameworkPaint().apply {
        isAntiAlias = true
        textSize = 32f
        color = android.graphics.Color.BLACK
        typeface = android.graphics.Typeface.DEFAULT_BOLD
    }

    // Get the last data point (week 13) to position labels
    val lastLMS = referenceData.last()
    val labelX = chartStartX + chartWidth + 10f

    // Z-score values and their colors
    val zScores = listOf(3.0, 2.0, 0.0, -2.0, -3.0)
    val colors = listOf(
        android.graphics.Color.BLACK,
        android.graphics.Color.RED,
        android.graphics.Color.parseColor("#4CAF50"),
        android.graphics.Color.RED,
        android.graphics.Color.BLACK
    )

    zScores.forEachIndexed { index, zScore ->
        val value = calculatePercentileFromLMS(lastLMS.L, lastLMS.M, lastLMS.S, zScore).toFloat()
        val y = chartStartY + chartHeight - ((value - minHeadCircumference) / (maxHeadCircumference - minHeadCircumference)) * chartHeight

        textPaint.color = colors[index]
        drawContext.canvas.nativeCanvas.drawText(
            if (zScore > 0) "+${zScore.toInt()}" else zScore.toInt().toString(),
            labelX,
            y + 8f,
            textPaint
        )
    }
}

private fun DrawScope.plotPatientData(
    records: List<Any>,
    chartStartX: Float,
    chartStartY: Float,
    chartWidth: Float,
    chartHeight: Float,
    maxWeeks: Float,
    minHeadCircumference: Float,
    maxHeadCircumference: Float
) {
    // This function would plot actual patient data points
    // You'll need to cast records to your actual record type and extract the data
    /*
    records.forEach { record ->
        val x = chartStartX + (record.ageInWeeks / maxWeeks) * chartWidth
        val y = chartStartY + chartHeight - ((record.headCircumference - minHeadCircumference) / (maxHeadCircumference - minHeadCircumference)) * chartHeight

        drawCircle(
            color = Color.Blue,
            radius = 8f,
            center = Offset(x, y)
        )
    }
    */
}

private fun DrawScope.drawGrid(
    chartStartX: Float,
    chartStartY: Float,
    chartWidth: Float,
    chartHeight: Float,
    maxWeeks: Float,
    minHeadCircumference: Float,
    maxHeadCircumference: Float
) {

    val gridColorLight = Color(0x18B0B0B0)
    val gridColorMid   = Color(0x30A0A0A0)

    for (week in 0..maxWeeks.toInt()) {
        if (week % 2 != 0) continue
        val x = chartStartX + (week / maxWeeks) * chartWidth
        val isMajor = week % 4 == 0
        drawLine(
            color = if (isMajor) gridColorMid else gridColorLight,
            start = Offset(x, chartStartY + 8f),
            end   = Offset(x, chartStartY + chartHeight - 8f),
            strokeWidth = if (isMajor) 1.2f else 0.8f,
            pathEffect = if (isMajor) null
            else PathEffect.dashPathEffect(floatArrayOf(6f, 6f), 0f)
        )
    }

    val stepSize = 2f
    val steps = ((maxHeadCircumference - minHeadCircumference) / stepSize).toInt()
    for (step in 0..steps) {
        val value = minHeadCircumference + step * stepSize
        val y = chartStartY + chartHeight -
                ((value - minHeadCircumference) /
                        (maxHeadCircumference - minHeadCircumference)) * chartHeight
        val isMajor = step % 2 == 0
        drawLine(
            color = if (isMajor) gridColorMid else gridColorLight,
            start = Offset(chartStartX + 8f, y),
            end   = Offset(chartStartX + chartWidth - 8f, y),
            strokeWidth = if (isMajor) 1.2f else 0.8f,
            pathEffect = PathEffect.dashPathEffect(floatArrayOf(8f, 8f), 0f)
        )
    }
}

private fun DrawScope.drawAxes(
    chartStartX: Float,
    chartStartY: Float,
    chartWidth: Float,
    chartHeight: Float,
    maxWeeks: Float,
    minHeadCircumference: Float,
    maxHeadCircumference: Float
) {
    val axisColor = Color.Black
    val textPaint = Paint().asFrameworkPaint().apply {
        isAntiAlias = true
        textSize = 24f
        color = android.graphics.Color.BLACK
    }

    // X-axis
    drawLine(
        color = axisColor,
        start = Offset(chartStartX, chartStartY + chartHeight),
        end = Offset(chartStartX + chartWidth, chartStartY + chartHeight),
        strokeWidth = 2f
    )

    // Y-axis
    drawLine(
        color = axisColor,
        start = Offset(chartStartX, chartStartY),
        end = Offset(chartStartX, chartStartY + chartHeight),
        strokeWidth = 2f
    )

    // X-axis labels (weeks)
    for (week in 0..maxWeeks.toInt()) {
        val x = chartStartX + (week / maxWeeks) * chartWidth
        val label = if (week == 0) "Birth" else "${week}"
        drawContext.canvas.nativeCanvas.drawText(
            label,
            x - 15f,
            chartStartY + chartHeight + 30f,
            textPaint
        )
    }

    // Y-axis labels (head circumference) - every 2cm
    val stepSize = 2f
    val steps = ((maxHeadCircumference - minHeadCircumference) / stepSize).toInt()
    for (step in 0..steps) {
        val value = minHeadCircumference + step * stepSize
        val y = chartStartY + chartHeight - ((value - minHeadCircumference) / (maxHeadCircumference - minHeadCircumference)) * chartHeight
        drawContext.canvas.nativeCanvas.drawText(
            "${value.toInt()}",
            chartStartX - 50f,
            y + 8f,
            textPaint
        )
    }

    // Axis titles
    val titlePaint = Paint().asFrameworkPaint().apply {
        isAntiAlias = true
        textSize = 28f
        color = android.graphics.Color.BLACK
        isFakeBoldText = true
    }

    // X-axis title
    drawContext.canvas.nativeCanvas.drawText(
        "Edad (meses)",
        chartStartX + chartWidth/2 - 60f,
        chartStartY + chartHeight + 70f,
        titlePaint
    )

    // Y-axis title (rotated)
    drawContext.canvas.nativeCanvas.save()
    drawContext.canvas.nativeCanvas.rotate(-90f, 25f, chartStartY + chartHeight/2)
    drawContext.canvas.nativeCanvas.drawText(
        "Perímetro cefálico (cm)",
        -120f,
        chartStartY + chartHeight/2,
        titlePaint
    )
    drawContext.canvas.nativeCanvas.restore()
}

// Function to calculate percentile value from L, M, S parameters
fun calculatePercentileFromLMS(L: Double, M: Double, S: Double, zScore: Double): Double {
    return if (L == 0.0) {
        // When L = 0, use the log-normal distribution formula
        M * exp(zScore * S)
    } else {
        // When L ≠ 0, use the Box-Cox transformation formula
        M * (1 + L * S * zScore).pow(1 / L)
    }
}

// Your existing functions remain the same
fun calcularZScorePerimetroCefalico(
    headCircumference: Double?,
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
    val z = 2.0

    val pcMin = if (L != 0.0) M * Math.pow(1 + L * S * -z, 1 / L) else M * Math.exp(-z * S)
    val pcMax = if (L != 0.0) M * Math.pow(1 + L * S * z, 1 / L) else M * Math.exp(z * S)

    return HeadCircumferenceRange(
        min = String.format("%.2f", pcMin).toDouble(),
        max = String.format("%.2f", pcMax).toDouble()
    )
}

fun generateHeadCircumferencePDF(
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
        fileName = "Reporte_Perimetro_Cefalico",
        title = "Reporte de Perimetro Cefálico",
        subtitle = "Nombre del Bebé: $babyName",
        logoResId = R.drawable.app_child_care_logo
    ) {
        // Draw table
        val headers = listOf("Edad", "Peso (kg)", "Talla (cm)", "P. Cefálico")
        val columnPositions = floatArrayOf(60f, 135f, 230f, 330f)

        val tableData = records.map { record ->
            val zScore = calcularZScoreTallaEdad(
                talla = record.height,
                edadMeses = record.ageInMonths,
                lmsList = lmsTable
            )

            val rango = calcularRangoNormalTalla(record.ageInMonths, lmsTable)
            val rangoText = rango?.let {
                "${String.format("%.1f", it.min)}-${String.format("%.1f", it.max)}"
            } ?: "N/A"

            listOf(
                "${record.ageInMonths} m",
                "${record.height ?: "N/A"}",
                zScore?.let { String.format("%.2f", it) } ?: "N/A",
                rangoText
            )
        }

        drawTable(headers, columnPositions, tableData)

        // Add summary section
        addSectionTitle("Resumen:")

        val totalMeasurements = records.size
        val lastRecord = records.lastOrNull()
        val lastHeight = lastRecord?.height?.let { String.format("%.1f", it) } ?: "N/A"
        val lastAge = lastRecord?.ageInMonths ?: 0

        val normalCount = records.count { record ->
            val zScore = calcularZScoreTallaEdad(
                talla = record.height,
                edadMeses = record.ageInMonths,
                lmsList = lmsTable
            )
            zScore?.let { it >= -2 && it <= 2 } ?: false
        }

        addText("• Total de mediciones: $totalMeasurements", indent = 20f)
        addText("• Última medición: $lastHeight cm ($lastAge meses)", indent = 20f)
        addText("• Mediciones en rango normal: $normalCount de $totalMeasurements", indent = 20f)
    }
}

