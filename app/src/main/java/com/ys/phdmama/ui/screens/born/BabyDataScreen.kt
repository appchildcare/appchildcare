package com.ys.phdmama.ui.screens.born

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
import androidx.compose.material.icons.Icons
import androidx.compose.material3.*
import androidx.compose.material3.Icon
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.SnackbarHostState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.filled.DateRange
import androidx.compose.ui.platform.LocalContext
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import com.ys.phdmama.ui.components.PhdButtons
import com.ys.phdmama.ui.components.PhdDropdown
import com.ys.phdmama.ui.components.PhdLayoutMenu
import com.ys.phdmama.ui.components.PhdMediumText
import com.ys.phdmama.ui.components.PhdNormalText
import com.ys.phdmama.ui.components.PhdTextField
import com.ys.phdmama.ui.components.PhdTitle
import com.ys.phdmama.ui.screens.billing.BillingScreen
import com.ys.phdmama.viewmodel.BabyDataViewModel
import com.ys.phdmama.viewmodel.BabyStatusViewModel
import com.ys.phdmama.viewmodel.LoginViewModel
import java.util.Calendar

@SuppressLint("UnusedMaterial3ScaffoldPaddingParameter")
@Composable
fun BabyDataScreen(
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
            PhdTitle("Perfil del bebé")
            Spacer(modifier = Modifier.height(16.dp))

            PhdTextField("Nombre", name) { name = it }

            PhdMediumText("Fecha Nacimiento")
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

            PhdNormalText(text = formattedDate)

            PhdTextField("APGAR", apgarScore) { apgarScore = it }
            Spacer(modifier = Modifier.width(16.dp))

            PhdTextField("Peso", weight) { weight = it }
            PhdTextField("Talla", height) { height = it }
            Spacer(modifier = Modifier.width(16.dp))

            PhdDropdown("Sexo", sexOptions, selectedSex) { selectedSex = it }
            PhdTextField("Perímetro cefálico", headCircumference) { headCircumference = it }

            Spacer(modifier = Modifier.width(16.dp))

            PhdDropdown("Tipo de sangre", bloodTypeOptions, selectedBloodType) { selectedBloodType = it }

            Spacer(modifier = Modifier.height(32.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                PhdButtons ("Guardar") {
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

                    babyDataViewModel.addBabyToUser(
                        babyData = babyData,
                        onError = { errorMessage ->
                            Log.e("BabySummary", "Failed to save baby data: $errorMessage")
                            babyStatusViewModel.setLoadingRoleUpdate(false)
                            Toast.makeText(context, errorMessage, Toast.LENGTH_SHORT).show()
                        }
                    )
                }
                PhdButtons("Volver") {
                    navController.navigate("bornDashboard")
                }
            }
        }
    }
}


@Composable
fun AddBabyDataScreen(loginViewModel: LoginViewModel = viewModel(), navController: NavController,
                      openDrawer: () -> Unit) {
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

    PhdLayoutMenu(
        title = "Agregar Bebé",
        navController = navController,
        openDrawer = openDrawer
    ) {
        if (showPaymentUI) {
            BillingScreen()
        } else {
            val babyDataViewModel: BabyDataViewModel = viewModel()
            val babyStatusViewModel: BabyStatusViewModel = viewModel()
            BabyDataScreen(
                navController = navController,
                babyDataViewModel = babyDataViewModel,
                babyStatusViewModel = babyStatusViewModel
            )
        }
        Spacer(modifier = Modifier.height(16.dp))
    }
}
