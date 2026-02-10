package com.ys.phdmama.ui.screens

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
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowDropDown
import androidx.compose.material.icons.filled.DateRange
import androidx.compose.material.icons.filled.KeyboardArrowUp
import androidx.compose.material3.AssistChip
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Checkbox
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
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
import androidx.navigation.NavController
import com.ys.phdmama.ui.components.PhdLayoutMenu
import com.ys.phdmama.viewmodel.CheckItemsViewModel

@Composable
fun Resources(navController: NavController, userRole: String?, openDrawer: () -> Unit) {
    PhdLayoutMenu(
        title = "Checklist",
        navController = navController,
        openDrawer = openDrawer
    ) {
        CheckItemsScreen(userRole)
    }
}

@Composable
fun CheckItemsScreen(
    userRole: String?,
    checkItemsViewModel: CheckItemsViewModel = hiltViewModel(),
) {
    val topicGroups by checkItemsViewModel.topicGroups.collectAsState()
    val isLoading by checkItemsViewModel.isLoading.collectAsState()
    val error by checkItemsViewModel.error.collectAsState()
    val babyAgeMonths by checkItemsViewModel.babyAgeWeeks.collectAsState()

    val filteredTopicGroups = remember(topicGroups, babyAgeMonths, userRole) {

        babyAgeMonths?.toIntOrNull()?.let { ageMonths ->
            val filtered = topicGroups.filter {
                val matches = it.months == ageMonths && it.role == userRole
                matches
            }
            filtered
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
                items(filteredTopicGroups.size) { index ->
                    TopicSection(
                        topicGroup = filteredTopicGroups[index],
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
    }
}

