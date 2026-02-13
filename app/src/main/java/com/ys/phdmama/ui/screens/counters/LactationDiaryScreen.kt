package com.ys.phdmama.ui.screens.counters

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavHostController
import com.ys.phdmama.model.Lactation
import com.ys.phdmama.model.LactationWeekDay
import com.ys.phdmama.ui.components.PhdLayoutMenu
import com.ys.phdmama.viewmodel.LactancyDiaryViewModel

@Composable
fun LactationDiaryScreen(
    babyId: String?,
    viewModel: LactancyDiaryViewModel = hiltViewModel(),
    navController: NavHostController,
    openDrawer: () -> Unit
) {
    val lactancyEntries by viewModel.lactationEntries.collectAsStateWithLifecycle()
    val weekDays by viewModel.weekDays.collectAsStateWithLifecycle()

    LaunchedEffect(Unit) {
        if (babyId != null) {
            viewModel.fetchLactancyData(babyId)
        }
    }

    PhdLayoutMenu(
        title = "Reporte de lactancia",
        navController = navController,
        openDrawer = openDrawer
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(20.dp)
        ) {

            val lightGreen = Color(0xFF8BC34A)
            val darkGreen = Color(0xFF4CAF50)
            val backgroundColor = Color(0xFFFCE4EC)

            // Header Section - Now passing lactancyEntries
            LactancyHeaderSection(
                selectedDate = "Lunes 25",
                lightGreen = lightGreen,
                weekDays = weekDays
            )

            // Lactancy Records
            LazyColumn(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f)
                    .padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                lactancyEntries.forEach { dayEntry ->
                    item {
                        Text(
                            text = dayEntry.dayName,
                            fontSize = 16.sp,
                            fontWeight = FontWeight.Bold,
                            color = darkGreen,
                            modifier = Modifier.padding(bottom = 8.dp)
                        )
                    }
                    items(dayEntry.items) { lactation ->
                        LactancyRecordCard(
                            entry = lactation,
                            backgroundColor = backgroundColor,
                            darkGreen = darkGreen
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun LactancyHeaderSection(
    selectedDate: String,
    lightGreen: Color,
    weekDays: List<LactationWeekDay> = emptyList()

) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .background(
                lightGreen,
                RoundedCornerShape(bottomStart = 24.dp, bottomEnd = 24.dp)
            )
            .padding(16.dp)
    ) {
        Spacer(modifier = Modifier.height(16.dp))
        Spacer(modifier = Modifier.height(16.dp))

        // Week Calendar for Lactancy - Now using actual data
        LactancyWeekCalendar(weekDays = weekDays)
        Spacer(modifier = Modifier.height(12.dp))

        // Time Scale
        LactancyTimeScale()
        Spacer(modifier = Modifier.height(16.dp))

        // Selected Date Navigation
        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(12.dp),
            colors = CardDefaults.cardColors(containerColor = lightGreen)
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(
                        text = "Fecha actual",
                        color = Color.White,
                        fontSize = 12.sp
                    )
                    Text(
                        text = selectedDate,
                        color = Color.White,
                        fontSize = 20.sp,
                        fontWeight = FontWeight.Bold
                    )
                }
            }
        }
    }
}

@Composable
fun LactancyWeekCalendar(
    weekDays: List<LactationWeekDay> = emptyList()
) {
    Column(
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        weekDays.forEach { day ->
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Text(
                    text = day.name,
                    color = if (day.isSelected) Color(0xFF5D4037) else Color.Gray,
                    fontSize = 12.sp,
                    fontWeight = if (day.isSelected) FontWeight.SemiBold else FontWeight.Normal,
                    modifier = Modifier.width(80.dp)
                )

                Row(
                    modifier = Modifier.weight(1f),
                    horizontalArrangement = Arrangement.spacedBy(4.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    repeat(day.lactationCount) { index ->
                        Box(
                            modifier = Modifier
                                .size(12.dp)
                                .background(
                                    Color(0xFF8D6E63),
                                    RoundedCornerShape(6.dp)
                                )
                        )
                    }

                    if (day.lactationCount == 0) {
                        Text(
                            text = "Sin registros",
                            color = Color.Gray,
                            fontSize = 10.sp,
                            fontStyle = FontStyle.Italic
                        )
                    }
                }

                Text(
                    text = "${day.lactationCount}",
                    color = if (day.isSelected) Color(0xFF5D4037) else Color.Gray,
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.width(20.dp),
                    textAlign = TextAlign.End
                )
            }
        }
    }
}


@Composable
fun LactancyTimeScale() {
    val times = listOf("6h", "8h", "10h", "12h", "14h", "16h", "18h", "20h", "22h", "0h", "2h", "4h", "6h")

    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        times.forEach { time ->
            Text(
                text = time,
                color = Color.Gray,
                fontSize = 10.sp
            )
        }
    }
}

@Composable
fun LactancyRecordCard(
    entry: Lactation,
    backgroundColor: Color,
    darkGreen: Color
) {
    val startTime = formatHourFraction(entry.startHourFraction)
    val endTime = formatHourFraction(entry.startHourFraction + (entry.durationHours / 60f))
    val durationFormatted = formatLactancyDuration(entry.durationHours)

    // Get lactation type info
    val lactationType = getLactationTypeInfo(entry.lactancyType ?: "natural")

    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = backgroundColor),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    // Lactation type icon and label
                    Text(
                        text = lactationType.emoji,
                        fontSize = 16.sp
                    )

                    Text(
                        text = lactationType.displayName,
                        color = lactationType.color,
                        fontSize = 14.sp,
                        fontWeight = FontWeight.SemiBold
                    )

                    Text(
                        text = "• $durationFormatted",
                        color = Color(0xFFE91E63),
                        fontSize = 12.sp
                    )
                }

                Icon(
                    imageVector = Icons.Default.ArrowForward,
                    contentDescription = "Details",
                    tint = Color(0xFFE91E63),
                    modifier = Modifier.size(20.dp)
                )
            }

            Spacer(modifier = Modifier.height(8.dp))

            // Lactation type badge
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.Start
            ) {
                Card(
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(
                        containerColor = lactationType.color.copy(alpha = 0.1f)
                    ),
                    modifier = Modifier.padding(bottom = 8.dp)
                ) {
                    Text(
                        text = lactationType.displayName,
                        color = lactationType.color,
                        fontSize = 10.sp,
                        fontWeight = FontWeight.Medium,
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                    )
                }
            }

            Column(
                modifier = Modifier.fillMaxWidth(),
                horizontalAlignment = Alignment.End
            ) {
                Text(
                    text = "Ha tomando leche de",
                    color = Color(0xFFE91E63),
                    fontSize = 12.sp
                )

                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    Text(
                        text = "⏰",
                        fontSize = 12.sp
                    )
                    Text(
                        text = "$startTime a $endTime",
                        color = darkGreen,
                        fontSize = 14.sp,
                        fontWeight = FontWeight.SemiBold
                    )
                }
            }

            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 4.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "⏰ Tiempo desde última toma • 2h 30min",
                    color = Color.Gray,
                    fontSize = 10.sp
                )
            }
        }
    }
}

// Data class to hold lactation type display information
data class LactationTypeInfo(
    val displayName: String,
    val emoji: String,
    val color: Color
)

// Helper function to get lactation type display info
fun getLactationTypeInfo(lactancyType: String): LactationTypeInfo {
    return when (lactancyType.lowercase()) {
        "natural" -> LactationTypeInfo(
            displayName = "Leche materna",
            emoji = "🤱",
            color = Color(0xFF4CAF50) // Green for natural
        )
        "formula" -> LactationTypeInfo(
            displayName = "Leche de fórmula",
            emoji = "🍼",
            color = Color(0xFF2196F3) // Blue for formula
        )
        else -> LactationTypeInfo(
            displayName = "Leche materna",
            emoji = "🤱",
            color = Color(0xFF4CAF50)
        )
    }
}

fun getBreastText(breast: String): String {
    return when (breast.lowercase()) {
        "left" -> "Izquierdo"
        "right" -> "Derecho"
        "both" -> "Ambos"
        else -> "Ambos"
    }
}

fun formatLactancyDuration(durationMinutes: Float): String {
    val hours = (durationMinutes / 60).toInt()
    val minutes = (durationMinutes % 60).toInt()

    return if (hours > 0) {
        String.format("%dh %02dmin", hours, minutes)
    } else {
        String.format("%02dmin", minutes)
    }
}

fun formatHourFraction(hourFraction: Float): String {
    val hours = hourFraction.toInt()
    val minutes = ((hourFraction - hours) * 60).toInt()
    return String.format("%02d:%02d", hours, minutes)
}
