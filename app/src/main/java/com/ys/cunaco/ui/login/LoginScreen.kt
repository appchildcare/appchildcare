package com.ys.cunaco.ui.login

import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.ClickableText
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Email
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.paint
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController
import com.google.android.gms.auth.api.signin.GoogleSignIn
import androidx.compose.ui.platform.LocalUriHandler
import com.ys.cunaco.R
import com.ys.cunaco.navigation.NavRoutes
import com.ys.cunaco.ui.theme.primaryGray
import com.ys.cunaco.ui.theme.primaryTeal
import com.ys.cunaco.ui.theme.secondaryCream
import com.ys.cunaco.viewmodel.LoginViewModel

private const val TERMS_TAG = "TERMS"
private const val PRIVACY_TAG = "PRIVACY"

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun LoginScreen(
    navController: NavController,
    modifier: Modifier = Modifier,
    // Scope to Activity to share state with Sidebar/MainActivity
    loginViewModel: LoginViewModel = hiltViewModel(LocalContext.current as ComponentActivity)
) {
    val context = LocalContext.current
    val uriHandler = LocalUriHandler.current
    val email by loginViewModel.email.collectAsState()
    val password by loginViewModel.password.collectAsState()
    var isGoogleLoading by remember { mutableStateOf(false) }
    var isEmailLoading by remember { mutableStateOf(false) }
    var acceptedTerms by rememberSaveable { mutableStateOf(false) }
    var showPrivacyDialog by rememberSaveable { mutableStateOf(false) }


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
                        navController.navigate(NavRoutes.BABY_STATUS) {
                            popUpTo(0) { inclusive = true }
                        }
                    },
                    onError = { errorMessage ->
                        Toast.makeText(context, errorMessage as CharSequence, Toast.LENGTH_SHORT).show()
                    }
                )

            }
        } catch (e: Exception) {
            val msg = "Google Sign-In falló: ${e.localizedMessage ?: ""}"
            Toast.makeText(context, msg as CharSequence, Toast.LENGTH_SHORT).show()
        }
    }

    if (showPrivacyDialog) {
        LegalDialog(
            title = stringResource(R.string.legal_privacy_title),
            text = stringResource(R.string.legal_privacy_text),
            onDismiss = { showPrivacyDialog = false }
        )
    }

    Column(
        modifier = modifier
            .fillMaxWidth()
            .fillMaxHeight()
            .background(secondaryCream)
            .paint(
                painter = painterResource(R.drawable.background1_sun),
                contentScale = ContentScale.Crop
            )
            .verticalScroll(rememberScrollState()),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally,

        ) {
        Image(
            painter = painterResource(id = R.drawable.mascota_juntos),
            contentDescription = "Auth image",
            modifier = modifier
                .fillMaxWidth()
                .padding(16.dp, 4.dp)
                .height(380.dp)
        )

        Spacer(modifier = Modifier
            .fillMaxWidth()
            .padding(12.dp))

        OutlinedTextField(
            singleLine = true,
            value = email,
            onValueChange = { loginViewModel.onEmailChange(it) },
            placeholder = { Text(stringResource(R.string.login_label_email)) },
            leadingIcon = { Icon(imageVector = Icons.Default.Email, contentDescription = "Email") },
            colors = TextFieldDefaults.outlinedTextFieldColors(
                focusedBorderColor = Color.White,
                unfocusedBorderColor = Color.White
            ),
            modifier = modifier
                .fillMaxWidth()
                .padding(16.dp, 4.dp)
                .background(color = Color.White)
                .border(
                    BorderStroke(width = 2.dp, color = primaryGray),
                    shape = RoundedCornerShape(50)
                ),
        )

        OutlinedTextField(
            singleLine = true,
            modifier = modifier
                .fillMaxWidth()
                .padding(16.dp, 4.dp)
                .background(color = Color.White)
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
            placeholder = { Text(stringResource(R.string.login_label_password)) },
            leadingIcon = { Icon(imageVector = Icons.Default.Lock, contentDescription = "Password") },
            visualTransformation = PasswordVisualTransformation()
        )

        Spacer(modifier = Modifier
            .fillMaxWidth()
            .padding(4.dp))

        val acceptPrefix = stringResource(R.string.legal_accept_prefix)
        val acceptAnd = stringResource(R.string.legal_accept_and)
        val termsTitle = stringResource(R.string.legal_terms_title)
        val privacyTitle = stringResource(R.string.legal_privacy_title)
        val termsUrl = stringResource(R.string.legal_terms_url)
        val linkStyle = SpanStyle(
            color = primaryTeal,
            fontWeight = FontWeight.Bold,
            textDecoration = TextDecoration.Underline
        )
        val termsAnnotated = buildAnnotatedString {
            append("$acceptPrefix ")
            pushStringAnnotation(tag = TERMS_TAG, annotation = TERMS_TAG)
            withStyle(linkStyle) { append(termsTitle) }
            pop()
            append(" $acceptAnd ")
            pushStringAnnotation(tag = PRIVACY_TAG, annotation = PRIVACY_TAG)
            withStyle(linkStyle) { append(privacyTitle) }
            pop()
        }

        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Checkbox(
                checked = acceptedTerms,
                onCheckedChange = { acceptedTerms = it },
                colors = CheckboxDefaults.colors(checkedColor = primaryTeal)
            )
            ClickableText(
                text = termsAnnotated,
                style = MaterialTheme.typography.bodySmall.copy(color = primaryGray),
                onClick = { offset ->
                    when {
                        termsAnnotated.getStringAnnotations(TERMS_TAG, offset, offset).isNotEmpty() ->
                            uriHandler.openUri(termsUrl)
                        termsAnnotated.getStringAnnotations(PRIVACY_TAG, offset, offset).isNotEmpty() ->
                            showPrivacyDialog = true
                        else -> acceptedTerms = !acceptedTerms
                    }
                }
            )
        }

        Spacer(modifier = Modifier
            .fillMaxWidth()
            .padding(4.dp))

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
                                isEmailLoading = false
                                Toast.makeText(context, errorMessage as CharSequence, Toast.LENGTH_SHORT).show()
                            }
                        )
                    },
                    onError = { uiText ->
                        isEmailLoading = false
                        Toast.makeText(context, uiText.asString(context) as CharSequence, Toast.LENGTH_SHORT).show()
                    }
                )
            },
            modifier = modifier.fillMaxWidth().padding(16.dp, 0.dp),
            enabled = acceptedTerms && !isEmailLoading
        ) {
            if (isEmailLoading) {
                CircularProgressIndicator(color = Color.White, modifier = Modifier.size(24.dp))
            } else {
                Text(text = stringResource(R.string.login_button_login))
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

        val errorText = stringResource(R.string.login_button_error)
        Button(
            onClick = {
                isGoogleLoading = true
                try {
                    val signInClient = loginViewModel.getGoogleSignInClient(context)
                    launcher.launch(signInClient.signInIntent)
                } catch (e: Exception) {
                    isGoogleLoading = false
                    Toast.makeText(context, errorText as CharSequence, Toast.LENGTH_SHORT).show()
                }
            },
            colors = ButtonDefaults.buttonColors(containerColor = primaryTeal),
            modifier = modifier
                .fillMaxWidth()
                .padding(16.dp, 0.dp),
            enabled = acceptedTerms && !isGoogleLoading
        ) {
            if (isGoogleLoading) {
                CircularProgressIndicator(color = Color.White, modifier = Modifier.size(24.dp))
            } else {
                Text(text = stringResource(R.string.login_button_login_google))
            }
        }

        Spacer(modifier = Modifier
            .fillMaxWidth()
            .padding(8.dp))

        TextButton(onClick = {
            navController.navigate("register")
        }) {
            Text(text = stringResource(R.string.login_button_sign_up), style = MaterialTheme.typography.labelSmall)
        }
    }
}
