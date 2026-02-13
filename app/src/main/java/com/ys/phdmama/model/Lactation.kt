package com.ys.phdmama.model

// Lactation Data Classes
data class LactationSession(
    val startHourFraction: Float, // e.g. 14.5 means 14:30
    val durationMinutes: Int,
    val side: String, // "left", "right", or "both"
    val amount: Float? = null // Optional amount in ml for bottle feeding
)

data class LactationEntry(
    val dayName: String,           // e.g., "Lunes 25", "Martes 19"
    val items: List<Lactation>,           // List of lactation sessions for that day
    val date: String? = null       // Optional: actual date in a parseable format
)

data class Lactation(
    val startHourFraction: Float,  // e.g., 14.5 for 2:30 PM
    val durationHours: Float,      // Duration in minutes (despite the name)
    val lactancyType: String? = null, // "natural" or "formula"
    val breast: String? = null     // "left", "right", or "both" (if applicable)
)

data class LactationWeekDay(
    val name: String,
    val isSelected: Boolean = false,
    val lactationCount: Int = 0
)
