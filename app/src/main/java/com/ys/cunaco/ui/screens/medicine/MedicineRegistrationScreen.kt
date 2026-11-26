package com.ys.cunaco.ui.screens.medicine

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
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.RadioButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.compose.ui.res.stringResource
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.graphics.Color
import androidx.navigation.NavHostController
import androidx.compose.ui.platform.LocalContext
import androidx.hilt.navigation.compose.hiltViewModel
import com.ys.cunaco.R
import com.ys.cunaco.model.MedicineRecord
import com.ys.cunaco.ui.components.PhdBoldText
import com.ys.cunaco.ui.components.PhdLayoutMenu
import com.ys.cunaco.ui.components.PhdGenericCardList
import com.ys.cunaco.ui.components.PhdLabelText
import com.ys.cunaco.ui.components.PhdNormalText
import com.ys.cunaco.ui.components.PhdSubtitle
import com.ys.cunaco.viewmodel.MedicineRegistrationViewModel

@RequiresApi(Build.VERSION_CODES.Q)
@Composable
fun MedicineRegistrationScreen(
    navController: NavHostController,
    openDrawer: () -> Unit,
    viewModel: MedicineRegistrationViewModel = hiltViewModel()
) {
    val selectedBaby by viewModel.selectedBaby.collectAsState()
    val medicineList by viewModel.medicineList.collectAsState()

    LaunchedEffect(selectedBaby) {
        if (selectedBaby != null) {
            viewModel.loadMedicineRecords()
        }
    }

    PhdLayoutMenu(
        title = stringResource(R.string.medicine_registration_title),
        navController = navController,
        openDrawer = openDrawer
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(16.dp)
        ) {
            Spacer(modifier = Modifier.height(16.dp))

            PhdLabelText(stringResource(R.string.medicine_name))

            OutlinedTextField(
                value = viewModel.medicineName,
                onValueChange = { viewModel.medicineName = it },
                modifier = Modifier.fillMaxWidth()
            )

            Spacer(modifier = Modifier.height(16.dp))

            PhdLabelText(stringResource(R.string.medicine_take_time))

            val context = LocalContext.current

            // Time Picker
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable {
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
                    text = String.format(
                        "%02d:%02d",
                        viewModel.selectedHour,
                        viewModel.selectedMinute
                    ),
                    modifier = Modifier.weight(1f)
                )

                Icon(
                    imageVector = Icons.Default.DateRange,
                    contentDescription = stringResource(R.string.medicine_select_time)
                )
            }

            Spacer(modifier = Modifier.height(16.dp))

            PhdLabelText(stringResource(R.string.medicine_reminder_question))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.Start,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.clickable {
                        viewModel.wantsReminder = true
                    }
                ) {
                    RadioButton(
                        selected = viewModel.wantsReminder == true,
                        onClick = { viewModel.wantsReminder = true }
                    )

                    Spacer(modifier = Modifier.width(4.dp))

                    Text(stringResource(R.string.medicine_yes))
                }

                Spacer(modifier = Modifier.width(32.dp))

                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.clickable {
                        viewModel.wantsReminder = false
                    }
                ) {
                    RadioButton(
                        selected = viewModel.wantsReminder == false,
                        onClick = { viewModel.wantsReminder = false }
                    )

                    Spacer(modifier = Modifier.width(4.dp))

                    Text(stringResource(R.string.medicine_no))
                }
            }

            if (viewModel.wantsReminder == true) {
                Spacer(modifier = Modifier.height(16.dp))

                PhdLabelText(stringResource(R.string.medicine_reminder_date))

                // Date Picker
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable {
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
                        contentDescription = stringResource(R.string.medicine_select_date)
                    )
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            Button(
                onClick = {
                    Log.d(
                        "MedicineScreen",
                        "Guardar clicked - Medicine: ${viewModel.medicineName}"
                    )
                    viewModel.saveMedicineRecord()
                },
                enabled = viewModel.medicineName.isNotBlank(),
                colors = ButtonDefaults.buttonColors(
                    containerColor = Color(0xFFFADA7D)
                ),
                shape = RoundedCornerShape(20.dp),
                modifier = Modifier.align(Alignment.End)
            ) {
                Text(stringResource(R.string.medicine_save))
            }

            Spacer(modifier = Modifier.height(32.dp))

            if (medicineList.isNotEmpty()) {
                PhdSubtitle(stringResource(R.string.medicine_history))

                Spacer(modifier = Modifier.height(8.dp))

                ListMedicineRecords(
                    medicineList = medicineList,
                    viewModel = viewModel
                )

                Spacer(modifier = Modifier.height(16.dp))

                Button(
                    onClick = {
                        viewModel.generatePdfReport(context)
                    },
                    colors = ButtonDefaults.buttonColors(
                        containerColor = Color(0xFFFADA7D)
                    ),
                    shape = RoundedCornerShape(20.dp),
                    modifier = Modifier.align(Alignment.End)
                ) {
                    Text(stringResource(R.string.medicine_report))
                }
            } else {
                PhdNormalText(stringResource(R.string.medicine_no_records))
            }
        }
    }
}
@Composable
fun ListMedicineRecords(
    medicineList: List<MedicineRecord>,
    viewModel: MedicineRegistrationViewModel
) {
    var editingMedicine by remember {
        mutableStateOf<MedicineRecord?>(null)
    }

    var editedMedicineName by remember {
        mutableStateOf("")
    }

    var editedHour by remember {
        mutableStateOf(0)
    }

    var editedMinute by remember {
        mutableStateOf(0)
    }

    PhdGenericCardList(
        items = medicineList,
        onEditClick = { medicine ->
            editingMedicine = medicine
            editedMedicineName = medicine.medicineName

            val timeParts = medicine.timeToTake.split(":")

            editedHour = timeParts.getOrNull(0)?.toIntOrNull() ?: 0
            editedMinute = timeParts.getOrNull(1)?.toIntOrNull() ?: 0
        }
    ) { medicine ->
        Column {
            PhdBoldText(stringResource(R.string.medicine_label))
            PhdNormalText(medicine.medicineName)

            Spacer(modifier = Modifier.height(8.dp))

            PhdBoldText(stringResource(R.string.medicine_time_label))
            PhdNormalText(medicine.timeToTake)

            Spacer(modifier = Modifier.height(8.dp))

            PhdBoldText(stringResource(R.string.medicine_reminder_label))

            PhdNormalText(
                if (medicine.notificationReminder == "Yes") {
                    stringResource(
                        R.string.medicine_reminder_yes_date,
                        medicine.reminderDate
                    )
                } else {
                    stringResource(R.string.medicine_no)
                }
            )
        }
    }

    if (editingMedicine != null) {
        val context = LocalContext.current

        AlertDialog(
            onDismissRequest = {
                editingMedicine = null
            },
            title = {
                Text(stringResource(R.string.medicine_edit_title))
            },
            text = {
                Column {
                    PhdLabelText(stringResource(R.string.medicine_name))

                    TextField(
                        value = editedMedicineName,
                        onValueChange = { editedMedicineName = it },
                        modifier = Modifier.fillMaxWidth()
                    )

                    Spacer(modifier = Modifier.height(8.dp))

                    PhdLabelText(stringResource(R.string.medicine_take_time))

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
                        Text(
                            String.format(
                                "%02d:%02d",
                                editedHour,
                                editedMinute
                            )
                        )
                    }
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        val updated = editingMedicine!!.copy(
                            medicineName = editedMedicineName,
                            timeToTake = String.format(
                                "%02d:%02d",
                                editedHour,
                                editedMinute
                            )
                        )

                        viewModel.updateMedicineRecord(updated)
                        editingMedicine = null
                    }
                ) {
                    Text(stringResource(R.string.medicine_save))
                }
            },
            dismissButton = {
                Button(
                    onClick = {
                        editingMedicine = null
                    }
                ) {
                    Text(stringResource(R.string.medicine_cancel))
                }
            }
        )
    }
}
