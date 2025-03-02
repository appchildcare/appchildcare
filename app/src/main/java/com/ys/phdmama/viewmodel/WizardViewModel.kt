package com.ys.phdmama.viewmodel

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.SetOptions
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await
import java.util.Date

class WizardViewModel(
    private val firebaseAuth: FirebaseAuth = FirebaseAuth.getInstance(),
    private val firestore: FirebaseFirestore = FirebaseFirestore.getInstance()
) : ViewModel() {

    private val _wizardFinished = MutableStateFlow(false)
    val wizardFinished: StateFlow<Boolean> = _wizardFinished

    init {
        checkWizardFinished()
    }

    fun setWizardFinished(finished: Boolean) {
        viewModelScope.launch(Dispatchers.IO) {
            val currentUser = firebaseAuth.currentUser
            currentUser?.let {
                firestore.collection("users")
                    .document(it.uid)
                    .update("wizardFinished", finished)
                    .addOnSuccessListener { _wizardFinished.value = finished }
                    .addOnFailureListener { _wizardFinished.value = false } // Opcional, maneja el error
            }
        }
    }

    fun checkWizardFinished() {
        viewModelScope.launch(Dispatchers.IO) {
            val currentUser = firebaseAuth.currentUser
            currentUser?.let {
                try {
                    val documentSnapshot = firestore.collection("users").document(it.uid).get().await()
                    val isFinished = documentSnapshot.getBoolean("wizardFinished") ?: false
                    _wizardFinished.value = isFinished
                } catch (e: Exception) {
                    _wizardFinished.value = false
                }
            }
        }
    }

    fun savePregnancyTracker(
        birthProximateDate: Date,
        ecoWeeks: Int,
        lastMenstruationDate: Date
    ) {
        viewModelScope.launch(Dispatchers.IO) {
            val currentUser = firebaseAuth.currentUser
            currentUser?.let {
                val pregnancyTracker = hashMapOf(
                    "birthProximateDate" to birthProximateDate,
                    "ecoWeeks" to ecoWeeks,
                    "lastMenstruationDate" to lastMenstruationDate
                )

                firestore.collection("users")
                    .document(it.uid)
                    .set(pregnancyTracker, SetOptions.merge())
                    .addOnSuccessListener {
                        Log.d("SUCCESS SAVED", "pregnancyTracker saved $pregnancyTracker")
                    }
                    .addOnFailureListener { e ->
                        Log.e("ERROR PREGNANCY", "detail $e")
                    }
            }
        }
    }
}