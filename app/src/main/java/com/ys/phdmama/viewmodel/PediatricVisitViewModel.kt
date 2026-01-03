package com.ys.phdmama.viewmodel

import android.util.Log
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.ys.phdmama.model.PediatricianVisit
import dagger.hilt.android.lifecycle.HiltViewModel
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import java.util.UUID
import javax.inject.Inject

@HiltViewModel
class PediatricVisitViewModel @Inject constructor(): ViewModel() {
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
            // .orderBy("timestamp", Query.Direction.DESCENDING)
            .addSnapshotListener { snapshot, exception ->

                // Handle Firestore exceptions
                if (exception != null) {
                    Log.e("PediatricianVisits", "Error loading visits: ${exception.message}", exception)
                    // Initialize empty list on error
                    visitDataList = emptyList()
                    return@addSnapshotListener
                }

                // Handle null snapshot
                if (snapshot == null) {
                    Log.d("PediatricianVisits", "Snapshot is null")
                    visitDataList = emptyList()
                    return@addSnapshotListener
                }

                // Handle empty collection (collection doesn't exist or has no documents)
                if (snapshot.isEmpty) {
                    Log.d("PediatricianVisits", "Collection is empty or doesn't exist")
                    visitDataList = emptyList()
                    return@addSnapshotListener
                }

                // Safe mapping with additional null checks
                try {
                    visitDataList = snapshot.documents.mapNotNull { doc ->
                        try {
                            doc.toObject(PediatricianVisit::class.java)?.copy(id = doc.id)
                        } catch (e: Exception) {
                            Log.e("PediatricianVisits", "Error parsing document ${doc.id}: ${e.message}")
                            null // Skip this document if parsing fails
                        }
                    }
                    Log.d("PediatricianVisits", "Loaded ${visitDataList.size} visits")
                } catch (e: Exception) {
                    Log.e("PediatricianVisits", "Error processing snapshot: ${e.message}", e)
                    visitDataList = emptyList()
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
