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
import java.util.Calendar
import java.util.Date
import java.util.Locale
import java.util.UUID
import javax.inject.Inject

data class MedicineRecord(
    val id: String = "",
    val medicineName: String = "",
    val timeToTake: String = "",
    val notificationReminder: String = "No", // "Yes" or "No"
    val reminderDate: String = "",
    val date: String = ""
)

@HiltViewModel
class MedicineRegistrationViewModel @Inject constructor(): ViewModel() {
    private val firestore = FirebaseFirestore.getInstance()
    private val auth = FirebaseAuth.getInstance()

    var medicineName by mutableStateOf("")
    var selectedHour by mutableStateOf(12)
    var selectedMinute by mutableStateOf(0)
    var wantsReminder by mutableStateOf<Boolean?>(null)

    // Reminder date fields
    private val calendar = Calendar.getInstance()
    var reminderYear by mutableStateOf(calendar.get(Calendar.YEAR))
    var reminderMonth by mutableStateOf(calendar.get(Calendar.MONTH))
    var reminderDay by mutableStateOf(calendar.get(Calendar.DAY_OF_MONTH))

    var medicineList by mutableStateOf<List<MedicineRecord>>(emptyList())
        private set

    fun saveMedicineRecord() {
        Log.d("MedicineRegistrationVM", "saveMedicineRecord called - Medicine: $medicineName")

        if (medicineName.isBlank()) {
            Log.e("MedicineRegistrationVM", "Medicine name is blank")
            return
        }

        val medicineId = UUID.randomUUID().toString()
        val formatter = SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault())
        val currentDate = formatter.format(Date())

        val timeToTake = String.format("%02d:%02d", selectedHour, selectedMinute)

        // Format reminder date if user wants reminder
        val reminderDateStr = if (wantsReminder == true) {
            String.format("%04d-%02d-%02d", reminderYear, reminderMonth + 1, reminderDay)
        } else {
            ""
        }

        val medicineRecord = MedicineRecord(
            id = medicineId,
            medicineName = medicineName,
            timeToTake = timeToTake,
            notificationReminder = if (wantsReminder == true) "Yes" else "No",
            reminderDate = reminderDateStr,
            date = currentDate
        )

        Log.d("MedicineRegistrationVM", "Creating medicine record: $medicineRecord")

        val userId = auth.currentUser?.uid
        if (userId == null) {
            Log.e("MedicineRegistrationVM", "User ID is null")
            return
        }

        firestore.collection("users").document(userId)
            .collection("medicine_registration").document(medicineId)
            .set(medicineRecord)
            .addOnSuccessListener {
                Log.d("MedicineRegistrationVM", "Medicine record saved successfully")

                // Schedule notification if reminder is enabled
                if (wantsReminder == true) {
                    scheduleNotification(medicineRecord)
                }

                // Clear form
                clearForm()
            }
            .addOnFailureListener { e ->
                Log.e("MedicineRegistrationVM", "Error saving medicine record", e)
            }
    }

    private fun scheduleNotification(medicineRecord: MedicineRecord) {
        // This method schedules the push notification
        // You'll need to implement this based on your notification service
        // (WorkManager, AlarmManager, or Firebase Cloud Messaging)

        Log.d("MedicineRegistrationVM", "Scheduling notification for ${medicineRecord.medicineName} at ${medicineRecord.reminderDate} ${medicineRecord.timeToTake}")

        // Example implementation with WorkManager:
        /*
        val data = workDataOf(
            "medicineId" to medicineRecord.id,
            "medicineName" to medicineRecord.medicineName,
            "timeToTake" to medicineRecord.timeToTake
        )

        // Parse reminder date and time
        val dateFormat = SimpleDateFormat("yyyy-MM-dd HH:mm", Locale.getDefault())
        val reminderDateTime = dateFormat.parse("${medicineRecord.reminderDate} ${medicineRecord.timeToTake}")

        if (reminderDateTime != null) {
            val delay = reminderDateTime.time - System.currentTimeMillis()

            if (delay > 0) {
                val notificationWork = OneTimeWorkRequestBuilder<MedicineNotificationWorker>()
                    .setInitialDelay(delay, TimeUnit.MILLISECONDS)
                    .setInputData(data)
                    .addTag("medicine_reminder_${medicineRecord.id}")
                    .build()

                WorkManager.getInstance(context).enqueue(notificationWork)
                Log.d("MedicineRegistrationVM", "Notification scheduled for $delay milliseconds from now")
            }
        }
        */

        // Example implementation with AlarmManager:
        /*
        val intent = Intent(context, MedicineReminderReceiver::class.java).apply {
            putExtra("medicineId", medicineRecord.id)
            putExtra("medicineName", medicineRecord.medicineName)
            putExtra("timeToTake", medicineRecord.timeToTake)
        }

        val pendingIntent = PendingIntent.getBroadcast(
            context,
            medicineRecord.id.hashCode(),
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as AlarmManager
        val dateFormat = SimpleDateFormat("yyyy-MM-dd HH:mm", Locale.getDefault())
        val reminderDateTime = dateFormat.parse("${medicineRecord.reminderDate} ${medicineRecord.timeToTake}")

        if (reminderDateTime != null) {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                alarmManager.setExactAndAllowWhileIdle(
                    AlarmManager.RTC_WAKEUP,
                    reminderDateTime.time,
                    pendingIntent
                )
            } else {
                alarmManager.setExact(
                    AlarmManager.RTC_WAKEUP,
                    reminderDateTime.time,
                    pendingIntent
                )
            }
            Log.d("MedicineRegistrationVM", "Alarm set for ${reminderDateTime.time}")
        }
        */
    }

    fun loadMedicineRecords() {
        val userId = auth.currentUser?.uid ?: return
        firestore.collection("users").document(userId)
            .collection("medicine_registration")
            .orderBy("date", Query.Direction.DESCENDING)
            .addSnapshotListener { snapshot, error ->
                if (error != null) {
                    Log.e("MedicineRegistrationVM", "Error loading medicine records", error)
                    return@addSnapshotListener
                }

                if (snapshot != null) {
                    medicineList = snapshot.documents.mapNotNull { doc ->
                        doc.toObject(MedicineRecord::class.java)?.copy(id = doc.id)
                    }
                    Log.d("MedicineRegistrationVM", "Loaded ${medicineList.size} medicine records")
                } else {
                    medicineList = emptyList()
                }
            }
    }

    fun updateMedicineRecord(medicineRecord: MedicineRecord) {
        val userId = auth.currentUser?.uid ?: return

        firestore.collection("users").document(userId)
            .collection("medicine_registration").document(medicineRecord.id)
            .set(medicineRecord)
            .addOnSuccessListener {
                Log.d("MedicineRegistrationVM", "Medicine record updated successfully")
                loadMedicineRecords()
            }
            .addOnFailureListener { e ->
                Log.e("MedicineRegistrationVM", "Error updating medicine record", e)
            }
    }

    fun deleteMedicineRecord(medicineRecord: MedicineRecord) {
        val userId = auth.currentUser?.uid ?: return

        firestore.collection("users").document(userId)
            .collection("medicine_registration").document(medicineRecord.id)
            .delete()
            .addOnSuccessListener {
                Log.d("MedicineRegistrationVM", "Medicine record deleted successfully")
                // Cancel scheduled notification if exists
                cancelNotification(medicineRecord)
            }
            .addOnFailureListener { e ->
                Log.e("MedicineRegistrationVM", "Error deleting medicine record", e)
            }
    }

    private fun cancelNotification(medicineRecord: MedicineRecord) {
        // Cancel the scheduled notification
        Log.d("MedicineRegistrationVM", "Canceling notification for ${medicineRecord.medicineName}")

        // Example with WorkManager:
        /*
        WorkManager.getInstance(context)
            .cancelAllWorkByTag("medicine_reminder_${medicineRecord.id}")
        */

        // Example with AlarmManager:
        /*
        val intent = Intent(context, MedicineReminderReceiver::class.java)
        val pendingIntent = PendingIntent.getBroadcast(
            context,
            medicineRecord.id.hashCode(),
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )
        val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as AlarmManager
        alarmManager.cancel(pendingIntent)
        */
    }

    private fun clearForm() {
        medicineName = ""
        selectedHour = 12
        selectedMinute = 0
        wantsReminder = null
        val calendar = Calendar.getInstance()
        reminderYear = calendar.get(Calendar.YEAR)
        reminderMonth = calendar.get(Calendar.MONTH)
        reminderDay = calendar.get(Calendar.DAY_OF_MONTH)
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
                canvas.drawText("Reporte de Medicinas", 50f, 50f, paint)

                // Date
                paint.textSize = 12f
                paint.typeface = Typeface.create(Typeface.DEFAULT, Typeface.NORMAL)
                val currentDate = SimpleDateFormat("dd/MM/yyyy HH:mm", Locale.getDefault()).format(Date())
                canvas.drawText("Fecha de generación: $currentDate", 50f, 80f, paint)

                var yPosition = 120f
                val lineHeight = 20f
                val pageHeight = 792f
                var pageNumber = 1

                // Headers
                paint.typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
                canvas.drawText("Fecha", 50f, yPosition, paint)
                canvas.drawText("Medicina", 150f, yPosition, paint)
                canvas.drawText("Hora", 320f, yPosition, paint)
                canvas.drawText("Recordatorio", 400f, yPosition, paint)

                yPosition += lineHeight
                paint.typeface = Typeface.create(Typeface.DEFAULT, Typeface.NORMAL)

                // Draw line under headers
                canvas.drawLine(50f, yPosition, 545f, yPosition, paint)
                yPosition += lineHeight

                // Data rows
                medicineList.forEach { medicine ->
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
                        val date = SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault()).parse(medicine.date)
                        SimpleDateFormat("dd/MM/yyyy", Locale.getDefault()).format(date ?: Date())
                    } catch (e: Exception) {
                        medicine.date
                    }

                    canvas.drawText(formattedDate, 50f, yPosition, paint)
                    canvas.drawText(medicine.medicineName, 150f, yPosition, paint)
                    canvas.drawText(medicine.timeToTake, 320f, yPosition, paint)

                    val reminderText = if (medicine.notificationReminder == "Yes") {
                        "Sí"
                    } else {
                        "No"
                    }
                    canvas.drawText(reminderText, 400f, yPosition, paint)
                    yPosition += lineHeight

                    // If has reminder, add reminder date on next line
                    if (medicine.notificationReminder == "Yes" && medicine.reminderDate.isNotEmpty()) {
                        if (yPosition > pageHeight) {
                            pdfDocument.finishPage(page)
                            pageNumber++
                            val newPageInfo = PdfDocument.PageInfo.Builder(595, 842, pageNumber).create()
                            page = pdfDocument.startPage(newPageInfo)
                            canvas = page.canvas
                            yPosition = 50f
                        }

                        paint.textSize = 10f
                        // Format reminder date for display
                        val formattedReminderDate = try {
                            val date = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).parse(medicine.reminderDate)
                            SimpleDateFormat("dd/MM/yyyy", Locale.getDefault()).format(date ?: Date())
                        } catch (e: Exception) {
                            medicine.reminderDate
                        }
                        canvas.drawText("  Fecha recordatorio: $formattedReminderDate", 150f, yPosition, paint)
                        paint.textSize = 12f
                        yPosition += lineHeight
                    }
                }

                pdfDocument.finishPage(page)

                // Save the document
                val fileName = "Reporte_Medicinas_${SimpleDateFormat("yyyyMMdd_HHmmss", Locale.getDefault()).format(Date())}.pdf"
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
                Log.e("MedicineRegistrationVM", "Error generating PDF", e)
                withContext(Dispatchers.Main) {
                    Toast.makeText(context, "Error al generar PDF: ${e.message}", Toast.LENGTH_LONG).show()
                }
            }
        }
    }
}