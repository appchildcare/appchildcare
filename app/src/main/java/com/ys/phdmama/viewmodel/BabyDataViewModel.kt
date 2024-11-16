package com.ys.phdmama.viewmodel

import androidx.lifecycle.ViewModel
import kotlinx.coroutines.flow.MutableStateFlow

class BabyDataViewModel : ViewModel() {
    private val _babyAttributes = MutableStateFlow(mapOf<String, String>())

    fun setBabyAttribute(attribute: String, value: String) {
        _babyAttributes.value = _babyAttributes.value.toMutableMap().apply {
            this[attribute] = value
        }
    }

    fun getBabyAttribute(attribute: String): String? {
        return _babyAttributes.value[attribute]
    }
}
