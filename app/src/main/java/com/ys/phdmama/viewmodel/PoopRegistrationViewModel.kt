package com.ys.phdmama.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.google.firebase.auth.FirebaseAuth
import com.ys.phdmama.datastore.PoopRepository
import com.ys.phdmama.model.PoopColor
import com.ys.phdmama.model.PoopRecord
import com.ys.phdmama.model.PoopSize
import com.ys.phdmama.model.PoopTexture
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class PoopRegistrationViewModel(
    private val repository: PoopRepository = PoopRepository()
) : ViewModel() {
    private val auth = FirebaseAuth.getInstance()

    private val _uiState = MutableStateFlow(PoopRegistrationUiState())
    val uiState: StateFlow<PoopRegistrationUiState> = _uiState.asStateFlow()

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

    fun savePoopRecord(userId: String, babyId: String) {
        val currentState = _uiState.value

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

            repository.savePoopRecord(userId, babyId, poopRecord)
                .onSuccess {
                    _uiState.value = currentState.copy(
                        isLoading = false,
                        isSuccess = true
                    )
                    resetForm()
                }
                .onFailure { exception ->
                    _uiState.value = currentState.copy(
                        isLoading = false,
                        error = "Error al guardar: ${exception.message}"
                    )
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
