package com.ys.phdmama.ui.screens.born

import androidx.annotation.DrawableRes
import androidx.compose.foundation.Image
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
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import com.ys.phdmama.R
import com.ys.phdmama.navigation.NavRoutes
import com.ys.phdmama.ui.components.PhdLayoutMenu

data class MenuItemData(val label: String, val description: String, val icon: ImageVector, val route: String, @DrawableRes val image: Int)

val menuItems = listOf(
    MenuItemData(
        label = "Vacunas",
        description = "Controla las vacunas del bebé",
        icon = Icons.Default.Build,
        route = NavRoutes.BORN_VACCINES,
        image = R.mipmap.vacuna_icon_color
    ),
    MenuItemData(
        label = "Crecimiento",
        description = "Verifica el crecimiento del bebé",
        icon = Icons.Default.Create,
        route = NavRoutes.BORN_GROWTHMILESTONES,
        image = R.mipmap.crecimiento
    ),
    MenuItemData(
        label = "Registro de alimentos",
        description = "Ten al día los alimentos que consume tu bebé",
        icon = Icons.Default.Create,
        route = NavRoutes.FOOD_REGISTRATION,
        image = R.mipmap.registro_alimento
    )

)

@Composable
fun BabyMenuScreen(navController: NavController, openDrawer: () -> Unit,) {
    PhdLayoutMenu(
        title = "Bebé",
        navController = navController,
        openDrawer = openDrawer
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp),
            verticalArrangement = Arrangement.Top,
            horizontalAlignment = Alignment.Start
        ) {
            menuItems.forEach { item ->
                MenuListItem(item, navController, item.image)
                Spacer(modifier = Modifier.height(16.dp))
            }
        }
    }
}

@Composable
fun MenuListItem(item: MenuItemData, navController: NavController, @DrawableRes imageResId: Int) {
    ListItem(
        headlineContent = { Text(item.label) },
        supportingContent = { Text(item.description) },
        leadingContent = {
            Image(
                painter = painterResource(id = imageResId),
                contentDescription = "Baby icon",
                modifier = Modifier
                    .size(40.dp)
            )
        },
        modifier = Modifier
            .fillMaxWidth()
            .clickable { navController.navigate(item.route) }
    )
}
