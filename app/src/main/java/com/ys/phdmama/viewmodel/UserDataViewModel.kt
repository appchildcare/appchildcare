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
                    val checklistNames = documents.documents.map { it.id } // Get existing checklist names

                    when (userRole) {
                        "waiting" -> {
                            if (!checklistNames.contains("waiting")) { // Create "waiting" checklist only if it doesn't exist
                                val waitingChecklist = mapOf(
                                    "1" to ChecklistItem(1, "Documentos personales o copias", false),
                                    "2" to ChecklistItem(2, "Seguros médicos", false),
                                    "3" to ChecklistItem(3, "Permiso de maternidad", false),
                                    "4" to ChecklistItem(4, "Vacaciones acumuladas", false),
                                    "5" to ChecklistItem(5, "Visita al hospital en donde nacerá tu bebé", false),
                                    "6" to ChecklistItem(6, "Consulta prenatal con el pediatra (Si el ginecólogo tiene a alguien en su equipo, pídele que te lo presente antes)", false)
                                )
                                userRef.collection("checklists").document("waiting").set(waitingChecklist)
                                    .addOnSuccessListener { Log.d("Firestore", "Waiting checklist created") }
                                    .addOnFailureListener { e -> Log.e("Checklist", "Error creating waiting checklist", e) }
                            } else {
                                Log.d("Firestore", "Waiting checklist already exists, skipping creation")
                            }
                        }

                        "born" -> {
                            if (!checklistNames.contains("born")) { // Create "born" checklist only if it doesn't exist
                                val bornChecklist = mapOf(
                                    "4" to ChecklistItem(4, "Alcohol en spray (para quien cargue al bebé, prefiere siempre el lavado de manos).", false),
                                    "5" to ChecklistItem(5, "Pañales para recién nacido", false),
                                    "6" to ChecklistItem(6, "Toallitas húmedas para recién nacido, en agua.", false),
                                    "7" to ChecklistItem(7, "Lima de uñas (de preferencia de vidrio).", false),
                                    "8" to ChecklistItem(8, "3 Conjuntos de mangas largas: Botones delanteros.", false),
                                    "9" to ChecklistItem(9, "2 Mantas.", false),
                                    "10" to ChecklistItem(10, "3 gorritos.", false),
                                    "11" to ChecklistItem(11, "Cambiador (protector).", false),
                                    "12" to ChecklistItem(12, "Regalo para herman@ o sobrin@.", false)
                                )
                                userRef.collection("checklists").document("born").set(bornChecklist)
                                    .addOnSuccessListener { Log.d("Firestore", "Born checklist created") }
                                    .addOnFailureListener { e -> Log.e("Checklist", "Error creating born checklist", e) }
                            } else {
                                Log.d("Firestore", "Born checklist already exists, skipping creation")
                            }
                        }
                    }
                }
                .addOnFailureListener { e ->
                    Log.e("Firestore", "Error checking existing checklists", e)
                }
        }
    }

    fun fetchWaitingChecklist(userRole: String, onResult: (List<ChecklistItem>) -> Unit) {
        val checklistType = if (userRole == "waiting")  "waiting" else "born"
        val userId = auth.currentUser?.uid
        val db = FirebaseFirestore.getInstance()
        val docRef = userId?.let {
            db.collection("users").document(it)
                .collection("checklists").document(checklistType)
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

    fun updateCheckedState(itemId: Int, isChecked: Boolean, userRole: String) {
        val userId = auth.currentUser?.uid ?: return
        val db = FirebaseFirestore.getInstance()
        val docRef = db.collection("users").document(userId)
            .collection("checklists").document(userRole)

        docRef.update(FieldPath.of(itemId.toString(), "checked"), isChecked)
            .addOnSuccessListener { println("Checkbox updated successfully!") }
            .addOnFailureListener { e -> println("Error updating checkbox: $e") }
    }

    fun updateUserRole(newRole: String, onSuccess: () -> Unit, onFailure: (Exception) -> Unit) {
        val userId = auth.currentUser?.uid ?: return
        FirebaseFirestore.getInstance().collection("users")
            .document(userId)
            .update("role", newRole)
            .addOnSuccessListener {
                onSuccess()
            }
            .addOnFailureListener { exception ->
                onFailure(exception)
            }
    }
}
