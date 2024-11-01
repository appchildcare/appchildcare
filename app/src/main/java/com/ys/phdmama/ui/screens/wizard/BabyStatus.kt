package com.ys.phdmama.ui.screens.wizard

import android.widget.Toast
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.*
import androidx.compose.material3.Button
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavHostController
import com.ys.phdmama.R
import com.ys.phdmama.navigation.NavRoutes
import com.ys.phdmama.viewmodel.BabyStatusViewModel

@Composable
fun BabyStatusScreen(
    navController: NavHostController,
    babyStatusViewModel: BabyStatusViewModel = viewModel()
) {
    val context = LocalContext.current

    Box(
        modifier = Modifier.fillMaxSize()
    ) {
        // Imagen de fondo
        Image(
            painter = painterResource(id = R.drawable.one),
            contentDescription = null,
            contentScale = ContentScale.Crop,
            modifier = Modifier.fillMaxSize()
        )

        // Contenido principal
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp),
            verticalArrangement = Arrangement.Center,
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Button(
                onClick = {
                    babyStatusViewModel.updateUserRole("waiting", onSuccess = {
                        navController.navigate(NavRoutes.ROUGHBIRTH) {
                            popUpTo(NavRoutes.BABY_STATUS) { inclusive = true }
                        }
                    }, onError = { errorMessage ->
                        Toast.makeText(context, errorMessage, Toast.LENGTH_SHORT).show()
                    })
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(8.dp)
            ) {
                Text(text = "En la dulce espera")
            }

            Button(
                onClick = {
                    babyStatusViewModel.updateUserRole("born", onSuccess = {
                        navController.navigate(NavRoutes.BABY_ALREADY_BORN) {
                            popUpTo(NavRoutes.BABY_STATUS) { inclusive = true }
                        }
                    }, onError = { errorMessage ->
                        Toast.makeText(context, errorMessage, Toast.LENGTH_SHORT).show()
                    })
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(8.dp)
            ) {
                Text(text = "Mi bebé ya nació")
            }
        }
    }
}
