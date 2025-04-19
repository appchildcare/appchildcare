package com.ys.phdmama.ui.screens.born

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavHostController
import com.ys.phdmama.ui.components.PhdDropdownType
import com.ys.phdmama.ui.components.PhdLayoutMenu
import com.ys.phdmama.ui.components.PhdTitle
import com.ys.phdmama.viewmodel.BabyDataViewModel
import com.ys.phdmama.viewmodel.BabyProfile

@Composable
fun BabySelectionScreen (
    navController: NavHostController,
    viewModel: BabyDataViewModel = viewModel(),
    openDrawer: () -> Unit
) {
    LaunchedEffect(Unit) {
        viewModel.fetchBabyList()
    }

    val babyList by viewModel.babyListData
    var selectedBaby by remember { mutableStateOf<BabyProfile?>(null) }

    LaunchedEffect(babyList) {
        if (babyList.isNotEmpty() && selectedBaby == null) {
            selectedBaby = babyList.first()
        }
    }

    PhdLayoutMenu(
        title = "Mis Bebés",
        navController = navController,
        openDrawer = openDrawer
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .background(Color(0xFFF2F2F2))
                .verticalScroll(rememberScrollState())
                .padding(24.dp)
        ) {
            PhdTitle("Selección de mi bebé")

            if (babyList.isNotEmpty()) {
                PhdDropdownType(
                    label = "Selecciona un bebé",
                    options = babyList,
                    selectedOption = selectedBaby,
                    onOptionSelected = { selectedBaby = it },
                    optionLabel = { it.name }
                )
            } else {
                Text("No hay bebés registrados.")
            }
        }
    }
}
