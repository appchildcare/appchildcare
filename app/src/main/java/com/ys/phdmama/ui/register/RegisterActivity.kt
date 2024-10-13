package com.ys.phdmama.ui.register

import android.os.Bundle
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import com.google.firebase.auth.FirebaseAuth

class RegisterActivity : ComponentActivity() {
    private lateinit var auth: FirebaseAuth

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // start Firebase Auth
        auth = FirebaseAuth.getInstance()

        setContent {
            RegisterScreen(
                onSignUpClick = { email, password, repeatPassword ->
                    if (password == repeatPassword) {
                        signUpWithEmail(email, password)
                    } else {
                        Toast.makeText(this, "Passwords do not match", Toast.LENGTH_SHORT).show()
                    }
                }
            )
        }
    }

    // Register with email & password
    private fun signUpWithEmail(email: String, password: String) {
        auth.createUserWithEmailAndPassword(email, password)
            .addOnCompleteListener(this) { task ->
                if (task.isSuccessful) {
                    // Succesful register
                    Toast.makeText(this, "Registro exitoso", Toast.LENGTH_SHORT).show()
                    finish() // Regresar a la actividad anterior o Login
                } else {
                    // On error, show it
                    Toast.makeText(this, "Registro fallido: ${task.exception?.message}", Toast.LENGTH_SHORT).show()
                }
            }
    }
}