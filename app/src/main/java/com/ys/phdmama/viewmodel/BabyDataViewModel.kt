package com.ys.phdmama.viewmodel

import android.util.Log
import androidx.compose.runtime.State
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
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

data class Vaccine(
    val id: String? = "",
    val vaccineName: String = "",

)

class BabyDataViewModel (
    private val firebaseAuth: FirebaseAuth = FirebaseAuth.getInstance(),
    private val firestore: FirebaseFirestore = FirebaseFirestore.getInstance()
) : ViewModel() {

    private val _uiEvent = Channel<BabyDataViewModel.UiEvent>()
    val uiEvent = _uiEvent.receiveAsFlow()

    private val _babyAttributes = MutableStateFlow(mapOf<String, String>())
    private val _babyData = MutableStateFlow<BabyProfile?>(null)

    val babyData: StateFlow<BabyProfile?> = _babyData.asStateFlow()

    var calculatedDate by mutableStateOf<String?>(null)
        private set
    var locale = Locale("es", "ES")
        private set

    private val _vaccineAttributes = MutableStateFlow(mapOf<String, String>())
    private val _vaccineData = MutableStateFlow<Vaccine?>(null)
    val vaccineData: StateFlow<Vaccine?> = _vaccineData.asStateFlow()

    private val _babyListAttributes = MutableStateFlow(mapOf<String, String>())
    private val _babyListData = mutableStateOf<List<BabyProfile>>(emptyList())
    val babyListData: State<List<BabyProfile>> = _babyListData

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

    fun setVaccineAttribute(attribute: String, value: String) {
        _vaccineAttributes.value = _vaccineAttributes.value.toMutableMap().apply {
            this[attribute] = value
        }
    }

    fun getVaccineAttribute(attribute: String): String? {
        return _vaccineAttributes.value[attribute]
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
        vaccineData: Map<String, Any>,
        onError: (String) -> Unit
    ){
        val uid = firebaseAuth.currentUser?.uid
        if (uid != null) {
            viewModelScope.launch {
                try {
                    val vaccinesRef = firestore.collection("users").document(uid).collection("vaccines")
                    vaccinesRef.add(vaccineData).await()

                    sendSnackbar("Información agregada correctamente!")

                } catch (e: Exception) {
                    onError(e.localizedMessage ?: "Error al añadir vacuna")
                }
            }
        } else {
            onError("UID de usuario no encontrado")
        }
    }

    fun fetchBabyList() {
        viewModelScope.launch {
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
                    val babies = result.map { document ->
                        document.toObject(BabyProfile::class.java)
                            .copy(id = document.id)
                    }
                    _babyListData.value = babies
                }
                .addOnFailureListener { exception ->
                    Log.e("MotherViewModel", "Error fetching babies: ", exception)
                }
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
