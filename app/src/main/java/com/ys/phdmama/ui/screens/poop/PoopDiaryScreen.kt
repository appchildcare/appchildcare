package com.ys.phdmama.ui.screens.poop

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.ArrowForward
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.ui.Alignment
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavHostController
import com.ys.phdmama.viewmodel.PoopDiaryViewModel
import com.ys.phdmama.ui.components.PhdLayoutMenu
import java.util.Calendar

data class PoopRecord(
    val id: String = "",
    val timestamp: Long = System.currentTimeMillis(),
    val time: String = "",
    val color: String = "",
    val texture: String = "",
    val size: String = "",
    val notes: String = "",
    val createdAt: Long = System.currentTimeMillis()
)

data class DayPoopEntry(
    val dayName: String,
    val poops: List<PoopRecord>
)

data class WeekDay(
    val name: String,
    val isSelected: Boolean = false,
    val poopCount: Int = 0
)

@Composable
fun PoopDiaryScreen(
    babyId: String?,
    viewModel: PoopDiaryViewModel = hiltViewModel(),
    navController: NavHostController,
    openDrawer: () -> Unit
) {
    val poopEntries by viewModel.poopEntries.collectAsStateWithLifecycle()

    LaunchedEffect(Unit) {
        if (babyId != null) {
            viewModel.fetchPoopData(babyId)
        }
    }

    PhdLayoutMenu(
        title = "Reporte de deposiciones",
        navController = navController,
        openDrawer = openDrawer
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(20.dp)
        ) {

            val lightBrown = Color(0xFFD7CCC8)
            val darkBrown = Color(0xFF8D6E63)
            val backgroundColor = Color(0xFFFFF8E1)

            // Header Section
            PoopHeaderSection(
                userName = "Héctor",
                selectedDate = "Lunes 25",
                lightBrown = lightBrown
            )

            // Poop Records
            LazyColumn(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f)
                    .padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                poopEntries.forEach { dayEntry ->
                    item {
                        Text(
                            text = dayEntry.dayName,
                            fontSize = 16.sp,
                            fontWeight = FontWeight.Bold,
                            color = darkBrown,
                            modifier = Modifier.padding(bottom = 8.dp)
                        )
                    }

                    items(dayEntry.poops) { poopRecord ->
                        PoopRecordCard(
                            poopRecord = poopRecord,
                            backgroundColor = backgroundColor,
                            darkBrown = darkBrown
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun PoopHeaderSection(
    userName: String,
    selectedDate: String,
    lightBrown: Color
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .background(
                lightBrown,
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

        // Week Calendar
        PoopWeekCalendar()
        Spacer(modifier = Modifier.height(12.dp))

        // Time Scale
        PoopTimeScale()
        Spacer(modifier = Modifier.height(16.dp))

        // Selected Date Navigation
        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(12.dp),
            colors = CardDefaults.cardColors(containerColor = lightBrown)
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
fun PoopWeekCalendar() {
    // TODO: Replace with actual week data from ViewModel
    val weekDays = listOf(
        WeekDay("Martes 19", false, 2),
        WeekDay("Miércoles 20", false, 3),
        WeekDay("Jueves 21", false, 1),
        WeekDay("Viernes 22", false, 4),
        WeekDay("Sábado 23", false, 2),
        WeekDay("Domingo 24", false, 1),
        WeekDay("Lunes 25", true, 3)
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
                    repeat(day.poopCount) { index ->
                        Box(
                            modifier = Modifier
                                .size(12.dp)
                                .background(
                                    Color(0xFF8D6E63),
                                    RoundedCornerShape(6.dp)
                                )
                        )
                    }

                    if (day.poopCount == 0) {
                        Text(
                            text = "Sin registros",
                            color = Color.Gray,
                            fontSize = 10.sp,
                            fontStyle = FontStyle.Italic
                        )
                    }
                }

                Text(
                    text = "${day.poopCount}",
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
fun PoopTimeScale() {
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
fun PoopRecordCard(
    poopRecord: PoopRecord,
    backgroundColor: Color,
    darkBrown: Color
) {
    val timeFormatted = poopRecord.time.ifEmpty {
        formatTimestamp(poopRecord.timestamp)
    }

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
                    Icon(
                        imageVector = Icons.Default.Favorite,
                        contentDescription = "Poop",
                        tint = getColorForPoopColor(poopRecord.color),
                        modifier = Modifier.size(20.dp)
                    )

                    Text(
                        text = "Deposición",
                        color = darkBrown,
                        fontSize = 14.sp,
                        fontWeight = FontWeight.SemiBold
                    )

                    Text(
                        text = "• $timeFormatted",
                        color = Color(0xFF8D6E63),
                        fontSize = 12.sp
                    )
                }

                Icon(
                    imageVector = Icons.Default.ArrowForward,
                    contentDescription = "Details",
                    tint = Color(0xFF8D6E63),
                    modifier = Modifier.size(20.dp)
                )
            }

            Spacer(modifier = Modifier.height(12.dp))

            // Poop details
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Column {
                    if (poopRecord.color.isNotEmpty()) {
                        PoopDetailItem(
                            label = "Color:",
                            value = poopRecord.color,
                            darkBrown = darkBrown
                        )
                    }

                    if (poopRecord.texture.isNotEmpty()) {
                        PoopDetailItem(
                            label = "Textura:",
                            value = poopRecord.texture,
                            darkBrown = darkBrown
                        )
                    }
                }

                Column {
                    if (poopRecord.size.isNotEmpty()) {
                        PoopDetailItem(
                            label = "Tamaño:",
                            value = poopRecord.size,
                            darkBrown = darkBrown
                        )
                    }
                }
            }

            if (poopRecord.notes.isNotEmpty()) {
                Spacer(modifier = Modifier.height(8.dp))

                Row(
                    verticalAlignment = Alignment.Top,
                    horizontalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    Text(
                        text = "💭",
                        fontSize = 12.sp
                    )
                    Text(
                        text = poopRecord.notes,
                        color = Color.Gray,
                        fontSize = 12.sp,
                        fontStyle = FontStyle.Italic,
                        modifier = Modifier.weight(1f)
                    )
                }
            }
        }
    }
}

@Composable
fun PoopDetailItem(
    label: String,
    value: String,
    darkBrown: Color
) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(4.dp)
    ) {
        Text(
            text = label,
            color = Color.Gray,
            fontSize = 11.sp
        )
        Text(
            text = value,
            color = darkBrown,
            fontSize = 12.sp,
            fontWeight = FontWeight.Medium
        )
    }
}

fun getColorForPoopColor(color: String): Color {
    return when (color.lowercase()) {
        "amarillo", "yellow" -> Color(0xFFFFC107)
        "marrón", "brown", "café" -> Color(0xFF8D6E63)
        "verde", "green" -> Color(0xFF4CAF50)
        "negro", "black" -> Color(0xFF424242)
        "rojo", "red" -> Color(0xFFF44336)
        "naranja", "orange" -> Color(0xFFFF9800)
        else -> Color(0xFF8D6E63) // Default brown
    }
}

fun formatTimestamp(timestamp: Long): String { //TODO: Move to utils
    val calendar = Calendar.getInstance()
    calendar.timeInMillis = timestamp
    val hour = calendar.get(Calendar.HOUR_OF_DAY)
    val minute = calendar.get(Calendar.MINUTE)
    return String.format("%02d:%02d", hour, minute)
}