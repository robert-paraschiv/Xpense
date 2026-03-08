package com.rokudo.xpense.activities

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.runtime.*
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.google.firebase.FirebaseException
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.PhoneAuthCredential
import com.google.firebase.auth.PhoneAuthOptions
import com.google.firebase.auth.PhoneAuthProvider
import com.google.firebase.messaging.FirebaseMessaging
import com.rokudo.xpense.data.viewmodels.WalletsViewModel
import com.rokudo.xpense.fragments.login.LoginScreen
import com.rokudo.xpense.fragments.login.UserDetailsScreen
import com.rokudo.xpense.models.User
import com.rokudo.xpense.models.Wallet
import com.rokudo.xpense.ui.theme.XpenseTheme
import com.rokudo.xpense.utils.DatabaseUtils
import com.rokudo.xpense.utils.PrefsUtils
import java.util.*
import java.util.concurrent.TimeUnit

class LoginActivity : ComponentActivity() {
    private lateinit var mAuth: FirebaseAuth
    private var verificationId: String? = null
    private lateinit var walletsViewModel: WalletsViewModel

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        mAuth = FirebaseAuth.getInstance()
        walletsViewModel = ViewModelProvider(this)[WalletsViewModel::class.java]

        setContent {
            XpenseTheme {
                val navController = rememberNavController()
                var phoneNumber by remember { mutableStateOf("") }
                var otp by remember { mutableStateOf("") }
                var showOtpField by remember { mutableStateOf(false) }
                var isLoading by remember { mutableStateOf(false) }

                NavHost(navController = navController, startDestination = "login") {
                    composable("login") {
                        LoginScreen(
                            phoneNumber = phoneNumber,
                            otp = otp,
                            showOtpField = showOtpField,
                            isLoading = isLoading,
                            onPhoneNumberChange = { phoneNumber = it },
                            onOtpChange = { otp = it },
                            onSendOtp = {
                                if (phoneNumber.isNotEmpty()) {
                                    isLoading = true
                                    sendVerificationCode(phoneNumber,
                                        onSuccess = {
                                            isLoading = false
                                            showOtpField = true
                                        },
                                        onError = { msg ->
                                            isLoading = false
                                            Toast.makeText(this@LoginActivity, msg, Toast.LENGTH_LONG).show()
                                        }
                                    )
                                }
                            },
                            onVerifyOtp = {
                                if (otp.isNotEmpty() && verificationId != null) {
                                    isLoading = true
                                    verifyCode(otp) { isNewUser ->
                                        if (isNewUser) {
                                            navController.navigate("user_details") {
                                                popUpTo("login") { inclusive = true }
                                            }
                                        }
                                    }
                                }
                            }
                        )
                    }
                    composable("user_details") {
                        var name by remember { mutableStateOf("") }
                        var detailsLoading by remember { mutableStateOf(false) }

                        UserDetailsScreen(
                            name = name,
                            isLoading = detailsLoading,
                            onNameChange = { name = it },
                            onDone = {
                                if (name.trim().isNotEmpty()) {
                                    detailsLoading = true
                                    val currentUser = FirebaseAuth.getInstance().currentUser
                                    val phone = currentUser?.phoneNumber
                                    if (phone != null) {
                                        DatabaseUtils.usersRef.document(phone)
                                            .update("name", name.trim())
                                            .addOnSuccessListener {
                                                detailsLoading = false
                                                startActivity(Intent(this@LoginActivity, MainActivity::class.java))
                                                finish()
                                            }
                                            .addOnFailureListener {
                                                detailsLoading = false
                                                Toast.makeText(this@LoginActivity, "Failed to save name", Toast.LENGTH_SHORT).show()
                                            }
                                    }
                                } else {
                                    Toast.makeText(this@LoginActivity, "Name cannot be empty", Toast.LENGTH_SHORT).show()
                                }
                            }
                        )
                    }
                }
            }
        }
    }

    private fun sendVerificationCode(
        phoneNumber: String,
        onSuccess: () -> Unit,
        onError: (String) -> Unit
    ) {
        val options = PhoneAuthOptions.newBuilder(mAuth)
            .setPhoneNumber(phoneNumber)
            .setTimeout(60L, TimeUnit.SECONDS)
            .setActivity(this)
            .setCallbacks(object : PhoneAuthProvider.OnVerificationStateChangedCallbacks() {
                override fun onCodeSent(id: String, token: PhoneAuthProvider.ForceResendingToken) {
                    verificationId = id
                    onSuccess()
                }
                override fun onVerificationCompleted(credential: PhoneAuthCredential) {
                    credential.smsCode?.let { verifyCode(it) {} }
                }
                override fun onVerificationFailed(e: FirebaseException) {
                    Log.e("LoginActivity", "Verification failed", e)
                    onError(e.message ?: "Verification failed")
                }
                override fun onCodeAutoRetrievalTimeOut(p0: String) {}
            })
            .build()
        PhoneAuthProvider.verifyPhoneNumber(options)
    }

    private fun verifyCode(code: String, onNewUser: (Boolean) -> Unit) {
        verificationId?.let { id ->
            val credential = PhoneAuthProvider.getCredential(id, code)
            signInWithCredential(credential, onNewUser)
        }
    }

    private fun signInWithCredential(credential: PhoneAuthCredential, onNewUser: (Boolean) -> Unit) {
        mAuth.signInWithCredential(credential)
            .addOnCompleteListener { task ->
                if (task.isSuccessful) {
                    val isNewUser = task.result.additionalUserInfo?.isNewUser ?: false
                    val user = task.result.user
                    val phoneNumber = user?.phoneNumber

                    if (isNewUser && phoneNumber != null) {
                        val newUser = User(uid = user.uid, phoneNumber = phoneNumber)
                        DatabaseUtils.usersRef.document(phoneNumber).set(newUser)

                        val defaultWallet = Wallet(
                            id = DatabaseUtils.walletsRef.document().id,
                            amount = 0.0,
                            creation_date = Date(),
                            currency = "RON",
                            title = "Default Wallet",
                            users = listOf(user.uid),
                            creator_id = user.uid
                        )
                        PrefsUtils.setSelectedWalletId(this, defaultWallet.id ?: "")
                        DatabaseUtils.walletsRef.document(defaultWallet.id ?: "").set(defaultWallet)

                        walletsViewModel.addWallet(defaultWallet).observe(this) { result ->
                            if (result == "Success") {
                                FirebaseMessaging.getInstance().token.addOnSuccessListener { token ->
                                    DatabaseUtils.usersRef.document(phoneNumber).update("token", token)
                                }
                                onNewUser(true)
                            }
                        }
                    } else if (phoneNumber != null) {
                        FirebaseMessaging.getInstance().token.addOnSuccessListener { token ->
                            DatabaseUtils.usersRef.document(phoneNumber).update("token", token)
                        }
                        startActivity(Intent(this, MainActivity::class.java))
                        finish()
                    }
                } else {
                    Toast.makeText(this, task.exception?.message ?: "Sign in failed", Toast.LENGTH_LONG).show()
                }
            }
    }
}
