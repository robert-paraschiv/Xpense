package com.rokudo.xpense.fragments.login

import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.compose.runtime.*
import androidx.compose.ui.platform.ComposeView
import androidx.fragment.app.Fragment
import com.google.firebase.auth.FirebaseAuth
import com.rokudo.xpense.activities.MainActivity
import com.rokudo.xpense.utils.DatabaseUtils
import com.rokudo.xpense.ui.theme.XpenseTheme

class UserDetailsFragment : Fragment() {

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        return ComposeView(requireContext()).apply {
            setContent {
                XpenseTheme {
                var name by remember { mutableStateOf("") }
                var isLoading by remember { mutableStateOf(false) }

                UserDetailsScreen(
                    name = name,
                    isLoading = isLoading,
                    onNameChange = { name = it },
                    onDone = {
                        if (name.trim().isNotEmpty()) {
                            isLoading = true
                            val currentUser = FirebaseAuth.getInstance().currentUser
                            val phoneNumber = currentUser?.phoneNumber

                            if (phoneNumber != null) {
                                DatabaseUtils.usersRef.document(phoneNumber)
                                    .update("name", name.trim())
                                    .addOnSuccessListener {
                                        isLoading = false
                                        startActivity(Intent(requireActivity(), MainActivity::class.java))
                                        requireActivity().finish()
                                    }
                                    .addOnFailureListener {
                                        isLoading = false
                                        Toast.makeText(
                                            requireContext(),
                                            "Failed to save name",
                                            Toast.LENGTH_SHORT
                                        ).show()
                                    }
                            }
                        } else {
                            Toast.makeText(
                                requireContext(),
                                "Name cannot be empty",
                                Toast.LENGTH_SHORT
                            ).show()
                        }
                    }
                )
                } // XpenseTheme
            }
        }
    }
}

