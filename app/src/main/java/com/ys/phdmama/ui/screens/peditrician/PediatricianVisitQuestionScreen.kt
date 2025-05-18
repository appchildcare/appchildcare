package com.ys.phdmama.ui.screens.peditrician

import android.app.DatePickerDialog
import android.app.TimePickerDialog
import androidx.compose.foundation.background
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
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.DateRange
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavHostController
import com.ys.phdmama.model.PediatricianVisit
import com.ys.phdmama.ui.components.PhdBoldText
import com.ys.phdmama.ui.components.PhdLayoutMenu
import com.ys.phdmama.ui.components.PhdSubtitle
import com.ys.phdmama.viewmodel.PediatricVisitViewModel
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Locale

@Composable
fun PediatricVisitScreen(navController: NavHostController,
                         openDrawer: () -> Unit,
                         viewModel: PediatricVisitViewModel = viewModel()) {
    val context = LocalContext.current
    val calendar = remember { Calendar.getInstance() }

    var dateTime by remember { mutableStateOf("") }
    var notes by remember { mutableStateOf("") }
    var weight by remember { mutableStateOf("") }
    var height by remember { mutableStateOf("") }
    var headCircumference by remember { mutableStateOf("") }

    var errorMessage by remember { mutableStateOf("") }
    var successMessage by remember { mutableStateOf("") }

    val showDatePicker = remember { mutableStateOf(false) }
    val showTimePicker = remember { mutableStateOf(false) }

    LaunchedEffect(Unit) {
        viewModel.loadPediatricianVisits()
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
                .background(Color(0xFFF1F1F1))
                .padding(16.dp)
        ) {
            PhdSubtitle("Registro de Visitas al pediatra")
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
                modifier = Modifier.fillMaxWidth().height(150.dp),
                maxLines = 5,
                singleLine = false
            )

            OutlinedTextField(
                value = weight,
                onValueChange = { weight = it },
                label = { Text("Peso (kg)") },
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                modifier = Modifier.fillMaxWidth()
            )

            OutlinedTextField(
                value = height,
                onValueChange = { height = it },
                label = { Text("Talla (cm)") },
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                modifier = Modifier.fillMaxWidth()
            )

            OutlinedTextField(
                value = headCircumference,
                onValueChange = { headCircumference = it },
                label = { Text("Perímetro cefálico (cm)") },
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                modifier = Modifier.fillMaxWidth()
            )

            Button(
                onClick = {
                    if (dateTime.isBlank() || notes.isBlank() || weight.isBlank() || height.isBlank() || headCircumference.isBlank()) {
                        errorMessage = "Todos los campos son obligatorios."
                        successMessage = ""
                    } else {
                        viewModel.saveVisit(
                            dateTime, notes, weight, height, headCircumference,
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
                Text(errorMessage, color = Color.Red)
            }
            if (successMessage.isNotEmpty()) {
                Text(successMessage, color = Color.Green)
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

    Spacer(modifier = Modifier.height(32.dp))
    Text("Registro de visitas al pediatra", fontSize = 20.sp, fontWeight = FontWeight.Bold)
    Spacer(modifier = Modifier.height(8.dp))

    // Replacing LazyColumn with Column
    questionList.forEach { question ->
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 4.dp),
            elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
            colors = CardDefaults.cardColors(containerColor = Color(0xFFDDE1F5))
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Column {
                        PhdBoldText("Fecha Visita:")
                        Text(question.date)
                    }
                    IconButton(onClick = {
                        editingPediatricianVisit = question
                        editedNotes = question.notes
                        editWeight = question.weight
                    }) {
                        Icon(imageVector = Icons.Default.Edit, contentDescription = "Editar")
                    }
                }
                Spacer(modifier = Modifier.height(8.dp))
                PhdBoldText("Notas:")
                Text(question.notes)
                PhdBoldText("Peso (kg):")
                Text(question.weight)
                PhdBoldText("Talla (cm):")
                Text(question.height)
                PhdBoldText("Perímetro cefálico (cm):")
                Text(question.headCircumference)
            }
        }
    }

    // Dialog code remains the same
    if (editingPediatricianVisit != null) {
        AlertDialog(
            onDismissRequest = { editingPediatricianVisit = null },
            title = { Text("Editar visita") },
            text = {
                Column {
                    OutlinedTextField(
                        value = editedNotes,
                        onValueChange = { editedNotes = it },
                        label = { Text("Fecha Visita") },
                        modifier = Modifier.fillMaxWidth()
                    )
                    OutlinedTextField(
                        value = editWeight,
                        onValueChange = { editWeight = it },
                        label = { Text("Notas") },
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            },
            confirmButton = {
                TextButton(onClick = {
                    // Handle save
                    editingPediatricianVisit = null
                }) {
                    Text("Guardar")
                }
            },
            dismissButton = {
                TextButton(onClick = { editingPediatricianVisit = null }) {
                    Text("Cancelar")
                }
            }
        )
    }
}

