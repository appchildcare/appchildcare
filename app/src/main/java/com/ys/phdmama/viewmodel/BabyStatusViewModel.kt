package com.ys.phdmama.viewmodel

import android.content.Context
import androidx.datastore.preferences.core.edit
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.flow.MutableStateFlow
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await

class BabyStatusViewModel(
    private val firebaseAuth: FirebaseAuth = FirebaseAuth.getInstance(),
    private val firestore: FirebaseFirestore = FirebaseFirestore.getInstance()
) : ViewModel() {
    val isLoadingRoleUpdate = MutableStateFlow(false)

    // DataStore
    val Context.dataStore by preferencesDataStore(name = "user_prefs")
    private val BABY_UID_KEY = stringPreferencesKey("default_baby_uid")

    private val _uiEvent = Channel<UiEvent>()
    val uiEvent = _uiEvent.receiveAsFlow()

    // Función para guardar el UID del bebé
    suspend fun saveDefaultBabyUid(context: Context, babyUid: String) {
        context.dataStore.edit { preferences ->
            preferences[BABY_UID_KEY] = babyUid
        }
    }

    // Recuperar UID del bebé
    fun getDefaultBabyUid(context: Context): Flow<String?> {
        return context.dataStore.data.map { preferences ->
            preferences[BABY_UID_KEY]
        }
    }


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

