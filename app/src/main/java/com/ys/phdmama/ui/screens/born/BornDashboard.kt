package com.ys.phdmama.ui.screens.born

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Menu
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavHostController
import com.ys.phdmama.ui.components.BottomNavigationBar
import com.ys.phdmama.viewmodel.BabyDataViewModel
import com.ys.phdmama.viewmodel.UserDataViewModel
import kotlin.random.Random

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun BornDashboardScreen(
    navController: NavHostController,
    userViewModel: UserDataViewModel = viewModel(),
    dashboardViewModel: BabyDataViewModel = viewModel(),
    openDrawer: () -> Unit
) {
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Panel") },
                navigationIcon = {
                    IconButton(onClick = openDrawer) {
                        Icon(Icons.Default.Menu, contentDescription = "Menu")
                    }
                }
            )
        },
        bottomBar = { BottomNavigationBar(navController) }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            userViewModel.createUserChecklists("born")
            BabyInfoCard(name = "Pepito", ageInMonths = 8)
            Spacer(modifier = Modifier.height(16.dp))
            GrowthChartCard()
        }
    }
}

@Composable
fun BabyInfoCard(name: String, ageInMonths: Int) {
    Card(
        modifier = Modifier
            .fillMaxWidth(0.8f)
            .padding(16.dp),
        shape = RoundedCornerShape(16.dp),
        elevation = CardDefaults.cardElevation(4.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Avatar
            Box(
                modifier = Modifier
                    .size(64.dp)
                    .background(Color.LightGray, shape = RoundedCornerShape(32.dp)),
                contentAlignment = Alignment.Center
            ) {
                Text(text = "P", style = MaterialTheme.typography.titleMedium, color = Color.White)
            }

            Spacer(modifier = Modifier.width(16.dp))

            // Nombre y edad
            Column {
                Text(
                    text = name,
                    style = MaterialTheme.typography.titleLarge,
                    color = MaterialTheme.colorScheme.primary
                )
                Text(
                    text = "$ageInMonths meses",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onBackground
                )
            }
        }
    }
}

@Composable
fun GrowthChartCard() {
    Card(
        modifier = Modifier
            .fillMaxWidth(0.8f) // Ocupa 80% del ancho
            .aspectRatio(1.3f) // Relación de aspecto para mantener proporción
            .padding(16.dp),
        shape = RoundedCornerShape(16.dp),
        elevation = CardDefaults.cardElevation(4.dp)
    ) {
        Column(
            modifier = Modifier.fillMaxSize().padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = "Hitos del crecimiento",
                style = MaterialTheme.typography.titleMedium,
                color = MaterialTheme.colorScheme.primary
            )

            Spacer(modifier = Modifier.height(16.dp))

            GrowthChartWithGrid()
        }
    }
}

@Composable
fun GrowthChartWithGrid() {
    val months = (0..12).toList() // Meses de 0 a 24
    val weightValues = generateIncrementalValues(2, 10, months.size)
    val heightValues = generateIncrementalValues(50, 100, months.size)
    val headCircumferenceValues = generateIncrementalValues(30, 40, months.size)

    // Curva de referencia "normal"
    val normalGrowthCurve = List(months.size) { index ->
        6 + index * 0.2 // Simula un crecimiento incremental
    }

    Canvas(modifier = Modifier.fillMaxSize()) {
        val cellSize = size.width / 12
        val gridRows = 10
        val gridCols = 12

        // Dibujar cuadrícula
        for (i in 0..gridRows) {
            val y = i * cellSize
            drawLine(
                color = Color.LightGray,
                start = Offset(0f, y),
                end = Offset(size.width, y),
                strokeWidth = 1.dp.toPx()
            )
        }

        for (j in 0..gridCols) {
            val x = j * cellSize
            drawLine(
                color = Color.LightGray,
                start = Offset(x, 0f),
                end = Offset(x, size.height),
                strokeWidth = 1.dp.toPx()
            )
        }

        // Dibujar líneas del gráfico para cada métrica
        val maxWeight = weightValues.maxOrNull() ?: 1
        val maxHeight = heightValues.maxOrNull() ?: 1
        val maxCircumference = headCircumferenceValues.maxOrNull() ?: 1
        val maxBarHeight = size.height

        // Dibujar curva de peso
        val weightPath = Path().apply {
            moveTo(0f, size.height - (weightValues[0] / maxWeight.toFloat()) * maxBarHeight)
            months.forEachIndexed { index, _ ->
                lineTo(
                    x = index * size.width / months.size,
                    y = size.height - (weightValues[index] / maxWeight.toFloat()) * maxBarHeight
                )
            }
        }
        drawPath(
            path = weightPath,
            color = Color.Red,
            style = Stroke(width = 3.dp.toPx())
        )

        // Dibujar curva de talla
        val heightPath = Path().apply {
            moveTo(0f, size.height - (heightValues[0] / maxHeight.toFloat()) * maxBarHeight)
            months.forEachIndexed { index, _ ->
                lineTo(
                    x = index * size.width / months.size,
                    y = size.height - (heightValues[index] / maxHeight.toFloat()) * maxBarHeight
                )
            }
        }
        drawPath(
            path = heightPath,
            color = Color.Blue,
            style = Stroke(width = 3.dp.toPx())
        )

        // Dibujar curva de perímetro cefálico
        val circumferencePath = Path().apply {
            moveTo(0f, size.height - (headCircumferenceValues[0] / maxCircumference.toFloat()) * maxBarHeight)
            months.forEachIndexed { index, _ ->
                lineTo(
                    x = index * size.width / months.size,
                    y = size.height - (headCircumferenceValues[index] / maxCircumference.toFloat()) * maxBarHeight
                )
            }
        }
        drawPath(
            path = circumferencePath,
            color = Color.Green,
            style = Stroke(width = 3.dp.toPx())
        )

        // Dibujar curva de crecimiento "normal"
        val normalPath = Path().apply {
            moveTo(0f,
                (size.height - (normalGrowthCurve[0] / maxWeight.toFloat()) * maxBarHeight).toFloat()
            )
            months.forEachIndexed { index, _ ->
                lineTo(
                    x = index * size.width / months.size,
                    y = (size.height - (normalGrowthCurve[index] / maxWeight.toFloat()) * maxBarHeight).toFloat()
                )
            }
        }
        drawPath(
            path = normalPath,
            color = Color.Gray,
            style = Stroke(width = 2.dp.toPx())
        )
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