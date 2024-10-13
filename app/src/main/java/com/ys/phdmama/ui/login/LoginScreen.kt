package com.ys.phdmama.ui.login

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Email
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.ys.phdmama.R

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun LoginScreen(
    onSignInWithEmail: (String, String) -> Unit,
    onSignUp: () -> Unit,
    onSignInWithGoogle: () -> Unit,
    modifier: Modifier = Modifier
) {
    var email by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }

    Column(
        modifier = modifier
            .fillMaxWidth()
            .fillMaxHeight()
            .verticalScroll(rememberScrollState()),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Image(
            painter = painterResource(id = R.mipmap.auth_image),
            contentDescription = "Auth image",
            modifier = modifier
                .fillMaxWidth()
                .padding(16.dp, 4.dp)
        )

        Spacer(modifier = Modifier
            .fillMaxWidth()
            .padding(12.dp))

        // Campo para ingresar email
        OutlinedTextField(
            singleLine = true,
            modifier = modifier
                .fillMaxWidth()
                .padding(16.dp, 4.dp)
                .border(
                    BorderStroke(width = 2.dp, color = Color(0xFF6200EE)),
                    shape = RoundedCornerShape(50)
                ),
            colors = TextFieldDefaults.textFieldColors(
                containerColor = Color.Transparent,
                focusedIndicatorColor = Color.Transparent,
                unfocusedIndicatorColor = Color.Transparent
            ),
            value = email,
            onValueChange = { email = it },
            placeholder = { Text("Correo Electrónico") },
            leadingIcon = { Icon(imageVector = Icons.Default.Email, contentDescription = "Email") }
        )

        // Campo para ingresar la contraseña
        OutlinedTextField(
            singleLine = true,
            modifier = modifier
                .fillMaxWidth()
                .padding(16.dp, 4.dp)
                .border(
                    BorderStroke(width = 2.dp, color = Color(0xFF6200EE)),
                    shape = RoundedCornerShape(50)
                ),
            colors = TextFieldDefaults.textFieldColors(
                containerColor = Color.Transparent,
                focusedIndicatorColor = Color.Transparent,
                unfocusedIndicatorColor = Color.Transparent
            ),
            value = password,
            onValueChange = { password = it },
            placeholder = { Text("Contraseña") },
            leadingIcon = { Icon(imageVector = Icons.Default.Lock, contentDescription = "Password") },
            visualTransformation = PasswordVisualTransformation()
        )

        Spacer(modifier = Modifier
            .fillMaxWidth()
            .padding(12.dp))

        // Botón de inicio de sesión con correo y contraseña
        Button(
            onClick = { onSignInWithEmail(email, password) },
            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF6200EE)),
            modifier = modifier
                .fillMaxWidth()
                .padding(16.dp, 0.dp)
        ) {
            Text(
                text = "Iniciar Sesión",
                fontSize = 16.sp,
                modifier = modifier.padding(0.dp, 6.dp)
            )
        }

        Spacer(modifier = Modifier
            .fillMaxWidth()
            .padding(4.dp))

        Text(text = "O", fontSize = 16.sp, color = Color(0xFF6200EE))

        Spacer(modifier = Modifier
            .fillMaxWidth()
            .padding(4.dp))

        // Botón para iniciar sesión con Google
        Button(
            onClick = { onSignInWithGoogle() },
            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF4285F4)),
            modifier = modifier
                .fillMaxWidth()
                .padding(16.dp, 0.dp)
        ) {
            Text(
                text = "Iniciar sesión con Google",
                fontSize = 16.sp,
                modifier = modifier.padding(0.dp, 6.dp)
            )
        }

        Spacer(modifier = Modifier
            .fillMaxWidth()
            .padding(8.dp))

        // Link para ir a la pantalla de registro
        TextButton(onClick = { onSignUp() }) {
            Text(text = "No tienes cuenta? Regístrate aquí", fontSize = 16.sp, color = Color(0xFF6200EE))
        }
    }
}
