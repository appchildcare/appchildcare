package com.ys.phdmama.viewmodel

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.ys.phdmama.model.ChecklistItemState
import com.ys.phdmama.repository.BabyPreferencesRepository
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

    // Data class for checklist items
    data class ChecklistItem(
        val id: String = "", // Unique identifier for the item
        val item: String = "",
        val subtopic: String = "",
        val months: Int = 0,
        val topic: String = "",
        val role: String = "",
        val isChecked: Boolean = false
    )

    // Group items by subtopic
    data class SubtopicGroup(
        val subtopic: String,
        val items: List<ChecklistItem>,
        val topic: String = "", // Parent level topic
        val months: Int = 0, // Parent level months
        val role: String = "",
    )

    // Group subtopics by topic
    data class TopicGroup(
        val topic: String,
        val months: Int,
        val role: String = "",
        val subtopicGroups: List<SubtopicGroup>
    )

    // UI State
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
            babyPreferencesRepository.currentBabyAgeMonthsFlow.collect { savedBabyAgeWeeks ->
                if (savedBabyAgeWeeks != null) {
                    // Find the baby in the current list
                    _babyAgeWeeks.value = savedBabyAgeWeeks
                }
            }
        }
    }

    fun fetchCurrentList() {
        viewModelScope.launch {
            _isLoading.value = true
            _error.value = null

            try {
                val userId = auth.currentUser?.uid
                if (userId == null) {
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

                    // Get root-level topic and months
                    val rootTopic = data["topic"] as? String ?: ""
                    val rootMonths = (data["months"] as? Long)?.toInt() ?: 0
                    val role = data["role"] as? String ?: ""


                    var index = 1
                    while (data.containsKey(index.toString())) {
                        val itemData = data[index.toString()] as? Map<*, *>

                        if (itemData != null) {
                            val itemId = "${document.id}_$index"
                            val itemText = itemData["item"] as? String ?: ""
                            val itemSubtopic = itemData["subtopic"] as? String ?: ""

                            val item = ChecklistItem(
                                id = itemId,
                                item = itemText,
                                subtopic = itemSubtopic,
                                months = rootMonths,
                                topic = rootTopic,
                                role = role,
                                isChecked = false
                            )

                            allItems.add(item)
                        }
                        index++
                    }
                }

                // Load saved checkbox states
                val savedStates = loadCheckboxStates()

                // Apply states to items
                val itemsWithStates = allItems.map { item ->
                    item.copy(isChecked = savedStates[item.id] ?: false)
                }

                // Group by topic + months
                val groupedByTopicAndMonths = itemsWithStates.groupBy { it.topic }

                val topicGroups = groupedByTopicAndMonths.map { (key, items) ->
                    val topic = items.first().topic
                    val months = items.first().months
                    val role = items.first().role

                    val subtopicGroups = items.groupBy { it.subtopic }.map { (subtopic, subtopicItems) ->
                        SubtopicGroup(
                            subtopic = subtopic,
                            items = subtopicItems,
                            topic = topic,
                            months = months,
                            role = items.first().role
                        )
                    }.sortedBy { it.subtopic }

                    TopicGroup(
                        topic = topic,
                        months = months,
                        subtopicGroups = subtopicGroups,
                        role = role
                    )
                }.sortedBy { "${it.topic}_${it.months}" }

                _topicGroups.value = topicGroups
                _isLoading.value = false

            } catch (e: Exception) {
                _error.value = "Error loading checklists: ${e.message}"
                _isLoading.value = false
            }
        }
    }


    // Toggle checkbox state for a specific item
    fun toggleItemChecked(itemId: String) {
        // Optimistically update UI first
        val updatedGroups = _topicGroups.value.map { topicGroup ->
            topicGroup.copy(
                subtopicGroups = topicGroup.subtopicGroups.map { subtopicGroup ->
                    subtopicGroup.copy(
                        items = subtopicGroup.items.map { item ->
                            if (item.id == itemId) {
                                item.copy(isChecked = !item.isChecked)
                            } else {
                                item
                            }
                        }
                    )
                }
            )
        }

        val newCheckedState = updatedGroups
            .flatMap { it.subtopicGroups }
            .flatMap { it.items }
            .find { it.id == itemId }
            ?.isChecked ?: false

        _topicGroups.value = updatedGroups

        // Save to Firestore
        saveCheckboxState(itemId, newCheckedState)
    }

    fun saveCheckboxState(itemId: String, checked: Boolean) {
        viewModelScope.launch {
            try {
                val userId = auth.currentUser?.uid
                if (userId == null) {
                    return@launch
                }

                // Get baby ID from DataStore
                val babyId = babyPreferencesRepository.getSelectedBabyId()
                if (babyId == null) {
                    _error.value = "No baby selected"
                    return@launch
                }

                // Parse itemId: "08eaPOJrd5hcSL1OTfAF_1" -> docId="08eaPOJrd5hcSL1OTfAF", index=1
                val parts = itemId.split("_")
                if (parts.size != 2) {
                    return@launch
                }

                val checklistDocId = parts[0]
                val itemIndex = parts[1].toIntOrNull() ?: 0

                val stateData = ChecklistItemState(
                    checklistDocId = checklistDocId,
                    itemIndex = itemIndex,
                    checked = checked,
                    timestamp = System.currentTimeMillis()
                )

                // Save to: /users/{userId}/babies/{babyId}/checklist_states/{itemId}
                firestore.collection("users")
                    .document(userId)
                    .collection("babies")
                    .document(babyId)
                    .collection("checklist_states")
                    .document(itemId)
                    .set(stateData)
                    .await()

            } catch (e: Exception) {
                _error.value = "Failed to save: ${e.message}"
            }
        }
    }

    // Load all checkbox states for current baby
    private suspend fun loadCheckboxStates(): Map<String, Boolean> {
        return try {
            val userId = auth.currentUser?.uid
            val babyId = babyPreferencesRepository.getSelectedBabyId()

            if (userId == null || babyId == null) {
                return emptyMap()
            }

            val snapshot = firestore.collection("users")
                .document(userId)
                .collection("babies")
                .document(babyId)
                .collection("checklist_states")
                .get()
                .await()

            val states = snapshot.documents.mapNotNull { doc ->
                val state = doc.toObject(ChecklistItemState::class.java)
                if (state != null) {
                    val itemId = "${state.checklistDocId}_${state.itemIndex}"
                    itemId to state.checked
                } else {
                    null
                }
            }.toMap()

            states

        } catch (e: Exception) {
            Log.e("CheckItemsViewModel", "Error loading checkbox states", e)
            emptyMap()
        }
    }

    // Listen to real-time checkbox state changes
    private fun observeCheckboxStates() {
        viewModelScope.launch {
            try {
                val userId = auth.currentUser?.uid
                val babyId = babyPreferencesRepository.getSelectedBabyId()

                if (userId == null || babyId == null) {
                    Log.w("CheckItemsViewModel", "Cannot observe: user=$userId, baby=$babyId")
                    return@launch
                }

                firestore.collection("users")
                    .document(userId)
                    .collection("babies")
                    .document(babyId)
                    .collection("checklist_states")
                    .addSnapshotListener { snapshot, error ->
                        if (error != null) {
                            Log.e("CheckItemsViewModel", "Error observing states", error)
                            return@addSnapshotListener
                        }

                        if (snapshot != null) {
                            val states = snapshot.documents.mapNotNull { doc ->
                                val state = doc.toObject(ChecklistItemState::class.java)
                                if (state != null) {
                                    val itemId = "${state.checklistDocId}_${state.itemIndex}"
                                    itemId to state.checked
                                } else {
                                    null
                                }
                            }.toMap()

                            applyCheckboxStates(states)
                        }
                    }
            } catch (e: Exception) {
                Log.e("CheckItemsViewModel", "Error setting up observer", e)
            }
        }
    }

    // Apply checkbox states to current topic groups
    private fun applyCheckboxStates(states: Map<String, Boolean>) {
        val updated = _topicGroups.value.map { topicGroup ->
            topicGroup.copy(
                subtopicGroups = topicGroup.subtopicGroups.map { subtopicGroup ->
                    subtopicGroup.copy(
                        items = subtopicGroup.items.map { item ->
                            item.copy(isChecked = states[item.id] ?: false)
                        }
                    )
                }
            )
        }
        _topicGroups.value = updated
    }
}
