package com.ys.cunaco.viewmodel

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import com.ys.cunaco.R
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject
import kotlin.math.ceil

@HiltViewModel
class CarbonFootprintViewModel @Inject constructor() : ViewModel() {
    // Carbon footprint constants (kg CO₂e)
    private val C_DISPOSABLE_DIAPER = 0.4
    private val C_CLOTH_DIAPER_WASH = 0.2
    private val C_WET_WIPE = 0.01
    private val C_FORMULA_FEEDING = 0.3
    private val C_BOTTLE_WASH = 0.05
    private val C_BATH = 0.15

    // UI State
    var disposableDiapers by mutableStateOf("")
    var clothDiapers by mutableStateOf("")
    var wetWipes by mutableStateOf("")
    var formulaFeedings by mutableStateOf("")
    var bottleWashes by mutableStateOf("")
    var baths by mutableStateOf("")

    var isLoading by mutableStateOf(false)
    var errorMessage by mutableStateOf("")
    
    // Result data for UI
    var showResult by mutableStateOf(false)
    var totalFootprint by mutableStateOf(0.0)
    var savings by mutableStateOf(0.0)
    var clothDiapersCount by mutableStateOf(0)
    var clothWashesCount by mutableStateOf(0)
    var monthlySavingsValue by mutableStateOf(0.0)
    var yearlySavingsValue by mutableStateOf(0.0)
    var analogyResId by mutableStateOf(0)
    var ecoMessageResId by mutableStateOf(0)

    // Calculate cloth diaper washes needed
    private fun calculateClothDiaperWashes(clothDiapers: Int): Int {
        return ceil(clothDiapers / 3.0).toInt()
    }

    // Calculate total carbon footprint
    private fun calculateCarbonFootprint(
        disposableDiapers: Int,
        clothDiapers: Int,
        wetWipes: Int,
        formulaFeedings: Int,
        bottleWashes: Int,
        baths: Int
    ): Double {
        val clothWashes = calculateClothDiaperWashes(clothDiapers)

        return (disposableDiapers * C_DISPOSABLE_DIAPER) +
                (clothWashes * C_CLOTH_DIAPER_WASH) +
                (wetWipes * C_WET_WIPE) +
                (formulaFeedings * C_FORMULA_FEEDING) +
                (bottleWashes * C_BOTTLE_WASH) +
                (baths * C_BATH)
    }

    private fun getCo2AnalogyResId(valueKg: Double): Int {
        return when {
            valueKg < 100 -> R.string.co2_analogy_low
            valueKg in 100.0..400.0 -> R.string.co2_analogy_mid_low
            valueKg in 400.0..800.0 -> R.string.co2_analogy_mid
            valueKg in 800.0..1000.0 -> R.string.co2_analogy_mid_high
            valueKg in 1000.0..4000.0 -> R.string.co2_analogy_high
            else -> R.string.co2_analogy_very_high
        }
    }

    // Calculate carbon footprint
    fun calculateFootprint() {
        try {
            isLoading = true
            errorMessage = ""

            val disposable = disposableDiapers.toIntOrNull() ?: 0
            val cloth = clothDiapers.toIntOrNull() ?: 0
            val wipes = wetWipes.toIntOrNull() ?: 0
            val formula = formulaFeedings.toIntOrNull() ?: 0
            val bottles = bottleWashes.toIntOrNull() ?: 0
            val bathsCount = baths.toIntOrNull() ?: 0

            val total = calculateCarbonFootprint(
                disposable, cloth, wipes, formula, bottles, bathsCount
            )
            val clothWashes = calculateClothDiaperWashes(cloth)
            
            val disposableImpact = disposable * C_DISPOSABLE_DIAPER
            val clothImpact = clothWashes * C_CLOTH_DIAPER_WASH
            val dailySavings = disposableImpact - clothImpact

            this.totalFootprint = total
            this.savings = dailySavings
            this.clothDiapersCount = cloth
            this.clothWashesCount = clothWashes
            this.monthlySavingsValue = dailySavings * 30
            this.yearlySavingsValue = dailySavings * 365
            this.analogyResId = getCo2AnalogyResId(this.yearlySavingsValue)
            
            this.ecoMessageResId = when {
                cloth > disposable -> R.string.eco_msg_excellent
                cloth > 0 -> R.string.eco_msg_progress
                else -> R.string.eco_msg_consider
            }
            
            showResult = true

        } catch (e: Exception) {
            errorMessage = e.message ?: "Unknown error"
        } finally {
            isLoading = false
        }
    }

    // Clear form and reset calculation
    fun clearForm() {
        disposableDiapers = ""
        clothDiapers = ""
        wetWipes = ""
        formulaFeedings = ""
        bottleWashes = ""
        baths = ""
        showResult = false
        errorMessage = ""
    }
}
