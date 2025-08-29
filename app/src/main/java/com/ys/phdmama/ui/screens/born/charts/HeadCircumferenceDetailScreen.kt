package com.ys.phdmama.ui.screens.born.charts

import android.content.ContentValues
import android.content.Context
import android.provider.MediaStore
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Paint
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.nativeCanvas
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavHostController
import com.ys.phdmama.R
import com.ys.phdmama.model.LMSHeadCircumference
import com.ys.phdmama.ui.components.PhdBoldText
import com.ys.phdmama.ui.components.PhdLayoutMenu
import com.ys.phdmama.util.LmsUtils
import com.ys.phdmama.viewmodel.BabyDataViewModel
import com.ys.phdmama.viewmodel.GrowthMilestonesViewModel
import com.ys.phdmama.viewmodel.UserDataViewModel
import kotlin.math.ln
import kotlin.math.pow
import kotlin.math.exp

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
    val context = LocalContext.current

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
                        babyId = babyId ?: "unknown"
                    )
                },
                colors = ButtonDefaults.buttonColors(
                    containerColor = MaterialTheme.colorScheme.primary
                ),
                modifier = Modifier.padding(start = 8.dp)
            ) {
//                Icon(
//                    imageVector = Icons.Default.Favorite,
//                    contentDescription = "Descargar",
//                    tint = Color.White
//                )
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
                            calcularZScorePerimetroCefalico(
                                headCircumference = record.headCircumference,
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

//                                diagnostico?.let {
//                                    Text("Diagnóstico perímetro cefálico (OMS): $it")
//                                }
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

@Composable
fun HeadCircumferenceChart(
    records: List<Any>, // Replace with your actual record type
    modifier: Modifier = Modifier
) {
    // Reference data for girls (first 14 weeks)
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
        val chartWidth = size.width - 100f
        val chartHeight = size.height - 100f
        val chartStartX = 50f
        val chartStartY = 50f

        // Chart bounds
        val maxWeeks = 13f
        val minHeadCircumference = 30f
        val maxHeadCircumference = 44f

        // Draw background
        drawRect(
            color = Color(0xFFF5F5F5),
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

        // Draw median line (P50)
        drawPercentileLine(
            data = referenceData,
            chartStartX = chartStartX,
            chartStartY = chartStartY,
            chartWidth = chartWidth,
            chartHeight = chartHeight,
            maxWeeks = maxWeeks,
            minHeadCircumference = minHeadCircumference,
            maxHeadCircumference = maxHeadCircumference,
            color = Color(0xFF4CAF50), // Green
            getValueFromLMS = { it.M }
        )

        // Draw P3 and P97 lines (calculated from L, M, S)
        drawPercentileLine(
            data = referenceData,
            chartStartX = chartStartX,
            chartStartY = chartStartY,
            chartWidth = chartWidth,
            chartHeight = chartHeight,
            maxWeeks = maxWeeks,
            minHeadCircumference = minHeadCircumference,
            maxHeadCircumference = maxHeadCircumference,
            color = Color(0xFFFF9800), // Orange
            getValueFromLMS = { calculatePercentileFromLMS(it.L, it.M, it.S, -1.88) } // P3 ≈ Z-score -1.88
        )

        drawPercentileLine(
            data = referenceData,
            chartStartX = chartStartX,
            chartStartY = chartStartY,
            chartWidth = chartWidth,
            chartHeight = chartHeight,
            maxWeeks = maxWeeks,
            minHeadCircumference = minHeadCircumference,
            maxHeadCircumference = maxHeadCircumference,
            color = Color(0xFFFF9800), // Orange
            getValueFromLMS = { calculatePercentileFromLMS(it.L, it.M, it.S, 1.88) } // P97 ≈ Z-score 1.88
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
    val gridColor = Color(0xFFE0E0E0)

    // Vertical grid lines (age)
    for (week in 0..maxWeeks.toInt()) {
        val x = chartStartX + (week / maxWeeks) * chartWidth
        drawLine(
            color = gridColor,
            start = Offset(x, chartStartY),
            end = Offset(x, chartStartY + chartHeight),
            strokeWidth = 1f
        )
    }

    // Horizontal grid lines (head circumference)
    val stepSize = 2f // 2cm intervals
    val steps = ((maxHeadCircumference - minHeadCircumference) / stepSize).toInt()
    for (step in 0..steps) {
        val value = minHeadCircumference + step * stepSize
        val y = chartStartY + chartHeight - ((value - minHeadCircumference) / (maxHeadCircumference - minHeadCircumference)) * chartHeight
        drawLine(
            color = gridColor,
            start = Offset(chartStartX, y),
            end = Offset(chartStartX + chartWidth, y),
            strokeWidth = 1f
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
        style = androidx.compose.ui.graphics.drawscope.Stroke(width = 3f)
    )
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
    for (week in 0..maxWeeks.toInt() step 2) {
        val x = chartStartX + (week / maxWeeks) * chartWidth
        drawContext.canvas.nativeCanvas.drawText(
            "${week}w",
            x - 15f,
            chartStartY + chartHeight + 30f,
            textPaint
        )
    }

    // Y-axis labels (head circumference)
    val stepSize = 2f
    val steps = ((maxHeadCircumference - minHeadCircumference) / stepSize).toInt()
    for (step in 0..steps step 2) {
        val value = minHeadCircumference + step * stepSize
        val y = chartStartY + chartHeight - ((value - minHeadCircumference) / (maxHeadCircumference - minHeadCircumference)) * chartHeight
        drawContext.canvas.nativeCanvas.drawText(
            "${value.toInt()}",
            chartStartX - 40f,
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
        "Edad (semanas)",
        chartStartX + chartWidth/2 - 70f,
        chartStartY + chartHeight + 70f,
        titlePaint
    )

    // Y-axis title (rotated)
    drawContext.canvas.nativeCanvas.save()
    drawContext.canvas.nativeCanvas.rotate(-90f, 20f, chartStartY + chartHeight/2)
    drawContext.canvas.nativeCanvas.drawText(
        "Perímetro cefálico (cm)",
        -100f,
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
    context: android.content.Context,
    records: List<Any>, // Replace with your actual record type
    babyId: String
) {
    try {
        // Create PDF document
        val pdfDocument = android.graphics.pdf.PdfDocument()
        val pageInfo = android.graphics.pdf.PdfDocument.PageInfo.Builder(595, 842, 1).create() // A4 size
        val page = pdfDocument.startPage(pageInfo)
        val canvas = page.canvas
        val paint = android.graphics.Paint()

        // Title
        paint.textSize = 24f
        paint.typeface = android.graphics.Typeface.DEFAULT_BOLD
        paint.color = android.graphics.Color.BLACK
        canvas.drawText("Reporte de Perímetro Cefálico", 50f, 80f, paint)

        // Baby ID
        paint.textSize = 16f
        paint.typeface = android.graphics.Typeface.DEFAULT
        canvas.drawText("ID del Bebé: $babyId", 50f, 120f, paint)

        // Date
        val currentDate = java.text.SimpleDateFormat("dd/MM/yyyy", java.util.Locale.getDefault()).format(java.util.Date())
        canvas.drawText("Fecha: $currentDate", 50f, 150f, paint)

        // Chart area placeholder
        paint.style = android.graphics.Paint.Style.STROKE
        paint.strokeWidth = 2f
        paint.color = android.graphics.Color.GRAY
        canvas.drawRect(50f, 180f, 545f, 400f, paint)

        paint.style = android.graphics.Paint.Style.FILL
        paint.textSize = 14f
        canvas.drawText("Gráfico de Perímetro Cefálico vs Edad", 200f, 300f, paint)

        // Data table
        var yPosition = 450f
        paint.textSize = 16f
        paint.typeface = android.graphics.Typeface.DEFAULT_BOLD
        canvas.drawText("Datos Registrados:", 50f, yPosition, paint)

        yPosition += 30f
        paint.textSize = 12f
        paint.typeface = android.graphics.Typeface.DEFAULT

        // Table headers
        canvas.drawText("Mes", 50f, yPosition, paint)
        canvas.drawText("Peso (kg)", 150f, yPosition, paint)
        canvas.drawText("Talla (cm)", 250f, yPosition, paint)
        canvas.drawText("P. Cefálico (cm)", 350f, yPosition, paint)
        canvas.drawText("Diagnóstico", 470f, yPosition, paint)

        yPosition += 5f
        paint.strokeWidth = 1f
        paint.style = android.graphics.Paint.Style.STROKE
        canvas.drawLine(50f, yPosition, 545f, yPosition, paint)
        paint.style = android.graphics.Paint.Style.FILL

        // Data rows (you'll need to adapt this to your actual record structure)
        val lmsTable = LmsUtils.lmsHeadCircumference
        records.take(15).forEach { record -> // Limit to prevent overflow
            yPosition += 20f

            // You'll need to adapt these property accesses to your actual record type
            /*
            canvas.drawText("${record.ageInMonths}", 50f, yPosition, paint)
            canvas.drawText("${record.weight}", 150f, yPosition, paint)
            canvas.drawText("${record.height}", 250f, yPosition, paint)
            canvas.drawText("${record.headCircumference}", 350f, yPosition, paint)

            val zScore = record.headCircumference?.let {
                calcularZScorePerimetroCefalico(
                    headCircumference = it,
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
            } ?: "N/A"

            canvas.drawText(diagnostico, 470f, yPosition, paint)
            */

            // Placeholder data - replace with actual record data
            canvas.drawText("N/A", 50f, yPosition, paint)
            canvas.drawText("N/A", 150f, yPosition, paint)
            canvas.drawText("N/A", 250f, yPosition, paint)
            canvas.drawText("N/A", 350f, yPosition, paint)
            canvas.drawText("N/A", 470f, yPosition, paint)
        }

        // Footer
        yPosition = 750f
        paint.textSize = 10f
        paint.color = android.graphics.Color.GRAY
        canvas.drawText("Generado por PhD Mama App", 50f, yPosition, paint)
        canvas.drawText("Página 1 de 1", 450f, yPosition, paint)

        pdfDocument.finishPage(page)

        // Modern approach: Save to MediaStore (Android 10+)
        val fileName = "reporte_perimetro_cefalico_${babyId}_${System.currentTimeMillis()}.pdf"
        val mimeType = "application/pdf"

        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.Q) {
            // Use MediaStore for Android 10+
            val contentValues = ContentValues().apply {
                put(MediaStore.MediaColumns.DISPLAY_NAME, fileName)
                put(MediaStore.MediaColumns.MIME_TYPE, mimeType)
                put(MediaStore.MediaColumns.RELATIVE_PATH, "Download/") // Save to Download folder
            }

            val resolver = context.contentResolver
            val uri = resolver.insert(MediaStore.Downloads.EXTERNAL_CONTENT_URI, contentValues)

            uri?.let { pdfUri ->
                resolver.openOutputStream(pdfUri)?.use { outputStream ->
                    pdfDocument.writeTo(outputStream)
                    pdfDocument.close()
                    android.widget.Toast.makeText(
                        context,
                        "PDF guardado en Descargas: $fileName",
                        android.widget.Toast.LENGTH_LONG
                    ).show()
                }
            } ?: run {
                pdfDocument.close()
                android.widget.Toast.makeText(
                    context,
                    "Error al crear el archivo PDF",
                    android.widget.Toast.LENGTH_LONG
                ).show()
            }
        } else {
            // Fallback for older Android versions (API < 29)
            val downloadsDir = android.os.Environment.getExternalStoragePublicDirectory(
                android.os.Environment.DIRECTORY_DOWNLOADS
            )
            val file = java.io.File(downloadsDir, fileName)

            try {
                val fileOutputStream = java.io.FileOutputStream(file)
                pdfDocument.writeTo(fileOutputStream)
                pdfDocument.close()
                fileOutputStream.close()

                android.widget.Toast.makeText(
                    context,
                    "PDF guardado en Descargas: $fileName",
                    android.widget.Toast.LENGTH_LONG
                ).show()
            } catch (e: Exception) {
                pdfDocument.close()
                android.widget.Toast.makeText(
                    context,
                    "Error al guardar PDF: ${e.message}",
                    android.widget.Toast.LENGTH_LONG
                ).show()
            }
        }

    } catch (e: Exception) {
        android.widget.Toast.makeText(
            context,
            "Error al generar PDF: ${e.message}",
            android.widget.Toast.LENGTH_LONG
        ).show()
        e.printStackTrace()
    }
}
