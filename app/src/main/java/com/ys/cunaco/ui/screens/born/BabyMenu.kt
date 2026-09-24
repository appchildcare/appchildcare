package com.ys.cunaco.ui.screens.born

import androidx.annotation.DrawableRes
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import com.ys.cunaco.R
import com.ys.cunaco.navigation.NavRoutes
import com.ys.cunaco.ui.components.PhdClickableCard
import com.ys.cunaco.ui.components.PhdLayoutMenu

data class MenuItemData(
    val label: Int,
    @DrawableRes val icon: Int,
    val route: String,
    val backgroundColor: Color
)

// Pastel tones sampled from the reference design
private val cardMint = Color(0xFF9FE3D0)
private val cardTeal = Color(0xFF7FB8C4)
private val cardSky  = Color(0xFF9FD6E8)

val menuItems = listOf(
    MenuItemData(R.string.baby_menu_vaccine_title, R.drawable.icono_vacunas, NavRoutes.BORN_VACCINES, cardMint),
    MenuItemData(R.string.baby_menu_growth_title, R.drawable.icono_crecimiento_cinta, NavRoutes.BORN_GROWTHMILESTONES, cardTeal),
    MenuItemData(R.string.baby_menu_food_title, R.drawable.icono_alimentos, NavRoutes.FOOD_REGISTRATION, cardMint),
    MenuItemData(R.string.baby_menu_mediccine_title, R.drawable.icono_medicina, NavRoutes.MEDICINE_REGISTRATION, cardSky),
)

@Composable
fun BabyMenuScreen(navController: NavController, openDrawer: () -> Unit) {
    PhdLayoutMenu(
        title = stringResource(R.string.baby_menu_screen_label),
        navController = navController,
        openDrawer = openDrawer
    ) {
        LazyVerticalGrid(
            columns = GridCells.Fixed(2),
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp),
            horizontalArrangement = Arrangement.spacedBy(16.dp),
            verticalArrangement = Arrangement.spacedBy(24.dp)
        ) {
            items(menuItems) { item ->
                PhdClickableCard(
                    title = stringResource(item.label),
                    imageResId = item.icon,
                    backgroundColor = item.backgroundColor,
                    onClick = { navController.navigate(item.route) }
                )
            }
        }
    }
}
