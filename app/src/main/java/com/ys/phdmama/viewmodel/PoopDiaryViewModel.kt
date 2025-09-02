package com.ys.phdmama.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query
import com.ys.phdmama.ui.screens.poop.DayPoopEntry
import com.ys.phdmama.ui.screens.poop.PoopRecord
import com.ys.phdmama.ui.screens.poop.WeekDay
import java.text.SimpleDateFormat
import java.util.*

class PoopDiaryViewModel : ViewModel() {
    private val firestore = FirebaseFirestore.getInstance()

    private val _poopEntries = MutableStateFlow<List<DayPoopEntry>>(emptyList())
    val poopEntries: StateFlow<List<DayPoopEntry>> = _poopEntries.asStateFlow()

    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()

    private val _errorMessage = MutableStateFlow<String?>(null)
    val errorMessage: StateFlow<String?> = _errorMessage.asStateFlow()

    private val _selectedDate = MutableStateFlow(Calendar.getInstance())
    val selectedDate: StateFlow<Calendar> = _selectedDate.asStateFlow()

    private val _weekDays = MutableStateFlow<List<WeekDay>>(emptyList())
    val weekDays: StateFlow<List<WeekDay>> = _weekDays.asStateFlow()

    private val dateFormatter = SimpleDateFormat("dd/MM/yyyy", Locale.getDefault())
    private val dayNameFormatter = SimpleDateFormat("EEEE dd", Locale.getDefault())

    init {
        generateCurrentWeek()
    }

    fun fetchPoopData(babyId: String) {
        viewModelScope.launch {
            _isLoading.value = true
            _errorMessage.value = null

            try {
                val calendar = Calendar.getInstance()
                calendar.set(Calendar.DAY_OF_WEEK, Calendar.MONDAY)
                val startOfWeek = calendar.timeInMillis

                calendar.add(Calendar.DAY_OF_WEEK, 6)
                val endOfWeek = calendar.timeInMillis

                firestore.collection("babies")
                    .document(babyId)
                    .collection("poopRecords")
                    .whereGreaterThanOrEqualTo("timestamp", startOfWeek)
                    .whereLessThanOrEqualTo("timestamp", endOfWeek)
                    .orderBy("timestamp", Query.Direction.DESCENDING)
                    .get()
                    .addOnSuccessListener { documents ->
                        val poopRecords = documents.mapNotNull { doc ->
                            try {
                                PoopRecord(
                                    id = doc.id,
                                    timestamp = doc.getLong("timestamp") ?: System.currentTimeMillis(),
                                    time = doc.getString("time") ?: "",
                                    color = doc.getString("color") ?: "",
                                    texture = doc.getString("texture") ?: "",
                                    size = doc.getString("size") ?: "",
                                    notes = doc.getString("notes") ?: "",
                                    createdAt = doc.getLong("createdAt") ?: System.currentTimeMillis()
                                )
                            } catch (e: Exception) {
                                null
                            }
                        }

                        processPoopData(poopRecords)
                        updateWeekDaysWithData(poopRecords)
                        _isLoading.value = false
                    }
                    .addOnFailureListener { exception ->
                        _errorMessage.value = "Error al cargar datos: ${exception.message}"
                        _isLoading.value = false
                    }

            } catch (e: Exception) {
                _errorMessage.value = "Error inesperado: ${e.message}"
                _isLoading.value = false
            }
        }
    }

    private fun processPoopData(poopRecords: List<PoopRecord>) {
        val groupedByDay = poopRecords.groupBy { poopRecord ->
            val calendar = Calendar.getInstance()
            calendar.timeInMillis = poopRecord.timestamp
            dateFormatter.format(calendar.time)
        }

        val dayEntries = groupedByDay.map { (dateString, records) ->
            val calendar = Calendar.getInstance()
            calendar.timeInMillis = records.first().timestamp
            val dayName = dayNameFormatter.format(calendar.time)
                .replaceFirstChar { it.uppercase() }

            DayPoopEntry(
                dayName = dayName,
                poops = records.sortedBy { it.timestamp }
            )
        }.sortedByDescending { entry ->
            // Sort by the timestamp of the first poop record in each day
            entry.poops.firstOrNull()?.timestamp ?: 0L
        }

        _poopEntries.value = dayEntries
    }

    private fun generateCurrentWeek() {
        val calendar = Calendar.getInstance()
        val today = Calendar.getInstance()

        // Set to Monday of current week
        calendar.set(Calendar.DAY_OF_WEEK, Calendar.MONDAY)

        val weekDaysList = mutableListOf<WeekDay>()

        repeat(7) { dayIndex ->
            val dayName = dayNameFormatter.format(calendar.time)
                .replaceFirstChar { it.uppercase() }

            val isToday = calendar.get(Calendar.DAY_OF_YEAR) == today.get(Calendar.DAY_OF_YEAR) &&
                    calendar.get(Calendar.YEAR) == today.get(Calendar.YEAR)

            weekDaysList.add(
                WeekDay(
                    name = dayName,
                    isSelected = isToday,
                    poopCount = 0 // Will be updated when data is fetched
                )
            )

            if (isToday) {
                _selectedDate.value = calendar.clone() as Calendar
            }

            calendar.add(Calendar.DAY_OF_MONTH, 1)
        }

        _weekDays.value = weekDaysList
    }

    private fun updateWeekDaysWithData(poopRecords: List<PoopRecord>) {
        val poopCountsByDay = poopRecords.groupBy { poopRecord ->
            val calendar = Calendar.getInstance()
            calendar.timeInMillis = poopRecord.timestamp
            calendar.get(Calendar.DAY_OF_YEAR)
        }.mapValues { it.value.size }

        val updatedWeekDays = _weekDays.value.map { weekDay ->
            val calendar = Calendar.getInstance()
            calendar.timeInMillis = _selectedDate.value.timeInMillis

            // Find the calendar day for this weekDay
            val weekCalendar = Calendar.getInstance()
            weekCalendar.set(Calendar.DAY_OF_WEEK, Calendar.MONDAY)

            repeat(7) { dayIndex ->
                val dayName = dayNameFormatter.format(weekCalendar.time)
                    .replaceFirstChar { it.uppercase() }

                if (dayName == weekDay.name) {
                    val dayOfYear = weekCalendar.get(Calendar.DAY_OF_YEAR)
                    val poopCount = poopCountsByDay[dayOfYear] ?: 0

                    return@map weekDay.copy(poopCount = poopCount)
                }

                weekCalendar.add(Calendar.DAY_OF_MONTH, 1)
            }

            weekDay
        }

        _weekDays.value = updatedWeekDays
    }

    fun selectDate(calendar: Calendar) {
        _selectedDate.value = calendar

        val updatedWeekDays = _weekDays.value.map { weekDay ->
            val selectedDayName = dayNameFormatter.format(calendar.time)
                .replaceFirstChar { it.uppercase() }

            weekDay.copy(isSelected = weekDay.name == selectedDayName)
        }

        _weekDays.value = updatedWeekDays
    }

    fun navigateToNextDay() {
        val calendar = _selectedDate.value.clone() as Calendar
        calendar.add(Calendar.DAY_OF_MONTH, 1)
        selectDate(calendar)
    }

    fun navigateToPreviousDay() {
        val calendar = _selectedDate.value.clone() as Calendar
        calendar.add(Calendar.DAY_OF_MONTH, -1)
        selectDate(calendar)
    }

    fun addPoopRecord(babyId: String, poopRecord: PoopRecord) {
        viewModelScope.launch {
            _isLoading.value = true
            _errorMessage.value = null

            try {
                val recordData = hashMapOf(
                    "timestamp" to poopRecord.timestamp,
                    "time" to poopRecord.time,
                    "color" to poopRecord.color,
                    "texture" to poopRecord.texture,
                    "size" to poopRecord.size,
                    "notes" to poopRecord.notes,
                    "createdAt" to poopRecord.createdAt
                )

                firestore.collection("babies")
                    .document(babyId)
                    .collection("poopRecords")
                    .add(recordData)
                    .addOnSuccessListener {
                        // Refresh data after adding
                        fetchPoopData(babyId)
                    }
                    .addOnFailureListener { exception ->
                        _errorMessage.value = "Error al guardar registro: ${exception.message}"
                        _isLoading.value = false
                    }

            } catch (e: Exception) {
                _errorMessage.value = "Error inesperado: ${e.message}"
                _isLoading.value = false
            }
        }
    }

    fun deletePoopRecord(babyId: String, recordId: String) {
        viewModelScope.launch {
            _isLoading.value = true
            _errorMessage.value = null

            try {
                firestore.collection("babies")
                    .document(babyId)
                    .collection("poopRecords")
                    .document(recordId)
                    .delete()
                    .addOnSuccessListener {
                        // Refresh data after deleting
                        fetchPoopData(babyId)
                    }
                    .addOnFailureListener { exception ->
                        _errorMessage.value = "Error al eliminar registro: ${exception.message}"
                        _isLoading.value = false
                    }

            } catch (e: Exception) {
                _errorMessage.value = "Error inesperado: ${e.message}"
                _isLoading.value = false
            }
        }
    }

    fun updatePoopRecord(babyId: String, recordId: String, updatedRecord: PoopRecord) {
        viewModelScope.launch {
            _isLoading.value = true
            _errorMessage.value = null

            try {
                val recordData = hashMapOf(
                    "timestamp" to updatedRecord.timestamp,
                    "time" to updatedRecord.time,
                    "color" to updatedRecord.color,
                    "texture" to updatedRecord.texture,
                    "size" to updatedRecord.size,
                    "notes" to updatedRecord.notes,
                    "createdAt" to updatedRecord.createdAt
                )

                firestore.collection("babies")
                    .document(babyId)
                    .collection("poopRecords")
                    .document(recordId)
                    .update(recordData as Map<String, Any>)
                    .addOnSuccessListener {
                        // Refresh data after updating
                        fetchPoopData(babyId)
                    }
                    .addOnFailureListener { exception ->
                        _errorMessage.value = "Error al actualizar registro: ${exception.message}"
                        _isLoading.value = false
                    }

            } catch (e: Exception) {
                _errorMessage.value = "Error inesperado: ${e.message}"
                _isLoading.value = false
            }
        }
    }

    fun clearErrorMessage() {
        _errorMessage.value = null
    }

    fun getPoopRecordsForDate(calendar: Calendar): List<PoopRecord> {
        val targetDateString = dateFormatter.format(calendar.time)

        return _poopEntries.value
            .find { entry ->
                val entryCalendar = Calendar.getInstance()
                if (entry.poops.isNotEmpty()) {
                    entryCalendar.timeInMillis = entry.poops.first().timestamp
                    dateFormatter.format(entryCalendar.time) == targetDateString
                } else {
                    false
                }
            }?.poops ?: emptyList()
    }

    fun getTotalPoopCountForWeek(): Int {
        return _poopEntries.value.sumOf { it.poops.size }
    }

    fun getAveragePoopCountPerDay(): Float {
        val totalCount = getTotalPoopCountForWeek()
        val daysWithData = _weekDays.value.count { it.poopCount > 0 }
        return if (daysWithData > 0) totalCount.toFloat() / daysWithData else 0f
    }
}