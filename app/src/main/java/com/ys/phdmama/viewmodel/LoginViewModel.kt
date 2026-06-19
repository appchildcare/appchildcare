package com.ys.phdmama.viewmodel

import android.content.Context
import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.navigation.NavController
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInClient
import com.google.android.gms.auth.api.signin.GoogleSignInOptions
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.ys.phdmama.R
import com.ys.phdmama.navigation.NavRoutes
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await
import kotlinx.coroutines.withContext
import javax.inject.Inject

@HiltViewModel
class LoginViewModel @Inject constructor(): ViewModel() {
    private val firebaseAuth: FirebaseAuth = FirebaseAuth.getInstance()
    private val firestore: FirebaseFirestore = FirebaseFirestore.getInstance()

    private val _email = MutableStateFlow("")
    val email: StateFlow<String> = _email.asStateFlow()

    private val _password = MutableStateFlow("")
    val password: StateFlow<String> = _password.asStateFlow()

    private val _displayName = MutableStateFlow("")
    val displayName: StateFlow<String> = _displayName.asStateFlow()

    private val _userRole = MutableStateFlow<String?>(null)
    val userRole: StateFlow<String?> = _userRole.asStateFlow()

    init {
        fetchUserRole()
    }

    fun fetchUserRole() {
        val uid = firebaseAuth.currentUser?.uid ?: return
        viewModelScope.launch {
            _userRole.value = null
            try {
                val document = firestore.collection("users")
                    .document(uid)
                    .get()
                    .await()
                _userRole.value = document.getString("role")
            } catch (e: Exception) {
                _userRole.value = null
            }
        }
    }

    fun logout(navController: NavController, loginViewModel: LoginViewModel,  babyDataViewModel: BabyDataViewModel) {
        FirebaseAuth.getInstance().signOut()
        babyDataViewModel.clearUserData()
        navController.navigate(NavRoutes.LOGIN) {
            popUpTo(0) { inclusive = true } // Clears backstack
        }
    }

    fun onEmailChange(newEmail: String) {
        _email.value = newEmail
    }

    fun onPasswordChange(newPassword: String) {
        _password.value = newPassword
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

    fun onUserLoggedIn(uid: String, email: String, displayName: String, babyDataViewModel: BabyDataViewModel, onComplete: () -> Unit) {
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
        onError: (UiText) -> Unit
    ) {
        val email = _email.value
        val password = _password.value

        if (email.isNotBlank() && password.isNotBlank()) {
            viewModelScope.launch {
                try {
                    val result = firebaseAuth.signInWithEmailAndPassword(email, password).await()
                    if (result.user != null) {
                        onSuccess()
                    } else {
                        onError(UiText.StringResource(R.string.login_error_authentication))
                    }
                } catch (e: Exception) {
                    onError(UiText.StringResource(R.string.login_error_authentication))
                }
            }
        } else {
            val errorValidationText = UiText.StringResource(R.string.login_validation_empty_fields)
            onError(errorValidationText)
        }
    }

    fun UiText.asString(context: Context): String {
        return when (this) {
            is UiText.StringResource -> context.getString(resId)
            is UiText.DynamicString -> value
        }
    }
    fun getGoogleSignInClient(context: Context): GoogleSignInClient {
        val gso = GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
            .requestIdToken(context.getString(R.string.default_web_client_id))
            .requestEmail()
            .build()
        return GoogleSignIn.getClient(context, gso)
    }

    fun checkUserAuthState(): Boolean {
        return firebaseAuth.currentUser != null
    }

    fun getUserUid(onSuccess: (String?) -> Unit, onSkip: () -> Unit, onError: (String) -> Unit) {
        val uid = firebaseAuth.currentUser?.uid
        if (uid != null) {
            viewModelScope.launch {
                try {
                    Log.d("BabyData ViewModel uid", uid)
                    onSuccess(uid)
                } catch (e: Exception) {
                    onError(e.localizedMessage ?: "Error al obtener detalles del bebe")
                }
            }
        } else {
            onError("UID de usuario no encontrado")
        }
    }

    // Función para obtener el usuario desde Firestore en una coroutine
    fun fetchUserDetails(onSuccess: (String?) -> Unit, onSkip: () -> Unit, onError: (String) -> Unit) {
        val uid = firebaseAuth.currentUser?.uid
        if (uid != null) {
            viewModelScope.launch {
                try {
                    val userRef = firestore.collection("users").document(uid)
                    val document = userRef.get().await()
                    val role = document.getString("role")
                    val wizardFinished = document.getBoolean("wizardFinished") ?: false

                    // Si role es nulo o wizardFinished es false, saltamos la validación.
                    if (role == null || !wizardFinished) {
                        onSkip()
                    } else {
                        onSuccess(role)
                    }
                } catch (e: Exception) {
                    onError(e.localizedMessage ?: "Error al obtener detalles del usuario")
                }
            }
        } else {
            onError("UID de usuario no encontrado")
        }
    }

    sealed class UiText {
        data class StringResource(val resId: Int) : UiText()
        data class DynamicString(val value: String) : UiText()
    }
}
