package com.ys.phdmama.viewmodel

import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.os.Build
import android.util.Log
import androidx.annotation.RequiresApi
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.google.firebase.Timestamp
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.ys.phdmama.repository.BabyPreferencesRepository
import com.ys.phdmama.services.CounterService
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter
import javax.inject.Inject

data class ContractionEntry(
    val start: LocalDateTime,
    val end: LocalDateTime,
    val durationSeconds: Long
)

data class ContractionInterval(
    val windowStart: LocalDateTime,
    val windowEnd: LocalDateTime,
    val frequency: Int,
    val contractions: List<ContractionEntry>
)

@HiltViewModel
class ContractionCounterViewModel @Inject constructor(
    @ApplicationContext private val context: Context,
    private val preferencesRepository: BabyPreferencesRepository
) : ViewModel() {

    private val auth = FirebaseAuth.getInstance()
    private val sharedPreferences = context.getSharedPreferences("counter_prefs", Context.MODE_PRIVATE)

    private val _counter = MutableStateFlow(sharedPreferences.getInt("counter", 0))
    val counter: StateFlow<Int> = _counter

    private val _isRunning = MutableStateFlow(false)
    val isRunning: StateFlow<Boolean> = _isRunning

    private val _selectedBaby = MutableStateFlow<String?>(null)
    val selectedBaby: StateFlow<String?> = _selectedBaby.asStateFlow()

    // Tracks all contraction entries in memory for the session
    private val _contractionEntries = MutableStateFlow<List<ContractionEntry>>(emptyList())

    // Add this new StateFlow for Firestore-loaded data
    private val _isLoadingReport = MutableStateFlow(false)
    val isLoadingReport: StateFlow<Boolean> = _isLoadingReport.asStateFlow()

    private val _firestoreIntervals = MutableStateFlow<List<ContractionInterval>>(emptyList())
    val firestoreIntervals: StateFlow<List<ContractionInterval>> = _firestoreIntervals.asStateFlow()

    // Grouped intervals (1 hour windows) exposed to the UI
    @RequiresApi(Build.VERSION_CODES.O)
    val contractionIntervals: StateFlow<List<ContractionInterval>> =
        _contractionEntries.map { groupIntoIntervals(it) }
            .stateIn(viewModelScope, SharingStarted.Lazily, emptyList())

    // Tracks when the current contraction started
    private var currentContractionStart: LocalDateTime? = null

    init {
        observeSelectedBabyFromDataStore()
        observeCounterChanges()
        _isRunning.value = sharedPreferences.getBoolean("counter_running", false)
    }

    private fun observeSelectedBabyFromDataStore() {
        viewModelScope.launch {
            preferencesRepository.selectedBabyIdFlow.collect { savedBabyId ->
                _selectedBaby.value = savedBabyId?.toString()
            }
        }
    }

    private fun observeCounterChanges() {
        viewModelScope.launch {
            callbackFlow {
                val listener = SharedPreferences.OnSharedPreferenceChangeListener { _, key ->
                    if (key == "counter") trySend(sharedPreferences.getInt("counter", 0))
                }
                sharedPreferences.registerOnSharedPreferenceChangeListener(listener)
                awaitClose { sharedPreferences.unregisterOnSharedPreferenceChangeListener(listener) }
            }.collect { newCounter -> _counter.value = newCounter }
        }
    }

    @RequiresApi(Build.VERSION_CODES.O)
    fun startCounter() {
        try {
            val intent = Intent(context, CounterService::class.java).apply { action = "START" }
            context.startForegroundService(intent)
            _isRunning.value = true
            sharedPreferences.edit().putBoolean("counter_running", true).apply()
            _counter.value = 0
            currentContractionStart = LocalDateTime.now()
        } catch (e: Exception) {
            Log.e("ContractionCounterViewModel", "Error starting counter service", e)
        }
    }

    @RequiresApi(Build.VERSION_CODES.O)
    fun stopCounter() {
        try {
            val intent = Intent(context, CounterService::class.java).apply { action = "STOP" }
            context.startService(intent)

            val endTime = LocalDateTime.now()
            val startTime = currentContractionStart

            if (startTime != null) {
                val durationSeconds = java.time.Duration.between(startTime, endTime).seconds
                val entry = ContractionEntry(startTime, endTime, durationSeconds)
                _contractionEntries.value = _contractionEntries.value + entry
                saveCounterTime(_counter.value, startTime, endTime)
            }

            _isRunning.value = false
            sharedPreferences.edit().putBoolean("counter_running", false).apply()
            currentContractionStart = null
        } catch (e: Exception) {
            Log.e("ContractionCounterViewModel", "Error stopping counter service", e)
        }
    }

    @RequiresApi(Build.VERSION_CODES.O)
    private fun groupIntoIntervals(entries: List<ContractionEntry>): List<ContractionInterval> {
        if (entries.isEmpty()) return emptyList()

        val intervals = mutableListOf<ContractionInterval>()
        val sorted = entries.sortedBy { it.start }

        var windowStart = sorted.first().start
        var windowEnd = windowStart.plusMinutes(60)
        var currentGroup = mutableListOf<ContractionEntry>()

        for (entry in sorted) {
            if (entry.start < windowEnd) {
                currentGroup.add(entry)
            } else {
                if (currentGroup.isNotEmpty()) {
                    intervals.add(ContractionInterval(windowStart, windowEnd, currentGroup.size, currentGroup.toList()))
                }
                windowStart = entry.start
                windowEnd = windowStart.plusMinutes(60)
                currentGroup = mutableListOf(entry)
            }
        }

        if (currentGroup.isNotEmpty()) {
            intervals.add(ContractionInterval(windowStart, windowEnd, currentGroup.size, currentGroup.toList()))
        }

        return intervals
    }

    @RequiresApi(Build.VERSION_CODES.O)
    private fun saveCounterTime(counterSeconds: Int, start: LocalDateTime, end: LocalDateTime) {
        val formatter = DateTimeFormatter.ofPattern("HH:mm")
        val userId = auth.currentUser?.uid ?: return
        val db = FirebaseFirestore.getInstance()
        val babyId = selectedBaby.value ?: return

        val data = hashMapOf<String, Any>(
            "time" to formatTime(counterSeconds.toLong()),
            "startTime" to start.atZone(java.time.ZoneId.systemDefault()).toInstant().toEpochMilli(),
            "endTime" to end.atZone(java.time.ZoneId.systemDefault()).toInstant().toEpochMilli(),
            "timestamp" to Timestamp.now()
        )

        db.collection("users").document(userId)
            .collection("babies").document(babyId)
            .collection("contraction_counter_time")
            .add(data)
            .addOnSuccessListener { Log.d("ContractionCounterViewModel", "Saved successfully") }
            .addOnFailureListener { Log.e("ContractionCounterViewModel", "Error saving", it) }
    }


    // Call this from the report screen
    @RequiresApi(Build.VERSION_CODES.O)
    fun loadContractionReport() {
        val userId = auth.currentUser?.uid ?: return
        val babyId = _selectedBaby.value ?: return

        _isLoadingReport.value = true

        val db = FirebaseFirestore.getInstance()
        db.collection("users")
            .document(userId)
            .collection("babies")
            .document(babyId)
            .collection("contraction_counter_time")
            .orderBy("timestamp")
            .get()
            .addOnSuccessListener { snapshot ->
                val entries = snapshot.documents.mapNotNull { doc ->
                    parseFirestoreEntry(doc)
                }
                _firestoreIntervals.value = groupIntoIntervals(entries)
                _isLoadingReport.value = false
                Log.d("ContractionCounterViewModel", "Loaded ${entries.size} entries from Firestore")
            }
            .addOnFailureListener { e ->
                Log.e("ContractionCounterViewModel", "Error loading report", e)
                _isLoadingReport.value = false
            }
    }

    @RequiresApi(Build.VERSION_CODES.O)
    private fun parseFirestoreEntry(doc: com.google.firebase.firestore.DocumentSnapshot): ContractionEntry? {
        return try {
            val startMillis = doc.getLong("startTime")
            val endMillis = doc.getLong("endTime")

            // Fallback to old HH:mm string format for existing documents
            if (startMillis != null && endMillis != null) {
                // New format: milliseconds
                val start = LocalDateTime.ofInstant(
                    java.time.Instant.ofEpochMilli(startMillis),
                    java.time.ZoneId.systemDefault()
                )
                val end = LocalDateTime.ofInstant(
                    java.time.Instant.ofEpochMilli(endMillis),
                    java.time.ZoneId.systemDefault()
                )
                val durationSeconds = java.time.Duration.between(start, end).seconds
                ContractionEntry(start, end, durationSeconds)
            } else {
                // Old format: HH:mm strings + timestamp for the date
                val timestamp = doc.getTimestamp("timestamp") ?: return null
                val startTimeStr = doc.getString("startTime") ?: return null
                val endTimeStr = doc.getString("endTime") ?: return null

                val date = timestamp.toDate()
                val calendar = java.util.Calendar.getInstance().apply { time = date }
                val year = calendar.get(java.util.Calendar.YEAR)
                val month = calendar.get(java.util.Calendar.MONTH) + 1
                val day = calendar.get(java.util.Calendar.DAY_OF_MONTH)

                val formatter = DateTimeFormatter.ofPattern("HH:mm")
                val startHour = java.time.LocalTime.parse(startTimeStr, formatter)
                val endHour = java.time.LocalTime.parse(endTimeStr, formatter)

                val start = LocalDateTime.of(year, month, day, startHour.hour, startHour.minute)
                val endDay = if (endHour.isBefore(startHour)) day + 1 else day
                val end = LocalDateTime.of(year, month, endDay, endHour.hour, endHour.minute)

                val durationSeconds = java.time.Duration.between(start, end).seconds
                ContractionEntry(start, end, durationSeconds)
            }
        } catch (e: Exception) {
            Log.e("ContractionCounterViewModel", "Error parsing document ${doc.id}", e)
            null
        }
    }

    private fun formatTime(durationSeconds: Long): String {
        val minutes = durationSeconds / 60
        val seconds = durationSeconds % 60
        return when {
            minutes >= 1 -> "$minutes minuto${if (minutes != 1L) "s" else ""}"
            else         -> "$seconds segundo${if (seconds != 1L) "s" else ""}"
        }
    }

    override fun onCleared() {
        super.onCleared()
        Log.d("ContractionCounterViewModel", "ViewModel cleared")
    }
}