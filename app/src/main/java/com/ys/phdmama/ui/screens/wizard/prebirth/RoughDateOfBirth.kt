package com.ys.phdmama.ui.screens.wizard.prebirth

import android.app.DatePickerDialog
import android.widget.DatePicker
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavHostController
import com.ys.phdmama.viewmodel.RoughDateOfBirthViewModel
import java.util.*

@Composable
fun RoughDateOfBirthScreen(navController: NavHostController,
                   viewModel: RoughDateOfBirthViewModel = viewModel()) {
    
    val context = LocalContext.current
    val calendar = Calendar.getInstance()
    var selectedDate by remember { mutableStateOf(calendar.time) }

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

        Text(text = "Fecha seleccionada: ${selectedDate.toString()}", style = MaterialTheme.typography.bodyMedium)

        Spacer(modifier = Modifier.height(16.dp))

        Text(text = "Fecha aproximada de parto: ${viewModel.calculatedDate}", style = MaterialTheme.typography.bodyMedium)
    }
}