package com.ys.phdmama.viewmodel

import android.app.Application
import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.os.Build
import android.util.Log
import androidx.annotation.RequiresApi
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.google.firebase.Timestamp
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.ys.phdmama.services.CounterService
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class LactationViewModel @Inject constructor(
    @ApplicationContext private val context: Context
) : ViewModel() {
    private val auth = FirebaseAuth.getInstance()

    private val sharedPreferences = context.getSharedPreferences("counter_prefs", Context.MODE_PRIVATE)
    private val _counter = MutableStateFlow(sharedPreferences.getInt("counter", 0))
    val counter: StateFlow<Int> = _counter

    private val _isRunning = MutableStateFlow(false)
    val isRunning: StateFlow<Boolean> = _isRunning

    private val _selectedLactationType = MutableStateFlow(
        sharedPreferences.getString("selected_lactation_type", "Leche natural") ?: "Leche natural"
    )
    val selectedLactationType: StateFlow<String> = _selectedLactationType

    init {
        observeCounterChanges()
        // Load initial running state
        _isRunning.value = sharedPreferences.getBoolean("counter_running", false)
    }

    private fun observeCounterChanges() {
        viewModelScope.launch {
            callbackFlow {
                val listener = SharedPreferences.OnSharedPreferenceChangeListener { _, key ->
                    when (key) {
                        "counter" -> {
                            trySend(sharedPreferences.getInt("counter", 0))
                        }
                        "counter_running" -> {
                            val isRunning = sharedPreferences.getBoolean("counter_running", false)
                            Log.d("LactationViewModel", "Counter running state changed: $isRunning")
                        }
                    }
                }
                sharedPreferences.registerOnSharedPreferenceChangeListener(listener)
                awaitClose {
                    sharedPreferences.unregisterOnSharedPreferenceChangeListener(listener)
                }
            }.collect { newCounter ->
                _counter.value = newCounter
                Log.d("LactationViewModel", "Counter updated to: $newCounter")
            }
        }
    }

    fun setLactationType(type: String) {
        _selectedLactationType.value = type
        sharedPreferences.edit().putString("selected_lactation_type", type).apply()
        Log.d("LactationViewModel", "Lactation type set to: $type")
    }

    @RequiresApi(Build.VERSION_CODES.O)
    fun startCounter() {
        Log.d("LactationViewModel", "Starting counter service")
//        val context = getApplication<Application>().Application

        try {
            val intent = Intent(context, CounterService::class.java).apply {
                action = "START"
            }
            context.startForegroundService(intent)

            // Update running state
            _isRunning.value = true
            sharedPreferences.edit().putBoolean("counter_running", true).apply()

            // Reset UI counter
            _counter.value = 0
        } catch (e: Exception) {
            Log.e("LactationViewModel", "Error starting counter service", e)
        }
    }

    fun stopCounter(babyId: String?) {
        Log.d("CounterViewModel", "Stopping counter service")
//        val context = getApplication<Application>().applicationContext

        try {
            val intent = Intent(context, CounterService::class.java).apply {
                action = "STOP"
            }
            context.startService(intent)

            // Save the counter time to Firestore before resetting
            saveCounterTime(
                _counter.value,
                babyId
            )

            // Update running state
            _isRunning.value = false
            sharedPreferences.edit().putBoolean("counter_running", false).apply()
        } catch (e: Exception) {
            Log.e("LactationViewModel", "Error stopping counter service", e)
        }
    }

    override fun onCleared() {
        super.onCleared()
        Log.d("LactationViewModel", "ViewModel cleared")
    }

    private fun saveCounterTime(counterSeconds: Int, babyId: String?) {
        val formattedTime = formatTime(counterSeconds)
        val timestamp = Timestamp.now()

        // Convert lactation type to the required format
        val lactationType = when (_selectedLactationType.value) {
            "Leche natural" -> "natural"
            "Leche de fórmula" -> "formula"
            else -> "natural" // default fallback
        }

        val userId = auth.currentUser?.uid ?: return
        val db = FirebaseFirestore.getInstance()

        if (babyId != null) {
            val lactationData = hashMapOf(
                "time" to formattedTime,
                "timestamp" to timestamp,
                "lactancy_type" to lactationType
            )

            db.collection("users")
                .document(userId)
                .collection("babies")
                .document(babyId)
                .collection("lactation_counter_time")
                .add(lactationData)
                .addOnSuccessListener {
                    Log.d("LactationViewModel", "Lactation counter saved successfully with type: $lactationType")
                }
                .addOnFailureListener { e ->
                    Log.e("LactationViewModel", "Error saving lactation counter", e)
                }
        } else {
            Log.e("LactationViewModel", "UserId or BabyId is null, cannot save lactation data")
        }
    }

    private fun formatTime(seconds: Int): String {
        val hours = seconds / 3600
        val minutes = (seconds % 3600) / 60
        val remainingSeconds = seconds % 60

        return when {
            hours > 0 -> String.format("%02d:%02d:%02d", hours, minutes, remainingSeconds)
            minutes > 0 -> String.format("%02d:%02d", minutes, remainingSeconds)
            else -> String.format("00:%02d", seconds)
        }
    }
}
