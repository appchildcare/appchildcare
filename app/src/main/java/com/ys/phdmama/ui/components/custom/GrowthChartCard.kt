package com.ys.phdmama.ui.components.custom

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.nativeCanvas
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavHostController
import com.ys.phdmama.navigation.NavRoutes
import com.ys.phdmama.ui.theme.primaryGray
import com.ys.phdmama.viewmodel.GrowthMilestonesViewModel



@Composable
fun GrowthChartCard(navController: NavHostController, growthMilestonesViewModel: GrowthMilestonesViewModel = viewModel(), babyId: String?) {
    var testId by remember { mutableStateOf("") }

    LaunchedEffect(Unit) {
        growthMilestonesViewModel.fetchBabyId(
            onSuccess = { baby ->
                if (!baby.isNullOrEmpty()) {
                    growthMilestonesViewModel.loadGrowthData(babyId)
                }
            },
            onSkip = {
                testId = ""
            },
            onError = {
                testId = ""
            }
        )

    }
//    val records = growthMilestonesViewModel.growthRecords.value
    Card(
        modifier = Modifier
            .fillMaxWidth(0.8f) // Ocupa 80% del ancho
            .aspectRatio(1.3f) // Relación de aspecto para mantener proporción
            .padding(16.dp),
        shape = RoundedCornerShape(16.dp),
        elevation = CardDefaults.cardElevation(4.dp)
    ) {
        Column(
            modifier = Modifier.fillMaxSize().padding(20.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = "Hitos del crecimiento",
                style = MaterialTheme.typography.titleMedium,
                color = primaryGray
            )

            Spacer(modifier = Modifier.height(16.dp))
            val records = growthMilestonesViewModel.growthRecords.value

            if (records.isNotEmpty()) {
                val months = records.map { it.ageInMonths }
                val weights = records.map { it.weight }
                val heights = records.map { it.height }
                val circumferences = records.map { it.headCircumference }

                GrowthChartWithGrid(
                    months = months,
                    weightValues = weights,
                    heightValues = heights,
                    headCircumferenceValues = circumferences,
                    onViewDetailClick = {
                        navController.navigate(NavRoutes.BORN_GROW_CHART_DETAILS)
                    },
                )
            } else {
                Text("Aún no se han ingresado datos de crecimiento...")
            }
        }
    }
}


@Composable
fun GrowthChartWithGrid(
//    navController: NavHostController,
    months: List<Int>,
    weightValues: List<Double>,
    heightValues: List<Double>,
    headCircumferenceValues: List<Double>,
    onViewDetailClick: () -> Unit
) {
    // Curva de referencia "normal"
    val normalGrowthCurve = List(months.size) { index ->
        6 + index * 0.2 // Simula un crecimiento incremental
    }

    Box(modifier = Modifier.fillMaxSize()) {
        Canvas(modifier = Modifier.matchParentSize()) {
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
                moveTo(
                    0f,
                    (size.height - (weightValues[0] / maxWeight.toFloat()) * maxBarHeight).toFloat()
                )
                months.forEachIndexed { index, _ ->
                    lineTo(
                        x = index * size.width / months.size,
                        y = (size.height - (weightValues[index] / maxWeight.toFloat()) * maxBarHeight).toFloat()
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
                moveTo(
                    0f,
                    (size.height - (heightValues[0] / maxHeight.toFloat()) * maxBarHeight).toFloat()
                )
                months.forEachIndexed { index, _ ->
                    lineTo(
                        x = index * size.width / months.size,
                        y = (size.height - (heightValues[index] / maxHeight.toFloat()) * maxBarHeight).toFloat()
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
                moveTo(
                    0f,
                    (size.height - (headCircumferenceValues[0] / maxCircumference.toFloat()) * maxBarHeight).toFloat()
                )
                months.forEachIndexed { index, _ ->
                    lineTo(
                        x = index * size.width / months.size,
                        y = (size.height - (headCircumferenceValues[index] / maxCircumference.toFloat()) * maxBarHeight).toFloat()
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
                moveTo(
                    0f,
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
                style = Stroke(width = 2.dp.toPx()),
            )

            // Eje X label
//            drawContext.canvas.nativeCanvas.apply {
//                drawText(
//                    "Meses",
//                    size.width / 2,
//                    size.height - 8, // 8px from bottom
//                    android.graphics.Paint().apply {
//                        color = android.graphics.Color.DKGRAY
//                        textAlign = android.graphics.Paint.Align.CENTER
//                        textSize = 32f
//                    }
//                )
//            }
//            // Eje Y label (rotado)
//            drawContext.canvas.nativeCanvas.apply {
//                save()
//                rotate(-90f, 24f, size.height / 2)
//                drawText(
//                    "Valor",
//                    24f,
//                    size.height / 2,
//                    android.graphics.Paint().apply {
//                        color = android.graphics.Color.DKGRAY
//                        textAlign = android.graphics.Paint.Align.CENTER
//                        textSize = 32f
//                    }
//                )
//                restore()
//            }
        }

        Spacer(Modifier.width(8.dp).height(20.dp))
        // Leyenda
        Column(
            modifier = Modifier
                //.align(Alignment.CenterStart)
                .padding(top = 120.dp)
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                LegendItem(color = Color.Red, label = "Peso")
                Spacer(Modifier.width(8.dp))
                LegendItem(color = Color.Blue, label = "Talla")
                Spacer(Modifier.width(8.dp))
//                LegendItem(color = Color.Green, label = "Perímetro cefálico")
//                Spacer(Modifier.width(8.dp))
//                LegendItem(color = Color.Gray, label = "Curva normal")
            }

        }

        Column(
            modifier = Modifier
                //.align(Alignment.CenterStart)
                .padding(top = 100.dp)
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
//                LegendItem(color = Color.Red, label = "Peso")
//                Spacer(Modifier.width(8.dp))
//                LegendItem(color = Color.Blue, label = "Talla")
//                Spacer(Modifier.width(8.dp))
                LegendItem(color = Color.Green, label = "Perímetro cefálico")
                Spacer(Modifier.width(8.dp))
                LegendItem(color = Color.Gray, label = "Curva normal")
            }

            // Spacer and Button
            Spacer(modifier = Modifier.height(16.dp))
            Button(
                onClick = { onViewDetailClick() },
                modifier = Modifier
                    .padding(bottom = 16.dp)
            ) {
                Text("Ver detalle")
            }
        }
    }

}


@Composable
fun LegendItem(color: Color, label: String) {
    Row(verticalAlignment = Alignment.CenterVertically) {
        Canvas(modifier = Modifier.size(12.dp)) {
            drawCircle(color = color)
        }
        Spacer(Modifier.width(4.dp))
        Text(label, style = MaterialTheme.typography.bodySmall)
    }
}