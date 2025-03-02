package com.ys.phdmama.repository

import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import com.ys.phdmama.model.User
import com.ys.phdmama.model.UserRoleDTO
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.tasks.await

class UserRepository() {
    private val firestore = Firebase.firestore
    // Get the current user ID
    val currentUser = FirebaseAuth.getInstance().currentUser


    // Fetch user data by userId
    suspend fun getUser(userId: String): User? {
        val userId = currentUser?.uid.toString()
        return try {
            val document = firestore.collection("users").document(userId).get().await()
            document.toObject(User::class.java)
        } catch (e: Exception) {
            // Handle error (e.g., log or throw)
            null
        }
    }

}
