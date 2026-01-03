package com.ys.phdmama.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await
import javax.inject.Inject

@HiltViewModel
class WizardViewModel @Inject constructor(): ViewModel() {
    private val firebaseAuth: FirebaseAuth = FirebaseAuth.getInstance()
    private val firestore: FirebaseFirestore = FirebaseFirestore.getInstance()
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
                    .addOnFailureListener {
                        _wizardFinished.value = false
                    } // Opcional, maneja el error
            }
        }
    }

    fun checkWizardFinished() {
        viewModelScope.launch(Dispatchers.IO) {
            val currentUser = firebaseAuth.currentUser
            currentUser?.let {
                try {
                    val documentSnapshot =
                        firestore.collection("users").document(it.uid).get().await()
                    val isFinished = documentSnapshot.getBoolean("wizardFinished") ?: false
                    _wizardFinished.value = isFinished
                } catch (e: Exception) {
                    _wizardFinished.value = false
                }
            }
        }
    }

}
