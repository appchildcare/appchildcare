package com.ys.phdmama.utils

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Build
import android.os.PowerManager
import android.provider.Settings
import android.app.AlertDialog

object BatteryOptimizationHelper {

    /**
     * Check if the app is whitelisted from battery optimization
     */
    fun isIgnoringBatteryOptimizations(context: Context): Boolean {
        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            val powerManager = context.getSystemService(Context.POWER_SERVICE) as PowerManager
            powerManager.isIgnoringBatteryOptimizations(context.packageName)
        } else {
            true // Battery optimization doesn't exist in older versions
        }
    }

    /**
     * Request to disable battery optimization for the app
     */
    fun requestDisableBatteryOptimization(activity: Activity) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            if (!isIgnoringBatteryOptimizations(activity)) {
                showBatteryOptimizationDialog(activity)
            }
        }
    }

    /**
     * Show dialog explaining why battery optimization should be disabled
     */
    private fun showBatteryOptimizationDialog(activity: Activity) {
        AlertDialog.Builder(activity)
            .setTitle("Optimización de Batería")
            .setMessage(
                "Para que el contador funcione correctamente en segundo plano, " +
                        "necesitas desactivar la optimización de batería para esta aplicación.\n\n" +
                        "Esto permitirá que el contador continúe funcionando cuando cierres la app."
            )
            .setPositiveButton("Configurar") { _, _ ->
                openBatteryOptimizationSettings(activity)
            }
            .setNegativeButton("Más tarde", null)
            .setCancelable(false)
            .show()
    }

    /**
     * Open battery optimization settings for the app
     */
    private fun openBatteryOptimizationSettings(activity: Activity) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            try {
                val intent = Intent(Settings.ACTION_REQUEST_IGNORE_BATTERY_OPTIMIZATIONS).apply {
                    data = Uri.parse("package:${activity.packageName}")
                }
                activity.startActivity(intent)
            } catch (e: Exception) {
                // If the specific intent doesn't work, try opening general battery settings
                openGeneralBatterySettings(activity)
            }
        }
    }

    /**
     * Open general battery optimization settings
     */
    private fun openGeneralBatterySettings(activity: Activity) {
        try {
            val intent = when {
                Build.VERSION.SDK_INT >= Build.VERSION_CODES.M -> {
                    Intent(Settings.ACTION_IGNORE_BATTERY_OPTIMIZATION_SETTINGS)
                }
                else -> Intent(Settings.ACTION_SETTINGS)
            }
            activity.startActivity(intent)
        } catch (e: Exception) {
            // Last resort - open main settings
            val intent = Intent(Settings.ACTION_SETTINGS)
            activity.startActivity(intent)
        }
    }

    /**
     * Check and request battery optimization exemption with manufacturer-specific handling
     */
    fun checkAndRequestBatteryOptimization(activity: Activity) {
        if (!isIgnoringBatteryOptimizations(activity)) {
            showManufacturerSpecificDialog(activity)
        }
    }

    /**
     * Show manufacturer-specific instructions
     */
    private fun showManufacturerSpecificDialog(activity: Activity) {
        val manufacturer = Build.MANUFACTURER.lowercase()
        val instructions = getManufacturerSpecificInstructions(manufacturer)

        AlertDialog.Builder(activity)
            .setTitle("Configuración de Batería")
            .setMessage(
                "Para que el contador funcione correctamente:\n\n" +
                        "1. Desactiva la optimización de batería\n" +
                        "2. ${instructions}\n\n" +
                        "¿Quieres abrir la configuración ahora?"
            )
            .setPositiveButton("Sí") { _, _ ->
                openBatteryOptimizationSettings(activity)
            }
            .setNegativeButton("No", null)
            .show()
    }

    /**
     * Get manufacturer-specific instructions
     */
    private fun getManufacturerSpecificInstructions(manufacturer: String): String {
        return when {
            manufacturer.contains("xiaomi") ->
                "En MIUI: Ve a Configuración > Apps > Administrar apps > PhdMama > Ahorro de batería > Sin restricciones"
            manufacturer.contains("huawei") ->
                "En EMUI: Ve a Configuración > Apps > PhdMama > Batería > Inicio automático (activar)"
            manufacturer.contains("oppo") ->
                "En ColorOS: Ve a Configuración > Batería > Optimización de energía > PhdMama > Desactivar"
            manufacturer.contains("vivo") ->
                "En FunTouch: Ve a Configuración > Batería > Gestión de consumo > PhdMama > Desactivar"
            manufacturer.contains("oneplus") ->
                "En OxygenOS: Ve a Configuración > Batería > Optimización de batería > PhdMama > No optimizar"
            manufacturer.contains("samsung") ->
                "En One UI: Ve a Configuración > Cuidado del dispositivo > Batería > Más opciones > Optimizar uso de batería > PhdMama > Desactivar"
            else ->
                "Busca en configuración de batería la opción para desactivar optimización para esta app"
        }
    }
}
