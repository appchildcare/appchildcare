package com.ys.phdmama.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.launch

class BabyStatusViewModel(
    private val firebaseAuth: FirebaseAuth = FirebaseAuth.getInstance(),
    private val firestore: FirebaseFirestore = FirebaseFirestore.getInstance()
) : ViewModel() {

    fun updateUserRole(role: String, onSuccess: () -> Unit, onError: (String) -> Unit) {
        val uid = firebaseAuth.currentUser?.uid
        if (uid != null) {
            viewModelScope.launch {
                val userRef = firestore.collection("users").document(uid)
                userRef.update("role", role)
                    .addOnSuccessListener { onSuccess() }
                    .addOnFailureListener { e -> onError(e.localizedMessage ?: "Error al actualizar rol") }
            }
        } else {
            onError("UID de usuario no encontrado")
        }
    }
}
