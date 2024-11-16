
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
fun BabyPerimeterScreen(navController: NavHostController, viewModel: BabyDataViewModel = viewModel()) {
    var babyPerimeter by remember { mutableStateOf(viewModel.getBabyAttribute("perimeter") ?: "") }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        TextField(
            value = babyPerimeter,
            onValueChange = {
                babyPerimeter = it
                viewModel.setBabyAttribute("perimeter", it)
            },
            label = { Text("Perímetro cefálico") },
            modifier = Modifier.fillMaxWidth()
        )

        Spacer(modifier = Modifier.height(16.dp))

        Button(onClick = {
            navController.navigate(NavRoutes.BABY_BLOOD_TYPE) {
                popUpTo(NavRoutes.BABY_STATUS) { inclusive = true }
            }
        }) {
            Text(text = "Guardar perímetro cefálico")
        }
        Button(onClick = {
            navController.navigate(NavRoutes.BABY_HEIGHT) {}
        }) {
            Text(text = "Revisar altura")
        }
    }
}