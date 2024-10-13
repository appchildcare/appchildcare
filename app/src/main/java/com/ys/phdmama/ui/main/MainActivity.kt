package com.ys.phdmama.ui.main

import android.content.Intent
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.runtime.Composable
import com.google.firebase.auth.FirebaseAuth
import com.ys.phdmama.ui.login.LoginActivity

class MainActivity : ComponentActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            MainScreen(
                onSignOutClick = {
                    signOut()
                }
            )
        }
    }

    // Función para cerrar sesión
    private fun signOut() {
        FirebaseAuth.getInstance().signOut()
        // Redirigir a la pantalla de inicio de sesión después de cerrar sesión
        startActivity(Intent(this, LoginActivity::class.java))
        finish()
    }
}

@Composable
fun MainScreen(onSignOutClick: () -> Unit) {
    // Aquí puedes agregar el resto de la UI de tu MainScreen si es necesario
    ExitAppCard(onSignOutClick = onSignOutClick)
}
