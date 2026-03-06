package com.ys.phdmama.ui.main

import androidx.compose.foundation.Image
import androidx.compose.material3.*
import androidx.compose.foundation.layout.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
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
import androidx.compose.ui.res.stringResource
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.ys.phdmama.R
import com.ys.phdmama.ui.components.PhdBoldText
import com.ys.phdmama.viewmodel.BabyDataViewModel
import com.ys.phdmama.viewmodel.LoginViewModel

data class NavBarItem(val route: String, val label: Int, val icon: ImageVector,
                      val isPremium: Boolean = false)

val navItems = listOf(
    NavBarItem("home", R.string.side_navigation_home_label, Icons.Default.Home),
    NavBarItem("counters", R.string.side_navigation_controls_label, Icons.Default.AddCircle),
    NavBarItem("resources", R.string.side_navigation_checklist_label, Icons.Default.Menu),
    NavBarItem("baby", R.string.side_navigation_baby_label, Icons.Default.Face)
)

val sideNavItems = listOf(
    NavBarItem(NavRoutes.SIDEBAR_BABY_PROFILE, R.string.side_navigation_baby_profile_label, Icons.Default.Edit, isPremium = true),
    NavBarItem(NavRoutes.POO_MAIN_SELECTION, R.string.side_navigation_poop_label,Icons.Default.Add),
    NavBarItem(NavRoutes.TERMS_CONDITIONS, R.string.side_navigation_terms_label, Icons.Default.Info),
    NavBarItem(NavRoutes.CARBON_FOOTPRINT, R.string.side_navigation_carbon_label, Icons.Default.Star),
)


@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MainScreen(
    navController: NavController = rememberNavController(),
    loginViewModel: LoginViewModel = hiltViewModel(),
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
                }
            )
        }
    }
}

@Composable
fun SideNavigationBar(navController: NavController, loginViewModel: LoginViewModel = hiltViewModel(), babyDataViewModel: BabyDataViewModel = hiltViewModel(), closeDrawer: () -> Unit) {
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
        Text(stringResource(R.string.side_navigation_label), style = MaterialTheme.typography.titleMedium, modifier = Modifier.padding(12.dp))
        HorizontalDivider()
        val Gold = Color(0xFFA28834)
        sideNavItems.forEach { item ->
            val label = stringResource(item.label)
            NavigationDrawerItem(
                label = { Text(label, style = MaterialTheme.typography.labelMedium) },
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
                            Icon(item.icon, contentDescription = label)
                        }
                    } else {
                        // Regular icon for non-premium items
                        Icon(item.icon, contentDescription = label)
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
                PhdBoldText(stringResource(R.string.side_navigation_logout_label))
            }
        }
    }
}
