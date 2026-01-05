package com.ys.phdmama.ui.components

import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable

@Composable
fun AppChildAlert(
    showAlert: Boolean,
    onDismiss: () -> Unit,
    title: String = "Confirmación",
    message: String = "Datos registrados",
    confirmButtonText: String = "Aceptar",
    onConfirm: () -> Unit
) {
    if (showAlert) {
        AlertDialog(
            onDismissRequest = onDismiss,
            title = { Text(text = title) },
            text = { Text(text = message) },
            confirmButton = {
                Button(onClick = onConfirm) {
                    Text(text = confirmButtonText)
                }
            }
        )
    }
}