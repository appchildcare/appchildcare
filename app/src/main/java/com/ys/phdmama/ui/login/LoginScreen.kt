package com.ys.phdmama.ui.login

import android.app.PendingIntent
import android.util.Log
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
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
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import com.google.android.gms.auth.api.identity.Identity
import com.ys.phdmama.R
import com.ys.phdmama.viewmodel.LoginViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun LoginScreen(
    navController: NavController,
    modifier: Modifier = Modifier,
    loginViewModel: LoginViewModel = viewModel()
) {
    val email by loginViewModel.email.collectAsState()
    val password by loginViewModel.password.collectAsState()
    val context = LocalContext.current

    // Configure the Google Sign-In launcher
    val launcher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.StartIntentSenderForResult()
    ) { result ->
        if (result.resultCode == android.app.Activity.RESULT_OK) {
            val credential = Identity.getSignInClient(context).getSignInCredentialFromIntent(result.data)
            val idToken = credential.googleIdToken
            if (idToken != null) {
                loginViewModel.signInWithGoogle(
                    idToken = idToken,
                    onSuccess = {
                        Log.d("LoginScreen", "Navegando a la pantalla principal")
                        navController.navigate("main") {
                            popUpTo("login") { inclusive = true }
                        }
                    },
                    onError = { errorMessage ->
                        Log.e("LoginScreen", "Error al iniciar sesión con Google: $errorMessage")
                        Toast.makeText(context, errorMessage, Toast.LENGTH_SHORT).show()
                    }
                )
            } else {
                Toast.makeText(context, "Error: No se pudo obtener el token de Google", Toast.LENGTH_SHORT).show()
            }
        } else {
            Toast.makeText(context, "Inicio de sesión cancelado", Toast.LENGTH_SHORT).show()
        }
    }

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

        // Campo de correo electrónico
        OutlinedTextField(
            singleLine = true,
            value = email,
            onValueChange = { loginViewModel.onEmailChange(it) },
            placeholder = { Text("Correo Electrónico") },
            leadingIcon = { Icon(imageVector = Icons.Default.Email, contentDescription = "Email") },
            colors = TextFieldDefaults.outlinedTextFieldColors(
                focusedBorderColor = Color.Transparent,
                unfocusedBorderColor = Color.Transparent
            ),
            modifier = modifier
                .fillMaxWidth()
                .padding(16.dp, 4.dp)
                .border(
                    BorderStroke(width = 2.dp, color = Color(0xFF6200EE)),
                    shape = RoundedCornerShape(50)
                ),
        )

        // Campo de contraseña
        OutlinedTextField(
            singleLine = true,
            modifier = modifier
                .fillMaxWidth()
                .padding(16.dp, 4.dp)
                .border(
                    BorderStroke(width = 2.dp, color = Color(0xFF6200EE)),
                    shape = RoundedCornerShape(50)
                ),
            colors = TextFieldDefaults.outlinedTextFieldColors(
                focusedBorderColor = Color.Transparent,
                unfocusedBorderColor = Color.Transparent
            ),
            value = password,
            onValueChange = { loginViewModel.onPasswordChange(it) },
            placeholder = { Text("Contraseña") },
            leadingIcon = { Icon(imageVector = Icons.Default.Lock, contentDescription = "Password") },
            visualTransformation = PasswordVisualTransformation()
        )

        Spacer(modifier = Modifier
            .fillMaxWidth()
            .padding(12.dp))

        // Botón de inicio de sesión con email
        Button(
            onClick = {
                loginViewModel.onSignInWithEmailPassword(
                    onSuccess = {
                        navController.navigate("main") {
                            popUpTo("login") { inclusive = true }
                        }
                    },
                    onError = { errorMessage ->
                        Toast.makeText(context, errorMessage, Toast.LENGTH_SHORT).show()
                    }
                )
            },
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

        // Botón de inicio de sesión con Google
        Button(
            onClick = {
                loginViewModel.initGoogleSignIn(
                    context = context,
                    onSuccess = { pendingIntent : PendingIntent ->
                        launcher.launch(
                            androidx.activity.result.IntentSenderRequest.Builder(pendingIntent).build()
                        )
                    },
                    onError = { errorMessage ->
                        Toast.makeText(context, errorMessage, Toast.LENGTH_SHORT).show()
                    }
                )
            },
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
        TextButton(onClick = {
            navController.navigate("register")
        }) {
            Text(text = "No tienes cuenta? Regístrate aquí", fontSize = 16.sp, color = Color(0xFF6200EE))
        }
    }
}
