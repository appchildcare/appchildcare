package com.ys.phdmama.viewmodel

import android.content.Context
import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.navigation.NavController
import androidx.navigation.NavHostController
import com.google.android.gms.auth.api.identity.Identity
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInAccount
import com.google.android.gms.auth.api.signin.GoogleSignInClient
import com.google.android.gms.auth.api.signin.GoogleSignInOptions
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.GoogleAuthProvider
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.FirebaseFirestoreSettings
import com.google.firebase.firestore.Source
import com.ys.phdmama.R
import com.ys.phdmama.navigation.NavRoutes
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await
import kotlinx.coroutines.withContext

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

    fun logout(navController: NavController, loginViewModel: LoginViewModel) {
        FirebaseAuth.getInstance().signOut()
        navController.navigate(NavRoutes.LOGIN) {
            popUpTo(NavRoutes.MAIN) { inclusive = true } // Clears backstack
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
                try {
                    val result = firebaseAuth.signInWithEmailAndPassword(email, password).await()
                    if (result.user != null) {
                        onSuccess()
                    } else {
                        onError("Error de autenticación")
                    }
                } catch (e: Exception) {
                    onError(e.localizedMessage ?: "Error de autenticación")
                }
            }
        } else {
            onError("El correo o la contraseña no pueden estar vacíos")
        }
    }


//    private suspend fun checkUserData(onSuccess: () -> Unit, onError: (String) -> Unit) {
//        try {
//            fetchUserRole { role ->
//                if (role != "unknown") {
//                    onSuccess()
//                } else {
//                    onError("No se pudo obtener el rol del usuario")
//                }
//            }
//        } catch (e: Exception) {
//            onError(e.localizedMessage ?: "Error al obtener los datos del usuario")
//        }
//    }


    fun getUserRole(uid: String, onComplete: (String?) -> Unit) {
        val userRef = firestore.collection("users").document(uid)
        userRef.get().addOnSuccessListener { document ->
            onComplete(document.getString("role"))
        }.addOnFailureListener { e ->
            onComplete(null)
        }
    }

//    fun handleGoogleSignInResult(
//        account: GoogleSignInAccount?,
//        onSuccess: () -> Unit,
//        onError: (String) -> Unit
//    ) {
//        if (account == null) {
//            onError("Cuenta de Google no encontrada")
//            return
//        }
//
//        val credential = GoogleAuthProvider.getCredential(account.idToken, null)
//        viewModelScope.launch {
//            try {
//                firebaseAuth.signInWithCredential(credential).await()
//                val uid = firebaseAuth.currentUser?.uid
//                if (uid != null) {
//                    fetchUserRole { role ->
//                        if (role != "unknown") {
//                            onSuccess()
//                        } else {
//                            onError("No se pudo obtener el rol del usuario")
//                        }
//                    }
//                } else {
//                    onError("No se pudo obtener el UID del usuario")
//                }
//            } catch (e: Exception) {
//                onError(e.localizedMessage ?: "Error de autenticación con Google")
//            }
//        }
//    }



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


    suspend fun getUserRoleFromFirestore(): String {
        return withContext(Dispatchers.IO) {
            val currentUser = firebaseAuth.currentUser
            if (currentUser != null) {
                try {
                    val uid = currentUser.uid
                    val document = firestore.collection("users").document(uid).get().await()
                    document.getString("role") ?: "unknown"
                } catch (e: Exception) {
                    "unknown"
                }
            } else {
                "unknown"
            }
        }
    }


}
