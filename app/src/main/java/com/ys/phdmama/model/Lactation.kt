package com.ys.phdmama.model

// Lactation Data Classes
data class LactationSession(
    val startHourFraction: Float, // e.g. 14.5 means 14:30
    val durationMinutes: Int,
    val side: String, // "left", "right", or "both"
    val amount: Float? = null // Optional amount in ml for bottle feeding
)

data class DayLactationEntry(
    val dayName: String,
    val sessions: List<LactationSession>
)

data class LactationRecord(
    val id: String = "",
    val time: String = "", // Duration like "00:15"
    val timestamp: String = "", // Firebase timestamp string
    val side: String = "left", // "left", "right", or "both"
    val amount: String = "", // Amount in ml (optional)
    val type: String = "breastfeeding" // "breastfeeding" or "bottle"
)