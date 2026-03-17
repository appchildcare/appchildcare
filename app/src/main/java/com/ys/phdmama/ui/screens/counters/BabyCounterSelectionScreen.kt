import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import com.ys.phdmama.R
import com.ys.phdmama.navigation.NavRoutes.CONTRACTION_COUNTER
import com.ys.phdmama.navigation.NavRoutes.LACTATION_TRACKING
import com.ys.phdmama.navigation.NavRoutes.SLEEP_TRACKING
import com.ys.phdmama.ui.components.PhdLayoutMenu
import com.ys.phdmama.ui.components.PhdMediumText
import com.ys.phdmama.ui.components.PhdTextBold
import com.ys.phdmama.ui.theme.primaryTeal
import com.ys.phdmama.ui.theme.secondaryAqua

@Composable
fun BabyCounterSelectionScreen(
    babyId: String?,
    navController: NavController,
    openDrawer: () -> Unit
) {
    PhdLayoutMenu(
        title = "Seguimiento del bebé",
        navController = navController,
        openDrawer = openDrawer
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(20.dp),
            verticalArrangement = Arrangement.Center,
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // Welcome Section
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                modifier = Modifier.padding(bottom = 48.dp)
            ) {
                Image(
                    painter = painterResource(id = R.drawable.mascota_juntos),
                    contentDescription = "Image",
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp, 4.dp)
                        .height(180.dp)
                )

                PhdTextBold(
                    text = "¿Qué quieres ?",
                )

                PhdMediumText(
                    text = "Selecciona una opción para comenzar",
                )
            }

            // Sleep Option
            TrackingOptionCard(
                title = "Sueño",
                subtitle = "Registrar siestas y tiempo de descanso",
                icon = Icons.Default.Info,
                gradientColors = listOf(
                    primaryTeal,
                    secondaryAqua
                ),
                emoji = "😴",
                onClick = {
                    navController.navigate(SLEEP_TRACKING)
                }
            )

            Spacer(modifier = Modifier.height(24.dp))

            // Lactation Option
            TrackingOptionCard(
                title = "Lactancia",
                subtitle = "Registrar sesiones de alimentación",
                icon = Icons.Default.Favorite,
                gradientColors = listOf(
                    primaryTeal,
                    secondaryAqua
                ),
                emoji = "🤱",
                onClick = {
                    navController.navigate(LACTATION_TRACKING)
                }
            )

            Spacer(modifier = Modifier.height(24.dp))

            // Lactation Option
            TrackingOptionCard(
                title = "Contracciones",
                subtitle = "Registrar contracciones en embarazo",
                icon = Icons.Default.Favorite,
                gradientColors = listOf(
                    primaryTeal,
                    secondaryAqua
                ),
                emoji = "🤱",
                onClick = {
                    navController.navigate(CONTRACTION_COUNTER)
                }
            )

            Spacer(modifier = Modifier.height(24.dp))
        }
    }
}

@Composable
fun TrackingOptionCard(
    title: String,
    subtitle: String,
    icon: ImageVector,
    gradientColors: List<Color>,
    emoji: String,
    onClick: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .height(120.dp)
            .clickable { onClick() },
        shape = RoundedCornerShape(20.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 8.dp)
    ) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(
                    Brush.horizontalGradient(gradientColors)
                )
        ) {
            Row(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(20.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Column(
                    modifier = Modifier.weight(1f)
                ) {
                    Text(
                        text = title,
                        fontSize = 24.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color.White
                    )
                    Text(
                        text = subtitle,
                        fontSize = 14.sp,
                        modifier = Modifier.padding(top = 4.dp)
                    )
                }
            }
        }
    }
}
