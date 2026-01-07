package com.ys.phdmama.ui.screens.foodregistration

import android.os.Build
import android.util.Log
import androidx.annotation.RequiresApi
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.RadioButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.graphics.Color
import androidx.navigation.NavHostController
import androidx.compose.ui.platform.LocalContext
import androidx.hilt.navigation.compose.hiltViewModel
import com.ys.phdmama.model.FoodReaction
import com.ys.phdmama.ui.components.EditableField
import com.ys.phdmama.ui.components.PhdBoldText
import com.ys.phdmama.ui.components.PhdLayoutMenu
import com.ys.phdmama.ui.theme.secondaryCream
import com.ys.phdmama.ui.components.PhdGenericCardList
import com.ys.phdmama.ui.components.PhdEditItemDialog
import com.ys.phdmama.ui.components.PhdLabelText
import com.ys.phdmama.ui.components.PhdNormalText
import com.ys.phdmama.ui.components.PhdSubtitle
import com.ys.phdmama.viewmodel.FoodRegistrationViewModel

@RequiresApi(Build.VERSION_CODES.Q)
@Composable
fun FoodRegistrationScreen(
    navController: NavHostController,
    openDrawer: () -> Unit,
    viewModel: FoodRegistrationViewModel = hiltViewModel()
) {
    val selectedBaby by viewModel.selectedBaby.collectAsState()
    val foodList by viewModel.foodList.collectAsState()

    LaunchedEffect(selectedBaby) {
        if (selectedBaby != null) {
            viewModel.loadFoodReactions()
        }
    }

    PhdLayoutMenu(
        title = "Registro de alimentos",
        navController = navController,
        openDrawer = openDrawer
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .background(secondaryCream)
                .padding(16.dp)
        ) {
            Spacer(modifier = Modifier.height(16.dp))

            PhdLabelText("Alimento")
            TextField(
                value = viewModel.foodName,
                onValueChange = { viewModel.foodName = it },
                modifier = Modifier.fillMaxWidth(),
            )

            Spacer(modifier = Modifier.height(16.dp))

            PhdLabelText("Reacción")
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.Start,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.clickable { viewModel.hasReaction = true }
                ) {
                    RadioButton(
                        selected = viewModel.hasReaction == true,
                        onClick = { viewModel.hasReaction = true }
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("Sí")
                }

                Spacer(modifier = Modifier.width(32.dp))

                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.clickable { viewModel.hasReaction = false }
                ) {
                    RadioButton(
                        selected = viewModel.hasReaction == false,
                        onClick = { viewModel.hasReaction = false }
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("No")
                }
            }

            if (viewModel.hasReaction == true) {
                Spacer(modifier = Modifier.height(16.dp))

                PhdLabelText("Detalle de la reacción")
                TextField(
                    value = viewModel.reactionDetail,
                    onValueChange = { viewModel.reactionDetail = it },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(120.dp)
                )
            }

            Spacer(modifier = Modifier.height(16.dp))

            Button(
                onClick = {
                    Log.d("FoodScreen", "Guardar clicked - Food: ${viewModel.foodName}, HasReaction: ${viewModel.hasReaction}")
                    viewModel.saveFoodReaction()
                },
                enabled = viewModel.foodName.isNotBlank() && viewModel.hasReaction != null,
                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFFADA7D)),
                shape = RoundedCornerShape(20.dp),
                modifier = Modifier.align(Alignment.End)
            ) {
                Text("Guardar")
            }

            Spacer(modifier = Modifier.height(32.dp))

            if(foodList.none { it.hasReaction }) {
                PhdNormalText("No hay registros de alimentos con reacción.")
                return@Column
            } else{
                PhdSubtitle("Historial de alimentos con reacción")
                Spacer(modifier = Modifier.height(8.dp))

                ListFoodReactions(
                    foodList = foodList.filter { it.hasReaction },
                    viewModel = viewModel
                )

                Spacer(modifier = Modifier.height(16.dp))

                val context = LocalContext.current

                Button(
                    onClick = { viewModel.generatePdfReport(context) },
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFFADA7D)),
                    shape = RoundedCornerShape(20.dp),
                    modifier = Modifier.align(Alignment.End)
                ) {
                    Text("Reporte")
                }
            }
        }
    }
}

@Composable
fun ListFoodReactions(
    foodList: List<FoodReaction>,
    viewModel: FoodRegistrationViewModel
) {
    var editingFood by remember { mutableStateOf<FoodReaction?>(null) }
    var editedFoodName by remember { mutableStateOf("") }
    var editedReactionDetail by remember { mutableStateOf("") }

    PhdGenericCardList(
        items = foodList,
        onEditClick = { food ->
            editingFood = food
            editedFoodName = food.foodName
            editedReactionDetail = food.reactionDetail
        }
    ) { food ->
        Column {
            PhdBoldText("Alimento:")
            PhdNormalText(food.foodName)
            Spacer(modifier = Modifier.height(8.dp))
            PhdBoldText("Reacción:")
            PhdNormalText(food.reactionDetail.ifEmpty { "Sin detalle" })
        }
    }

    if (editingFood != null) {
        PhdEditItemDialog(
            title = "Editar registro de alimento",
            fields = listOf(
                EditableField("Alimento", editedFoodName) { editedFoodName = it },
                EditableField("Detalle de la reacción", editedReactionDetail) { editedReactionDetail = it }
            ),
            onDismiss = { editingFood = null },
            onSave = {
                val updated = editingFood!!.copy(
                    foodName = editedFoodName,
                    reactionDetail = editedReactionDetail
                )
                viewModel.updateFoodReaction(updated)
                editingFood = null
            }
        )
    }
}
