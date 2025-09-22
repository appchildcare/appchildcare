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
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import com.ys.phdmama.navigation.NavRoutes.LACTATION_TRACKING
import com.ys.phdmama.navigation.NavRoutes.SLEEP_TRACKING
import com.ys.phdmama.ui.components.PhdLayoutMenu
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
                Text(
                    text = "👶",
                    fontSize = 72.sp,
                    modifier = Modifier.padding(bottom = 16.dp)
                )

                Text(
                    text = "¿Qué quieres ?",
                    fontSize = 24.sp,
                    fontWeight = FontWeight.Bold,
                    color =Color.Gray,
                    textAlign = TextAlign.Center
                )

                Text(
                    text = "Selecciona una opción para comenzar",
                    fontSize = 16.sp,
                    color = Color.Gray,
                    textAlign = TextAlign.Center,
                    modifier = Modifier.padding(top = 8.dp)
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

            Spacer(modifier = Modifier.height(48.dp))
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

                Box(
                    modifier = Modifier
                        .size(60.dp)
                        .background(
                            Color.White.copy(alpha = 0.2f),
                            RoundedCornerShape(30.dp)
                        ),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = emoji,
                        fontSize = 32.sp
                    )
                }
            }
        }
    }
}

@Composable
fun QuickStatItem(
    icon: String,
    title: String,
    value: String
) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            text = icon,
            fontSize = 32.sp,
            modifier = Modifier.padding(bottom = 8.dp)
        )

        Text(
            text = title,
            fontSize = 12.sp,
            color = Color.Gray,
            fontWeight = FontWeight.Medium
        )

        Text(
            text = value,
            fontSize = 16.sp,
            fontWeight = FontWeight.Bold,
            color = Color(0xFF424242)
        )
    }
}
