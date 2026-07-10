package com.ys.cunaco.viewmodel

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.ys.cunaco.model.ChecklistItemState
import com.ys.cunaco.repository.BabyPreferencesRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await
import javax.inject.Inject

@HiltViewModel
class CheckItemsViewModel @Inject constructor(
    private val babyPreferencesRepository: BabyPreferencesRepository,
) : ViewModel() {
    private val firestore = FirebaseFirestore.getInstance()
    private val auth = FirebaseAuth.getInstance()

    data class ChecklistItem(
        val id: String = "",
        val item: String = "",
        val subtopic: String = "",
        val months: Int = 0,
        val topic: String = "",
        val role: String = "",
        val isChecked: Boolean = false
    )

    data class SubtopicGroup(
        val subtopic: String,
        val items: List<ChecklistItem>,
        val topic: String = "",
        val months: Int = 0,
        val role: String = "",
    )

    data class TopicGroup(
        val topic: String,
        val months: Int,
        val role: String = "",
        val subtopicGroups: List<SubtopicGroup>
    )

    private val _topicGroups = MutableStateFlow<List<TopicGroup>>(emptyList())
    val topicGroups: StateFlow<List<TopicGroup>> = _topicGroups.asStateFlow()

    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()

    private val _error = MutableStateFlow<String?>(null)
    val error: StateFlow<String?> = _error.asStateFlow()

    private val _babyAgeWeeks = MutableStateFlow<String>("")
    val babyAgeWeeks: StateFlow<String> = _babyAgeWeeks.asStateFlow()

    init {
        observeSelectedBabyFromDataStore()
        fetchCurrentList()
        observeCheckboxStates()
    }

    private fun observeSelectedBabyFromDataStore() {
        viewModelScope.launch {
            babyPreferencesRepository.currentBabyAgeMonthsFlow.collect { savedBabyAgeMonths ->
                if (savedBabyAgeMonths != null) {
                    _babyAgeWeeks.value = savedBabyAgeMonths
                }
            }
        }
    }

    fun fetchCurrentList() {
        viewModelScope.launch {
            _isLoading.value = true
            _error.value = null

            try {
                val userId = auth.currentUser?.uid ?: run {
                    _error.value = "User not authenticated"
                    _isLoading.value = false
                    return@launch
                }

                val querySnapshot = firestore.collection("checklists").get().await()
                if (querySnapshot.isEmpty) {
                    _topicGroups.value = emptyList()
                    _isLoading.value = false
                    return@launch
                }

                val allItems = mutableListOf<ChecklistItem>()

                for (document in querySnapshot.documents) {
                    val data = document.data ?: continue
                    
                    // Manejo de tipos robusto para months y topic
                    val rootTopic = data["topic"]?.toString() ?: ""
                    val rootMonths = when (val m = data["months"]) {
                        is Long -> m.toInt()
                        is String -> m.toIntOrNull() ?: 0
                        else -> 0
                    }
                    val role = data["role"]?.toString() ?: ""

                    var index = 1
                    while (data.containsKey(index.toString())) {
                        val itemData = data[index.toString()] as? Map<*, *>
                        if (itemData != null) {
                            val itemId = "${document.id}_$index"
                            val itemText = itemData["item"]?.toString() ?: ""
                            val itemSubtopic = itemData["subtopic"]?.toString() ?: ""

                            allItems.add(ChecklistItem(
                                id = itemId,
                                item = itemText,
                                subtopic = itemSubtopic,
                                months = rootMonths,
                                topic = rootTopic,
                                role = role,
                                isChecked = false
                            ))
                        }
                        index++
                    }
                }

                val savedStates = loadCheckboxStates()
                val itemsWithStates = allItems.map { it.copy(isChecked = savedStates[it.id] ?: false) }

                // Agrupar por Tema + Meses + Rol para evitar mezclas
                val grouped = itemsWithStates.groupBy { "${it.topic}_${it.months}_${it.role}" }

                val topicGroups = grouped.map { (_, items) ->
                    val first = items.first()
                    val subtopicGroups = items.groupBy { it.subtopic }.map { (subtopic, subItems) ->
                        SubtopicGroup(
                            subtopic = subtopic,
                            items = subItems,
                            topic = first.topic,
                            months = first.months,
                            role = first.role
                        )
                    }.sortedBy { it.subtopic }

                    TopicGroup(
                        topic = first.topic,
                        months = first.months,
                        subtopicGroups = subtopicGroups,
                        role = first.role
                    )
                }.sortedWith(compareBy({ it.months }, { it.topic }))

                _topicGroups.value = topicGroups
                _isLoading.value = false

            } catch (e: Exception) {
                Log.e("CheckItemsViewModel", "Error loading checklists", e)
                _error.value = "Error al cargar checklists: ${e.message}"
                _isLoading.value = false
            }
        }
    }

    fun toggleItemChecked(itemId: String) {
        val currentGroups = _topicGroups.value
        val updatedGroups = currentGroups.map { topicGroup ->
            topicGroup.copy(
                subtopicGroups = topicGroup.subtopicGroups.map { subtopicGroup ->
                    subtopicGroup.copy(
                        items = subtopicGroup.items.map { item ->
                            if (item.id == itemId) item.copy(isChecked = !item.isChecked) else item
                        }
                    )
                }
            )
        }

        _topicGroups.value = updatedGroups
        
        // Buscar el nuevo estado para guardar
        updatedGroups.flatMap { it.subtopicGroups }.flatMap { it.items }
            .find { it.id == itemId }?.let { saveCheckboxState(itemId, it.isChecked) }
    }

    fun saveCheckboxState(itemId: String, checked: Boolean) {
        viewModelScope.launch {
            try {
                val userId = auth.currentUser?.uid ?: return@launch
                val babyId = babyPreferencesRepository.getSelectedBabyId() ?: return@launch
                val parts = itemId.split("_")
                if (parts.size != 2) return@launch

                val stateData = ChecklistItemState(
                    checklistDocId = parts[0],
                    itemIndex = parts[1].toIntOrNull() ?: 0,
                    checked = checked,
                    timestamp = System.currentTimeMillis()
                )

                firestore.collection("users").document(userId).collection("babies").document(babyId)
                    .collection("checklist_states").document(itemId).set(stateData).await()
            } catch (e: Exception) {
                Log.e("CheckItemsViewModel", "Error saving state", e)
            }
        }
    }

    private suspend fun loadCheckboxStates(): Map<String, Boolean> {
        return try {
            val userId = auth.currentUser?.uid ?: return emptyMap()
            val babyId = babyPreferencesRepository.getSelectedBabyId() ?: return emptyMap()

            val snapshot = firestore.collection("users").document(userId).collection("babies").document(babyId)
                .collection("checklist_states").get().await()

            snapshot.documents.mapNotNull { doc ->
                val state = doc.toObject(ChecklistItemState::class.java)
                if (state != null) "${state.checklistDocId}_${state.itemIndex}" to state.checked else null
            }.toMap()
        } catch (e: Exception) {
            emptyMap()
        }
    }

    private fun observeCheckboxStates() {
        viewModelScope.launch {
            try {
                val userId = auth.currentUser?.uid ?: return@launch
                val babyId = babyPreferencesRepository.getSelectedBabyId() ?: return@launch

                firestore.collection("users").document(userId).collection("babies").document(babyId)
                    .collection("checklist_states").addSnapshotListener { snapshot, _ ->
                        if (snapshot != null) {
                            val states = snapshot.documents.mapNotNull { doc ->
                                val state = doc.toObject(ChecklistItemState::class.java)
                                if (state != null) "${state.checklistDocId}_${state.itemIndex}" to state.checked else null
                            }.toMap()
                            applyCheckboxStates(states)
                        }
                    }
            } catch (e: Exception) {
                Log.e("CheckItemsViewModel", "Error observing", e)
            }
        }
    }

    private fun applyCheckboxStates(states: Map<String, Boolean>) {
        val updated = _topicGroups.value.map { topicGroup ->
            topicGroup.copy(
                subtopicGroups = topicGroup.subtopicGroups.map { subtopicGroup ->
                    subtopicGroup.copy(items = subtopicGroup.items.map { item -> item.copy(isChecked = states[item.id] ?: false) })
                }
            )
        }
        _topicGroups.value = updated
    }
}
