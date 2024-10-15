//package com.ys.phdmama.ui.login
//
//import android.content.Intent
//import android.os.Bundle
//import android.widget.Toast
//import androidx.activity.ComponentActivity
//import androidx.activity.compose.setContent
//import androidx.activity.result.ActivityResultLauncher
//import androidx.activity.result.contract.ActivityResultContracts
//import androidx.activity.viewModels
//import com.google.android.gms.auth.api.signin.GoogleSignIn
//import com.google.android.gms.auth.api.signin.GoogleSignInAccount
//import com.google.android.gms.auth.api.signin.GoogleSignInOptions
//import com.google.firebase.auth.FirebaseAuth
//import com.ys.phdmama.R
//import com.ys.phdmama.ui.main.MainActivity
//import com.ys.phdmama.ui.register.RegisterScreen
//import com.ys.phdmama.viewmodel.LoginViewModel
//
//class LoginActivity : ComponentActivity() {
//    private lateinit var auth: FirebaseAuth
//
//    private lateinit var googleSignInLauncher: ActivityResultLauncher<Intent>
//    private val viewModel: LoginViewModel by viewModels()
//
//    override fun onCreate(savedInstanceState: Bundle?) {
//        super.onCreate(savedInstanceState)
//        auth = FirebaseAuth.getInstance()
//
//        // Google SignIn
//        val gso = GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
//            .requestIdToken(getString(R.string.default_web_client_id))
//            .requestEmail()
//            .build()
//
//        val googleSignInClient = GoogleSignIn.getClient(this, gso)
//
//        // launcher of SignIn Google
//        googleSignInLauncher = registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
//            val task = GoogleSignIn.getSignedInAccountFromIntent(result.data)
//            handleGoogleSignInResult(task.result)
//        }
//
//        setContent {
////            LoginScreen(
////                onSignInWithEmail = { email: String, password: String ->
////                    signInWithEmail(email, password)
////                },
////                onSignUp = {
////                    startActivity(Intent(this, RegisterActivity::class.java))
////                },
////                onSignInWithGoogle = {
////                    val signInIntent = googleSignInClient.signInIntent
////                    googleSignInLauncher.launch(signInIntent)
////                }
////            )
//        }
//    }
//
//    // Result Google SignIn
//    private fun handleGoogleSignInResult(account: GoogleSignInAccount?) {
//        account?.let {
////            viewModel.onSignInWithGoogle(
////                account = it,
////                onSuccess = {
////                    startActivity(Intent(this, MainActivity::class.java))
////                    finish()
////                },
////                onError = { message ->
////                    Toast.makeText(this, message, Toast.LENGTH_SHORT).show()
////                }
////            )
//        }
//    }
//
//    // Manage email / password login
//    private fun signInWithEmail(email: String, password: String) {
//        auth.signInWithEmailAndPassword(email, password)
//            .addOnCompleteListener(this) { task ->
//                if (task.isSuccessful) {
//                    // success
//                   navigateUpTo("main")
//                } else {
//                    // error
//                    Toast.makeText(this, "${task.exception?.message}", Toast.LENGTH_LONG).show()
//                }
//            }
//    }
//}
//
//
//
//
