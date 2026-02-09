package com.ys.phdmama.services

import android.app.Activity
import android.content.ContentValues
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Color
import android.graphics.pdf.PdfDocument
import android.graphics.Typeface

import android.net.Uri
import android.os.Build
import android.os.Environment
import android.provider.MediaStore
import android.widget.Toast
import androidx.core.content.FileProvider
import java.io.File
import java.io.FileOutputStream
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

object PdfGeneratorUtils {

    /**
     * Generic PDF generator with customizable content
     * @param context Android context
     * @param fileName Name for the PDF file (without extension)
     * @param title Main title of the document
     * @param subtitle Optional subtitle (e.g., baby name, date range)
     * @param logoResId Optional logo resource ID
     * @param contentBuilder Lambda function to build the PDF content
     */
    fun generateAndSharePdf(
        context: Context,
        fileName: String,
        title: String,
        subtitle: String? = null,
        logoResId: Int? = null,
        contentBuilder: PdfContentBuilder.() -> Unit
    ) {
        try {
            val pdfDocument = PdfDocument()
            val pageWidth = 595f
            val pageHeight = 842f

            val builder = PdfContentBuilder(
                context = context,
                pdfDocument = pdfDocument,
                pageWidth = pageWidth,
                pageHeight = pageHeight,
                title = title,
                subtitle = subtitle,
                logoResId = logoResId
            )

            builder.contentBuilder()
            builder.finishDocument()

            // Generate timestamped filename
            val timestampedFileName = "${fileName}_${
                SimpleDateFormat("yyyyMMdd_HHmmss", Locale.getDefault()).format(Date())
            }.pdf"

            savePDFAndShare(context, pdfDocument, timestampedFileName)

        } catch (e: Exception) {
            Toast.makeText(
                context,
                "Error al generar PDF: ${e.message}",
                Toast.LENGTH_LONG
            ).show()
            e.printStackTrace()
        }
    }

    private fun savePDFAndShare(
        context: Context,
        pdfDocument: PdfDocument,
        fileName: String
    ) {
        try {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                // Use MediaStore for Android 10+
                val contentValues = ContentValues().apply {
                    put(MediaStore.MediaColumns.DISPLAY_NAME, fileName)
                    put(MediaStore.MediaColumns.MIME_TYPE, "application/pdf")
                    put(MediaStore.MediaColumns.RELATIVE_PATH, Environment.DIRECTORY_DOWNLOADS)
                }

                val resolver = context.contentResolver
                val uri = resolver.insert(MediaStore.Downloads.EXTERNAL_CONTENT_URI, contentValues)

                uri?.let { pdfUri ->
                    resolver.openOutputStream(pdfUri)?.use { outputStream ->
                        pdfDocument.writeTo(outputStream)
                        pdfDocument.close()

                        Toast.makeText(
                            context,
                            "PDF guardado exitosamente en Descargas",
                            Toast.LENGTH_SHORT
                        ).show()

                        // Share the PDF
                        sharePDF(context, pdfUri, fileName)
                    }
                } ?: run {
                    pdfDocument.close()
                    Toast.makeText(
                        context,
                        "Error al crear el archivo PDF",
                        Toast.LENGTH_LONG
                    ).show()
                }
            } else {
                // Fallback for older Android versions
                val downloadsDir = Environment.getExternalStoragePublicDirectory(
                    Environment.DIRECTORY_DOWNLOADS
                )
                val file = File(downloadsDir, fileName)

                try {
                    val fileOutputStream = FileOutputStream(file)
                    pdfDocument.writeTo(fileOutputStream)
                    pdfDocument.close()
                    fileOutputStream.close()

                    // Create content URI for sharing
                    val uri = FileProvider.getUriForFile(
                        context,
                        "${context.packageName}.fileprovider",
                        file
                    )

                    Toast.makeText(
                        context,
                        "PDF guardado exitosamente en Descargas",
                        Toast.LENGTH_SHORT
                    ).show()

                    // Share the PDF
                    sharePDF(context, uri, fileName)

                } catch (e: Exception) {
                    pdfDocument.close()
                    Toast.makeText(
                        context,
                        "Error al guardar PDF: ${e.message}",
                        Toast.LENGTH_LONG
                    ).show()
                }
            }
        } catch (e: Exception) {
            pdfDocument.close()
            Toast.makeText(
                context,
                "Error al procesar PDF: ${e.message}",
                Toast.LENGTH_LONG
            ).show()
        }
    }

    private fun sharePDF(context: Context, uri: Uri, fileName: String) {
        try {
            val shareIntent = Intent().apply {
                action = Intent.ACTION_SEND
                type = "application/pdf"
                putExtra(Intent.EXTRA_STREAM, uri)
                putExtra(Intent.EXTRA_SUBJECT, "Reporte Child Care")
                putExtra(
                    Intent.EXTRA_TEXT,
                    "Compartiendo reporte generado por Child Care App"
                )
                addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
            }

            // Create chooser with WhatsApp as preferred option
            val chooserIntent = Intent.createChooser(shareIntent, "Compartir reporte")

            // Add WhatsApp as a specific option if available
            val whatsappIntent = Intent().apply {
                action = Intent.ACTION_SEND
                type = "application/pdf"
                setPackage("com.whatsapp")
                putExtra(Intent.EXTRA_STREAM, uri)
                putExtra(Intent.EXTRA_TEXT, "Reporte Child Care")
                addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
            }

            // Check if WhatsApp is installed
            val packageManager = context.packageManager
            val whatsappAvailable = try {
                packageManager.getPackageInfo("com.whatsapp", 0)
                true
            } catch (e: PackageManager.NameNotFoundException) {
                false
            }

            if (whatsappAvailable) {
                chooserIntent.putExtra(
                    Intent.EXTRA_INITIAL_INTENTS,
                    arrayOf(whatsappIntent)
                )
            }

            // Start the share activity
            if (context is Activity) {
                context.startActivity(chooserIntent)
            } else {
                chooserIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                context.startActivity(chooserIntent)
            }

        } catch (e: Exception) {
            Toast.makeText(
                context,
                "Error al compartir: ${e.message}",
                Toast.LENGTH_LONG
            ).show()
            e.printStackTrace()
        }
    }
}


/**
 * Builder class for PDF content
 */
class PdfContentBuilder(
    private val context: Context,
    private val pdfDocument: PdfDocument,
    private val pageWidth: Float,
    private val pageHeight: Float,
    private val title: String,
    private val subtitle: String?,
    private val logoResId: Int?
) {
    private var currentPage: PdfDocument.Page
    private var canvas: android.graphics.Canvas  // Explicitly use android.graphics.Canvas
    private val paint =  android.graphics.Paint()
    private var yPosition = 0f
    private var pageNumber = 1
    private var totalPages = 1

    private val pageContentHeight = 750f
    private val marginLeft = 50f
    private val marginRight = 545f

    init {
        // Create first page
        val pageInfo = PdfDocument.PageInfo.Builder(
            pageWidth.toInt(),
            pageHeight.toInt(),
            pageNumber
        ).create()
        currentPage = pdfDocument.startPage(pageInfo)
        canvas = currentPage.canvas

        // Draw header
        drawHeader()
    }

    private fun drawHeader() {
        // Draw logo if provided
        logoResId?.let { resId ->
            val logo = BitmapFactory.decodeResource(context.resources, resId)
            val scaledLogo = Bitmap.createScaledBitmap(logo, 100, 100, false)
            val centerX = (pageWidth - 100) / 2f
            canvas.drawBitmap(scaledLogo, centerX, 20f, null)  // Change paint to null
            yPosition = 130f
        } ?: run {
            yPosition = 50f
        }

        // Draw title
        paint.textSize = 20f
        paint.typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
        paint.color = Color.BLACK
        canvas.drawText(title, marginLeft, yPosition, paint)
        yPosition += 30f

        // Draw subtitle if provided
        subtitle?.let {
            paint.textSize = 12f
            paint.typeface = Typeface.create(Typeface.DEFAULT, Typeface.NORMAL)
            canvas.drawText(it, marginLeft, yPosition, paint)
            yPosition += 20f
        }

        // Draw generation date
        paint.textSize = 12f
        paint.typeface = Typeface.create(Typeface.DEFAULT, Typeface.NORMAL)
        val currentDate = SimpleDateFormat("dd/MM/yyyy HH:mm", Locale.getDefault()).format(Date())
        canvas.drawText("Fecha de generación: $currentDate", marginLeft, yPosition, paint)
        yPosition += 40f
    }

    private fun drawFooter() {
        val footerPaint = android.graphics.Paint().apply {
            textSize = 10f
            typeface = Typeface.create(Typeface.DEFAULT, Typeface.NORMAL)
            color = android.graphics.Color.GRAY
        }

        // Draw line above footer
        canvas.drawLine(marginLeft, 800f, marginRight, 800f, footerPaint)

        // Left side: "Generado por Child Care App"
        canvas.drawText("Generado por Child Care App", marginLeft, 820f, footerPaint)

        // Right side: "Página X de Y"
        val pageText = "Página $pageNumber de $totalPages"
        footerPaint.textAlign = android.graphics.Paint.Align.RIGHT
        canvas.drawText(pageText, marginRight, 820f, footerPaint)
    }

    /**
     * Check if new page is needed and create it
     */
    fun checkNewPage(requiredSpace: Float = 30f): Boolean {
        if (yPosition + requiredSpace > pageContentHeight) {
            drawFooter()
            pdfDocument.finishPage(currentPage)

            pageNumber++
            totalPages = pageNumber

            val pageInfo = PdfDocument.PageInfo.Builder(
                pageWidth.toInt(),
                pageHeight.toInt(),
                pageNumber
            ).create()
            currentPage = pdfDocument.startPage(pageInfo)
            canvas = currentPage.canvas
            yPosition = 50f

            return true
        }
        return false
    }

    /**
     * Draw a table with headers and data
     */
    fun drawTable(
        headers: List<String>,
        columnPositions: FloatArray,
        data: List<List<String>>,
        rowHeight: Float = 30f,
        alternateRowColor: Boolean = true
    ) {
        val tableTop = yPosition - 15f
        val headerHeight = 30f

        // Draw table border
        paint.style = android.graphics.Paint.Style.STROKE
        paint.strokeWidth = 2f
        paint.color = android.graphics.Color.BLACK

        val maxRows = data.size
        val tableBottom = tableTop + headerHeight + (maxRows * rowHeight)

        canvas.drawRect(marginLeft, tableTop, marginRight, tableBottom, paint)

        // Draw column separators
        for (i in 1 until columnPositions.size) {
            canvas.drawLine(columnPositions[i], tableTop, columnPositions[i], tableBottom, paint)
        }

        // Draw headers
        paint.style =  android.graphics.Paint.Style.FILL
        paint.color = android.graphics.Color.BLACK
        paint.textSize = 11f
        paint.typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)

        headers.forEachIndexed { index, header ->
            canvas.drawText(header, columnPositions[index], yPosition, paint)
        }

        // Draw header separator line
        yPosition += 15f
        paint.style = android.graphics.Paint.Style.STROKE
        paint.strokeWidth = 1f
        canvas.drawLine(marginLeft, yPosition, marginRight, yPosition, paint)

        yPosition += 15f
        paint.style = android.graphics.Paint.Style.FILL
        paint.textSize = 9f
        paint.typeface = Typeface.create(Typeface.DEFAULT, Typeface.NORMAL)

        // Draw data rows
        data.forEachIndexed { index, row ->
            // Draw alternating row background
            if (alternateRowColor && index % 2 == 0) {
                paint.color = android.graphics.Color.parseColor("#F5F5F5")
                canvas.drawRect(
                    marginLeft + 1f,
                    yPosition - 12f,
                    marginRight - 1f,
                    yPosition + 13f,
                    paint
                )
            }

            paint.color = android.graphics.Color.BLACK

            row.forEachIndexed { colIndex, cell ->
                canvas.drawText(cell, columnPositions[colIndex], yPosition, paint)
            }

            yPosition += rowHeight

            // Draw row separator
            if (index < data.size - 1) {
                paint.style = android.graphics.Paint.Style.STROKE
                paint.strokeWidth = 0.5f
                paint.color = android.graphics.Color.LTGRAY
                canvas.drawLine(marginLeft, yPosition - 17f, marginRight, yPosition - 17f, paint)
                paint.style = android.graphics.Paint.Style.FILL
                paint.color = android.graphics.Color.BLACK
            }
        }

        yPosition += 20f
    }

    /**
     * Add a section title
     */
    fun addSectionTitle(text: String, size: Float = 14f) {
        checkNewPage()
        paint.textSize = size
        paint.typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
        paint.color = android.graphics.Color.BLACK
        canvas.drawText(text, marginLeft, yPosition, paint)
        yPosition += 25f
    }

    /**
     * Add regular text
     */
    fun addText(text: String, size: Float = 12f, indent: Float = 0f) {
        checkNewPage()
        paint.textSize = size
        paint.typeface = Typeface.create(Typeface.DEFAULT, Typeface.NORMAL)
        paint.color = android.graphics.Color.BLACK
        canvas.drawText(text, marginLeft + indent, yPosition, paint)
        yPosition += 20f
    }

    /**
     * Add a horizontal line
     */
    fun addLine() {
        checkNewPage()
        paint.style = android.graphics.Paint.Style.STROKE
        paint.strokeWidth = 1f
        paint.color = android.graphics.Color.LTGRAY
        canvas.drawLine(marginLeft, yPosition, marginRight, yPosition, paint)
        paint.style = android.graphics.Paint.Style.FILL
        yPosition += 10f
    }

    /**
     * Get current canvas for custom drawing
     */
    fun getCanvas(): android.graphics.Canvas = canvas

    /**
     * Get current paint for custom drawing
     */
    fun getPaint(): android.graphics.Paint = paint

    /**
     * Get current Y position
     */
    fun getCurrentY(): Float = yPosition

    /**
     * Set Y position
     */
    fun setCurrentY(y: Float) {
        yPosition = y
    }

    /**
     * Finish the document
     */
    fun finishDocument() {
        drawFooter()
        pdfDocument.finishPage(currentPage)
    }
}