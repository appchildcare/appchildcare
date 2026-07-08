package com.ys.cunaco.viewmodel

import android.os.Build
import android.util.Log
import androidx.annotation.RequiresApi
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query
import com.ys.cunaco.model.Vaccine
import com.ys.cunaco.repository.BabyPreferencesRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await
import java.text.SimpleDateFormat
import java.time.LocalDate
import java.time.Period
import java.util.Calendar
import java.util.Date
import java.util.Locale
import java.util.UUID
import javax.inject.Inject

data class BabyProfile(
    val id: String? = "",
    val apgar: String = "",
    val bloodType: String = "",
    val height: String = "",
    val name: String = "",
    val perimeter: String = "",
    val sex: String = "",
    val weight: String = "",
    val birthDate: String? = "",
    val weeksBirth: String? = "",
    val correctedAge: String? = ""
) {
    constructor() : this(id = "")
}

data class BabyAge(val years: Int, val months: Int)

@HiltViewModel
class BabyDataViewModel @Inject constructor(
    private val preferencesRepository: BabyPreferencesRepository
) : ViewModel() {

    private val firebaseAuth: FirebaseAuth = FirebaseAuth.getInstance()
    private val firestore: FirebaseFirestore = FirebaseFirestore.getInstance()

    private val _uiEvent = Channel<UiEvent>()
    val uiEvent = _uiEvent.receiveAsFlow()

    private val _babyAttributes = MutableStateFlow(mapOf<String, String>())
    
    var vaccineText by mutableStateOf("")
    var vaccineDate by mutableStateOf("")
    var calculatedDate by mutableStateOf<String?>(null)
    var locale = Locale("es", "ES")

    var vaccineList by mutableStateOf<List<Vaccine>>(emptyList())

    private val _babyList = MutableStateFlow<List<BabyProfile>>(emptyList())
    val babyList: StateFlow<List<BabyProfile>> = _babyList.asStateFlow()

    private val _isLoadingBabies = MutableStateFlow(false)
    val isLoadingBabies: StateFlow<Boolean> = _isLoadingBabies.asStateFlow()

    private val _selectedBaby = MutableStateFlow<BabyProfile?>(null)
    val selectedBaby: StateFlow<BabyProfile?> = _selectedBaby.asStateFlow()

    companion object {
        private const val FULL_TERM_WEEKS = 40
        private const val PREMATURE_THRESHOLD_WEEKS = 37
    }

    init {
        fetchBabies()
        firebaseAuth.addAuthStateListener { auth ->
            if (auth.currentUser != null) fetchBabies() else clearUserData()
        }
    }

    fun setSelectedBaby(baby: BabyProfile?) {
        _selectedBaby.value = baby
        viewModelScope.launch {
            if (baby?.id != null) {
                preferencesRepository.saveSelectedBabyId(baby.id)
            } else {
                preferencesRepository.clearSelectedBabyId()
            }
        }
    }

    fun setBabyAgeMonths(months: String?) {
        viewModelScope.launch {
            if (months != null) {
                preferencesRepository.saveBabyAgeMonths(months)
            }
        }
    }

    fun setBabyAttribute(attribute: String, value: String) {
        _babyAttributes.value = _babyAttributes.value.toMutableMap().apply { this[attribute] = value }
    }

    fun getBabyAttribute(attribute: String): String? = _babyAttributes.value[attribute]

    fun onDateSelected(date: Date) {
        val calendar = Calendar.getInstance().apply { time = date }
        calculatedDate = SimpleDateFormat("yyyy-MM-dd", locale).format(calendar.time)
    }

    fun fetchBabies() {
        val uid = firebaseAuth.currentUser?.uid ?: return
        _isLoadingBabies.value = true
        firestore.collection("users").document(uid).collection("babies").get()
            .addOnSuccessListener { snapshot ->
                val babies = snapshot.documents.mapNotNull { it.toObject(BabyProfile::class.java)?.copy(id = it.id) }
                _babyList.value = babies
                _isLoadingBabies.value = false
                viewModelScope.launch {
                    val savedId = preferencesRepository.getSelectedBabyId()
                    val baby = babies.find { it.id == savedId } ?: babies.firstOrNull()
                    if (baby != null) setSelectedBaby(baby)
                }
            }
            .addOnFailureListener { _isLoadingBabies.value = false }
    }

    fun addBabyToUser(babyData: Map<String, Any>, onSuccess: () -> Unit = {}, onError: (String) -> Unit) {
        val uid = firebaseAuth.currentUser?.uid ?: return
        viewModelScope.launch {
            try {
                val docRef = firestore.collection("users").document(uid).collection("babies").add(babyData).await()
                // Seleccionar inmediatamente el nuevo bebé
                preferencesRepository.saveSelectedBabyId(docRef.id)
                fetchBabies()
                onSuccess()
                sendSnackbar("Bebé registrado")
            } catch (e: Exception) { onError(e.localizedMessage ?: "Error") }
        }
    }

    fun updateBabyData(babyId: String?, babyData: Map<String, Any>, onSuccess: () -> Unit = {}, onError: (String) -> Unit) {
        val uid = firebaseAuth.currentUser?.uid ?: return
        if (babyId.isNullOrEmpty()) return
        viewModelScope.launch {
            try {
                firestore.collection("users").document(uid).collection("babies").document(babyId).set(babyData).await()
                fetchBabies()
                onSuccess()
                sendSnackbar("Datos actualizados")
            } catch (e: Exception) { onError(e.localizedMessage ?: "Error") }
        }
    }

    fun addVaccines(onError: (String) -> Unit) {
        val uid = firebaseAuth.currentUser?.uid ?: return
        val babyId = selectedBaby.value?.id ?: return
        viewModelScope.launch {
            try {
                val vaccine = Vaccine(id = UUID.randomUUID().toString(), vaccineName = vaccineText, vaccineDate = vaccineDate, timestamp = SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault()).format(Date()))
                firestore.collection("users").document(uid).collection("babies").document(babyId).collection("vaccines").add(vaccine).await()
                loadVaccines()
                sendSnackbar("Vacuna guardada")
            } catch (e: Exception) { onError(e.localizedMessage ?: "Error") }
        }
    }

    fun loadVaccines() {
        val uid = firebaseAuth.currentUser?.uid ?: return
        val babyId = selectedBaby.value?.id ?: return
        firestore.collection("users").document(uid).collection("babies").document(babyId).collection("vaccines").orderBy("timestamp", Query.Direction.DESCENDING)
            .addSnapshotListener { snapshot, _ -> if (snapshot != null) vaccineList = snapshot.documents.mapNotNull { it.toObject(Vaccine::class.java)?.copy(id = it.id) } }
    }

    fun updateVaccine(vaccine: Vaccine) {
        val uid = firebaseAuth.currentUser?.uid ?: return
        val babyId = selectedBaby.value?.id ?: return
        vaccine.id?.let { firestore.collection("users").document(uid).collection("babies").document(babyId).collection("vaccines").document(it).set(vaccine) }
    }

    @RequiresApi(Build.VERSION_CODES.O)
    fun calculateCorrectedAge(birthDate: String?, weeksBirth: String?): BabyAge? {
        if (birthDate.isNullOrEmpty()) return null
        
        // Normalización para parseo robusto (elimina "de", comas, y múltiples espacios)
        val normalizedDate = birthDate.replace(Regex("(?i)\\bde\\b"), " ")
            .replace(",", " ")
            .replace(Regex("\\s+"), " ")
            .trim()
            .lowercase()

        val formats = listOf("dd MMMM yyyy", "d MMMM yyyy", "dd MMM yyyy", "yyyy-MM-dd", "dd/MM/yyyy")
        val locales = listOf(Locale("es", "ES"), Locale.getDefault(), Locale.ENGLISH)

        var date: Date? = null
        outer@for (loc in locales) {
            for (fmt in formats) {
                try {
                    val sdf = SimpleDateFormat(fmt, loc)
                    sdf.isLenient = true
                    date = sdf.parse(normalizedDate)
                    if (date != null) break@outer
                } catch (e: Exception) { continue }
            }
        }

        if (date == null) {
            Log.e("BabyDataViewModel", "Fallo al parsear fecha: $birthDate")
            return null
        }

        return try {
            val weeksAtBirth = weeksBirth?.toIntOrNull() ?: 40
            val birthCalendar = Calendar.getInstance().apply { time = date }
            val today = Calendar.getInstance()
            
            val diffMillis = today.timeInMillis - birthCalendar.timeInMillis
            val chronologicalWeeks = diffMillis / (7L * 24 * 60 * 60 * 1000)
            
            val finalYears: Int
            val finalMonths: Int
            
            if (weeksAtBirth < PREMATURE_THRESHOLD_WEEKS) {
                val weeksPrematurity = FULL_TERM_WEEKS - weeksAtBirth
                val correctedWeeks = chronologicalWeeks - weeksPrematurity
                val totalMonths = (correctedWeeks / 4.345).toInt().coerceAtLeast(0)
                finalYears = totalMonths / 12
                finalMonths = totalMonths % 12
            } else {
                val birthLocalDate = LocalDate.of(birthCalendar.get(Calendar.YEAR), birthCalendar.get(Calendar.MONTH) + 1, birthCalendar.get(Calendar.DAY_OF_MONTH))
                val period = Period.between(birthLocalDate, LocalDate.now())
                finalYears = period.years.coerceAtLeast(0)
                finalMonths = period.months.coerceAtLeast(0)
            }
            
            setBabyAgeMonths(((finalYears * 12) + finalMonths).toString())
            BabyAge(finalYears, finalMonths)
        } catch (e: Exception) {
            Log.e("BabyDataViewModel", "Error en cálculo de edad", e)
            null
        }
    }

    fun clearUserData() {
        _babyList.value = emptyList()
        _isLoadingBabies.value = false
        _selectedBaby.value = null
        vaccineList = emptyList()
    }

    sealed class UiEvent { data class ShowSnackbar(val message: String) : UiEvent() }
    private fun sendSnackbar(message: String) { viewModelScope.launch { _uiEvent.send(UiEvent.ShowSnackbar(message)) } }
}
