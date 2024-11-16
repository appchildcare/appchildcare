
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
fun BabyAPGARScreen(navController: NavHostController, viewModel: BabyDataViewModel = viewModel()) {
    var babyAPGAR by remember { mutableStateOf(viewModel.getBabyAttribute("apgar") ?: "") }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        TextField(
            value = babyAPGAR,
            onValueChange = {
                babyAPGAR = it
                viewModel.setBabyAttribute("apgar", it)
                            },
            label = { Text("APGAR") },
            modifier = Modifier.fillMaxWidth()
        )

        Spacer(modifier = Modifier.height(16.dp))

        Button(onClick = {
            viewModel.setBabyAttribute("apgar", babyAPGAR)
            navController.navigate(NavRoutes.BABY_WEIGHT) {
                popUpTo(NavRoutes.BABY_STATUS) { inclusive = true }
            }
        }) {
            Text(text = "Guardar APGAR")
        }
        Button(onClick = {
            navController.navigate(NavRoutes.BABY_NAME) {}
        }) {
            Text(text = "Revisar nombre")
        }
    }
}