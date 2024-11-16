package com.ys.phdmama.ui.screens.wizard.alreadyborn

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
fun BabyBloodTypeScreen(navController: NavHostController, viewModel: BabyDataViewModel = viewModel()) {
    var expandedBloodType by remember { mutableStateOf(false) }
    var babyBloodType by remember { mutableStateOf(viewModel.getBabyAttribute("bloodType") ?: "") }

    val bloodTypes = listOf("A+", "A-", "B+", "B-", "AB+", "AB-", "O+", "O-")

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        // Dropdown for Blood Type
        Box {
            TextField(
                value = babyBloodType,
                onValueChange = {},
                label = { Text("Blood Type") },
                modifier = Modifier.fillMaxWidth(),
                readOnly = true,
                trailingIcon = {
                    IconButton(onClick = { expandedBloodType = !expandedBloodType }) {
                        Icon(imageVector = Icons.Default.ArrowDropDown, contentDescription = null)
                    }
                }
            )
            DropdownMenu(
                expanded = expandedBloodType,
                onDismissRequest = { expandedBloodType = false }
            ) {
                bloodTypes.forEach { bloodType ->
                    DropdownMenuItem(
                        text = { Text(bloodType) },
                        onClick = {
                            babyBloodType = bloodType
                            viewModel.setBabyAttribute("bloodType", bloodType)
                            expandedBloodType = false
                        }
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        Button(onClick = {
            navController.navigate(NavRoutes.BABY_SEX) {
                popUpTo(NavRoutes.BABY_STATUS) { inclusive = true }
            }
        }) {
            Text(text = "Guardar Blood Type")
        }

        Button(onClick = {
            navController.navigate(NavRoutes.BABY_PERIMETER) {}
        }) {
            Text(text = "Revisar perímetro")
        }
    }
}
