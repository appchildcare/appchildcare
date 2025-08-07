package com.ys.phdmama.ui.login


import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
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
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.ys.phdmama.R
import com.ys.phdmama.navigation.NavRoutes
import com.ys.phdmama.ui.theme.primaryGray
import com.ys.phdmama.ui.theme.primaryTeal
import com.ys.phdmama.ui.theme.secondaryCream
import com.ys.phdmama.viewmodel.LoginViewModel
import com.ys.phdmama.viewmodel.WizardViewModel
import com.ys.phdmama.viewmodel.WizardViewModelFactory

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun LoginScreen(
    navController: NavController,
    modifier: Modifier = Modifier,
    loginViewModel: LoginViewModel = viewModel()
) {
    val context = LocalContext.current
    val email by loginViewModel.email.collectAsState()
    val password by loginViewModel.password.collectAsState()
    val wizardViewModel: WizardViewModel = viewModel(
        factory = WizardViewModelFactory()
    )
    var isGoogleLoading by remember { mutableStateOf(false) }
    var isEmailLoading by remember { mutableStateOf(false) }


    // Configure the Google Sign-In launcher
    val launcher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.StartActivityForResult()
    ) { result ->
        val task = GoogleSignIn.getSignedInAccountFromIntent(result.data)
        try {
            val account = task.getResult(Exception::class.java)
            if (account != null) {
                loginViewModel.fetchUserDetails(
                    onSuccess = { role ->
                        // Verificamos el rol y redirigimos
                        val destination = if (role == "born") {
                            NavRoutes.BORN_DASHBOARD
                        } else {
                            NavRoutes.WAITING_DASHBOARD
                        }
                        navController.navigate(destination) {
                            popUpTo(0) { inclusive = true }
                        }
                    },
                    onSkip = {
                        // Si no existe el rol o wizardFinished es false, vamos a BABY_STATUS
                        navController.navigate(NavRoutes.BABY_STATUS) {
                            popUpTo(0) { inclusive = true }
                        }
                    },
                    onError = { errorMessage ->
                        Toast.makeText(context, errorMessage, Toast.LENGTH_SHORT).show()
                    }
                )

            }
        } catch (e: Exception) {
            Toast.makeText(context, "Google Sign-In falló: ${e.localizedMessage}", Toast.LENGTH_SHORT).show()
        }
    }

    Column(
        modifier = modifier
            .fillMaxWidth()
            .fillMaxHeight()
            .background(secondaryCream)
            .verticalScroll(rememberScrollState()),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        Image(
            painter = painterResource(id = R.mipmap.ilustraciones_login),
            contentDescription = "Auth image",
            modifier = modifier
                .fillMaxWidth()
                .padding(16.dp, 4.dp)
                .height(380.dp)
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
                    BorderStroke(width = 2.dp, color = primaryGray),
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
                    BorderStroke(width = 2.dp, color = primaryGray),
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
                isEmailLoading = true
                loginViewModel.onSignInWithEmailPassword(
                    onSuccess = {
                        loginViewModel.fetchUserDetails(
                            onSuccess = { role ->
                                val destination = when (role) {
                                    "born" -> NavRoutes.BORN_DASHBOARD
                                    "waiting" -> NavRoutes.WAITING_DASHBOARD
                                    else -> NavRoutes.BABY_STATUS
                                }
                                navController.navigate(destination) {
                                    popUpTo(0) { inclusive = true }
                                }
                            },
                            onSkip = {
                                navController.navigate(NavRoutes.BABY_STATUS) {
                                    popUpTo(0) { inclusive = true }
                                }
                            },
                            onError = { errorMessage ->
                                Toast.makeText(context, errorMessage, Toast.LENGTH_SHORT).show()
                            }
                        )
                    },
                    onError = { errorMessage ->
                        isEmailLoading = false
                        Toast.makeText(context, errorMessage, Toast.LENGTH_SHORT).show()
                    }
                )
            },
            modifier = modifier.fillMaxWidth().padding(16.dp, 0.dp),
            enabled = !isEmailLoading
        ) {
            if (isEmailLoading) {
                CircularProgressIndicator(color = Color.White)
            } else {
                Text(text = "Iniciar Sesión")
            }
        }

        Spacer(modifier = Modifier
            .fillMaxWidth()
            .padding(4.dp))

        Text(text = "O",
            style = MaterialTheme.typography.labelLarge,
            color = primaryGray
        )

        Spacer(modifier = Modifier
            .fillMaxWidth()
            .padding(4.dp))
// HAY QUE HACERLE EL MISMO TRABAJITO DE LAS PANTALLAS ASYNC en el botón de Waiting en la dulce espera
        // Botón de inicio de sesión con Google
        Button(
            onClick = {
                isGoogleLoading = true
                try {
                    val signInClient = loginViewModel.getGoogleSignInClient(context)
                    launcher.launch(signInClient.signInIntent)
                } catch (e: Exception) {
                    isGoogleLoading = false
                    Toast.makeText(context, "Error al iniciar Google Sign-In", Toast.LENGTH_SHORT).show()
                }
            },
            colors = ButtonDefaults.buttonColors(containerColor = primaryTeal),
            modifier = modifier
                .fillMaxWidth()
                .padding(16.dp, 0.dp),
            enabled = !isGoogleLoading
        ) {
            if (isGoogleLoading) {
                CircularProgressIndicator(color = Color.White)
            } else {
                Text(text = "Iniciar sesión con Google")
            }
        }


        Spacer(modifier = Modifier
            .fillMaxWidth()
            .padding(8.dp))

        // Link para ir a la pantalla de registro
        TextButton(onClick = {
            navController.navigate("register")
        }) {
            Text(text = "No tienes cuenta? Regístrate aquí", style = MaterialTheme.typography.labelSmall)
        }
    }
}
