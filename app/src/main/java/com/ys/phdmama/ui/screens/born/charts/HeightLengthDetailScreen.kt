package com.ys.phdmama.ui.screens.born.charts

import android.content.ContentValues
import android.provider.MediaStore
import androidx.compose.foundation.Image
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
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavHostController
import com.ys.phdmama.R
import com.ys.phdmama.model.LMSHeightWeight
import com.ys.phdmama.ui.components.PhdLayoutMenu
import com.ys.phdmama.util.LmsUtils
import com.ys.phdmama.viewmodel.BabyDataViewModel
import com.ys.phdmama.viewmodel.GrowthMilestonesViewModel
import com.ys.phdmama.viewmodel.UserDataViewModel
import androidx.compose.ui.platform.LocalContext
import androidx.hilt.navigation.compose.hiltViewModel
import com.ys.phdmama.viewmodel.GrowthRecord
import kotlin.math.exp
import kotlin.math.ln
import kotlin.math.pow

@Composable
fun HeightLengthDetailScreen(
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
        title = "Reporte de Longuitud/peso",
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

                val sexo = "girl" // O usa el valor real desde el ViewModel si está disponible
                val lmsTable = LmsUtils.lmsDataHeightWeightGirls

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
                                Text("Talla: ${record.height} cm")

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

fun generateHeightLengthDF(
    context: android.content.Context,
    records: List<GrowthRecord>, // Replace with your actual record type
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
        canvas.drawText("Reporte de Longitud/Altura", 50f, 80f, paint)

        // Baby ID
        paint.textSize = 16f
        paint.typeface = android.graphics.Typeface.DEFAULT
        canvas.drawText("ID del Bebé: $babyId", 50f, 120f, paint)

        // Date
        val currentDate = java.text.SimpleDateFormat("dd/MM/yyyy", java.util.Locale.getDefault()).format(java.util.Date())
        canvas.drawText("Fecha: $currentDate", 50f, 150f, paint)

        // Table title
        paint.textSize = 18f
        paint.typeface = android.graphics.Typeface.DEFAULT_BOLD
        canvas.drawText("Tabla de Mediciones", 50f, 200f, paint)

        // Table setup
        var yPosition = 240f
        paint.textSize = 12f
        paint.typeface = android.graphics.Typeface.DEFAULT_BOLD

        // Draw table border
        paint.style = android.graphics.Paint.Style.STROKE
        paint.strokeWidth = 2f
        paint.color = android.graphics.Color.BLACK
        val tableLeft = 50f
        val tableRight = 545f
        val tableTop = yPosition - 15f

        // Calculate table height based on number of records
        val rowHeight = 30f
        val headerHeight = 30f
        val maxRows = kotlin.math.min(records.size, 18) // Limit rows to fit page
        val tableBottom = tableTop + headerHeight + (maxRows * rowHeight)

        canvas.drawRect(tableLeft, tableTop, tableRight, tableBottom, paint)

        // Column positions (adjusted for height/length report)
        val colPositions = floatArrayOf(60f, 120f, 200f, 300f) // Edad, Talla, Z-Score, Rango OMS

        // Draw column separators
        for (i in 1 until colPositions.size) {
            canvas.drawLine(colPositions[i], tableTop, colPositions[i], tableBottom, paint)
        }

        // Table headers
        paint.style = android.graphics.Paint.Style.FILL
        paint.color = android.graphics.Color.BLACK
        paint.textSize = 11f
        paint.typeface = android.graphics.Typeface.DEFAULT_BOLD

        canvas.drawText("Edad", colPositions[0], yPosition, paint)
        canvas.drawText("Talla (cm)", colPositions[1], yPosition, paint)
        canvas.drawText("Z-Score", colPositions[2], yPosition, paint)
        canvas.drawText("Rango OMS (cm)", colPositions[3], yPosition, paint)

        // Draw header separator line
        yPosition += 15f
        paint.style = android.graphics.Paint.Style.STROKE
        paint.strokeWidth = 1f
        canvas.drawLine(tableLeft, yPosition, tableRight, yPosition, paint)

        yPosition += 15f
        paint.style = android.graphics.Paint.Style.FILL
        paint.textSize = 9f
        paint.typeface = android.graphics.Typeface.DEFAULT

        // Get LMS table (adjust based on baby's sex if available)
        val lmsTable = LmsUtils.lmsDataHeightWeightGirls // Or use actual sex from baby data

        // Data rows
        records.take(maxRows).forEachIndexed { index, record ->
            // Draw alternating row background
            if (index % 2 == 0) {
                paint.color = android.graphics.Color.parseColor("#F5F5F5")
                canvas.drawRect(
                    tableLeft + 1f,
                    yPosition - 12f,
                    tableRight - 1f,
                    yPosition + 13f,
                    paint
                )
            }

            paint.color = android.graphics.Color.BLACK

            // Calculate Z-Score
            val zScore = calcularZScoreTallaEdad(
                talla = record.height,
                edadMeses = record.ageInMonths,
                lmsList = lmsTable
            )


            // Calculate normal range
            val rango = calcularRangoNormalTalla(record.ageInMonths, lmsTable)
            val rangoText = rango?.let { "${String.format("%.1f", it.min)}-${String.format("%.1f", it.max)}" } ?: "N/A"

            // Draw data
            canvas.drawText("${record.ageInMonths} m", colPositions[0], yPosition, paint)
            canvas.drawText("${record.height ?: "N/A"}", colPositions[1], yPosition, paint)
            canvas.drawText(zScore?.let { String.format("%.2f", it) } ?: "N/A", colPositions[2], yPosition, paint)
            canvas.drawText(rangoText, colPositions[3], yPosition, paint)


            // Draw row separator
            yPosition += rowHeight
            if (index < maxRows - 1) {
                paint.style = android.graphics.Paint.Style.STROKE
                paint.strokeWidth = 0.5f
                paint.color = android.graphics.Color.LTGRAY
                canvas.drawLine(tableLeft, yPosition - 17f, tableRight, yPosition - 17f, paint)
                paint.style = android.graphics.Paint.Style.FILL
                paint.color = android.graphics.Color.BLACK
            }
        }

        // Summary section
        yPosition += 30f
        paint.textSize = 14f
        paint.typeface = android.graphics.Typeface.DEFAULT_BOLD
        paint.color = android.graphics.Color.BLACK
        canvas.drawText("Resumen:", 50f, yPosition, paint)

        yPosition += 25f
        paint.textSize = 12f
        paint.typeface = android.graphics.Typeface.DEFAULT

        // Calculate statistics
        val totalMeasurements = records.size
        val lastRecord = records.lastOrNull()
        val lastHeight = lastRecord?.height?.let { String.format("%.1f", it) } ?: "N/A"
        val lastAge = lastRecord?.ageInMonths ?: 0

        // Count diagnoses
        val normalCount = records.count { record ->
            val zScore = calcularZScoreTallaEdad(
                talla = record.height,
                edadMeses = record.ageInMonths,
                lmsList = lmsTable
            )
            zScore?.let { it >= -2 && it <= 2 } ?: false
        }

        canvas.drawText("• Total de mediciones: $totalMeasurements", 70f, yPosition, paint)
        yPosition += 20f
        canvas.drawText("• Última medición: $lastHeight cm ($lastAge meses)", 70f, yPosition, paint)
        yPosition += 20f


        // Legend section
        yPosition += 35f
        paint.textSize = 12f
        paint.typeface = android.graphics.Typeface.DEFAULT_BOLD

        // Footer
        yPosition = 800f
        paint.textSize = 10f
        paint.color = android.graphics.Color.GRAY
        canvas.drawText("Generado por PhD Mama App", 50f, yPosition, paint)
        canvas.drawText("Basado en estándares OMS", 250f, yPosition, paint)
        canvas.drawText("Página 1 de 1", 450f, yPosition, paint)

        pdfDocument.finishPage(page)

        // Create and save PDF, then share
        val fileName = "reporte_longitud_altura_${babyId}_${System.currentTimeMillis()}.pdf"
        savePDFAndShare(context, pdfDocument, fileName)

    } catch (e: Exception) {
        android.widget.Toast.makeText(
            context,
            "Error al generar PDF: ${e.message}",
            android.widget.Toast.LENGTH_LONG
        ).show()
        e.printStackTrace()
    }
}

private fun savePDFAndShare(
    context: android.content.Context,
    pdfDocument: android.graphics.pdf.PdfDocument,
    fileName: String
) {
    try {
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.Q) {
            // Use MediaStore for Android 10+
            val contentValues = ContentValues().apply {
                put(MediaStore.MediaColumns.DISPLAY_NAME, fileName)
                put(MediaStore.MediaColumns.MIME_TYPE, "application/pdf")
                put(MediaStore.MediaColumns.RELATIVE_PATH, "Download/")
            }

            val resolver = context.contentResolver
            val uri = resolver.insert(MediaStore.Downloads.EXTERNAL_CONTENT_URI, contentValues)

            uri?.let { pdfUri ->
                resolver.openOutputStream(pdfUri)?.use { outputStream ->
                    pdfDocument.writeTo(outputStream)
                    pdfDocument.close()

                    android.widget.Toast.makeText(
                        context,
                        "PDF guardado exitosamente",
                        android.widget.Toast.LENGTH_SHORT
                    ).show()

                    // Share the PDF
                    sharePDF(context, pdfUri, fileName)
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
            // Fallback for older Android versions
            val downloadsDir = android.os.Environment.getExternalStoragePublicDirectory(
                android.os.Environment.DIRECTORY_DOWNLOADS
            )
            val file = java.io.File(downloadsDir, fileName)

            try {
                val fileOutputStream = java.io.FileOutputStream(file)
                pdfDocument.writeTo(fileOutputStream)
                pdfDocument.close()
                fileOutputStream.close()

                // Create content URI for sharing
                val uri = androidx.core.content.FileProvider.getUriForFile(
                    context,
                    "${context.packageName}.fileprovider",
                    file
                )

                android.widget.Toast.makeText(
                    context,
                    "PDF guardado exitosamente",
                    android.widget.Toast.LENGTH_SHORT
                ).show()

                // Share the PDF
                sharePDF(context, uri, fileName)

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
        pdfDocument.close()
        android.widget.Toast.makeText(
            context,
            "Error al procesar PDF: ${e.message}",
            android.widget.Toast.LENGTH_LONG
        ).show()
    }
}

private fun sharePDF(context: android.content.Context, uri: android.net.Uri, fileName: String) {
    try {
        val shareIntent = android.content.Intent().apply {
            action = android.content.Intent.ACTION_SEND
            type = "application/pdf"
            putExtra(android.content.Intent.EXTRA_STREAM, uri)
            putExtra(android.content.Intent.EXTRA_SUBJECT, "Reporte de Longitud/Altura")
            putExtra(android.content.Intent.EXTRA_TEXT, "Compartiendo reporte de crecimiento del bebé")
            addFlags(android.content.Intent.FLAG_GRANT_READ_URI_PERMISSION)
        }

        // Create chooser with WhatsApp as preferred option
        val chooserIntent = android.content.Intent.createChooser(shareIntent, "Compartir reporte")

        // Add WhatsApp as a specific option if available
        val whatsappIntent = android.content.Intent().apply {
            action = android.content.Intent.ACTION_SEND
            type = "application/pdf"
            setPackage("com.whatsapp")
            putExtra(android.content.Intent.EXTRA_STREAM, uri)
            putExtra(android.content.Intent.EXTRA_TEXT, "Reporte de longitud/altura")
            addFlags(android.content.Intent.FLAG_GRANT_READ_URI_PERMISSION)
        }

        // Check if WhatsApp is installed
        val packageManager = context.packageManager
        val whatsappAvailable = try {
            packageManager.getPackageInfo("com.whatsapp", 0)
            true
        } catch (e: android.content.pm.PackageManager.NameNotFoundException) {
            false
        }

        if (whatsappAvailable) {
            chooserIntent.putExtra(android.content.Intent.EXTRA_INITIAL_INTENTS, arrayOf(whatsappIntent))
        }

        // Start the share activity
        if (context is android.app.Activity) {
            context.startActivity(chooserIntent)
        } else {
            chooserIntent.addFlags(android.content.Intent.FLAG_ACTIVITY_NEW_TASK)
            context.startActivity(chooserIntent)
        }

    } catch (e: Exception) {
        android.widget.Toast.makeText(
            context,
            "Error al compartir: ${e.message}",
            android.widget.Toast.LENGTH_LONG
        ).show()
        e.printStackTrace()
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
