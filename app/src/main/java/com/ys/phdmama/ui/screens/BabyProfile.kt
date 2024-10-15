package com.ys.phdmama.ui.screens

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.navigation.NavController
import com.ys.phdmama.ui.theme.PhdmamaTheme

class BabyProfileActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            PhdmamaTheme {
//                BabyProfileScreen()
            }
        }
    }
}

@Composable
fun BabyProfileScreen(navController: NavController) {
    Text("Pantalla de Perfil del Bebé")
}