package com.ys.phdmama.viewmodel

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query
import com.ys.phdmama.model.Question
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import java.util.UUID

class QuestionsAndAnswersViewModel : ViewModel() {
    private val firestore = FirebaseFirestore.getInstance()
    private val auth = FirebaseAuth.getInstance()

    var questionText by mutableStateOf("")
    var answerText by mutableStateOf("")
    var savedQuestion by mutableStateOf<Question?>(null)
    var questionList by mutableStateOf<List<Question>>(emptyList())
        private set

    fun saveQuestion() {
        val questionId = UUID.randomUUID().toString()
        val formatter = SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault())
        val currentDate = formatter.format(Date())
        val question = Question(id = questionId, text = questionText,  date = currentDate)

        val userId = auth.currentUser?.uid ?: return
        val db = FirebaseFirestore.getInstance()

        db.collection("users").document(userId)
            .collection("pediatrician_questions").document(questionId)
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
                .collection("pediatrician_questions").document(question.id)
                .update("answer", answerText)
                .addOnSuccessListener {
                    savedQuestion = question.copy(answer = answerText)
                    answerText = ""
                }
        }
    }

    fun loadQuestions() {
        val userId = auth.currentUser?.uid ?: return
        firestore.collection("users").document(userId)
            .collection("pediatrician_questions")
            .orderBy("date", Query.Direction.DESCENDING)
            .addSnapshotListener { snapshot, _ ->
                if (snapshot != null && !snapshot.isEmpty) {
                    questionList = snapshot.documents.mapNotNull { doc ->
                        doc.toObject(Question::class.java)?.copy(id = doc.id)
                    }
                }
            }
    }

    fun updateQuestion(question: Question) {
        val userId = auth.currentUser?.uid ?: return

        firestore.collection("users").document(userId)
            .collection("pediatrician_questions").document(question.id)
            .set(question)
    }

}
