package com.ys.phdmama.ui.components

import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.navigation.NavController
import androidx.navigation.compose.currentBackStackEntryAsState
import com.ys.phdmama.navigation.NavRoutes
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.compose.material3.NavigationBarItemDefaults
import androidx.compose.ui.res.stringResource
import androidx.hilt.navigation.compose.hiltViewModel
import com.ys.phdmama.R
import com.ys.phdmama.ui.theme.primaryTeal
import com.ys.phdmama.viewmodel.LoginViewModel

data class NavBarItem(val route: String, val label: Int, val icon: ImageVector)

val navBornItems = listOf(
    NavBarItem(NavRoutes.BORN_DASHBOARD, R.string.bottom_navigation_panel_label, Icons.Default.Home),
    NavBarItem(NavRoutes.BORN_COUNTERS,  R.string.bottom_navigation_counters_label, Icons.Default.AddCircle),
    NavBarItem(NavRoutes.BORN_RESOURCES,  R.string.bottom_navigation_checklist_label, Icons.Default.Menu),
    NavBarItem(NavRoutes.BORN_MENU,  R.string.bottom_navigation_baby_label, Icons.Default.Face)
)

val navWaitingItems = listOf(
    NavBarItem(NavRoutes.PREGNANCY_DASHBOARD, R.string.bottom_navigation_panel_label, Icons.Default.Home),
    NavBarItem(NavRoutes.PREGNANCY_RESOURCES, R.string.bottom_navigation_checklist_label, Icons.Default.Menu)
)

@Composable
fun BottomNavigationBar(navController: NavController,
                        loginViewModel: LoginViewModel = hiltViewModel()) {// Observe ViewModel state
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
            val label = stringResource(screen.label)
            NavigationBarItem(
                icon = { Icon(screen.icon, contentDescription = label) },
                label = { Text(label) },
                selected = currentRoute == screen.route,
                onClick = {
                    navController.navigate(screen.route) {
                        popUpTo(navController.graph.findStartDestination().id) {
                            saveState = true
                        }
                        launchSingleTop = true
                        restoreState = true
                    }
                },
                colors = NavigationBarItemDefaults.colors(
                    indicatorColor = primaryTeal
                )
            )
        }
    }
}
