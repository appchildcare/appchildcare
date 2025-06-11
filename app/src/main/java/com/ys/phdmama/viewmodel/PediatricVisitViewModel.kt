package com.ys.phdmama.viewmodel

import android.util.Log
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query
import com.ys.phdmama.model.PediatricianVisit
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import java.util.UUID


class PediatricVisitViewModel : ViewModel() {
    private val firestore = FirebaseFirestore.getInstance()
    private val auth = FirebaseAuth.getInstance()
    var visitDataList by mutableStateOf<List<PediatricianVisit>>(emptyList())
        private set

    fun saveVisit(
        date: String,
        notes: String,
        weight: String,
        height: String,
        headCircumference: String,
        onSuccess: () -> Unit,
        onError: (String) -> Unit
    ) {
        val visitId = UUID.randomUUID().toString()
        val formatter = SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault())
        val currentDate = formatter.format(Date())

        val pediatricianVisit = PediatricianVisit(id = visitId,
            date = date,
            notes = notes,
            weight = weight,
            height = height,
            headCircumference = headCircumference,
            timestamp = currentDate
        )

        val userId = auth.currentUser?.uid ?: return
        val db = FirebaseFirestore.getInstance()
        db.collection("users").document(userId)
            .collection("pediatrician_visit_questions")
            .add(pediatricianVisit)
            .addOnSuccessListener { onSuccess() }
            .addOnFailureListener { e -> onError(e.message ?: "Error desconocido") }
    }

    fun loadPediatricianVisits() {
        val userId = auth.currentUser?.uid ?: return
        firestore.collection("users").document(userId)
            .collection("pediatrician_visit_questions")
            .orderBy("timestamp", Query.Direction.DESCENDING)
            .addSnapshotListener { snapshot, _ ->
                Log.d("snapshot NINO", snapshot?.documents.toString())
                if (snapshot != null && !snapshot.isEmpty) {
                    visitDataList = snapshot.documents.mapNotNull { doc ->
                        doc.toObject(PediatricianVisit::class.java)?.copy(id = doc.id)
                    }
                }

            }
    }

    fun update(pediatrician: PediatricianVisit) {
        val userId = auth.currentUser?.uid ?: return

        firestore.collection("users").document(userId)
            .collection("pediatrician_visit_questions").document(pediatrician.id)
            .set(pediatrician)
    }
}
