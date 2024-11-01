package com.ys.phdmama.ui.register

import android.util.Log
import android.widget.Toast
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import com.ys.phdmama.navigation.NavRoutes
import com.ys.phdmama.viewmodel.LoginViewModel
import com.ys.phdmama.viewmodel.RegisterViewModel

@Composable
fun RegisterScreen(
    navController: NavController,
    loginViewModel: LoginViewModel = viewModel(),
) {
    var email by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var repeatPassword by remember { mutableStateOf("") }
    var displayName by remember { mutableStateOf("") }
    var modifier: Modifier = Modifier
    val context = LocalContext.current
    val registerViewModel: RegisterViewModel = viewModel()

    Column(
        modifier = modifier
            .fillMaxWidth()
            .fillMaxHeight()
            .padding(16.dp),
        verticalArrangement = Arrangement.Center
    ) {

        OutlinedTextField(
            value = displayName,
            onValueChange = {
                displayName = it
                registerViewModel.updateUserField("displayName", it)
            },
            label = { Text("Display Name") },
            singleLine = true,
            modifier = modifier.fillMaxWidth(),
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Text)
        )

        Spacer(modifier = Modifier.height(16.dp))

        OutlinedTextField(
            value = email,
            onValueChange = {
                email = it
                registerViewModel.updateUserField("email", it)
            },
            label = { Text("Email") },
            singleLine = true,
            modifier = modifier.fillMaxWidth(),
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Email)
        )

        Spacer(modifier = Modifier.height(16.dp))

        OutlinedTextField(
            value = password,
            onValueChange = {
                password = it
                registerViewModel.updateUserField("password", it)
            },
            label = { Text("Password") },
            singleLine = true,
            visualTransformation = PasswordVisualTransformation(),
            modifier = modifier.fillMaxWidth(),
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password)
        )

        Spacer(modifier = Modifier.height(16.dp))

        OutlinedTextField(
            value = repeatPassword,
            onValueChange = {
                repeatPassword = it
                registerViewModel.updateUserField("repeatPassword", it)
            },
            label = { Text("Repeat Password") },
            singleLine = true,
            visualTransformation = PasswordVisualTransformation(),
            modifier = modifier.fillMaxWidth(),
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password)
        )

        Spacer(modifier = Modifier.height(24.dp))

        Button(
            onClick = {
                registerViewModel.signUpWithEmailPassword(
                    onSuccess = {
                        val uid = loginViewModel.getCurrentUserUid().orEmpty()
                        val userEmail = loginViewModel.getCurrentUserEmail().orEmpty()
                        val userDisplayName = loginViewModel.getCurrentUserDisplayName().orEmpty()
                        loginViewModel.onUserLoggedIn(uid, userEmail, userDisplayName) {
                            navController.navigate(NavRoutes.BABY_STATUS) {
                                popUpTo(NavRoutes.REGISTER) { inclusive = true }
                            }
                        }
                    },
                    onError = { errorMessage ->
                        Toast.makeText(context, errorMessage, Toast.LENGTH_SHORT).show()
                    }
                )
            },
            modifier = modifier.fillMaxWidth()
        ) {
            Text(text = "Sign Up")
        }
    }

}