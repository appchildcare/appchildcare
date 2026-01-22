package com.ys.phdmama.viewmodel

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
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
        val isChecked: Boolean = false
    )

    // Group items by subtopic
    data class SubtopicGroup(
        val subtopic: String,
        val items: List<ChecklistItem>,
        val topic: String = "", // Parent level topic
        val months: Int = 0 // Parent level months
    )

    // Group subtopics by topic
    data class TopicGroup(
        val topic: String,
        val months: Int,
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
    }

    private fun observeSelectedBabyFromDataStore() {
        viewModelScope.launch {
            babyPreferencesRepository.currentBabyAgeMonthsFlow.collect { savedBabyAgeWeeks ->
                Log.d("CheckItemsViewModel", "DataStore changed, saved week months: $savedBabyAgeWeeks")

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
                Log.d("CheckItemsViewModel", "Found ${querySnapshot.documents.size} documents")

                for (document in querySnapshot.documents) {
                    val data = document.data ?: continue

                    // Get root-level topic and months
                    val rootTopic = data["topic"] as? String ?: ""
                    val rootMonths = (data["months"] as? Long)?.toInt() ?: 0

                    Log.d("CheckItemsViewModel", "Document: ${document.id}, root topic='$rootTopic', root months=$rootMonths")

                    var index = 1
                    while (data.containsKey(index.toString())) {
                        val itemData = data[index.toString()] as? Map<*, *>

                        if (itemData != null) {
                            val itemId = "${document.id}_$index"
                            val itemText = itemData["item"] as? String ?: ""
                            val itemSubtopic = itemData["subtopic"] as? String ?: ""

                            // Use root-level values for topic and months
                            val item = ChecklistItem(
                                id = itemId,
                                item = itemText,
                                subtopic = itemSubtopic,
                                months = rootMonths,  // From document root
                                topic = rootTopic,    // From document root
                                isChecked = false
                            )

                            allItems.add(item)
                            Log.d("CheckItemsViewModel", "  Item $index: topic='$rootTopic', months=$rootMonths")
                        }
                        index++
                    }
                }

                Log.d("CheckItemsViewModel", "Total items: ${allItems.size}")

                // Group by topic + months
                val groupedByTopicAndMonths = allItems.groupBy { "${it.topic}_${it.months}" }

                Log.d("CheckItemsViewModel", "Grouped into ${groupedByTopicAndMonths.size} topic+month combinations")

                val topicGroups = groupedByTopicAndMonths.map { (key, items) ->
                    val topic = items.first().topic
                    val months = items.first().months

                    Log.d("CheckItemsViewModel", "Creating group: topic='$topic', months=$months, items=${items.size}")

                    val subtopicGroups = items.groupBy { it.subtopic }.map { (subtopic, subtopicItems) ->
                        SubtopicGroup(
                            subtopic = subtopic,
                            items = subtopicItems,
                            topic = topic,
                            months = months
                        )
                    }.sortedBy { it.subtopic }

                    TopicGroup(
                        topic = topic,
                        months = months,
                        subtopicGroups = subtopicGroups
                    )
                }.sortedBy { "${it.topic}_${it.months}" }

                _topicGroups.value = topicGroups
                _isLoading.value = false

                Log.d("CheckItemsViewModel", "✅ Loaded ${topicGroups.size} topic groups")
                topicGroups.forEach { group ->
                    Log.d("CheckItemsViewModel", "  📋 '${group.topic}' - ${group.months} months - ${group.subtopicGroups.size} subtopics")
                }

            } catch (e: Exception) {
                _error.value = "Error loading checklists: ${e.message}"
                _isLoading.value = false
                Log.e("CheckItemsViewModel", "Error loading checklists", e)
            }
        }
    }

    // Toggle checkbox state for a specific item
    fun toggleItemChecked(itemId: String) {
        val currentTopicGroups = _topicGroups.value

        // Update the checked state for the specific item
        val updatedTopicGroups = currentTopicGroups.map { topicGroup ->
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

        _topicGroups.value = updatedTopicGroups
    }
}
