package com.ys.phdmama.ui.login

import android.content.Intent
import android.os.Bundle
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.viewModels
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInAccount
import com.google.android.gms.auth.api.signin.GoogleSignInOptions
import com.ys.phdmama.R
import com.ys.phdmama.ui.main.MainActivity
import com.ys.phdmama.viewmodel.SignInViewModel

class LoginActivity : ComponentActivity() {

    private lateinit var googleSignInLauncher: ActivityResultLauncher<Intent>
    private val viewModel: SignInViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // Configurar Google SignIn
        val gso = GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
            .requestIdToken(getString(R.string.default_web_client_id))
            .requestEmail()
            .build()

        val googleSignInClient = GoogleSignIn.getClient(this, gso)

        // Inicializar launcher para manejar el resultado del SignIn de Google
        googleSignInLauncher = registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
            val task = GoogleSignIn.getSignedInAccountFromIntent(result.data)
            handleGoogleSignInResult(task.result)
        }

        setContent {
            LoginScreen(
                onSignInWithEmail = { email, password ->
//                    signInWithEmail(email, password)
                },
                onSignUp = {
//                    startActivity(Intent(this, RegisterActivity::class.java))
                },
                onSignInWithGoogle = {
                    val signInIntent = googleSignInClient.signInIntent
                    googleSignInLauncher.launch(signInIntent)
                }
            )
        }
    }

    // Manejar el resultado de Google SignIn
    private fun handleGoogleSignInResult(account: GoogleSignInAccount?) {
        account?.let {
            viewModel.onSignInWithGoogle(
                account = it,
                onSuccess = {
                    startActivity(Intent(this, MainActivity::class.java))
                    finish()
                },
                onError = { message ->
                    Toast.makeText(this, message, Toast.LENGTH_SHORT).show()
                }
            )
        }
    }
}




