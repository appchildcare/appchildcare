package com.ys.phdmama.ui.screens.wizard.prebirth

import PregnancyTrackerViewModel
import android.app.DatePickerDialog
import android.widget.DatePicker
import android.widget.Toast
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.DateRange
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavHostController
import com.ys.phdmama.navigation.NavRoutes
import com.ys.phdmama.viewmodel.BabyStatusViewModel
import com.ys.phdmama.viewmodel.PregnancyTrackingViewModelFactory
import com.ys.phdmama.viewmodel.RoughDateOfBirthViewModel
import com.ys.phdmama.viewmodel.WizardViewModel
import com.ys.phdmama.viewmodel.WizardViewModelFactory
import java.text.SimpleDateFormat
import java.util.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun RoughDateOfBirthScreen(
    navController: NavHostController,
    babyStatusViewModel: BabyStatusViewModel = viewModel(),
    viewModel: RoughDateOfBirthViewModel = viewModel()
) {
    val context = LocalContext.current
    val wizardViewModel: WizardViewModel = viewModel(factory = WizardViewModelFactory())
    val pregnancyTrackerViewModel: PregnancyTrackerViewModel = viewModel(factory = PregnancyTrackingViewModelFactory())
    val calendar = Calendar.getInstance()
    var selectedDate by remember { mutableStateOf(calendar.time) }
    var isLoading by remember { mutableStateOf(false) }
    var locale = Locale("es", "ES")
    var formattedBirthDateWeek = ""

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
    ) {
        Spacer(modifier = Modifier.height(16.dp))
        Text(text = "Calcular fecha de parto", style = MaterialTheme.typography.titleLarge)
        Spacer(modifier = Modifier.height(16.dp))

        Text(
            text = "Ingresa tu fecha aproximada de última menstruación:",
            style = MaterialTheme.typography.labelLarge
        )

        Button(onClick = {
            DatePickerDialog(
                context,
                { _: DatePicker, year: Int, month: Int, dayOfMonth: Int ->
                    calendar.set(year, month, dayOfMonth)
                    selectedDate = calendar.time
                    viewModel.onDateSelected(selectedDate)
                },
                calendar.get(Calendar.YEAR),
                calendar.get(Calendar.MONTH),
                calendar.get(Calendar.DAY_OF_MONTH)
            ).show()
        }) {
            Icon(imageVector = Icons.Default.DateRange, contentDescription = "Abrir calendario")
            Spacer(modifier = Modifier.width(8.dp)) // Ad
            Text(text = "Seleccionar Fecha")
        }

        Spacer(modifier = Modifier.height(16.dp))

        val formattedDate = remember(selectedDate) {
            android.text.format.DateFormat.format("dd MMMM yyyy", selectedDate).toString()
        }

        Text(
            text = "Fecha seleccionada: $formattedDate",
            style = MaterialTheme.typography.bodyMedium
        )

        Spacer(modifier = Modifier.height(16.dp))

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

        Spacer(modifier = Modifier.height(16.dp))

        Text(
            text = "O ingresa semana según eco:",
            style = MaterialTheme.typography.labelLarge
        )

        // ComboBoxWeeks
        val options = List(40) { "${it + 1}" }
        var selectedOption by remember { mutableStateOf(options[0]) }
        var expanded by remember { mutableStateOf(false) }

        ExposedDropdownMenuBox(
            expanded = expanded,
            onExpandedChange = { expanded = !expanded }

        ) {
            TextField(
                value = selectedOption,
                onValueChange = {},
                readOnly = true,
                trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = expanded) },
                modifier = Modifier
                    .menuAnchor()
                    .width(120.dp),
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

        Spacer(modifier = Modifier.height(16.dp))

        val weekBirthDate = viewModel.calculatedWeekDate

        formattedBirthDateWeek = remember(weekBirthDate) {
            try {
                weekBirthDate?.let {
                    formattedBirthDate = ""
                    val myDate = SimpleDateFormat("yyyy-MM-dd",locale).parse(it)
                    myDate?.let { parsedDate ->
                        android.text.format.DateFormat.format("dd MMMM yyyy", parsedDate).toString()
                    } ?: ""
                } ?: ""
            } catch (e: Exception) {
                "Fecha no válida"
            }
        }

        Text(
            text = "Semana seleccionada: $selectedOption",
            style = MaterialTheme.typography.bodyMedium
        )
        // ComboBoxWeeks

        Spacer(modifier = Modifier.height(24.dp))

        val displayText = if(formattedBirthDate.isNotEmpty()) {
            formattedBirthDate
        } else {
            formattedBirthDateWeek
        }

        Text(text = "Fecha aproximada de parto: $displayText", style = MaterialTheme.typography.bodyMedium)


        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp),
            verticalArrangement = Arrangement.SpaceBetween // Pushes content apart
        ) {
            Spacer(modifier = Modifier.weight(1f))

            Button(
                onClick = {
                    isLoading = true
                    babyStatusViewModel.updateUserRole(
                        role = "waiting",
                        onSuccess = {
                            val upcomingBirthDate = if (formattedBirthDate.isNotEmpty()) {formattedBirthDate} else { formattedBirthDateWeek}
                            val convertedUpcomingBirthDate = viewModel.convertToDate(upcomingBirthDate)

                            pregnancyTrackerViewModel.savePregnancyTracker(
                                    convertedUpcomingBirthDate,
                                selectedOption.toInt(),
                                selectedDate)
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
                    .padding(8.dp)
            ) {
                if (isLoading) {
                    CircularProgressIndicator()
                } else {
                    Text(text = "Iniciemos la aventura!")
                }
            }

        }
    }
}
