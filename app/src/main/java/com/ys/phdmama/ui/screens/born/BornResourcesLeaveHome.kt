package com.ys.phdmama.ui.screens.born

import android.util.Log
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.Checkbox
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
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
import com.ys.phdmama.ui.components.PhdLayoutMenu
import com.ys.phdmama.viewmodel.ChecklistItem
import com.ys.phdmama.viewmodel.LoginViewModel
import com.ys.phdmama.viewmodel.UserDataViewModel

@Composable
fun BornResourcesLeaveHome(navController: NavController, openDrawer: () -> Unit) {
    PhdLayoutMenu(
        title = "Recursos",
        navController = navController,
        openDrawer = openDrawer
    ) {
        Box(
            modifier = Modifier
                .fillMaxSize(),
        ) {
            ChecklistScreen()
        }
    }
}

sealed class ChecklistDisplayItem {
    data class TopicHeader(val topic: String) : ChecklistDisplayItem()
    data class ChecklistEntry(val item: ChecklistItem) : ChecklistDisplayItem()
}

@Composable
fun ChecklistScreen(
    loginViewModel: LoginViewModel = viewModel(),
    userViewModel: UserDataViewModel = viewModel()
) {
    val userRole by loginViewModel.userRole.collectAsStateWithLifecycle()
    val textTitle = "Checklist del viaje"
    var checklistItems by remember { mutableStateOf<List<ChecklistItem>>(emptyList()) }

    LaunchedEffect(userRole) {
        userViewModel.fetchLeaveHomeChecklist { items ->
            checklistItems = items
        }
    }

    // Group items and flatten into displayable list
    val groupedDisplayItems = remember(checklistItems) {
        checklistItems
            .groupBy { it.topic }
            .flatMap { (topic, items) ->
                listOf(ChecklistDisplayItem.TopicHeader(topic)) +
                        items.map { ChecklistDisplayItem.ChecklistEntry(it) }
            }
    }
    checklistItems.forEach {
        Log.d("ChecklistItemLoaded", "ID: ${it.id}, Topic: ${it.topic}, Checked: ${it.checked}")
    }

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
    ) {
        item {
            Text(textTitle, style = MaterialTheme.typography.headlineSmall)
            Spacer(modifier = Modifier.height(16.dp))
        }

        items(
            items = groupedDisplayItems,
            key = { displayItem ->
                when (displayItem) {
                    is ChecklistDisplayItem.TopicHeader -> "header_${displayItem.topic}"
                    is ChecklistDisplayItem.ChecklistEntry -> "item_${displayItem.item.topic}_${displayItem.item.id}_${displayItem.item.text.hashCode()}"

                }
            }
        ) { displayItem ->
            when (displayItem) {
                is ChecklistDisplayItem.TopicHeader -> {
                    Text(
                        text = displayItem.topic,
                        style = MaterialTheme.typography.titleMedium,
                        modifier = Modifier
                            .padding(vertical = 12.dp)
                    )
                }

                is ChecklistDisplayItem.ChecklistEntry -> {
                    val item = displayItem.item
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 4.dp)
                    ) {
                        Checkbox(
                            checked = item.checked,
                            onCheckedChange = { checked ->
                                userViewModel.updateCheckedStateBornLeave(item.id, checked)
                                checklistItems = checklistItems.map {
                                    if (it.id == item.id && it.topic == item.topic) it.copy(checked = checked) else it
                                }
                            }
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = item.text,
                            style = MaterialTheme.typography.bodyLarge,
                            textDecoration = if (item.checked) TextDecoration.LineThrough else TextDecoration.None
                        )
                    }
                }
            }
        }
    }
}