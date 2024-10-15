package com.ys.phdmama.ui.screens

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import com.ys.phdmama.ui.theme.PhdmamaTheme

class ChecklistPreBirthActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            PhdmamaTheme {
                ChecklistPreBirthScreen()
            }
        }
    }
}

@Composable
fun ChecklistPreBirthScreen() {
    Text("Pantalla de Lista de Verificación Preparto")
}