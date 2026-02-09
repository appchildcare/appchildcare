package com.ys.phdmama.model

data class LMSHeadCircumference(
    val week: Int,
    val sex: String, // puedes usar "girl" o "boy"
    val L: Double,
    val M: Double,
    val S: Double,
    val SD: Double,
    val SD3neg: Double,
    val SD2neg: Double,
    val SD1neg: Double,
    val SD0: Double,
    val SD1: Double,
    val SD2: Double,
    val SD3: Double
)

data class LMSHeightLength(
    val month: Int,
    val sex: String,
    val L: Double,
    val M: Double,
    val S: Double,
)

data class LMSHeightWeight(
    val week: Int,
    val sex: String,
    val L: Double,
    val M: Double,
    val S: Double
)

data class LMS(
    val week: Int,
    val L: Double,
    val M: Double,
    val S: Double,
    val SD3neg: Double,
    val SD2neg: Double,
    val SD1neg: Double,
    val SD0: Double,
    val SD1: Double,
    val SD2: Double,
    val SD3: Double
)

data class LengthRange(val min: Double, val max: Double)