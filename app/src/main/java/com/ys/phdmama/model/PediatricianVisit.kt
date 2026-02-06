package com.ys.phdmama.model

data class PediatricianVisit(
    val id: String = "",
    val date: String = "",
    val notes: String = "",
    val weight: String = "",
    val height: String = "",
    val headCircumference: String = "",
    val timestamp: String = "",
    val nextVisit: String? = ""
)
