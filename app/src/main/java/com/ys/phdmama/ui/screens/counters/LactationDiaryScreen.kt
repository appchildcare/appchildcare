package com.ys.phdmama.ui.screens.counters

import Nap
import WeekDay
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
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavHostController
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
//            val darkPink = Color(0xFFC2185B)
            val darkGreen = Color(0xFF4CAF50)
            val backgroundColor = Color(0xFFFCE4EC)

            // Header Section
            LactancyHeaderSection(
                userName = "Héctor",
                selectedDate = "Lunes 25",
                lightGreen = lightGreen
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
                    items(dayEntry.naps) { nap ->
                        LactancyRecordCard(
                            session = nap,
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
    userName: String,
    selectedDate: String,
    lightGreen: Color
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
        // Top Bar
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Menu indicator
            Row(
                horizontalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                repeat(3) { index ->
                    Box(
                        modifier = Modifier
                            .size(width = 20.dp, height = 3.dp)
                            .background(
                                Color.White.copy(alpha = if (index == 0) 0.8f else 0.4f),
                                RoundedCornerShape(2.dp)
                            )
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(16.dp))
        Spacer(modifier = Modifier.height(16.dp))

        // Week Calendar for Lactancy
        LactancyWeekCalendar()
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
                Icon(
                    imageVector = Icons.Default.ArrowBack,
                    contentDescription = "Previous",
                    tint = Color.White,
                    modifier = Modifier.size(28.dp)
                )

                Column(
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(
                        text = "Fecha seleccionada",
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

                Icon(
                    imageVector = Icons.Default.ArrowForward,
                    contentDescription = "Next",
                    tint = Color.White,
                    modifier = Modifier.size(28.dp)
                )
            }
        }
    }
}

@Composable
fun LactancyWeekCalendar() {
    // TODO: Replace with actual week data from ViewModel
    val weekDays = listOf(
        WeekDay("Martes 19", false, 0.4f),
        WeekDay("Miércoles 20", false, 0.9f),
        WeekDay("Jueves 21", false, 0.7f),
        WeekDay("Viernes 22", false, 0.6f),
        WeekDay("Sábado 23", false, 0.3f),
        WeekDay("Domingo 24", false, 0.8f),
        WeekDay("Lunes 25", true, 0.6f)
    )

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
                    color = if (day.isSelected) Color(0xFF2E7D32) else Color.Gray,
                    fontSize = 12.sp,
                    fontWeight = if (day.isSelected) FontWeight.SemiBold else FontWeight.Normal,
                    modifier = Modifier.width(80.dp)
                )

                Box(
                    modifier = Modifier
                        .weight(1f)
                        .height(8.dp)
                        .background(
                            Color.White.copy(alpha = 0.5f),
                            RoundedCornerShape(4.dp)
                        )
                ) {
                    Box(
                        modifier = Modifier
                            .fillMaxHeight()
                            .fillMaxWidth(day.sleepPercentage)
                            .background(
                                Color(0xFF4CAF50),
                                RoundedCornerShape(4.dp)
                            )
                    )
                }
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
    session: Nap, // Note: You'll need to update this data class to include lactancy_type
    backgroundColor: Color,
    darkGreen: Color
) {
    val startTime = formatHourFraction(session.startHourFraction)
    val endTime = formatHourFraction(session.startHourFraction + (session.durationHours / 60f))
    val durationFormatted = formatLactancyDuration(session.durationHours)

    // Get lactation type info
    val lactationType = getLactationTypeInfo(session.lactancyType ?: "natural")

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
                    text = "Ha comido de",
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
            displayName = "Leche natural",
            emoji = "🤱",
            color = Color(0xFF4CAF50) // Green for natural
        )
        "formula" -> LactationTypeInfo(
            displayName = "Leche de fórmula",
            emoji = "🍼",
            color = Color(0xFF2196F3) // Blue for formula
        )
        else -> LactationTypeInfo(
            displayName = "Leche natural",
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
