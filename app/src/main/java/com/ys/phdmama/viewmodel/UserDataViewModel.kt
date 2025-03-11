package com.ys.phdmama.viewmodel

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FieldPath
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.PropertyName
import com.ys.phdmama.ui.screens.ChecklistItem
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import java.util.Date

data class User(
    @PropertyName("displayName") val displayName: String = "",
    @PropertyName("ecoWeeks") val ecoWeeks: Long = 0,
    @PropertyName("birthProximateDate") val birthProximateDate: Date? = null,
    @PropertyName("role") val role: String = ""
)

class UserDataViewModel : ViewModel() {
    private val firestore = FirebaseFirestore.getInstance()
    private val auth = FirebaseAuth.getInstance()

    private val _currentUser = MutableStateFlow<User?>(null)
    val currentUser: StateFlow<User?> get() = _currentUser

    init {
        Log.d("FirebaseInit", "Firestore instance created")
    }

    fun fetchCurrentUser() {
        viewModelScope.launch {
            // Get the current user's UID
            val currentUserId = auth.currentUser?.uid
            if (currentUserId == null) {
                Log.e("FirestoreError", "No user is currently signed in")
                return@launch
            }
            firestore.collection("users")
                .document(currentUserId)
                .get()
                .addOnSuccessListener { document ->
                    if (document.exists()) {
                        Log.d("FirestoreSuccess", "Fetched current user data")
                        val user = document.toObject(User::class.java)
                        _currentUser.value = user
                    } else {
                        Log.e("FirestoreError", "No user document found for UID: $currentUserId")
                    }
                }
                .addOnFailureListener { exception ->
                    Log.e("FirestoreError", "Error fetching users", exception)
                }
        }
    }

    fun createUserChecklists(userRole: String) {
        val currentUserId = auth.currentUser?.uid
        val db = FirebaseFirestore.getInstance()
        val userRef = currentUserId?.let { db.collection("users").document(it) }

        if (userRef != null) {
            userRef.collection("checklists").get()
                .addOnSuccessListener { documents ->
                    if (documents.isEmpty) { // Check if there are no checklists
                        when (userRole) {
                            "waiting" -> {
                                val waitingChecklist = mapOf(
                                    "1" to ChecklistItem(1, "Empezar a tomar suplemento de ácido fólico", false),
                                    "2" to ChecklistItem(2, "Historial médico completo (personal y familiar)", false),
                                    "3" to ChecklistItem(3, "Limitar el consumo de cafeína", false)
                                )
                                userRef.collection("checklists").document("waiting").set(waitingChecklist)
                                    .addOnSuccessListener { Log.d("Firestore", "Waiting checklist created") }
                                    .addOnFailureListener { e -> Log.e("Checklist", "Error creating checklist", e) }
                            }

                            "born" -> {
                                val bornChecklist = mapOf(
                                    "4" to ChecklistItem(4, "Llevar al bebé a sus vacunas", false),
                                    "5" to ChecklistItem(5, "Recordar darle su medicina de la noche", false),
                                    "6" to ChecklistItem(6, "Ejercicios de estimulación", false)
                                )
                                userRef.collection("checklists").document("born").set(bornChecklist)
                                    .addOnSuccessListener { Log.d("Firestore", "Born checklist created") }
                                    .addOnFailureListener { e -> Log.e("Checklist", "Error creating checklist", e) }
                            }
                        }
                    } else {
                        Log.d("Firestore", "Checklist already exists, skipping creation")
                    }
                }
                .addOnFailureListener { e -> Log.e("Firestore", "Error checking checklists", e) }
        }
    }


    fun fetchWaitingChecklist(onResult: (List<ChecklistItem>) -> Unit) {
        val userId = auth.currentUser?.uid
        val db = FirebaseFirestore.getInstance()
        val docRef = userId?.let {
            db.collection("users").document(it)
                .collection("checklists").document("waiting")
        }

        if (docRef != null) {
            docRef.get()
                .addOnSuccessListener { document ->
                    if (document.exists()) {
                        val checklistItems = mutableListOf<ChecklistItem>()

                        document.data?.forEach { (_, value) ->
                            if (value is Map<*, *>) {
                                val item = ChecklistItem(
                                    id = (value["id"] as? Long)?.toInt() ?: 0,
                                    text = value["text"] as? String ?: "",
                                    checked = when (value["checked"]) {
                                        is Boolean -> value["checked"] as Boolean
                                        is String -> value["checked"] as Boolean
                                        else -> false
                                    }
                                )
                                checklistItems.add(item)
                            }
                        }
                        onResult(checklistItems)
                    } else {
                        println("No such document!")
                    }
                }
                .addOnFailureListener { exception ->
                    println("Error fetching document: $exception")
                }
        }
    }

    fun updateCheckedState(itemId: Int, isChecked: Boolean) {
        val userId = auth.currentUser?.uid ?: return
        val db = FirebaseFirestore.getInstance()
        val docRef = db.collection("users").document(userId)
            .collection("checklists").document("waiting")

        docRef.update(FieldPath.of(itemId.toString(), "checked"), isChecked)
            .addOnSuccessListener { println("Checkbox updated successfully!") }
            .addOnFailureListener { e -> println("Error updating checkbox: $e") }
    }
}
