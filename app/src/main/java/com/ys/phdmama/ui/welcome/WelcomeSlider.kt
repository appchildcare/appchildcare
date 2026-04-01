package com.ys.phdmama.ui.welcome

import androidx.compose.foundation.Image
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
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringArrayResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavHostController
import com.ys.phdmama.R
import com.ys.phdmama.navigation.NavRoutes
import com.ys.phdmama.ui.splash.navigateSafely
import com.ys.phdmama.ui.theme.primaryTeal
import com.ys.phdmama.ui.theme.secondaryYellow
import kotlinx.coroutines.launch

data class SlideData(
    val title: String,
    val content: String,
    val bulletPoints: Array<String>? = null
)


@Composable
fun WelcomeSlider(navController: NavHostController) {
    val slides = listOf(
        SlideData(
            title = stringResource(R.string.welcome_slider_title),
            content = stringResource(R.string.welcome_slider_slide_1)
        ),
        SlideData(
            title = stringResource(R.string.welcome_slider_title_2),
            content = "",
            bulletPoints = stringArrayResource(R.array.welcome_slider_slide_bullet_points)
        ),
        SlideData(
            title =  stringResource(R.string.welcome_slider_title_3),
            content = "${stringResource(R.string.welcome_slider_slide_3)}\n${stringResource(R.string.welcome_slider_slide_3_1)}"
        )
    )

    val pagerState = rememberPagerState { slides.size }
    val scope = rememberCoroutineScope()

    Box(
        modifier = Modifier
            .fillMaxSize()
    ) {
        Image(
            painter = painterResource(id = R.drawable.background_slider),
            contentDescription = null,
            contentScale = ContentScale.Crop,
            modifier = Modifier.fillMaxSize()
        )
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

            Row{
                Image(
                    painter = painterResource(id = R.drawable.mascota_relax),
                    contentDescription = "Baby icon",
                )
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
                                    primaryTeal
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
                    containerColor = Color(0xFF4db0b2)
                ),
                shape = RoundedCornerShape(16.dp)
            ) {
                Text(
                    text = if (pagerState.currentPage < slides.size - 1) stringResource(R.string.welcome_slider_button_next) else stringResource(R.string.welcome_slider_button_start),
                    fontSize = 18.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = Color.White
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
