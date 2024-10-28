
package com.ys.phdmama.ui.screens.wizard.alreadyborn

import android.util.Log
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowDropDown
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
fun BabySexScreen(navController: NavHostController, viewModel: BabyDataViewModel = viewModel()) {
//    var babySex by remember { mutableStateOf("") }
    var expandedSex by remember { mutableStateOf(false) }
    val babySex by viewModel.babySex.collectAsState()

    val sexes = listOf("Masculino", "Femenino")

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        // Dropdown for Sex
        Box {
            TextField(
                value = babySex,
                onValueChange = {},
                label = { Text("Sexo") },
                modifier = Modifier.fillMaxWidth(),
                readOnly = true,
                trailingIcon = {
                    IconButton(onClick = { expandedSex = !expandedSex }) {
                        Icon(imageVector = Icons.Default.ArrowDropDown, contentDescription = null)
                    }
                }
            )
            DropdownMenu(
                expanded = expandedSex,
                onDismissRequest = { expandedSex = false }
            ) {
                sexes.forEach { sex ->
                    DropdownMenuItem(
                        text = { Text(sex) },
                        onClick = {
                            viewModel.updateBabySex(sex)
                            expandedSex = false
                        }
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        Button(onClick = {
            viewModel.setBabySex(babySex)
            navController.navigate(NavRoutes.BABY_SUMMARY) {
                popUpTo(NavRoutes.BABY_STATUS) { inclusive = true }
            }
        }) {
            Text(text = "Guardar Sexo")
        }

        Button(onClick = {
            navController.navigate(NavRoutes.BABY_BLOOD_TYPE) {
            }
        }) {
            Text(text = "Revisar tipo de sangre")
        }
    }
}