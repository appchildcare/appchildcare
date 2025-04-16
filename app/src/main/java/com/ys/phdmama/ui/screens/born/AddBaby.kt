@file:OptIn(ExperimentalMaterial3Api::class)

package com.ys.phdmama.ui.screens.born

import android.annotation.SuppressLint
import android.util.Log
import android.widget.DatePicker
import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
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
import androidx.compose.material.icons.filled.ArrowDropDown
import androidx.compose.material.icons.filled.Menu
import androidx.compose.material3.*
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.SnackbarHostState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.filled.DateRange
import androidx.compose.ui.platform.LocalContext
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import com.ys.phdmama.ui.components.BottomNavigationBar
import com.ys.phdmama.ui.screens.billing.BillingScreen
import com.ys.phdmama.viewmodel.BabyDataViewModel
import com.ys.phdmama.viewmodel.BabyStatusViewModel
import com.ys.phdmama.viewmodel.LoginViewModel
import java.util.Calendar

@SuppressLint("UnusedMaterial3ScaffoldPaddingParameter")
@Composable
fun BabyProfileScreen(
    navController: NavController,
    babyDataViewModel: BabyDataViewModel = viewModel(),
    babyStatusViewModel: BabyStatusViewModel = viewModel()
) {
    val sexOptions = listOf("Masculino", "Femenino", "Otro")
    val bloodTypeOptions = listOf("A+", "A-", "B+", "B-", "O+", "O-", "AB+", "AB-")

    val context = LocalContext.current
    var name by remember { mutableStateOf("") }
    val calendar = Calendar.getInstance()
    var selectedDate by remember { mutableStateOf(calendar.time) }
    var apgarScore by remember { mutableStateOf("") }
    var weight by remember { mutableStateOf("") }
    var height by remember { mutableStateOf("") }
    var headCircumference by remember { mutableStateOf("") }
    var selectedSex by remember { mutableStateOf(sexOptions[0]) }
    var selectedBloodType by remember { mutableStateOf(bloodTypeOptions[0]) }
    val snackbarHostState = remember { SnackbarHostState() }
    var showDatePicker by remember { mutableStateOf(false) }

    val babyProfile by babyDataViewModel.babyData.collectAsStateWithLifecycle()

    // ✅ Handle UiEvents
    LaunchedEffect(Unit) {
        babyStatusViewModel.uiEvent.collect { event ->
            when (event) {
                is BabyStatusViewModel.UiEvent.ShowSnackbar -> {
                    snackbarHostState.showSnackbar(event.message)
                    navController.popBackStack()
                    navController.navigate("bornDashboard")

                }
            }
        }
    }

    LaunchedEffect(babyProfile) {
        babyProfile?.let {
            name = it.name
            apgarScore = it.apgar
            weight = it.weight
            height = it.height
            headCircumference = it.perimeter
            selectedSex = it.sex
            selectedBloodType = it.bloodType
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
            Text("Perfil del bebé", fontSize = 28.sp, fontWeight = FontWeight.Bold)

            Spacer(modifier = Modifier.height(16.dp))

            BabyProfileField("Nombre", name) { name = it }

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
                        babyDataViewModel.onDateSelected(selectedDate)
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

            BabyProfileField("APGAR", apgarScore) { apgarScore = it }
            Spacer(modifier = Modifier.width(16.dp))
            BabyProfileField("Peso", weight) { weight = it }
            BabyProfileField("Talla", height) { height = it }
            Spacer(modifier = Modifier.width(16.dp))
            DropdownField("Sexo", sexOptions, selectedSex) { selectedSex = it }
            BabyProfileField("Perímetro cefálico", headCircumference) { headCircumference = it }
            Spacer(modifier = Modifier.width(16.dp))
            DropdownField("Tipo de sangre", bloodTypeOptions, selectedBloodType) { selectedBloodType = it }

            Spacer(modifier = Modifier.height(32.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                ActionButton("Guardar") {
                    babyDataViewModel.setBabyAttribute("name", name)
                    babyDataViewModel.setBabyAttribute("apgar", apgarScore)
                    babyDataViewModel.setBabyAttribute("height", height)
                    babyDataViewModel.setBabyAttribute("birthDate", formattedDate)
                    babyDataViewModel.setBabyAttribute("weight", weight)
                    babyDataViewModel.setBabyAttribute("perimeter", headCircumference)
                    babyDataViewModel.setBabyAttribute("bloodType", selectedBloodType)
                    babyDataViewModel.setBabyAttribute("sex", selectedSex)

                    val babyData = mapOf(
                        "name" to (babyDataViewModel.getBabyAttribute("name") ?: ""),
                        "apgar" to (babyDataViewModel.getBabyAttribute("apgar") ?: ""),
                        "height" to (babyDataViewModel.getBabyAttribute("height") ?: ""),
                        "weight" to (babyDataViewModel.getBabyAttribute("weight") ?: ""),
                        "perimeter" to (babyDataViewModel.getBabyAttribute("perimeter") ?: ""),
                        "bloodType" to (babyDataViewModel.getBabyAttribute("bloodType") ?: ""),
                        "birthDate" to (babyDataViewModel.getBabyAttribute("birthDate") ?: ""),
                        "sex" to (babyDataViewModel.getBabyAttribute("sex") ?: "")
                    )

                    babyStatusViewModel.addBabyToUser(
                        babyData = babyData,
                        onError = { errorMessage ->
                            Log.e("BabySummary", "Failed to save baby data: $errorMessage")
                            babyStatusViewModel.setLoadingRoleUpdate(false)
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


@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun Addbaby(loginViewModel: LoginViewModel = viewModel(), navController: NavController, openDrawer: () -> Unit) {
    val userRole by loginViewModel.userRole.collectAsStateWithLifecycle()
    var showPaymentUI by remember { mutableStateOf(true) }

    LaunchedEffect(userRole) {
        userRole?.let {
            showPaymentUI = when (userRole) {
                "born" -> false
                "waiting" -> true
                else -> true
            }
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Add Baby") },
                navigationIcon = {
                    IconButton(onClick = openDrawer) {
                        Icon(Icons.Default.Menu, contentDescription = "Menu")
                    }
                }
            )
        },
        bottomBar = { BottomNavigationBar(navController) }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {

            if(showPaymentUI) {
                BillingScreen()
            } else {
                val babyDataViewModel: BabyDataViewModel = viewModel()
                val babyStatusViewModel: BabyStatusViewModel = viewModel()
                BabyProfileScreen(
                    navController = navController,
                    babyDataViewModel = babyDataViewModel,
                    babyStatusViewModel = babyStatusViewModel
                )
            }

            Spacer(modifier = Modifier.height(16.dp))

        }
    }
}

@Composable
fun BabyProfileField(label: String, value: String, onValueChange: (String) -> Unit) {
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
fun DropdownField(label: String, options: List<String>, selectedOption: String, onOptionSelected: (String) -> Unit) {
    Column {
        Text(label, fontSize = 16.sp, fontWeight = FontWeight.Medium)
        var expanded by remember { mutableStateOf(false) }

        Box {
            TextField(
                value = selectedOption,
                onValueChange = {},
                modifier = Modifier
                    .fillMaxWidth()
                    .background(Color(0xFFADD8E6), shape = RoundedCornerShape(8.dp)),
                readOnly = true,
                trailingIcon = {
                    Icon(Icons.Default.ArrowDropDown, contentDescription = null, modifier = Modifier.clickable { expanded = true })
                }
            )
            DropdownMenu(expanded = expanded, onDismissRequest = { expanded = false }) {
                options.forEach { option ->
                    DropdownMenuItem(
                        text = { Text(option) }, // Pass text here instead of using content lambda
                        onClick = {
                            onOptionSelected(option)
                            expanded = false
                        }
                    )

                }
            }
        }
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
