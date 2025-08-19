package com.ys.phdmama.services

import android.app.*
import android.content.*
import android.os.*
import android.util.Log
import androidx.core.app.NotificationCompat
import com.ys.phdmama.R
import com.ys.phdmama.ui.main.MainActivity
import kotlinx.coroutines.*
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.asSharedFlow

class CounterService : Service() {

    private var counter = 0
    private var job: Job? = null
    private lateinit var sharedPreferences: SharedPreferences
    private val serviceScope = CoroutineScope(Dispatchers.Default + SupervisorJob())

    // SharedFlow to emit counter updates in real time
    private val _counterFlow = MutableSharedFlow<Int>(replay = 1)
    val counterFlow: SharedFlow<Int> = _counterFlow.asSharedFlow()

    companion object {
        private const val NOTIFICATION_ID = 1
        private const val CHANNEL_ID = "counter_channel"
    }

    override fun onCreate() {
        super.onCreate()
        Log.d("CounterService", "Service onCreate")
        sharedPreferences = getSharedPreferences("counter_prefs", Context.MODE_PRIVATE)
        counter = sharedPreferences.getInt("counter", 0)
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        Log.d("CounterService", "onStartCommand with action: ${intent?.action}")

        when (intent?.action) {
            "START" -> {
                startCounter()
                startForeground(NOTIFICATION_ID, createNotification())
            }
            "STOP" -> {
                stopCounter()
            }
        }
        return START_STICKY
    }

    private fun startCounter() {
        Log.d("CounterService", "Starting counter")

        // Cancel any existing job
        job?.cancel()

        // Reset counter when starting
        counter = 0
        sharedPreferences.edit().putInt("counter", counter).apply()
        _counterFlow.tryEmit(counter)

        job = serviceScope.launch {
            try {
                while (isActive) {
                    delay(1000)
                    counter++
                    sharedPreferences.edit().putInt("counter", counter).apply()
                    _counterFlow.tryEmit(counter)

                    // Update notification on main thread
                    withContext(Dispatchers.Main) {
                        updateNotification()
                    }

                    Log.d("CounterService", "Counter: $counter")
                }
            } catch (e: CancellationException) {
                Log.d("CounterService", "Counter job cancelled")
                throw e
            }
        }
    }

    private fun stopCounter() {
        Log.d("CounterService", "Stopping counter")
        job?.cancel()
        job = null
        stopForeground(STOP_FOREGROUND_REMOVE)
        stopSelf()
    }

    private fun createNotification(): Notification {
        // Create intent to open the app when notification is tapped
        val openAppIntent = Intent(this, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP
        }
        val openAppPendingIntent = PendingIntent.getActivity(
            this,
            0,
            openAppIntent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

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

        return NotificationCompat.Builder(this, CHANNEL_ID)
            .setContentTitle("Contador de sueño")
            .setContentText("Tiempo: ${formatTime(counter)}")
            .setSmallIcon(R.drawable.ic_counter) // Make sure this icon exists
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setCategory(NotificationCompat.CATEGORY_SERVICE)
            .setOngoing(true) // Keeps the notification persistent
            .setSilent(true) // Remove notification sound
            .setContentIntent(openAppPendingIntent)
            .addAction(R.drawable.ic_play, "Iniciar", playIntent) // Play button
            .addAction(R.drawable.ic_stop, "Detener", stopIntent) // Stop button
            .setVisibility(NotificationCompat.VISIBILITY_PUBLIC)
            .build()
    }

    private fun formatTime(seconds: Int): String {
        val minutes = seconds / 60
        val remainingSeconds = seconds % 60
        return if (minutes > 0) {
            "${minutes}m ${remainingSeconds}s"
        } else {
            "${seconds}s"
        }
    }

    private fun updateNotification() {
        val notificationManager = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        try {
            notificationManager.notify(NOTIFICATION_ID, createNotification())
        } catch (e: Exception) {
            Log.e("CounterService", "Error updating notification", e)
        }
    }

    override fun onDestroy() {
        Log.d("CounterService", "Service onDestroy")
        job?.cancel()
        serviceScope.cancel()
        super.onDestroy()
    }

    override fun onBind(intent: Intent?): IBinder {
        return CounterBinder()
    }

    inner class CounterBinder : Binder() {
        fun getService(): CounterService = this@CounterService
    }
}
