package com.ys.phdmama.viewmodel

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.ys.phdmama.model.PoopColor
import com.ys.phdmama.model.PoopRecord
import com.ys.phdmama.model.PoopSize
import com.ys.phdmama.model.PoopTexture
import com.ys.phdmama.repository.BabyPreferencesRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await
import java.util.Calendar
import javax.inject.Inject

@HiltViewModel
class PoopRegistrationViewModel @Inject constructor(
    private val preferencesRepository: BabyPreferencesRepository
) :
    ViewModel() {
    private val firebaseAuth: FirebaseAuth = FirebaseAuth.getInstance()
    private val firestore: FirebaseFirestore = FirebaseFirestore.getInstance()
    private val _uiState = MutableStateFlow(PoopRegistrationUiState())
    val uiState: StateFlow<PoopRegistrationUiState> = _uiState.asStateFlow()

    private val _selectedBaby = MutableStateFlow<String?>(null)
    val selectedBaby: StateFlow<String?> = _selectedBaby.asStateFlow()

    init {
        observeSelectedBabyFromDataStore()
        setDefaultValues()
    }

    private fun setDefaultValues() {
        val calendar = Calendar.getInstance()
        val currentTime = String.format(
            "%02d:%02d",
            calendar.get(Calendar.HOUR_OF_DAY),
            calendar.get(Calendar.MINUTE)
        )

        _uiState.value = _uiState.value.copy(
            selectedTime = currentTime,
            selectedSize = PoopSize.MONEDA
        )
    }

    private fun observeSelectedBabyFromDataStore() {
        viewModelScope.launch {
            preferencesRepository.selectedBabyIdFlow.collect { savedBabyId ->
                if (savedBabyId != null) {
                    _selectedBaby.value = savedBabyId.toString()
                } else {
                    Log.d("PoopRegistrationViewModel", "Saved baby ID not found in list")
                }
            }
        }
    }

    fun updateTime(time: String) {
        _uiState.value = _uiState.value.copy(selectedTime = time)
    }

    fun updateColor(color: PoopColor) {
        _uiState.value = _uiState.value.copy(selectedColor = color)
    }

    fun updateTexture(texture: PoopTexture) {
        _uiState.value = _uiState.value.copy(selectedTexture = texture)
    }

    fun updateSize(size: PoopSize) {
        _uiState.value = _uiState.value.copy(selectedSize = size)
    }

    fun updateNotes(notes: String) {
        _uiState.value = _uiState.value.copy(notes = notes)
    }

    fun savePoopRecord(
        onSuccess: () -> Unit = {},
        onError: (String) -> Unit
    ) {
        val currentState = _uiState.value
        val selectedBaby = selectedBaby.value

        if (!currentState.isValid()) {
            _uiState.value = currentState.copy(
                error = "Por favor completa todos los campos requeridos"
            )
            return
        }

        _uiState.value = currentState.copy(isLoading = true, error = null)

        viewModelScope.launch {
            val poopRecord = PoopRecord(
                time = currentState.selectedTime,
                color = currentState.selectedColor?.value ?: "",
                texture = currentState.selectedTexture?.value ?: "",
                size = currentState.selectedSize?.value ?: "",
                notes = currentState.notes
            )

            try {
                val userId = firebaseAuth.currentUser?.uid
                val documentReference = firestore
                    .collection("users")
                    .document(userId.toString())
                    .collection("babies")
                    .document(selectedBaby.toString())
                    .collection("poop_records")
                    .add(poopRecord)
                    .await() // Wait for the operation to complete

                Log.d("PoopRepository", "Poop saved successfully with ID: ${documentReference.id}")
                onSuccess()
            } catch (e: Exception) {
                onError(e.localizedMessage ?: "Error al añadir bebé")
            }
        }
    }

    private fun resetForm() {
        _uiState.value = PoopRegistrationUiState()
    }

    fun clearError() {
        _uiState.value = _uiState.value.copy(error = null)
    }

    fun clearSuccess() {
        _uiState.value = _uiState.value.copy(isSuccess = false)
    }
}

data class PoopRegistrationUiState(
    val selectedTime: String = "",
    val selectedColor: PoopColor? = null,
    val selectedTexture: PoopTexture? = null,
    val selectedSize: PoopSize? = null,
    val notes: String = "",
    val isLoading: Boolean = false,
    val error: String? = null,
    val isSuccess: Boolean = false
) {
    fun isValid(): Boolean {
        return selectedTime.isNotEmpty() &&
                selectedColor != null &&
                selectedTexture != null &&
                selectedSize != null
    }
}
