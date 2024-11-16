package com.ys.phdmama.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await
import kotlinx.coroutines.withTimeout
import kotlinx.coroutines.withTimeoutOrNull

class BabyStatusViewModel(
    private val firebaseAuth: FirebaseAuth = FirebaseAuth.getInstance(),
    private val firestore: FirebaseFirestore = FirebaseFirestore.getInstance()
) : ViewModel() {
    val isLoadingRoleUpdate = MutableStateFlow(false)


    fun updateUserRole(role: String, onSuccess: () -> Unit, onError: (String) -> Unit) {
        isLoadingRoleUpdate.value = false
        val uid = firebaseAuth.currentUser?.uid
        if (uid != null) {
            isLoadingRoleUpdate.value = true
            val userRef = firestore.collection("users").document(uid)
            userRef.update("role", role)
                .addOnSuccessListener {
                    isLoadingRoleUpdate.value = false
                    onSuccess()
                }
                .addOnFailureListener { e ->
                    isLoadingRoleUpdate.value = false
                    onError(e.localizedMessage ?: "Error al actualizar rol")
                }
        } else {
            onError("UID de usuario no encontrado")
        }
    }

    fun setLoadingRoleUpdate(isLoading: Boolean) {
        isLoadingRoleUpdate.value = isLoading
    }

    fun addBabyToUser(
        babyData: Map<String, Any>,
        onSuccess: () -> Unit,
        onError: (String) -> Unit
    ) {
        val uid = firebaseAuth.currentUser?.uid
        if (uid != null) {
            viewModelScope.launch {
                try {
                    val babyRef = firestore.collection("users").document(uid).collection("babies")
                    babyRef.add(babyData).await()
                    onSuccess()
                } catch (e: Exception) {
                    onError(e.localizedMessage ?: "Error al añadir bebé")
                }
            }
        } else {
            onError("UID de usuario no encontrado")
        }
    }

}

