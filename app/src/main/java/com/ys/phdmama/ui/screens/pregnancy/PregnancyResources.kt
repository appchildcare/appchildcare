package com.ys.phdmama.ui.screens.pregnancy

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material3.Icon
import androidx.compose.material3.ListItem
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import com.ys.phdmama.navigation.NavRoutes
import com.ys.phdmama.ui.components.PhdLayoutMenu

data class MenuItemData(val label: String, val description: String, val icon: ImageVector, val route: String)

val menuItems = listOf(
    MenuItemData(
        label = "Checklist Pre parto",
        description = "Información necesaria para el parto",
        icon = Icons.Default.Edit,
        route = NavRoutes.BORN_RESOURCES
    ),
    MenuItemData(
        label = "Checklist dummy",
        description = "checklist description",
        icon = Icons.Default.Edit,
        route = NavRoutes.BORN_RESOURCES
    )
)

@Composable
fun PregnancyResourcesMenuScreen(navController: NavController, openDrawer: () -> Unit) {
    PhdLayoutMenu(
        title = "Panel",
        navController = navController,
        openDrawer = openDrawer
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
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
