package com.ys.phdmama.navigation

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Menu
import androidx.compose.material.icons.filled.Star
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.navigation.NavController
import androidx.navigation.compose.currentBackStackEntryAsState
import com.ys.phdmama.navigation.NavRoutes

@Composable
fun BottomNavigationBar(navController: NavController) {
    val items = listOf(
        NavRoutes.BORN_DASHBOARD to Icons.Default.Home,
        NavRoutes.BORN_MENU to Icons.Default.Menu,
        NavRoutes.BORN_GROWTHMILESTONES to Icons.Default.Star
    )

    NavigationBar {
        val currentRoute = navController.currentBackStackEntryAsState().value?.destination?.route
        items.forEach { (route, icon) ->
            NavigationBarItem(
                selected = currentRoute == route,
                onClick = {
                    if (currentRoute != route) {
                        navController.navigate(route) {
                            popUpTo(NavRoutes.BORN_DASHBOARD) { inclusive = false }
                        }
                    }
                },
                icon = {
                    Icon(icon, contentDescription = route)
                },
                label = { Text(text = route) }
            )
        }
    }
}