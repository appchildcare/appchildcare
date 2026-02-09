package com.ys.phdmama.ui.screens.pediatrician

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.runtime.Composable
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.navigation.NavHostController
import com.ys.phdmama.ui.components.PhdBoldText
import com.ys.phdmama.ui.components.PhdLayoutMenu
import com.ys.phdmama.model.Question
import com.ys.phdmama.ui.components.EditableField
import com.ys.phdmama.ui.components.PhdGenericCardList
import com.ys.phdmama.ui.components.PhdEditItemDialog
import com.ys.phdmama.ui.components.PhdLabelText
import com.ys.phdmama.ui.components.PhdNormalText
import com.ys.phdmama.ui.components.PhdSubtitle
import com.ys.phdmama.ui.theme.secondaryCream
import com.ys.phdmama.viewmodel.QuestionsAndAnswersViewModel

@Composable
fun PediatricianQuestionsScreen(navController: NavHostController,
                                openDrawer: () -> Unit,
                                viewModel: QuestionsAndAnswersViewModel = viewModel()) {

    val question = viewModel.savedQuestion

    LaunchedEffect(Unit) {
        viewModel.loadQuestions()
    }

    PhdLayoutMenu(
        title = "Preguntas al pediatra",
        navController = navController,
        openDrawer = openDrawer
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
//                .background(secondaryCream)
                .padding(16.dp)
        ) {
            Spacer(modifier = Modifier.height(16.dp))

            PhdLabelText("Pregunta")
            TextField(
                value = viewModel.questionText,
                onValueChange = { viewModel.questionText = it },
                modifier = Modifier.fillMaxWidth(),
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

                PhdSubtitle("Registrar respuestas al pediatra")
                Spacer(modifier = Modifier.height(8.dp))

                PhdLabelText("Pregunta")
                TextField(
                    value = question.text,
                    onValueChange = {},
                    modifier = Modifier.fillMaxWidth(),
                    readOnly = true,
                )

                Spacer(modifier = Modifier.height(8.dp))

                PhdLabelText("Respuesta")
                TextField(
                    value = viewModel.answerText,
                    onValueChange = { viewModel.answerText = it },
                    modifier = Modifier.fillMaxWidth(),
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

    Spacer(modifier = Modifier.height(32.dp))
    PhdSubtitle("Lista de preguntas y respuestas")
    Spacer(modifier = Modifier.height(8.dp))

    PhdGenericCardList(
        items = questionList,
        onEditClick = { question ->
            editingQuestion = question
            editedQuestionText = question.text
            editedAnswerText = question.answer ?: "Sin respuesta"
        }
    ) { visit ->
        Column {
            PhdBoldText("Pregunta:")
            PhdNormalText(visit.text)
            Spacer(modifier = Modifier.height(8.dp))
            PhdBoldText("Respuesta:")
            PhdNormalText(visit.answer ?: "Sin respuesta")
        }
    }

    if (editingQuestion != null) {
        PhdEditItemDialog(
            title = "Editar pregunta y respuesta",
            fields = listOf(
                EditableField("Pregunta", editedQuestionText) { editedQuestionText = it },
                EditableField("Respuesta", editedAnswerText) { editedAnswerText = it },
            ),
            onDismiss = { editingQuestion = null },
            onSave = {
                // Save logic
                val updated = editingQuestion!!.copy(
                    text = editedQuestionText,
                    answer = editedAnswerText
                )
                viewModel.updateQuestion(updated)
                editingQuestion = null
            }
        )
    }
}
