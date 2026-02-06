package com.ys.phdmama.viewmodel

import android.annotation.SuppressLint
import android.app.AlarmManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.os.Build
import android.util.Log
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.work.OneTimeWorkRequestBuilder
import androidx.work.WorkManager
import androidx.work.workDataOf
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.ys.phdmama.model.PediatricianVisit
import com.ys.phdmama.repository.BabyPreferencesRepository
import com.ys.phdmama.workers.PediatricVisitNotificationReceiver
import com.ys.phdmama.workers.PediatricVisitNotificationWorker
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale
import java.util.UUID
import java.util.concurrent.TimeUnit
import javax.inject.Inject

@HiltViewModel
class PediatricVisitViewModel @Inject constructor(
    private val preferencesRepository: BabyPreferencesRepository,
    @ApplicationContext private val context: Context
): ViewModel() {
    private val firestore = FirebaseFirestore.getInstance()
    private val auth = FirebaseAuth.getInstance()
    var visitDataList by mutableStateOf<List<PediatricianVisit>>(emptyList())
        private set

    private val calendar = Calendar.getInstance()
    var reminderYear by mutableStateOf(calendar.get(Calendar.YEAR))
    var reminderMonth by mutableStateOf(calendar.get(Calendar.MONTH))
    var reminderDay by mutableStateOf(calendar.get(Calendar.DAY_OF_MONTH))
    var reminderHour by mutableStateOf(Calendar.getInstance().get(Calendar.HOUR_OF_DAY))
    var reminderMinute by mutableStateOf(Calendar.getInstance().get(Calendar.MINUTE))
    private val _selectedBaby = MutableStateFlow<String?>(null)
    val selectedBaby: StateFlow<String?> = _selectedBaby.asStateFlow()

    init {
        observeSelectedBabyFromDataStore()
    }

    private fun observeSelectedBabyFromDataStore() {
        viewModelScope.launch {
            preferencesRepository.selectedBabyIdFlow.collect { savedBabyId ->
                if (savedBabyId != null) {
                    _selectedBaby.value = savedBabyId.toString()
                } else {
                    Log.d("PediatricVisitViewModel", "Saved baby ID not found in list")
                }
            }
        }
    }

    fun saveVisit(
        date: String,
        notes: String,
        weight: String,
        height: String,
        headCircumference: String,
        nextVisit: String?,
        onSuccess: () -> Unit,
        onError: (String) -> Unit
    ) {
        val selectedBaby = selectedBaby.value
        val visitId = UUID.randomUUID().toString()
        val formatter = SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault())
        val currentDate = formatter.format(Date())

        val pediatricianVisit = PediatricianVisit(id = visitId,
            date = date,
            notes = notes,
            weight = weight,
            height = height,
            headCircumference = headCircumference,
            timestamp = currentDate,
            nextVisit = nextVisit
        )

        val userId = auth.currentUser?.uid ?: return
        val db = FirebaseFirestore.getInstance()

        if (selectedBaby != null) {
            db.collection("users")
                .document(userId)
                .collection("babies")
                .document(selectedBaby)
                .collection("pediatrician_visit_questions")
                .add(pediatricianVisit)
                .addOnSuccessListener {
                    if (pediatricianVisit.nextVisit?.isNotEmpty() == true) {
                        scheduleNotification(pediatricianVisit)
                    }
                    onSuccess()
                }
                .addOnFailureListener { e -> onError(e.message ?: "Error desconocido") }
        }
    }

    fun loadPediatricianVisits() {
        val userId = auth.currentUser?.uid ?: return

        firestore.collection("users")
            .document(userId)
            .collection("babies")
            .document(selectedBaby.value.toString())
            .collection("pediatrician_visit_questions")
            // .orderBy("timestamp", Query.Direction.DESCENDING)
            .addSnapshotListener { snapshot, exception ->

                // Handle Firestore exceptions
                if (exception != null) {
                    Log.e("PediatricianVisits", "Error loading visits: ${exception.message}", exception)
                    // Initialize empty list on error
                    visitDataList = emptyList()
                    return@addSnapshotListener
                }

                // Handle null snapshot
                if (snapshot == null) {
                    Log.d("PediatricianVisits", "Snapshot is null")
                    visitDataList = emptyList()
                    return@addSnapshotListener
                }

                // Handle empty collection (collection doesn't exist or has no documents)
                if (snapshot.isEmpty) {
                    Log.d("PediatricianVisits", "Collection is empty or doesn't exist")
                    visitDataList = emptyList()
                    return@addSnapshotListener
                }

                // Safe mapping with additional null checks
                try {
                    visitDataList = snapshot.documents.mapNotNull { doc ->
                        try {
                            doc.toObject(PediatricianVisit::class.java)?.copy(id = doc.id)
                        } catch (e: Exception) {
                            Log.e("PediatricianVisits", "Error parsing document ${doc.id}: ${e.message}")
                            null // Skip this document if parsing fails
                        }
                    }
                    Log.d("PediatricianVisits", "Loaded ${visitDataList.size} visits")
                } catch (e: Exception) {
                    Log.e("PediatricianVisits", "Error processing snapshot: ${e.message}", e)
                    visitDataList = emptyList()
                }
            }
    }
    fun update(pediatrician: PediatricianVisit) {
        val userId = auth.currentUser?.uid ?: return

        firestore.collection("users").document(userId)
            .collection("pediatrician_visit_questions").document(pediatrician.id)
            .set(pediatrician)
    }


    @SuppressLint("ServiceCast")
    private fun scheduleNotification(pediatrician: PediatricianVisit) {
        Log.d("PediatricianVM", "Scheduling notification for ${pediatrician.nextVisit}")

        val data = workDataOf(
            "visitId" to pediatrician.id,
            "nextVisit" to pediatrician.nextVisit
        )

        // Parse reminder date and time
        val dateFormat = SimpleDateFormat("yyyy-MM-dd HH:mm", Locale.getDefault())
        val reminderDateTime = dateFormat.parse("${pediatrician.nextVisit}")

        if (reminderDateTime != null) {
            // Subtract 1 day (24 hours) from the reminder date
            val oneDayInMillis = 24 * 60 * 60 * 1000L // 24 hours in milliseconds
            val notificationTime = reminderDateTime.time - oneDayInMillis
            val delay = notificationTime - System.currentTimeMillis()

            if (delay > 0) {
                // WorkManager notification
                val notificationWork = OneTimeWorkRequestBuilder<PediatricVisitNotificationWorker>()
                    .setInitialDelay(delay, TimeUnit.MILLISECONDS)
                    .setInputData(data)
                    .addTag("pediatrician_visit_reminder_${pediatrician.id}")
                    .build()

                WorkManager.getInstance(context).enqueue(notificationWork)

                val notificationDate = Date(notificationTime)
                Log.d("PediatricianVM", "Notification scheduled for ${dateFormat.format(notificationDate)} (1 day before visit)")
            } else {
                Log.d("PediatricianVM", "Notification time is in the past, not scheduling")
            }

            // AlarmManager notification (also 1 day before)
            val intent = Intent(context, PediatricVisitNotificationReceiver::class.java).apply {
                putExtra("visitId", pediatrician.id)
                putExtra("nextVisit", pediatrician.nextVisit)
            }

            val pendingIntent = PendingIntent.getBroadcast(
                context,
                pediatrician.id.hashCode(),
                intent,
                PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
            )

            val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as AlarmManager

            // Only set alarm if the notification time is in the future
            if (delay > 0) {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                    alarmManager.setExactAndAllowWhileIdle(
                        AlarmManager.RTC_WAKEUP,
                        notificationTime, // Use notificationTime instead of reminderDateTime.time
                        pendingIntent
                    )
                } else {
                    alarmManager.setExact(
                        AlarmManager.RTC_WAKEUP,
                        notificationTime,
                        pendingIntent
                    )
                }
                Log.d("PediatricianVM", "Alarm set for ${Date(notificationTime)} (1 day before)")
            }
        }
    }
}
