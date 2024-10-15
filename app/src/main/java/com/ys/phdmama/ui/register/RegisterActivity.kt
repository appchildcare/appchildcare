//package com.ys.phdmama.ui.register
//
//import android.widget.Toast
//import androidx.compose.foundation.layout.*
//import androidx.compose.foundation.text.KeyboardOptions
//import androidx.compose.material3.*
//import androidx.compose.runtime.*
//import androidx.compose.ui.Modifier
//import androidx.compose.ui.platform.LocalContext
//import androidx.compose.ui.text.input.KeyboardType
//import androidx.compose.ui.text.input.PasswordVisualTransformation
//import androidx.compose.ui.unit.dp
//import androidx.lifecycle.viewmodel.compose.viewModel
//import androidx.navigation.NavController
//import com.ys.phdmama.viewmodel.LoginViewModel
//
//@Composable
//fun RegisterScreen(
//    navController: NavController,
//    loginViewModel: LoginViewModel = viewModel()
//) {
//    var email by remember { mutableStateOf("") }
//    var password by remember { mutableStateOf("") }
//    var repeatPassword by remember { mutableStateOf("") }
//    var modifier: Modifier = Modifier
//    val context = LocalContext.current
//
//    Column(
//        modifier = modifier
//            .fillMaxWidth()
//            .fillMaxHeight()
//            .padding(16.dp),
//        verticalArrangement = Arrangement.Center
//    ) {
//        OutlinedTextField(
//            value = email,
//            onValueChange = { email = it },
//            label = { Text("Email") },
//            singleLine = true,
//            modifier = modifier.fillMaxWidth(),
//            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Email)
//        )
//
//        Spacer(modifier = Modifier.height(16.dp))
//
//        OutlinedTextField(
//            value = password,
//            onValueChange = { password = it },
//            label = { Text("Password") },
//            singleLine = true,
//            visualTransformation = PasswordVisualTransformation(),
//            modifier = modifier.fillMaxWidth(),
//            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password)
//        )
//
//        Spacer(modifier = Modifier.height(16.dp))
//
//        OutlinedTextField(
//            value = repeatPassword,
//            onValueChange = { repeatPassword = it },
//            label = { Text("Repeat Password") },
//            singleLine = true,
//            visualTransformation = PasswordVisualTransformation(),
//            modifier = modifier.fillMaxWidth(),
//            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password)
//        )
//
//        Spacer(modifier = Modifier.height(24.dp))
//
//        Button(
//            onClick = {
//                if (password == repeatPassword) {
//                    loginViewModel.signUpWithEmailPassword(
//                        email = email,
//                        password = password,
//                        onSuccess = {
//                            navController.naviqgate("main") {
//                                popUpTo("register") { inclusive = true }
//                            }
//                        },
//                        onError = { errorMessage ->
//                            Toast.makeText(context, errorMessage, Toast.LENGTH_SHORT).show()
//                        }
//                    )
//                } else {
//                    Toast.makeText(context, "Las contraseñas no coinciden", Toast.LENGTH_SHORT).show()
//                }
//            },
//            modifier = modifier.fillMaxWidth()
//        ) {
//            Text(text = "Sign Up")
//        }
//    }
//}