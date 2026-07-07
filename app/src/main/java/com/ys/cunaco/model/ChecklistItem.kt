package com.ys.cunaco.model

data class ChecklistItemState(
    val checklistDocId: String = "",
    val itemIndex: Int = 0,
    val checked: Boolean = false,
    val timestamp: Long = System.currentTimeMillis()
)
