import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavHostController
import com.ys.phdmama.navigation.NavRoutes
import com.ys.phdmama.ui.components.custom.LegendItem
import com.ys.phdmama.ui.theme.primaryGray
import com.ys.phdmama.viewmodel.GrowthMilestonesViewModel

@Composable
fun HeightLengthChartCard(
    navController: NavHostController,
    growthMilestonesViewModel: GrowthMilestonesViewModel = hiltViewModel(),
    babyId: String?
) {
    var testId by remember { mutableStateOf("") }

    LaunchedEffect(Unit) {
        growthMilestonesViewModel.fetchBabyId(
            onSuccess = { baby ->
                if (!baby.isNullOrEmpty()) {
                    growthMilestonesViewModel.loadGrowthData(babyId)
                }
            },
            onSkip = { testId = "" },
            onError = { testId = "" }
        )
    }

    Card(
        modifier = Modifier
            .fillMaxWidth(0.8f)
            .aspectRatio(1.3f)
            .padding(16.dp),
        shape = RoundedCornerShape(16.dp),
        elevation = CardDefaults.cardElevation(4.dp)
    ) {
        Column(
            modifier = Modifier.fillMaxSize().padding(20.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {

            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                IconButton(
                    onClick = {
                        navController.navigate(NavRoutes.BORN_HEIGHT_WEIGHT_CHART_DETAILS)
                    }
                ) {
                    Icon(
                        imageVector = Icons.Default.Search,
                        contentDescription = "Agregar nuevo dato",
                        tint = MaterialTheme.colorScheme.primary
                    )
                }

                Text(
                    text = "Peso/altura",
                    style = MaterialTheme.typography.titleMedium,
                    color = primaryGray
                )
            }

            Spacer(modifier = Modifier.height(16.dp))
            val records = growthMilestonesViewModel.growthRecords.value

            if (records.isNotEmpty()) {
                val months = records.map { it.ageInMonths }
                val heights = records.map { it.height }
                val weights = records.map { it.weight }

                HeightLengthChartWithGrid(
                    months = months,
                    heights = heights,
                    weights = weights,
                    navController  = navController,
                    onViewDetailClick = {
//                        navController.navigate(NavRoutes.BORN_GROW_CHART_DETAILS) {
//                            popUpTo(0) { inclusive = true }
//                        }
                    },
                )
            } else {
                Text("Aún no se han ingresado datos de perímetro cefálico...")
            }
        }
    }
}


@Composable
fun HeightLengthChartWithGrid(
    months: List<Int>,
    heights: List<Double>,
    weights: List<Double>,
    navController: NavHostController,
    onViewDetailClick: () -> Unit
) {
    Box(modifier = Modifier.fillMaxSize()) {
        Canvas(modifier = Modifier.matchParentSize()) {
            val cellSize = size.width / 12
            val gridRows = 10
            val gridCols = 12

            // Draw grid
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

            // Draw weight curve
            val maxWeight = weights.maxOrNull() ?: 1
            val maxBarHeight = size.height
            val weightPath = Path().apply {
                moveTo(
                    0f,
                    ((size.height - (weights[0] / maxWeight.toFloat()) * maxBarHeight).toFloat())
                )
                months.forEachIndexed { index, _ ->
                    lineTo(
                        x = index * size.width / months.size,
                        y = ((size.height - (weights[index] / maxWeight.toFloat()) * maxBarHeight).toFloat())
                    )
                }
            }
            drawPath(
                path = weightPath,
                color = Color.Red,
                style = Stroke(width = 3.dp.toPx())
            )

            // Draw height curve
            val maxHeight = heights.maxOrNull() ?: 1
            val heightPath = Path().apply {
                moveTo(
                    0f,
                    ((size.height - (heights[0] / maxHeight.toFloat()) * maxBarHeight).toFloat())
                )
                months.forEachIndexed { index, _ ->
                    lineTo(
                        x = index * size.width / months.size,
                        y = ((size.height - (heights[index] / maxHeight.toFloat()) * maxBarHeight).toFloat())
                    )
                }
            }
            drawPath(
                path = heightPath,
                color = Color.Blue,
                style = Stroke(width = 3.dp.toPx())
            )
        }

        Spacer(Modifier.width(8.dp).height(20.dp))
        // Legend
        Row(
            modifier = Modifier.padding(top = 120.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            LegendItem(color = Color.Red, label = "Peso")
            Spacer(Modifier.width(8.dp))
            LegendItem(color = Color.Blue, label = "Talla")
        }

        // Spacer and Button
        Spacer(modifier = Modifier.height(16.dp))
        Button(
            onClick = { onViewDetailClick() },
            modifier = Modifier.padding(top = 216.dp)
        ) {
            Text("Ver detalle")
        }
    }
}
