package com.ys.phdmama.ui.screens.born

import androidx.compose.material3.*
import androidx.compose.foundation.layout.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.Alignment
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import androidx.navigation.compose.rememberNavController
import com.ys.phdmama.ui.components.BottomNavigationBar
import com.ys.phdmama.viewmodel.BornDashboardViewModel

@Composable
fun BornDashboardScreen(
    navController: NavController = rememberNavController(),
    dashboardViewModel: BornDashboardViewModel = viewModel()
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
                text = "¡Bienvenido al dashboard del ya nacido!",
                modifier = Modifier.align(Alignment.Center),
                style = MaterialTheme.typography.titleLarge
            )
        }
    }
}
