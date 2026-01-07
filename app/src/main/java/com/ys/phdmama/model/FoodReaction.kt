package com.ys.phdmama.model

data class FoodReaction(
    val id: String = "",
    val foodName: String = "",
    val hasReaction: Boolean = false,
    val reactionDetail: String = "",
    val date: String = ""
)
