package com.rokudo.xpense.fragments.login

import androidx.compose.animation.*
import androidx.compose.animation.core.tween
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.rokudo.xpense.ui.theme.XpenseTheme

@Composable
fun LoginScreen(
    phoneNumber: String,
    otp: String,
    showOtpField: Boolean,
    isLoading: Boolean,
    onPhoneNumberChange: (String) -> Unit,
    onOtpChange: (String) -> Unit,
    onSendOtp: () -> Unit,
    onVerifyOtp: () -> Unit
) {
    Surface(
        modifier = Modifier.fillMaxSize(),
        color = MaterialTheme.colorScheme.background
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Text(
                text = "Welcome to Xpense",
                style = MaterialTheme.typography.displaySmall,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.primary
            )

            Spacer(modifier = Modifier.height(16.dp))

            Text(
                text = "Track your expenses effortlessly",
                style = MaterialTheme.typography.bodyLarge,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                textAlign = TextAlign.Center
            )

            Spacer(modifier = Modifier.height(48.dp))

            AnimatedContent(
                targetState = showOtpField,
                transitionSpec = {
                    (fadeIn(tween(400)) + slideInHorizontally(tween(400)) { it })
                        .togetherWith(fadeOut(tween(300)) + slideOutHorizontally(tween(300)) { -it })
                },
                label = "login_fields"
            ) { otpVisible ->
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    if (!otpVisible) {
                        OutlinedTextField(
                            value = phoneNumber,
                            onValueChange = onPhoneNumberChange,
                            label = { Text("Phone Number") },
                            placeholder = { Text("+40123456789") },
                            modifier = Modifier.fillMaxWidth(),
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Phone),
                            singleLine = true,
                            enabled = !isLoading,
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedContainerColor = MaterialTheme.colorScheme.surface,
                                unfocusedContainerColor = MaterialTheme.colorScheme.surface,
                                focusedBorderColor = MaterialTheme.colorScheme.primary,
                                unfocusedBorderColor = MaterialTheme.colorScheme.outlineVariant,
                            ),
                            shape = MaterialTheme.shapes.medium
                        )

                        Spacer(modifier = Modifier.height(24.dp))

                        Button(
                            onClick = onSendOtp,
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(52.dp),
                            enabled = !isLoading && phoneNumber.isNotEmpty(),
                            shape = MaterialTheme.shapes.medium
                        ) {
                            if (isLoading) {
                                CircularProgressIndicator(
                                    modifier = Modifier.size(24.dp),
                                    color = MaterialTheme.colorScheme.onPrimary,
                                    strokeWidth = 2.dp
                                )
                            } else {
                                Text("Send OTP", style = MaterialTheme.typography.labelLarge)
                            }
                        }
                    } else {
                        OutlinedTextField(
                            value = otp,
                            onValueChange = onOtpChange,
                            label = { Text("Enter OTP") },
                            placeholder = { Text("123456") },
                            modifier = Modifier.fillMaxWidth(),
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                            singleLine = true,
                            enabled = !isLoading,
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedContainerColor = MaterialTheme.colorScheme.surface,
                                unfocusedContainerColor = MaterialTheme.colorScheme.surface,
                                focusedBorderColor = MaterialTheme.colorScheme.primary,
                                unfocusedBorderColor = MaterialTheme.colorScheme.outlineVariant,
                            ),
                            shape = MaterialTheme.shapes.medium
                        )

                        Spacer(modifier = Modifier.height(24.dp))

                        Button(
                            onClick = onVerifyOtp,
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(52.dp),
                            enabled = !isLoading && otp.isNotEmpty(),
                            shape = MaterialTheme.shapes.medium
                        ) {
                            if (isLoading) {
                                CircularProgressIndicator(
                                    modifier = Modifier.size(24.dp),
                                    color = MaterialTheme.colorScheme.onPrimary,
                                    strokeWidth = 2.dp
                                )
                            } else {
                                Text("Verify OTP", style = MaterialTheme.typography.labelLarge)
                            }
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            Text(
                text = "By continuing, you agree to our Terms of Service",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                textAlign = TextAlign.Center,
                modifier = Modifier.padding(horizontal = 16.dp)
            )
        }
    }
}

@Preview(showBackground = true)
@Composable
fun LoginScreenPhonePreview() {
    XpenseTheme(dynamicColor = false) {
        LoginScreen(
            phoneNumber = "",
            otp = "",
            showOtpField = false,
            isLoading = false,
            onPhoneNumberChange = {},
            onOtpChange = {},
            onSendOtp = {},
            onVerifyOtp = {}
        )
    }
}

@Preview(showBackground = true)
@Composable
fun LoginScreenOtpPreview() {
    XpenseTheme(dynamicColor = false) {
        LoginScreen(
            phoneNumber = "+40123456789",
            otp = "",
            showOtpField = true,
            isLoading = false,
            onPhoneNumberChange = {},
            onOtpChange = {},
            onSendOtp = {},
            onVerifyOtp = {}
        )
    }
}
