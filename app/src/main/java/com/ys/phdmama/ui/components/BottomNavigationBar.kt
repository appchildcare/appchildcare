package com.ys.phdmama.ui.components

import android.util.Log
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.navigation.NavController
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.compose.currentBackStackEntryAsState
import com.ys.phdmama.navigation.NavRoutes
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.ys.phdmama.viewmodel.LoginViewModel

data class NavBarItem(val route: String, val label: String, val icon: ImageVector)

val navBornItems = listOf(
    NavBarItem(NavRoutes.BORN_DASHBOARD, "Panel", Icons.Default.Home),
    NavBarItem(NavRoutes.BORN_COUNTERS, "Contadores", Icons.Default.AddCircle),
    NavBarItem(NavRoutes.BORN_RESOURCES, "Recursos", Icons.Default.Menu),
    NavBarItem(NavRoutes.BORN_MENU, "Bebé", Icons.Default.Face)
)

val navWaitingItems = listOf(
    NavBarItem(NavRoutes.PREGNANCY_DASHBOARD, "Panel", Icons.Default.Home),
    NavBarItem(NavRoutes.PREGNANCY_RESOURCES, "Recursos", Icons.Default.Menu)
)

@Composable
fun BottomNavigationBar(navController: NavController,
                        loginViewModel: LoginViewModel = viewModel(),) {// Observe ViewModel state
    val userRole by loginViewModel.userRole.collectAsStateWithLifecycle()

    val navItems = when (userRole) {
        "born" -> navBornItems
        "waiting" -> navWaitingItems
        else -> emptyList() // Default to no items if role is unknown
    }

    NavigationBar {
        val navBackStackEntry by navController.currentBackStackEntryAsState()
        val currentRoute = navBackStackEntry?.destination?.route

        navItems.forEach { screen ->
            NavigationBarItem(
                icon = { Icon(screen.icon, contentDescription = screen.label) },
                label = { Text(screen.label) },
                selected = currentRoute == screen.route,
                onClick = {
                    navController.navigate(screen.route) {
//                        popUpTo(navController.graph.findStartDestination().id) {
                        popUpTo(0) {
                            saveState = true
                        }
                        launchSingleTop = true
                        restoreState = true
                    }
                }
            )
        }
    }
}
