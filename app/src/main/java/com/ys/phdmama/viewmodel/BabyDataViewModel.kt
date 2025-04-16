package com.ys.phdmama.viewmodel

import android.util.Log
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale

data class BabyProfile(
    val id: String? = "",
    val apgar: String = "",
    val bloodType: String = "",
    val height: String = "",
    val name: String = "",
    val perimeter: String = "",
    val sex: String = "",
    val weight: String = "",
//    val birthDate: Date? = null // TODO: REVISAR
)

class BabyDataViewModel (
    private val firebaseAuth: FirebaseAuth = FirebaseAuth.getInstance(),
    private val firestore: FirebaseFirestore = FirebaseFirestore.getInstance()
) : ViewModel() {
    private val _babyAttributes = MutableStateFlow(mapOf<String, String>())
    private val _babyData = MutableStateFlow<BabyProfile?>(null)
    val babyData: StateFlow<BabyProfile?> = _babyData.asStateFlow()
    var calculatedDate by mutableStateOf<String?>(null)
        private set

    var locale = Locale("es", "ES")
        private set

    fun setBabyAttribute(attribute: String, value: String) {
        _babyAttributes.value = _babyAttributes.value.toMutableMap().apply {
            this[attribute] = value
        }
    }

    fun getBabyAttribute(attribute: String): String? {
        return _babyAttributes.value[attribute]
    }

    init {
        fetchBabyProfile()
    }

    fun onDateSelected(date: Date) {
        val calendar = Calendar.getInstance().apply {
            time = date
            add(Calendar.WEEK_OF_YEAR, 40)
        }
        calculatedDate = SimpleDateFormat("yyyy-MM-dd", locale).format(calendar.time)
    }

    fun fetchBabyProfile() {
        viewModelScope.launch {
            // Get the current user's UID
            val currentUserId = firebaseAuth.currentUser?.uid
            if (currentUserId == null) {
                Log.e("FirestoreError", "No user is currently signed in")
                return@launch
            }
            firestore.collection("users")
                .document(currentUserId)
                .collection("babies")
                .get()
                .addOnSuccessListener { result ->
                    for (document in result) {
                        val baby = document.toObject(BabyProfile::class.java)
                           .copy(id = document.id)
                        _babyData.value = baby
                        Log.d("NINO", baby.toString())
                        break // Get only the first baby
                    }
                }
                .addOnFailureListener { exception ->
                    Log.e("BabyViewModel", "Error fetching baby: ", exception)
                }
        }
    }
}
