
package com.ys.cunaco.utils

import android.app.Activity
import android.app.AlertDialog
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Build
import android.provider.Settings
import androidx.core.app.NotificationManagerCompat
import com.ys.cunaco.R

object NotificationPermissionHelper {

    // Nombre real del canal de notificaciones (no se traduce)
    private const val CHANNEL_NAME = "Counter Service"
    private const val APP_NAME = "CunaCo"

    /**
     * Check if notifications are enabled for the app
     */
    fun areNotificationsEnabled(context: Context): Boolean {
        return NotificationManagerCompat
            .from(context)
            .areNotificationsEnabled()
    }

    /**
     * Show dialog explaining why notifications are needed
     * and how to enable them
     */
    fun showNotificationSettingsDialog(activity: Activity) {
        AlertDialog.Builder(activity)
            .setTitle(activity.getString(R.string.notif_disabled_title))
            .setMessage(activity.getString(R.string.notif_disabled_message, CHANNEL_NAME))
            .setPositiveButton(activity.getString(R.string.notif_go_to_settings)) { _, _ ->
                openNotificationSettings(activity)
            }
            .setNegativeButton(activity.getString(R.string.notif_later), null)
            .setCancelable(false)
            .show()
    }

    /**
     * Open notification settings for the app
     */
    private fun openNotificationSettings(activity: Activity) {
        try {
            val intent = Intent().apply {
                when {
                    Build.VERSION.SDK_INT >= Build.VERSION_CODES.O -> {
                        action = Settings.ACTION_APP_NOTIFICATION_SETTINGS
                        putExtra(
                            Settings.EXTRA_APP_PACKAGE,
                            activity.packageName
                        )
                    }

                    Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP -> {
                        action = "android.settings.APP_NOTIFICATION_SETTINGS"
                        putExtra("app_package", activity.packageName)
                        putExtra("app_uid", activity.applicationInfo.uid)
                    }

                    else -> {
                        action = Settings.ACTION_APPLICATION_DETAILS_SETTINGS
                        data = Uri.parse("package:${activity.packageName}")
                    }
                }
            }

            activity.startActivity(intent)

        } catch (e: Exception) {
            openAppSettings(activity)
        }
    }

    /**
     * Open general app settings as fallback
     */
    private fun openAppSettings(activity: Activity) {
        try {
            val intent = Intent(
                Settings.ACTION_APPLICATION_DETAILS_SETTINGS
            ).apply {
                data = Uri.parse("package:${activity.packageName}")
            }

            activity.startActivity(intent)

        } catch (e: Exception) {
            val intent = Intent(Settings.ACTION_SETTINGS)
            activity.startActivity(intent)
        }
    }

    /**
     * Show step-by-step instructions based on device manufacturer
     */
    fun showDeviceSpecificInstructions(activity: Activity) {
        val manufacturer = Build.MANUFACTURER.lowercase()

        val instructions = getDeviceSpecificInstructions(activity, manufacturer)

        AlertDialog.Builder(activity)
            .setTitle(activity.getString(R.string.notif_enable_title))
            .setMessage(instructions)
            .setPositiveButton(activity.getString(R.string.notif_go_to_settings)) { _, _ ->
                openNotificationSettings(activity)
            }
            .setNegativeButton(activity.getString(R.string.notif_understood), null)
            .show()
    }

    /**
     * Get device-specific instructions for enabling notifications
     */
    private fun getDeviceSpecificInstructions(
        context: Context,
        manufacturer: String
    ): String {
        return when {
            manufacturer.contains("samsung") ->
                context.getString(R.string.notif_instructions_samsung, APP_NAME, CHANNEL_NAME)

            manufacturer.contains("xiaomi") ->
                context.getString(R.string.notif_instructions_xiaomi, APP_NAME)

            manufacturer.contains("huawei") ->
                context.getString(R.string.notif_instructions_huawei, APP_NAME)

            manufacturer.contains("oppo") ->
                context.getString(R.string.notif_instructions_oppo, APP_NAME)

            manufacturer.contains("vivo") ->
                context.getString(R.string.notif_instructions_vivo, APP_NAME)

            manufacturer.contains("oneplus") ->
                context.getString(R.string.notif_instructions_oneplus, APP_NAME)

            else ->
                context.getString(R.string.notif_instructions_general, APP_NAME, CHANNEL_NAME)
        }
    }

    /**
     * Check notifications and show appropriate dialog if needed
     */
    fun checkAndRequestNotificationPermission(activity: Activity) {
        if (!areNotificationsEnabled(activity)) {
            showDeviceSpecificInstructions(activity)
        }
    }
}