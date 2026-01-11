package com.ys.phdmama.ui.screens.wizard

import BabyStatusViewModelFactory
import android.widget.Toast
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.*
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.paint
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavHostController
import com.ys.phdmama.R
import com.ys.phdmama.navigation.NavRoutes
import com.ys.phdmama.ui.theme.primaryTeal
import com.ys.phdmama.ui.theme.primaryYellow
import com.ys.phdmama.viewmodel.BabyStatusViewModel

@Composable
fun BabyStatusScreen(navController: NavHostController) {
    val context = LocalContext.current
    val babyStatusViewModel: BabyStatusViewModel = viewModel(factory = BabyStatusViewModelFactory())
    var isLoadingWaiting by remember { mutableStateOf(false) }
    var isLoadingBorn by remember { mutableStateOf(false) }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .paint(
                painter = painterResource(R.drawable.background_baby_happiness),
                contentScale = ContentScale.Crop
            )
            .padding(16.dp),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {

        Image(
            painter = painterResource(id = R.drawable.app_child_care_logo),
            contentDescription = "Auth image",
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp, 4.dp)
                .height(180.dp)
        )

        Image(
            painter = painterResource(id = R.drawable.mascota_juntos),
            contentDescription = "Auth image",
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp, 4.dp)
                .height(280.dp)
        )

        Button(
            onClick = {
                isLoadingWaiting = true
                babyStatusViewModel.updateUserRole(
                    role = "waiting",
                    onSuccess = {
                        isLoadingWaiting = false
                        navController.navigate(NavRoutes.ROUGHBIRTH) {
                            popUpTo(NavRoutes.BABY_STATUS) { inclusive = true }
                        }
                    },
                    onError = { errorMessage ->
                        isLoadingWaiting = false
                        Toast.makeText(context, errorMessage, Toast.LENGTH_SHORT).show()
                    }
                )
            },
            colors = ButtonDefaults.buttonColors(containerColor = primaryTeal),
            enabled = !isLoadingWaiting,
            modifier = Modifier
                .fillMaxWidth()
                .padding(8.dp)
        ) {
            if (isLoadingWaiting) {
                CircularProgressIndicator()
            } else {
                Text(text = "En la dulce espera")
            }
        }

        Button(
            onClick = {
                isLoadingBorn = true
                babyStatusViewModel.updateUserRole(
                    role = "waiting",
                    onSuccess = {
                        isLoadingBorn = false
                        navController.navigate(NavRoutes.BABY_PROFILE) {
                            popUpTo(NavRoutes.BABY_STATUS) { inclusive = true }
                        }
                    },
                    onError = { errorMessage ->
                        isLoadingBorn = false
                        Toast.makeText(context, errorMessage, Toast.LENGTH_SHORT).show()
                    }
                )
            },
            colors = ButtonDefaults.buttonColors(containerColor = primaryYellow),
            enabled = !isLoadingBorn,
            modifier = Modifier
                .fillMaxWidth()
                .padding(8.dp)
        ) {
            if (isLoadingBorn) {
                CircularProgressIndicator()
            } else {
                Text(text = "Mi bebé ya nació")
            }
        }
    }
}
