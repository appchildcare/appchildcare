package com.ys.cunaco.utils

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Build
import android.os.PowerManager
import android.provider.Settings
import android.app.AlertDialog
import com.ys.cunaco.R

object BatteryOptimizationHelper {

    private const val APP_NAME = "CunaCo"

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
            .setTitle(activity.getString(R.string.battery_opt_title))
            .setMessage(activity.getString(R.string.battery_opt_message))
            .setPositiveButton(activity.getString(R.string.battery_opt_configure)) { _, _ ->
                openBatteryOptimizationSettings(activity)
            }
            .setNegativeButton(activity.getString(R.string.battery_opt_later), null)
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
        val instructions = getManufacturerSpecificInstructions(activity, manufacturer)

        AlertDialog.Builder(activity)
            .setTitle(activity.getString(R.string.battery_opt_manufacturer_title))
            .setMessage(activity.getString(R.string.battery_opt_manufacturer_message, instructions))
            .setPositiveButton(activity.getString(R.string.battery_opt_yes)) { _, _ ->
                openBatteryOptimizationSettings(activity)
            }
            .setNegativeButton(activity.getString(R.string.battery_opt_no), null)
            .show()
    }

    /**
     * Get manufacturer-specific instructions
     */
    private fun getManufacturerSpecificInstructions(context: Context, manufacturer: String): String {
        return when {
            manufacturer.contains("xiaomi") ->
                context.getString(R.string.battery_opt_instructions_xiaomi, APP_NAME)
            manufacturer.contains("huawei") ->
                context.getString(R.string.battery_opt_instructions_huawei, APP_NAME)
            manufacturer.contains("oppo") ->
                context.getString(R.string.battery_opt_instructions_oppo, APP_NAME)
            manufacturer.contains("vivo") ->
                context.getString(R.string.battery_opt_instructions_vivo, APP_NAME)
            manufacturer.contains("oneplus") ->
                context.getString(R.string.battery_opt_instructions_oneplus, APP_NAME)
            manufacturer.contains("samsung") ->
                context.getString(R.string.battery_opt_instructions_samsung, APP_NAME)
            else ->
                context.getString(R.string.battery_opt_instructions_general)
        }
    }
}