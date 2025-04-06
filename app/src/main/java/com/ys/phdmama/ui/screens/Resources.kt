package com.ys.phdmama.ui.screens

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
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
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import com.ys.phdmama.ui.components.BottomNavigationBar
import com.ys.phdmama.viewmodel.LoginViewModel
import com.ys.phdmama.viewmodel.UserDataViewModel

data class ChecklistItem(
    val id: Int,
    val text: String,
    var checked: Boolean
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
fun ChecklistScreen(loginViewModel: LoginViewModel = viewModel(), userViewModel: UserDataViewModel = viewModel()) {
    val userRole by loginViewModel.userRole.collectAsStateWithLifecycle()
    val textTitle = when (userRole) {
        "born" -> "BEBÉ POSPARTO"
        "waiting" -> "LOGÍSTICA PARA EL GRAN DÍA"
        else -> ""
    }
    var checklistItems by remember { mutableStateOf<List<ChecklistItem>>(emptyList()) }

    LaunchedEffect(userRole) {
        userRole?.let {
            userViewModel.fetchWaitingChecklist(it) { items ->
                checklistItems = items
            }
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
    ) {
        Text(textTitle, style = MaterialTheme.typography.headlineSmall)
        Column(modifier = Modifier.padding(16.dp)) {
            checklistItems.forEach { item ->
                Row(modifier = Modifier.padding(vertical = 8.dp)) {
                    Checkbox(
                        checked = item.checked,
                        onCheckedChange = { checked ->
                            userViewModel.updateCheckedState(item.id, checked, userRole.toString())
                            checklistItems = checklistItems.map {
                                if (it.id == item.id) it.copy(checked = checked) else it
                            }
                        }
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(text = item.text,
                        style = MaterialTheme.typography.bodyLarge,
                        textDecoration = if (item.checked) {
                            TextDecoration.LineThrough} else {TextDecoration.None})
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
            checked = item.checked,
            onCheckedChange = { isChecked ->
                onCheckedChange(item.copy(checked = isChecked))
            }
        )
        Spacer(modifier = Modifier.width(8.dp))
        Text( text = item.text,
            style = MaterialTheme.typography.bodyLarge,
            textDecoration = if (item.checked) {
                TextDecoration.LineThrough} else {TextDecoration.None})
    }
}
