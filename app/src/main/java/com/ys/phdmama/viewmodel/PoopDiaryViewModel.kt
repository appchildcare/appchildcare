package com.ys.phdmama.viewmodel

import android.content.ContentValues
import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Canvas
import android.graphics.Paint
import android.graphics.Typeface
import android.graphics.pdf.PdfDocument
import android.os.Build
import android.os.Environment
import android.provider.MediaStore
import android.util.Log
import android.widget.Toast
import androidx.annotation.RequiresApi
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.google.firebase.auth.FirebaseAuth
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query
import com.ys.phdmama.R
import com.ys.phdmama.model.DayPoopEntry
import com.ys.phdmama.model.PoopRecord
import com.ys.phdmama.model.WeekDay
import com.ys.phdmama.repository.BabyPreferencesRepository
import com.ys.phdmama.ui.screens.poop.formatTimestamp
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.text.SimpleDateFormat
import java.util.*
import javax.inject.Inject

@HiltViewModel
class PoopDiaryViewModel @Inject constructor(
    private val preferencesRepository: BabyPreferencesRepository
): ViewModel() {
    private val firestore = FirebaseFirestore.getInstance()
    private val firebaseAuth: FirebaseAuth = FirebaseAuth.getInstance()

    private val _poopEntries = MutableStateFlow<List<DayPoopEntry>>(emptyList())
    val poopEntries: StateFlow<List<DayPoopEntry>> = _poopEntries.asStateFlow()

    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()

    private val _errorMessage = MutableStateFlow<String?>(null)
    val errorMessage: StateFlow<String?> = _errorMessage.asStateFlow()

    private val _selectedDate = MutableStateFlow(Calendar.getInstance())
    val selectedDate: StateFlow<Calendar> = _selectedDate.asStateFlow()

    private val _weekDays = MutableStateFlow<List<WeekDay>>(emptyList())
    val weekDays: StateFlow<List<WeekDay>> = _weekDays.asStateFlow()

    private val spanishLocale = Locale("es", "ES")
    private val dateFormatter = SimpleDateFormat("dd/MM/yyyy", spanishLocale)
    private val dayNameFormatter = SimpleDateFormat("EEEE dd", spanishLocale)

    private val _selectedBaby = MutableStateFlow<String?>(null)
    val selectedBaby: StateFlow<String?> = _selectedBaby.asStateFlow()

    init {
        generateCurrentWeek()
        observeSelectedBabyFromDataStore()
    }


    private fun observeSelectedBabyFromDataStore() {
        viewModelScope.launch {
            preferencesRepository.selectedBabyIdFlow.collect { savedBabyId ->
                if (savedBabyId != null) {
                    _selectedBaby.value = savedBabyId.toString()
                } else {
                    Log.d("PoopRegistrationViewModel", "Saved baby ID not found in list")
                }
            }
        }
    }

    fun fetchPoopData() {
        viewModelScope.launch {
            _isLoading.value = true
            _errorMessage.value = null

            try {
                val calendar = Calendar.getInstance()
                calendar.set(Calendar.DAY_OF_WEEK, Calendar.MONDAY)
                val startOfWeek = calendar.timeInMillis

                calendar.add(Calendar.DAY_OF_WEEK, 6)
                val endOfWeek = calendar.timeInMillis

                val userId = firebaseAuth.currentUser?.uid

                firestore
                    .collection("users")
                    .document(userId.toString())
                    .collection("babies")
                    .document(selectedBaby.value.toString())
                    .collection("poop_records")
                    .whereGreaterThanOrEqualTo("timestamp", startOfWeek)
                    .whereLessThanOrEqualTo("timestamp", endOfWeek)
                    .orderBy("timestamp", Query.Direction.DESCENDING)
                    .get()
                    .addOnSuccessListener { documents ->
                        val poopRecords = documents.mapNotNull { doc ->
                            try {
                                PoopRecord(
                                    timestamp = doc.getLong("timestamp") ?: System.currentTimeMillis(),
                                    time = doc.getString("time") ?: "",
                                    color = doc.getString("color") ?: "",
                                    texture = doc.getString("texture") ?: "",
                                    size = doc.getString("size") ?: "",
                                    notes = doc.getString("notes") ?: "",
                                    createdAt = doc.getLong("createdAt") ?: System.currentTimeMillis()
                                )
                            } catch (e: Exception) {
                                null
                            }
                        }

                        processPoopData(poopRecords)
                        updateWeekDaysWithData(poopRecords)
                        _isLoading.value = false
                    }
                    .addOnFailureListener { exception ->
                        _errorMessage.value = "Error al cargar datos: ${exception.message}"
                        _isLoading.value = false
                    }

            } catch (e: Exception) {
                _errorMessage.value = "Error inesperado: ${e.message}"
                _isLoading.value = false
            }
        }
    }

    private fun processPoopData(poopRecords: List<PoopRecord>) {
        val groupedByDay = poopRecords.groupBy { poopRecord ->
            val calendar = Calendar.getInstance()
            calendar.timeInMillis = poopRecord.timestamp
            dateFormatter.format(calendar.time)
        }

        val dayEntries = groupedByDay.map { (dateString, records) ->
            val calendar = Calendar.getInstance()
            calendar.timeInMillis = records.first().timestamp
            val dayName = dayNameFormatter.format(calendar.time)
                .replaceFirstChar { it.uppercase() }

            DayPoopEntry(
                dayName = dayName,
                poops = records.sortedBy { it.timestamp }
            )
        }.sortedByDescending { entry ->
            // Sort by the timestamp of the first poop record in each day
            entry.poops.firstOrNull()?.timestamp ?: 0L
        }

        _poopEntries.value = dayEntries
    }

    private fun generateCurrentWeek() {
        val calendar = Calendar.getInstance()
        val today = Calendar.getInstance()

        // Set to Monday of current week
        calendar.set(Calendar.DAY_OF_WEEK, Calendar.MONDAY)

        val weekDaysList = mutableListOf<WeekDay>()

        repeat(7) { dayIndex ->
            val dayName = dayNameFormatter.format(calendar.time)
                .replaceFirstChar { it.uppercase() }

            val isToday = calendar.get(Calendar.DAY_OF_YEAR) == today.get(Calendar.DAY_OF_YEAR) &&
                    calendar.get(Calendar.YEAR) == today.get(Calendar.YEAR)

            weekDaysList.add(
                WeekDay(
                    name = dayName,
                    isSelected = isToday,
                    poopCount = 0 // Will be updated when data is fetched
                )
            )

            if (isToday) {
                _selectedDate.value = calendar.clone() as Calendar
            }

            calendar.add(Calendar.DAY_OF_MONTH, 1)
        }

        _weekDays.value = weekDaysList
    }

    private fun updateWeekDaysWithData(poopRecords: List<PoopRecord>) {
        val poopCountsByDay = poopRecords.groupBy { poopRecord ->
            val calendar = Calendar.getInstance()
            calendar.timeInMillis = poopRecord.timestamp
            calendar.get(Calendar.DAY_OF_YEAR)
        }.mapValues { it.value.size }

        val updatedWeekDays = _weekDays.value.map { weekDay ->
            val calendar = Calendar.getInstance()
            calendar.timeInMillis = _selectedDate.value.timeInMillis

            // Find the calendar day for this weekDay
            val weekCalendar = Calendar.getInstance()
            weekCalendar.set(Calendar.DAY_OF_WEEK, Calendar.MONDAY)

            repeat(7) { dayIndex ->
                val dayName = dayNameFormatter.format(weekCalendar.time)
                    .replaceFirstChar { it.uppercase() }

                if (dayName == weekDay.name) {
                    val dayOfYear = weekCalendar.get(Calendar.DAY_OF_YEAR)
                    val poopCount = poopCountsByDay[dayOfYear] ?: 0

                    return@map weekDay.copy(poopCount = poopCount)
                }

                weekCalendar.add(Calendar.DAY_OF_MONTH, 1)
            }

            weekDay
        }

        _weekDays.value = updatedWeekDays
    }

    fun selectDate(calendar: Calendar) {
        _selectedDate.value = calendar

        val updatedWeekDays = _weekDays.value.map { weekDay ->
            val selectedDayName = dayNameFormatter.format(calendar.time)
                .replaceFirstChar { it.uppercase() }

            weekDay.copy(isSelected = weekDay.name == selectedDayName)
        }

        _weekDays.value = updatedWeekDays
    }

    @RequiresApi(Build.VERSION_CODES.Q)
    fun generatePoopPdfReport(context: Context) {
        viewModelScope.launch(Dispatchers.IO) {
            try {
                val pdfDocument = PdfDocument()
                val pageWidth = 595f
                val pageHeight = 842f
                var pageNumber = 1
                var totalPages = 1

                val pageInfo = PdfDocument.PageInfo.Builder(595, 842, pageNumber).create()
                var page = pdfDocument.startPage(pageInfo)
                var canvas = page.canvas
                val paint = Paint()

                // Load and draw image/logo at the top
                val logo = BitmapFactory.decodeResource(context.resources, R.drawable.app_child_care_logo)
                val scaledLogo = Bitmap.createScaledBitmap(logo, 100, 100, false)
                val centerX = (pageWidth - 100) / 2f
                canvas.drawBitmap(scaledLogo, centerX, 20f, paint)

                // Title
                paint.textSize = 20f
                paint.typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
                canvas.drawText("Reporte de Deposiciones", 50f, 150f, paint)

                // Date
                paint.textSize = 12f
                paint.typeface = Typeface.create(Typeface.DEFAULT, Typeface.NORMAL)
                val currentDate = SimpleDateFormat("dd/MM/yyyy HH:mm", spanishLocale).format(Date())
                canvas.drawText("Fecha de generación: $currentDate", 50f, 180f, paint)

                var yPosition = 220f
                val lineHeight = 20f
                val pageContentHeight = 750f // Leave space for footer

                // Helper function to draw footer
                fun drawFooter(currentCanvas: Canvas, currentPage: Int) {
                    val footerPaint = Paint().apply {
                        textSize = 10f
                        typeface = Typeface.create(Typeface.DEFAULT, Typeface.NORMAL)
                        color = android.graphics.Color.GRAY
                    }

                    // Draw line above footer
                    currentCanvas.drawLine(50f, 800f, 545f, 800f, footerPaint)

                    // Left side: "Generado por Child Care App"
                    currentCanvas.drawText("Generado por Child Care App", 50f, 820f, footerPaint)

                    // Right side: "Página X de Y"
                    val pageText = "Página $currentPage de $totalPages"
                    footerPaint.textAlign = Paint.Align.RIGHT
                    currentCanvas.drawText(pageText, 545f, 820f, footerPaint)
                    footerPaint.textAlign = Paint.Align.LEFT // Reset alignment
                }

                // Helper function to check and create new page if needed
                fun checkNewPage(requiredSpace: Float): Boolean {
                    if (yPosition + requiredSpace > pageContentHeight) {
                        drawFooter(canvas, pageNumber)
                        pdfDocument.finishPage(page)

                        pageNumber++
                        totalPages = pageNumber
                        val newPageInfo = PdfDocument.PageInfo.Builder(595, 842, pageNumber).create()
                        page = pdfDocument.startPage(newPageInfo)
                        canvas = page.canvas
                        yPosition = 50f
                        return true
                    }
                    return false
                }

                // Data rows - iterate through poop entries by day
                _poopEntries.value.forEach { dayEntry ->
                    // Check if we need a new page for the day header
                    checkNewPage(lineHeight * 2)

                    // Draw day header
                    paint.typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
                    paint.textSize = 14f
                    canvas.drawText(dayEntry.dayName, 50f, yPosition, paint)
                    yPosition += lineHeight

                    // Draw separator line
                    paint.strokeWidth = 1f
                    canvas.drawLine(50f, yPosition, 545f, yPosition, paint)
                    yPosition += lineHeight / 2

                    // Draw each poop record for this day
                    dayEntry.poops.forEach { poopRecord ->
                        // Calculate required space for this record
                        val recordLines = mutableListOf<String>()
                        val time = if (poopRecord.time.isNotEmpty()) {
                            poopRecord.time
                        } else {
                            formatTimestamp(poopRecord.timestamp)
                        }

                        recordLines.add("Hora: $time")

                        if (poopRecord.color.isNotEmpty()) {
                            recordLines.add("Color: ${poopRecord.color}")
                        }
                        if (poopRecord.texture.isNotEmpty()) {
                            recordLines.add("Textura: ${poopRecord.texture}")
                        }
                        if (poopRecord.size.isNotEmpty()) {
                            recordLines.add("Tamaño: ${poopRecord.size}")
                        }
                        if (poopRecord.notes.isNotEmpty()) {
                            recordLines.add("Notas: ${poopRecord.notes}")
                        }

                        val requiredSpace = (recordLines.size + 1) * lineHeight
                        checkNewPage(requiredSpace)

                        // Draw record header with time
                        paint.typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
                        paint.textSize = 12f
                        canvas.drawText("🚼 Deposición - $time", 70f, yPosition, paint)
                        yPosition += lineHeight

                        // Draw record details
                        paint.typeface = Typeface.create(Typeface.DEFAULT, Typeface.NORMAL)
                        paint.textSize = 11f

                        if (poopRecord.color.isNotEmpty()) {
                            canvas.drawText("   Color: ${poopRecord.color}", 90f, yPosition, paint)
                            yPosition += lineHeight
                        }

                        if (poopRecord.texture.isNotEmpty()) {
                            canvas.drawText("   Textura: ${poopRecord.texture}", 90f, yPosition, paint)
                            yPosition += lineHeight
                        }

                        if (poopRecord.size.isNotEmpty()) {
                            canvas.drawText("   Tamaño: ${poopRecord.size}", 90f, yPosition, paint)
                            yPosition += lineHeight
                        }

                        if (poopRecord.notes.isNotEmpty()) {
                            // Handle long notes - wrap text if needed
                            val maxWidth = 450f
                            val words = poopRecord.notes.split(" ")
                            var currentLine = "   Notas: "

                            words.forEach { word ->
                                val testLine = if (currentLine == "   Notas: ") {
                                    "$currentLine$word"
                                } else {
                                    "$currentLine $word"
                                }

                                val textWidth = paint.measureText(testLine)

                                if (textWidth > maxWidth) {
                                    checkNewPage(lineHeight)
                                    canvas.drawText(currentLine, 90f, yPosition, paint)
                                    yPosition += lineHeight
                                    currentLine = "          $word"
                                } else {
                                    currentLine = testLine
                                }
                            }

                            checkNewPage(lineHeight)
                            canvas.drawText(currentLine, 90f, yPosition, paint)
                            yPosition += lineHeight
                        }

                        // Add spacing between records
                        yPosition += lineHeight / 2
                    }

                    // Add spacing between days
                    yPosition += lineHeight
                }

                // Summary section
                checkNewPage(lineHeight * 6)

                paint.typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
                paint.textSize = 14f
                canvas.drawText("Resumen Semanal", 50f, yPosition, paint)
                yPosition += lineHeight

                paint.strokeWidth = 2f
                canvas.drawLine(50f, yPosition, 545f, yPosition, paint)
                yPosition += lineHeight

                paint.typeface = Typeface.create(Typeface.DEFAULT, Typeface.NORMAL)
                paint.textSize = 12f

                val totalPoops = getTotalPoopCountForWeek()
                val avgPoops = getAveragePoopCountPerDay()

                canvas.drawText("Total de deposiciones en la semana: $totalPoops", 70f, yPosition, paint)
                yPosition += lineHeight

                canvas.drawText("Promedio por día: %.1f".format(avgPoops), 70f, yPosition, paint)
                yPosition += lineHeight

                // Draw footer on last page
                drawFooter(canvas, pageNumber)
                pdfDocument.finishPage(page)

                // Save the document
                val fileName = "Reporte_Deposiciones_${SimpleDateFormat("yyyyMMdd_HHmmss", spanishLocale).format(Date())}.pdf"
                val resolver = context.contentResolver
                val contentValues = ContentValues().apply {
                    put(MediaStore.MediaColumns.DISPLAY_NAME, fileName)
                    put(MediaStore.MediaColumns.MIME_TYPE, "application/pdf")
                    put(MediaStore.MediaColumns.RELATIVE_PATH, Environment.DIRECTORY_DOWNLOADS)
                }

                val uri = resolver.insert(MediaStore.Downloads.EXTERNAL_CONTENT_URI, contentValues)
                uri?.let {
                    resolver.openOutputStream(it)?.use { outputStream ->
                        pdfDocument.writeTo(outputStream)
                    }

                    withContext(Dispatchers.Main) {
                        Toast.makeText(context, "PDF guardado en Descargas", Toast.LENGTH_LONG).show()
                    }
                }

                pdfDocument.close()

            } catch (e: Exception) {
                Log.e("PoopDiaryViewModel", "Error generating PDF", e)
                withContext(Dispatchers.Main) {
                    Toast.makeText(context, "Error al generar PDF: ${e.message}", Toast.LENGTH_LONG).show()
                }
            }
        }
    }

    fun navigateToNextDay() {
        val calendar = _selectedDate.value.clone() as Calendar
        calendar.add(Calendar.DAY_OF_MONTH, 1)
        selectDate(calendar)
    }

    fun navigateToPreviousDay() {
        val calendar = _selectedDate.value.clone() as Calendar
        calendar.add(Calendar.DAY_OF_MONTH, -1)
        selectDate(calendar)
    }

    fun addPoopRecord(babyId: String, poopRecord: PoopRecord) {
        viewModelScope.launch {
            _isLoading.value = true
            _errorMessage.value = null

            try {
                val recordData = hashMapOf(
                    "timestamp" to poopRecord.timestamp,
                    "time" to poopRecord.time,
                    "color" to poopRecord.color,
                    "texture" to poopRecord.texture,
                    "size" to poopRecord.size,
                    "notes" to poopRecord.notes,
                    "createdAt" to poopRecord.createdAt
                )

                firestore.collection("babies")
                    .document(babyId)
                    .collection("poop_records")
                    .add(recordData)
                    .addOnSuccessListener {
                        // Refresh data after adding
                        fetchPoopData()
                    }
                    .addOnFailureListener { exception ->
                        _errorMessage.value = "Error al guardar registro: ${exception.message}"
                        _isLoading.value = false
                    }

            } catch (e: Exception) {
                _errorMessage.value = "Error inesperado: ${e.message}"
                _isLoading.value = false
            }
        }
    }

    fun deletePoopRecord(babyId: String, recordId: String) {
        viewModelScope.launch {
            _isLoading.value = true
            _errorMessage.value = null

            try {
                firestore.collection("babies")
                    .document(babyId)
                    .collection("poop_records")
                    .document(recordId)
                    .delete()
                    .addOnSuccessListener {
                        // Refresh data after deleting
                        fetchPoopData()
                    }
                    .addOnFailureListener { exception ->
                        _errorMessage.value = "Error al eliminar registro: ${exception.message}"
                        _isLoading.value = false
                    }

            } catch (e: Exception) {
                _errorMessage.value = "Error inesperado: ${e.message}"
                _isLoading.value = false
            }
        }
    }

    fun updatePoopRecord(babyId: String, recordId: String, updatedRecord: PoopRecord) {
        viewModelScope.launch {
            _isLoading.value = true
            _errorMessage.value = null

            try {
                val recordData = hashMapOf(
                    "timestamp" to updatedRecord.timestamp,
                    "time" to updatedRecord.time,
                    "color" to updatedRecord.color,
                    "texture" to updatedRecord.texture,
                    "size" to updatedRecord.size,
                    "notes" to updatedRecord.notes,
                    "createdAt" to updatedRecord.createdAt
                )

                firestore.collection("babies")
                    .document(babyId)
                    .collection("poop_records")
                    .document(recordId)
                    .update(recordData as Map<String, Any>)
                    .addOnSuccessListener {
                        // Refresh data after updating
                        fetchPoopData()
                    }
                    .addOnFailureListener { exception ->
                        _errorMessage.value = "Error al actualizar registro: ${exception.message}"
                        _isLoading.value = false
                    }

            } catch (e: Exception) {
                _errorMessage.value = "Error inesperado: ${e.message}"
                _isLoading.value = false
            }
        }
    }

    fun clearErrorMessage() {
        _errorMessage.value = null
    }

    fun getPoopRecordsForDate(calendar: Calendar): List<PoopRecord> {
        val targetDateString = dateFormatter.format(calendar.time)

        return _poopEntries.value
            .find { entry ->
                val entryCalendar = Calendar.getInstance()
                if (entry.poops.isNotEmpty()) {
                    entryCalendar.timeInMillis = entry.poops.first().timestamp
                    dateFormatter.format(entryCalendar.time) == targetDateString
                } else {
                    false
                }
            }?.poops ?: emptyList()
    }

    fun getTotalPoopCountForWeek(): Int {
        return _poopEntries.value.sumOf { it.poops.size }
    }

    fun getAveragePoopCountPerDay(): Float {
        val totalCount = getTotalPoopCountForWeek()
        val daysWithData = _weekDays.value.count { it.poopCount > 0 }
        return if (daysWithData > 0) totalCount.toFloat() / daysWithData else 0f
    }
}