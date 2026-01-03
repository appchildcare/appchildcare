package com.ys.phdmama.ui.screens.medicine

import android.app.DatePickerDialog
import android.app.TimePickerDialog
import android.os.Build
import android.util.Log
import androidx.annotation.RequiresApi
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
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
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.DateRange
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.RadioButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.graphics.Color
import androidx.navigation.NavHostController
import androidx.compose.ui.platform.LocalContext
import androidx.hilt.navigation.compose.hiltViewModel
import com.ys.phdmama.ui.components.PhdBoldText
import com.ys.phdmama.ui.components.PhdLayoutMenu
import com.ys.phdmama.ui.theme.secondaryCream
import com.ys.phdmama.ui.components.PhdGenericCardList
import com.ys.phdmama.ui.components.PhdLabelText
import com.ys.phdmama.ui.components.PhdNormalText
import com.ys.phdmama.ui.components.PhdSubtitle
import com.ys.phdmama.viewmodel.MedicineRecord
import com.ys.phdmama.viewmodel.MedicineRegistrationViewModel
import java.util.Calendar

@RequiresApi(Build.VERSION_CODES.Q)
@Composable
fun MedicineRegistrationScreen(
    navController: NavHostController,
    openDrawer: () -> Unit,
    viewModel: MedicineRegistrationViewModel = hiltViewModel()
) {
    LaunchedEffect(Unit) {
        viewModel.loadMedicineRecords()
    }

    PhdLayoutMenu(
        title = "Registro de medicinas",
        navController = navController,
        openDrawer = openDrawer
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .background(secondaryCream)
                .padding(16.dp)
        ) {
            Spacer(modifier = Modifier.height(16.dp))

            PhdLabelText("Medicina")
            TextField(
                value = viewModel.medicineName,
                onValueChange = { viewModel.medicineName = it },
                modifier = Modifier.fillMaxWidth(),
            )

            Spacer(modifier = Modifier.height(16.dp))

            PhdLabelText("Hora de toma")

            // Time Picker
            val context = LocalContext.current
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable {
                        val calendar = Calendar.getInstance()
                        TimePickerDialog(
                            context,
                            { _, hour, minute ->
                                viewModel.selectedHour = hour
                                viewModel.selectedMinute = minute
                            },
                            viewModel.selectedHour,
                            viewModel.selectedMinute,
                            true
                        ).show()
                    }
                    .background(Color.White, RoundedCornerShape(4.dp))
                    .border(1.dp, Color.Gray, RoundedCornerShape(4.dp))
                    .padding(16.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = String.format("%02d:%02d", viewModel.selectedHour, viewModel.selectedMinute),
                    modifier = Modifier.weight(1f)
                )
                Icon(
                    imageVector = Icons.Default.DateRange,
                    contentDescription = "Seleccionar hora"
                )
            }

            Spacer(modifier = Modifier.height(16.dp))

            PhdLabelText("¿Desea recibir recordatorio?")
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.Start,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.clickable { viewModel.wantsReminder = true }
                ) {
                    RadioButton(
                        selected = viewModel.wantsReminder == true,
                        onClick = { viewModel.wantsReminder = true }
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("Sí")
                }

                Spacer(modifier = Modifier.width(32.dp))

                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.clickable { viewModel.wantsReminder = false }
                ) {
                    RadioButton(
                        selected = viewModel.wantsReminder == false,
                        onClick = { viewModel.wantsReminder = false }
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("No")
                }
            }

            if (viewModel.wantsReminder == true) {
                Spacer(modifier = Modifier.height(16.dp))

                PhdLabelText("Fecha de recordatorio")

                // Date Picker
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable {
                            val calendar = Calendar.getInstance()
                            DatePickerDialog(
                                context,
                                { _, year, month, day ->
                                    viewModel.reminderYear = year
                                    viewModel.reminderMonth = month
                                    viewModel.reminderDay = day
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
                            "%02d/%02d/%04d",
                            viewModel.reminderDay,
                            viewModel.reminderMonth + 1,
                            viewModel.reminderYear
                        ),
                        modifier = Modifier.weight(1f)
                    )
                    Icon(
                        imageVector = Icons.Default.DateRange,
                        contentDescription = "Seleccionar fecha"
                    )
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            Button(
                onClick = {
                    Log.d("MedicineScreen", "Guardar clicked - Medicine: ${viewModel.medicineName}")
                    viewModel.saveMedicineRecord()
                },
                enabled = viewModel.medicineName.isNotBlank(),
                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFFADA7D)),
                shape = RoundedCornerShape(20.dp),
                modifier = Modifier.align(Alignment.End)
            ) {
                Text("Guardar")
            }

            Spacer(modifier = Modifier.height(32.dp))

            PhdSubtitle("Historial de medicinas")
            Spacer(modifier = Modifier.height(8.dp))

            ListMedicineRecords(
                medicineList = viewModel.medicineList,
                viewModel = viewModel
            )

            Spacer(modifier = Modifier.height(16.dp))

            Button(
                onClick = { viewModel.generatePdfReport(context) },
                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFFADA7D)),
                shape = RoundedCornerShape(20.dp),
                modifier = Modifier.align(Alignment.End)
            ) {
                Text("Reporte")
            }
        }
    }
}

@Composable
fun ListMedicineRecords(
    medicineList: List<MedicineRecord>,
    viewModel: MedicineRegistrationViewModel
) {
    var editingMedicine by remember { mutableStateOf<MedicineRecord?>(null) }
    var editedMedicineName by remember { mutableStateOf("") }
    var editedHour by remember { mutableStateOf(0) }
    var editedMinute by remember { mutableStateOf(0) }

    PhdGenericCardList(
        items = medicineList,
        onEditClick = { medicine ->
            editingMedicine = medicine
            editedMedicineName = medicine.medicineName
            val timeParts = medicine.timeToTake.split(":")
            editedHour = timeParts[0].toIntOrNull() ?: 0
            editedMinute = timeParts[1].toIntOrNull() ?: 0
        }
    ) { medicine ->
        Column {
            PhdBoldText("Medicina:")
            PhdNormalText(medicine.medicineName)
            Spacer(modifier = Modifier.height(8.dp))
            PhdBoldText("Hora de toma:")
            PhdNormalText(medicine.timeToTake)
            Spacer(modifier = Modifier.height(8.dp))
            PhdBoldText("Recordatorio:")
            PhdNormalText(if (medicine.notificationReminder == "Yes") "Sí - ${medicine.reminderDate}" else "No")
        }
    }

    if (editingMedicine != null) {
        val context = LocalContext.current
        AlertDialog(
            onDismissRequest = { editingMedicine = null },
            title = { Text("Editar medicina") },
            text = {
                Column {
                    PhdLabelText("Medicina")
                    TextField(
                        value = editedMedicineName,
                        onValueChange = { editedMedicineName = it },
                        modifier = Modifier.fillMaxWidth()
                    )

                    Spacer(modifier = Modifier.height(8.dp))

                    PhdLabelText("Hora de toma")
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable {
                                TimePickerDialog(
                                    context,
                                    { _, hour, minute ->
                                        editedHour = hour
                                        editedMinute = minute
                                    },
                                    editedHour,
                                    editedMinute,
                                    true
                                ).show()
                            }
                            .background(Color.White, RoundedCornerShape(4.dp))
                            .border(1.dp, Color.Gray, RoundedCornerShape(4.dp))
                            .padding(16.dp)
                    ) {
                        Text(String.format("%02d:%02d", editedHour, editedMinute))
                    }
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        val updated = editingMedicine!!.copy(
                            medicineName = editedMedicineName,
                            timeToTake = String.format("%02d:%02d", editedHour, editedMinute)
                        )
                        viewModel.updateMedicineRecord(updated)
                        editingMedicine = null
                    }
                ) {
                    Text("Guardar")
                }
            },
            dismissButton = {
                Button(onClick = { editingMedicine = null }) {
                    Text("Cancelar")
                }
            }
        )
    }
}
