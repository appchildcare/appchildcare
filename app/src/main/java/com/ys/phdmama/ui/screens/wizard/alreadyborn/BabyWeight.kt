
package com.ys.phdmama.ui.screens.wizard.alreadyborn

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavHostController
import com.ys.phdmama.navigation.NavRoutes
import com.ys.phdmama.viewmodel.BabyDataViewModel

@Composable
fun BabyWeightScreen(navController: NavHostController, viewModel: BabyDataViewModel = viewModel()) {
    val babyWeight by viewModel.babyWeight.collectAsState()

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        TextField(
            value = babyWeight,
            onValueChange = { viewModel.updateBabyWeight(it) },
            label = { Text("Baby Weight") },
            modifier = Modifier.fillMaxWidth()
        )

        Spacer(modifier = Modifier.height(16.dp))

        Button(onClick = {
            viewModel.setBabyWeight(babyWeight)
            navController.navigate(NavRoutes.BABY_HEIGHT) {
                popUpTo(NavRoutes.BABY_STATUS) { inclusive = true }
            }
        }) {
            Text(text = "Guardar Weight")
        }
        Button(onClick = {
            navController.navigate(NavRoutes.BABY_APGAR) {}
        }) {
            Text(text = "Revisar APGAR")
        }
    }
}