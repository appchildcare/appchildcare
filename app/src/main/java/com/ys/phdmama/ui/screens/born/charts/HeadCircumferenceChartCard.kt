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
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavHostController
import com.ys.phdmama.navigation.NavRoutes
import com.ys.phdmama.ui.components.custom.LegendItem
import com.ys.phdmama.ui.theme.primaryGray
import com.ys.phdmama.viewmodel.GrowthMilestonesViewModel
import kotlin.math.ceil

@Composable
fun HeadCircumferenceChartCard(
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
            // Botón en esquina superior izquierda
//            IconButton(
//                onClick = {
//                    navController.navigate(NavRoutes.BORN_HEAD_CIRCUMFERENCE_CHART_DETAILS)
//                },
//                modifier = Modifier
//                    // .align(Alignment.TopStart)
//                   // .padding(bottom = 6.dp)
//                //.padding(8.dp)
//            ) {
//                //Text("Ver más", fontSize = 12.sp, color = MaterialTheme.colorScheme.primary)
//                Icon(
//                    imageVector = Icons.Default.Search,
//                    contentDescription = "Agregar nuevo dato",
//                    tint = MaterialTheme.colorScheme.primary
//                )
//            }
//            Text(
//                text = "Perímetro cefálico",
//                style = MaterialTheme.typography.titleMedium,
//                color = primaryGray
//            )

            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                IconButton(
                    onClick = {
                        navController.navigate(NavRoutes.BORN_HEAD_CIRCUMFERENCE_CHART_DETAILS)
                    }
                ) {
                    Icon(
                        imageVector = Icons.Default.Search,
                        contentDescription = "Agregar nuevo dato",
                        tint = MaterialTheme.colorScheme.primary
                    )
                }

                Text(
                    text = "Perímetro cefálico",
                    style = MaterialTheme.typography.titleMedium,
                    color = primaryGray
                )
            }

            Spacer(modifier = Modifier.height(16.dp))
            val records = growthMilestonesViewModel.growthRecords.value

            if (records.isNotEmpty()) {
                val months = records.map { it.ageInMonths }
                val circumferences = records.map { it.headCircumference }

                HeadCircumferenceChartWithGrid(
                    months = months,
                    headCircumferenceValues = circumferences,
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
fun HeadCircumferenceChartWithGrid(
    months: List<Int>,
    headCircumferenceValues: List<Double>,
    navController: NavHostController,
    onViewDetailClick: () -> Unit
) {
    val minY = 13.0
    val rawMax = headCircumferenceValues.maxOrNull() ?: minY
    val maxY = ceil(rawMax + 1)
    val steps = 6
    val stepSize = (maxY - minY) / (steps - 1)

    Box(modifier = Modifier.fillMaxSize()) {
        // Canvas para grid y curva
        Canvas(modifier = Modifier
            .matchParentSize()
            .padding(start = 40.dp, bottom = 24.dp)
        ) {
            val gridCols = 12
            val gridRows = steps - 1
            val cellWidth = size.width / gridCols
            val cellHeight = size.height / gridRows

            // Dibujar líneas horizontales (grid Y)
            for (i in 0..gridRows) {
                val y = i * cellHeight
                drawLine(
                    color = Color.LightGray,
                    start = Offset(0f, y),
                    end = Offset(size.width, y),
                    strokeWidth = 1.dp.toPx()
                )
            }

            // Dibujar líneas verticales (grid X)
            for (j in 0..gridCols) {
                val x = j * cellWidth
                drawLine(
                    color = Color.LightGray,
                    start = Offset(x, 0f),
                    end = Offset(x, size.height),
                    strokeWidth = 1.dp.toPx()
                )
            }

            // Dibujar curva de perímetro cefálico
            val path = Path().apply {
                months.forEachIndexed { index, month ->
                    val x = index * size.width / (months.size - 1)
                    val normalized = (headCircumferenceValues[index] - minY) / (maxY - minY)
                    val y = size.height - (normalized * size.height).toFloat()
                    if (index == 0) moveTo(x, y) else lineTo(x, y)
                }
            }

            drawPath(
                path = path,
                color = Color.Green,
                style = Stroke(width = 3.dp.toPx())
            )
        }



        // Etiquetas del eje Y
        Column(
            modifier = Modifier
                .fillMaxHeight()
                .padding(start = 4.dp, top = 20.dp, bottom = 44.dp),
            verticalArrangement = Arrangement.SpaceEvenly
        ) {
            for (i in 0 until steps) {
                val value = maxY - i * stepSize
                Text(
                    text = String.format("%.1f", value),
                    fontSize = 10.sp,
                    color = Color.Gray
                )
            }
        }

        // Etiquetas del eje X
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 4.dp, start = 40.dp, end = 8.dp)
                .align(Alignment.BottomCenter),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            months.forEachIndexed { index, month ->
                if (index % 1 == 0) {
                    Text(
                        text = month.toString(),
                        fontSize = 10.sp,
                        color = Color.Gray
                    )
                }
            }
        }

        // Leyenda
        Row(
            modifier = Modifier.padding(top = 120.dp, start = 16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            LegendItem(color = Color.Green, label = "Perímetro cefálico")
        }

        // Botón
        Button(
            onClick = { onViewDetailClick() },
            modifier = Modifier
                .padding(top = 216.dp)
                .align(Alignment.TopCenter)
        ) {
            Text("Ver detalle")
        }
    }
}

