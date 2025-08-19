package com.ys.phdmama.utils

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Build
import android.provider.Settings
import androidx.appcompat.app.AlertDialog
import androidx.core.app.NotificationManagerCompat

object NotificationPermissionHelper {

    /**
     * Check if notifications are enabled for the app
     */
    fun areNotificationsEnabled(context: Context): Boolean {
        return NotificationManagerCompat.from(context).areNotificationsEnabled()
    }

    /**
     * Show dialog explaining why notifications are needed and how to enable them
     */
    fun showNotificationSettingsDialog(activity: Activity) {
        AlertDialog.Builder(activity)
            .setTitle("Notificaciones Deshabilitadas")
            .setMessage(
                "Las notificaciones están deshabilitadas para esta aplicación.\n\n" +
                        "Para que el contador funcione correctamente en segundo plano, " +
                        "necesitas habilitar las notificaciones.\n\n" +
                        "Pasos:\n" +
                        "1. Ir a Configuración de la App\n" +
                        "2. Buscar 'Notificaciones'\n" +
                        "3. Activar todas las notificaciones\n" +
                        "4. Activar el canal 'Counter Service'"
            )
            .setPositiveButton("Ir a Configuración") { _, _ ->
                openNotificationSettings(activity)
            }
            .setNegativeButton("Más tarde", null)
            .setCancelable(false)
            .show()
    }

    /**
     * Open notification settings for the app
     */
    private fun openNotificationSettings(activity: Activity) {
        try {
            // Try to open specific app notification settings
            val intent = Intent().apply {
                when {
                    Build.VERSION.SDK_INT >= Build.VERSION_CODES.O -> {
                        action = Settings.ACTION_APP_NOTIFICATION_SETTINGS
                        putExtra(Settings.EXTRA_APP_PACKAGE, activity.packageName)
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
            // Fallback to general app settings
            openAppSettings(activity)
        }
    }

    /**
     * Open general app settings as fallback
     */
    private fun openAppSettings(activity: Activity) {
        try {
            val intent = Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS).apply {
                data = Uri.parse("package:${activity.packageName}")
            }
            activity.startActivity(intent)
        } catch (e: Exception) {
            // Last resort - open main settings
            val intent = Intent(Settings.ACTION_SETTINGS)
            activity.startActivity(intent)
        }
    }

    /**
     * Show step-by-step instructions based on device manufacturer
     */
    fun showDeviceSpecificInstructions(activity: Activity) {
        val manufacturer = Build.MANUFACTURER.lowercase()
        val instructions = getDeviceSpecificInstructions(manufacturer, activity.packageName)

        AlertDialog.Builder(activity)
            .setTitle("Habilitar Notificaciones")
            .setMessage(instructions)
            .setPositiveButton("Ir a Configuración") { _, _ ->
                openNotificationSettings(activity)
            }
            .setNegativeButton("Entendido", null)
            .show()
    }

    /**
     * Get device-specific instructions for enabling notifications
     */
    private fun getDeviceSpecificInstructions(manufacturer: String, packageName: String): String {
        val appName = "PhdMama" // Replace with your app name

        return when {
            manufacturer.contains("samsung") ->
                "Samsung (One UI):\n" +
                        "1. Configuración → Aplicaciones → $appName\n" +
                        "2. Notificaciones → Permitir notificaciones (ON)\n" +
                        "3. Asegúrate que 'Counter Service' esté habilitado"

            manufacturer.contains("xiaomi") ->
                "Xiaomi (MIUI):\n" +
                        "1. Configuración → Aplicaciones → Administrar aplicaciones\n" +
                        "2. Buscar '$appName'\n" +
                        "3. Notificaciones → Mostrar notificaciones (ON)\n" +
                        "4. También revisar: Configuración → Notificaciones → $appName"

            manufacturer.contains("huawei") ->
                "Huawei (EMUI):\n" +
                        "1. Configuración → Aplicaciones → $appName\n" +
                        "2. Notificaciones → Permitir notificaciones (ON)\n" +
                        "3. Verificar que todos los tipos estén habilitados"

            manufacturer.contains("oppo") ->
                "Oppo (ColorOS):\n" +
                        "1. Configuración → Notificaciones y barra de estado\n" +
                        "2. Administrar notificaciones → $appName\n" +
                        "3. Permitir notificaciones (ON)"

            manufacturer.contains("vivo") ->
                "Vivo (FunTouch):\n" +
                        "1. Configuración → Notificaciones y barra de estado\n" +
                        "2. $appName → Permitir notificaciones (ON)"

            manufacturer.contains("oneplus") ->
                "OnePlus (OxygenOS):\n" +
                        "1. Configuración → Aplicaciones y notificaciones\n" +
                        "2. $appName → Notificaciones → Mostrar notificaciones (ON)"

            else ->
                "Pasos generales:\n" +
                        "1. Ve a Configuración de tu dispositivo\n" +
                        "2. Busca 'Aplicaciones' o 'Apps'\n" +
                        "3. Encuentra '$appName'\n" +
                        "4. Toca en 'Notificaciones'\n" +
                        "5. Activa 'Permitir notificaciones'\n" +
                        "6. Asegúrate que 'Counter Service' esté habilitado"
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