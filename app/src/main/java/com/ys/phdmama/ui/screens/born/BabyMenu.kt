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
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import com.ys.phdmama.R
import com.ys.phdmama.navigation.NavRoutes
import com.ys.phdmama.ui.components.PhdLayoutMenu

data class MenuItemData(val label: Int, val description: Int, val icon: ImageVector, val route: String, @DrawableRes val image: Int)

val menuItems = listOf(
    MenuItemData(
        label = R.string.baby_menu_vaccine_title,
        description = R.string.baby_menu_vaccine_desc,
        icon = Icons.Default.Build,
        route = NavRoutes.BORN_VACCINES,
        image = R.mipmap.vacuna_icon_color
    ),
    MenuItemData(
        label = R.string.baby_menu_growth_title,
        description = R.string.baby_menu_growth_desc,
        icon = Icons.Default.Create,
        route = NavRoutes.BORN_GROWTHMILESTONES,
        image = R.mipmap.crecimiento
    ),
    MenuItemData(
        label = R.string.baby_menu_food_title,
        description = R.string.baby_menu_food_desc,
        icon = Icons.Default.Create,
        route = NavRoutes.FOOD_REGISTRATION,
        image = R.mipmap.registro_alimento
    ),
    MenuItemData(
        label = R.string.baby_menu_mediccine_title,
        description = R.string.baby_menu_mediccine_desc,
        icon = Icons.Default.Create,
        route = NavRoutes.MEDICINE_REGISTRATION,
        image = R.mipmap.medicina_registro
    )
)

@Composable
fun BabyMenuScreen(navController: NavController, openDrawer: () -> Unit,) {
    PhdLayoutMenu(
        title = stringResource(R.string.baby_menu_screen_label),
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
    val label = stringResource(item.label)
    val desc = stringResource(item.description)
    ListItem(
        headlineContent = { Text(label) },
        supportingContent = { Text(desc) },
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
