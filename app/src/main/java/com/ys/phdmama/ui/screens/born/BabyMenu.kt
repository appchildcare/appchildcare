package com.ys.phdmama.ui.screens.born

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Build
import androidx.compose.material.icons.filled.Create
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import com.ys.phdmama.navigation.NavRoutes
import com.ys.phdmama.ui.components.BottomNavigationBar

data class MenuItemData(val label: String, val description: String, val icon: ImageVector, val route: String)

val menuItems = listOf(
    MenuItemData(
        label = "Vacunas",
        description = "Controla las vacunas del bebé",
        icon = Icons.Default.Build,
        route = NavRoutes.BORN_VACCINES
    ),
    MenuItemData(
        label = "Crecimiento",
        description = "Verifica el crecimiento del bebé",
        icon = Icons.Default.Create,
        route = NavRoutes.BORN_GROWTHMILESTONES
    )
)

@Composable
fun BabyMenuScreen(navController: NavController) {
    Scaffold(
        bottomBar = { BottomNavigationBar(navController) }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .padding(16.dp),
            verticalArrangement = Arrangement.Top,
            horizontalAlignment = Alignment.Start
        ) {
            menuItems.forEach { item ->
                MenuListItem(item, navController)
                Spacer(modifier = Modifier.height(16.dp))
            }
        }
    }
}

@Composable
fun MenuListItem(item: MenuItemData, navController: NavController) {
    ListItem(
        headlineContent = { Text(item.label) },
        supportingContent = { Text(item.description) },
        leadingContent = {
            Icon(item.icon, contentDescription = item.label)
        },
        modifier = Modifier
            .fillMaxWidth()
            .clickable { navController.navigate(item.route) }
    )
}
