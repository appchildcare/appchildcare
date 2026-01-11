package com.ys.phdmama.ui.screens.wizard.prebirth

import PregnancyViewModel
import android.app.DatePickerDialog
import android.widget.DatePicker
import android.widget.Toast
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.DateRange
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.paint
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavHostController
import com.ys.phdmama.navigation.NavRoutes
import com.ys.phdmama.viewmodel.BabyStatusViewModel
import com.ys.phdmama.viewmodel.RoughDateOfBirthViewModel
import com.ys.phdmama.viewmodel.WizardViewModel
import java.text.SimpleDateFormat
import java.util.*
import androidx.lifecycle.viewmodel.compose.viewModel
import com.ys.phdmama.R

@JvmOverloads
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun RoughDateOfBirthScreen(
    navController: NavHostController,
    babyStatusViewModel: BabyStatusViewModel = hiltViewModel(),
    viewModel: RoughDateOfBirthViewModel = hiltViewModel()
) {
    val context = LocalContext.current
    val wizardViewModel: WizardViewModel = hiltViewModel()
    val pregnancyViewModel: PregnancyViewModel = viewModel()
    val calendar = Calendar.getInstance()
    var selectedDate by remember { mutableStateOf(calendar.time) }
    var isLoading by remember { mutableStateOf(false) }
    val locale = Locale("es", "ES")
    var formattedBirthDateWeek = ""

    Column(
        modifier = Modifier
            .fillMaxSize()
            .paint(
                painter = painterResource(R.drawable.background_sun),
                contentScale = ContentScale.Crop
            )
            .padding(16.dp)
            .verticalScroll(rememberScrollState())
    ) {
        // Logo
        Image(
            painter = painterResource(id = R.drawable.app_child_care_logo),
            contentDescription = "Logo image",
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 32.dp, vertical = 8.dp)
                .height(190.dp)
        )

        Spacer(modifier = Modifier.height(16.dp))

        // Title
        Text(
            text = "Calcular fecha de parto",
            style = MaterialTheme.typography.headlineMedium.copy(
                fontWeight = FontWeight.Bold,
                color = Color(0xFF2D2D2D)
            ),
            modifier = Modifier.fillMaxWidth(),
            textAlign = TextAlign.Center
        )

        Spacer(modifier = Modifier.height(24.dp))

        // Mascot and input section side by side
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Left side - Text and Date Picker
            Column(
                modifier = Modifier.weight(1f)
            ) {
                Text(
                    text = "Ingresa tu fecha aproximada de última menstruación:",
                    style = MaterialTheme.typography.bodyMedium.copy(
                        color = Color(0xFF5D5D5D)
                    ),
                    modifier = Modifier.padding(bottom = 8.dp)
                )
            }

            // Right side - Mascot
            Image(
                painter = painterResource(id = R.drawable.mascota_thinking),
                contentDescription = "Mascota",
                modifier = Modifier
                    .size(140.dp)
                    .padding(start = 1.dp)
            )
        }

        Spacer(modifier = Modifier.height(24.dp))

        // Date Picker Button with gradient
        Button(
            onClick = {
                val datePicker = DatePickerDialog(
                    context,
                    { _: DatePicker, year: Int, month: Int, dayOfMonth: Int ->
                        calendar.set(year, month, dayOfMonth)
                        selectedDate = calendar.time
                        viewModel.onDateSelected(selectedDate)
                    },
                    calendar.get(Calendar.YEAR),
                    calendar.get(Calendar.MONTH),
                    calendar.get(Calendar.DAY_OF_MONTH)
                )
                datePicker.show()
            },
            modifier = Modifier
                .fillMaxWidth()
                .height(56.dp),
            shape = RoundedCornerShape(28.dp),
            colors = ButtonDefaults.buttonColors(
                containerColor = Color.Transparent
            ),
            contentPadding = PaddingValues(0.dp)
        ) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(
                        brush = Brush.horizontalGradient(
                            colors = listOf(
                                Color(0xFFFF9A3D),
                                Color(0xFFFF4E50)
                            )
                        ),
                        shape = RoundedCornerShape(28.dp)
                    ),
                contentAlignment = Alignment.Center
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.DateRange,
                        contentDescription = "Abrir calendario",
                        tint = Color.White
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "Seleccionar Fecha",
                        color = Color.White,
                        fontWeight = FontWeight.SemiBold
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(12.dp))

        // Selected date display
        val formattedDate = remember(selectedDate) {
            android.text.format.DateFormat.format("dd MMMM yyyy", selectedDate).toString()
        }

        Text(
            text = "Fecha selecciona: $formattedDate",
            style = MaterialTheme.typography.bodyMedium.copy(
                color = Color(0xFF5D5D5D)
            )
        )
        Spacer(modifier = Modifier.height(12.dp))

        // Week selector with border
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 8.dp),
            colors = CardDefaults.cardColors(
                containerColor = Color.White.copy(alpha = 0.8f)
            ),
        ) {
            Column(
                modifier = Modifier.padding(16.dp)
            ) {
                Text(
                    text = "O ingresa semana según eco:",
                    style = MaterialTheme.typography.bodyMedium.copy(
                        color = Color(0xFF5D5D5D)
                    )
                )

                Spacer(modifier = Modifier.height(12.dp))

                // ComboBoxWeeks
                val options = List(40) { "${it + 1}" }
                var selectedOption by remember { mutableStateOf(options[0]) }
                var expanded by remember { mutableStateOf(false) }

                ExposedDropdownMenuBox(
                    expanded = expanded,
                    onExpandedChange = { expanded = !expanded }
                ) {
                    OutlinedTextField(
                        value = selectedOption,
                        onValueChange = {},
                        readOnly = true,
                        trailingIcon = {
                            ExposedDropdownMenuDefaults.TrailingIcon(expanded = expanded)
                        },
                        modifier = Modifier
                            .menuAnchor()
                            .fillMaxWidth(),
                        shape = RoundedCornerShape(12.dp),
                        colors = OutlinedTextFieldDefaults.colors(
                            unfocusedBorderColor = Color(0xFFB794F6),
                            focusedBorderColor = Color(0xFF9F7AEA)
                        )
                    )

                    ExposedDropdownMenu(
                        expanded = expanded,
                        onDismissRequest = { expanded = false }
                    ) {
                        options.forEach { option ->
                            DropdownMenuItem(
                                text = { Text(option) },
                                onClick = {
                                    selectedOption = option
                                    expanded = false
                                    viewModel.onBirthDateByWeeks(selectedOption.toInt())
                                }
                            )
                        }
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Birth date calculation
        val roughBirthDate = viewModel.calculatedDate
        var formattedBirthDate = remember(roughBirthDate) {
            try {
                formattedBirthDateWeek = ""
                roughBirthDate?.let {
                    val myDate = SimpleDateFormat("yyyy-MM-dd", locale).parse(it)
                    android.text.format.DateFormat.format("dd MMMM yyyy", myDate).toString()
                } ?: ""
            } catch (e: Exception) {
                "Fecha no válida"
            }
        }

        val weekBirthDate = viewModel.calculatedWeekDate
        val options = List(40) { "${it + 1}" }
        var selectedOption by remember { mutableStateOf(options[0]) }


        formattedBirthDateWeek = remember(weekBirthDate) {
            try {
                weekBirthDate?.let {
                    formattedBirthDate = ""
                    val myDate = SimpleDateFormat("yyyy-MM-dd", locale).parse(it)
                    myDate?.let { parsedDate ->
                        android.text.format.DateFormat.format("dd MMMM yyyy", parsedDate).toString()
                    } ?: ""
                } ?: ""
            } catch (e: Exception) {
                "Fecha no válida"
            }
        }

        val displayText = if (formattedBirthDate.isNotEmpty()) {
            formattedBirthDate
        } else {
            formattedBirthDateWeek
        }

        // Results display
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 8.dp)
        ) {
            Text(
                text = "Fecha aprimada de parto:",
                style = MaterialTheme.typography.bodyLarge.copy(
                    fontWeight = FontWeight.SemiBold,
                    color = Color(0xFF2D2D2D)
                )
            )
            Text(
                text = "Semana seleccionando: ${selectedOption}",
                style = MaterialTheme.typography.bodyMedium.copy(
                    color = Color(0xFF5D5D5D)
                )
            )
            if (displayText.isNotEmpty()) {
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = displayText,
                    style = MaterialTheme.typography.bodyMedium.copy(
                        color = Color(0xFF5D5D5D)
                    )
                )
            }
        }

        Spacer(modifier = Modifier.height(24.dp))

        // Bottom button
        Button(
            onClick = {
                isLoading = true
                babyStatusViewModel.updateUserRole(
                    role = "waiting",
                    onSuccess = {
                        val upcomingBirthDate = if (formattedBirthDate.isNotEmpty()) {
                            formattedBirthDate
                        } else {
                            formattedBirthDateWeek
                        }
                        val convertedUpcomingBirthDate = viewModel.convertToDate(upcomingBirthDate)

                        if (convertedUpcomingBirthDate != null) { // TODO: Manejar el caso cuando es null con calculateBirthDateFromWeek
                            pregnancyViewModel.savePregnancyTracker(
                                convertedUpcomingBirthDate,
                                selectedOption.toInt(),
                                selectedDate
                            )
                        }
                        isLoading = false
                        navController.navigate(NavRoutes.WAITING_DASHBOARD) {
                            popUpTo(0) { inclusive = true }
                        }
                        wizardViewModel.setWizardFinished(true)
                    },
                    onError = { errorMessage ->
                        isLoading = false
                        Toast.makeText(context, errorMessage, Toast.LENGTH_SHORT).show()
                    }
                )
            },
            enabled = !isLoading,
            modifier = Modifier
                .fillMaxWidth()
                .height(56.dp),
            shape = RoundedCornerShape(28.dp),
            colors = ButtonDefaults.buttonColors(
                containerColor = Color(0xFF4CAF50)
            )
        ) {
            if (isLoading) {
                CircularProgressIndicator(color = Color.White, modifier = Modifier.size(24.dp))
            } else {
                Text(
                    text = "Iniciemos la aventura!",
                    color = Color.White,
                    fontWeight = FontWeight.Bold,
                    fontSize = 16.sp
                )
            }
        }

        Spacer(modifier = Modifier.height(24.dp))
    }
}
