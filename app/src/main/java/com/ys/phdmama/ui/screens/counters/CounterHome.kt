package com.ys.phdmama.ui.screens.counters

import android.os.Build
import androidx.annotation.RequiresApi
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import com.ys.phdmama.ui.components.PhdLayoutMenu
import com.ys.phdmama.viewmodel.CounterViewModel

@RequiresApi(Build.VERSION_CODES.O)
@Composable
fun CounterHome(navController: NavController,  openDrawer: () -> Unit) {
    PhdLayoutMenu(
        title = "Contadores",
        navController = navController,
        openDrawer = openDrawer
    ) { innerPadding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) {
            CounterComponent()
        }
    }
}

@RequiresApi(Build.VERSION_CODES.O)
@Composable
fun CounterComponent(viewModel: CounterViewModel = androidx.lifecycle.viewmodel.compose.viewModel()) {
    val counter by viewModel.counter.collectAsState()

    Column(
        modifier = Modifier.fillMaxSize().padding(16.dp),
        verticalArrangement = Arrangement.Center
    ) {
        Text("Contador: $counter", style = MaterialTheme.typography.headlineLarge)
        Spacer(Modifier.height(20.dp))
        Row {
            Button(onClick = { viewModel.startCounter() }) {
                Text("Start")
            }
            Spacer(Modifier.width(10.dp))
            Button(onClick = { viewModel.stopCounter() }) {
                Text("Stop")
            }
        }
    }
}
