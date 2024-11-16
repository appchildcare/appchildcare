package com.ys.phdmama.ui.components

import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.navigation.NavController
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.compose.currentBackStackEntryAsState
import com.ys.phdmama.navigation.NavRoutes
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.ui.graphics.vector.ImageVector

data class NavBarItem(val route: String, val label: String, val icon: ImageVector)

val navItems = listOf(
    NavBarItem(NavRoutes.BORN_DASHBOARD, "Panel", Icons.Default.Home),
    NavBarItem(NavRoutes.BORN_COUNTERS, "Contadores", Icons.Default.AddCircle),
    NavBarItem(NavRoutes.BORN_RESOURCES, "Recursos", Icons.Default.Menu),
    NavBarItem(NavRoutes.BORN_MENU, "Bebé", Icons.Default.Face)
)

@Composable
fun BottomNavigationBar(navController: NavController) {
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
