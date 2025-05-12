package com.ys.phdmama.ui.screens.questionsanswers

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.runtime.Composable
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TextField
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.sp
import androidx.navigation.NavHostController
import com.ys.phdmama.ui.components.PhdBoldText
import com.ys.phdmama.ui.components.PhdLayoutMenu
import com.ys.phdmama.model.Question
import com.ys.phdmama.viewmodel.QuestionsAndAnswersViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun QuestionScreen(navController: NavHostController,
                   openDrawer: () -> Unit,
                   viewModel: QuestionsAndAnswersViewModel = viewModel()) {

    val question = viewModel.savedQuestion

    LaunchedEffect(Unit) {
        viewModel.loadQuestions()
    }

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
            ListQuestions(questionList = viewModel.questionList, viewModel = viewModel)
        }
    }
}

@Composable
fun ListQuestions(questionList: List<Question>, viewModel: QuestionsAndAnswersViewModel) {
    var editingQuestion by remember { mutableStateOf<Question?>(null) }
    var editedQuestionText by remember { mutableStateOf("") }
    var editedAnswerText by remember { mutableStateOf("") }

    Column {
        Spacer(modifier = Modifier.height(32.dp))
        Text("Lista de preguntas y respuestas", fontSize = 20.sp, fontWeight = FontWeight.Bold)
        Spacer(modifier = Modifier.height(8.dp))

        LazyColumn {
            items(questionList) { question ->
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 4.dp),
                    elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
                    colors = CardDefaults.cardColors(containerColor = Color(0xFFDDE1F5))
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Column {
                                PhdBoldText("Pregunta:")
                                Text(question.text)
                            }
                            IconButton(onClick = {
                                editingQuestion = question
                                editedQuestionText = question.text
                                editedAnswerText = question.answer ?: ""
                            }) {
                                Icon(imageVector = Icons.Default.Edit, contentDescription = "Editar")
                            }
                        }
                        Spacer(modifier = Modifier.height(8.dp))
                        PhdBoldText("Respuesta:")
                        Text(question.answer ?: "Sin respuesta")
                        PhdBoldText("Fecha:")
                        Text(question.date)
                    }
                }
            }
        }

        // Diálogo de edición
        if (editingQuestion != null) {
            AlertDialog(
                onDismissRequest = { editingQuestion = null },
                title = { Text("Editar pregunta y respuesta") },
                text = {
                    Column {
                        OutlinedTextField(
                            value = editedQuestionText,
                            onValueChange = { editedQuestionText = it },
                            label = { Text("Pregunta") },
                            modifier = Modifier.fillMaxWidth()
                        )
                        OutlinedTextField(
                            value = editedAnswerText,
                            onValueChange = { editedAnswerText = it },
                            label = { Text("Respuesta") },
                            modifier = Modifier.fillMaxWidth()
                        )
                    }
                },
                confirmButton = {
                    TextButton(onClick = {
                        val updated = editingQuestion!!.copy(
                            text = editedQuestionText,
                            answer = editedAnswerText
                        )
                        viewModel.updateQuestion(updated)
                        editingQuestion = null
                    }) {
                        Text("Guardar")
                    }
                },
                dismissButton = {
                    TextButton(onClick = { editingQuestion = null }) {
                        Text("Cancelar")
                    }
                }
            )
        }
    }
}
