package com.rokudo.xpense.activities

import android.content.Intent
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.runtime.*
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.rokudo.xpense.data.repositories.AuthRepository
import com.rokudo.xpense.fragments.login.LoginScreen
import com.rokudo.xpense.fragments.login.RegisterScreen
import com.rokudo.xpense.ui.theme.XpenseTheme
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import java.net.ConnectException
import java.net.SocketTimeoutException
import java.net.UnknownHostException

class LoginActivity : ComponentActivity() {

    private lateinit var authRepository: AuthRepository

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        authRepository = AuthRepository(applicationContext)

        setContent {
            XpenseTheme {
                val navController = rememberNavController()

                // Login state
                var loginEmail by remember { mutableStateOf("") }
                var loginPassword by remember { mutableStateOf("") }
                var loginLoading by remember { mutableStateOf(false) }
                var loginError by remember { mutableStateOf<String?>(null) }

                // Register state
                var regName by remember { mutableStateOf("") }
                var regEmail by remember { mutableStateOf("") }
                var regPassword by remember { mutableStateOf("") }
                var regPhone by remember { mutableStateOf("") }
                var regLoading by remember { mutableStateOf(false) }
                var regError by remember { mutableStateOf<String?>(null) }

                NavHost(navController = navController, startDestination = "login") {
                    composable("login") {
                        LoginScreen(
                            email = loginEmail,
                            password = loginPassword,
                            isLoading = loginLoading,
                            errorMessage = loginError,
                            onEmailChange = { loginEmail = it; loginError = null },
                            onPasswordChange = { loginPassword = it; loginError = null },
                            onLogin = {
                                loginLoading = true
                                loginError = null
                                CoroutineScope(Dispatchers.Main).launch {
                                    val result = authRepository.login(loginEmail, loginPassword)
                                    loginLoading = false
                                    result.onSuccess {
                                        navigateToMain()
                                    }.onFailure { e ->
                                        loginError = friendlyError(e)
                                    }
                                }
                            },
                            onNavigateToRegister = {
                                navController.navigate("register")
                            }
                        )
                    }
                    composable("register") {
                        RegisterScreen(
                            name = regName,
                            email = regEmail,
                            password = regPassword,
                            phoneNumber = regPhone,
                            isLoading = regLoading,
                            errorMessage = regError,
                            onNameChange = { regName = it; regError = null },
                            onEmailChange = { regEmail = it; regError = null },
                            onPasswordChange = { regPassword = it; regError = null },
                            onPhoneNumberChange = { regPhone = it; regError = null },
                            onRegister = {
                                regLoading = true
                                regError = null
                                CoroutineScope(Dispatchers.Main).launch {
                                    val result = authRepository.register(
                                        name = regName.trim(),
                                        email = regEmail.trim(),
                                        password = regPassword,
                                        phoneNumber = regPhone.trim()
                                    )
                                    regLoading = false
                                    result.onSuccess {
                                        navigateToMain()
                                    }.onFailure { e ->
                                        regError = friendlyError(e)
                                    }
                                }
                            },
                            onNavigateToLogin = {
                                navController.popBackStack()
                            }
                        )
                    }
                }
            }
        }
    }

    private fun navigateToMain() {
        startActivity(Intent(this@LoginActivity, MainActivity::class.java))
        finish()
    }

    private fun friendlyError(e: Throwable): String {
        return when (e) {
            is ConnectException ->
                "Unable to connect to server. Please check that the server is running."
            is SocketTimeoutException ->
                "Connection timed out. Please try again."
            is UnknownHostException ->
                "Server not found. Please check your network connection."
            else -> {
                val msg = e.message ?: "An unexpected error occurred"
                // Strip verbose exception class prefixes
                if (msg.contains(":")) msg.substringAfter(":").trim()
                else msg
            }
        }
    }
}
