package com.ys.phdmama.ui.screens.waiting

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import androidx.navigation.compose.rememberNavController
import com.ys.phdmama.ui.components.PhdLayoutMenu
import com.ys.phdmama.viewmodel.BornDashboardViewModel

@Composable
fun WaitingDashboardScreen(
    navController: NavController = rememberNavController(),
    dashboardViewModel: BornDashboardViewModel = viewModel(),
    openDrawer: () -> Unit
) {
    PhdLayoutMenu(
        title = "Dulce espera Dashboard",
        navController = navController,
        openDrawer = openDrawer
    ) { innerPadding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) {
            Text(
                text = "¡Bienvenido al dashboard de la dulce espera",
                modifier = Modifier.align(Alignment.Center),
                style = MaterialTheme.typography.titleLarge
            )
        }
    }
}