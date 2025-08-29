package com.ys.phdmama.datastore

import com.google.firebase.Firebase
import com.ys.phdmama.model.PoopRecord
import com.google.firebase.firestore.Query
import com.google.firebase.firestore.QuerySnapshot
import com.google.firebase.firestore.firestore
import com.google.firebase.firestore.toObject
import kotlinx.coroutines.tasks.await


class PoopRepository {
    private val firestore = Firebase.firestore

    suspend fun savePoopRecord(
        userId: String,
        babyId: String,
        poopRecord: PoopRecord
    ): Result<String> = try {
        val documentId = firestore
            .collection("users")
            .document(userId)
            .collection("babies")
            .document(babyId)
            .collection("poop")
            .document().id

        val poopWithId = poopRecord.copy(id = documentId)

        firestore
            .collection("users")
            .document(userId)
            .collection("babies")
            .document(babyId)
            .collection("poop")
            .document(documentId)
            .set(poopWithId)
            .await()

        Result.success(documentId)
    } catch (e: Exception) {
        Result.failure(e)
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
