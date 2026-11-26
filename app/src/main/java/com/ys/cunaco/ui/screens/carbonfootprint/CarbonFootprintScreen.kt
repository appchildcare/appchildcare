@file:OptIn(ExperimentalMaterial3Api::class)

package com.ys.cunaco.ui.screens.carbonfootprint

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import com.ys.cunaco.R
import com.ys.cunaco.ui.components.PhdLayoutMenu
import com.ys.cunaco.ui.components.PhdSubtitle
import com.ys.cunaco.ui.theme.primaryYellow
import com.ys.cunaco.viewmodel.CarbonFootprintViewModel
import java.util.Locale

@Composable
fun CarbonFootprintScreen(viewModel: CarbonFootprintViewModel, navController: NavController,
                          openDrawer: () -> Unit) {
    PhdLayoutMenu(
        title = stringResource(id = R.string.carbon_footprint_title),
        navController = navController,
        openDrawer = openDrawer
    ) {
        DataEntryScreen(viewModel)
    }
}

@Composable
fun DataEntryScreen(viewModel: CarbonFootprintViewModel) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
            .verticalScroll(rememberScrollState()),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        PhdSubtitle(stringResource(id = R.string.carbon_footprint_subtitle))

        // Input Fields
        InputField(
            label = stringResource(id = R.string.label_disposable_diapers),
            value = viewModel.disposableDiapers,
            onValueChange = { viewModel.disposableDiapers = it },
        )

        InputField(
            label = stringResource(id = R.string.label_cloth_diapers),
            value = viewModel.clothDiapers,
            onValueChange = { viewModel.clothDiapers = it },
        )

        InputField(
            label = stringResource(id = R.string.label_wet_wipes),
            value = viewModel.wetWipes,
            onValueChange = { viewModel.wetWipes = it },
        )

        InputField(
            label = stringResource(id = R.string.label_formula_feedings),
            value = viewModel.formulaFeedings,
            onValueChange = { viewModel.formulaFeedings = it },
        )

        InputField(
            label = stringResource(id = R.string.label_bottle_washes),
            value = viewModel.bottleWashes,
            onValueChange = { viewModel.bottleWashes = it },
        )

        InputField(
            label = stringResource(id = R.string.label_baths),
            value = viewModel.baths,
            onValueChange = { viewModel.baths = it },
        )

        if (viewModel.showResult) {
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(containerColor = Color(0xFFE8F5E8))
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text(
                        text = stringResource(
                            id = R.string.carbon_footprint_calculated,
                            String.format(Locale.getDefault(), "%.2f", viewModel.totalFootprint)
                        ),
                        color = Color(0xFF2E7D32),
                        style = MaterialTheme.typography.titleMedium
                    )
                    
                    Spacer(modifier = Modifier.height(16.dp))
                    
                    val savingsFormatted = String.format(Locale.getDefault(), "%.2f", viewModel.savings)
                    val monthlySavingsFormatted = String.format(Locale.getDefault(), "%.2f", viewModel.monthlySavingsValue)
                    val yearlySavingsFormatted = String.format(Locale.getDefault(), "%.2f", viewModel.yearlySavingsValue)
                    val analogy = stringResource(id = viewModel.analogyResId)

                    val ecoMessage = when (viewModel.ecoMessageResId) {
                        R.string.eco_msg_excellent -> stringResource(
                            id = R.string.eco_msg_excellent,
                            savingsFormatted,
                            viewModel.clothDiapersCount,
                            viewModel.clothWashesCount,
                            monthlySavingsFormatted,
                            yearlySavingsFormatted,
                            analogy
                        )
                        R.string.eco_msg_progress -> stringResource(
                            id = R.string.eco_msg_progress,
                            viewModel.clothDiapersCount,
                            viewModel.clothWashesCount,
                            monthlySavingsFormatted,
                            yearlySavingsFormatted,
                            analogy
                        )
                        R.string.eco_msg_consider -> stringResource(
                            id = R.string.eco_msg_consider,
                            monthlySavingsFormatted,
                            yearlySavingsFormatted,
                            analogy
                        )
                        else -> ""
                    }

                    Text(
                        text = ecoMessage,
                        color = Color(0xFF2E7D32)
                    )
                }

                Image(
                    painter = painterResource(id = R.drawable.iconos_arbol),
                    contentDescription = stringResource(id = R.string.image_description_tree),
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(180.dp)
                )
            }
        }

        // Calculate Button
        Button(
            onClick = { viewModel.calculateFootprint() },
            modifier = Modifier
                .fillMaxWidth()
                .height(56.dp),
            enabled = !viewModel.isLoading,
            colors = ButtonDefaults.buttonColors(
                containerColor = primaryYellow
            )
        ) {
            if (viewModel.isLoading) {
                CircularProgressIndicator(
                    modifier = Modifier
                        .width(20.dp)
                        .height(20.dp),
                    color = Color.White
                )
            } else {
                Text(stringResource(id = R.string.btn_calculate_footprint), fontSize = 16.sp)
            }
        }

        // Clear Button
        Button(
            onClick = { viewModel.clearForm() },
            modifier = Modifier
                .fillMaxWidth()
                .height(56.dp),
            colors = ButtonDefaults.buttonColors(
                containerColor = Color.Gray
            )
        ) {
            Text(stringResource(id = R.string.btn_clear_form), fontSize = 16.sp, color = Color.White)
        }

        // Messages
        if (viewModel.errorMessage.isNotEmpty()) {
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(containerColor = Color(0xFFFFEBEE))
            ) {
                Text(
                    text = stringResource(id = R.string.carbon_footprint_error, viewModel.errorMessage),
                    modifier = Modifier.padding(16.dp),
                    color = Color(0xFFD32F2F)
                )
            }
        }
    }
}

@Composable
fun InputField(
    label: String,
    value: String,
    onValueChange: (String) -> Unit,
) {
    OutlinedTextField(
        value = value,
        onValueChange = onValueChange,
        label = { Text(label) },

        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
        modifier = Modifier.fillMaxWidth()
            .padding(16.dp, 4.dp)
            .background(color = Color.White),
        singleLine = true,
    )
}
