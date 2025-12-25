package com.ys.phdmama.ui.components

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Menu
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.paint
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.navigation.NavController
import com.ys.phdmama.R
import com.ys.phdmama.ui.theme.secondaryCream

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PhdLayoutMenu(
    title: String,
    navController: NavController,
    openDrawer: () -> Unit,
    showBottomBar: Boolean = true,
    snackbarHostState: SnackbarHostState? = null,
    content: @Composable (PaddingValues) -> Unit
) {
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(title, style = MaterialTheme.typography.titleMedium) },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = secondaryCream,
                ),
                navigationIcon = {
                    IconButton(onClick = openDrawer) {
                        Icon(Icons.Default.Menu, contentDescription = "Menu")
                    }
                }
            )
        },
        bottomBar = {
            if (showBottomBar) {
                BottomNavigationBar(navController)
            }
        },
        snackbarHost = {
            snackbarHostState?.let { SnackbarHost(it) }
        },
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxHeight()
                .paint(
                    painter = painterResource(R.drawable.background_sun),
                    contentScale = ContentScale.Crop
                )
                .padding(innerPadding),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            content(innerPadding)
        }
    }
}
