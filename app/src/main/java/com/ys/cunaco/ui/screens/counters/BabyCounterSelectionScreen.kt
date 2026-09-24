package com.ys.cunaco.ui.screens.counters

import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController
import com.ys.cunaco.R
import com.ys.cunaco.navigation.NavRoutes.CARBON_FOOTPRINT
import com.ys.cunaco.navigation.NavRoutes.CONTRACTION_COUNTER
import com.ys.cunaco.navigation.NavRoutes.LACTATION_TRACKING
import com.ys.cunaco.navigation.NavRoutes.POO_MAIN_SELECTION
import com.ys.cunaco.navigation.NavRoutes.SLEEP_TRACKING
import com.ys.cunaco.ui.components.PhdClickableCard
import com.ys.cunaco.ui.components.PhdLayoutMenu
import com.ys.cunaco.ui.components.PhdMediumText
import com.ys.cunaco.ui.components.PhdTextBold
import com.ys.cunaco.viewmodel.LoginViewModel

// Pastel tones — misma paleta que BabyMenuScreen
private val cardMint = Color(0xFF9FE3D0)
private val cardTeal = Color(0xFF7FB8C4)
private val cardSky  = Color(0xFF9FD6E8)

private data class CounterMenuItemData(
    val title: String,
    val icon: Int,
    val route: String,
    val backgroundColor: Color
)

@Composable
fun BabyCounterSelectionScreen(
    navController: NavController,
    loginViewModel: LoginViewModel = hiltViewModel(),
    openDrawer: () -> Unit
) {
    val userRole by loginViewModel.userRole.collectAsState()
    val isWaiting = userRole == "waiting"

    val items = if (isWaiting) {
        listOf(
            CounterMenuItemData("Contracciones", R.drawable.icono_vacunas, CONTRACTION_COUNTER, cardTeal)
        )
    } else {
        listOf(
            CounterMenuItemData("Sueño", R.drawable.icono_sueno_bebe_registro, SLEEP_TRACKING, cardMint),
            CounterMenuItemData("Lactancia", R.drawable.icono_lactancia_registro, LACTATION_TRACKING, cardTeal),
            CounterMenuItemData("Popós", R.drawable.icono_popo_registro, POO_MAIN_SELECTION, cardSky),
            CounterMenuItemData("Huella de carbono", R.drawable.icon_huella_carbono_leaf, CARBON_FOOTPRINT, cardSky)
        )
    }

    PhdLayoutMenu(
        title = "Registros",
        navController = navController,
        openDrawer = openDrawer
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(20.dp),
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

            items.chunked(2).forEach { rowItems ->
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    rowItems.forEach { item ->
                        Box(modifier = Modifier.weight(1f)) {
                            PhdClickableCard(
                                title = item.title,
                                imageResId = item.icon,
                                backgroundColor = item.backgroundColor,
                                onClick = { navController.navigate(item.route) }
                            )
                        }
                    }
                    // Pad an odd last row so the single card doesn't stretch full width
                    if (rowItems.size == 1) {
                        Spacer(modifier = Modifier.weight(1f))
                    }
                }
                Spacer(modifier = Modifier.height(24.dp))
            }
        }
    }
}
