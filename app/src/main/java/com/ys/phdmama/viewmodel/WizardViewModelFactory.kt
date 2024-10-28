package com.ys.phdmama.viewmodel

import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider

class WizardViewModelFactory(private val context: Context) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(WizardViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return WizardViewModel(context) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}