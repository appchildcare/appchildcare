package com.ys.phdmama.viewmodel

import android.util.Log
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import dagger.hilt.android.lifecycle.HiltViewModel
import java.text.ParseException
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale
import javax.inject.Inject

@HiltViewModel
class RoughDateOfBirthViewModel @Inject constructor(): ViewModel() {
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

    fun convertToDate(dateString: String): Date? {
        if (dateString.isEmpty() || dateString == "Fecha no válida") {
            return null
        }

        return try {
            val locale = Locale("es", "ES")
            // Parse the formatted date string back to Date
            val format = SimpleDateFormat("dd MMMM yyyy", locale)
            format.parse(dateString)
        } catch (e: ParseException) {
            Log.e("RoughDateOfBirthVM", "Error parsing date: ${e.message}")
            null
        }
    }

    fun calculateBirthDateFromWeek(weekNumber: Int): Date {
        val calendar = Calendar.getInstance()
        // Calculate due date: current date + (40 - weekNumber) weeks
        val weeksRemaining = 40 - weekNumber
        calendar.add(Calendar.WEEK_OF_YEAR, weeksRemaining)
        return calendar.time
    }
}
