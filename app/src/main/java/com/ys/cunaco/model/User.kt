package com.ys.cunaco.model

data class User(
    val uid: String = "",
    val name: String = "",
    val email: String = "",
    val role: String = "" // "waiting" para "En la dulce espera", "born" para "Ya nació"
)
