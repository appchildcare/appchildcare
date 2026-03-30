package com.ys.phdmama.ui.screens.counters

import android.os.Build
import androidx.annotation.RequiresApi
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import com.ys.phdmama.ui.components.PhdLayoutMenu
import com.ys.phdmama.viewmodel.ContractionCounterViewModel
import com.ys.phdmama.viewmodel.ContractionInterval
import java.time.format.DateTimeFormatter

@RequiresApi(Build.VERSION_CODES.O)
@Composable
fun ContractionReportScreen(
    viewModel: ContractionCounterViewModel,
    navController: NavController,
    openDrawer: () -> Unit
) {

    // Trigger Firestore load as soon as the screen appears
    LaunchedEffect(Unit) {
        viewModel.loadContractionReport()
    }

    PhdLayoutMenu(
        title = "Reporte de Contracciones",
        navController = navController,
        openDrawer = openDrawer
    ) { innerPadding ->

        val intervals by viewModel.firestoreIntervals.collectAsState()
        val isLoading by viewModel.isLoadingReport.collectAsState()
        val timeFormatter = DateTimeFormatter.ofPattern("HH:mm:ss")
        val dateFormatter = DateTimeFormatter.ofPattern("dd/MM/yyyy")

        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) {
            when {
                // Loading state
                isLoading -> {
                    Column(
                        modifier = Modifier.align(Alignment.Center),
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        CircularProgressIndicator()
                        Text(
                            text = "Cargando contracciones...",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }

                // Empty state
                intervals.isEmpty() -> {
                    Text(
                        text = "No hay contracciones registradas aún.\nVuelve al contador para registrar una.",
                        modifier = Modifier
                            .align(Alignment.Center)
                            .padding(32.dp),
                        textAlign = TextAlign.Center,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        style = MaterialTheme.typography.bodyMedium
                    )
                }

                // Data state
                else -> {
                    LazyColumn(
                        modifier = Modifier.fillMaxSize(),
                        contentPadding = PaddingValues(horizontal = 16.dp, vertical = 16.dp),
                        verticalArrangement = Arrangement.spacedBy(16.dp)
                    ) {
                        items(intervals) { interval ->
                            ContractionIntervalCard(
                                interval = interval,
                                timeFormatter = timeFormatter,
                                dateFormatter = dateFormatter
                            )
                        }
                    }
                }
            }
        }
    }
}

@RequiresApi(Build.VERSION_CODES.O)
@Composable
fun ContractionIntervalCard(
    interval: ContractionInterval,
    timeFormatter: DateTimeFormatter,
    dateFormatter: DateTimeFormatter
) {
    val headerBg = MaterialTheme.colorScheme.primaryContainer
    val rowBg = MaterialTheme.colorScheme.surface
    val altRowBg = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f)
    val borderColor = MaterialTheme.colorScheme.outline.copy(alpha = 0.3f)

    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(12.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(modifier = Modifier.fillMaxWidth()) {

            // Date header row
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(headerBg)
                    .padding(horizontal = 12.dp, vertical = 8.dp)
            ) {
                Text(
                    text = interval.windowStart.format(dateFormatter),
                    fontWeight = FontWeight.Bold,
                    fontSize = 15.sp,
                    color = MaterialTheme.colorScheme.onPrimaryContainer
                )
            }

            HorizontalDivider(color = borderColor)

            // Interval + frequency row
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(headerBg.copy(alpha = 0.5f))
                    .padding(horizontal = 12.dp, vertical = 6.dp),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(
                    text = "${interval.windowStart.format(timeFormatter)} – ${interval.windowEnd.format(timeFormatter)}",
                    fontWeight = FontWeight.SemiBold,
                    fontSize = 14.sp,
                    color = MaterialTheme.colorScheme.onSurface
                )
                Text(
                    text = "Frecuencia: ${interval.frequency} contracción${if (interval.frequency != 1) "es" else ""}",
                    fontWeight = FontWeight.SemiBold,
                    fontSize = 13.sp,
                    color = Color.Gray
                )
            }

            HorizontalDivider(color = borderColor)

            // Column headers
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(MaterialTheme.colorScheme.surfaceVariant)
                    .padding(horizontal = 12.dp, vertical = 6.dp)
            ) {
                TableHeaderCell("Inicio", Modifier.weight(1f))
                TableHeaderCell("Fin", Modifier.weight(1f))
                TableHeaderCell("Duración", Modifier.weight(1f))
            }

            HorizontalDivider(color = borderColor)

            // Contraction rows
            interval.contractions.forEachIndexed { index, contraction ->
                val bg = if (index % 2 == 0) rowBg else altRowBg
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(bg)
                        .padding(horizontal = 12.dp, vertical = 8.dp)
                ) {
                    TableCell(contraction.start.format(timeFormatter), Modifier.weight(1f))
                    TableCell(contraction.end.format(timeFormatter), Modifier.weight(1f))
                    TableCell(
                        formatDuration(contraction.durationSeconds),
                        Modifier.weight(1f)
                    )
                }
                if (index < interval.contractions.lastIndex) {
                    HorizontalDivider(color = borderColor)
                }
            }
        }
    }
}

@Composable
private fun TableHeaderCell(text: String, modifier: Modifier = Modifier) {
    Text(
        text = text,
        modifier = modifier,
        fontWeight = FontWeight.Bold,
        fontSize = 13.sp,
        color = MaterialTheme.colorScheme.onSurfaceVariant
    )
}

@Composable
private fun TableCell(text: String, modifier: Modifier = Modifier) {
    Text(
        text = text,
        modifier = modifier,
        fontSize = 13.sp,
        color = MaterialTheme.colorScheme.onSurface
    )
}

fun formatDuration(durationSeconds: Long): String {
    val minutes = durationSeconds / 60
    val seconds = durationSeconds % 60
    return when {
        minutes >= 1 -> "$minutes minuto${if (minutes != 1L) "s" else ""}"
        else         -> "$seconds segundo${if (seconds != 1L) "s" else ""}"
    }
}
