package com.ys.phdmama.ui.screens.wizard.prebirth

import androidx.compose.foundation.layout.*
import androidx.compose.material3.Button
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import com.ys.phdmama.navigation.NavRoutes

@Composable
fun HappyWaitingScreen(navController: NavController) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(text = "Estamos felicies de acompañarte en este camino hermoso de ser mamá. A continuación te presentamos recursos que te acompañarán en todo el camino...")

        Spacer(modifier = Modifier.height(16.dp))

        Button(onClick = {
            navController.navigate(NavRoutes.MAIN) {
                popUpTo(NavRoutes.BABY_STATUS) { inclusive = true }
            }
        }) {
            Text(text = "Iniciemos la aventura!")
        }
    }
}
