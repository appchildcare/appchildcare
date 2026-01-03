package com.ys.phdmama.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowDropDown
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.Icon
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color

@Composable
fun PhdDropdown(label: String, options: List<String>, selectedOption: String, onOptionSelected: (String) -> Unit) {
    Column {
        PhdTextBold(label)
        var expanded by remember { mutableStateOf(false) }

        Box {
            OutlinedTextField(
                value = selectedOption,
                onValueChange = {},
                modifier = Modifier
                    .fillMaxWidth()
                    .background(color = Color.White),
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