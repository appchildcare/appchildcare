package com.ys.phdmama.ui.screens

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Menu
import androidx.compose.material3.Checkbox
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import com.ys.phdmama.ui.components.BottomNavigationBar

data class ChecklistItem(
    val id: Int,
    val text: String,
    var isChecked: Boolean = false
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun Resources(navController: NavController, openDrawer: () -> Unit) {

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Recursos") },
                navigationIcon = {
                    IconButton(onClick = openDrawer) {
                        Icon(Icons.Default.Menu, contentDescription = "Menu")
                    }
                }
            )
        },
        bottomBar = { BottomNavigationBar(navController) }
    ) { innerPadding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) {
            ChecklistScreen()
        }
    }
}

@Composable
fun ChecklistScreen() {
    val checklistItems = remember {
        mutableStateListOf(
            ChecklistItem(1, " Empezar a tomar suplemento de ácido fólico"),
            ChecklistItem(2, "Historial médico completo (personal y familiar)."),
            ChecklistItem(3, "Limitar el consumo de cafeína.")
        )
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
    ) {
        Text("Checklist del bebé", style = MaterialTheme.typography.headlineSmall)

        LazyColumn {
            items(checklistItems) { item ->
                ChecklistItemRow(item) { updatedItem ->
                    val index = checklistItems.indexOfFirst { it.id == updatedItem.id }
                    if (index != -1) {
                        checklistItems[index] = updatedItem
                    }
                }
            }
        }
    }
}

@Composable
fun ChecklistItemRow(item: ChecklistItem, onCheckedChange: (ChecklistItem) -> Unit) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(8.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Checkbox(
            checked = item.isChecked,
            onCheckedChange = { isChecked ->
                onCheckedChange(item.copy(isChecked = isChecked))
            }
        )
        Spacer(modifier = Modifier.width(8.dp))
        Text( text = item.text,
            style = MaterialTheme.typography.bodyLarge,
            textDecoration = if (item.isChecked) {
                TextDecoration.LineThrough} else {TextDecoration.None})
    }
}
