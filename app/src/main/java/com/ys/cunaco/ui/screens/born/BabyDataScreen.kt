package com.ys.cunaco.ui.screens.born

import android.annotation.SuppressLint
import android.os.Build
import android.text.format.DateFormat
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.annotation.RequiresApi
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.DateRange
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavController
import androidx.annotation.StringRes
import androidx.compose.ui.res.stringResource
import com.ys.cunaco.R
import com.ys.cunaco.ui.components.*
import com.ys.cunaco.ui.screens.billing.BillingScreen
import com.ys.cunaco.viewmodel.BabyDataViewModel
import com.ys.cunaco.viewmodel.BabyStatusViewModel
import com.ys.cunaco.viewmodel.LoginViewModel
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.*

@RequiresApi(Build.VERSION_CODES.O)
@SuppressLint("UnusedMaterial3ScaffoldPaddingParameter")
@Composable
fun BabyDataScreen(
    navController: NavController,
    babyDataViewModel: BabyDataViewModel,
    babyStatusViewModel: BabyStatusViewModel = hiltViewModel(),
    openDrawer: () -> Unit
) {
    // Valores guardados en la base de datos (NO cambian con el idioma)
    val sexValues = listOf("Masculino", "Femenino", "Otro")
    // Etiquetas que ve el usuario
    val sexLabels = listOf(
        stringResource(R.string.sex_male),
        stringResource(R.string.sex_female),
        stringResource(R.string.sex_other)
    )
    val bloodTypeOptions = listOf("A+", "A-", "B+", "B-", "O+", "O-", "AB+", "AB-")

    val context = LocalContext.current
    var name by remember { mutableStateOf("") }
    val calendar = Calendar.getInstance()
    var selectedDate by remember { mutableStateOf(calendar.time) }
    var apgarScore by remember { mutableStateOf("") }
    var weight by remember { mutableStateOf("") }
    var height by remember { mutableStateOf("") }
    var headCircumference by remember { mutableStateOf("") }
    var selectedSex by remember { mutableStateOf(sexValues[0]) }
    var selectedWeeksBirth by remember { mutableStateOf("") }
    var selectedBloodType by remember { mutableStateOf(bloodTypeOptions[0]) }
    val snackbarHostState = remember { SnackbarHostState() }
    var showDatePicker by remember { mutableStateOf(false) }

    val selectedBaby by babyDataViewModel.selectedBaby.collectAsState()
    val babyList by babyDataViewModel.babyList.collectAsState()
    val isLoadingBabies by babyDataViewModel.isLoadingBabies.collectAsState()

    var isAddingNewBaby by remember { mutableStateOf(false) }
    var showSuccessAlert by remember { mutableStateOf(false) }
    @StringRes var successMessageRes by remember { mutableStateOf(R.string.baby_data_saved) }

    val babyAgeInMonths = remember(selectedBaby?.birthDate) {
        selectedBaby?.let { baby ->
            babyDataViewModel.calculateCorrectedAge(baby.birthDate, baby.weeksBirth)
        }
    }

    LaunchedEffect(Unit) {
        babyDataViewModel.fetchBabies()
        launch {
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
    }

    LaunchedEffect(selectedBaby) {
        selectedBaby?.let { baby ->
            if (!isAddingNewBaby) {
                name = baby.name
                apgarScore = baby.apgar
                weight = baby.weight
                height = baby.height
                headCircumference = baby.perimeter
                selectedSex = baby.sex
                selectedBloodType = baby.bloodType
                selectedWeeksBirth = baby.weeksBirth ?: ""

                baby.birthDate?.let { dateString ->
                    try {
                        val sdf = SimpleDateFormat("dd MMMM yyyy", Locale.getDefault())
                        selectedDate = sdf.parse(dateString) ?: Calendar.getInstance().time
                    } catch (e: Exception) {
                        selectedDate = Calendar.getInstance().time
                    }
                }
            }
        }
    }

    Column(
        modifier = Modifier
            .verticalScroll(rememberScrollState())
            .padding(12.dp)
    ) {
        Spacer(modifier = Modifier.height(16.dp))

        if (isLoadingBabies) {
            Box(modifier = Modifier.fillMaxWidth().height(200.dp), contentAlignment = Alignment.Center) {
                CircularProgressIndicator()
            }
        } else {
            if (babyList.isNotEmpty()) {
                BabySelectorCard(
                    babies = babyList,
                    selectedBaby = selectedBaby,
                    onBabySelected = { baby ->
                        babyDataViewModel.setSelectedBaby(baby)
                        isAddingNewBaby = false
                    },
                    babyAgeInMonths = babyAgeInMonths
                )
                Spacer(modifier = Modifier.height(16.dp))
            }
        }

        if (isAddingNewBaby) {
            PhdMediumText(stringResource(R.string.add_new_baby))
        } else if (selectedBaby != null) {
            PhdMediumText(stringResource(R.string.editing_baby, selectedBaby?.name.orEmpty()))
        }

        Spacer(modifier = Modifier.height(16.dp))
        PhdTextField(stringResource(R.string.name), name) { name = it }
        Spacer(modifier = Modifier.height(16.dp))

        PhdTextBold(stringResource(R.string.birth_date))
        Button(onClick = { showDatePicker = true }) {
            Icon(imageVector = Icons.Default.DateRange, contentDescription = null)
            Spacer(modifier = Modifier.width(8.dp))
            Text(text = stringResource(R.string.select_date))
        }

        if (showDatePicker) {
            android.app.DatePickerDialog(
                context,
                { _, year, month, day ->
                    calendar.set(year, month, day)
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
        Spacer(modifier = Modifier.height(16.dp))
        PhdNormalText(text = formattedDate)
        Spacer(modifier = Modifier.height(16.dp))

        WeeksSelectionDropdown(
            selectedWeeks = if (isAddingNewBaby) selectedWeeksBirth else selectedBaby?.weeksBirth,
            onWeeksSelected = { weeks -> selectedWeeksBirth = weeks.toString() },
            label = stringResource(R.string.weeks_born_question)
        )

        PhdTextField(stringResource(R.string.apgar), apgarScore) { apgarScore = it }
        PhdTextField(stringResource(R.string.weight_kg), weight) { weight = it }
        PhdTextField(stringResource(R.string.height_cm), height) { height = it }
        PhdDropdown(
            stringResource(R.string.sex),
            sexLabels,
            sexLabels[sexValues.indexOf(selectedSex).coerceAtLeast(0)]
        ) { picked ->
            selectedSex = sexValues[sexLabels.indexOf(picked).coerceAtLeast(0)]
        }
        PhdTextField(stringResource(R.string.head_circumference_cm), headCircumference) { headCircumference = it }
        PhdDropdown(stringResource(R.string.blood_type), bloodTypeOptions, selectedBloodType) { selectedBloodType = it }

        Spacer(modifier = Modifier.height(32.dp))

        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
            PhdButtons(
                if (isAddingNewBaby || babyList.isEmpty()) stringResource(R.string.add_baby)
                else stringResource(R.string.update)
            ) {
                babyDataViewModel.setBabyAttribute("name", name)
                babyDataViewModel.setBabyAttribute("apgar", apgarScore)
                babyDataViewModel.setBabyAttribute("height", height)
                babyDataViewModel.setBabyAttribute("birthDate", formattedDate)
                babyDataViewModel.setBabyAttribute("weight", weight)
                babyDataViewModel.setBabyAttribute("perimeter", headCircumference)
                babyDataViewModel.setBabyAttribute("bloodType", selectedBloodType)
                babyDataViewModel.setBabyAttribute("sex", selectedSex)
                babyDataViewModel.setBabyAttribute("weeksBirth", selectedWeeksBirth)

                val babyData = mapOf(
                    "name" to (babyDataViewModel.getBabyAttribute("name") ?: ""),
                    "apgar" to (babyDataViewModel.getBabyAttribute("apgar") ?: ""),
                    "height" to (babyDataViewModel.getBabyAttribute("height") ?: ""),
                    "weight" to (babyDataViewModel.getBabyAttribute("weight") ?: ""),
                    "perimeter" to (babyDataViewModel.getBabyAttribute("perimeter") ?: ""),
                    "bloodType" to (babyDataViewModel.getBabyAttribute("bloodType") ?: ""),
                    "birthDate" to (babyDataViewModel.getBabyAttribute("birthDate") ?: ""),
                    "sex" to (babyDataViewModel.getBabyAttribute("sex") ?: ""),
                    "weeksBirth" to (babyDataViewModel.getBabyAttribute("weeksBirth") ?: ""),
                )

                if (isAddingNewBaby || babyList.isEmpty()) {
                    babyDataViewModel.addBabyToUser(
                        babyData = babyData,
                        onSuccess = {
                            successMessageRes = R.string.baby_added_success
                            showSuccessAlert = true
                            isAddingNewBaby = false
                        },
                        onError = { Toast.makeText(context, it, Toast.LENGTH_SHORT).show() }
                    )
                } else {
                    selectedBaby?.id?.let { id ->
                        babyDataViewModel.updateBabyData(
                            babyId = id,
                            babyData = babyData,
                            onSuccess = {
                                successMessageRes = R.string.baby_data_updated_success
                                showSuccessAlert = true
                            },
                            onError = { Toast.makeText(context, it, Toast.LENGTH_SHORT).show() }
                        )
                    }
                }
            }

            AppChildAlert(
                showAlert = showSuccessAlert,
                onDismiss = { showSuccessAlert = false },
                message = stringResource(successMessageRes),
                onConfirm = { showSuccessAlert = false }
            )

            PhdButtons(stringResource(R.string.back)) { navController.navigate("bornDashboard") }
        }

        if (babyList.isNotEmpty() && !isAddingNewBaby) {
            Spacer(modifier = Modifier.height(16.dp))
            Button(
                onClick = {
                    isAddingNewBaby = true
                    name = ""; apgarScore = ""; weight = ""; height = ""
                    headCircumference = ""; selectedWeeksBirth = ""
                },
                modifier = Modifier.fillMaxWidth()
            ) {
                Text(stringResource(R.string.add_another_baby))
            }
        }
    }
}

@RequiresApi(Build.VERSION_CODES.O)
@Composable
fun AddBabyDataScreen(
    loginViewModel: LoginViewModel = hiltViewModel(),
    navController: NavController,
    openDrawer: () -> Unit,
    babyId: String?
) {
    val userRole by loginViewModel.userRole.collectAsStateWithLifecycle()
    var showPaymentUI by remember { mutableStateOf(true) }

    LaunchedEffect(userRole) {
        userRole?.let {
            showPaymentUI = when (it) {
                "born" -> false
                else -> true
            }
        }
    }

    PhdLayoutMenu(
        title = stringResource(R.string.baby_profile_title),
        navController = navController,
        openDrawer = openDrawer
    ) {
        if (showPaymentUI) {
            BillingScreen()
        } else {
            val babyDataViewModel: BabyDataViewModel = hiltViewModel(LocalContext.current as ComponentActivity)
            BabyDataScreen(
                navController = navController,
                babyDataViewModel = babyDataViewModel,
                openDrawer = openDrawer
            )
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun WeeksSelectionDropdown(
    selectedWeeks: String?,
    onWeeksSelected: (Int) -> Unit,
    modifier: Modifier = Modifier,
    label: String = stringResource(R.string.gestation_weeks)
) {
    val options = (20..42).map { it.toString() }
    var selectedOption by remember(selectedWeeks) { mutableStateOf(selectedWeeks ?: options[options.size / 2]) }
    var expanded by remember { mutableStateOf(false) }

    Column(modifier = modifier) {
        if (label.isNotEmpty()) {
            Text(text = label, style = MaterialTheme.typography.bodyMedium, modifier = Modifier.padding(bottom = 4.dp))
        }

        ExposedDropdownMenuBox(expanded = expanded, onExpandedChange = { expanded = !expanded }) {
            OutlinedTextField(
                value = stringResource(R.string.weeks_format, selectedOption),
                onValueChange = {},
                readOnly = true,
                trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = expanded) },
                modifier = Modifier.menuAnchor().background(Color.White).fillMaxWidth(),
                shape = RoundedCornerShape(12.dp),
                colors = OutlinedTextFieldDefaults.colors(
                    unfocusedBorderColor = Color(0xFFB794F6),
                    focusedBorderColor = Color(0xFF9F7AEA)
                )
            )

            ExposedDropdownMenu(expanded = expanded, onDismissRequest = { expanded = false }) {
                options.forEach { option ->
                    DropdownMenuItem(
                        text = { Text(text = stringResource(R.string.weeks_format, option)) },
                        onClick = {
                            selectedOption = option
                            expanded = false
                            onWeeksSelected(option.toInt())
                        }
                    )
                }
            }
        }
    }
}
