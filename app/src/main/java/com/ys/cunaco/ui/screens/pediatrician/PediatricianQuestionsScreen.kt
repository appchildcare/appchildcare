package com.ys.cunaco.ui.screens.pediatrician

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
import androidx.compose.ui.res.stringResource
import androidx.navigation.NavHostController
import com.ys.cunaco.R
import com.ys.cunaco.ui.components.PhdBoldText
import com.ys.cunaco.ui.components.PhdLayoutMenu
import com.ys.cunaco.model.Question
import com.ys.cunaco.ui.components.EditableField
import com.ys.cunaco.ui.components.PhdGenericCardList
import com.ys.cunaco.ui.components.PhdEditItemDialog
import com.ys.cunaco.ui.components.PhdLabelText
import com.ys.cunaco.ui.components.PhdNormalText
import com.ys.cunaco.ui.components.PhdSubtitle
import com.ys.cunaco.viewmodel.QuestionsAndAnswersViewModel

@Composable
fun PediatricianQuestionsScreen(
    navController: NavHostController,
    openDrawer: () -> Unit,
    viewModel: QuestionsAndAnswersViewModel = viewModel()
) {
    val question = viewModel.savedQuestion

    LaunchedEffect(Unit) {
        viewModel.loadQuestions()
    }

    PhdLayoutMenu(
        title = stringResource(R.string.pediatrician_questions_title),
        navController = navController,
        openDrawer = openDrawer
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                // .background(secondaryCream)
                .padding(16.dp)
        ) {
            Spacer(modifier = Modifier.height(16.dp))

            PhdLabelText(stringResource(R.string.pediatrician_question))

            TextField(
                value = viewModel.questionText,
                onValueChange = { viewModel.questionText = it },
                modifier = Modifier.fillMaxWidth()
            )

            Spacer(modifier = Modifier.height(8.dp))

            Button(
                onClick = {
                    viewModel.saveQuestion()
                },
                enabled = viewModel.questionText.isNotBlank(),
                colors = ButtonDefaults.buttonColors(
                    containerColor = Color(0xFFFADA7D)
                ),
                shape = RoundedCornerShape(20.dp)
            ) {
                Text(stringResource(R.string.pediatrician_save_question))
            }

            if (question != null) {
                Spacer(modifier = Modifier.height(32.dp))

                PhdSubtitle(
                    stringResource(R.string.pediatrician_register_answers)
                )

                Spacer(modifier = Modifier.height(8.dp))

                PhdLabelText(
                    stringResource(R.string.pediatrician_question)
                )

                TextField(
                    value = question.text,
                    onValueChange = {},
                    modifier = Modifier.fillMaxWidth(),
                    readOnly = true
                )

                Spacer(modifier = Modifier.height(8.dp))

                PhdLabelText(
                    stringResource(R.string.pediatrician_answer)
                )

                TextField(
                    value = viewModel.answerText,
                    onValueChange = { viewModel.answerText = it },
                    modifier = Modifier.fillMaxWidth()
                )

                Spacer(modifier = Modifier.height(8.dp))

                Button(
                    onClick = {
                        viewModel.saveAnswer()
                    },
                    enabled = viewModel.answerText.isNotBlank(),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = Color(0xFFFADA7D)
                    ),
                    shape = RoundedCornerShape(20.dp)
                ) {
                    Text(
                        stringResource(R.string.pediatrician_save_answer)
                    )
                }
            }

            ListQuestions(
                questionList = viewModel.questionList,
                viewModel = viewModel
            )
        }
    }
}
@Composable
fun ListQuestions(
    questionList: List<Question>,
    viewModel: QuestionsAndAnswersViewModel
) {
    var editingQuestion by remember {
        mutableStateOf<Question?>(null)
    }

    var editedQuestionText by remember {
        mutableStateOf("")
    }

    var editedAnswerText by remember {
        mutableStateOf("")
    }

    Spacer(modifier = Modifier.height(32.dp))

    PhdSubtitle(
        stringResource(R.string.pediatrician_questions_list)
    )

    Spacer(modifier = Modifier.height(8.dp))

    val noAnswerText = stringResource(R.string.pediatrician_no_answer)


    PhdGenericCardList(
        items = questionList,
        onEditClick = { question ->
            editingQuestion = question
            editedQuestionText = question.text
            editedAnswerText = question.answer ?: noAnswerText
        }
    ) { visit ->
        Column {
            PhdBoldText(
                stringResource(R.string.pediatrician_question_label)
            )

            PhdNormalText(visit.text)

            Spacer(modifier = Modifier.height(8.dp))

            PhdBoldText(
                stringResource(R.string.pediatrician_answer_label)
            )

            PhdNormalText(
                visit.answer
                    ?: stringResource(R.string.pediatrician_no_answer)
            )
        }
    }

    if (editingQuestion != null) {
        PhdEditItemDialog(
            title = stringResource(R.string.pediatrician_edit_title),
            fields = listOf(
                EditableField(
                    stringResource(R.string.pediatrician_question),
                    editedQuestionText
                ) {
                    editedQuestionText = it
                },
                EditableField(
                    stringResource(R.string.pediatrician_answer),
                    editedAnswerText
                ) {
                    editedAnswerText = it
                }
            ),
            onDismiss = {
                editingQuestion = null
            },
            onSave = {
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
