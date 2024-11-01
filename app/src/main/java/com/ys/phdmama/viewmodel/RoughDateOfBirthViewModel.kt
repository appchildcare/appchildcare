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

    fun onDateSelected(date: Date) {
        val calendar = Calendar.getInstance().apply {
            time = date
            add(Calendar.WEEK_OF_YEAR, 40)
        }
        calculatedDate = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(calendar.time)
    }
}
