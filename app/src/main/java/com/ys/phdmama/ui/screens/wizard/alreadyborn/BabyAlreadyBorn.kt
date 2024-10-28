package com.ys.phdmama.ui.screens.wizard.alreadyborn

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
fun BabyAlreadyBornScreen(navController: NavController) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(text = "¡Aqui hay que poner una saludo, una felicitación y una invitación a crear el perfil del bebé!")

        Spacer(modifier = Modifier.height(16.dp))

        Button(onClick = {
            navController.navigate(NavRoutes.BABY_NAME) {
                popUpTo("baby_status") { inclusive = true }
            }
        }) {
            Text(text = "Iniciemos juntos la aventura de ser mamá")
        }
    }
}
