package com.ys.phdmama.viewmodel

import androidx.lifecycle.ViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

class BabyDataViewModel : ViewModel() {
    private val _babyName = MutableStateFlow("")
    val babyName: StateFlow<String> = _babyName.asStateFlow()

    private val _babyAPGAR = MutableStateFlow("")
//    val babyAPGAR: StateFlow<String> = _babyAPGAR
    val babyAPGAR: StateFlow<String> = _babyAPGAR.asStateFlow()

    private val _babyHeight = MutableStateFlow("")
    val babyHeight: StateFlow<String> = _babyHeight

    private val _babyWeight = MutableStateFlow("")
//    val babyWeight: StateFlow<String> = _babyWeight
    val babyWeight: StateFlow<String> = _babyWeight.asStateFlow()

    private val _babyCefalicPerimeter = MutableStateFlow("")
//    val babyCefalicPerimeter: StateFlow<String> = _babyCefalicPerimeter
    val babyCefalicPerimeter: StateFlow<String> = _babyCefalicPerimeter.asStateFlow()

    private val _babyBloodType = MutableStateFlow("")
//    val babyBloodType: StateFlow<String> = _babyBloodType
    val babyBloodType: StateFlow<String> = _babyBloodType.asStateFlow()

    private val _babySex = MutableStateFlow("")
//    val babySex: StateFlow<String> = _babySex
    val babySex: StateFlow<String> = _babySex.asStateFlow()

    fun setBabyName(name: String) {
        _babyName.value = name
    }

    fun updateBabyName(name: String) {
        _babyName.value = name
    }

    fun setBabyAPGAR(apgar: String) {
        _babyAPGAR.value = apgar
    }

    fun updateBabyAPGAR(apgar: String) {
        _babyAPGAR.value = apgar
    }

    fun setBabyHeight(height: String) {
        _babyHeight.value = height
    }

    fun updateBabyHeight(height: String) {
        _babyHeight.value = height
    }

    fun setBabyWeight(weight: String) {
        _babyWeight.value = weight
    }

    fun updateBabyWeight(weight: String) {
        _babyWeight.value = weight
    }

    fun setBabyCefalicPerimeter(perimeter: String) {
        _babyCefalicPerimeter.value = perimeter
    }

    fun updateBabyCefalicPerimeter(perimeter: String) {
        _babyCefalicPerimeter.value = perimeter
    }

    fun setBabyBloodType(bloodType: String) {
        _babyBloodType.value = bloodType
    }

    fun updateBabyBloodType(bloodType: String) {
        _babyBloodType.value = bloodType
    }

    fun setBabySex(sex: String) {
        _babySex.value = sex
    }

    fun updateBabySex(sex: String) {
        _babySex.value = sex
    }
}