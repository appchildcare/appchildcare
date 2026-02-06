package com.ys.phdmama.workers

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.os.Build
import androidx.core.app.NotificationCompat

class PediatricVisitNotificationReceiver  : BroadcastReceiver() {

    companion object {
        const val CHANNEL_ID = "pediatrician_visit_reminder_channel"
    }

    override fun onReceive(context: Context, intent: Intent) {
        val visitId = intent.getStringExtra("visitId") ?: return
//        val medicineName = intent.getStringExtra("medicineName") ?: return
        val nextVisit = intent.getStringExtra("nextVisit") ?: return

        createNotificationChannel(context)
        showNotification(context, visitId, nextVisit)
    }

    private fun createNotificationChannel(context: Context) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val name = "Pediatric Reminders"
            val descriptionText = "Notifications for next pediatrician visit reminders"
            val importance = NotificationManager.IMPORTANCE_HIGH
            val channel = NotificationChannel(CHANNEL_ID, name, importance).apply {
                description = descriptionText
                enableVibration(true)
                enableLights(true)
            }

            val notificationManager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
            notificationManager.createNotificationChannel(channel)
        }
    }

    private fun showNotification(context: Context, visitId: String, nextVisit: String) {
        // Create intent to open the app when notification is tapped
        val intent = context.packageManager.getLaunchIntentForPackage(context.packageName)
        intent?.apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
            putExtra("visitId", visitId)
        }

        val pendingIntent = PendingIntent.getActivity(
            context,
            visitId.hashCode(),
            intent!!,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        // Build the notification
        val notification = NotificationCompat.Builder(context, CHANNEL_ID)
            .setSmallIcon(android.R.drawable.ic_dialog_info) // Replace with your app icon
            .setContentTitle("Próxima visita al pediatra")
//            .setContentText("$medicineName - $timeToTake")
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setAutoCancel(true)
            .setContentIntent(pendingIntent)
            .setVibrate(longArrayOf(0, 500, 250, 500))
            .build()

        val notificationManager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        notificationManager.notify(visitId.hashCode(), notification)
    }
}