package com.ys.phdmama.ui.screens.questionsanswers

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.runtime.Composable
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.sp
import androidx.navigation.NavHostController
import com.ys.phdmama.ui.components.PhdLayoutMenu
import com.ys.phdmama.viewmodel.QuestionsAndAnswersViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun QuestionScreen(navController: NavHostController,
                   openDrawer: () -> Unit,
                   viewModel: QuestionsAndAnswersViewModel = viewModel()) {

    val question = viewModel.savedQuestion

    PhdLayoutMenu(
        title = "Registrar preguntas al pediatra",
        navController = navController,
        openDrawer = openDrawer
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .background(Color(0xFFF1F1F1))
                .padding(16.dp)
        ) {
            Spacer(modifier = Modifier.height(16.dp))

            Text("Pregunta")
            TextField(
                value = viewModel.questionText,
                onValueChange = { viewModel.questionText = it },
                modifier = Modifier.fillMaxWidth(),
                colors = TextFieldDefaults.textFieldColors(containerColor = Color(0xFFD1E9FF))
            )

            Spacer(modifier = Modifier.height(8.dp))

            Button(
                onClick = { viewModel.saveQuestion() },
                enabled = viewModel.questionText.isNotBlank(),
                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFFADA7D)),
                shape = RoundedCornerShape(20.dp)
            ) {
                Text("Guardar pregunta")
            }

            if (question != null) {
                Spacer(modifier = Modifier.height(32.dp))

                Text("Registrar respuestas al pediatra", fontSize = 20.sp, fontWeight = FontWeight.Bold)
                Spacer(modifier = Modifier.height(8.dp))

                Text("Pregunta")
                TextField(
                    value = question.text,
                    onValueChange = {},
                    modifier = Modifier.fillMaxWidth(),
                    readOnly = true,
                    colors = TextFieldDefaults.textFieldColors(containerColor = Color(0xFFD1E9FF))
                )

                Spacer(modifier = Modifier.height(8.dp))

                Text("Respuesta")
                TextField(
                    value = viewModel.answerText,
                    onValueChange = { viewModel.answerText = it },
                    modifier = Modifier.fillMaxWidth(),
                    colors = TextFieldDefaults.textFieldColors(containerColor = Color(0xFFD1E9FF))
                )

                Spacer(modifier = Modifier.height(8.dp))

                Button(
                    onClick = { viewModel.saveAnswer() },
                    enabled = viewModel.answerText.isNotBlank(),
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFFADA7D)),
                    shape = RoundedCornerShape(20.dp)
                ) {
                    Text("Guardar respuesta")
                }
            }
        }
    }
}
