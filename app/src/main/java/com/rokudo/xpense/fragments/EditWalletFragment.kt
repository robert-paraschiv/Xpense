package com.rokudo.xpense.fragments

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.compose.ui.platform.ComposeView
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.fragment.findNavController
import com.rokudo.xpense.data.viewmodels.WalletsViewModel
import com.rokudo.xpense.models.Wallet
import com.rokudo.xpense.utils.DatabaseUtils
import com.rokudo.xpense.utils.PrefsUtils
import com.rokudo.xpense.utils.dialogs.ConfirmationDialog
import java.util.*
import com.rokudo.xpense.ui.theme.XpenseTheme

class EditWalletFragment : Fragment() {

    private lateinit var walletsViewModel: WalletsViewModel
    private var mWallet: Wallet? = null

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        walletsViewModel = ViewModelProvider(requireActivity())[WalletsViewModel::class.java]

        val args = try {
            EditWalletFragmentArgs.fromBundle(requireArguments())
        } catch (e: Exception) {
            Toast.makeText(requireContext(), "Error accessing wallet details", Toast.LENGTH_SHORT).show()
            findNavController().popBackStack()
            return ComposeView(requireContext())
        }

        mWallet = args.wallet

        return ComposeView(requireContext()).apply {
            setContent {
                XpenseTheme {
                val currencies = listOf("$", "€", "£", "₹", "¥", "₽", "lei", "CHF")

                EditWalletScreen(
                    wallet = mWallet,
                    currencies = currencies,
                    onBackClick = {
                        findNavController().popBackStack()
                    },
                    onSaveClick = { title, amount, currency ->
                        if (mWallet == null) {
                            addWalletToDb(title, amount, currency)
                        } else {
                            updateWallet(title, amount, currency)
                        }
                    },
                    onDeleteClick = {
                        val dialog = ConfirmationDialog(
                            "WARNING!\nYou will not be able to recover the wallet data\n" +
                                    "Are you sure you want to delete this wallet?"
                        )
                        dialog.setOnClickListener {
                            mWallet?.let { wallet ->
                                walletsViewModel.deleteWallet(wallet.id)
                                    .observe(viewLifecycleOwner) { result ->
                                        dialog.dismiss()
                                        if (result == true) {
                                            Toast.makeText(
                                                requireContext(),
                                                "Wallet deleted successfully",
                                                Toast.LENGTH_SHORT
                                            ).show()
                                            findNavController().popBackStack()
                                        } else {
                                            Toast.makeText(
                                                requireContext(),
                                                "Could not delete wallet, please try again",
                                                Toast.LENGTH_SHORT
                                            ).show()
                                        }
                                    }
                            }
                        }
                        dialog.show(parentFragmentManager, "confirmDeletionDialog")
                    },
                    onInviteClick = {
                        mWallet?.let { wallet ->
                            val action = EditWalletFragmentDirections
                                .actionEditWalletFragmentToContactsFragment(wallet)
                            findNavController().navigate(action)
                        }
                    }
                )
                } // XpenseTheme
            }
        }
    }

    private fun updateWallet(title: String, amountStr: String, currency: String) {
        val wallet = mWallet ?: return

        try {
            val amount = amountStr.toDouble()
            wallet.title = title
            wallet.amount = amount
            wallet.currency = currency

            walletsViewModel.updateWallet(wallet).observe(viewLifecycleOwner) { result ->
                if (result == true) {
                    findNavController().popBackStack()
                } else {
                    Toast.makeText(
                        requireContext(),
                        "Something went wrong, please try again",
                        Toast.LENGTH_SHORT
                    ).show()
                }
            }
        } catch (e: NumberFormatException) {
            Toast.makeText(requireContext(), "Invalid amount format", Toast.LENGTH_SHORT).show()
        }
    }

    private fun addWalletToDb(title: String, amountStr: String, currency: String) {
        try {
            val amount = amountStr.toDouble()

            val documentReference = DatabaseUtils.walletsRef.document()
            val wallet = Wallet().apply {
                id = documentReference.id
                this.amount = amount
                creation_date = Date()
                this.currency = currency
                this.title = title
                users = listOf(DatabaseUtils.getCurrentUser().uid)
                creator_id = DatabaseUtils.getCurrentUser().uid
            }

            PrefsUtils.setSelectedWalletId(requireContext(), wallet.id)

            walletsViewModel.addWallet(wallet).observe(viewLifecycleOwner) { result ->
                if (result == "Success") {
                    findNavController().popBackStack()
                } else {
                    Toast.makeText(
                        requireContext(),
                        "Failed to create wallet",
                        Toast.LENGTH_SHORT
                    ).show()
                }
            }
        } catch (e: NumberFormatException) {
            Toast.makeText(requireContext(), "Invalid amount format", Toast.LENGTH_SHORT).show()
        }
    }
}

