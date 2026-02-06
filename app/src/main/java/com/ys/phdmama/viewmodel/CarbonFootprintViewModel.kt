package com.ys.phdmama.viewmodel

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import com.ys.phdmama.model.CarbonFootprintReport
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
    var successMessage by mutableStateOf("")

    var currentReport by mutableStateOf<CarbonFootprintReport?>(null)

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

    // Generate eco-friendly message
    private fun generateEcoMessage(
        disposableDiapers: Int,
        clothDiapers: Int,
        clothWashes: Int
    ): String {
        val disposableImpact = disposableDiapers * C_DISPOSABLE_DIAPER
        val clothImpact = clothWashes * C_CLOTH_DIAPER_WASH
        val savings = disposableImpact - clothImpact

        val disposableImpactDaily = disposableDiapers * C_DISPOSABLE_DIAPER
        val clothImpactDaily = clothWashes * C_CLOTH_DIAPER_WASH
        val dailySavings = disposableImpactDaily - clothImpactDaily

        val monthlySavings = formatProjection(dailySavings, 30)
        val yearlySavings = formatProjection(dailySavings, 365)
        val yearlyAnalogy = co2Analogy(yearlySavings.toDouble())

        return when {
            clothDiapers > disposableDiapers -> {
                "🌱 ¡Excelente opción! Usar pañales de tela ahorra aproximadamente ${String.format("%.2f", savings)} kg CO₂e. " +
                        "Cada pañal de tela se puede reutilizar cientos de veces, mientras que los pañales desechables tardan más de 500 años en descomponerse. " +
                        "¡Tu lavado ecológico con ${clothDiapers} pañales de tela (${clothWashes} lavados eficientes) está haciendo una verdadera diferencia!\n" +
                        "  \uD83D\uDCC6 Si mantienes este hábito puedes reducir la huella de carbono en:\n" +
                        "• En 30 días: $monthlySavings kg CO₂e\n" +
                        "• En 1 año: $yearlySavings kg CO₂e\n" +
                        "  👉 $yearlyAnalogy\n"
            }
            clothDiapers > 0 -> {
                "🌿 ¡Gran progreso! Tu ${clothDiapers} Los pañales de tela ayudan a reducir las emisiones de carbono. " +
                        "Considere utilizar más pañales de tela para aumentar su impacto ambiental. " +
                        "Con un lavado eficiente (${clothWashes} cargas), ¡Ya estás salvando el planeta!\n" +
                        "  \uD83D\uDCC6 Si mantienes este hábito, puedes reducir la huella de carbono en:\n" +
                        "• En 30 días: $monthlySavings kg CO₂e\n" +
                        "• En 1 año: $yearlySavings kg CO₂e\n" +
                        "  👉 $yearlyAnalogy\n"
            }
            else -> {
                "💚 ¡Considera cambiar a pañales de tela! Pueden reducir significativamente tu huella de carbono. " +
                        "Solo 8 pañales de tela con un ciclo de lavado ecológico pueden reemplazar muchos pañales desechables " +
                        "y ayudar a proteger nuestro medio ambiente para las generaciones futuras.\n" +
                        "  \uD83D\uDCC6 Si mantienes este hábito puedes reducir la huella de carbono en:\n" +
                        "• En 30 días: $monthlySavings kg CO₂e\n" +
                        "• En 1 año: $yearlySavings kg CO₂e\n" +
                        "  👉 $yearlyAnalogy\n"
            }
        }
    }

    private fun formatProjection(valuePerDay: Double, days: Int): String {
        return String.format("%.2f", valuePerDay * days)
    }

    // Calculate carbon footprint without saving
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

            val totalFootprint = calculateCarbonFootprint(
                disposable, cloth, wipes, formula, bottles, bathsCount
            )
            val clothWashes = calculateClothDiaperWashes(cloth)

            // Generate report and get eco message
            val ecoMessage = generateReport(
                disposable, cloth, wipes, formula, bottles, bathsCount,
                totalFootprint, clothWashes
            )

            successMessage = "Huella de carbono calculada: ${String.format("%.2f", totalFootprint)} kg CO₂e\n\n$ecoMessage"

        } catch (e: Exception) {
            errorMessage = "Error calculating footprint: ${e.message}"
        } finally {
            isLoading = false
        }
    }

    // Generate detailed report
    private fun generateReport(
        disposableDiapers: Int,
        clothDiapers: Int,
        wetWipes: Int,
        formulaFeedings: Int,
        bottleWashes: Int,
        baths: Int,
        totalFootprint: Double,
        clothWashes: Int
    ): String {
        val disposableFootprint = disposableDiapers * C_DISPOSABLE_DIAPER
        val clothFootprint = clothWashes * C_CLOTH_DIAPER_WASH
        val wipesFootprint = wetWipes * C_WET_WIPE
        val formulaFootprint = formulaFeedings * C_FORMULA_FEEDING
        val bottleFootprint = bottleWashes * C_BOTTLE_WASH
        val bathFootprint = baths * C_BATH

        val ecoMessage = generateEcoMessage(
            disposableDiapers,
            clothDiapers,
            clothWashes
        )

        currentReport = CarbonFootprintReport(
            period = "Diario",
            totalFootprint = totalFootprint,
            disposableDiapersFootprint = disposableFootprint,
            clothDiapersFootprint = clothFootprint,
            wetWipesFootprint = wipesFootprint,
            formulaFootprint = formulaFootprint,
            bottleWashFootprint = bottleFootprint,
            bathsFootprint = bathFootprint,
            ecoMessage = ecoMessage
        )

        return ecoMessage
    }

    private fun co2Analogy(valueKg: Double): String {
        return when {
            valueKg < 100 -> {
                "Equivale a apagar luces y electrodomésticos innecesarios durante varias semanas 💡"
            }
            valueKg in 100.0..400.0 -> {
                "Equivale a un auto encendido sin moverse durante 10 a 40 horas 🚗"
            }
            valueKg in 400.0..800.0 -> {
                "Equivale a conducir un auto durante 1 a 2 meses 🚘"
            }
            valueKg in 800.0..1000.0 -> {
                "Equivale a un vuelo internacional corto ✈️"
            }
            valueKg in 1000.0..4000.0 -> {
                "Equivale a uno o dos vuelos internacionales o un año de uso de un auto 🚗✈️"
            }
            else -> {
                "Equivale a la huella de carbono anual completa de una persona promedio 🌍"
            }
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
        currentReport = null
        successMessage = ""
        errorMessage = ""
    }
}
