package com.ys.phdmama.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.ktx.userProfileChangeRequest
import com.ys.phdmama.model.UserRegistrationData
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class RegisterViewModel @Inject constructor(): ViewModel() {
    private val firebaseAuth: FirebaseAuth = FirebaseAuth.getInstance()
    val userRegistrationData = UserRegistrationData()

    fun updateUserField(field: String, value: String) {
        when (field) {
            "email" -> userRegistrationData.email = value
            "password" -> userRegistrationData.password = value
            "repeatPassword" -> userRegistrationData.repeatPassword = value
            "displayName" -> userRegistrationData.displayName = value
        }
    }

    fun signUpWithEmailPassword(
        onSuccess: () -> Unit,
        onError: (String) -> Unit,
    ) {
        if (userRegistrationData.password == userRegistrationData.repeatPassword) {
            viewModelScope.launch {
                firebaseAuth.createUserWithEmailAndPassword(
                    userRegistrationData.email, userRegistrationData.password
                ).addOnCompleteListener { task ->
                    if (task.isSuccessful) {
                        val user = firebaseAuth.currentUser
                        user?.let {
                            val profileUpdates = userProfileChangeRequest {
                                displayName = userRegistrationData.displayName
                            }
                            it.updateProfile(profileUpdates).addOnCompleteListener { profileUpdateTask ->
                                if (profileUpdateTask.isSuccessful) {
                                    onSuccess()
                                } else {
                                    onError(profileUpdateTask.exception?.message ?: "Error al actualizar el perfil")
                                }
                            }
                        } ?: onError("No se pudo obtener el usuario")
                    } else {
                        onError(task.exception?.message ?: "Error de registro")
                    }
                }
            }
        } else {
            onError("Las contraseñas no coinciden")
        }
    }
}
