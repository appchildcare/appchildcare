package com.ys.phdmama.ui.register

import android.widget.Toast
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.paint
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import com.ys.phdmama.R
import com.ys.phdmama.navigation.NavRoutes
import com.ys.phdmama.ui.theme.primaryGray
import com.ys.phdmama.ui.theme.primaryTeal
import com.ys.phdmama.ui.theme.secondaryCream
import com.ys.phdmama.ui.theme.secondaryLightGray
import com.ys.phdmama.viewmodel.BabyDataViewModel
import com.ys.phdmama.viewmodel.LoginViewModel
import com.ys.phdmama.viewmodel.RegisterViewModel

@Composable
fun RegisterScreen(
    navController: NavController,
    loginViewModel: LoginViewModel = hiltViewModel(),
    babyDataViewModel: BabyDataViewModel = hiltViewModel()
) {
    var email by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var repeatPassword by remember { mutableStateOf("") }
    var displayName by remember { mutableStateOf("") }
    val modifier: Modifier = Modifier
    val context = LocalContext.current
    val registerViewModel: RegisterViewModel = hiltViewModel()

    Column(
        modifier = modifier
            .fillMaxWidth()
            .fillMaxHeight()
            .background(secondaryCream)
            .paint(
                painter = painterResource(R.drawable.background1_sun),
                contentScale = ContentScale.Crop
            )
            .padding(16.dp),
        verticalArrangement = Arrangement.Center
    ) {

        OutlinedTextField(
            value = displayName,
            colors = OutlinedTextFieldDefaults.colors(
                focusedBorderColor = primaryGray,
                unfocusedBorderColor = primaryGray,
                disabledBorderColor = secondaryLightGray,
                errorBorderColor = Color.Red
            ),
            onValueChange = {
                displayName = it
                registerViewModel.updateUserField("displayName", it)
            },
            label = { Text("Nombres") },
            singleLine = true,
            modifier = modifier.fillMaxWidth().background(color = Color.White),
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
            modifier = modifier.fillMaxWidth().background(color = Color.White),
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Email)
        )

        Spacer(modifier = Modifier.height(16.dp))

        OutlinedTextField(
            value = password,
            onValueChange = {
                password = it
                registerViewModel.updateUserField("password", it)
            },
            label = { Text("Contraseña") },
            singleLine = true,
            visualTransformation = PasswordVisualTransformation(),
            modifier = modifier.fillMaxWidth().background(color = Color.White),
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password)
        )

        Spacer(modifier = Modifier.height(16.dp))

        OutlinedTextField(
            value = repeatPassword,
            onValueChange = {
                repeatPassword = it
                registerViewModel.updateUserField("repeatPassword", it)
            },
            label = { Text("Repetir Contraseña") },
            singleLine = true,
            visualTransformation = PasswordVisualTransformation(),
            modifier = modifier.fillMaxWidth().background(color = Color.White),
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password)
        )

        Spacer(modifier = Modifier.height(24.dp))

        val isFormValid = displayName.isNotBlank() &&
                email.isNotBlank() &&
                password.isNotBlank() &&
                repeatPassword.isNotBlank()

        Image(
            painter = painterResource(id = R.drawable.mascota_ok),
            contentDescription = "Auth image",
            modifier = modifier
                .fillMaxWidth()
                .padding(16.dp, 4.dp)
                .height(180.dp)
        )

        Button(
            onClick = {
                registerViewModel.signUpWithEmailPassword(
                    onSuccess = {
                        val uid = loginViewModel.getCurrentUserUid().orEmpty()
                        val userEmail = loginViewModel.getCurrentUserEmail().orEmpty()
                        val userDisplayName = loginViewModel.getCurrentUserDisplayName().orEmpty()
                        loginViewModel.onUserLoggedIn(uid, userEmail, userDisplayName, babyDataViewModel) {
                            navController.navigate(NavRoutes.BABY_STATUS) {
                                popUpTo(0) { inclusive = true }
                            }
                        }
                    },
                    onError = { errorMessage ->
                        Toast.makeText(context, errorMessage, Toast.LENGTH_SHORT).show()
                    }
                )
            },
            colors = ButtonDefaults.buttonColors(containerColor = primaryTeal),
            modifier = modifier.fillMaxWidth(),
            enabled = isFormValid
        ) {
            Text(text = "Registrarse")
        }
    }
}