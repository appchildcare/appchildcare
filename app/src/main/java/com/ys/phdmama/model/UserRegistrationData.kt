package com.ys.phdmama.model

data class UserRegistrationData(
    var email: String = "",
    var password: String = "",
    var repeatPassword: String = "",
    var displayName: String = ""
)
