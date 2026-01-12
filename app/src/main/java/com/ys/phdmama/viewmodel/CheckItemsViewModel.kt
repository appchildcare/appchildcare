package com.ys.phdmama.viewmodel

import androidx.lifecycle.SavedStateHandle
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
        val isChecked: Boolean? = false
    )

    // Group items by subtopic
    data class SubtopicGroup(
        val subtopic: String,
        val items: List<ChecklistItem>
    )

    // UI State
    private val _checklistItems = MutableStateFlow<List<SubtopicGroup>>(emptyList())
    val checklistItems: StateFlow<List<SubtopicGroup>> = _checklistItems.asStateFlow()

    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()

    private val _error = MutableStateFlow<String?>(null)
    val error: StateFlow<String?> = _error.asStateFlow()

    init {
        fetchCurrentList()
    }

    private fun fetchCurrentList() {
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
                    _checklistItems.value = emptyList()
                    _isLoading.value = false
                    return@launch
                }

                // Parse all items from all documents
                val allItems = mutableListOf<ChecklistItem>()

                // Iterate through all documents
                for (document in querySnapshot.documents) {
                    val data = document.data ?: continue

                    // Iterate through numbered fields (1, 2, 3, etc.) in each document
                    var index = 1
                    while (data.containsKey(index.toString())) {
                        val itemData = data[index.toString()] as? Map<*, *>
                        if (itemData != null) {
                            allItems.add(
                                ChecklistItem(
                                    item = itemData["item"] as? String ?: "",
                                    subtopic = itemData["subtopic"] as? String ?: "",
                                    months = (itemData["months"] as? Long)?.toInt() ?: 0,
                                    topic = itemData["topic"] as? String ?: ""
                                )
                            )
                        }
                        index++
                    }
                }

                // Group items by subtopic
                val groupedItems = allItems
                    .groupBy { it.subtopic }
                    .map { (subtopic, itemsList) ->
                        SubtopicGroup(subtopic, itemsList)
                    }
                    .sortedBy { it.subtopic }

                _checklistItems.value = groupedItems
                _isLoading.value = false

            } catch (e: Exception) {
                _error.value = "Error loading checklists: ${e.message}"
                _isLoading.value = false
            }
        }
    }

    // Toggle checkbox state for a specific item
    fun toggleItemChecked(itemId: String) {
        val currentItems = _checklistItems.value

        // Update the checked state for the specific item
        val updatedGroups = currentItems.map { group ->
            group.copy(
                items = group.items.map { item ->
                    if (item.id == itemId) {
                        item.copy(isChecked = item.isChecked)
                    } else {
                        item
                    }
                }
            )
        }

        _checklistItems.value = updatedGroups
    }
}
