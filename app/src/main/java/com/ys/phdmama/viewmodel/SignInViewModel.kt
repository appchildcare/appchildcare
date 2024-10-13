package com.ys.phdmama.viewmodel

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.google.android.gms.auth.api.signin.GoogleSignInAccount
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.GoogleAuthProvider
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

class SignInViewModel : ViewModel() {

    private val auth = FirebaseAuth.getInstance()

    // Estado del email y password
    private val _email = MutableStateFlow("")
    val email: StateFlow<String> = _email

    private val _password = MutableStateFlow("")
    val password: StateFlow<String> = _password

    // Actualizar email y password
    fun updateEmail(newEmail: String) {
        _email.value = newEmail
    }

    fun updatePassword(newPassword: String) {
        _password.value = newPassword
    }

    // Función para manejar el inicio de sesión con correo y contraseña
    fun onSignInWithEmail(onSuccess: () -> Unit, onError: (String) -> Unit) {
        viewModelScope.launch {
            try {
                auth.signInWithEmailAndPassword(_email.value, _password.value)
                    .addOnCompleteListener { task ->
                        if (task.isSuccessful) {
                            Log.e("[LORENX]", "LORENX exito")
                            onSuccess()
                        } else {
                            Log.e("[LORENX]", "LORENX Error en el inicio de sesión")
                            onError(task.exception?.message ?: "Error en el inicio de sesión")
                        }
                    }
            } catch (e: Exception) {
                Log.e("[LORENX]", "LORENX Error inesperado 1")
                onError(e.message ?: "Error inesperado")
            }
        }
    }

    // Función para manejar el inicio de sesión con Google
    fun onSignInWithGoogle(account: GoogleSignInAccount, onSuccess: () -> Unit, onError: (String) -> Unit) {
        viewModelScope.launch {
            Log.e("[LORENX]", "LORENX")
            try {
                val credential = GoogleAuthProvider.getCredential(account.idToken, null)
                auth.signInWithCredential(credential)
                    .addOnCompleteListener { task ->
                        if (task.isSuccessful) {
                            Log.e("[LORENX]", "LORENX exito")
                            onSuccess()
                        } else {
                            Log.e("[LORENX]", "LORENX Error en Google SignIn")
                            onError(task.exception?.message ?: "Error en Google SignIn")
                        }
                    }
            } catch (e: Exception) {
                Log.e("[LORENX]", "LORENX Error inesperado 2")
                onError(e.message ?: "Error inesperado")
            }
        }
    }
}
