package com.ys.phdmama.viewmodel

import DayNapEntry
import Nap
import SleepWeekDay
import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query
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

//class SleepReportViewModel {
//}


@HiltViewModel
class SleepReportViewModel @Inject constructor(
    private val preferencesRepository: BabyPreferencesRepository
) : ViewModel() {
    private val firestore = FirebaseFirestore.getInstance()
    private val auth = FirebaseAuth.getInstance()

    private val _napEntries = MutableStateFlow<List<DayNapEntry>>(emptyList())
    val napEntries: StateFlow<List<DayNapEntry>> = _napEntries

    private val _weekDays = MutableStateFlow<List<SleepWeekDay>>(emptyList())
    val weekDays: StateFlow<List<SleepWeekDay>> = _weekDays

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
                    Log.d("FoodRegistrationVM", "Saved baby ID not found in list")
                }
            }
        }
    }

    private fun generateCurrentWeek() {
        val calendar = Calendar.getInstance()
        val today = Calendar.getInstance()

        // Set to Monday of current week
        calendar.set(Calendar.DAY_OF_WEEK, Calendar.MONDAY)

        val weekDaysList = mutableListOf<SleepWeekDay>()

        repeat(7) { dayIndex ->
            val dayName = SimpleDateFormat("EEEE dd", Locale("es")).format(calendar.time)
                .replaceFirstChar { it.uppercase() }

            val isToday = calendar.get(Calendar.DAY_OF_YEAR) == today.get(Calendar.DAY_OF_YEAR) &&
                    calendar.get(Calendar.YEAR) == today.get(Calendar.YEAR)

            weekDaysList.add(
                SleepWeekDay(
                    name = dayName,
                    isSelected = isToday,
                    sleepPercentage = 0f,
                    napCount = 0
                )
            )

            calendar.add(Calendar.DAY_OF_MONTH, 1)
        }

        _weekDays.value = weekDaysList
    }

    fun fetchNapData(babyId: String) {
        val db = FirebaseFirestore.getInstance()
        val userId = auth.currentUser?.uid ?: return
        val selectedBaby =  _selectedBaby.value

        db.collection("users")
            .document(userId)
            .collection("babies")
            .document(selectedBaby.toString())
            .collection("nap_counter_time") // or your collection name
            .orderBy("timestamp", Query.Direction.DESCENDING)
            .get()
            .addOnSuccessListener { result ->
                val groupedByDay = result.documents.groupBy {
                    val ts = it.getTimestamp("timestamp")?.toDate()
                    SimpleDateFormat("EEEE dd, yyyy", Locale("es")).format(ts ?: Date())
                }

                val dayEntries = groupedByDay.map { (day, docs) ->
                    DayNapEntry(
                        dayName = day,
                        naps = docs.map { doc ->
                            val ts = doc.getTimestamp("timestamp")?.toDate()
                            val timeParts = doc.getString("time")?.split(":") ?: listOf("0", "0")
                            val durationMinutes = timeParts[0].toInt() * 60 + timeParts[1].toInt()
                            val durationHours = durationMinutes / 60f

                            val startHour = ts?.let {
                                val cal = Calendar.getInstance().apply { time = it }
                                cal.get(Calendar.HOUR_OF_DAY) + cal.get(Calendar.MINUTE) / 60f
                            } ?: 0f

                            Nap(
                                startHourFraction = startHour,
                                durationHours = durationHours
                            )
                        }
                    )
                }

                _napEntries.value = dayEntries
                updateWeekDaysWithData(dayEntries)
            }
    }

    private fun updateWeekDaysWithData(entries: List<DayNapEntry>) {
        Log.d("SleepDebug", "Updating week days with ${entries.size} day entries")

        val updatedWeekDays = _weekDays.value.map { weekDay ->
            // Find matching entry for this day
            val matchingEntry = entries.find { entry ->
                val entryDisplayName = entry.dayName
                    .replace(Regex(",?\\s*\\d{4}"), "")
                    .trim()
                    .lowercase()

                val weekDayName = weekDay.name.lowercase()

                Log.d("SleepDebug", "Comparing: '$entryDisplayName' with '$weekDayName'")

                entryDisplayName == weekDayName
            }

            val napCount = matchingEntry?.naps?.size ?: 0

            Log.d("SleepDebug", "Day: ${weekDay.name}, Nap count: $napCount")

            weekDay.copy(
                napCount = napCount
            )
        }

        _weekDays.value = updatedWeekDays

        Log.d("UpdateSleepWeekDays", "Final updated week days:")
        updatedWeekDays.forEach { day ->
            Log.d("UpdateSleepWeekDays", "${day.name}: naps=${day.napCount}, selected=${day.isSelected}")
        }
    }
}