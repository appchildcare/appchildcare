package com.ys.phdmama.workers

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.os.Build
import androidx.core.app.NotificationCompat
import androidx.work.Worker
import androidx.work.WorkerParameters

class PediatricVisitNotificationWorker (
    context: Context,
    workerParams: WorkerParameters
) : Worker(context, workerParams) {

    companion object {
        const val CHANNEL_ID = "pediatrician_visit_reminder_channel"
    }

    override fun doWork(): Result {
        val visitId = inputData.getString("visitId") ?: return Result.failure()
//        val medicineName = inputData.getString("medicineName") ?: return Result.failure()
        val nextVisit = inputData.getString("nextVisit") ?: return Result.failure()

        createNotificationChannel()
        showNotification(visitId, nextVisit)

        return Result.success()
    }

    private fun createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val name = "Pediatric Reminders"
            val descriptionText = "Notifications for next pediatrician visit reminders"
            val importance = NotificationManager.IMPORTANCE_HIGH
            val channel = NotificationChannel(CHANNEL_ID, name, importance).apply {
                description = descriptionText
                enableVibration(true)
                enableLights(true)
            }

            val notificationManager = applicationContext.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
            notificationManager.createNotificationChannel(channel)
        }
    }

    private fun showNotification(visitId: String, timeToTake: String) {
        // Create intent to open the app when notification is tapped
        val intent = applicationContext.packageManager.getLaunchIntentForPackage(applicationContext.packageName)
        intent?.apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
            putExtra("visitId", visitId)
        }

        val pendingIntent = PendingIntent.getActivity(
            applicationContext,
            visitId.hashCode(),
            intent!!,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        // Build the notification
        val notification = NotificationCompat.Builder(applicationContext, CHANNEL_ID)
            .setSmallIcon(android.R.drawable.ic_dialog_info) // Replace with your app icon
            .setContentTitle("Próxima visita al pediatra")
//            .setContentText("$medicineName - $timeToTake")
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setAutoCancel(true)
            .setContentIntent(pendingIntent)
            .setVibrate(longArrayOf(0, 500, 250, 500))
            .build()

        val notificationManager = applicationContext.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        notificationManager.notify(visitId.hashCode(), notification)
    }
}