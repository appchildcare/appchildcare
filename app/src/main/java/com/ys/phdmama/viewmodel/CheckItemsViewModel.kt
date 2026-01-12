package com.ys.phdmama.viewmodel

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await
import javax.inject.Inject

@HiltViewModel
class CheckItemsViewModel @Inject constructor() : ViewModel() {
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

    init {
        fetchCurrentList()
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

                // Fetch all documents from the checklists collection
                val querySnapshot = firestore
                    .collection("checklists")
                    .get()
                    .await()

                if (querySnapshot.isEmpty) {
                    _topicGroups.value = emptyList()
                    _isLoading.value = false
                    return@launch
                }

                // Parse all items from all documents
                val allItems = mutableListOf<Pair<ChecklistItem, Pair<String, Int>>>()

                Log.d("CheckItemsViewModel", "Found ${querySnapshot.documents.size} documents")

                // Iterate through all documents
                for (document in querySnapshot.documents) {
                    val data = document.data ?: continue

                    Log.d("CheckItemsViewModel", "Processing document: ${document.id}")

                    // Get parent-level topic and months from the document
                    val parentTopic = data["topic"] as? String ?: ""
                    val parentMonths = (data["months"] as? Long)?.toInt() ?: 0

                    Log.d("CheckItemsViewModel", "Parent topic: $parentTopic, Parent months: $parentMonths")

                    // Iterate through numbered fields (1, 2, 3, etc.) in each document
                    var index = 1
                    var itemsInDoc = 0
                    while (data.containsKey(index.toString())) {
                        val itemData = data[index.toString()] as? Map<*, *>
                        if (itemData != null) {
                            // Create unique ID combining document ID and field index
                            val itemId = "${document.id}_$index"
                            val item = ChecklistItem(
                                id = itemId,
                                item = itemData["item"] as? String ?: "",
                                subtopic = itemData["subtopic"] as? String ?: "",
                                months = (itemData["months"] as? Long)?.toInt() ?: 0,
                                topic = itemData["topic"] as? String ?: "",
                                isChecked = false
                            )
                            // Store item with its parent-level data
                            allItems.add(item to (parentTopic to parentMonths))
                            itemsInDoc++
                        }
                        index++
                    }
                    Log.d("CheckItemsViewModel", "Found $itemsInDoc items in document ${document.id}")
                }

                // Group items by subtopic and include parent-level data
                val subtopicGroups = allItems
                    .groupBy { it.first.subtopic }
                    .map { (subtopic, itemsWithParentData) ->
                        // Get parent data from the first item in the group
                        val (parentTopic, parentMonths) = itemsWithParentData.firstOrNull()?.second ?: ("" to 0)
                        SubtopicGroup(
                            subtopic = subtopic,
                            items = itemsWithParentData.map { it.first },
                            topic = parentTopic,
                            months = parentMonths
                        )
                    }
                    .sortedBy { it.subtopic }

                // Group subtopics by their parent topic
                val topicGroups = subtopicGroups
                    .groupBy { it.topic }
                    .map { (topic, subtopics) ->
                        TopicGroup(
                            topic = topic,
                            months = subtopics.firstOrNull()?.months ?: 0,
                            subtopicGroups = subtopics
                        )
                    }
                    .sortedBy { it.topic }

                _topicGroups.value = topicGroups
                _isLoading.value = false

                Log.d("CheckItemsViewModel", "Loaded ${topicGroups.size} topic groups")
                topicGroups.forEach { topicGroup ->
                    Log.d("CheckItemsViewModel", "Topic: ${topicGroup.topic}, Months: ${topicGroup.months}, Subtopics: ${topicGroup.subtopicGroups.size}")
                    topicGroup.subtopicGroups.forEach { subtopic ->
                        Log.d("CheckItemsViewModel", "  - Subtopic: ${subtopic.subtopic}, Items: ${subtopic.items.size}")
                    }
                }

            } catch (e: Exception) {
                _error.value = "Error loading checklists: ${e.message}"
                _isLoading.value = false
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
