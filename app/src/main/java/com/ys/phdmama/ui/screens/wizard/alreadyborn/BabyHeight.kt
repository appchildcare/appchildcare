
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
fun BabyHeightScreen(navController: NavHostController, viewModel: BabyDataViewModel = viewModel()) {
    val babyHeight by viewModel.babyHeight.collectAsState()

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        TextField(
            value = babyHeight,
            onValueChange = { viewModel.updateBabyHeight(it) },
            label = { Text("Baby Height") },
            modifier = Modifier.fillMaxWidth()
        )

        Spacer(modifier = Modifier.height(16.dp))

        Button(onClick = {
            viewModel.setBabyHeight(babyHeight)
            navController.navigate(NavRoutes.BABY_PERIMETER) {
                popUpTo(NavRoutes.BABY_STATUS) { inclusive = true }
            }
        }) {
            Text(text = "Guardar Height")
        }

        Button(onClick = {
            navController.navigate(NavRoutes.BABY_WEIGHT) {}
        }) {
            Text(text = "Revisar Weight")
        }
    }
}