package com.ys.cunaco.ui.screens.counters

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController
import com.ys.cunaco.R
import com.ys.cunaco.navigation.NavRoutes.CONTRACTION_COUNTER
import com.ys.cunaco.navigation.NavRoutes.LACTATION_TRACKING
import com.ys.cunaco.navigation.NavRoutes.SLEEP_TRACKING
import com.ys.cunaco.ui.components.PhdLayoutMenu
import com.ys.cunaco.ui.components.PhdMediumText
import com.ys.cunaco.ui.components.PhdTextBold
import com.ys.cunaco.ui.theme.primaryTeal
import com.ys.cunaco.viewmodel.LoginViewModel

@Composable
fun BabyCounterSelectionScreen(
    navController: NavController,
    loginViewModel: LoginViewModel = hiltViewModel(),
    openDrawer: () -> Unit
) {
    val userRole by loginViewModel.userRole.collectAsState()
    val isWaiting = userRole == "waiting"

    PhdLayoutMenu(
        title = "Seguimiento",
        navController = navController,
        openDrawer = openDrawer
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(20.dp),
            verticalArrangement = Arrangement.Top,
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                modifier = Modifier.padding(bottom = 32.dp, top = 16.dp)
            ) {
                Image(
                    painter = painterResource(id = R.drawable.mascota_juntos),
                    contentDescription = "Image",
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp, 4.dp)
                        .height(180.dp)
                )

                PhdTextBold(text = "¿Qué quieres registrar?")
                PhdMediumText(text = "Selecciona una opción para comenzar")
            }

            if (isWaiting) {
                // FLUJO WAITING: Activar SOLO la opción de Contracciones
                TrackingOptionCard(
                    title = "Contracciones",
                    subtitle = "Registrar frecuencia de contracciones",
                    gradientColors = listOf(primaryTeal, primaryTeal),
                    onClick = { navController.navigate(CONTRACTION_COUNTER) }
                )
            } else {
                // FLUJO BORN: Mostrar Sueño y Lactancia
                TrackingOptionCard(
                    title = "Sueño",
                    subtitle = "Registrar siestas y tiempo de descanso",
                    gradientColors = listOf(primaryTeal, primaryTeal),
                    onClick = { navController.navigate(SLEEP_TRACKING) }
                )

                Spacer(modifier = Modifier.height(24.dp))

                TrackingOptionCard(
                    title = "Lactancia",
                    subtitle = "Registrar sesiones de alimentación",
                    gradientColors = listOf(primaryTeal, primaryTeal),
                    onClick = { navController.navigate(LACTATION_TRACKING) }
                )
            }
        }
    }
}

@Composable
fun TrackingOptionCard(
    title: String,
    subtitle: String,
    gradientColors: List<Color>,
    onClick: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .height(110.dp)
            .clickable { onClick() },
        shape = RoundedCornerShape(20.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
    ) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(Brush.horizontalGradient(gradientColors))
        ) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(20.dp),
                verticalArrangement = Arrangement.Center
            ) {
                Text(
                    text = title,
                    fontSize = 22.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color(0xFF5B5C61)
                )
                Text(
                    text = subtitle,
                    fontSize = 13.sp,
                    color = Color(0xFF5B5C61).copy(alpha = 0.8f)
                )
            }
        }
    }
}
