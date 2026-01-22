package com.ys.phdmama.model

data class ChecklistItemState(
    val checklistDocId: String = "",
    val itemIndex: Int = 0,
    val checked: Boolean = false,
    val timestamp: Long = System.currentTimeMillis()
)
