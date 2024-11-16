package com.ys.phdmama.ui.screens.wizard.prebirth

import android.app.DatePickerDialog
import android.widget.DatePicker
import android.widget.Toast
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavHostController
import com.ys.phdmama.navigation.NavRoutes
import com.ys.phdmama.viewmodel.BabyStatusViewModel
import com.ys.phdmama.viewmodel.RoughDateOfBirthViewModel
import com.ys.phdmama.viewmodel.WizardViewModel
import com.ys.phdmama.viewmodel.WizardViewModelFactory
import java.text.SimpleDateFormat
import java.util.*

@Composable
fun RoughDateOfBirthScreen(
    navController: NavHostController,
    babyStatusViewModel: BabyStatusViewModel = viewModel(),
    viewModel: RoughDateOfBirthViewModel = viewModel()
) {
    val context = LocalContext.current
    val wizardViewModel: WizardViewModel = viewModel(factory = WizardViewModelFactory())
    val calendar = Calendar.getInstance()
    var selectedDate by remember { mutableStateOf(calendar.time) }
    var isLoading by remember { mutableStateOf(false) }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
    ) {
        Text(text = "Ingresa tu fecha aproximada de última menstruación", style = MaterialTheme.typography.titleLarge)

        Spacer(modifier = Modifier.height(16.dp))

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
            Text(text = "Seleccionar Fecha")
        }

        Spacer(modifier = Modifier.height(16.dp))

        val formattedDate = remember(selectedDate) {
            android.text.format.DateFormat.format("EEEE, dd MMMM yyyy", selectedDate).toString()
        }

        Text(
            text = "Fecha seleccionada: $formattedDate",
            style = MaterialTheme.typography.bodyMedium
        )
        Spacer(modifier = Modifier.height(16.dp))

        val roughtBirthDate = viewModel.calculatedDate
        val formattedBirthDate = remember(roughtBirthDate) {
            try {
                roughtBirthDate?.let {
                    val myDate = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).parse(it)
                    android.text.format.DateFormat.format("EEEE, dd MMMM yyyy", myDate).toString()
                } ?: ""
            } catch (e: Exception) {
                "Fecha no válida"
            }
        }

        Text(text = "Fecha aproximada de parto: $formattedBirthDate", style = MaterialTheme.typography.bodyMedium)

        Button(
            onClick = {
                isLoading = true
                babyStatusViewModel.updateUserRole(
                    role = "waiting",
                    onSuccess = {
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
