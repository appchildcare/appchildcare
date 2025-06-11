package com.ys.phdmama.viewmodel

import android.util.Log
import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.auth.FirebaseAuth
import androidx.compose.runtime.State
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.launch

data class GrowthRecord(
    val ageInMonths: Int = 0,
    val headCircumference: Double = 0.0,
    val height: Double = 0.0,
    val weight: Double = 0.0,
    val timestamp: Long = 0L
)

class GrowthMilestonesViewModel : ViewModel() {
    private val firebaseAuth = FirebaseAuth.getInstance()
    private val userId: String? = firebaseAuth.currentUser?.uid
    private val errorMessage = MutableLiveData<String?>()

    // Simula que este valor se configura al seleccionar un bebé, actualízalo según sea necesario.
    private var currentBabyId: String? = null

    fun setBabyId(babyId: String) {
        currentBabyId = babyId
    }

    private val _growthRecords = mutableStateOf<List<GrowthRecord>>(emptyList())
    val growthRecords: State<List<GrowthRecord>> = _growthRecords

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

    fun fetchGrowthMilestones(
        babyId: String?,
        onResult: (List<GrowthRecord>) -> Unit,
        onError: (Exception) -> Unit
    ) {
        val db = FirebaseFirestore.getInstance()
        val userId = this.userId
        val currentBabyId = babyId ?: currentBabyId

        if (userId != null ) {
            db.collection("users")
                .document(userId)
                .collection("babies")
                .document(currentBabyId.toString())
                .collection("growth_milestones")
                .get()
                .addOnSuccessListener { result ->
                    val growthRecords = result.mapNotNull { doc ->
                        try {
                            GrowthRecord(
                                ageInMonths = doc.getLong("ageInMonths")?.toInt() ?: 0,
                                headCircumference = doc.getString("headCircumference")?.toDoubleOrNull() ?: 0.0,
                                height = doc.getString("height")?.toDoubleOrNull() ?: 0.0,
                                weight = doc.getString("weight")?.toDoubleOrNull() ?: 0.0,
                                timestamp = doc.getLong("timestamp") ?: 0L
                            )
                        } catch (e: Exception) {
                            Log.e("fetchGrowthMilestones error", e.message.toString())
                            null // Puedes hacer logging si deseas
                        }
                    }
                    onResult(growthRecords)
                }
                .addOnFailureListener { exception ->
                    onError(exception)
                }
        }
    }


    fun calculateStandardDeviation(
        records: List<GrowthRecord>,
        selector: (GrowthRecord) -> Double,
        isSample: Boolean = false
    ): Double {
        if (records.isEmpty()) return 0.0

        val values = records.map(selector)
        val mean = values.average()

        val squaredDifferences = values.map { (it - mean) * (it - mean) }
        val divisor = if (isSample) values.size - 1 else values.size

        return kotlin.math.sqrt(squaredDifferences.sum() / divisor)
    }

    fun loadGrowthData(babyId: String?) {
        fetchGrowthMilestones(
            babyId = babyId,
            onResult = { records ->
                _growthRecords.value = records.sortedBy { it.ageInMonths }
            },
            onError = { error ->
                Log.e("GrowthViewModel", "Error loading growth data", error)
            }
        )
    }

    fun fetchBabyId(onSuccess: (List<String>?) -> Unit, onSkip: () -> Unit, onError: (String) -> Unit) {
        val uid = firebaseAuth.currentUser?.uid
        val db = FirebaseFirestore.getInstance()
        var babyIds: MutableList<String> = mutableListOf()

        if (uid != null) {
            viewModelScope.launch {
                try {
                    db.collection("users")
                        .document(uid)
                        .collection("babies")
                        .get()
                        .addOnSuccessListener { result ->
                            if (result.size() > 0) {
                                babyIds = result.map { it.id }.toMutableList()
                                Log.d("GrowViewModel fetchBabyId", babyIds.toString())
//                                onSuccess(babyIds)
                            }
//                            val babyIds = result.map { it.id }
                            Log.d("GrowViewModel not fetchBabyId", "size = 0")
                            onSuccess(babyIds)
                        }
                        .addOnFailureListener { onError(it.toString()) }
//                    Log.d("BabyData ViewModel", babyId)
//                    onSuccess(babyId)
                } catch (e: Exception) {
                    onError(e.localizedMessage ?: "Error al obtener detalles del bebe")
                }
            }
        } else {
            onError("UID de usuario no encontrado")
        }
    }

}