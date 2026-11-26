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
import androidx.compose.ui.res.stringResource
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
import java.util.Locale
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
        title = stringResource(id = R.string.head_circumference_report_title),
        navController = navController,
        openDrawer = openDrawer
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(20.dp)
        ) {
            PhdBoldText(stringResource(id = R.string.head_circumference_for_age))

            Button(
                onClick = {
                    generateHeadCircumferencePDF(
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

            Spacer(Modifier.height(16.dp))

            if (records.isNotEmpty() && babySex.isNotEmpty()) {
                // The chart is now rendered with the correct sex from Firebase
                HeadCircumferenceChart(
                    records = records,
                    sex = babySex,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(300.dp)
                        .padding(16.dp)
                )

                Spacer(Modifier.height(16.dp))

                val lmsTable = LmsUtils.lmsHeadCircumference

                LazyColumn {
                    items(records) { record ->
                        val zScore = record.headCircumference.let { medida ->
                            selectedBabyProfile?.let { it1 ->
                                calcularZScorePerimetroCefalico(
                                    headCircumference = record.headCircumference,
                                    edadMeses = record.ageInMonths,
                                    sexo = it1.sex,
                                    lmsList = lmsTable,
                                )
                            }
                        }

                        Card(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 4.dp),
                            elevation = CardDefaults.cardElevation(2.dp)
                        ) {
                            Column(modifier = Modifier.padding(12.dp)) {
                                Text(text = stringResource(id = R.string.month_detail_label, record.ageInMonths))
                                Text(text = stringResource(id = R.string.weight_detail_label, String.format(Locale.getDefault(), "%.1f", record.weight)))
                                Text(text = stringResource(id = R.string.height_detail_label, String.format(Locale.getDefault(), "%.1f", record.height)))
                                Text(text = stringResource(id = R.string.head_circumference_detail_label, String.format(Locale.getDefault(), "%.1f", record.headCircumference)))
                            }
                        }
                    }
                }
            } else if (records.isEmpty()) {
                Text(text = stringResource(id = R.string.no_data_available))
            }

            Spacer(modifier = Modifier.height(8.dp))
        }
    }
}

@Composable
fun HeadCircumferenceChart(
    records: List<GrowthRecord>,
    sex: String,
    modifier: Modifier = Modifier
) {
    // Reference data filtered by sex - WHO standards from LmsUtils
    val referenceData = remember(sex) {
        LmsUtils.lmsHeadCircumference.filter { it.sex.equals(sex, ignoreCase = true) }
    }

    if (referenceData.isEmpty()) return

    val birthLabel = stringResource(id = R.string.birth)
    val ageLabel = stringResource(id = R.string.age_months_label)
    val hcLabel = stringResource(id = R.string.head_circumference_cm_axis_label)

    Canvas(modifier = modifier) {
        val chartWidth = size.width - 120f
        val chartHeight = size.height - 120f
        val chartStartX = 80f
        val chartStartY = 40f

        // Chart bounds - matching WHO chart
        val maxMonths = 13f
        val minHeadCircumference = 30f
        val maxHeadCircumference = 44f

        // Draw background with pink tint like WHO chart (could be blue for boys if preferred)
        drawRect(
            color = if (sex == "boy") Color(0xFFE3F2FD) else Color(0xFFFCF0FC),
            topLeft = Offset(chartStartX, chartStartY),
            size = androidx.compose.ui.geometry.Size(chartWidth, chartHeight)
        )

        // Draw grid lines
        drawGrid(
            chartStartX = chartStartX,
            chartStartY = chartStartY,
            chartWidth = chartWidth,
            chartHeight = chartHeight,
            maxWeeks = maxMonths,
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
            maxWeeks = maxMonths,
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
            maxWeeks = maxMonths,
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
            maxWeeks = maxMonths,
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
            maxWeeks = maxMonths,
            minHeadCircumference = minHeadCircumference,
            maxHeadCircumference = maxHeadCircumference
        )

        // Draw axes
        drawAxes(
            chartStartX = chartStartX,
            chartStartY = chartStartY,
            chartWidth = chartWidth,
            chartHeight = chartHeight,
            maxWeeks = maxMonths,
            minHeadCircumference = minHeadCircumference,
            maxHeadCircumference = maxHeadCircumference,
            birthLabel = birthLabel,
            ageLabel = ageLabel,
            hcLabel = hcLabel
        )

        // Plot patient data points from Firebase
        plotPatientData(
            records = records,
            chartStartX = chartStartX,
            chartStartY = chartStartY,
            chartWidth = chartWidth,
            chartHeight = chartHeight,
            maxWeeks = maxMonths,
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
    records: List<GrowthRecord>,
    chartStartX: Float,
    chartStartY: Float,
    chartWidth: Float,
    chartHeight: Float,
    maxWeeks: Float,
    minHeadCircumference: Float,
    maxHeadCircumference: Float
) {
    records.forEach { record ->
        val x = chartStartX + (record.ageInMonths.toFloat() / maxWeeks) * chartWidth
        val y = chartStartY + chartHeight - ((record.headCircumference.toFloat() - minHeadCircumference) / (maxHeadCircumference - minHeadCircumference)) * chartHeight

        // Only draw if within chart bounds
        if (x in chartStartX..(chartStartX + chartWidth) && y in chartStartY..(chartStartY + chartHeight)) {
            drawCircle(
                color = Color(0xFF2196F3), // Material Blue
                radius = 8f,
                center = Offset(x, y)
            )
            
            // Draw a small outline for better visibility
            drawCircle(
                color = Color.White,
                radius = 4f,
                center = Offset(x, y)
            )
        }
    }
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
    maxHeadCircumference: Float,
    birthLabel: String,
    ageLabel: String,
    hcLabel: String
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

    // X-axis labels (weeks/months)
    for (week in 0..maxWeeks.toInt()) {
        val x = chartStartX + (week / maxWeeks) * chartWidth
        val label = if (week == 0) birthLabel else "$week"
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
        ageLabel,
        chartStartX + chartWidth/2 - 60f,
        chartStartY + chartHeight + 70f,
        titlePaint
    )

    // Y-axis title (rotated)
    drawContext.canvas.nativeCanvas.save()
    drawContext.canvas.nativeCanvas.rotate(-90f, 25f, chartStartY + chartHeight/2)
    drawContext.canvas.nativeCanvas.drawText(
        hcLabel,
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
    // Note: fixing reference to missing functions by using local ones or placeholder logic
    val lmsTable = LmsUtils.lmsHeadCircumference

    PdfGeneratorUtils.generateAndSharePdf(
        context = context,
        fileName = context.getString(R.string.pdf_file_name_head_circumference),
        title = context.getString(R.string.head_circumference_report_title),
        subtitle = context.getString(R.string.pdf_report_baby_name, babyName),
        logoResId = R.drawable.app_child_care_logo
    ) {
        // Draw table
        val headers = listOf(
            context.getString(R.string.table_header_age),
            context.getString(R.string.table_header_weight),
            context.getString(R.string.table_header_height),
            context.getString(R.string.table_header_head_circumference)
        )
        val columnPositions = floatArrayOf(60f, 135f, 230f, 330f)

        val tableData = records.map { record ->
            val pc = record.headCircumference
            val age = record.ageInMonths
            
            val zScore = calcularZScorePerimetroCefalico(pc, age, sex, lmsTable)
            val rango = calcularRangoNormalPerimetroCefalico(age, sex, lmsTable)
            
            listOf(
                "$age m",
                "${record.weight}",
                "${record.height}",
                "${record.headCircumference}"
            )
        }

        drawTable(headers, columnPositions, tableData)

        // Add summary section
        addSectionTitle(context.getString(R.string.summary_title))

        val totalMeasurements = records.size
        val lastRecord = records.lastOrNull()
        val lastPC = lastRecord?.headCircumference?.let { String.format(Locale.getDefault(), "%.1f", it) } ?: "N/A"
        val lastAge = lastRecord?.ageInMonths ?: 0

        val normalCount = records.count { record ->
            val zScore = calcularZScorePerimetroCefalico(record.headCircumference, record.ageInMonths, sex, lmsTable)
            zScore?.let { it >= -2 && it <= 2 } ?: false
        }

        addText(context.getString(R.string.summary_total_measurements, totalMeasurements), indent = 20f)
        addText(context.getString(R.string.summary_last_pc_measurement, lastPC, lastAge), indent = 20f)
        addText(context.getString(R.string.summary_normal_range_count, normalCount, totalMeasurements), indent = 20f)
    }
}
