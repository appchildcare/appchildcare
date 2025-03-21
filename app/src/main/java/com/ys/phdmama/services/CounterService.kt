package com.ys.phdmama.services

import android.app.*
import android.content.*
import android.os.*
import android.util.Log
import androidx.core.app.NotificationCompat
import com.ys.phdmama.R
import kotlinx.coroutines.*
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.asSharedFlow

class CounterService : Service() {

    private var counter = 0
    private var job: Job? = null
    private lateinit var sharedPreferences: SharedPreferences

    // SharedFlow to emit counter updates in real time
    private val _counterFlow = MutableSharedFlow<Int>(replay = 1)
    val counterFlow: SharedFlow<Int> = _counterFlow.asSharedFlow()

    override fun onCreate() {
        super.onCreate()
        sharedPreferences = getSharedPreferences("counter_prefs", Context.MODE_PRIVATE)
        counter = sharedPreferences.getInt("counter", 0)
        startForeground(1, createNotification())
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        when (intent?.action) {
            "START" -> startCounter()
            "STOP" -> stopCounter()
        }
        return START_STICKY
    }

    private fun startCounter() {
        if (job == null) {
            counter = 0 // Reset counter when starting
            sharedPreferences.edit().putInt("counter", counter).apply()

            job = CoroutineScope(Dispatchers.Main).launch {
                while (true) {
                    counter++
                    sharedPreferences.edit().putInt("counter", counter).apply()
                    updateNotification()
                    delay(1000)
                }
            }
        }
        Log.d("CounterService", "Service Started")
        startForeground(1, createNotification()) // Keep notification active
    }


    private fun stopCounter() {
        job?.cancel()
        job = null
        stopForeground(STOP_FOREGROUND_REMOVE)
        stopSelf()
    }

    private fun createNotification(): Notification {
        val playIntent = PendingIntent.getService(
            this,
            0,
            Intent(this, CounterService::class.java).setAction("START"),
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        val stopIntent = PendingIntent.getService(
            this,
            1,
            Intent(this, CounterService::class.java).setAction("STOP"),
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        return NotificationCompat.Builder(this, "counter_channel")
            .setContentTitle("Counter Running")
            .setContentText("Time: $counter sec")
            .setSmallIcon(R.drawable.ic_counter) // Make sure this icon exists
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setOngoing(true) // Keeps the notification persistent
            .setSilent(true) // Remove notification sound
            .setStyle(androidx.media.app.NotificationCompat.MediaStyle().setShowActionsInCompactView(0, 1))
            .addAction(R.drawable.ic_play, "Start", playIntent) // Play button
            .addAction(R.drawable.ic_stop, "Stop", stopIntent) // Stop button
            .build()
    }


    private fun updateNotification() {
        val notificationManager = getSystemService(NotificationManager::class.java)
        notificationManager.notify(1, createNotification())
    }

    override fun onBind(intent: Intent?): IBinder {
        return CounterBinder()
    }

    inner class CounterBinder : Binder() {
        fun getService(): CounterService = this@CounterService
    }
}

