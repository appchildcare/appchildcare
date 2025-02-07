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

val sideNavItems = listOf(
    NavBarItem("option1", "Perfil de bebé", Icons.Default.Edit),
    NavBarItem("option2", "Elegir bebé", Icons.Default.Face),
    NavBarItem("option3", "Políticas de uso", Icons.Default.Info),
    NavBarItem("option4", "Link 1", Icons.Default.Star),
    NavBarItem("option5", "Link 2", Icons.Default.Star),
)


@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MainScreen(
    navController: NavController = rememberNavController(),
    loginViewModel: LoginViewModel = viewModel(),
    openDrawer: () -> Unit
) {
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("PHDMama") },
                navigationIcon = {
                    IconButton(onClick = { openDrawer() }) {
                        Icon(Icons.Default.Menu, contentDescription = "Menu")
                    }
                }
            )
        },
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

@Composable
fun SideNavigationBar(navController: NavController, closeDrawer: () -> Unit) {
    ModalDrawerSheet {
        Text("Menú de Navegación", style = MaterialTheme.typography.titleLarge, modifier = Modifier.padding(16.dp))
        Divider()

        sideNavItems.forEach { item ->
            NavigationDrawerItem(
                label = { Text(item.label) },
                icon = { Icon(item.icon, contentDescription = item.label) },
                selected = false,
                onClick = {
                    navController.navigate(item.route)
                    closeDrawer()
                },
                modifier = Modifier.padding(NavigationDrawerItemDefaults.ItemPadding)
            )
        }
    }
}