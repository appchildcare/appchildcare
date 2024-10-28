package com.ys.phdmama.viewmodel

import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

class WizardViewModel(context: Context) : ViewModel() {
    private val sharedPreferences = context.getSharedPreferences("user_prefs", Context.MODE_PRIVATE)

    private val _wizardFinished = MutableStateFlow(false)
    val wizardFinished: StateFlow<Boolean> = _wizardFinished

    init {
        viewModelScope.launch(Dispatchers.IO) {
            _wizardFinished.value = sharedPreferences.getBoolean("wizardFinished", false)
        }
    }

    fun setWizardFinished(finished: Boolean) {
        viewModelScope.launch(Dispatchers.IO) {
            sharedPreferences.edit().putBoolean("wizardFinished", finished).apply()
            _wizardFinished.value = finished
        }
    }

    fun checkWizardFinished() {
        viewModelScope.launch(Dispatchers.IO) {
            _wizardFinished.value = sharedPreferences.getBoolean("wizardFinished", false)
        }
    }
}