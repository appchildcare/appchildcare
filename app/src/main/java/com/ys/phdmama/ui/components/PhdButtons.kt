package com.ys.phdmama.ui.components

import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import com.ys.phdmama.ui.theme.primaryTeal

@Composable
fun PhdButtons(text: String,  enabled: Boolean = true, onClick: () -> Unit) {
    Button(
        onClick = onClick,
        enabled = enabled,
        colors = ButtonDefaults.buttonColors(
            containerColor = primaryTeal)
    ) {
        Text(text, style = MaterialTheme.typography.labelSmall, color = Color.White)
    }
}