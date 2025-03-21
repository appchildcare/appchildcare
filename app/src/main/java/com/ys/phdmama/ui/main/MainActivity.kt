package com.ys.phdmama.ui.main

import android.app.NotificationChannel
import android.app.NotificationManager
import android.os.Build
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.annotation.RequiresApi
import androidx.compose.material3.DrawerValue
import androidx.compose.material3.ModalNavigationDrawer
import androidx.compose.material3.rememberDrawerState
import androidx.compose.runtime.rememberCoroutineScope
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.compose.rememberNavController
import com.ys.phdmama.navigation.NavGraph
import com.ys.phdmama.ui.theme.PhdmamaTheme
import com.ys.phdmama.viewmodel.LoginViewModel
import kotlinx.coroutines.launch

class MainActivity : ComponentActivity() {
    @RequiresApi(Build.VERSION_CODES.O)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            PhdmamaTheme {
                val navController = rememberNavController()
                val drawerState = rememberDrawerState(DrawerValue.Closed)
                val scope = rememberCoroutineScope()
                val loginViewModel: LoginViewModel = viewModel()

                ModalNavigationDrawer(
                    drawerState = drawerState,
                    drawerContent = {
                        SideNavigationBar(
                            navController,
                            loginViewModel,
                            closeDrawer = { scope.launch { drawerState.close() } })
                    }
                ) {
                    NavGraph(
                        navController = navController,
                        openDrawer = { scope.launch { drawerState.open() } })
                }
            }
        }
        createNotificationChannel()
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
            }
            val notificationManager = getSystemService(NotificationManager::class.java)
            notificationManager.createNotificationChannel(channel)
        }
    }
}
