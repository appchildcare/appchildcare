package com.ys.phdmama.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore

class WizardViewModelFactory(
    private val firebaseAuth: FirebaseAuth = FirebaseAuth.getInstance(),
    private val firestore: FirebaseFirestore = FirebaseFirestore.getInstance()
) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(WizardViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return WizardViewModel(firebaseAuth, firestore) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}
