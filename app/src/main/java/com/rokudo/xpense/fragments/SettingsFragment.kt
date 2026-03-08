package com.rokudo.xpense.fragments

import android.app.Activity
import android.content.Intent
import android.graphics.Bitmap
import android.net.Uri
import android.os.Bundle
import android.provider.MediaStore
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.runtime.getValue
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.ui.platform.ComposeView
import androidx.compose.ui.platform.LocalContext
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.fragment.findNavController
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.storage.StorageReference
import com.rokudo.xpense.data.viewmodels.InvitesViewModel
import com.rokudo.xpense.data.viewmodels.TransactionViewModel
import com.rokudo.xpense.data.viewmodels.WalletsViewModel
import com.rokudo.xpense.models.Invitation
import com.rokudo.xpense.utils.DatabaseUtils
import com.rokudo.xpense.utils.PrefsUtils
import com.rokudo.xpense.utils.RotateBitmap
import com.rokudo.xpense.utils.dialogs.UploadingDialog
import com.rokudo.xpense.ui.theme.XpenseTheme
import java.io.ByteArrayOutputStream

class SettingsFragment : Fragment() {

    private lateinit var invitesViewModel: InvitesViewModel

    private val startForResultFromGallery = registerForActivityResult(
        ActivityResultContracts.StartActivityForResult()
    ) { result ->
        if (result.resultCode == Activity.RESULT_OK) {
            try {
                val imageUri = result.data?.data
                if (imageUri != null) {
                    uploadProfilePicture(imageUri)
                }
            } catch (e: Exception) {
                Toast.makeText(requireContext(), "Error selecting image", Toast.LENGTH_SHORT).show()
            }
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        invitesViewModel = ViewModelProvider(requireActivity())[InvitesViewModel::class.java]

        return ComposeView(requireContext()).apply {
            setContent {
                XpenseTheme {
                val context = LocalContext.current

                val currentUser = DatabaseUtils.getCurrentUser()
                val invitations by invitesViewModel.loadInvitations().observeAsState(emptyList())

                // Filter only pending invitations
                val pendingInvitations = invitations.filter {
                    it.status != Invitation.STATUS_ACCEPTED && it.status != Invitation.STATUS_DECLINED
                }

                SettingsScreen(
                    userName = currentUser?.name ?: "User",
                    userProfilePicUrl = currentUser?.pictureUrl,
                    invitations = pendingInvitations,
                    onBackClick = {
                        findNavController().popBackStack()
                    },
                    onSignOutClick = {
                        deleteViewModelsData()
                        FirebaseAuth.getInstance().signOut()
                    },
                    onProfilePictureClick = {
                        val intent = Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI)
                        startForResultFromGallery.launch(intent)
                    },
                    onAcceptInvitation = { invitation ->
                        invitesViewModel.updateStatus(invitation.id, Invitation.STATUS_ACCEPTED)
                        Toast.makeText(context, "Invitation accepted", Toast.LENGTH_SHORT).show()
                    },
                    onDeclineInvitation = { invitation ->
                        invitesViewModel.updateStatus(invitation.id, Invitation.STATUS_DECLINED)
                        Toast.makeText(context, "Invitation declined", Toast.LENGTH_SHORT).show()
                    }
                )
                } // XpenseTheme
            }
        }
    }

    private fun deleteViewModelsData() {
        val transactionViewModel = ViewModelProvider(requireActivity())[TransactionViewModel::class.java]
        val walletsViewModel = ViewModelProvider(requireActivity())[WalletsViewModel::class.java]

        transactionViewModel.removeAllData()
        walletsViewModel.removeAllData()
        PrefsUtils.setSelectedWalletId(requireContext(), null)
    }

    private fun uploadProfilePicture(imageUri: Uri) {
        try {
            val uploadingDialog = UploadingDialog("")
            uploadingDialog.show(parentFragmentManager, "UploadingDialog")

            val bitmap = RotateBitmap.HandleSamplingAndRotationBitmap(requireContext(), imageUri)

            val baos = ByteArrayOutputStream()
            bitmap.compress(Bitmap.CompressFormat.JPEG, 50, baos)
            val data = baos.toByteArray()

            val userId = FirebaseAuth.getInstance().currentUser?.uid ?: return
            val storageRef: StorageReference = DatabaseUtils.userPicturesRef
                .child("$userId.jpg")

            storageRef.putBytes(data)
                .addOnSuccessListener {
                    storageRef.downloadUrl.addOnSuccessListener { uri ->
                        DatabaseUtils.usersRef
                            .document(userId)
                            .update("pictureUrl", uri.toString())
                            .addOnSuccessListener {
                                uploadingDialog.dismiss()
                                Toast.makeText(requireContext(), "Profile picture updated", Toast.LENGTH_SHORT).show()
                            }
                            .addOnFailureListener {
                                uploadingDialog.dismiss()
                                Toast.makeText(requireContext(), "Failed to update profile", Toast.LENGTH_SHORT).show()
                            }
                    }
                }
                .addOnFailureListener {
                    uploadingDialog.dismiss()
                    Toast.makeText(requireContext(), "Upload failed", Toast.LENGTH_SHORT).show()
                }
        } catch (e: Exception) {
            Toast.makeText(requireContext(), "Error: ${e.message}", Toast.LENGTH_SHORT).show()
        }
    }
}


