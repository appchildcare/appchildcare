package com.ys.cunaco.ui.screens.counters

import android.os.Build
import androidx.annotation.RequiresApi
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.res.stringResource
import java.util.Locale
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController
import com.ys.cunaco.R
import com.ys.cunaco.navigation.NavRoutes
import com.ys.cunaco.ui.components.PhdLayoutMenu
import com.ys.cunaco.ui.theme.primaryYellow
import com.ys.cunaco.ui.theme.secondaryAqua
import com.ys.cunaco.viewmodel.LactationViewModel

@RequiresApi(Build.VERSION_CODES.O)
@Composable
fun LactationCounterScreen(babyId: String?, navController: NavController,
                           viewModel: LactationViewModel = hiltViewModel(),
                           openDrawer: () -> Unit) {
    PhdLayoutMenu(
        title = stringResource(R.string.lactation_counter_title),
        navController = navController,
        openDrawer = openDrawer
    ) {
        Box(
            modifier = Modifier
                .fillMaxSize()
        ) {
            LactationComponent(babyId, navController, viewModel)
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@RequiresApi(Build.VERSION_CODES.O)
@Composable
fun LactationComponent(babyId: String?, navController: NavController, viewModel: LactationViewModel = androidx.lifecycle.viewmodel.compose.viewModel()) {
    val counter by viewModel.counter.collectAsState()
    val isRunning by viewModel.isRunning.collectAsState()
    val selectedLactationType by viewModel.selectedLactationType.collectAsState()

    var expanded by remember { mutableStateOf(false) }

    // Valores guardados (NO cambian con el idioma)
    val lactationValues = listOf("Leche materna", "Leche de fórmula")
    // Etiquetas que ve el usuario
    val lactationLabels = listOf(
        stringResource(R.string.lactation_type_breast_milk),
        stringResource(R.string.lactation_type_formula)
    )
    val selectedLabel = lactationLabels.getOrElse(lactationValues.indexOf(selectedLactationType)) {
        selectedLactationType
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Image(
            painter = painterResource(id = R.drawable.icono_app_gota),
            contentDescription = stringResource(R.string.lactation_counter_image_description),
            modifier = Modifier
                .fillMaxWidth()
                .height(280.dp)
        )

        Spacer(modifier = Modifier.height(32.dp))

        // Lactation Type Dropdown
        ExposedDropdownMenuBox(
            expanded = expanded,
            onExpandedChange = { expanded = !expanded },
            modifier = Modifier.fillMaxWidth(0.8f)
        ) {
            OutlinedTextField(
                value = selectedLabel,
                onValueChange = { },
                readOnly = true,
                label = { Text(stringResource(R.string.lactation_type_label)) },
                trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = expanded) },
                colors = ExposedDropdownMenuDefaults.outlinedTextFieldColors(),
                modifier = Modifier
                    .menuAnchor()
                    .fillMaxWidth()
            )

            ExposedDropdownMenu(
                expanded = expanded,
                onDismissRequest = { expanded = false }
            ) {
                lactationValues.forEachIndexed { index, value ->
                    DropdownMenuItem(
                        text = { Text(lactationLabels[index]) },
                        onClick = {
                            viewModel.setLactationType(value)
                            expanded = false
                        }
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(24.dp))

        // Display counter with better formatting
        Text(
            text = formatTime(counter),
            style = MaterialTheme.typography.headlineLarge.copy(
                fontSize = 48.sp,
                fontWeight = FontWeight.Bold
            ),
            color = if (isRunning) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurface
        )

        Text(
            text = stringResource(
                if (isRunning) R.string.lactation_counter_running else R.string.lactation_counter_stopped
            ),
            style = MaterialTheme.typography.bodyLarge,
            color = if (isRunning) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant
        )

        Spacer(modifier = Modifier.height(32.dp))

        Row(
            horizontalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Button(
                onClick = { viewModel.startCounter() },
                enabled = !isRunning,
                modifier = Modifier
                    .weight(1f)
                    .height(56.dp),
                shape = RoundedCornerShape(28.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = primaryYellow
                ),
                elevation = ButtonDefaults.buttonElevation(
                    defaultElevation = 6.dp,
                    pressedElevation = 2.dp
                )
            ) {
                Text(stringResource(R.string.lactation_counter_start))
            }

            Button(
                onClick = { viewModel.stopCounter(babyId) },
                enabled = isRunning,
                modifier = Modifier
                    .weight(1f)
                    .height(56.dp),
                shape = RoundedCornerShape(28.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = secondaryAqua
                ),
                elevation = ButtonDefaults.buttonElevation(
                    defaultElevation = 6.dp,
                    pressedElevation = 2.dp
                )
            ) {
                Text(stringResource(R.string.lactation_counter_stop))
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        Button(
            onClick = { navController.navigate(NavRoutes.BORN_LACTATION_COUNTER_REPORTS) },
            colors = ButtonDefaults.buttonColors(
                containerColor = primaryYellow,
                disabledContainerColor = MaterialTheme.colorScheme.surfaceVariant
            ),
            modifier = Modifier
                .padding(horizontal = 16.dp)
                .height(44.dp),
        ) {
            Text(stringResource(R.string.lactation_counter_view_reports), fontSize = 14.sp)
        }
    }
}

private fun formatTime(seconds: Int): String { // TODO: Move to utils
    val hours = seconds / 3600
    val minutes = (seconds % 3600) / 60
    val remainingSeconds = seconds % 60

    return when {
        hours > 0 -> String.format(Locale.US, "%02d:%02d:%02d", hours, minutes, remainingSeconds)
        minutes > 0 -> String.format(Locale.US, "%02d:%02d", minutes, remainingSeconds)
        else -> String.format(Locale.US, "00:%02d", seconds)
    }
}