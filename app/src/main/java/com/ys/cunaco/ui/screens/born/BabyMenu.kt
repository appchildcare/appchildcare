package com.ys.cunaco.ui.screens.born

import androidx.annotation.DrawableRes
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Build
import androidx.compose.material.icons.filled.Create
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import com.ys.cunaco.R
import com.ys.cunaco.navigation.NavRoutes
import com.ys.cunaco.ui.components.PhdLayoutMenu
import com.ys.cunaco.ui.theme.primaryTeal

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
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            menuItems.forEach { item ->
                TrackingOptionCard(
                    title = stringResource(item.label),
                    subtitle = stringResource(item.description),
                    imageResId = item.image,
                    gradientColors = listOf(
                        primaryTeal,
                        primaryTeal
                    ),
                    onClick = {
                        navController.navigate(item.route)
                    }
                )
                Spacer(modifier = Modifier.height(16.dp))
            }
        }
    }
}

@Composable
fun TrackingOptionCard(
    title: String,
    subtitle: String,
    @DrawableRes imageResId: Int? = null,
    gradientColors: List<Color>,
    onClick: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .height(120.dp)
            .clickable { onClick() },
        shape = RoundedCornerShape(20.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 8.dp)
    ) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(
                    Brush.horizontalGradient(gradientColors)
                )
        ) {
            Row(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(20.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Column(
                    modifier = Modifier.weight(1f)
                ) {
                    Text(
                        text = title,
                        fontSize = 24.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFF5B5C61)
                    )
                    Text(
                        text = subtitle,
                        fontSize = 14.sp,
                        modifier = Modifier.padding(top = 4.dp),
                        color = Color(0xFF5B5C61)
                    )
                }

                if (imageResId != null) {
                    Image(
                        painter = painterResource(id = imageResId),
                        contentDescription = null,
                        modifier = Modifier.size(56.dp)
                    )
                }
            }
        }
    }
}