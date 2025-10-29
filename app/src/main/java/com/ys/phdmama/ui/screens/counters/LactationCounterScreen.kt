package com.ys.phdmama.ui.screens.counters

import android.os.Build
import androidx.annotation.RequiresApi
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import com.ys.phdmama.R
import com.ys.phdmama.navigation.NavRoutes
import com.ys.phdmama.ui.components.PhdLayoutMenu
import com.ys.phdmama.viewmodel.LactationViewModel

@RequiresApi(Build.VERSION_CODES.O)
@Composable
fun LactationCounterScreen(babyId: String?, navController: NavController,
                           viewModel: LactationViewModel = androidx.lifecycle.viewmodel.compose.viewModel(),
                           openDrawer: () -> Unit) {
    PhdLayoutMenu(
        title = "Contador de Lactancia",
        navController = navController,
        openDrawer = openDrawer
    ) { innerPadding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
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
    val lactationTypes = listOf("Leche natural", "Leche de fórmula")

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Image(
            painter = painterResource(id = R.mipmap.lactancia_bebe),
            contentDescription = "lactancia image",
            modifier = Modifier
                .fillMaxWidth()
                .height(180.dp)
        )

        Spacer(modifier = Modifier.height(32.dp))

        // Lactation Type Dropdown
        ExposedDropdownMenuBox(
            expanded = expanded,
            onExpandedChange = { expanded = !expanded },
            modifier = Modifier.fillMaxWidth(0.8f)
        ) {
            OutlinedTextField(
                value = selectedLactationType,
                onValueChange = { },
                readOnly = true,
                label = { Text("Tipo de Lactancia") },
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
                lactationTypes.forEach { type ->
                    DropdownMenuItem(
                        text = { Text(type) },
                        onClick = {
                            viewModel.setLactationType(type)
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
            text = if (isRunning) "Corriendo..." else "Detenido",
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
                colors = ButtonDefaults.buttonColors(
                    containerColor = MaterialTheme.colorScheme.primary,
                    disabledContainerColor = MaterialTheme.colorScheme.surfaceVariant
                )
            ) {
                Text("Iniciar")
            }

            Button(
                onClick = { viewModel.stopCounter(babyId) },
                enabled = isRunning,
                colors = ButtonDefaults.buttonColors(
                    containerColor = MaterialTheme.colorScheme.error,
                    disabledContainerColor = MaterialTheme.colorScheme.surfaceVariant
                )
            ) {
                Text("Detener")
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        Button(
            onClick = { navController.navigate(NavRoutes.BORN_LACTATION_COUNTER_REPORTS) },
            colors = ButtonDefaults.buttonColors(
                containerColor = MaterialTheme.colorScheme.primary,
                disabledContainerColor = MaterialTheme.colorScheme.surfaceVariant
            )
        ) {
            Text("Ver Reportes")
        }

//        // Add notification check button
//        val context = androidx.compose.ui.platform.LocalContext.current
//        Button(
//            onClick = {
//                val activity = context as Activity
//                com.ys.phdmama.utils.NotificationPermissionHelper.checkAndRequestNotificationPermission(activity)
//            },
//            colors = ButtonDefaults.buttonColors(
//                containerColor = MaterialTheme.colorScheme.tertiary
//            )
//        ) {
//            Text("Verificar Notificaciones")
//        }
    }
}

private fun formatTime(seconds: Int): String { // TODO: Move to utils
    val hours = seconds / 3600
    val minutes = (seconds % 3600) / 60
    val remainingSeconds = seconds % 60

    return when {
        hours > 0 -> String.format("%02d:%02d:%02d", hours, minutes, remainingSeconds)
        minutes > 0 -> String.format("%02d:%02d", minutes, remainingSeconds)
        else -> String.format("00:%02d", seconds)
    }
}
