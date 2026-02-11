package com.ys.phdmama.ui.screens.pediatrician

import android.app.DatePickerDialog
import android.app.TimePickerDialog
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.DateRange
import androidx.compose.ui.platform.LocalContext
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavHostController
import com.ys.phdmama.model.PediatricianVisit
import com.ys.phdmama.ui.components.EditableField
import com.ys.phdmama.ui.components.PhdGenericCardList
import com.ys.phdmama.ui.components.PhdBoldText
import com.ys.phdmama.ui.components.PhdEditItemDialog
import com.ys.phdmama.ui.components.PhdErrorText
import com.ys.phdmama.ui.components.PhdLabelText
import com.ys.phdmama.ui.components.PhdLayoutMenu
import com.ys.phdmama.ui.components.PhdNormalText
import com.ys.phdmama.ui.components.PhdSubtitle
import com.ys.phdmama.viewmodel.PediatricVisitViewModel
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Locale

@Composable
fun PediatricVisitScreen(navController: NavHostController,
                         openDrawer: () -> Unit,
                         viewModel: PediatricVisitViewModel = hiltViewModel()) {
    val context = LocalContext.current
    val calendar = remember { Calendar.getInstance() }
    val selectedBaby by viewModel.selectedBaby.collectAsState()

    var dateTime by remember { mutableStateOf("") }
    var notes by remember { mutableStateOf("") }
    var weight by remember { mutableStateOf("") }
    var height by remember { mutableStateOf("") }
    var headCircumference by remember { mutableStateOf("") }

    var errorMessage by remember { mutableStateOf("") }
    var successMessage by remember { mutableStateOf("") }

    val showDatePicker = remember { mutableStateOf(false) }
    val showTimePicker = remember { mutableStateOf(false) }

    LaunchedEffect(selectedBaby) {
        if (selectedBaby != null) {
            viewModel.loadPediatricianVisits()
        }
    }


    if (showDatePicker.value) {
        DatePickerDialog(
            context,
            { _, year, month, dayOfMonth ->
                calendar.set(Calendar.YEAR, year)
                calendar.set(Calendar.MONTH, month)
                calendar.set(Calendar.DAY_OF_MONTH, dayOfMonth)
                showDatePicker.value = false
                showTimePicker.value = true
            },
            calendar.get(Calendar.YEAR),
            calendar.get(Calendar.MONTH),
            calendar.get(Calendar.DAY_OF_MONTH)
        ).show()
    }

    if (showTimePicker.value) {
        TimePickerDialog(
            context,
            { _, hour, minute ->
                calendar.set(Calendar.HOUR_OF_DAY, hour)
                calendar.set(Calendar.MINUTE, minute)
                val formatter = SimpleDateFormat("yyyy-MM-dd HH:mm", Locale.getDefault())
                dateTime = formatter.format(calendar.time)
                showTimePicker.value = false
            },
            calendar.get(Calendar.HOUR_OF_DAY),
            calendar.get(Calendar.MINUTE),
            true
        ).show()
    }

    PhdLayoutMenu(
        title = "Visitas al pediatra",
        navController = navController,
        openDrawer = openDrawer
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(16.dp)
        ) {
            PhdLabelText("Fecha visita")
            Button(onClick = {
                showDatePicker.value = true
            }) {
                Icon(imageVector = Icons.Default.DateRange, contentDescription = "Abrir calendario")
                Spacer(modifier = Modifier.width(8.dp))
                Text(if (dateTime.isEmpty()) "Seleccionar fecha y hora" else dateTime)
            }

            OutlinedTextField(
                value = notes,
                onValueChange = { notes = it },
                label = { Text("Notas") },
                modifier = Modifier.fillMaxWidth().height(150.dp).background(Color.White),
                maxLines = 5,
                singleLine = false
            )

            OutlinedTextField(
                value = weight,
                onValueChange = { weight = it },
                label = { Text("Peso (kg)") },
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                modifier = Modifier.fillMaxWidth().background(Color.White)
            )

            OutlinedTextField(
                value = height,
                onValueChange = { height = it },
                label = { Text("Talla (cm)") },
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                modifier = Modifier.fillMaxWidth().background(Color.White)
            )

            OutlinedTextField(
                value = headCircumference,
                onValueChange = { headCircumference = it },
                label = { Text("Perímetro cefálico (cm)") },
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                modifier = Modifier.fillMaxWidth().background(Color.White)
            )

            Spacer(modifier = Modifier.height(8.dp))

            PhdLabelText("Fecha de próxima visita")
            // Date Picker
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable {
                        // Primero mostrar el DatePicker
                        DatePickerDialog(
                            context,
                            { _, year, month, day ->
                                viewModel.reminderYear = year
                                viewModel.reminderMonth = month
                                viewModel.reminderDay = day

                                // Después de seleccionar la fecha, mostrar el TimePicker
                                TimePickerDialog(
                                    context,
                                    { _, hour, minute ->
                                        viewModel.reminderHour = hour
                                        viewModel.reminderMinute = minute
                                    },
                                    viewModel.reminderHour,
                                    viewModel.reminderMinute,
                                    true // true para formato 24h, false para AM/PM
                                ).show()
                            },
                            viewModel.reminderYear,
                            viewModel.reminderMonth,
                            viewModel.reminderDay
                        ).show()
                    }
                    .background(Color.White, RoundedCornerShape(4.dp))
                    .border(1.dp, Color.Gray, RoundedCornerShape(4.dp))
                    .padding(16.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = String.format(
                        "%02d/%02d/%04d %02d:%02d",
                        viewModel.reminderDay,
                        viewModel.reminderMonth + 1,
                        viewModel.reminderYear,
                        viewModel.reminderHour,
                        viewModel.reminderMinute
                    ),
                    modifier = Modifier.weight(1f)
                )
                Icon(
                    imageVector = Icons.Default.DateRange,
                    contentDescription = "Seleccionar fecha y hora"
                )
            }

            Spacer(modifier = Modifier.height(8.dp))

            Button(
                onClick = {
                    val nextVisit = String.format(
                        "%04d-%02d-%02d %02d:%02d:00",
                        viewModel.reminderYear,
                        viewModel.reminderMonth + 1,
                        viewModel.reminderDay,
                        viewModel.reminderHour,
                        viewModel.reminderMinute
                    )
                    if (dateTime.isBlank() || notes.isBlank() || weight.isBlank() || height.isBlank() || headCircumference.isBlank()) {
                        errorMessage = "Todos los campos son obligatorios."
                        successMessage = ""
                    } else {
                        viewModel.saveVisit(
                            dateTime, notes, weight, height, headCircumference, nextVisit,
                            onSuccess = {
                                successMessage = "Visita guardada con éxito"
                                errorMessage = ""
                            },
                            onError = {
                                errorMessage = "Error: $it"
                                successMessage = ""
                            }
                        )
                    }
                },
                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFEFD377)),
                shape = RoundedCornerShape(24.dp),
                modifier = Modifier
                    .width(200.dp)
                    .height(60.dp)
            ) {
                Text("Guardar", fontWeight = FontWeight.Medium)
            }

            if (errorMessage.isNotEmpty()) {
                PhdErrorText(errorMessage)
            }
            if (successMessage.isNotEmpty()) {
                PhdBoldText(successMessage)
            }

            ListPediatricianVisits(questionList = viewModel.visitDataList, viewModel = viewModel)
        }

    }
}

@Composable
fun ListPediatricianVisits(questionList: List<PediatricianVisit>, viewModel: PediatricVisitViewModel) {
    var editingPediatricianVisit by remember { mutableStateOf<PediatricianVisit?>(null) }
    var editedNotes by remember { mutableStateOf("") }
    var editWeight by remember { mutableStateOf("") }
    var editHeight by remember { mutableStateOf("") }
    var editHeadCircumference by remember { mutableStateOf("") }
    var editNextVisit by remember { mutableStateOf("") }

    Spacer(modifier = Modifier.height(32.dp))
    PhdSubtitle("Registro de visitas al pediatra")
    Spacer(modifier = Modifier.height(8.dp))

    PhdGenericCardList(
        items = questionList,
        onEditClick = { visit ->
            editingPediatricianVisit = visit
            editedNotes = visit.notes
            editWeight = visit.weight
        }
    ) { visit ->
        Column {
            PhdBoldText("Fecha Visita:")
            PhdNormalText(visit.date)
            Spacer(modifier = Modifier.height(8.dp))
            PhdBoldText("Notas:")
            PhdNormalText(visit.notes)
            PhdBoldText("Peso (kg):")
            PhdNormalText(visit.weight)
            PhdBoldText("Talla (cm):")
            PhdNormalText(visit.height)
            PhdBoldText("Perímetro cefálico (cm):")
            PhdNormalText(visit.headCircumference)
            if(visit.nextVisit?.isNotEmpty() == true) {
                PhdBoldText("Próxima visita:")
                PhdNormalText(visit.nextVisit)
            }

        }
    }

    if (editingPediatricianVisit != null) {
        PhdEditItemDialog(
            title = "Editar visita",
            fields = listOf(
                EditableField("Notas", editedNotes) { editedNotes = it },
                EditableField("Peso (kg)", editWeight) { editWeight = it },
                EditableField("Talla (cm)", editHeight) { editHeight = it },
                EditableField("Perímetro cefálico (cm)", editHeadCircumference) { editHeadCircumference = it },
//                EditableField("Próxima visita", editNextVisit) { editNextVisit = it }
            ),
            onDismiss = { editingPediatricianVisit = null },
            onSave = {
                // Save logic
                val updated = editingPediatricianVisit!!.copy(
                    notes = editedNotes,
                    weight = editWeight,
                    height = editHeight,
//                    headCircumference = editHeadCircumference,
                )
                viewModel.update(updated)
                editingPediatricianVisit = null
            }
        )
    }
}

