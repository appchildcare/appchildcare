package com.ys.phdmama.ui.main

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.navigation.NavHostController

@Composable
fun MainScreen(navController: NavHostController) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
    ) {
        // Cabecera 1: "Antes del parto"
        Header(text = "Antes del parto")
        Spacer(modifier = Modifier.height(16.dp))

        // Filas de botones alternados
        TwoColumnButtons(
            leftButton = "BabyProfile",
            rightButton = "ChecklistPreBirth",
            onLeftClick = { navController.navigate("baby_profile") },
            onRightClick = { navController.navigate("checklist_prebirth") }
        )

        TwoColumnButtons(
            leftButton = "CheckListNewBorn",
            rightButton = "RegisterContraction",
            onLeftClick = { navController.navigate("newborn") },
            onRightClick = { navController.navigate("register_contraction") }
        )

        Spacer(modifier = Modifier.height(16.dp))

        // Footer 1: RoughBirth
        FooterButton(
            text = "RoughBirth",
            onClick = { navController.navigate("roughbirth") }
        )

        Spacer(modifier = Modifier.height(32.dp))

        // Cabecera 2: "Después del parto"
        Header(text = "Después del parto")
        Spacer(modifier = Modifier.height(16.dp))

        // Más filas de botones alternados
        TwoColumnButtons(
            leftButton = "PediatricianQuestions",
            rightButton = "FoodRegistry",
            onLeftClick = { navController.navigate("pediatrician_questions") },
            onRightClick = { navController.navigate("food_registry") }
        )

        TwoColumnButtons(
            leftButton = "PediatricianAppointments",
            rightButton = "DiaperChange",
            onLeftClick = { navController.navigate("pediatrician_appointments") },
            onRightClick = { navController.navigate("diaper_change") }
        )
    }
}

@Composable
fun Header(text: String) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .height(50.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.Center
    ) {
        Text(text = text, style = MaterialTheme.typography.titleLarge)
    }
}

@Composable
fun TwoColumnButtons(
    leftButton: String,
    rightButton: String,
    onLeftClick: () -> Unit,
    onRightClick: () -> Unit
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Button(
            onClick = onLeftClick,
            modifier = Modifier
                .weight(1f)
                .padding(8.dp)
        ) {
            Text(leftButton)
        }

        Button(
            onClick = onRightClick,
            modifier = Modifier
                .weight(1f)
                .padding(8.dp)
        ) {
            Text(rightButton)
        }
    }
}

@Composable
fun FooterButton(
    text: String,
    onClick: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(16.dp),
        horizontalArrangement = Arrangement.Center
    ) {
        Button(onClick = onClick) {
            Text(text)
        }
    }
}
