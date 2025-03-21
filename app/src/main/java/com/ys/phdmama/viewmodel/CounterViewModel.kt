package com.ys.phdmama.viewmodel

import android.app.Application
import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.os.Build
import androidx.annotation.RequiresApi
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.ys.phdmama.services.CounterService
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch

class CounterViewModel(application: Application) : AndroidViewModel(application) {
    private val sharedPreferences = application.getSharedPreferences("counter_prefs", Context.MODE_PRIVATE)
    private val _counter = MutableStateFlow(sharedPreferences.getInt("counter", 0))
    val counter: StateFlow<Int> = _counter

    init {
        observeCounterChanges()
    }

    private fun observeCounterChanges() {
        viewModelScope.launch {
            callbackFlow {
                val listener = SharedPreferences.OnSharedPreferenceChangeListener { _, key ->
                    if (key == "counter") {
                        trySend(sharedPreferences.getInt("counter", 0))
                    }
                }
                sharedPreferences.registerOnSharedPreferenceChangeListener(listener)
                awaitClose { sharedPreferences.unregisterOnSharedPreferenceChangeListener(listener) }
            }.collect {
                _counter.value = it
            }
        }
    }

    @RequiresApi(Build.VERSION_CODES.O)
    fun startCounter() {
        val context = getApplication<Application>().applicationContext
        val intent = Intent(context, CounterService::class.java).setAction("START")
        context.startForegroundService(intent)

        // Reset UI counter
        _counter.value = 0
    }

    fun stopCounter() {
        val context = getApplication<Application>().applicationContext
        val intent = Intent(context, CounterService::class.java).setAction("STOP")
        context.startService(intent)
    }
}


