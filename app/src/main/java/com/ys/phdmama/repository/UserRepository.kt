package com.ys.phdmama.repository

import com.google.firebase.firestore.FirebaseFirestore
import com.ys.phdmama.model.User
import com.ys.phdmama.model.UserRoleDTO
import kotlinx.coroutines.tasks.await

class UserRepository(private val firestore: FirebaseFirestore) {

    fun createUser(user: User) {
        firestore.collection("users").document(user.uid)
            .set(user) // Almacena todo el objeto User
            .addOnSuccessListener {
                // Operación exitosa
            }
            .addOnFailureListener {
                // Manejo de error
            }
    }

    suspend fun updateUserRole(userRoleDTO: UserRoleDTO) {
        firestore.collection("users").document(userRoleDTO.uid)
            .update("role", userRoleDTO.role)
            .await() // Espera a que se complete la operación en una coroutine suspendida
    }

    suspend fun getUserRole(uid: String): String? {
        val document = firestore.collection("users").document(uid).get().await()
        return document.getString("role") // Retorna solo el rol del usuario
    }
}
