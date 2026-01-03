package com.ys.phdmama.ui.screens.born

import android.annotation.SuppressLint
import android.text.format.DateFormat
import android.util.Log
import android.widget.DatePicker
import android.widget.Toast
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.material.icons.Icons
import androidx.compose.material3.*
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.material3.SnackbarHostState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.DateRange
import androidx.compose.ui.Alignment
import androidx.compose.ui.platform.LocalContext
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import com.ys.phdmama.ui.components.PhdButtons
import com.ys.phdmama.ui.components.PhdDropdown
import com.ys.phdmama.ui.components.PhdLayoutMenu
import com.ys.phdmama.ui.components.PhdMediumText
import com.ys.phdmama.ui.components.PhdNormalText
import com.ys.phdmama.ui.components.PhdTextField
import com.ys.phdmama.ui.screens.billing.BillingScreen
import com.ys.phdmama.viewmodel.BabyDataViewModel
import com.ys.phdmama.viewmodel.BabyProfile
import com.ys.phdmama.viewmodel.BabyStatusViewModel
import com.ys.phdmama.viewmodel.LoginViewModel
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Locale

@SuppressLint("UnusedMaterial3ScaffoldPaddingParameter")
@Composable
fun BabyDataScreen(
    navController: NavController,
    babyDataViewModel: BabyDataViewModel = hiltViewModel(),
    babyStatusViewModel: BabyStatusViewModel = hiltViewModel(),
    openDrawer: () -> Unit
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

    // New state for baby selection
    val babyList by babyDataViewModel.babyList.collectAsStateWithLifecycle()
    val isLoadingBabies by babyDataViewModel.isLoadingBabies.collectAsStateWithLifecycle()
    var selectedBaby by remember { mutableStateOf<BabyProfile?>(null) }
    var isAddingNewBaby by remember { mutableStateOf(false) }

    val babyProfile by babyDataViewModel.babyData.collectAsStateWithLifecycle()

    // Calculate baby age for display
    val babyAgeInMonths = remember(selectedBaby?.birthDate) {
        selectedBaby?.birthDate?.let { birthDate ->
            babyDataViewModel.calculateBabyAge(birthDate)
        }
    }

    // Fetch babies once when screen loads
    LaunchedEffect(Unit) {
        babyDataViewModel.fetchBabies("")
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

    // Auto-select first baby when list loads (SEPARATE LaunchedEffect)
    LaunchedEffect(babyList) {
        if (selectedBaby == null && babyList.isNotEmpty() && !isAddingNewBaby) {
            selectedBaby = babyList.first()
            Log.d("BabyDataScreen", "Auto-selected first baby: ${babyList.first().name}")
        }
    }

    // Fill form when a baby is selected
    LaunchedEffect(selectedBaby) {
        selectedBaby?.let { baby ->
            name = baby.name
            apgarScore = baby.apgar
            weight = baby.weight
            height = baby.height
            headCircumference = baby.perimeter
            selectedSex = baby.sex
            selectedBloodType = baby.bloodType

            baby.birthDate?.let { dateString ->
                try {
                    val sdf = SimpleDateFormat("dd MMMM yyyy", Locale.getDefault())
                    selectedDate = sdf.parse(dateString) ?: calendar.time
                } catch (e: Exception) {
                    selectedDate = calendar.time
                }
            }
            isAddingNewBaby = false
        }
    }

    // Clear form when adding new baby
    fun clearForm() {
        name = ""
        apgarScore = ""
        weight = ""
        height = ""
        headCircumference = ""
        selectedSex = sexOptions[0]
        selectedBloodType = bloodTypeOptions[0]
        selectedDate = calendar.time
        babyDataViewModel.clearSelectedBaby()
        isAddingNewBaby = true
    }

//    LaunchedEffect(babyProfile) {
//        babyProfile?.let {
//            name = it.name
//            apgarScore = it.apgar
//            weight = it.weight
//            height = it.height
//            headCircumference = it.perimeter
//            selectedSex = it.sex
//            selectedBloodType = it.bloodType
//        }
//    }

    Column(
        modifier = Modifier
            .verticalScroll(rememberScrollState())
            .padding(12.dp)
    ) {
        Spacer(modifier = Modifier.height(16.dp))

        if (isLoadingBabies) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(200.dp),
                contentAlignment = Alignment.Center
            ) {
                CircularProgressIndicator()
            }
        } else {
            if (babyList.isNotEmpty()) {
                Column {
                    // Baby Selector Card
                    BabySelectorCard(
                        babies = babyList,
                        selectedBaby = if (isAddingNewBaby) null else selectedBaby,
                        onBabySelected = { baby ->
                            babyDataViewModel.setSelectedBaby(baby)
                            isAddingNewBaby = false
                        },
                        babyAgeInMonths = babyAgeInMonths
                    )

                    Spacer(modifier = Modifier.height(16.dp))

                    // Add New Baby Button
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.Center
                    ) {
                        Button(
                            onClick = { clearForm() },
                            modifier = Modifier.padding(vertical = 8.dp),
                            colors = ButtonDefaults.buttonColors(
                                containerColor = MaterialTheme.colorScheme.secondary
                            )
                        ) {
                            Icon(
                                imageVector = Icons.Default.Add,
                                contentDescription = "Agregar nuevo bebé",
                                modifier = Modifier.size(18.dp)
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text("Agregar Bebé")
                        }
                    }
                }
                Spacer(modifier = Modifier.height(16.dp))
            } else {
                // Show add button when no babies exist
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.Center
                ) {
                    Button(
                        onClick = { clearForm() },
                        modifier = Modifier.padding(vertical = 16.dp),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = MaterialTheme.colorScheme.primary
                        )
                    ) {
                        Icon(
                            imageVector = Icons.Default.Add,
                            contentDescription = "Agregar Primer bebé",
                            modifier = Modifier.size(18.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("Agregar Primer Bebé")
                    }
                }
                Spacer(modifier = Modifier.height(16.dp))
            }
        }

        // Form title indicating mode
        if (isAddingNewBaby) {
            PhdMediumText("Agregar nuevo bebé")
        } else if (selectedBaby != null) {
            PhdMediumText("Editando: ${selectedBaby?.name}")
        }
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
                    showDatePicker = false
                },
                calendar.get(Calendar.YEAR),
                calendar.get(Calendar.MONTH),
                calendar.get(Calendar.DAY_OF_MONTH)
            ).show()
        }

        val formattedDate = remember(selectedDate) {
            DateFormat.format("dd MMMM yyyy", selectedDate).toString()
        }

        PhdNormalText(text = formattedDate)
        PhdTextField("APGAR", apgarScore) { apgarScore = it }
        Spacer(modifier = Modifier.width(16.dp))
        PhdTextField("Peso (kg)", weight) { weight = it }
        PhdTextField("Talla (cm)", height) { height = it }
        Spacer(modifier = Modifier.width(16.dp))
        PhdDropdown("Sexo", sexOptions, selectedSex) { selectedSex = it }
        PhdTextField("Perímetro cefálico (cm)", headCircumference) { headCircumference = it }
        Spacer(modifier = Modifier.width(16.dp))
        PhdDropdown("Tipo de sangre", bloodTypeOptions, selectedBloodType) {
            selectedBloodType = it
        }

        Spacer(modifier = Modifier.height(32.dp))

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            PhdButtons(if (isAddingNewBaby) "Agregar Bebé" else "Actualizar") {
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

                if (isAddingNewBaby) {
                    babyDataViewModel.addBabyToUser(
                        babyData = babyData,
                        onError = { errorMessage ->
                            Log.e("BabySummary", "Failed to save baby data: $errorMessage")
                            babyStatusViewModel.setLoadingRoleUpdate(false)
                            Toast.makeText(context, errorMessage, Toast.LENGTH_SHORT).show()
                        }
                    )
                } else {
                    selectedBaby?.let { baby ->
                        babyDataViewModel.updateBabyData(
                            babyId = baby.id,
                            babyData = babyData,
                            onError = { errorMessage ->
                                Log.e("BabySummary", "Failed to update baby data: $errorMessage")
                                babyStatusViewModel.setLoadingRoleUpdate(false)
                                Toast.makeText(context, errorMessage, Toast.LENGTH_SHORT).show()
                            }
                        )
                    }
                }
            }
            PhdButtons("Volver") {
                navController.navigate("bornDashboard")
            }
        }
    }
}

@Composable
fun AddBabyDataScreen(
    loginViewModel: LoginViewModel = hiltViewModel(), navController: NavController,
    openDrawer: () -> Unit, babyId: String?
) {
    val userRole by loginViewModel.userRole.collectAsStateWithLifecycle()
    var showPaymentUI by remember { mutableStateOf(true) }

    if (babyId != null) {
        Log.d("NALA", babyId)
    }

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
            val babyDataViewModel: BabyDataViewModel = hiltViewModel()
            val babyStatusViewModel: BabyStatusViewModel = hiltViewModel()
            BabyDataScreen(
                navController = navController,
                babyDataViewModel = babyDataViewModel,
                babyStatusViewModel = babyStatusViewModel,
                openDrawer = openDrawer
            )
        }
        Spacer(modifier = Modifier.height(16.dp))
    }
}
