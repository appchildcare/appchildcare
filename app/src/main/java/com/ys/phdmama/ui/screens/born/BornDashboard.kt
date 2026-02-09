package com.ys.phdmama.ui.screens.born

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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.compose.ui.platform.LocalContext
import androidx.navigation.NavController
import androidx.navigation.NavHostController
import com.ys.phdmama.R
import com.ys.phdmama.navigation.NavRoutes
import com.ys.phdmama.navigation.NavRoutes.BORN_HEAD_CIRCUMFERENCE_CHART_DETAILS
import com.ys.phdmama.navigation.NavRoutes.BORN_HEIGHT_WEIGHT_CHART_DETAILS
import com.ys.phdmama.navigation.NavRoutes.BORN_WEIGHT_CHART_DETAILS
import com.ys.phdmama.ui.components.PhdLayoutMenu
import com.ys.phdmama.ui.theme.primaryGray
import com.ys.phdmama.ui.theme.secondaryAqua
import com.ys.phdmama.ui.theme.secondaryLightGray
import com.ys.phdmama.viewmodel.BabyAge
import com.ys.phdmama.viewmodel.BabyDataViewModel
import com.ys.phdmama.viewmodel.BabyProfile
import com.ys.phdmama.viewmodel.GrowthMilestonesViewModel
import com.ys.phdmama.viewmodel.UserDataViewModel

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
            Log.d("BornDashboard", "Loading data for baby: ${selectedBaby?.name}")
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
                babyAgeInMonths = babyAgeInMonths
            )

            selectedBaby?.let {
                HeadCircumferenceCard(navController)
                WeightHeightCardsRow(navController)
                Spacer(modifier = Modifier.height(16.dp))
            }

            userViewModel.createUserChecklists("born")
            PediatricianQuestionsScreen(navController)
            Spacer(modifier = Modifier.height(16.dp))
            PediatricianVisitQuestionsScreen(navController)
        }
    }
}

@Composable
fun HeadCircumferenceCard(navController: NavController) {
    Column(modifier = Modifier.fillMaxWidth(0.90f)) {
        ClickableCard(
            title = "Perímetro cefálico",
            description = "Seguimiento del crecimiento del perímetro cefálico",
            onClick = {
                navController.navigate(BORN_HEAD_CIRCUMFERENCE_CHART_DETAILS)
            },
            color = secondaryAqua,
            type = "head_circumference"
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
                title = "Peso",
                description = "Seguimiento del peso del bebé",
                onClick = {
                    navController.navigate(BORN_WEIGHT_CHART_DETAILS) // Create new screen
                },
                color = secondaryAqua,
                type = "weight"
            )
        }

        // Height Card - Right Column
        Column(
            modifier = Modifier.weight(1f)
        ) {
            ClickableCard(
                title = "Altura",
                description = "Seguimiento de la altura del bebé",
                onClick = {
                    navController.navigate(BORN_HEIGHT_WEIGHT_CHART_DETAILS)
                },
                color = secondaryAqua,
                type = "height"
            )
        }
    }
}

@Composable
fun PediatricianQuestionsScreen(navController: NavController) {
    Column(modifier = Modifier.fillMaxWidth(0.90f)) {
        ClickableCard(
            title = "Preguntas al pediatra",
            description = "",
            onClick = { navController.navigate(NavRoutes.PEDIATRICIAN_QUESTIONS) },
            color = secondaryAqua,
            type = "questions"
        )
    }
}

@Composable
fun PediatricianVisitQuestionsScreen(navController: NavController) {
    Column(modifier = Modifier.fillMaxWidth(0.90f)) {
        ClickableCard(
            title = "Visitas al pediatra",
            description = "",
            onClick = { navController.navigate(NavRoutes.PEDIATRICIAN_VISITS) },
            color = secondaryAqua,
            type = "visit"
        )
    }
}

@Composable
fun ClickableCard(
    title: String,
    description: String,
    onClick: () -> Unit,
    color: Color = MaterialTheme.colorScheme.surface,
    type: String? = "visit" // Default to null, can be "visit", "questions", "head_circumference" or null for different images
) {
    val cardShape = RoundedCornerShape(16.dp)
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onClick() },
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp),
        colors = CardDefaults.cardColors(
            containerColor = color // This sets background within rounded shape
        )

    ) {
        Column(
            modifier = Modifier.padding(12.dp)
        ) {
            Text(
                text = title,
                style = MaterialTheme.typography.titleMedium,
                modifier = Modifier.padding(bottom = 16.dp)
            )
            when (type) {
                "visit" -> {
                    Image(
                        painter = painterResource(id = R.drawable.icono_app_visita_pediatra),
                        contentDescription = "image",
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(180.dp)
                    )
                }
                "head_circumference" -> {
                    Image(
                        painter = painterResource(id = R.drawable.icono_app_perimetro),
                        contentDescription = "head circumference image",
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(80.dp)
                    )
                }
                "weight" -> {
                    Image(
                        painter = painterResource(id = R.drawable.mascota_peso_bebe),
                        contentDescription = "weight tracking image",
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(120.dp)
                    )
                }
                "height" -> {
                    Image(
                        painter = painterResource(id = R.drawable.icono_app_altura),
                        contentDescription = "height tracking image",
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(120.dp)
                    )
                }
                else -> {
                    Image(
                        painter = painterResource(id = R.drawable.icono_app_pediatra),
                        contentDescription = "image",
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(180.dp)
                    )
                }
            }
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = description,
                style = MaterialTheme.typography.bodyMedium
            )
        }
    }
}

@Composable
fun BabySelectorCard(
    babies: List<BabyProfile>,
    selectedBaby: BabyProfile?,
    onBabySelected: (BabyProfile) -> Unit,
    babyAgeInMonths: BabyAge?
) {
    var expanded by remember { mutableStateOf(false) }

    Card(
        modifier = Modifier
            .padding(16.dp),
        shape = RoundedCornerShape(16.dp),
        elevation = CardDefaults.cardElevation(4.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .background(secondaryAqua)
                .padding(16.dp)
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Box(
                    modifier = Modifier
                        .size(64.dp)
                        .background(secondaryLightGray, shape = RoundedCornerShape(32.dp)),
                    contentAlignment = Alignment.Center
                ) {
                    val initial = selectedBaby?.name?.firstOrNull()?.toString() ?: "?"
                    Text(text = initial, style = MaterialTheme.typography.titleMedium, color = Color.White)
                }

                Spacer(modifier = Modifier.width(16.dp))

                // Dropdown anchor: name + age + arrow
                Box {
                    Column(
                        modifier = Modifier
                            .clickable { expanded = true }
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text(
                                text = selectedBaby?.name ?: "Seleccionar bebé",
                                style = MaterialTheme.typography.titleMedium,
                                color = primaryGray
                            )
                            if (babies.size > 1) {
                                Icon(
                                    imageVector = if (expanded) Icons.Default.KeyboardArrowUp else Icons.Default.KeyboardArrowDown,
                                    contentDescription = if (expanded) "Collapse dropdown" else "Expand dropdown",
                                    tint = primaryGray
                                )
                            }
                        }
                        if (babyAgeInMonths != null) {
                            Text(
                                text = "${babyAgeInMonths.years} años y ${babyAgeInMonths.months} meses",
                                style = MaterialTheme.typography.bodyMedium
                            )
                        }
                    }
                    DropdownMenu(
                        expanded = expanded,
                        onDismissRequest = { expanded = false }
                    ) {
                        babies.forEach { baby ->
                            DropdownMenuItem(
                                text = { Text(baby.name) },
                                onClick = {
                                    onBabySelected(baby)
                                    expanded = false
                                }
                            )
                        }
                    }
                }
            }
        }
    }
}
