import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.ArrowForward
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavHostController
import com.ys.phdmama.ui.components.PhdLayoutMenu
import com.ys.phdmama.viewmodel.SleepReportViewModel
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Locale

// Data classes
data class Nap(
    val startHourFraction: Float, // e.g. 14.5 means 14:30
    val durationHours: Float,
    val lactancyType: String? = null
)

data class DayNapEntry(
    val dayName: String,
    val naps: List<Nap>
)

data class SleepRecord(
    val id: String = "",
    val time: String = "", // Duration like "00:34"
    val timestamp: String = "", // Firebase timestamp string
    val type: String = "nap" // "nap" or "sleep"
)

data class SleepWeekDay(
    val name: String,
    val isSelected: Boolean = false,
    val sleepPercentage: Float = 0f,
    val napCount: Int = 0
)

@Composable
fun SleepDiaryScreen(
    babyId: String?,
    viewModel: SleepReportViewModel = hiltViewModel(),
//    newViewModel: SleepReportViewModel = hiltViewModel(),
    navController: NavHostController,
    openDrawer: () -> Unit
) {
    val napEntries by viewModel.napEntries.collectAsStateWithLifecycle()
    val weekDays by viewModel.weekDays.collectAsStateWithLifecycle()

    // Get current date in Spanish format
    val currentDate = remember {
        val calendar = Calendar.getInstance()
        val dayName = SimpleDateFormat("EEEE", Locale("es")).format(calendar.time)
            .replaceFirstChar { it.uppercase() }
        val dayNumber = calendar.get(Calendar.DAY_OF_MONTH)
        "$dayName $dayNumber"
    }

    LaunchedEffect(Unit) {
        if (babyId != null) {
            viewModel.fetchNapData(babyId)
        }
    }

    PhdLayoutMenu(
        title = "Reporte de siestas",
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
            val backgroundColor = Color(0xFFF1F8E9)

            // Header Section - Pass weekDays and current date
            SleepHeaderSection(
                selectedDate = currentDate,
                lightGreen = lightGreen,
                weekDays = weekDays
            )

            // Sleep Records
            LazyColumn(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f)
                    .padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                napEntries.forEach { dayEntry ->
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
                        SleepRecordCard(
                            nap = nap,
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
fun SleepHeaderSection(
    selectedDate: String,
    lightGreen: Color,
    weekDays: List<SleepWeekDay> = emptyList()
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

        // Week Calendar
        SleepWeekCalendar(weekDays = weekDays)
        Spacer(modifier = Modifier.height(12.dp))

        // Time Scale
        TimeScale()
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
            }
        }
    }
}

@Composable
fun SleepWeekCalendar(
    weekDays: List<SleepWeekDay> = emptyList()
) {
    Column(
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        weekDays.forEach { day ->
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                // Day name
                Text(
                    text = day.name,
                    color = if (day.isSelected) Color(0xFF2E7D32) else Color.Gray,
                    fontSize = 12.sp,
                    fontWeight = if (day.isSelected) FontWeight.SemiBold else FontWeight.Normal,
                    modifier = Modifier.width(80.dp)
                )

                // Circles representing nap sessions (instead of progress bar)
                Row(
                    modifier = Modifier.weight(1f),
                    horizontalArrangement = Arrangement.Start,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    if (day.napCount > 0) {
                        repeat(day.napCount) {
                            Box(
                                modifier = Modifier
                                    .size(12.dp)
                                    .padding(end = 4.dp)
                                    .background(
                                        color = Color(0xFF4CAF50),
                                        shape = CircleShape
                                    )
                            )
                        }
                    } else {
                        Text(
                            text = "Sin registros",
                            color = Color.Gray,
                            fontSize = 10.sp,
                            fontStyle = FontStyle.Italic
                        )
                    }
                }

                // Count number on the right
                Text(
                    text = day.napCount.toString(),
                    color = if (day.isSelected) Color(0xFF2E7D32) else Color.Gray,
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Medium,
                    modifier = Modifier.width(24.dp),
                    textAlign = TextAlign.End
                )
            }
        }
    }
}
@Composable
fun TimeScale() {
    val times = listOf("8h", "10h", "12h", "14h", "16h", "18h", "20h", "22h", "0h", "2h", "4h", "6h", "8h")

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
fun SleepRecordCard(
    nap: Nap,
    backgroundColor: Color,
    darkGreen: Color
) {
    val startTime = formatHourFraction(nap.startHourFraction)
    val endTime = formatHourFraction(nap.startHourFraction + nap.durationHours)
    val durationFormatted = formatDuration(nap.durationHours)
    val isLongSleep = nap.durationHours > 4f // Consider sleep if longer than 4 hours

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
                        imageVector = if (isLongSleep) Icons.Default.Notifications else Icons.Default.Favorite,
                        contentDescription = if (isLongSleep) "Night sleep" else "Nap",
                        tint = if (isLongSleep) Color(0xFF9C27B0) else Color(0xFFFF9800),
                        modifier = Modifier.size(20.dp)
                    )

                    Text(
                        text = if (isLongSleep) "Noche ⭐" else "Siesta",
                        color = darkGreen,
                        fontSize = 14.sp,
                        fontWeight = FontWeight.SemiBold
                    )

                    Text(
                        text = "• $durationFormatted",
                        color = Color(0xFF66BB6A),
                        fontSize = 12.sp
                    )
                }

                Icon(
                    imageVector = Icons.Default.ArrowForward,
                    contentDescription = "Details",
                    tint = Color(0xFF81C784),
                    modifier = Modifier.size(20.dp)
                )
            }

            Spacer(modifier = Modifier.height(8.dp))

            Column(
                modifier = Modifier.fillMaxWidth(),
                horizontalAlignment = Alignment.End
            ) {
                Text(
                    text = "Ha dormido de",
                    color = Color(0xFF66BB6A),
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
                    text = "⏰ Tiempo despierto • 8h 15min",
                    color = Color.Gray,
                    fontSize = 10.sp
                )
            }
        }
    }
}

fun formatHourFraction(hourFraction: Float): String {
    val hours = hourFraction.toInt()
    val minutes = ((hourFraction - hours) * 60).toInt()
    return String.format("%02d:%02d", hours, minutes)
}

fun formatDuration(durationHours: Float): String {
    val hours = durationHours.toInt()
    val minutes = ((durationHours - hours) * 60).toInt()
    return String.format("%02d:%02d", hours, minutes)
}