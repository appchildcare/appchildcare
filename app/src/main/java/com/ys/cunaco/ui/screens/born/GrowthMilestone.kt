package com.ys.cunaco.ui.screens.born

import android.util.Log
import android.widget.NumberPicker
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavHostController
import com.ys.cunaco.R
import com.ys.cunaco.ui.components.PhdLayoutMenu
import com.ys.cunaco.viewmodel.GrowthMilestonesViewModel


@Composable
fun GrowthMilestonesScreen(
    navController: NavHostController,
    viewModel: GrowthMilestonesViewModel = hiltViewModel(),
    openDrawer: () -> Unit,
    babyId: String?
) {
    // Text field states
    var weight by remember { mutableStateOf("") }
    var height by remember { mutableStateOf("") }
    var headCircumference by remember { mutableStateOf("") }

    // Alert visibility
    var showAlert by remember { mutableStateOf(false) }

    var selectedNumber by remember { mutableIntStateOf(1) }

    LaunchedEffect(babyId) {
        babyId?.let {
            viewModel.setBabyId(it)
        }
    }

    PhdLayoutMenu(
        title = stringResource(R.string.growth_milestones_title),
        navController = navController,
        openDrawer = openDrawer
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(16.dp)
        ) {
            Text(
                text = stringResource(R.string.growth_milestones_register),
                style = MaterialTheme.typography.titleLarge
            )

            Spacer(modifier = Modifier.height(16.dp))

            OutlinedTextField(
                value = weight,
                onValueChange = { weight = it },
                label = {
                    Text(stringResource(R.string.growth_weight))
                },
                modifier = Modifier.fillMaxWidth()
            )

            Spacer(modifier = Modifier.height(16.dp))

            OutlinedTextField(
                value = height,
                onValueChange = { height = it },
                label = {
                    Text(stringResource(R.string.growth_height))
                },
                modifier = Modifier.fillMaxWidth()
            )

            Spacer(modifier = Modifier.height(16.dp))

            OutlinedTextField(
                value = headCircumference,
                onValueChange = { headCircumference = it },
                label = {
                    Text(stringResource(R.string.growth_head_circumference))
                },
                modifier = Modifier.fillMaxWidth()
            )

            Spacer(modifier = Modifier.height(16.dp))

            LabeledAndroidNumberPicker(
                label = stringResource(R.string.growth_select_age),
                value = selectedNumber,
                range = 1..60,
                onValueChange = {
                    selectedNumber = it
                }
            )

            Spacer(modifier = Modifier.height(16.dp))

            Button(
                onClick = {
                    if (
                        weight.isNotEmpty() &&
                        height.isNotEmpty() &&
                        headCircumference.isNotEmpty()
                    ) {
                        val milestoneData = mapOf(
                            "weight" to weight,
                            "height" to height,
                            "headCircumference" to headCircumference,
                            "timestamp" to System.currentTimeMillis(),
                            "ageInMonths" to selectedNumber
                        )

                        viewModel.saveGrowthMilestone(
                            milestoneData = milestoneData,
                            onSuccess = {
                                showAlert = true
                            },
                            onError = { errorMessage ->
                                Log.e(
                                    "GrowthMilestone",
                                    "Error al guardar: $errorMessage"
                                )
                            }
                        )
                    } else {
                        viewModel.clearErrorMessage()
                    }
                }
            ) {
                Text(
                    text = stringResource(R.string.growth_save)
                )
            }
        }

        // Confirmation dialog
        if (showAlert) {
            AlertDialog(
                onDismissRequest = {
                    showAlert = false
                },
                title = {
                    Text(
                        text = stringResource(
                            R.string.growth_confirmation_title
                        )
                    )
                },
                text = {
                    Text(
                        text = stringResource(
                            R.string.growth_data_registered
                        )
                    )
                },
                confirmButton = {
                    Button(
                        onClick = {
                            showAlert = false
                            navController.popBackStack()
                        }
                    ) {
                        Text(
                            text = stringResource(
                                R.string.growth_accept
                            )
                        )
                    }
                }
            )
        }
    }
}

@Composable
fun LabeledAndroidNumberPicker(
    label: String,
    value: Int,
    range: IntRange,
    onValueChange: (Int) -> Unit
) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = Modifier.padding(16.dp)
    ) {
        Text(
            text = label,
            style = MaterialTheme.typography.bodyLarge,
            modifier = Modifier.padding(bottom = 10.dp)
        )

        AndroidView(
            modifier = Modifier.wrapContentSize(),
            factory = { context ->
                NumberPicker(context).apply {
                    minValue = range.first
                    maxValue = range.last

                    setOnValueChangedListener { _, _, newVal ->
                        onValueChange(newVal)
                    }
                }
            },
            update = { picker ->
                picker.value = value
            }
        )
    }
}
