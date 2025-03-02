package com.ys.phdmama.viewmodel

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale

class RoughDateOfBirthViewModel : ViewModel() {
    var calculatedDate by mutableStateOf<String?>(null)
        private set

    var calculatedWeekDate by mutableStateOf<String?>(null)
        private set

    var locale = Locale("es", "ES")
        private set

    fun onDateSelected(date: Date) {
        val calendar = Calendar.getInstance().apply {
            time = date
            add(Calendar.WEEK_OF_YEAR, 40)
        }
        calculatedDate = SimpleDateFormat("yyyy-MM-dd", locale).format(calendar.time)
    }

    fun onBirthDateByWeeks(weeks: Int) {
        val gestationWeeks = 40
        val remainingWeeks = gestationWeeks - weeks // Semanas faltantes para completar la gestación

        val calendar = Calendar.getInstance()
        calendar.add(Calendar.WEEK_OF_YEAR, remainingWeeks)

        calculatedWeekDate = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(calendar.time)
    }

    fun convertToDate(dateString: String): Date {
        val formatter = SimpleDateFormat("dd MMMM yyyy", Locale.ENGLISH) // Define the format
        return formatter.parse(dateString) as Date // Parse the string to a Date object
    }
}
