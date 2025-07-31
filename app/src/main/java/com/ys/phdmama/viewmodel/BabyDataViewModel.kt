package com.ys.phdmama.viewmodel

import android.util.Log
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query
import com.ys.phdmama.model.Vaccine
import com.ys.phdmama.viewmodel.BabyDataViewModel.UiEvent.*
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale
import java.util.UUID

data class BabyProfile(
    val id: String? = "",
    val apgar: String = "",
    val bloodType: String = "",
    val height: String = "",
    val name: String = "",
    val perimeter: String = "",
    val sex: String = "",
    val weight: String = "",
//    val birthDate: Date? = null // TODO: REVISAR
)

class BabyDataViewModel (
    private val firebaseAuth: FirebaseAuth = FirebaseAuth.getInstance(),
    private val firestore: FirebaseFirestore = FirebaseFirestore.getInstance()
) : ViewModel() {

    private val _uiEvent = Channel<BabyDataViewModel.UiEvent>()
    val uiEvent = _uiEvent.receiveAsFlow()

    private val _babyAttributes = MutableStateFlow(mapOf<String, String>())
    private val _babyData = MutableStateFlow<BabyProfile?>(null)

    var vaccineText by mutableStateOf("")
    var vaccineDate by mutableStateOf("")

    val babyData: StateFlow<BabyProfile?> = _babyData.asStateFlow()

    var calculatedDate by mutableStateOf<String?>(null)
        private set
    var locale = Locale("es", "ES")
        private set

    var vaccineList by mutableStateOf<List<Vaccine>>(emptyList())
        private set

    private val _babyList = MutableStateFlow<List<BabyProfile>>(emptyList())
    val babyList: StateFlow<List<BabyProfile>> = _babyList

    private val _babyDocumentIds = MutableLiveData<List<String>>()
    val babyDocumentIds: LiveData<List<String>> = _babyDocumentIds

    fun setBabyAttribute(attribute: String, value: String) {
        _babyAttributes.value = _babyAttributes.value.toMutableMap().apply {
            this[attribute] = value
        }
    }

    fun getBabyAttribute(attribute: String): String? {
        return _babyAttributes.value[attribute]
    }

    init {
        fetchBabyProfile()
    }

    fun fetchBabies(userId: String) {
        val db = FirebaseFirestore.getInstance()
        db.collection("users")
            .document(userId)
            .collection("babies")
            .get()
            .addOnSuccessListener { snapshot ->
                val babies = snapshot.documents.mapNotNull { doc ->
                    doc.toObject(BabyProfile::class.java)?.copy(id = doc.id)
                }
                _babyList.value = babies
            }
    }

    fun onDateSelected(date: Date) {
        val calendar = Calendar.getInstance().apply {
            time = date
            add(Calendar.WEEK_OF_YEAR, 40)
        }
        calculatedDate = SimpleDateFormat("yyyy-MM-dd", locale).format(calendar.time)
    }

    private fun fetchBabyProfile() {
        viewModelScope.launch {
            // Get the current user's UID
            val currentUserId = firebaseAuth.currentUser?.uid
            if (currentUserId == null) {
                Log.e("FirestoreError", "No user is currently signed in")
                return@launch
            }
            firestore.collection("users")
                .document(currentUserId)
                .collection("babies")
                .get()
                .addOnSuccessListener { result ->
                    for (document in result) {
                        val baby = document.toObject(BabyProfile::class.java)
                           .copy(id = document.id)
                        _babyData.value = baby
                        Log.d("NINO", baby.toString())
                        break // Get only the first baby
                    }
                }
                .addOnFailureListener { exception ->
                    Log.e("BabyViewModel", "Error fetching baby: ", exception)
                }
        }
    }

    fun addBabyToUser(
        babyData: Map<String, Any>,
        onError: (String) -> Unit
    ) {
        val uid = firebaseAuth.currentUser?.uid
        if (uid != null) {
            viewModelScope.launch {
                try {
                    val babyRef = firestore.collection("users").document(uid).collection("babies")
                    babyRef.add(babyData).await()
                    sendSnackbar("Información agregada correctamente!")
                } catch (e: Exception) {
                    onError(e.localizedMessage ?: "Error al añadir bebé")
                }
            }
        } else {
            onError("UID de usuario no encontrado")
        }
    }

    fun addVaccines(
        onError: (String) -> Unit
    ){
        val uid = firebaseAuth.currentUser?.uid
        if (uid != null) {
            viewModelScope.launch {
                try {
                    val vaccineId = UUID.randomUUID().toString()
                    val formatter = SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault())
                    val currentDate = formatter.format(Date())
                    val vaccineToSave = Vaccine(id = vaccineId, vaccineName = vaccineText, vaccineDate = vaccineDate,  timestamp = currentDate)
                    val vaccinesRef = firestore.collection("users").document(uid).collection("vaccines")
                    vaccinesRef.add(vaccineToSave).await()

                    sendSnackbar("Información agregada correctamente!")

                } catch (e: Exception) {
                    onError(e.localizedMessage ?: "Error al agregar vacuna")
                }
            }
        } else {
            onError("UID de usuario no encontrado")
        }
    }

    fun loadVaccines() {
        val userId = firebaseAuth.currentUser?.uid

        if(userId !=null) {
            firestore.collection("users").document(userId)
                .collection("vaccines")
                .orderBy("timestamp", Query.Direction.DESCENDING)
                .addSnapshotListener { snapshot, _ ->
                    if (snapshot != null && !snapshot.isEmpty) {
                        vaccineList = snapshot.documents.mapNotNull { doc ->
                            doc.toObject(Vaccine::class.java)?.copy(id = doc.id)
                        }
                    }
                }
        }

    }

    fun updateVaccine(vaccine: Vaccine) {
        val userId = firebaseAuth.currentUser?.uid

        if(userId != null) {
            vaccine.id?.let {
                firestore.collection("users").document(userId)
                    .collection("vaccines").document(it)
                    .set(vaccine)
            }
        }
    }

    fun loadBabyIds(onSuccess: (String?) -> Unit, onSkip: () -> Unit, onError: (String) -> Unit) {
        val uid = firebaseAuth.currentUser?.uid
        if (uid != null) {
            viewModelScope.launch {
                try {
                    val userRef = firestore.collection("babies").document(uid)
                    val document = userRef.get().await()
                    val babyId = document.id
                    Log.d("BabyData ViewModel", babyId)
                    onSuccess(babyId)
                } catch (e: Exception) {
                    onError(e.localizedMessage ?: "Error al obtener detalles del bebe")
                }
            }
        } else {
            onError("UID de usuario no encontrado")
        }
    }

    fun fetchBabiesForUser(
        onResult: (List<String>) -> Unit,
        onError: (Exception) -> Unit
    ) {
        val userId = firebaseAuth.currentUser?.uid

        if (userId != null) {
            FirebaseFirestore.getInstance()
                .collection("users")
                .document(userId)
                .collection("babies")
                .get()
                .addOnSuccessListener { result ->
                    val babyIds = result.map { it.id }
                    onResult(babyIds)
                }
                .addOnFailureListener { onError(it) }
        }
    }


    sealed class UiEvent {
        data class ShowSnackbar(val message: String) : UiEvent()
    }

    private fun sendSnackbar(message: String) {
        viewModelScope.launch {
            _uiEvent.send(ShowSnackbar(message))
            delay(100)
        }
    }
}
