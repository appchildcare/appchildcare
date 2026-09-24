package com.ys.cunaco.ui.screens.born

import android.os.Build
import android.util.Log
import androidx.activity.ComponentActivity
import androidx.annotation.RequiresApi
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material.icons.filled.KeyboardArrowUp
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.pluralStringResource
import androidx.compose.ui.res.stringResource
import androidx.navigation.NavController
import androidx.navigation.NavHostController
import com.ys.cunaco.R
import com.ys.cunaco.navigation.NavRoutes
import com.ys.cunaco.navigation.NavRoutes.BORN_HEAD_CIRCUMFERENCE_CHART_DETAILS
import com.ys.cunaco.navigation.NavRoutes.BORN_HEIGHT_WEIGHT_CHART_DETAILS
import com.ys.cunaco.navigation.NavRoutes.BORN_RESOURCES
import com.ys.cunaco.navigation.NavRoutes.BORN_WEIGHT_CHART_DETAILS
import com.ys.cunaco.ui.components.PhdLayoutMenu
import com.ys.cunaco.ui.theme.primaryGray
import com.ys.cunaco.ui.theme.primaryTeal
import com.ys.cunaco.ui.theme.secondaryAqua
import com.ys.cunaco.ui.theme.secondaryCream
import com.ys.cunaco.viewmodel.BabyAge
import com.ys.cunaco.viewmodel.BabyDataViewModel
import com.ys.cunaco.viewmodel.BabyProfile
import com.ys.cunaco.viewmodel.GrowthMilestonesViewModel
import com.ys.cunaco.viewmodel.UserDataViewModel

// Pastel tones sampled from the reference design
private val cardMint = Color(0xFF9FE3D0)
private val cardTeal = Color(0xFF7FB8C4)
private val cardSky  = Color(0xFF9FD6E8)

@RequiresApi(Build.VERSION_CODES.O)
@Composable
fun BornDashboardScreen(
    navController: NavHostController,
    growthMilestonesViewModel: GrowthMilestonesViewModel = hiltViewModel(),
    userViewModel: UserDataViewModel = hiltViewModel(),
    babyDataViewModel: BabyDataViewModel = hiltViewModel(LocalContext.current as ComponentActivity),
    openDrawer: () -> Unit,
) {
    // Collect from ViewModel's StateFlow
    val selectedBaby by babyDataViewModel.selectedBaby.collectAsState()
    val babyList by babyDataViewModel.babyList.collectAsState()

    // Refresh all user data when the dashboard is entered
    LaunchedEffect(Unit) {
        babyDataViewModel.fetchBabies()
        userViewModel.createUserChecklists("born")
    }

    // Calculate age reactively
    val babyAgeInMonths = remember(selectedBaby?.birthDate) {
        selectedBaby?.let { baby ->
            babyDataViewModel.calculateCorrectedAge(baby.birthDate, baby.weeksBirth)
        }
    }

    // Load growth data when selected baby changes
    LaunchedEffect(selectedBaby?.id) {
        selectedBaby?.id?.let { id ->
            growthMilestonesViewModel.loadGrowthData(id)
            Log.d("BornDashboard", "Loading growth data for baby: ${selectedBaby?.name}")
        }
    }

    PhdLayoutMenu(
        title = "Panel",
        navController = navController,
        openDrawer = openDrawer
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState()),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {

            BabySelectorCard(
                babies = babyList,
                selectedBaby = selectedBaby,
                onBabySelected = { baby ->
                    Log.d("BornDashboard", "User selected baby: ${baby.name}")
                    babyDataViewModel.setSelectedBaby(baby)
                },
                babyAgeInMonths = babyAgeInMonths,
                gradientColors = listOf(
                    primaryTeal,
                    primaryTeal
                ),

                )

            selectedBaby?.let {
                HeadCircumferenceMilestonesRow(navController)
                WeightHeightCardsRow(navController)
                Spacer(modifier = Modifier.height(16.dp))
            }
            PediatricianCardsRow(navController)
        }
    }
}

@Composable
fun HeadCircumferenceMilestonesRow(navController: NavController) {
    Row(
        modifier = Modifier
            .fillMaxWidth(0.90f)
            .padding(horizontal = 8.dp),
        horizontalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        // Perímetro Cefálico - Left Column
        Column(modifier = Modifier.weight(1f)) {
            ClickableCard(
                title = stringResource(R.string.head_circumference_label),
                onClick = { navController.navigate(BORN_HEAD_CIRCUMFERENCE_CHART_DETAILS) },
                type = "head_circumference",
                backgroundColor = cardMint,
            )
        }

        // Desarrollo / Milestones - Right Column
        Column(modifier = Modifier.weight(1f)) {
            ClickableCard(
                title = stringResource(R.string.growth_milestones_description),
                onClick = { navController.navigate(BORN_RESOURCES) },
                type = "milestones",
                backgroundColor = cardSky,
            )
        }
    }
}

@Composable
fun HeadCircumferenceCard(navController: NavController) {
    Column(modifier = Modifier.fillMaxWidth(0.90f)) {
        ClickableCard(
            title = stringResource(R.string.head_circumference_label),
            onClick = {
                navController.navigate(BORN_HEAD_CIRCUMFERENCE_CHART_DETAILS)
            },
            type = "head_circumference",
            backgroundColor = cardMint,
        )
        Spacer(modifier = Modifier.height(16.dp))
    }
}

@Composable
fun GrowthMilestonesCard(navController: NavController) {
    Column(modifier = Modifier.fillMaxWidth(0.90f)) {
        ClickableCard(
            title = stringResource(R.string.growth_milestones_description),
            onClick = {
                navController.navigate(BORN_RESOURCES)
            },
            type = "milestones",
            backgroundColor = cardMint,
        )
        Spacer(modifier = Modifier.height(16.dp))
    }
}

@Composable
fun WeightHeightCardsRow(navController: NavController) {
    Row(
        modifier = Modifier
            .fillMaxWidth(0.90f)
            .padding(horizontal = 8.dp),
        horizontalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        // Weight Card - Left Column
        Column(
            modifier = Modifier.weight(1f)
        ) {
            ClickableCard(
                title = stringResource(R.string.weight_label),
//                description = stringResource(R.string.weight_description),
                onClick = {
                    navController.navigate(BORN_WEIGHT_CHART_DETAILS)
                },
                type = "weight",
                backgroundColor = cardTeal,
            )
        }

        // Height Card - Right Column
        Column(
            modifier = Modifier.weight(1f)
        ) {
            ClickableCard(
                title = stringResource(R.string.height_label),
//                description = stringResource(R.string.height_description),
                onClick = {
                    navController.navigate(BORN_HEIGHT_WEIGHT_CHART_DETAILS)
                },
                type = "height",
                backgroundColor = cardSky,
            )
        }
    }
}

@Composable
fun PediatricianCardsRow(navController: NavController) {
    Row(
        modifier = Modifier
            .fillMaxWidth(0.90f)
            .padding(horizontal = 8.dp),
        horizontalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        // Questions Card - Left Column
        Column(modifier = Modifier.weight(1f)) {
            ClickableCard(
                title = stringResource(R.string.pediatrician_question_label),
//                description = "",
                onClick = { navController.navigate(NavRoutes.PEDIATRICIAN_QUESTIONS) },
                type = "questions",
                backgroundColor = cardMint,
            )
        }

        // Visit Card - Right Column
        Column(modifier = Modifier.weight(1f)) {
            ClickableCard(
                title = stringResource(R.string.pediatrician_visit_label),
//                description = "",
                onClick = { navController.navigate(NavRoutes.PEDIATRICIAN_VISITS) },
                type = "visit",
                backgroundColor = cardTeal,
            )
        }
    }
}

@Composable
fun ClickableCard(
    title: String,
    description: String = "",
    onClick: () -> Unit,
    backgroundColor: Color,
    type: String? = "visit"
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onClick() },
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .aspectRatio(1f)
                .background(backgroundColor, RoundedCornerShape(28.dp)),
            contentAlignment = Alignment.Center
        ) {
            val imageRes = when (type) {
                "visit" -> R.drawable.icono_app_visita_pediatra
                "head_circumference" -> R.drawable.icono_app_perimetro
                "weight" -> R.drawable.mascota_peso_bebe
                "height" -> R.drawable.icono_app_altura
                "milestones" -> R.drawable.icono_app_signos_alerta
                else -> R.drawable.icono_app_pediatra
            }
            Image(
                painter = painterResource(id = imageRes),
                contentDescription = null,
                modifier = Modifier.fillMaxSize(0.55f)
            )
        }
        Spacer(modifier = Modifier.height(10.dp))
        Text(
            text = title.uppercase(),
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.Bold,
            color = Color(0xFF1F4E5F),
            textAlign = TextAlign.Center,
            modifier = Modifier.padding(horizontal = 4.dp)
        )
        if (description.isNotEmpty()) {
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = description,
                style = MaterialTheme.typography.bodyMedium,
                textAlign = TextAlign.Center,
                modifier = Modifier.padding(horizontal = 4.dp)
            )
        }
    }
}

@Composable
fun BabySelectorCard(
    babies: List<BabyProfile>,
    selectedBaby: BabyProfile?,
    onBabySelected: (BabyProfile) -> Unit,
    babyAgeInMonths: BabyAge?,
    gradientColors: List<Color> = listOf(secondaryAqua, secondaryAqua)
) {
    var expanded by remember { mutableStateOf(false) }

    Card(
        modifier = Modifier.padding(16.dp).fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        elevation = CardDefaults.cardElevation(4.dp),
        colors = CardDefaults.cardColors(containerColor = Color.Transparent)
    ) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(Brush.horizontalGradient(gradientColors))
        ) {
            Column(
                modifier = Modifier.fillMaxWidth().padding(16.dp)
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(
                        modifier = Modifier.size(64.dp).background(secondaryCream, shape = RoundedCornerShape(32.dp)),
                        contentAlignment = Alignment.Center
                    ) {
                        val initial = selectedBaby?.name?.firstOrNull()?.toString() ?: "?"
                        Text(text = initial, style = MaterialTheme.typography.titleMedium, color = Color.Gray)
                    }

                    Spacer(modifier = Modifier.width(16.dp))

                    Box {
                        Column(modifier = Modifier.clickable { if (babies.size > 1) expanded = true }) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Text(
                                    text = selectedBaby?.name ?: stringResource(R.string.baby_selector_label),
                                    style = MaterialTheme.typography.titleMedium,
                                    color = primaryGray
                                )
                                if (babies.size > 1) {
                                    Icon(
                                        imageVector = if (expanded) Icons.Default.KeyboardArrowUp else Icons.Default.KeyboardArrowDown,
                                        contentDescription = null,
                                        tint = primaryGray
                                    )
                                }
                            }
                            if (babyAgeInMonths != null) {
                                val yearsText = pluralStringResource(
                                    R.plurals.baby_selector_years,
                                    babyAgeInMonths.years,
                                    babyAgeInMonths.years
                                )
                                val monthsText = pluralStringResource(
                                    R.plurals.baby_selector_months,
                                    babyAgeInMonths.months,
                                    babyAgeInMonths.months
                                )

                                Text(
                                    text = "$yearsText ${stringResource(R.string.baby_selector_and)} $monthsText",
                                    style = MaterialTheme.typography.bodyMedium
                                )
                            }
                        }
                        DropdownMenu(expanded = expanded, onDismissRequest = { expanded = false }) {
                            babies.forEach { baby ->
                                DropdownMenuItem(
                                    text = { Text(baby.name) },
                                    onClick = { onBabySelected(baby); expanded = false }
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}
