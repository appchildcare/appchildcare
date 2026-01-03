package com.ys.phdmama.viewmodel

import android.content.ContentValues
import android.content.Context
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
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import java.util.UUID
import javax.inject.Inject

@HiltViewModel
class FoodRegistrationViewModel @Inject constructor() :  ViewModel() {
    private val firestore = FirebaseFirestore.getInstance()
    private val auth = FirebaseAuth.getInstance()

    var foodName by mutableStateOf("")
    var hasReaction by mutableStateOf<Boolean?>(null)
    var reactionDetail by mutableStateOf("")
    var foodList by mutableStateOf<List<FoodReaction>>(emptyList())
        private set

    fun saveFoodReaction() {
        Log.d("FoodRegistrationVM", "saveFoodReaction called - Food: $foodName, HasReaction: $hasReaction")

        if (foodName.isBlank()) {
            Log.e("FoodRegistrationVM", "Food name is blank")
            return
        }
        if (hasReaction == null) {
            Log.e("FoodRegistrationVM", "HasReaction is null")
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

        Log.d("FoodRegistrationVM", "Creating food reaction: $foodReaction")

        val userId = auth.currentUser?.uid
        if (userId == null) {
            Log.e("FoodRegistrationVM", "User ID is null")
            return
        }

        val db = FirebaseFirestore.getInstance()

        db.collection("users").document(userId)
            .collection("food_reactions").document(foodId)
            .set(foodReaction)
            .addOnSuccessListener {
                Log.d("FoodRegistrationVM", "Food reaction saved successfully")
                // Clear form
                foodName = ""
                hasReaction = null
                reactionDetail = ""
            }
            .addOnFailureListener { e ->
                // Log error for debugging
                Log.e("FoodRegistrationVM", "Error saving food reaction", e)
            }
    }

    fun loadFoodReactions() {
        val userId = auth.currentUser?.uid ?: return
        firestore.collection("users").document(userId)
            .collection("food_reactions")
            .orderBy("date", Query.Direction.DESCENDING)
            .addSnapshotListener { snapshot, error ->
                if (error != null) {
                    Log.e("FoodRegistrationVM", "Error loading food reactions", error)
                    return@addSnapshotListener
                }

                if (snapshot != null) {
                    foodList = snapshot.documents.mapNotNull { doc ->
                        doc.toObject(FoodReaction::class.java)?.copy(id = doc.id)
                    }
                    Log.d("FoodRegistrationVM", "Loaded ${foodList.size} food reactions")
                } else {
                    foodList = emptyList()
                }
            }
    }

    fun updateFoodReaction(foodReaction: FoodReaction) {
        val userId = auth.currentUser?.uid ?: return

        firestore.collection("users").document(userId)
            .collection("food_reactions").document(foodReaction.id)
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
                val pageInfo = PdfDocument.PageInfo.Builder(595, 842, 1).create() // A4 size
                var page = pdfDocument.startPage(pageInfo)
                var canvas = page.canvas
                val paint = Paint()

                // Title
                paint.textSize = 20f
                paint.typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
                canvas.drawText("Reporte de Alimentos", 50f, 50f, paint)

                // Date
                paint.textSize = 12f
                paint.typeface = Typeface.create(Typeface.DEFAULT, Typeface.NORMAL)
                val currentDate = SimpleDateFormat("dd/MM/yyyy HH:mm", Locale.getDefault()).format(Date())
                canvas.drawText("Fecha de generación: $currentDate", 50f, 80f, paint)

                var yPosition = 120f
                val lineHeight = 20f
                val pageHeight = 792f // Leave margin at bottom
                var pageNumber = 1

                // Headers
                paint.typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
                canvas.drawText("Fecha", 50f, yPosition, paint)
                canvas.drawText("Alimento", 150f, yPosition, paint)
                canvas.drawText("Reacción", 350f, yPosition, paint)

                yPosition += lineHeight
                paint.typeface = Typeface.create(Typeface.DEFAULT, Typeface.NORMAL)

                // Draw line under headers
                canvas.drawLine(50f, yPosition, 545f, yPosition, paint)
                yPosition += lineHeight

                // Data rows
                foodList.forEach { food ->
                    // Check if we need a new page
                    if (yPosition > pageHeight) {
                        pdfDocument.finishPage(page)
                        pageNumber++
                        val newPageInfo = PdfDocument.PageInfo.Builder(595, 842, pageNumber).create()
                        page = pdfDocument.startPage(newPageInfo)
                        canvas = page.canvas
                        yPosition = 50f
                    }

                    // Format date
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

                    // If has reaction detail, add it on next line
                    if (food.hasReaction && food.reactionDetail.isNotEmpty()) {
                        if (yPosition > pageHeight) {
                            pdfDocument.finishPage(page)
                            pageNumber++
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

    fun getReactionsOnly(): List<FoodReaction> {
        return foodList.filter { it.hasReaction }
    }
}

data class FoodReaction(
    val id: String = "",
    val foodName: String = "",
    val hasReaction: Boolean = false,
    val reactionDetail: String = "",
    val date: String = ""
)
