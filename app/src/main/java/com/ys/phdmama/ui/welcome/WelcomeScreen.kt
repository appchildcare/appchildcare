package com.ys.phdmama.ui.welcome

import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.runtime.Composable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavHostController
import com.ys.phdmama.navigation.NavRoutes
import com.ys.phdmama.ui.splash.navigateSafely

@Composable
fun WelcomeScreen(navController: NavHostController,) {
    Surface(
        modifier = Modifier.fillMaxSize(),
        color = Color(0xFFF3E5F5)
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp),
            verticalArrangement = Arrangement.Center,
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Card(
                shape = RoundedCornerShape(16.dp),
                elevation = CardDefaults.cardElevation(defaultElevation = 8.dp),
                colors = CardDefaults.cardColors(containerColor = Color.White),
                modifier = Modifier.padding(16.dp)
            ) {
                Column(
                    modifier = Modifier.padding(16.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(
                        text = "CARTA DE BIENVENIDA:",
                        fontSize = 22.sp,
                        fontWeight = FontWeight.Bold,
                        textAlign = TextAlign.Center,
                        color = Color(0xFF6A1B9A)
                    )
                    Spacer(modifier = Modifier.height(12.dp))
                    Text(
                        text = "¡Felicidades, futuros padres!\n\n" +
                                "La dulce espera ha comenzado, un viaje lleno de nuevas emociones y\n\n" +
                                "transformaciones. Sabemos que este camino puede ser tan emocionante como\n\n" +
                                "desafiante, es por eso que queremos acompañarlos en cada paso.\n" +
                                "Queremos ser parte de tu refugio sano y científico, un espacio en donde no te\n" +
                                "sientas sol@. Tu equipo médico y esta comunidad, estamos para apoyarlos. \n" +
                                "Juntos, haremos de esta experiencia un viaje inolvidable.\n\n" +
                                "¡Bienvenidos a ChildCare, cuidaremos de ustedes!",
                        fontSize = 12.sp,
                        textAlign = TextAlign.Justify,
                        color = Color.DarkGray
                    )
                    Spacer(modifier = Modifier.height(12.dp))
                    Button(
                        onClick = {  navigateSafely(navController, NavRoutes.LOGIN) },
                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF6A1B9A))
                    ) {
                        Text(text = "Iniciar la aventura", color = Color.White)
                    }
                }
            }
        }
    }
}
