package com.ys.phdmama.ui.main

import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.res.stringResource
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ExitToApp
import androidx.compose.ui.platform.LocalContext
import com.ys.phdmama.R

@Composable
fun ExitAppCard(onSignOutClick: () -> Unit) {
    val context = LocalContext.current
    var showExitAppDialog by remember { mutableStateOf(false) }

    // Botón para mostrar el diálogo de confirmación
    Button(onClick = { showExitAppDialog = true }) {
        Icon(imageVector = Icons.Filled.ExitToApp, contentDescription = "Sign Out")
        Text(text = stringResource(R.string.sign_out))
    }

    // Diálogo de confirmación para cerrar sesión
    if (showExitAppDialog) {
        AlertDialog(
            title = { Text(stringResource(R.string.sign_out_title)) },
            text = { Text(stringResource(R.string.sign_out_description)) },
            dismissButton = {
                Button(onClick = { showExitAppDialog = false }) {
                    Text(text = stringResource(R.string.cancel))
                }
            },
            confirmButton = {
                Button(onClick = {
                    onSignOutClick()
                    showExitAppDialog = false
//                    wizardViewModel.setWizardFinished(false)
                }) {
                    Text(text = stringResource(R.string.sign_out))
                }
            },
            onDismissRequest = { showExitAppDialog = false }
        )
    }
}
