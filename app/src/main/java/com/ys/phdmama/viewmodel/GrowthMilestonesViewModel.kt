package com.ys.phdmama.viewmodel

import android.util.Log
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import com.google.firebase.auth.FirebaseAuth

class GrowthMilestonesViewModel : ViewModel() {
    private val firebaseAuth = FirebaseAuth.getInstance()
    private val userId: String? = firebaseAuth.currentUser?.uid
    private val errorMessage = MutableLiveData<String?>()

    // Simula que este valor se configura al seleccionar un bebé, actualízalo según sea necesario.
    private var currentBabyId: String? = null

    private fun setBabyId(babyId: String) {
        currentBabyId = babyId
    }

    private fun fetchBabyId(onSuccess: (String) -> Unit, onError: (String) -> Unit) {
        val userId = this.userId
        if (userId == null) {
            onError("El usuario no está autenticado")
            return
        }

        val db = FirebaseFirestore.getInstance()
        val babiesCollection = db.collection("users").document(userId).collection("babies")

        babiesCollection.limit(1).get()
            .addOnSuccessListener { querySnapshot ->
                if (!querySnapshot.isEmpty) {
                    val babyId = querySnapshot.documents[0].id
                    setBabyId(babyId) // Configura el babyId en el ViewModel
                    Log.d("GrowthMilestone ORIGINAL", "babyId: $babyId")
                    onSuccess(babyId)
                } else {
                    onError("No se encontró ningún bebé registrado")
                }
            }
            .addOnFailureListener { exception ->
                onError(exception.message ?: "Error desconocido al obtener el babyId")
            }
    }

    fun saveGrowthMilestone(
        milestoneData: Map<String, Any>,
        onSuccess: () -> Unit,
        onError: (String) -> Unit
    ) {
        val userId = this.userId

        // Si el babyId no está configurado, intenta obtenerlo
        if (currentBabyId == null) {
            fetchBabyId(
                onSuccess = { babyId ->
                    setBabyId(babyId) // Configura el babyId en el ViewModel
                    saveMilestoneToFirestore(userId, babyId, milestoneData, onSuccess, onError)
                },
                onError = { error ->
                    onError("Error al obtener el babyId: $error")
                }
            )
        } else {
            // Si el babyId ya está configurado, procede a guardar
            saveMilestoneToFirestore(userId, currentBabyId!!, milestoneData, onSuccess, onError)
        }
    }

    // Método para guardar los datos en Firestore
    private fun saveMilestoneToFirestore(
        userId: String?,
        babyId: String,
        milestoneData: Map<String, Any>,
        onSuccess: () -> Unit,
        onError: (String) -> Unit
    ) {
        if (userId == null) {
            onError("El usuario no está autenticado.")
            return
        }

        val db = FirebaseFirestore.getInstance()
        val growthMilestonesRef = db
            .collection("users")
            .document(userId)
            .collection("babies")
            .document(babyId)
            .collection("growth_milestones")

        growthMilestonesRef.add(milestoneData)
            .addOnSuccessListener { onSuccess() }
            .addOnFailureListener { e -> onError(e.message ?: "Error desconocido") }
    }

    fun clearErrorMessage() {
        // Implementa la lógica para limpiar mensajes de error
        errorMessage.value = null // Si usas LiveData o State para manejar errores
    }
}