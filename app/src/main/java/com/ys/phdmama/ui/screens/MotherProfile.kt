package com.ys.phdmama.ui.screens

import android.annotation.SuppressLint
import android.util.Log
import android.widget.DatePicker
import android.widget.Toast
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
import androidx.compose.material.icons.Icons
import androidx.compose.material3.*
import androidx.compose.material3.Icon
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.material3.SnackbarHostState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.filled.DateRange
import androidx.compose.ui.platform.LocalContext
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import com.ys.phdmama.viewmodel.MotherProfileViewModel
import java.util.Calendar

@SuppressLint("UnusedMaterial3ScaffoldPaddingParameter")
@Composable
fun MotherProfileScreen(
    navController: NavController,
    motherProfileViewModel: MotherProfileViewModel = hiltViewModel(),
    openDrawer: () -> Unit
) {
    val context = LocalContext.current
    var motherName by remember { mutableStateOf("") }
    var motherField1 by remember { mutableStateOf("") }
    val calendar = Calendar.getInstance()
    var selectedDate by remember { mutableStateOf(calendar.time) }
    val snackbarHostState = remember { SnackbarHostState() }
    var showDatePicker by remember { mutableStateOf(false) }

    val motherProfile by motherProfileViewModel.motherData.collectAsStateWithLifecycle()

    // ✅ Handle UiEvents
    LaunchedEffect(Unit) {
        motherProfileViewModel.uiEvent.collect { event ->
            when (event) {
                is MotherProfileViewModel.UiEvent.ShowSnackbar -> {
                    snackbarHostState.showSnackbar(event.message)
                    navController.popBackStack()
                    navController.navigate("bornDashboard")

                }
            }
        }
    }

    LaunchedEffect(motherProfile) {
        motherProfile?.let {
            motherName = it.motherName
//            motherField1 = it.motherField1
        }
    }

    Scaffold(
        snackbarHost = { SnackbarHost(hostState = snackbarHostState) }
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .background(Color(0xFFF2F2F2))
                .verticalScroll(rememberScrollState())
                .padding(24.dp)
        ) {
            Text("Nombre", fontSize = 28.sp, fontWeight = FontWeight.Bold)

            Spacer(modifier = Modifier.height(16.dp))

            MotherProfileField("Nombre", motherName) { motherName = it }

            Text("Fecha Nacimiento", fontSize = 16.sp, fontWeight = FontWeight.Medium)
            Button(onClick = {
                showDatePicker = true
            }) {
                Icon(imageVector = Icons.Default.DateRange, contentDescription = "Abrir calendario")
                Spacer(modifier = Modifier.width(8.dp))
                Text(text = "Seleccionar Fecha")
            }
            if (showDatePicker) {
                android.app.DatePickerDialog(
                    context,
                    { _: DatePicker, year: Int, month: Int, dayOfMonth: Int ->
                        calendar.set(year, month, dayOfMonth)
                        selectedDate = calendar.time
                        motherProfileViewModel.onDateSelected(selectedDate)
                        showDatePicker = false // close the dialog
                    },
                    calendar.get(Calendar.YEAR),
                    calendar.get(Calendar.MONTH),
                    calendar.get(Calendar.DAY_OF_MONTH)
                ).show()
            }

            val formattedDate = remember(selectedDate) {
                android.text.format.DateFormat.format("dd MMMM yyyy", selectedDate).toString()
            }

            Text(text = formattedDate, style = MaterialTheme.typography.bodyMedium)

            MotherProfileField("Field1", motherField1) { motherField1 = it }

            Spacer(modifier = Modifier.height(32.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                ActionButton("Guardar") {
                    motherProfileViewModel.setMotherAttribute("motherName", motherName)
                    motherProfileViewModel.setMotherAttribute("motherField1", motherField1)
                    motherProfileViewModel.setMotherAttribute("motherBirthDate", formattedDate)

                    val motherData = mapOf(
                        "motherName" to (motherProfileViewModel.getMotherAttribute("motherName") ?: ""),
                        "motherBirthDate" to (motherProfileViewModel.getMotherAttribute("motherBirthDate") ?: ""),
                        "motherField1" to (motherProfileViewModel.getMotherAttribute("motherField1") ?: ""),
                    )

                    motherProfileViewModel.addMotherProfile(
                        motherData = motherData,
                        onError = { errorMessage ->
                            Log.e("Mother Summary", "Failed to save mother data: $errorMessage")
//                            motherProfileViewModel.setLoadingRoleUpdate(false)
                            Toast.makeText(context, errorMessage, Toast.LENGTH_SHORT).show()
                        }
                    )
                }

                ActionButton("Volver") {
                    navController.navigate("bornDashboard")
                }
            }
        }
    }
}


@Composable
fun MotherProfileField(label: String, value: String, onValueChange: (String) -> Unit) {
    Column {
        Text(label, fontSize = 16.sp, fontWeight = FontWeight.Medium)
        TextField(
            value = value,
            onValueChange = onValueChange,
            modifier = Modifier
                .fillMaxWidth()
                .background(Color(0xFFADD8E6), shape = RoundedCornerShape(8.dp))
        )
    }
}

@Composable
fun ActionButton(text: String, onClick: () -> Unit) {
    Button(
        onClick = onClick,
        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF6A1B9A))
    ) {
        Text(text)
    }
}
