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
class CounterViewModel @Inject constructor(
    @ApplicationContext private val context: Context
) : ViewModel() {

    private val auth = FirebaseAuth.getInstance()

    private val sharedPreferences: SharedPreferences =
        context.getSharedPreferences("counter_prefs", Context.MODE_PRIVATE)

    private val _counter = MutableStateFlow(sharedPreferences.getInt("counter", 0))
    val counter: StateFlow<Int> = _counter

    private val _isRunning = MutableStateFlow(false)
    val isRunning: StateFlow<Boolean> = _isRunning

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
                            Log.d("CounterViewModel", "Counter running state changed: $isRunning")
                        }
                    }
                }
                sharedPreferences.registerOnSharedPreferenceChangeListener(listener)
                awaitClose {
                    sharedPreferences.unregisterOnSharedPreferenceChangeListener(listener)
                }
            }.collect { newCounter ->
                _counter.value = newCounter
                Log.d("CounterViewModel", "Counter updated to: $newCounter")
            }
        }
    }

    @RequiresApi(Build.VERSION_CODES.O)
    fun startCounter() {
        Log.d("CounterViewModel", "Starting counter service")

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
            Log.e("CounterViewModel", "Error starting counter service", e)
        }
    }

    fun stopCounter(babyId: String?) {
        Log.d("CounterViewModel", "Stopping counter service")

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
            Log.e("CounterViewModel", "Error stopping counter service", e)
        }
    }

    override fun onCleared() {
        super.onCleared()
        Log.d("CounterViewModel", "ViewModel cleared")
    }

    private fun saveCounterTime(counterSeconds: Int, babyId: String?) {
        val formattedTime = formatTime(counterSeconds)
        val timestamp = Timestamp.now()

        val userId = auth.currentUser?.uid ?: return
        val db = FirebaseFirestore.getInstance()

        if (babyId != null) {
            val napData = hashMapOf(
                "time" to formattedTime,
                "timestamp" to timestamp
            )

            db.collection("users")
                .document(userId)
                .collection("babies")
                .document(babyId)
                .collection("nap_counter_time")
                .add(napData)
                .addOnSuccessListener {
                    Log.d("CounterViewModel", "Nap counter saved successfully")
                }
                .addOnFailureListener { e ->
                    Log.e("CounterViewModel", "Error saving nap counter", e)
                }
        } else {
            Log.e("CounterViewModel", "UserId or BabyId is null, cannot save nap data")
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
