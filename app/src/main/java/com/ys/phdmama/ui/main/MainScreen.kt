package com.ys.phdmama.ui.main

import androidx.compose.material3.*
import androidx.compose.foundation.layout.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import com.ys.phdmama.navigation.NavRoutes
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.graphics.vector.ImageVector
import com.ys.phdmama.viewmodel.LoginViewModel

data class NavBarItem(val route: String, val label: String, val icon: ImageVector)

val navItems = listOf(
    NavBarItem("home", "Inicio", Icons.Default.Home),
    NavBarItem("counters", "Controles", Icons.Default.AddCircle),
    NavBarItem("resources", "Recursos", Icons.Default.Menu),
    NavBarItem("baby", "Bebé", Icons.Default.Face)
)


@Composable
fun MainScreen(
    navController: NavController = rememberNavController(),
    loginViewModel: LoginViewModel = viewModel()
) {
    Scaffold(
        bottomBar = { BottomNavigationBar(navController) }
    ) { innerPadding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) {
            Text(
                text = "¡Bienvenido a tu Panel!",
                modifier = Modifier.align(Alignment.Center),
                style = MaterialTheme.typography.titleLarge
            )
        }
    }
}

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
                        popUpTo(navController.graph.findStartDestination().id) {
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

