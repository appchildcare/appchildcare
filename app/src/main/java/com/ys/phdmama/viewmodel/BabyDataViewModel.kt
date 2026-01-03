package com.ys.phdmama.viewmodel

import android.os.Build
import android.util.Log
import androidx.annotation.RequiresApi
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query
import com.ys.phdmama.model.Vaccine
import com.ys.phdmama.viewmodel.BabyDataViewModel.UiEvent.*
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.map
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
    val birthDate: String? = "" // TODO: REVISAR
)

data class BabyAge(val years: Int, val months: Int)

@HiltViewModel
class BabyDataViewModel @Inject constructor(
    private val dataStore: DataStore<Preferences>
) : ViewModel() {

    private val SELECTED_BABY_ID = stringPreferencesKey("selected_baby_id")

    private val firebaseAuth: FirebaseAuth = FirebaseAuth.getInstance()
    private val firestore: FirebaseFirestore = FirebaseFirestore.getInstance()

    private val _uiEvent = Channel<BabyDataViewModel.UiEvent>()
    val uiEvent = _uiEvent.receiveAsFlow()

    private val _babyAttributes = MutableStateFlow(mapOf<String, String>())
    private val _babyData = MutableStateFlow<BabyProfile?>(null)

    var vaccineText by mutableStateOf("")
    var vaccineDate by mutableStateOf("")

    val babyData: StateFlow<BabyProfile?> = _babyData.asStateFlow()

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

    private val _babyDocumentIds = MutableLiveData<List<String>>()
    val babyDocumentIds: LiveData<List<String>> = _babyDocumentIds

    private val _selectedBaby = MutableStateFlow<BabyProfile?>(null)
    val selectedBaby: StateFlow<BabyProfile?> = _selectedBaby.asStateFlow()

    fun setBabyAttribute(attribute: String, value: String) {
        _babyAttributes.value = _babyAttributes.value.toMutableMap().apply {
            this[attribute] = value
        }
    }

    fun getBabyAttribute(attribute: String): String? {
        return _babyAttributes.value[attribute]
    }

    init {
        fetchBabyProfile()
        loadSelectedBaby()
    }

    private fun loadSelectedBaby() {
        viewModelScope.launch {
            dataStore.data
                .map { preferences ->
                    preferences[SELECTED_BABY_ID]
                }
                .collect { savedBabyId ->
                    if (savedBabyId != null && _babyList.value.isNotEmpty()) {
                        _babyList.value.find { it.id == savedBabyId }?.let { baby ->
                            _selectedBaby.value = baby
                            Log.d("BabyDataViewModel", "Restored selected baby: ${baby.name}")
                        }
                    }
                }
        }
    }

    fun setSelectedBaby(baby: BabyProfile?) {
        _selectedBaby.value = baby
        viewModelScope.launch {
            dataStore.edit { preferences ->
                if (baby != null) {
                    baby.id?.let { id ->
                        preferences[SELECTED_BABY_ID] = id
                        Log.d("BabyDataViewModel", "Saved selected baby: ${baby.name}")
                    }
                } else {
                    preferences.remove(SELECTED_BABY_ID)
                    Log.d("BabyDataViewModel", "Cleared selected baby")
                }
            }
        }
    }

    fun clearSelectedBaby() {
        setSelectedBaby(null)
    }

    fun fetchBabies(userId: String?) {
        val uid = firebaseAuth.currentUser?.uid
        _isLoadingBabies.value = true
        if (uid == null) {
            _isLoadingBabies.value = false
            return
        }

        Log.d("BabyDataViewModel", "ViewModel instance in fetchBabies: ${this.hashCode()}")
        Log.d("BabyDataViewModel", "Fetching babies for userId: $uid")
        Log.d("BabyDataViewModel", "ViewModel instance in fetchBabies: ${this.hashCode()}")
        Log.d("BabyDataViewModel", "Fetching babies for userId: $uid")
        val db = FirebaseFirestore.getInstance()
        db.collection("users")
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
            }
            .addOnFailureListener { exception ->
                Log.e("BabyDataViewModel", "Error fetching babies: ", exception)
                _isLoadingBabies.value = false
            }
    }

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

    fun clearUserData() {
        _babyList.value = emptyList()
        _isLoadingBabies.value = false
        _babyData.value = null
        vaccineList = emptyList()
        calculatedDate = null
        vaccineText = ""
        vaccineDate = ""
    }

    private fun fetchBabyProfile() {
        viewModelScope.launch {
            // Get the current user's UID
            val currentUserId = firebaseAuth.currentUser?.uid
            if (currentUserId == null) {
                Log.e("FirestoreError", "No user is currently signed in")
                return@launch
            }
            firestore.collection("users")
                .document(currentUserId)
                .collection("babies")
                .get()
                .addOnSuccessListener { result ->
                    for (document in result) {
                        val baby = document.toObject(BabyProfile::class.java)
                           .copy(id = document.id)
                        _babyData.value = baby
                        setSelectedBaby(baby)
                        Log.d("BabyDataViewModel", baby.toString())
                        break // Get only the first baby
                    }
                }
                .addOnFailureListener { exception ->
                    Log.e("BabyViewModel", "Error fetching baby: ", exception)
                }
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
                    babyRef.add(babyData).await()
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

    fun addVaccines(
        onError: (String) -> Unit
    ){
        val uid = firebaseAuth.currentUser?.uid
        if (uid != null) {
            viewModelScope.launch {
                try {
                    val vaccineId = UUID.randomUUID().toString()
                    val formatter = SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault())
                    val currentDate = formatter.format(Date())
                    val vaccineToSave = Vaccine(id = vaccineId, vaccineName = vaccineText, vaccineDate = vaccineDate,  timestamp = currentDate)
                    val vaccinesRef = firestore.collection("users").document(uid).collection("vaccines")
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

        if(userId !=null) {
            firestore.collection("users").document(userId)
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

        if(userId != null) {
            vaccine.id?.let {
                firestore.collection("users").document(userId)
                    .collection("vaccines").document(it)
                    .set(vaccine)
            }
        }
    }

    fun loadBabyIds(onSuccess: (String?) -> Unit, onSkip: () -> Unit, onError: (String) -> Unit) {
        val uid = firebaseAuth.currentUser?.uid
        if (uid != null) {
            viewModelScope.launch {
                try {
                    val userRef = firestore.collection("babies").document(uid)
                    val document = userRef.get().await()
                    val babyId = document.id
                    Log.d("BabyData ViewModel", babyId)
                    onSuccess(babyId)
                } catch (e: Exception) {
                    onError(e.localizedMessage ?: "Error al obtener detalles del bebe")
                }
            }
        } else {
            onError("UID de usuario no encontrado")
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
                    val babyRef = firestore.collection("users")
                        .document(uid)
                        .collection("babies")
                        .document(babyId)

                    babyRef.update(babyData).await()
                    onSuccess()
                    sendSnackbar("Información actualizada correctamente!")

                    // Refresh the baby list after updating
                    fetchBabies(uid)

                    // Update the current baby data if it's the one being edited
                    if (_babyData.value?.id == babyId) {
                        val updatedBaby = _babyData.value?.copy(
                            name = babyData["name"] as? String ?: _babyData.value?.name ?: "",
                            apgar = babyData["apgar"] as? String ?: _babyData.value?.apgar ?: "",
                            height = babyData["height"] as? String ?: _babyData.value?.height ?: "",
                            weight = babyData["weight"] as? String ?: _babyData.value?.weight ?: "",
                            perimeter = babyData["perimeter"] as? String ?: _babyData.value?.perimeter ?: "",
                            bloodType = babyData["bloodType"] as? String ?: _babyData.value?.bloodType ?: "",
                            birthDate = babyData["birthDate"] as? String ?: _babyData.value?.birthDate ?: "",
                            sex = babyData["sex"] as? String ?: _babyData.value?.sex ?: ""
                        )
                        _babyData.value = updatedBaby
                    }
                } catch (e: Exception) {
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


    sealed class UiEvent {
        data class ShowSnackbar(val message: String) : UiEvent()
    }

    private fun sendSnackbar(message: String) {
        viewModelScope.launch {
            _uiEvent.send(ShowSnackbar(message))
            delay(100)
        }
    }
}
