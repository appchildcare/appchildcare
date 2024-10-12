package com.ys.phdmama.ui

import android.content.Intent
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.size
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.lifecycle.lifecycleScope
import com.google.firebase.auth.FirebaseAuth
import com.ys.phdmama.ui.login.LoginActivity
import com.ys.phdmama.ui.main.MainActivity
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

class SplashScreen : ComponentActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            SplashScreenView(onFinish = { isAuthenticated ->
                if (isAuthenticated) {
                    // Si hay un usuario autenticado, navega a MainActivity
                    startActivity(Intent(this, MainActivity::class.java))
                } else {
                    // Si no, navega a LoginActivity
                    startActivity(Intent(this, LoginActivity::class.java))
                }
                finish() // Cierra la SplashScreen
            })
        }
    }
}

@Composable
fun SplashScreenView(onFinish: (Boolean) -> Unit) {
    // Estado local para verificar autenticación
    var isAuthenticated by remember { mutableStateOf(false) }

    LaunchedEffect(Unit) {
        // Simulación de un retraso para mostrar el SplashScreen (2 segundos)
        delay(2000)

        // Verificación del usuario autenticado
        val currentUser = FirebaseAuth.getInstance().currentUser
        isAuthenticated = currentUser != null

        // Una vez validado, ejecuta el callback para terminar el SplashScreen
        onFinish(isAuthenticated)
    }

    // Interfaz de la pantalla Splash
    Box(
        modifier = Modifier.fillMaxSize(),
        contentAlignment = Alignment.Center
    ) {
        CircularProgressIndicator(modifier = Modifier.size(50.dp))
    }
}
