package com.ys.phdmama.ui.screens.born

import HeadCircumferenceChartCard
import HeightLengthChartCard
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
import com.ys.phdmama.ui.components.PhdLayoutMenu
import com.ys.phdmama.ui.components.custom.GrowthChartCard
import com.ys.phdmama.ui.theme.primaryGray
import com.ys.phdmama.ui.theme.secondaryAqua
import com.ys.phdmama.ui.theme.secondaryLightGray
import com.ys.phdmama.ui.theme.secondaryYellow
import com.ys.phdmama.viewmodel.BabyAge
import com.ys.phdmama.viewmodel.BabyDataViewModel
import com.ys.phdmama.viewmodel.BabyProfile
import com.ys.phdmama.viewmodel.GrowthMilestonesViewModel
import com.ys.phdmama.viewmodel.UserDataViewModel
import kotlin.random.Random

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
                GrowthChartCard(navController, growthMilestonesViewModel, it.id)
                HeadCircumferenceChartCard(navController, growthMilestonesViewModel, it.id)
                HeightLengthChartCard(navController, growthMilestonesViewModel, it.id)
            }

            userViewModel.createUserChecklists("born")
            PediatricianQuestionsScreen(navController)
            Spacer(modifier = Modifier.height(16.dp))
            PediatricianVisitQuestionsScreen(navController)
        }
    }
}

fun generateIncrementalValues(min: Int, max: Int, size: Int): List<Int> {
    val values = mutableListOf(min)
    for (i in 1 until size) {
        val previousValue = values.last()
        val remainingRange = (max - previousValue).coerceAtLeast(1)
        val increment = Random.nextInt(0, remainingRange) // Ajuste para evitar errores de rango
        values.add((previousValue + increment).coerceAtMost(max))
    }
    return values
}

@Composable
fun PediatricianQuestionsScreen(navController: NavController) {
    Column(modifier = Modifier.fillMaxWidth(0.75f)) {
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
    Column(modifier = Modifier.fillMaxWidth(0.75f)) {
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
    type: String? = "visit" // Default to null, can be "visit" or null for different images
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
            if (type == "visit") {
                Image(
                    painter = painterResource(id = R.mipmap.visitas_pediatra),
                    contentDescription = "image",
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(180.dp)
                )
            } else {
                Image(
                    painter = painterResource(id = R.mipmap.pediatra),
                    contentDescription = "image",
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(180.dp)
                )
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
            .fillMaxWidth(0.8f)
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