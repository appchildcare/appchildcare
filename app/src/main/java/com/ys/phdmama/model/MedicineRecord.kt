package com.ys.phdmama.model

data class MedicineRecord(
    val id: String = "",
    val medicineName: String = "",
    val timeToTake: String = "",
    val notificationReminder: String = "No", // "Yes" or "No"
    val reminderDate: String = "",
    val date: String = ""
)
