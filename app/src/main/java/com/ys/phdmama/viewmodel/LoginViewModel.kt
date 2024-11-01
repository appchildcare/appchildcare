package com.ys.phdmama.viewmodel

import android.app.PendingIntent
import android.content.Context
import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.google.android.gms.auth.api.identity.BeginSignInRequest
import com.google.android.gms.auth.api.identity.Identity
import com.google.android.gms.auth.api.identity.SignInClient
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInAccount
import com.google.android.gms.auth.api.signin.GoogleSignInClient
import com.google.android.gms.auth.api.signin.GoogleSignInOptions
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.GoogleAuthProvider
import com.google.firebase.firestore.FirebaseFirestore
import com.ys.phdmama.R
import com.ys.phdmama.model.User
import com.ys.phdmama.model.UserRoleDTO
import com.ys.phdmama.repository.UserRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class LoginViewModel(
    private val firebaseAuth: FirebaseAuth = FirebaseAuth.getInstance(),
    private val firestore: FirebaseFirestore = FirebaseFirestore.getInstance()
) : ViewModel() {

    private val _email = MutableStateFlow("")
    val email: StateFlow<String> = _email.asStateFlow()

    private val _password = MutableStateFlow("")
    val password: StateFlow<String> = _password.asStateFlow()

    private val _displayName = MutableStateFlow("")
    val displayName: StateFlow<String> = _displayName.asStateFlow()

    fun onEmailChange(newEmail: String) {
        _email.value = newEmail
    }

    fun onPasswordChange(newPassword: String) {
        _password.value = newPassword
    }

    fun onDisplayNameChange(newDisplayName: String) {
        _displayName.value = newDisplayName
    }

    fun getCurrentUserUid(): String? {
        return firebaseAuth.currentUser?.uid
    }

    fun getCurrentUserEmail(): String? {
        return firebaseAuth.currentUser?.email
    }

    fun getCurrentUserDisplayName(): String? {
        return firebaseAuth.currentUser?.displayName
    }


    fun onUserLoggedIn(uid: String, email: String, displayName: String, onComplete: () -> Unit) {
        val user = hashMapOf(
            "uid" to uid,
            "email" to email,
            "displayName" to displayName
        )

        firestore.collection("users").document(uid).set(user)
            .addOnSuccessListener {
                Log.d("LoginViewModel", "Usuario creado o actualizado en Firestore")
                onComplete() // Llama al callback de finalización
            }
            .addOnFailureListener { e ->
                Log.e("LoginViewModel", "Error al crear o actualizar el usuario en Firestore", e)
            }
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

    fun handleGoogleSignInResult(
        account: GoogleSignInAccount?,
        onSuccess: () -> Unit,
        onError: (String) -> Unit
    ) {
        if (account == null) {
            onError("Cuenta de Google no encontrada")
            return
        }

        val credential = GoogleAuthProvider.getCredential(account.idToken, null)
        viewModelScope.launch {
            firebaseAuth.signInWithCredential(credential)
                .addOnCompleteListener { task ->
                    if (task.isSuccessful) {
                        // Asegúrate de obtener el uid después de una autenticación exitosa
                        val uid = firebaseAuth.currentUser?.uid
                        if (uid != null) {
                            onUserLoggedIn(uid, account.email ?: "", account.displayName ?: "") {
                                onSuccess()
                            }
                        } else {
                            onError("No se pudo obtener el UID del usuario después de iniciar sesión con Google")
                        }
                    } else {
                        onError(task.exception?.message ?: "Error de autenticación con Google")
                    }
                }
        }
    }

    fun getGoogleSignInClient(context: Context): GoogleSignInClient {
        val gso = GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
            .requestIdToken(context.getString(R.string.default_web_client_id))
            .requestEmail()
            .build()
        return GoogleSignIn.getClient(context, gso)
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

    fun checkUserAuthState(): Boolean {
        return firebaseAuth.currentUser != null
    }
}
