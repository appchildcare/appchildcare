package com.ys.phdmama.viewmodel

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
import com.ys.phdmama.model.Vaccine
import com.ys.phdmama.repository.BabyPreferencesRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await
import java.text.SimpleDateFormat
import java.time.LocalDate
import java.time.Period
import java.time.format.DateTimeFormatter
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
    val correctedAge: String? = "" // Corrected age in weeks
) {
    // No-argument constructor required by Firestore
    constructor() : this(
        id = "",
        apgar = "",
        bloodType = "",
        height = "",
        name = "",
        perimeter = "",
        sex = "",
        weight = "",
        birthDate = "",
        weeksBirth = "",
        correctedAge = ""
    )
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
    private val _babyData = MutableStateFlow<BabyProfile?>(null)

    var vaccineText by mutableStateOf("")
    var vaccineDate by mutableStateOf("")

    var calculatedDate by mutableStateOf<String?>(null)
        private set
    var locale = Locale("es", "ES")
        private set

    var vaccineList by mutableStateOf<List<Vaccine>>(emptyList())
        private set

    private val _babyList = MutableStateFlow<List<BabyProfile>>(emptyList())
    val babyList: StateFlow<List<BabyProfile>> = _babyList.asStateFlow()

    private val _isLoadingBabies = MutableStateFlow(false)
    val isLoadingBabies: StateFlow<Boolean> = _isLoadingBabies.asStateFlow()

    // Main selected baby StateFlow - combines DataStore with baby list
    private val _selectedBaby = MutableStateFlow<BabyProfile?>(null)
    val selectedBaby: StateFlow<BabyProfile?> = _selectedBaby.asStateFlow()

    companion object {
        private const val FULL_TERM_WEEKS = 40
        private const val PREMATURE_THRESHOLD_WEEKS = 37
    }

    init {
        // Observe saved baby ID from DataStore and sync with baby list
        observeSelectedBabyFromDataStore()
        fetchBabies()
    }

    // ============================================
    // DATASTORE INTEGRATION
    // ============================================

    private fun observeSelectedBabyFromDataStore() {
        viewModelScope.launch {
            preferencesRepository.selectedBabyIdFlow.collect { savedBabyId ->
                Log.d("BabyDataViewModel", "DataStore changed, saved ID: $savedBabyId")

                if (savedBabyId != null && _babyList.value.isNotEmpty()) {
                    // Find the baby in the current list
                    val baby = _babyList.value.find { it.id == savedBabyId }
                    if (baby != null) {
                        _selectedBaby.value = baby
                        _babyData.value = baby
                        Log.d("BabyDataViewModel", "Restored baby from DataStore: ${baby.name}")
                    } else {
                        Log.d("BabyDataViewModel", "Saved baby ID not found in list")
                    }
                } else if (savedBabyId == null && _babyList.value.isNotEmpty() && _selectedBaby.value == null) {
                    // No saved ID, auto-select first baby
                    val firstBaby = _babyList.value.first()
                    setSelectedBaby(firstBaby)
                    Log.d("BabyDataViewModel", "Auto-selected first baby: ${firstBaby.name}")
                }
            }
        }
    }

    fun setSelectedBaby(baby: BabyProfile?) {
        _selectedBaby.value = baby
        _babyData.value = baby

        viewModelScope.launch {
            if (baby?.id != null) {
                preferencesRepository.saveSelectedBabyId(baby.id!!)
                Log.d("BabyDataViewModel", "Set selected baby: ${baby.name} (${baby.id})")
            } else {
                preferencesRepository.clearSelectedBabyId()
                Log.d("BabyDataViewModel", "Cleared selected baby")
            }
        }
    }

    // ============================================
    // BABY OPERATIONS
    // ============================================

    fun setBabyAttribute(attribute: String, value: String) {
        _babyAttributes.value = _babyAttributes.value.toMutableMap().apply {
            this[attribute] = value
        }
    }

    fun getBabyAttribute(attribute: String): String? {
        return _babyAttributes.value[attribute]
    }

    fun fetchBabies() {
        val uid = firebaseAuth.currentUser?.uid
        _isLoadingBabies.value = true

        if (uid == null) {
            _isLoadingBabies.value = false
            return
        }

        Log.d("BabyDataViewModel", "Fetching babies for userId: $uid")

        firestore.collection("users")
            .document(uid)
            .collection("babies")
            .get()
            .addOnSuccessListener { snapshot ->
                val babies = snapshot.documents.mapNotNull { doc ->
                    doc.toObject(BabyProfile::class.java)?.copy(id = doc.id)
                }
                _babyList.value = babies
                _isLoadingBabies.value = false

                Log.d("BabyDataViewModel", "Babies fetched successfully. Count: ${babies.size}")

                // After fetching babies, restore selected baby from DataStore
                viewModelScope.launch {
                    val savedBabyId = preferencesRepository.getSelectedBabyId()
                    if (savedBabyId != null) {
                        val baby = babies.find { it.id == savedBabyId }
                        if (baby != null) {
                            _selectedBaby.value = baby
                            _babyData.value = baby
                            Log.d("BabyDataViewModel", "Restored saved baby: ${baby.name}")
                        }
                    } else if (babies.isNotEmpty() && _selectedBaby.value == null) {
                        // No saved baby, select first one
                        setSelectedBaby(babies.first())
                    }
                }
            }
            .addOnFailureListener { exception ->
                Log.e("BabyDataViewModel", "Error fetching babies: ", exception)
                _isLoadingBabies.value = false
            }
    }

    fun addBabyToUser(
        babyData: Map<String, Any>,
        onSuccess: () -> Unit = {},
        onError: (String) -> Unit
    ) {
        val uid = firebaseAuth.currentUser?.uid
        if (uid != null) {
            viewModelScope.launch {
                try {
                    val babyRef = firestore.collection("users").document(uid).collection("babies")
                    val docRef = babyRef.add(babyData).await()
                    val weeksBirth = babyData["weeksBirth"] as? String
                    val birthDate = babyData["birthDate"] as? String
                    val correctedAge = calculateCorrectedAge(birthDate, weeksBirth)

                    // Create baby profile with the new ID
                    val newBaby = BabyProfile(
                        id = docRef.id,
                        name = babyData["name"] as? String ?: "",
                        apgar = babyData["apgar"] as? String ?: "",
                        bloodType = babyData["bloodType"] as? String ?: "",
                        height = babyData["height"] as? String ?: "",
                        perimeter = babyData["perimeter"] as? String ?: "",
                        sex = babyData["sex"] as? String ?: "",
                        weight = babyData["weight"] as? String ?: "",
                        birthDate = birthDate,
                        weeksBirth = weeksBirth ?: "",
                        correctedAge = correctedAge
                    )

                    // Add to list and set as selected
                    _babyList.value = _babyList.value + newBaby
                    setSelectedBaby(newBaby)

                    // Log corrected age info
                    if (correctedAge != null) {
                        val weeksOfPrematurity = getWeeksOfPrematurity(weeksBirth)
                        Log.d("BabyDataViewModel",
                            "Baby added with corrected age: $correctedAge weeks " +
                                    "(Prematurity: $weeksOfPrematurity weeks)")
                    }

                    onSuccess()
                    sendSnackbar("Información agregada correctamente!")
                } catch (e: Exception) {
                    onError(e.localizedMessage ?: "Error al añadir bebé")
                }
            }
        } else {
            onError("UID de usuario no encontrado")
        }
    }

    fun updateBabyData(
        babyId: String?,
        babyData: Map<String, Any>,
        onSuccess: () -> Unit = {},
        onError: (String) -> Unit
    ) {
        val uid = firebaseAuth.currentUser?.uid
        if (uid != null && !babyId.isNullOrEmpty()) {
            viewModelScope.launch {
                try {
                    // Recalculate corrected age with updated data
                    val weeksBirthRaw = babyData["weeksBirth"] as? String ?: ""
                    val birthDate = babyData["birthDate"] as? String
                    val correctedAge = calculateCorrectedAge(birthDate, weeksBirthRaw)

                    // Add corrected age to the update data
                    val completeData = babyData.toMutableMap().apply {
                        if (correctedAge != null) {
                            this["realWeeksBirth"] = correctedAge.toDouble()
                            Log.d("BabyDataViewModel", "Updating realWeeksBirth in Firebase: ${weeksBirthRaw}")
                        }
                    }

                    val babyRef = firestore.collection("users")
                        .document(uid)
                        .collection("babies")
                        .document(babyId)

                    babyRef.update(completeData).await()

                    // Update local state
                    val updatedBabies = _babyList.value.map { baby ->
                        if (baby.id == babyId) {
                            baby.copy(
                                name = babyData["name"] as? String ?: baby.name,
                                apgar = babyData["apgar"] as? String ?: baby.apgar,
                                height = babyData["height"] as? String ?: baby.height,
                                weight = babyData["weight"] as? String ?: baby.weight,
                                perimeter = babyData["perimeter"] as? String ?: baby.perimeter,
                                bloodType = babyData["bloodType"] as? String ?: baby.bloodType,
                                birthDate = birthDate ?: baby.birthDate,
                                sex = babyData["sex"] as? String ?: baby.sex,
                                weeksBirth = weeksBirthRaw,
                                correctedAge = correctedAge
                            )
                        } else {
                            baby
                        }
                    }
                    _babyList.value = updatedBabies

                    // Update selected baby if it's the one being edited
                    if (_selectedBaby.value?.id == babyId) {
                        val updatedBaby = updatedBabies.find { it.id == babyId }
                        if (updatedBaby != null) {
                            _selectedBaby.value = updatedBaby
                            _babyData.value = updatedBaby
                        }
                    }

                    // Log corrected age info
                    if (correctedAge != null) {
                        val weeksOfPrematurity = getWeeksOfPrematurity(correctedAge)
                        Log.d("BabyDataViewModel",
                            "Baby updated with corrected age: $correctedAge " +
                                    "(Prematurity: $weeksOfPrematurity weeks)")
                    }

                    onSuccess()
                    sendSnackbar("Información actualizada correctamente!")
                } catch (e: Exception) {
                    Log.e("BabyDataViewModel", "Error updating baby: ", e)
                    onError(e.localizedMessage ?: "Error al actualizar información del bebé")
                }
            }
        } else {
            if (uid == null) {
                onError("UID de usuario no encontrado")
            } else {
                onError("ID del bebé no válido")
            }
        }
    }

    /**
     * Calculates corrected age (edad corregida) for premature babies
     *
     * Formula:
     * - Weeks of prematurity = 40 - weeks at birth
     * - Corrected age = Chronological age - Weeks of prematurity
     *
     * @param birthDate Baby's birth date in format "dd/MM/yyyy" or ISO format
     * @param weeksBirth Gestational age at birth in weeks
     * @return Corrected age in weeks, or null if baby is not premature or data is invalid
     */
    private fun calculateCorrectedAge(
        birthDate: String?,
        weeksBirth: String?
    ): String? {

        if (birthDate.isNullOrEmpty() || weeksBirth.isNullOrEmpty()) {
            return null
        }

        val weeksAtBirth = weeksBirth.toIntOrNull() ?: return null

        // Only premature babies
        if (weeksAtBirth >= PREMATURE_THRESHOLD_WEEKS) {
            return null
        }

        val birthDateParsed = parseBirthDate(birthDate) ?: return null
        val today = Calendar.getInstance()
        val diffInMillis = today.timeInMillis - birthDateParsed.timeInMillis

        val daysOfWeek = 7
        val hoursInDay = 24
        val minutesInHour = 60
        val secondsInMinute = 60
        val millisInSecond = 1000

        val chronologicalAgeInWeeks =
            diffInMillis / (daysOfWeek * hoursInDay * minutesInHour * secondsInMinute * millisInSecond)
        val weeksOfPrematurity = FULL_TERM_WEEKS - weeksAtBirth

        val correctedAge = chronologicalAgeInWeeks - weeksOfPrematurity
        val response = correctedAge.takeIf { it >= 0 }

        return response.toString()
    }


    private fun parseBirthDate(birthDate: String): Calendar? {
        return try {
            val format = SimpleDateFormat("dd MMMM yyyy", Locale.ENGLISH)
            format.isLenient = false
            val date = format.parse(birthDate) ?: return null
            Calendar.getInstance().apply {
                time = date
            }
        } catch (e: Exception) {
            Log.e("BabyDataViewModel", "Error parsing birth date: $birthDate", e)
            null
        }
    }

    /**
     * Gets weeks of prematurity (semanas de prematuridad)
     * Formula: 40 - weeks at birth
     */
    fun getWeeksOfPrematurity(weeksBirth: String?): Double? {
        if (weeksBirth == null) return null
        val weeksAtBirth = weeksBirth.toDouble()
        return if (weeksAtBirth < PREMATURE_THRESHOLD_WEEKS) {
            FULL_TERM_WEEKS - weeksAtBirth
        } else null
    }

    // ============================================
    // VACCINE OPERATIONS
    // ============================================

    fun addVaccines(onError: (String) -> Unit) {
        val uid = firebaseAuth.currentUser?.uid
        val selectedBaby = selectedBaby.value

        if (uid != null && selectedBaby != null) {
            viewModelScope.launch {
                try {
                    val vaccineId = UUID.randomUUID().toString()
                    val formatter = SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault())
                    val currentDate = formatter.format(Date())
                    val vaccineToSave = Vaccine(
                        id = vaccineId,
                        vaccineName = vaccineText,
                        vaccineDate = vaccineDate,
                        timestamp = currentDate
                    )
                    val vaccinesRef = firestore.collection("users")
                        .document(uid)
                        .collection("babies")
                        .document(selectedBaby.id.toString())
                        .collection("vaccines")

                    vaccinesRef.add(vaccineToSave).await()

                    sendSnackbar("Información agregada correctamente!")
                } catch (e: Exception) {
                    onError(e.localizedMessage ?: "Error al agregar vacuna")
                }
            }
        } else {
            onError("UID de usuario no encontrado")
        }
    }

    fun loadVaccines() {
        val userId = firebaseAuth.currentUser?.uid
        val selectedBaby = selectedBaby.value

        if (userId != null && selectedBaby != null) {
            vaccineList = emptyList()
            firestore
                .collection("users")
                .document(userId)
                .collection("babies")
                .document(selectedBaby.id.toString())
                .collection("vaccines")
                .orderBy("timestamp", Query.Direction.DESCENDING)
                .addSnapshotListener { snapshot, _ ->
                    if (snapshot != null && !snapshot.isEmpty) {
                        vaccineList = snapshot.documents.mapNotNull { doc ->
                            doc.toObject(Vaccine::class.java)?.copy(id = doc.id)
                        }
                    }
                }
        }
    }

    fun updateVaccine(vaccine: Vaccine) {
        val userId = firebaseAuth.currentUser?.uid

        if (userId != null) {
            vaccine.id?.let {
                firestore.collection("users").document(userId)
                    .collection("vaccines").document(it)
                    .set(vaccine)
            }
        }
    }

    // ============================================
    // UTILITY FUNCTIONS
    // ============================================

    fun onDateSelected(date: Date) {
        val calendar = Calendar.getInstance().apply {
            time = date
            add(Calendar.WEEK_OF_YEAR, 40)
        }
        calculatedDate = SimpleDateFormat("yyyy-MM-dd", locale).format(calendar.time)
    }

    @RequiresApi(Build.VERSION_CODES.O)
    fun calculateAgeInMonths(birthDate: String?): BabyAge {
        if (birthDate.isNullOrBlank()) return BabyAge(0, 0)
        val formatter = DateTimeFormatter.ofPattern("dd MMMM yyyy", Locale.ENGLISH)
        val convertedBirthDate = LocalDate.parse(birthDate, formatter)
        val today = LocalDate.now()
        val timeLapse = Period.between(convertedBirthDate, today)
        return BabyAge(timeLapse.years, timeLapse.months)
    }

    fun calculateBabyAge(birthDateString: String): BabyAge {
        try {
            val sdf = SimpleDateFormat("dd MMMM yyyy", Locale.getDefault())
            val birthDate = sdf.parse(birthDateString) ?: return BabyAge(0, 0)

            val now = Calendar.getInstance()
            val birth = Calendar.getInstance().apply { time = birthDate }

            var years = now.get(Calendar.YEAR) - birth.get(Calendar.YEAR)
            var months = now.get(Calendar.MONTH) - birth.get(Calendar.MONTH)

            if (months < 0) {
                years--
                months += 12
            }

            return BabyAge(years, months)
        } catch (e: Exception) {
            return BabyAge(0, 0)
        }
    }

    fun clearUserData() {
        _babyList.value = emptyList()
        _isLoadingBabies.value = false
        _babyData.value = null
        _selectedBaby.value = null
        vaccineList = emptyList()
        calculatedDate = null
        vaccineText = ""
        vaccineDate = ""

        viewModelScope.launch {
            preferencesRepository.clearSelectedBabyId()
        }
    }

    fun fetchBabiesForUser(
        onResult: (List<String>) -> Unit,
        onError: (Exception) -> Unit
    ) {
        val userId = firebaseAuth.currentUser?.uid

        if (userId != null) {
            FirebaseFirestore.getInstance()
                .collection("users")
                .document(userId)
                .collection("babies")
                .get()
                .addOnSuccessListener { result ->
                    val babyIds = result.map { it.id }
                    onResult(babyIds)
                }
                .addOnFailureListener { onError(it) }
        }
    }

    sealed class UiEvent {
        data class ShowSnackbar(val message: String) : UiEvent()
    }

    private fun sendSnackbar(message: String) {
        viewModelScope.launch {
            _uiEvent.send(UiEvent.ShowSnackbar(message))
            delay(100)
        }
    }
}
