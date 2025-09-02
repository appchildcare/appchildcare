package com.ys.phdmama.datastore

import android.util.Log
import com.google.firebase.Firebase
import com.google.firebase.auth.FirebaseAuth
import com.ys.phdmama.model.PoopRecord
import com.google.firebase.firestore.Query
import com.google.firebase.firestore.QuerySnapshot
import com.google.firebase.firestore.firestore
import com.google.firebase.firestore.toObject
import kotlinx.coroutines.tasks.await


class PoopRepository {
    private val firestore = Firebase.firestore
    private val auth = FirebaseAuth.getInstance()

    suspend fun savePoopRecord(
        userId: String,
        babyId: String,
        poopRecord: PoopRecord
    ): Result<String> {
        return try {
            val userId = auth.currentUser?.uid ?: return Result.failure(Exception("User not authenticated"))

            Log.d("PoopRepository", "User ID: $userId")

            val documentReference = firestore
                .collection("users")
                .document(userId)
                .collection("babies")
                .document(babyId)
                .collection("poop_records")
                .add(poopRecord)
                .await() // Wait for the operation to complete

            Log.d("PoopRepository", "Poop saved successfully with ID: ${documentReference.id}")
            Result.success(documentReference.id)

        } catch (e: Exception) {
            Log.e("PoopRepository", "Error saving poop", e)
            Result.failure(e)
        }
    }

    suspend fun getPoopRecords(
        userId: String,
        babyId: String
    ): Result<List<PoopRecord>> = try {
        val snapshot: QuerySnapshot = firestore
            .collection("users")
            .document(userId)
            .collection("babies")
            .document(babyId)
            .collection("poop")
            .orderBy("timestamp", Query.Direction.DESCENDING)
            .get()
            .await()

        val records = snapshot.documents.mapNotNull { doc ->
            doc.toObject<PoopRecord>()
        }

        Result.success(records)
    } catch (e: Exception) {
        Result.failure(e)
    }

    suspend fun deletePoopRecord(
        userId: String,
        babyId: String,
        poopId: String
    ): Result<Unit> = try {
        firestore
            .collection("users")
            .document(userId)
            .collection("babies")
            .document(babyId)
            .collection("poop")
            .document(poopId)
            .delete()
            .await()

        Result.success(Unit)
    } catch (e: Exception) {
        Result.failure(e)
    }
}
