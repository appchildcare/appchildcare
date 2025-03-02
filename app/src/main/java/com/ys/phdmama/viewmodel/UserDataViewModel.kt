package com.ys.phdmama.viewmodel

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.PropertyName
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import java.util.Date

data class User(
//    @PropertyName("id") val id: String = "",
    @PropertyName("displayName") val displayName: String = "",
    @PropertyName("ecoWeeks") val ecoWeeks: Long = 0,
    @PropertyName("birthProximateDate") val birthProximateDate: Date? = null
)

class UserDataViewModel : ViewModel() {
    private val firestore = FirebaseFirestore.getInstance()
    private val auth = FirebaseAuth.getInstance()

    private val _currentUser = MutableStateFlow<User?>(null)
    val currentUser: StateFlow<User?> get() = _currentUser

    init {
        Log.d("FirebaseInit", "Firestore instance created")
    }

    fun fetchCurrentUser() {
        viewModelScope.launch {
            // Get the current user's UID
            val currentUserId = auth.currentUser?.uid
            if (currentUserId == null) {
                Log.e("FirestoreError", "No user is currently signed in")
                return@launch
            }
            firestore.collection("users")
                .document(currentUserId)
                .get()
                .addOnSuccessListener { document ->
                    if (document.exists()) {
                        Log.d("FirestoreSuccess", "Fetched current user data")
                        val user = document.toObject(User::class.java)
                        _currentUser.value = user
                    } else {
                        Log.e("FirestoreError", "No user document found for UID: $currentUserId")
                    }
                }
                .addOnFailureListener { exception ->
                    Log.e("FirestoreError", "Error fetching users", exception)
                }
        }
    }
}