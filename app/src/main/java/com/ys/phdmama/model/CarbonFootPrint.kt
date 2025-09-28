package com.ys.phdmama.model

data class CarbonFootprintData(
    var id: String = "",
    val date: String = "",
    val disposableDiapers: Int = 0,
    val clothDiapers: Int = 0,
    val wetWipes: Int = 0,
    val formulaFeedings: Int = 0,
    val bottleWashes: Int = 0,
    val baths: Int = 0,
    val totalCarbonFootprint: Double = 0.0,
    val clothDiaperWashes: Int = 0
)

data class CarbonFootprintReport(
    val period: String,
    val totalFootprint: Double,
    val disposableDiapersFootprint: Double,
    val clothDiapersFootprint: Double,
    val wetWipesFootprint: Double,
    val formulaFootprint: Double,
    val bottleWashFootprint: Double,
    val bathsFootprint: Double,
    val ecoMessage: String
)
