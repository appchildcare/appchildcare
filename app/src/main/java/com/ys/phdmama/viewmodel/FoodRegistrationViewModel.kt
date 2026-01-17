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
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query
import com.ys.phdmama.R
import com.ys.phdmama.model.FoodReaction
import com.ys.phdmama.repository.BabyPreferencesRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import java.util.UUID
import javax.inject.Inject

@HiltViewModel
class FoodRegistrationViewModel @Inject constructor(
    private val preferencesRepository: BabyPreferencesRepository
) : ViewModel() {
    private val firestore = FirebaseFirestore.getInstance()
    private val auth = FirebaseAuth.getInstance()

    private val _selectedBaby = MutableStateFlow<String?>(null)
    val selectedBaby: StateFlow<String?> = _selectedBaby.asStateFlow()

    var foodName by mutableStateOf("")
    var hasReaction by mutableStateOf<Boolean?>(null)
    var reactionDetail by mutableStateOf("")
    private val _foodList = MutableStateFlow<List<FoodReaction>>(emptyList())
    val foodList: StateFlow<List<FoodReaction>> = _foodList.asStateFlow()

    init {
        observeSelectedBabyFromDataStore()
    }

    private fun observeSelectedBabyFromDataStore() {
        viewModelScope.launch {
            preferencesRepository.selectedBabyIdFlow.collect { savedBabyId ->
                if (savedBabyId != null) {
                    _selectedBaby.value = savedBabyId.toString()
                } else {
                    Log.d("FoodRegistrationVM", "Saved baby ID not found in list")
                }
            }
        }
    }

    fun saveFoodReaction() {
        if (foodName.isBlank()) {
            return
        }
        if (hasReaction == null) {
            return
        }

        val foodId = UUID.randomUUID().toString()
        val formatter = SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault())
        val currentDate = formatter.format(Date())

        val foodReaction = FoodReaction(
            id = foodId,
            foodName = foodName,
            hasReaction = hasReaction!!,
            reactionDetail = if (hasReaction == true) reactionDetail else "",
            date = currentDate
        )

        val userId = auth.currentUser?.uid
        val selectedBaby = selectedBaby.value
        val db = FirebaseFirestore.getInstance()

        if (userId != null && selectedBaby != null) {
            val foodRef = db.collection("users")
                .document(userId)
                .collection("babies")
                .document(selectedBaby.toString())
                .collection("food_reactions")

            foodRef.add(foodReaction)
                .addOnSuccessListener {
                    foodName = ""
                    hasReaction = null
                    reactionDetail = ""
                }
                .addOnFailureListener { e ->
                    // Log error for debugging
                    Log.e("FoodRegistrationVM", "Error saving food reaction", e)
                }
        } else {
            Log.e("FoodRegistrationVM", "User ID or babyID is null")
            return
        }
    }

    fun loadFoodReactions() {
        val userId = auth.currentUser?.uid ?: return
        val selectedBaby = selectedBaby.value

        firestore.collection("users")
            .document(userId)
            .collection("babies")
            .document(selectedBaby.toString())
            .collection("food_reactions")
            .orderBy("date", Query.Direction.DESCENDING)
            .addSnapshotListener { snapshot, error ->
                if (error != null) {
                    Log.e("FoodRegistrationVM", "Error loading food reactions", error)
                    return@addSnapshotListener
                }

                val reactions = snapshot?.documents?.mapNotNull { doc ->
                    doc.toObject(FoodReaction::class.java)?.copy(id = doc.id)
                } ?: emptyList()

                _foodList.value = reactions
            }
    }

    fun updateFoodReaction(foodReaction: FoodReaction) {
        val userId = auth.currentUser?.uid ?: return
        val selectedBaby = selectedBaby.value

        firestore.collection("users")
            .document(userId)
            .collection("babies")
            .document(selectedBaby.toString())
            .collection("food_reactions")
            .document(foodReaction.id)
            .set(foodReaction)
            .addOnSuccessListener {
                loadFoodReactions()
            }
    }

    @RequiresApi(Build.VERSION_CODES.Q)
    fun generatePdfReport(context: Context) {
        viewModelScope.launch(Dispatchers.IO) {
            try {
                val pdfDocument = PdfDocument()
                val pageWidth = 595f
                val pageHeight = 842f
                var pageNumber = 1
                var totalPages = 1 // We'll calculate this if needed

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
                canvas.drawText("Reporte de Alimentos", 50f, 150f, paint)

                // Date
                paint.textSize = 12f
                paint.typeface = Typeface.create(Typeface.DEFAULT, Typeface.NORMAL)
                val currentDate = SimpleDateFormat("dd/MM/yyyy HH:mm", Locale.getDefault()).format(Date())
                canvas.drawText("Fecha de generación: $currentDate", 50f, 180f, paint)

                var yPosition = 220f
                val lineHeight = 20f
                val pageContentHeight = 750f // Leave space for footer

                // Headers
                paint.typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
                canvas.drawText("Fecha", 50f, yPosition, paint)
                canvas.drawText("Alimento", 150f, yPosition, paint)
                canvas.drawText("Reacción", 350f, yPosition, paint)

                yPosition += lineHeight
                paint.typeface = Typeface.create(Typeface.DEFAULT, Typeface.NORMAL)

                canvas.drawLine(50f, yPosition, 545f, yPosition, paint)
                yPosition += lineHeight

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

                // Data rows
                _foodList.value.forEach { food ->
                    // Check if we need a new page
                    if (yPosition > pageContentHeight) {
                        // Draw footer on current page before finishing
                        drawFooter(canvas, pageNumber)
                        pdfDocument.finishPage(page)

                        pageNumber++
                        totalPages = pageNumber // Update total pages
                        val newPageInfo = PdfDocument.PageInfo.Builder(595, 842, pageNumber).create()
                        page = pdfDocument.startPage(newPageInfo)
                        canvas = page.canvas
                        yPosition = 50f
                    }

                    val formattedDate = try {
                        val date = SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault()).parse(food.date)
                        SimpleDateFormat("dd/MM/yyyy", Locale.getDefault()).format(date ?: Date())
                    } catch (e: Exception) {
                        food.date
                    }

                    canvas.drawText(formattedDate, 50f, yPosition, paint)
                    canvas.drawText(food.foodName, 150f, yPosition, paint)
                    canvas.drawText(if (food.hasReaction) "Sí" else "No", 350f, yPosition, paint)
                    yPosition += lineHeight

                    if (food.hasReaction && food.reactionDetail.isNotEmpty()) {
                        if (yPosition > pageContentHeight) {
                            drawFooter(canvas, pageNumber)
                            pdfDocument.finishPage(page)

                            pageNumber++
                            totalPages = pageNumber
                            val newPageInfo = PdfDocument.PageInfo.Builder(595, 842, pageNumber).create()
                            page = pdfDocument.startPage(newPageInfo)
                            canvas = page.canvas
                            yPosition = 50f
                        }

                        paint.textSize = 10f
                        canvas.drawText("  Detalle: ${food.reactionDetail}", 150f, yPosition, paint)
                        paint.textSize = 12f
                        yPosition += lineHeight
                    }
                }

                // Draw footer on last page
                drawFooter(canvas, pageNumber)
                pdfDocument.finishPage(page)

                // Save the document
                val fileName = "Reporte_Alimentos_${SimpleDateFormat("yyyyMMdd_HHmmss", Locale.getDefault()).format(Date())}.pdf"
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
                Log.e("FoodRegistrationVM", "Error generating PDF", e)
                withContext(Dispatchers.Main) {
                    Toast.makeText(context, "Error al generar PDF: ${e.message}", Toast.LENGTH_LONG).show()
                }
            }
        }
    }
}
