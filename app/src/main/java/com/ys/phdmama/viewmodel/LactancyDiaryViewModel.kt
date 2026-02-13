package com.ys.phdmama.viewmodel

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query
import com.ys.phdmama.model.Lactation
import com.ys.phdmama.model.LactationEntry
import com.ys.phdmama.model.LactationWeekDay
import com.ys.phdmama.repository.BabyPreferencesRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale
import javax.inject.Inject

@HiltViewModel
class LactancyDiaryViewModel @Inject constructor(
    private val preferencesRepository: BabyPreferencesRepository
): ViewModel() {
    private val auth = FirebaseAuth.getInstance()
    private val _lactationEntries = MutableStateFlow<List<LactationEntry>>(emptyList())
    val lactationEntries: StateFlow<List<LactationEntry>> = _lactationEntries

    private val _weekDays = MutableStateFlow<List<LactationWeekDay>>(emptyList())
    val weekDays: StateFlow<List<LactationWeekDay>> = _weekDays.asStateFlow()

    private val _selectedDate = MutableStateFlow(Calendar.getInstance())

    private val spanishLocale = Locale("es", "ES")
    private val dateFormatter = SimpleDateFormat("dd/MM/yyyy", spanishLocale)
    private val dayNameFormatter = SimpleDateFormat("EEEE dd", spanishLocale)

    private val _selectedBaby = MutableStateFlow<String?>(null)
    val selectedBaby: StateFlow<String?> = _selectedBaby.asStateFlow()


    init {
        observeSelectedBabyFromDataStore()
        generateCurrentWeek()
    }

    private fun observeSelectedBabyFromDataStore() {
        viewModelScope.launch {
            preferencesRepository.selectedBabyIdFlow.collect { savedBabyId ->
                if (savedBabyId != null) {
                    _selectedBaby.value = savedBabyId.toString()
                } else {
                    Log.d("LactancyDiaryViewModel", "Saved baby ID not found in list")
                }
            }
        }
    }


    private fun generateCurrentWeek() {
        val calendar = Calendar.getInstance()
        val today = Calendar.getInstance()

        // Set to Monday of current week
        calendar.set(Calendar.DAY_OF_WEEK, Calendar.MONDAY)

        val weekDaysList = mutableListOf<LactationWeekDay>()

        repeat(7) { dayIndex ->
            val dayName = dayNameFormatter.format(calendar.time)
                .replaceFirstChar { it.uppercase() }

            val isToday = calendar.get(Calendar.DAY_OF_YEAR) == today.get(Calendar.DAY_OF_YEAR) &&
                    calendar.get(Calendar.YEAR) == today.get(Calendar.YEAR)

            weekDaysList.add(
                LactationWeekDay(
                    name = dayName,
                    isSelected = isToday,
                    lactationCount = 0 // Will be updated when data is fetched
                )
            )

            if (isToday) {
                _selectedDate.value = calendar.clone() as Calendar
            }

            calendar.add(Calendar.DAY_OF_MONTH, 1)
        }

        _weekDays.value = weekDaysList
    }

    fun fetchLactancyData(babyId: String?) {
        val db = FirebaseFirestore.getInstance()
        val userId = auth.currentUser?.uid ?: return

        if (babyId != null) {
            val selectedBaby = selectedBaby.value

            db.collection("users")
                .document(userId)
                .collection("babies")
                .document(selectedBaby.toString())
                .collection("lactation_counter_time")
                .orderBy("timestamp", Query.Direction.DESCENDING)
                .get()
                .addOnSuccessListener { result ->
                    val groupedByDay = result.documents.groupBy {
                        val ts = it.getTimestamp("timestamp")?.toDate()
                        SimpleDateFormat("EEEE dd, yyyy", Locale("es")).format(ts ?: Date())
                    }

                    val dayEntries = groupedByDay.map { (day, docs) ->
                        LactationEntry(
                            dayName = day,
                            items = docs.map { doc ->
                                val ts = doc.getTimestamp("timestamp")?.toDate()
                                val timeParts = doc.getString("time")?.split(":") ?: listOf("0", "0")
                                val durationSec = timeParts[0].toInt() * 60 + timeParts[1].toInt()
                                val startHour = ts?.let {
                                    val cal = Calendar.getInstance().apply { time = it }
                                    cal.get(Calendar.HOUR_OF_DAY) + cal.get(Calendar.MINUTE) / 60f
                                } ?: 0f

                                Lactation(
                                    startHourFraction = startHour,
                                    durationHours = durationSec / 60f,
                                    lactancyType = doc.getString("lactancy_type")
                                )
                            }
                        )
                    }
                    updateWeekDaysWithData(dayEntries)
                    _lactationEntries.value = dayEntries
                }
        }
    }

    private fun updateWeekDaysWithData(entries: List<LactationEntry>) {
        val updatedWeekDays = _weekDays.value.map { weekDay ->
            // Find matching entry for this day
            val matchingEntry = entries.find { entry ->
                // Remove year from entry name for comparison
                val entryDisplayName = entry.dayName
                    .replace(Regex(",?\\s*\\d{4}"), "")
                    .trim()
                    .lowercase()

                val weekDayName = weekDay.name.lowercase()

                // Match by day name and day number
                entryDisplayName == weekDayName
            }

            // Update the count if we found matching data
            weekDay.copy(
                lactationCount = matchingEntry?.items?.size ?: 0
            )
        }

        _weekDays.value = updatedWeekDays
    }
}
