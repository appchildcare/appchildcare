package com.ys.phdmama.ui.screens

import android.util.Log
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.tween
import androidx.compose.animation.expandVertically
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkVertically
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowDropDown
import androidx.compose.material.icons.filled.DateRange
import androidx.compose.material.icons.filled.KeyboardArrowUp
import androidx.compose.material.icons.filled.Star
import androidx.compose.material3.AssistChip
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Checkbox
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.SuggestionChip
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavController
import com.ys.phdmama.ui.components.PhdLayoutMenu
import com.ys.phdmama.viewmodel.CheckItemsViewModel
import com.ys.phdmama.viewmodel.ChecklistItem
import com.ys.phdmama.viewmodel.LoginViewModel
import com.ys.phdmama.viewmodel.UserDataViewModel


@Composable
fun Resources(navController: NavController, openDrawer: () -> Unit) {
    PhdLayoutMenu(
        title = "Checklist",
        navController = navController,
        openDrawer = openDrawer
    ) {
        CheckItemsScreen()
    }
}

@Composable
fun CheckItemsScreen(
    checkItemsViewModel: CheckItemsViewModel = hiltViewModel(),
) {
    val topicGroups by checkItemsViewModel.topicGroups.collectAsState()
    val isLoading by checkItemsViewModel.isLoading.collectAsState()
    val error by checkItemsViewModel.error.collectAsState()
    val babyAgeMonths by checkItemsViewModel.babyAgeWeeks.collectAsState()

    val filteredTopicGroups = remember(topicGroups, babyAgeMonths) {
        babyAgeMonths?.toIntOrNull()?.let { ageMonths ->
            topicGroups.filter { it.months == ageMonths }
        } ?: emptyList()
    }


    when {
        isLoading -> {
            Box(
                modifier = Modifier.fillMaxSize(),
                contentAlignment = Alignment.Center
            ) {
                CircularProgressIndicator()
            }
        }

        error != null -> {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(16.dp),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = error ?: "Unknown error",
                    textAlign = TextAlign.Center,
                    color = MaterialTheme.colorScheme.error
                )
            }
        }

        filteredTopicGroups.isEmpty() -> {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(16.dp),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = "No hay checklists disponibles para la edad actual del bebé.",
                    textAlign = TextAlign.Center
                )
            }
        }

        else -> {
            LazyColumn(
                modifier = Modifier.fillMaxSize(),
                contentPadding = PaddingValues(16.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                items(topicGroups.size) { index ->
                    TopicSection(
                        topicGroup = topicGroups[index],
                        onItemCheckedChange = { itemId, checked ->
                            checkItemsViewModel.toggleItemChecked(itemId)
                        }
                    )
                }
            }
        }
    }
}



@Composable
fun TopicSection(
    topicGroup: CheckItemsViewModel.TopicGroup,
    onItemCheckedChange: (String, Boolean) -> Unit
) {
    Column(
        modifier = Modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        // Topic header with badge
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 8.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = topicGroup.topic,
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.weight(1f)
            )

            if (topicGroup.months > 0) {
                AssistChip(
                    onClick = { },
                    label = {
                        Text(
                            text = "${topicGroup.months} meses",
                            style = MaterialTheme.typography.labelMedium
                        )
                    },
                    leadingIcon = {
                        Icon(
                            imageVector = Icons.Default.DateRange,
                            contentDescription = "Months",
                            modifier = Modifier.size(18.dp)
                        )
                    }
                )
            }
        }

        // All subtopic groups under this topic
        topicGroup.subtopicGroups.forEach { subtopicGroup ->
            ExpandableListItem(
                subtopicGroup = subtopicGroup,
                onItemCheckedChange = onItemCheckedChange
            )
        }
    }
}

@Composable
fun ExpandableListItem(
    subtopicGroup: CheckItemsViewModel.SubtopicGroup,
    onItemCheckedChange: (String, Boolean) -> Unit
) {
    var expanded by remember { mutableStateOf(false) }

    Card(
        modifier = Modifier.fillMaxWidth(),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(
            modifier = Modifier.fillMaxWidth()
        ) {
            // Header - Subtopic (clickable to expand/collapse)
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable { expanded = !expanded }
                    .padding(16.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = subtopicGroup.subtopic,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.weight(1f)
                )

                Icon(
                    imageVector = if (expanded) {
                        Icons.Default.KeyboardArrowUp
                    } else {
                        Icons.Default.ArrowDropDown
                    },
                    contentDescription = if (expanded) "Collapse" else "Expand"
                )
            }

            // Expandable content - List of items
            AnimatedVisibility(
                visible = expanded,
                enter = expandVertically(
                    animationSpec = tween(durationMillis = 300)
                ) + fadeIn(),
                exit = shrinkVertically(
                    animationSpec = tween(durationMillis = 300)
                ) + fadeOut()
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(start = 16.dp, end = 16.dp, bottom = 8.dp)
                ) {
                    HorizontalDivider(
                        modifier = Modifier.padding(bottom = 8.dp),
                        color = MaterialTheme.colorScheme.outlineVariant
                    )

                    subtopicGroup.items.forEachIndexed { index, item ->
                        ChecklistItemRow(
                            item = item,
                            onCheckedChange = { checked ->
                                onItemCheckedChange(item.id, checked)
                            }
                        )

                        // Add divider between items (but not after the last one)
                        if (index < subtopicGroup.items.size - 1) {
                            HorizontalDivider(
                                modifier = Modifier.padding(vertical = 8.dp),
                                color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f)
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun ChecklistItemRow(
    item: CheckItemsViewModel.ChecklistItem,
    onCheckedChange: (Boolean) -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Checkbox item
            item.isChecked?.let {
                Checkbox(
                    checked = it,
                    onCheckedChange = { checked ->
                        onCheckedChange(checked)
                    }
                )
            }

            // Item text beside checkbox
            Text(
                text = item.item,
                style = MaterialTheme.typography.bodyMedium,
                textDecoration = if (item.isChecked == true) TextDecoration.LineThrough else null,
                color = if (item.isChecked == true) {
                    MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
                } else {
                    MaterialTheme.colorScheme.onSurface
                },
                modifier = Modifier
                    .weight(1f)
                    .padding(start = 8.dp)
            )
        }

        // Metadata chips
        Row(
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            modifier = Modifier.fillMaxWidth()
        ) {
            if (item.topic.isNotEmpty()) {
                SuggestionChip(
                    onClick = { },
                    label = {
                        Text(
                            text = item.topic,
                            style = MaterialTheme.typography.labelSmall
                        )
                    },
                    icon = {
                        Icon(
                            imageVector = Icons.Default.Star,
                            contentDescription = "Topic",
                            modifier = Modifier.size(16.dp)
                        )
                    }
                )
            }

            if (item.months > 0) {
                SuggestionChip(
                    onClick = { },
                    label = {
                        Text(
                            text = "${item.months} months",
                            style = MaterialTheme.typography.labelSmall
                        )
                    },
                    icon = {
                        Icon(
                            imageVector = Icons.Default.DateRange,
                            contentDescription = "Months",
                            modifier = Modifier.size(16.dp)
                        )
                    }
                )
            }
        }
    }
}

sealed class ChecklistDisplayItem {
    data class TopicHeader(val topic: String) : ChecklistDisplayItem()
    data class ChecklistEntry(val item: ChecklistItem) : ChecklistDisplayItem()
}

@Composable
fun ChecklistScreen(
    loginViewModel: LoginViewModel = hiltViewModel(),
    userViewModel: UserDataViewModel = hiltViewModel()
) {
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
                                userViewModel.updateCheckedState(
                                    item.id,
                                    checked,
                                    userRole.toString()
                                )
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
