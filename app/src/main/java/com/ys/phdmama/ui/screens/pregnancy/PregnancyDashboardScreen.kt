package com.ys.phdmama.ui.screens.pregnancy

import android.os.Build
import androidx.annotation.RequiresApi
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Menu
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavHostController
import com.ys.phdmama.R
import com.ys.phdmama.ui.components.BottomNavigationBar
import com.ys.phdmama.viewmodel.UserDataViewModel
import java.time.LocalDate
import java.time.ZoneId
import java.time.temporal.ChronoUnit

@RequiresApi(Build.VERSION_CODES.O)
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PregnancyDashboardScreen (
    navController: NavHostController,
    viewModel: UserDataViewModel = viewModel(),
    openDrawer: () -> Unit
) {
    val currentUser by viewModel.currentUser.collectAsState()

    LaunchedEffect(Unit) {
        viewModel.fetchCurrentUser()
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Panel") },
                navigationIcon = {
                    IconButton(onClick = openDrawer) {
                        Icon(Icons.Default.Menu, contentDescription = "Menu")
                    }
                }
            )
        },
        bottomBar = { BottomNavigationBar(navController) }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {

            Image(
                painter = painterResource(id = R.mipmap.pregnant_woman),
                contentDescription = "Auth image",
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp, 4.dp)
            )

            if (currentUser == null) {
                Text("Loading...")
            } else {
                Spacer(modifier = Modifier.height(16.dp))
                Text(
                    text = "${currentUser?.displayName}, tu fecha de parto aproximada es:",
                    style = MaterialTheme.typography.titleMedium
                )
                val formattedDate = remember(currentUser?.birthProximateDate) {
                    android.text.format.DateFormat.format("dd MMMM yyyy", currentUser?.birthProximateDate).toString()
                }
                Text(text = formattedDate,
                    style = MaterialTheme.typography.bodyLarge)

                val fechaFinal = currentUser?.birthProximateDate
                    ?.toInstant()?.atZone(ZoneId.systemDefault())?.toLocalDate()

                val diasEntreFechas = ChronoUnit.DAYS.between(LocalDate.now(), fechaFinal)

                Spacer(modifier = Modifier.height(32.dp))
                Text(
                    text = "Días para tu fecha de parto",
                    style = MaterialTheme.typography.titleMedium
                )
                Text(
                    text = "$diasEntreFechas",
                    style = MaterialTheme.typography.titleLarge
                )
            }

            Spacer(modifier = Modifier.height(16.dp))

        }
    }
}
