package com.ys.phdmama.viewmodel

import android.util.Log
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
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

data class MotherProfile(
    val id: String? = "",
    val motherName: String = "",
    val motherField1: String = "",
//    val birthDate: Date? = null // TODO: REVISAR
)

class MotherProfileViewModel(
    private val firebaseAuth: FirebaseAuth = FirebaseAuth.getInstance(),
    private val firestore: FirebaseFirestore = FirebaseFirestore.getInstance()
) : ViewModel() {

    private val _uiEvent = Channel<UiEvent>()
    val uiEvent = _uiEvent.receiveAsFlow()

    var calculatedDate by mutableStateOf<String?>(null)
        private set

    var locale = Locale("es", "ES")
        private set

    private val _motherAttributes = MutableStateFlow(mapOf<String, String>())
    private val _motherData = MutableStateFlow<MotherProfile?>(null)
    val motherData: StateFlow<MotherProfile?> = _motherData.asStateFlow()

    init {
        fetchMotherProfile()
    }

    fun setMotherAttribute(attribute: String, value: String) {
        _motherAttributes.value = _motherAttributes.value.toMutableMap().apply {
            this[attribute] = value
        }
    }

    fun getMotherAttribute(attribute: String): String? {
        return _motherAttributes.value[attribute]
    }

    fun addMotherProfile(
        motherData: Map<String, Any>,
        onError: (String) -> Unit
    ) {
        val uid = firebaseAuth.currentUser?.uid
        if (uid != null) {
            viewModelScope.launch {
                try {
                    val motherRef = firestore.collection("users").document(uid).collection("family")
                    motherRef.add(motherData).await()

                    sendSnackbar("Información agregada correctamente!")

                } catch (e: Exception) {
                    onError(e.localizedMessage ?: "Error al añadir mamá")
                }
            }
        } else {
            onError("UID de usuario no encontrado")
        }
    }

    fun fetchMotherProfile() {
        viewModelScope.launch {
            // Get the current user's UID
            val currentUserId = firebaseAuth.currentUser?.uid
            if (currentUserId == null) {
                Log.e("FirestoreError", "No user is currently signed in")
                return@launch
            }
            firestore.collection("users")
                .document(currentUserId)
                .collection("family")
                .get()
                .addOnSuccessListener { result ->
                    for (document in result) {
                        val mother = document.toObject(MotherProfile::class.java)
                            .copy(id = document.id)
                        _motherData.value = mother
                        Log.d("NINO", mother.toString())
                        break // Get only the first baby
                    }
                }
                .addOnFailureListener { exception ->
                    Log.e("MotherViewModel", "Error fetching mother: ", exception)
                }
        }
    }

    fun onDateSelected(date: Date) {
        val calendar = Calendar.getInstance().apply {
            time = date
            add(Calendar.WEEK_OF_YEAR, 40)
        }
        calculatedDate = SimpleDateFormat("yyyy-MM-dd", locale).format(calendar.time)
    }


    sealed class UiEvent {
        data class ShowSnackbar(val message: String) : UiEvent()
    }

    fun sendSnackbar(message: String) {
        viewModelScope.launch {
            _uiEvent.send(UiEvent.ShowSnackbar(message))
            delay(100)
        }
    }
}


