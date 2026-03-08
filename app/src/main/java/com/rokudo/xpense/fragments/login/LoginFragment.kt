package com.rokudo.xpense.fragments.login

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.compose.runtime.*
import androidx.compose.ui.platform.ComposeView
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.fragment.findNavController
import com.google.firebase.FirebaseException
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.PhoneAuthCredential
import com.google.firebase.auth.PhoneAuthOptions
import com.google.firebase.auth.PhoneAuthProvider
import com.google.firebase.messaging.FirebaseMessaging
import com.rokudo.xpense.activities.MainActivity
import com.rokudo.xpense.data.viewmodels.WalletsViewModel
import com.rokudo.xpense.models.User
import com.rokudo.xpense.models.Wallet
import com.rokudo.xpense.utils.DatabaseUtils
import com.rokudo.xpense.utils.PrefsUtils
import java.util.*
import java.util.concurrent.TimeUnit
import com.rokudo.xpense.ui.theme.XpenseTheme

class LoginFragment : Fragment() {

    private lateinit var mAuth: FirebaseAuth
    private var verificationId: String? = null
    private lateinit var walletsViewModel: WalletsViewModel

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        mAuth = FirebaseAuth.getInstance()
        walletsViewModel = ViewModelProvider(requireActivity())[WalletsViewModel::class.java]

        return ComposeView(requireContext()).apply {
            setContent {
                XpenseTheme {
                var phoneNumber by remember { mutableStateOf("") }
                var otp by remember { mutableStateOf("") }
                var showOtpField by remember { mutableStateOf(false) }
                var isLoading by remember { mutableStateOf(false) }

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
                                onError = {
                                    isLoading = false
                                    Toast.makeText(requireContext(), it, Toast.LENGTH_LONG).show()
                                }
                            )
                        }
                    },
                    onVerifyOtp = {
                        if (otp.isNotEmpty() && verificationId != null) {
                            isLoading = true
                            verifyCode(otp)
                        }
                    }
                )
                } // XpenseTheme
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
            .setActivity(requireActivity())
            .setCallbacks(object : PhoneAuthProvider.OnVerificationStateChangedCallbacks() {
                override fun onCodeSent(
                    verificationId: String,
                    token: PhoneAuthProvider.ForceResendingToken
                ) {
                    this@LoginFragment.verificationId = verificationId
                    onSuccess()
                }

                override fun onVerificationCompleted(credential: PhoneAuthCredential) {
                    credential.smsCode?.let { code ->
                        verifyCode(code)
                    }
                }

                override fun onVerificationFailed(e: FirebaseException) {
                    Log.e("LoginFragment", "Verification failed", e)
                    onError(e.message ?: "Verification failed")
                }

                override fun onCodeAutoRetrievalTimeOut(p0: String) {
                    Log.d("LoginFragment", "Auto retrieval timeout")
                }
            })
            .build()

        PhoneAuthProvider.verifyPhoneNumber(options)
    }

    private fun verifyCode(code: String) {
        verificationId?.let { id ->
            val credential = PhoneAuthProvider.getCredential(id, code)
            signInWithCredential(credential)
        }
    }

    private fun signInWithCredential(credential: PhoneAuthCredential) {
        mAuth.signInWithCredential(credential)
            .addOnCompleteListener { task ->
                if (task.isSuccessful) {
                    val isNewUser = task.result.additionalUserInfo?.isNewUser ?: false
                    val user = task.result.user
                    val phoneNumber = user?.phoneNumber

                    if (isNewUser && phoneNumber != null) {
                        // Create new user
                        val newUser = User().apply {
                            uid = user.uid
                            this.phoneNumber = phoneNumber
                        }

                        DatabaseUtils.usersRef.document(phoneNumber).set(newUser)

                        // Create default wallet
                        val defaultWallet = Wallet().apply {
                            id = DatabaseUtils.walletsRef.document().id
                            amount = 0.0
                            creation_date = Date()
                            currency = "RON"
                            title = "Default Wallet"
                            users = listOf(user.uid)
                            creator_id = user.uid
                        }

                        PrefsUtils.setSelectedWalletId(requireContext(), defaultWallet.id)

                        DatabaseUtils.walletsRef.document(defaultWallet.id).set(defaultWallet)

                        walletsViewModel.addWallet(defaultWallet).observe(viewLifecycleOwner) { result ->
                            if (result == "Success") {
                                // Update FCM token
                                FirebaseMessaging.getInstance().token.addOnSuccessListener { token ->
                                    DatabaseUtils.usersRef.document(phoneNumber)
                                        .update("token", token)
                                }

                                // Navigate to user details
                                findNavController().navigate(
                                    LoginFragmentDirections.actionLoginFragmentToUserDetailsFragment()
                                )
                            }
                        }
                    } else if (phoneNumber != null) {
                        // Existing user - update token
                        FirebaseMessaging.getInstance().token.addOnSuccessListener { token ->
                            DatabaseUtils.usersRef.document(phoneNumber)
                                .update("token", token)
                        }

                        // Navigate to main activity
                        startActivity(Intent(requireActivity(), MainActivity::class.java))
                        requireActivity().finish()
                    }
                } else {
                    Toast.makeText(
                        requireContext(),
                        task.exception?.message ?: "Sign in failed",
                        Toast.LENGTH_LONG
                    ).show()
                }
            }
    }
}

