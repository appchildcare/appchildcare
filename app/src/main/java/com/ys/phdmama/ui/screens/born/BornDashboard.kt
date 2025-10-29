package com.ys.phdmama.ui.screens.born

import android.os.Build
import android.util.Log
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
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import androidx.navigation.NavHostController
import com.ys.phdmama.R
import com.ys.phdmama.navigation.NavRoutes
import com.ys.phdmama.navigation.NavRoutes.BORN_HEAD_CIRCUMFERENCE_CHART_DETAILS
import com.ys.phdmama.navigation.NavRoutes.BORN_HEIGHT_WEIGHT_CHART_DETAILS
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
    growthMilestonesViewModel: GrowthMilestonesViewModel = viewModel(),
    userViewModel: UserDataViewModel = viewModel(),
    dashboardViewModel: BabyDataViewModel = viewModel(),
    babyDataViewModel: BabyDataViewModel = viewModel(),
    openDrawer: () -> Unit,
    babyId: String?
) {
    if (babyId != null) {
        Log.d("BABY ID received", babyId)
    }
    val babyList by babyDataViewModel.babyList.collectAsStateWithLifecycle()
    var selectedBaby by remember { mutableStateOf<BabyProfile?>(null) }
    var babyAgeInMonths by remember { mutableStateOf<BabyAge?>(null) }

    LaunchedEffect(Unit) {
        babyId?.let { babyDataViewModel.fetchBabies(it) }
    }

    LaunchedEffect(babyList) {
        if (selectedBaby == null && babyList.isNotEmpty()) {
            selectedBaby = babyList.first()
            growthMilestonesViewModel.loadGrowthData(babyList.first().id) // Load data immediately
        }
    }

    LaunchedEffect(selectedBaby) {
        selectedBaby?.birthDate?.let {
            babyAgeInMonths = babyDataViewModel.calculateAgeInMonths(it)
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
                onBabySelected = {
                    selectedBaby = it
                    growthMilestonesViewModel.loadGrowthData(it.id) // Reemplaza testId logic
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
                    navController.navigate(BORN_HEIGHT_WEIGHT_CHART_DETAILS)
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
                    navController.navigate(BORN_HEIGHT_WEIGHT_CHART_DETAILS) //TODO: Separate screen for height
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
                        painter = painterResource(id = R.mipmap.peditrician_baby),
                        contentDescription = "image",
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(180.dp)
                    )
                }
                "head_circumference" -> {
                    // Add an appropriate image for head circumference
                    // Replace with your actual drawable resource
//                    Image(
//                        painter = painterResource(id = R.mipmap.recien_nacido), // You'll need to add this image
//                        contentDescription = "head circumference image",
//                        modifier = Modifier
//                            .fillMaxWidth()
//                            .height(180.dp)
//                    )
                }
                "weight" -> {
                    Image(
                        painter = painterResource(id = R.mipmap.icon_baby_peso),
                        contentDescription = "weight tracking image",
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(120.dp)
                    )
                }
                "height" -> {
                    Image(
                        painter = painterResource(id = R.mipmap.icon_baby_height),
                        contentDescription = "height tracking image",
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(120.dp)
                    )
                }
                else -> {
                    Image(
                        painter = painterResource(id = R.mipmap.pediatra),
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
