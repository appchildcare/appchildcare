package com.ys.phdmama.viewmodel

import android.app.PendingIntent
import android.content.Context
import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.google.android.gms.auth.api.identity.BeginSignInRequest
import com.google.android.gms.auth.api.identity.Identity
import com.google.android.gms.auth.api.identity.SignInClient
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.GoogleAuthProvider
import com.ys.phdmama.R
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class LoginViewModel : ViewModel() {

    private val _email = MutableStateFlow("")
    val email: StateFlow<String> = _email.asStateFlow()

    private val _password = MutableStateFlow("")
    val password: StateFlow<String> = _password.asStateFlow()

    private val firebaseAuth: FirebaseAuth = FirebaseAuth.getInstance()

    fun onEmailChange(newEmail: String) {
        _email.value = newEmail
    }

    fun onPasswordChange(newPassword: String) {
        _password.value = newPassword
    }

    fun onSignInWithEmailPassword(
        onSuccess: () -> Unit,
        onError: (String) -> Unit
    ) {
        val email = _email.value
        val password = _password.value

        if (email.isNotBlank() && password.isNotBlank()) {
            viewModelScope.launch {
                firebaseAuth.signInWithEmailAndPassword(email, password)
                    .addOnCompleteListener { task ->
                        if (task.isSuccessful) {
                            onSuccess()
                        } else {
                            onError(task.exception?.message ?: "Error de autenticación")
                        }
                    }
            }
        } else {
            onError("El correo o la contraseña no pueden estar vacíos")
        }
    }

    fun initGoogleSignIn(context: Context, onSuccess: (PendingIntent) -> Unit, onError: (String) -> Unit) {
        val oneTapClient: SignInClient = Identity.getSignInClient(context)

        val signInRequest = BeginSignInRequest.builder()
            .setGoogleIdTokenRequestOptions(
                BeginSignInRequest.GoogleIdTokenRequestOptions.builder()
                    .setSupported(true)
                    .setServerClientId(context.getString(R.string.default_web_client_id))
                    .setFilterByAuthorizedAccounts(false)
                    .build()
            )
            .setAutoSelectEnabled(true)
            .build()

        viewModelScope.launch {
            oneTapClient.beginSignIn(signInRequest)
                .addOnSuccessListener { result ->
                    try {
                        onSuccess(result.pendingIntent)
                    } catch (e: Exception) {
                        onError("Error obteniendo las credenciales: ${e.localizedMessage}")
                    }
                }
                .addOnFailureListener { e ->
                    onError(e.localizedMessage ?: "Error durante el inicio de sesión con Google")
                }
        }
    }

    fun signOut(
        context: Context,
        onSuccess: () -> Unit,
        onError: (String) -> Unit
    ) {
        viewModelScope.launch {
            try {
                // Cerrar sesión de Firebase
                firebaseAuth.signOut()

                // Cerrar sesión de Google Sign-In si fue utilizado
                Identity.getSignInClient(context).signOut().addOnCompleteListener { task ->
                    if (task.isSuccessful) {
                        onSuccess()
                    } else {
                        onError("Error al cerrar sesión con Google")
                    }
                }
            } catch (e: Exception) {
                onError(e.localizedMessage ?: "Error al cerrar sesión")
            }
        }
    }





    fun signInWithGoogle(idToken: String, onSuccess: () -> Unit, onError: (String) -> Unit) {
        val credential = GoogleAuthProvider.getCredential(idToken, null)
        viewModelScope.launch {
            firebaseAuth.signInWithCredential(credential)
                .addOnCompleteListener { task ->
                    if (task.isSuccessful) {
                        onSuccess()
                    } else {
                        onError(task.exception?.message ?: "Error de autenticación con Google")
                    }
                }
        }
    }

    fun checkUserAuthState(): Boolean {
        return firebaseAuth.currentUser != null
    }

    fun signUpWithEmailPassword(
        email: String,
        password: String,
        onSuccess: () -> Unit,
        onError: (String) -> Unit
    ) {
        if (email.isNotBlank() && password.isNotBlank()) {
            viewModelScope.launch {
                firebaseAuth.createUserWithEmailAndPassword(email, password)
                    .addOnCompleteListener { task ->
                        if (task.isSuccessful) {
                            onSuccess()
                        } else {
                            onError(task.exception?.message ?: "Error de registro")
                        }
                    }
            }
        } else {
            onError("El correo o la contraseña no pueden estar vacíos")
        }
    }
}
