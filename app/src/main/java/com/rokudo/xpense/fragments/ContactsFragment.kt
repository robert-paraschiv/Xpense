package com.rokudo.xpense.fragments

import android.Manifest
import android.annotation.SuppressLint
import android.content.pm.PackageManager
import android.database.Cursor
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.provider.ContactsContract
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.runtime.*
import androidx.compose.ui.platform.ComposeView
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import com.google.android.gms.tasks.Tasks
import com.rokudo.xpense.R
import com.rokudo.xpense.models.Invitation
import com.rokudo.xpense.models.User
import com.rokudo.xpense.models.Wallet
import com.rokudo.xpense.utils.DatabaseUtils
import com.rokudo.xpense.utils.dialogs.TimedDialog
import java.util.*
import com.rokudo.xpense.ui.theme.XpenseTheme

class ContactsFragment : Fragment() {

    private var mWallet: Wallet? = null
    private val contactsWithApp = mutableListOf<User>()

    private val permissionLauncher = registerForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { isGranted ->
        if (isGranted) {
            loadContacts()
        } else {
            Toast.makeText(
                requireContext(),
                "Cannot get contacts without permission",
                Toast.LENGTH_SHORT
            ).show()
            findNavController().popBackStack()
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        val args = try {
            ContactsFragmentArgs.fromBundle(requireArguments())
        } catch (_: Exception) {
            Toast.makeText(requireContext(), "Something went wrong", Toast.LENGTH_SHORT).show()
            findNavController().popBackStack()
            return ComposeView(requireContext())
        }

        mWallet = args.wallet

        // Show info dialog
        showInfoDialog()

        return ComposeView(requireContext()).apply {
            setContent {
                XpenseTheme {
                var contacts by remember { mutableStateOf<List<User>>(emptyList()) }
                var isLoading by remember { mutableStateOf(true) }

                LaunchedEffect(Unit) {
                    checkPermissionsAndLoad()
                }

                // Update contacts state when loaded
                LaunchedEffect(contactsWithApp.size) {
                    contacts = contactsWithApp.toList()
                    isLoading = false
                }

                ContactsScreen(
                    contacts = contacts,
                    isLoading = isLoading,
                    onBackClick = {
                        findNavController().popBackStack()
                    },
                    onRefreshClick = {
                        isLoading = true
                        checkPermissionsAndLoad()
                    },
                    onContactClick = { user ->
                        sendInvitation(user)
                    }
                )
                } // XpenseTheme
            }
        }
    }

    private fun showInfoDialog() {
        val dialog = TimedDialog(
            "Please note that in order for a contact to be displayed, " +
                    "the phone number should start with + and the user must have an existing Xpense account",
            4500
        )
        dialog.show(parentFragmentManager, "warning")
        dialog.startAnimation()
        Handler(Looper.getMainLooper()).postDelayed({
            dialog.dismiss()
        }, 4500)
    }

    private fun checkPermissionsAndLoad() {
        when {
            ContextCompat.checkSelfPermission(
                requireContext(),
                Manifest.permission.READ_CONTACTS
            ) == PackageManager.PERMISSION_GRANTED -> {
                loadContacts()
            }
            else -> {
                permissionLauncher.launch(Manifest.permission.READ_CONTACTS)
            }
        }
    }

    @SuppressLint("Range")
    private fun loadContacts() {
        contactsWithApp.clear()

        val contactNumbers = mutableListOf<String>()
        val contentResolver = requireActivity().contentResolver
        val projection = arrayOf(
            ContactsContract.Contacts.DISPLAY_NAME,
            ContactsContract.CommonDataKinds.Phone.NUMBER
        )

        val cursor: Cursor? = contentResolver.query(
            ContactsContract.Data.CONTENT_URI,
            projection,
            null,
            null,
            null
        )

        cursor?.use {
            while (it.moveToNext()) {
                val phoneNumber = it.getString(
                    it.getColumnIndex(ContactsContract.CommonDataKinds.Phone.NUMBER)
                )

                if (phoneNumber != null && phoneNumber.startsWith("+") && phoneNumber.length >= 10) {
                    val trimmed = phoneNumber.replace(" ", "")
                    if (trimmed !in contactNumbers) {
                        contactNumbers.add(trimmed)
                    }
                }
            }
        }

        getContactsWithApp(contactNumbers)
    }

    private fun getContactsWithApp(contactNumbers: List<String>) {
        val tasks = contactNumbers.map { number ->
            DatabaseUtils.usersRef.document(number).get()
        }

        Tasks.whenAllComplete(tasks).addOnCompleteListener { _ ->
            tasks.filter { it.isSuccessful && it.result.exists() }.forEach { task ->
                try {
                    val user = task.result.toObject(User::class.java)
                    if (user != null && user !in contactsWithApp) {
                        contactsWithApp.add(user)
                    }
                } catch (e: Exception) {
                    e.printStackTrace()
                }
            }

            contactsWithApp.sort()
        }
    }

    private fun sendInvitation(user: User) {
        val invitation = Invitation().apply {
            id = mWallet?.id ?: ""
            wallet_title = mWallet?.title
            status = Invitation.STATUS_SENT
            date = Date()
            creator_id = DatabaseUtils.getCurrentUser().uid
            creator_name = DatabaseUtils.getCurrentUser().name
            creator_pic_url = DatabaseUtils.getCurrentUser().pictureUrl
            invited_person_phone_number = user.phoneNumber
        }

        val uploadingDialog = TimedDialog(
            "Sending invitation to ${user.name} ...",
            1500
        )
        uploadingDialog.show(parentFragmentManager, "sentInvite")

        DatabaseUtils.invitationsRef.document(invitation.id)
            .get()
            .addOnCompleteListener { task ->
                if (task.result.exists()) {
                    Toast.makeText(
                        requireContext(),
                        "A user has already been invited to this wallet",
                        Toast.LENGTH_LONG
                    ).show()
                    uploadingDialog.dismiss()
                    findNavController().popBackStack(R.id.homeFragment, false)
                } else {
                    DatabaseUtils.invitationsRef.document(invitation.id)
                        .set(invitation)
                        .addOnSuccessListener {
                            uploadingDialog.startAnimation()
                            Handler(Looper.getMainLooper()).postDelayed({
                                uploadingDialog.dismiss()
                                findNavController().popBackStack(R.id.homeFragment, false)
                            }, 1500)
                        }
                }
            }
    }
}

