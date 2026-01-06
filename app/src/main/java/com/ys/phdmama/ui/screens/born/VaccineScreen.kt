package com.ys.phdmama.ui.screens.born

import android.annotation.SuppressLint
import android.app.DatePickerDialog
import android.text.format.DateFormat
import android.util.Log
import android.widget.DatePicker
import android.widget.Toast
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.DateRange
import androidx.compose.material3.Button
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController
import com.ys.phdmama.model.Vaccine
import com.ys.phdmama.ui.components.EditableField
import com.ys.phdmama.ui.components.PhdBoldText
import com.ys.phdmama.ui.components.PhdButtons
import com.ys.phdmama.ui.components.PhdEditItemDialog
import com.ys.phdmama.ui.components.PhdGenericCardList
import com.ys.phdmama.ui.components.PhdLayoutMenu
import com.ys.phdmama.ui.components.PhdSubtitle
import com.ys.phdmama.ui.components.PhdTextField
import com.ys.phdmama.viewmodel.BabyDataViewModel
import java.util.Calendar


@SuppressLint("UnusedMaterial3ScaffoldPaddingParameter")
@Composable
fun VaccineScreen(navController: NavController,
                  babyDataViewModel: BabyDataViewModel = hiltViewModel(),
                  openDrawer: () -> Unit) {
    val context = LocalContext.current
    var vaccineName by remember { mutableStateOf("") }
    val calendar = Calendar.getInstance()
    var selectedDate by remember { mutableStateOf(calendar.time) }
    val snackbarHostState = remember { SnackbarHostState() }
    var showDatePicker by remember { mutableStateOf(false) }


    LaunchedEffect(Unit) {
        babyDataViewModel.loadVaccines()
        babyDataViewModel.uiEvent.collect { event ->
            when (event) {
                is BabyDataViewModel.UiEvent.ShowSnackbar -> {
                    snackbarHostState.showSnackbar(event.message)
                    navController.popBackStack()
                    navController.navigate("bornDashboard")

                }
            }
        }
    }

    val isFormValid by remember(vaccineName, selectedDate) {
        derivedStateOf {
            vaccineName.isNotBlank() && selectedDate != null
        }
    }

    PhdLayoutMenu(
        title = "Agregar Vacunas",
        navController = navController,
        snackbarHostState = snackbarHostState,
        openDrawer = openDrawer
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(24.dp)
        ) {
            PhdSubtitle("Registro de Vacunas")
            Spacer(modifier = Modifier.height(16.dp))
            
            PhdTextField("Nombre", vaccineName) { vaccineName = it }
            Text("Fecha de vacuna", fontSize = 16.sp, fontWeight = FontWeight.Medium)
            
            Button(onClick = {
                showDatePicker = true
            }) {
                Icon(imageVector = Icons.Default.DateRange, contentDescription = "Abrir calendario")
                Spacer(modifier = Modifier.width(8.dp))
                Text(text = "Seleccionar Fecha")
            }
            if (showDatePicker) {
                DatePickerDialog(
                    context,
                    { _: DatePicker, year: Int, month: Int, dayOfMonth: Int ->
                        calendar.set(year, month, dayOfMonth)
                        selectedDate = calendar.time
                        babyDataViewModel.onDateSelected(selectedDate)
                        showDatePicker = false // close the dialog
                    },
                    calendar.get(Calendar.YEAR),
                    calendar.get(Calendar.MONTH),
                    calendar.get(Calendar.DAY_OF_MONTH)
                ).show()
            }

            val formattedDate = remember(selectedDate) {
                DateFormat.format("dd MMMM yyyy", selectedDate).toString()
            }

            Text(text = formattedDate, style = MaterialTheme.typography.bodyMedium)

            Spacer(modifier = Modifier.height(32.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                PhdButtons(
                    "Guardar", enabled = isFormValid,
                ) {
                    babyDataViewModel.vaccineText = vaccineName
                    babyDataViewModel.vaccineDate = formattedDate
                    babyDataViewModel.addVaccines(
                        onError = { errorMessage ->
                            Log.e("Vaccine Summary", "Failed to save vaccine data: $errorMessage")
                            Toast.makeText(context, errorMessage, Toast.LENGTH_SHORT).show()
                        }
                    )
                }

                PhdButtons("Volver") {
                    navController.navigate("bornDashboard")
                }
            }

            ListVaccines(vaccineList = babyDataViewModel.vaccineList, viewModel = babyDataViewModel)
        }
    }
}

@Composable
fun ListVaccines(vaccineList: List<Vaccine>, viewModel: BabyDataViewModel) {
    var editingVaccine by remember { mutableStateOf<Vaccine?>(null) }
    var editedVaccineName by remember { mutableStateOf("") }
    var editedVaccineDate by remember { mutableStateOf("") }

    Spacer(modifier = Modifier.height(32.dp))
    Text("Lista de vacunas", fontSize = 20.sp, fontWeight = FontWeight.Bold)
    Spacer(modifier = Modifier.height(8.dp))

    PhdGenericCardList(
        items = vaccineList,
        onEditClick = { editedVaccine ->
            editingVaccine = editedVaccine
            editedVaccineName = editedVaccine.vaccineName
            editedVaccineDate = editedVaccine.vaccineDate
        }
    ) { vaccine ->
        Column {
            PhdBoldText("Vacuna:")
            Text(vaccine.vaccineName)
            Spacer(modifier = Modifier.height(8.dp))
            PhdBoldText("Fecha Vacuna:")
            Text(vaccine.vaccineDate)
        }
    }

    if (editingVaccine != null) {
        PhdEditItemDialog(
            title = "Editar vacuna",
            fields = listOf(
                EditableField("Nombre", editedVaccineName) { editedVaccineName = it },
                EditableField("Fecha", editedVaccineDate) { editedVaccineDate = it },
            ),
            onDismiss = { editingVaccine = null },
            onSave = {
                // Save logic
                val updated = editingVaccine!!.copy(
                    vaccineName = editedVaccineName,
                    vaccineDate = editedVaccineDate
                )
                viewModel.updateVaccine(updated)
                editingVaccine = null
            }
        )
    }
}
