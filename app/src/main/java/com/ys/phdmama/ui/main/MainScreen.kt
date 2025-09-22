package com.ys.phdmama.ui.main

import androidx.compose.foundation.Image
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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.painterResource
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.ys.phdmama.R
import com.ys.phdmama.ui.components.PhdBoldText
import com.ys.phdmama.viewmodel.BabyDataViewModel
import com.ys.phdmama.viewmodel.LoginViewModel

data class NavBarItem(val route: String, val label: String, val icon: ImageVector,
                      val isPremium: Boolean = false)

val navItems = listOf(
    NavBarItem("home", "Inicio", Icons.Default.Home),
    NavBarItem("counters", "Controles", Icons.Default.AddCircle),
    NavBarItem("resources", "Recursos", Icons.Default.Menu),
    NavBarItem("baby", "Bebé", Icons.Default.Face)
)

val sideNavItems = listOf(
    NavBarItem(NavRoutes.SIDEBAR_BABY_PROFILE, "Perfil de bebé", Icons.Default.Edit, isPremium = true),
    NavBarItem(NavRoutes.POO_MAIN_SELECTION, "Registro de cacas",Icons.Default.Add),
    NavBarItem(NavRoutes.TERMS_CONDITIONS, "Políticas de uso", Icons.Default.Info),
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
fun SideNavigationBar(navController: NavController, loginViewModel: LoginViewModel = viewModel(), babyDataViewModel: BabyDataViewModel = viewModel(), closeDrawer: () -> Unit) {
    val userRole by loginViewModel.userRole.collectAsStateWithLifecycle()
    var showPremiumOption by remember { mutableStateOf(true) }

    LaunchedEffect(userRole) {
        userRole?.let {
            showPremiumOption = when (userRole) {
                "born" -> false
                "waiting" -> true
                else -> true
            }
        }
    }

    ModalDrawerSheet {
        Text("Menú de Navegación", style = MaterialTheme.typography.titleMedium, modifier = Modifier.padding(12.dp))
        HorizontalDivider()
        val Gold = Color(0xFFA28834)
        sideNavItems.forEach { item ->
            NavigationDrawerItem(
                label = { Text(item.label, style = MaterialTheme.typography.labelMedium) },
                icon = {
                    if (item.isPremium && showPremiumOption) {
                        BadgedBox(
                            badge = {
                                Box(
                                    modifier = Modifier
                                        .padding(start = 150.dp)
                                ) {
                                    Image(
                                        painter = painterResource(id = R.drawable.ic_premium),
                                        contentDescription = "Pregnant women",
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .padding(16.dp, 4.dp)
                                    )
                                }
                            }
                        ) {
                            Icon(item.icon, contentDescription = item.label)
                        }
                    } else {
                        // Regular icon for non-premium items
                        Icon(item.icon, contentDescription = item.label)
                    }
                },
                selected = false,
                onClick = {
                    navController.navigate(item.route)
                    closeDrawer()
                },
                modifier = Modifier.padding(NavigationDrawerItemDefaults.ItemPadding)
            )
        }
        Column (modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)) {
            Button(onClick = {
                loginViewModel.logout(navController, loginViewModel, babyDataViewModel)
                closeDrawer()
            }) {
                PhdBoldText("Logout")
            }
        }
    }
}