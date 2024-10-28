package com.ys.phdmama.ui.screens.wizard.alreadyborn

import android.util.Log
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
fun BabyNameScreen(navController: NavHostController, viewModel: BabyDataViewModel = viewModel()) {
    val babyName by viewModel.babyName.collectAsState()

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        TextField(
            value = babyName,
            onValueChange = { viewModel.updateBabyName(it) },
            label = { Text("Nombre del bebé") },
            modifier = Modifier.fillMaxWidth()
        )

        Spacer(modifier = Modifier.height(16.dp))

        Button(onClick = {
            viewModel.setBabyName(babyName)
            Log.d("BabyNameScreen", "Nombre del bebé: $babyName")
            navController.navigate(NavRoutes.BABY_APGAR) {
                popUpTo(NavRoutes.BABY_STATUS) { inclusive = true }
            }
        }) {
            Text(text = "Guardar Nombre")
        }
    }
}