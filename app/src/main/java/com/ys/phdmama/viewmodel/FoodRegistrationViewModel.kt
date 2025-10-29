package com.ys.phdmama.viewmodel

import android.util.Log
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import java.util.UUID

class FoodRegistrationViewModel : ViewModel() {
    private val firestore = FirebaseFirestore.getInstance()
    private val auth = FirebaseAuth.getInstance()

    var foodName by mutableStateOf("")
    var hasReaction by mutableStateOf<Boolean?>(null)
    var reactionDetail by mutableStateOf("")
    var foodList by mutableStateOf<List<FoodReaction>>(emptyList())
        private set

    fun saveFoodReaction() {
        Log.d("FoodRegistrationVM", "saveFoodReaction called - Food: $foodName, HasReaction: $hasReaction")

        if (foodName.isBlank()) {
            Log.e("FoodRegistrationVM", "Food name is blank")
            return
        }
        if (hasReaction == null) {
            Log.e("FoodRegistrationVM", "HasReaction is null")
            return
        }

        val foodId = UUID.randomUUID().toString()
        val formatter = SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault())
        val currentDate = formatter.format(Date())

        val foodReaction = FoodReaction(
            id = foodId,
            foodName = foodName,
            hasReaction = hasReaction!!,
            reactionDetail = if (hasReaction == true) reactionDetail else "",
            date = currentDate
        )

        Log.d("FoodRegistrationVM", "Creating food reaction: $foodReaction")

        val userId = auth.currentUser?.uid
        if (userId == null) {
            Log.e("FoodRegistrationVM", "User ID is null")
            return
        }

        val db = FirebaseFirestore.getInstance()

        db.collection("users").document(userId)
            .collection("food_reactions").document(foodId)
            .set(foodReaction)
            .addOnSuccessListener {
                Log.d("FoodRegistrationVM", "Food reaction saved successfully")
                // Clear form
                foodName = ""
                hasReaction = null
                reactionDetail = ""
            }
            .addOnFailureListener { e ->
                // Log error for debugging
                Log.e("FoodRegistrationVM", "Error saving food reaction", e)
            }
    }

    fun loadFoodReactions() {
        val userId = auth.currentUser?.uid ?: return
        firestore.collection("users").document(userId)
            .collection("food_reactions")
            .orderBy("date", Query.Direction.DESCENDING)
            .addSnapshotListener { snapshot, error ->
                if (error != null) {
                    Log.e("FoodRegistrationVM", "Error loading food reactions", error)
                    return@addSnapshotListener
                }

                if (snapshot != null) {
                    foodList = snapshot.documents.mapNotNull { doc ->
                        doc.toObject(FoodReaction::class.java)?.copy(id = doc.id)
                    }
                    Log.d("FoodRegistrationVM", "Loaded ${foodList.size} food reactions")
                } else {
                    foodList = emptyList()
                }
            }
    }

    fun updateFoodReaction(foodReaction: FoodReaction) {
        val userId = auth.currentUser?.uid ?: return

        firestore.collection("users").document(userId)
            .collection("food_reactions").document(foodReaction.id)
            .set(foodReaction)
            .addOnSuccessListener {
                loadFoodReactions()
            }
    }

    fun getReactionsOnly(): List<FoodReaction> {
        return foodList.filter { it.hasReaction }
    }
}

data class FoodReaction(
    val id: String = "",
    val foodName: String = "",
    val hasReaction: Boolean = false,
    val reactionDetail: String = "",
    val date: String = ""
)
