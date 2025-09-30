package com.ys.phdmama.ui.welcome

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavHostController
import com.ys.phdmama.navigation.NavRoutes
import com.ys.phdmama.ui.splash.navigateSafely
import com.ys.phdmama.ui.theme.primaryGray
import com.ys.phdmama.ui.theme.primaryTeal
import com.ys.phdmama.ui.theme.primaryYellow
import com.ys.phdmama.ui.theme.secondaryYellow
import kotlinx.coroutines.launch

data class SlideData(
    val title: String,
    val content: String,
    val bulletPoints: List<String>? = null
)


@Composable
fun WelcomeSlider(navController: NavHostController) {
    val slides = listOf(
        SlideData(
            title = "¡Felicidades, próximos ma/padres!",
            content = "El viaje más increíble de su vida ha comenzado. Esta aplicación será su apoyo en cada paso, desde la dulce espera, el nacimiento y crianza."
        ),
        SlideData(
            title = "Aquí encontrarás:",
            content = "",
            bulletPoints = listOf(
                "Organización",
                "Información confiable",
                "Apoyo y conexión",
                "Bienestar emocional"
            )
        ),
        SlideData(
            title = "Recuerden, no están solos.",
            content = "Estamos aquí para acompañarlos.\n¡Bienvenidos a ChildCare!"
        )
    )

    val pagerState = rememberPagerState { slides.size }
    val scope = rememberCoroutineScope()

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFFF5F9FF))
    ) {
        Column(
            modifier = Modifier.fillMaxSize(),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            HorizontalPager(
                state = pagerState,
                modifier = Modifier.weight(1f)
            ) { page ->
                SlideContent(slide = slides[page])
            }

            // Indicadores de página
            Row(
                modifier = Modifier.padding(vertical = 16.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                repeat(slides.size) { index ->
                    Box(
                        modifier = Modifier
                            .size(if (pagerState.currentPage == index) 24.dp else 8.dp, 8.dp)
                            .clip(CircleShape)
                            .background(
                                if (pagerState.currentPage == index)
                                    secondaryYellow
                                else
                                    primaryGray
                            )
                    )
                }
            }

            // Botón
            Button(
                onClick = {
                    if (pagerState.currentPage < slides.size - 1) {
                        scope.launch {
                            pagerState.animateScrollToPage(pagerState.currentPage + 1)
                        }
                    } else {
                        navigateSafely(navController, NavRoutes.LOGIN)
                    }
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 32.dp, vertical = 24.dp)
                    .height(56.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = primaryYellow
                ),
                shape = RoundedCornerShape(16.dp)
            ) {
                Text(
                    text = if (pagerState.currentPage < slides.size - 1) "Siguiente" else "Comenzar",
                    fontSize = 18.sp,
                    fontWeight = FontWeight.SemiBold
                )
            }
        }
    }
}

@Composable
fun SlideContent(slide: SlideData) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(32.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Text(
            text = slide.title,
            fontSize = 28.sp,
            fontWeight = FontWeight.Bold,
            color = Color(0xFF1F2937),
            textAlign = TextAlign.Center,
            modifier = Modifier.padding(bottom = 24.dp)
        )

        if (slide.bulletPoints != null) {
            Column(
                modifier = Modifier.padding(horizontal = 16.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                slide.bulletPoints.forEach { point ->
                    Row(
                        verticalAlignment = Alignment.Top,
                        horizontalArrangement = Arrangement.Start
                    ) {
                        Text(
                            text = "•",
                            fontSize = 20.sp,
                            color = primaryTeal,
                            modifier = Modifier.padding(end = 12.dp)
                        )
                        Text(
                            text = point,
                            fontSize = 18.sp,
                            color = Color(0xFF4B5563),
                            lineHeight = 28.sp
                        )
                    }
                }
            }
        } else {
            Text(
                text = slide.content,
                fontSize = 18.sp,
                color = Color(0xFF4B5563),
                textAlign = TextAlign.Center,
                lineHeight = 28.sp,
                modifier = Modifier.padding(horizontal = 16.dp)
            )
        }
    }
}
