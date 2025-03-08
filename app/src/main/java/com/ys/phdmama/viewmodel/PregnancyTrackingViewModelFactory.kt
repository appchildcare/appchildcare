package com.ys.phdmama.viewmodel

import PregnancyTrackerViewModel
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore

class PregnancyTrackingViewModelFactory(
    private val firebaseAuth: FirebaseAuth = FirebaseAuth.getInstance(),
    private val firestore: FirebaseFirestore = FirebaseFirestore.getInstance()
) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(PregnancyTrackerViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return PregnancyTrackerViewModel(firebaseAuth, firestore) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}
