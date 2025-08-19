package com.ys.phdmama.ui.main

import android.Manifest
import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import android.util.Log
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.result.contract.ActivityResultContracts
import androidx.annotation.RequiresApi
import androidx.compose.material3.DrawerValue
import androidx.compose.material3.ModalNavigationDrawer
import androidx.compose.material3.rememberDrawerState
import androidx.compose.runtime.rememberCoroutineScope
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.compose.rememberNavController
import com.ys.phdmama.navigation.NavGraph
import com.ys.phdmama.ui.theme.PhdmamaTheme
import com.ys.phdmama.utils.BatteryOptimizationHelper
import com.ys.phdmama.utils.NotificationPermissionHelper
import com.ys.phdmama.viewmodel.BabyDataViewModel
import com.ys.phdmama.viewmodel.LoginViewModel
import kotlinx.coroutines.launch

class MainActivity : ComponentActivity() {

    // Request permission launcher
    private val requestPermissionLauncher = registerForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { isGranted: Boolean ->
        if (isGranted) {
            // Permission granted, you can show notifications
        } else {
            // Permission denied, handle accordingly
        }
    }

    @RequiresApi(Build.VERSION_CODES.O)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // Create notification channel first
        createNotificationChannel()

        // Request notification permission if needed
        requestNotificationPermission()

        // Check battery optimization after a short delay to let the UI load
        checkBatteryOptimization()

        // Also check notification permissions
        checkNotificationPermissions()

        setContent {
            PhdmamaTheme {
                val navController = rememberNavController()
                val drawerState = rememberDrawerState(DrawerValue.Closed)
                val scope = rememberCoroutineScope()
                val loginViewModel: LoginViewModel = viewModel()
                val babyDataViewModel: BabyDataViewModel = viewModel()

                ModalNavigationDrawer(
                    drawerState = drawerState,
                    drawerContent = {
                        SideNavigationBar(
                            navController,
                            loginViewModel,
                            babyDataViewModel,
                            closeDrawer = { scope.launch { drawerState.close() } })
                    }
                ) {
                    NavGraph(
                        navController = navController,
                        openDrawer = { scope.launch { drawerState.open() } })
                }
            }
        }
    }

    private fun createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                "counter_channel",
                "Counter Service",
                NotificationManager.IMPORTANCE_HIGH
            ).apply {
                setSound(null, null)
                description = "Shows counter updates in the notification bar"
                setShowBadge(false)
                enableLights(false)
                enableVibration(false)
            }
            val notificationManager = getSystemService(NotificationManager::class.java)
            notificationManager.createNotificationChannel(channel)
        }
    }

    private fun requestNotificationPermission() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            if (ContextCompat.checkSelfPermission(
                    this,
                    Manifest.permission.POST_NOTIFICATIONS
                ) != PackageManager.PERMISSION_GRANTED
            ) {
                requestPermissionLauncher.launch(Manifest.permission.POST_NOTIFICATIONS)
            }
        }
    }

    private fun checkBatteryOptimization() {
        // Check after a delay to ensure the activity is fully loaded
        window.decorView.post {
            if (!BatteryOptimizationHelper.isIgnoringBatteryOptimizations(this)) {
                BatteryOptimizationHelper.requestDisableBatteryOptimization(this)
            }
        }
    }

    private fun checkNotificationPermissions() {
        // Check notification permissions after UI loads
        window.decorView.post {
            if (!NotificationPermissionHelper.areNotificationsEnabled(this)) {
                NotificationPermissionHelper.checkAndRequestNotificationPermission(this)
            }
        }
    }

    override fun onResume() {
        super.onResume()
        // Check notification permissions when user returns to the app
        // This helps catch cases where user went to settings and changed notification settings
        if (!NotificationPermissionHelper.areNotificationsEnabled(this)) {
            // Don't show dialog immediately on resume, just log for now
            Log.d("MainActivity", "Notifications still disabled after resume")
        }
    }
}