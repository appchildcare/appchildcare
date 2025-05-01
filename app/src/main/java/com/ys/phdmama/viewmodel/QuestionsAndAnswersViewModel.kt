package com.ys.phdmama.viewmodel

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import java.util.UUID

data class Question(
    val id: String = "",
    val text: String = "",
    val answer: String? = null,
    val type: String = ""
)

class QuestionsAndAnswersViewModel : ViewModel() {
    private val firestore = FirebaseFirestore.getInstance()
    private val auth = FirebaseAuth.getInstance()

    var questionText by mutableStateOf("")
    var answerText by mutableStateOf("")
    var savedQuestion by mutableStateOf<Question?>(null)

    fun saveQuestion() {
        val questionId = UUID.randomUUID().toString()
        val question = Question(id = questionId, text = questionText, type = "pediatrician")

        val userId = auth.currentUser?.uid ?: return
        val db = FirebaseFirestore.getInstance()

        db.collection("users").document(userId)
            .collection("questions").document(questionId)
            .set(question)
            .addOnSuccessListener {
                savedQuestion = question
                questionText = ""
            }
    }

    fun saveAnswer() {
        val userId = auth.currentUser?.uid ?: return
        val db = FirebaseFirestore.getInstance()

        savedQuestion?.let { question ->
            db.collection("users").document(userId)
                .collection("questions").document(question.id)
                .update("answer", answerText)
                .addOnSuccessListener {
                    savedQuestion = question.copy(answer = answerText)
                    answerText = ""
                }
        }
    }
}
