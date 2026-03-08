package com.rokudo.xpense.fragments

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.compose.runtime.*
import androidx.compose.ui.platform.ComposeView
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import com.rokudo.xpense.data.viewmodels.AddTransactionEffect
import com.rokudo.xpense.data.viewmodels.AddTransactionEvent
import com.rokudo.xpense.data.viewmodels.AddTransactionViewModel
import com.rokudo.xpense.utils.dialogs.CategoryDialog
import com.rokudo.xpense.utils.dialogs.ConfirmationDialog
import com.rokudo.xpense.ui.theme.XpenseTheme
import kotlinx.coroutines.launch

class AddTransactionFragment : Fragment() {

    private lateinit var viewModel: AddTransactionViewModel

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        viewModel = ViewModelProvider(this)[AddTransactionViewModel::class.java]

        val args = AddTransactionFragmentArgs.fromBundle(requireArguments())
        val walletId = args.walletId
        val currency = args.currency
        val mTransaction = args.transaction
        val isEditMode = args.editMode

        // Validate walletId
        if (walletId.isEmpty() || walletId == "Wallets") {
            Toast.makeText(requireContext(), "Invalid wallet. Please select a wallet first.", Toast.LENGTH_LONG).show()
            findNavController().popBackStack()
            return ComposeView(requireContext())
        }

        // Init ViewModel
        viewModel.onEvent(AddTransactionEvent.Init(walletId, currency, mTransaction, isEditMode))

        // Collect Effects
        viewLifecycleOwner.lifecycleScope.launch {
            viewModel.effect.collect { effect ->
                when(effect) {
                    is AddTransactionEffect.NavigateBack -> findNavController().popBackStack()
                    is AddTransactionEffect.ShowToast -> Toast.makeText(requireContext(), effect.message, Toast.LENGTH_SHORT).show()
                    is AddTransactionEffect.ShowCategoryDialog -> {
                        val categoryDialog = CategoryDialog(viewModel.state.value.selectedCategory)
                        categoryDialog.setClickListener { expenseCategory ->
                            viewModel.onEvent(AddTransactionEvent.OnCategoryChange(expenseCategory))
                            categoryDialog.dismiss()
                        }
                        categoryDialog.showNow(parentFragmentManager, "transactionCategoryDialog")
                    }
                    is AddTransactionEffect.ShowDeleteConfirmation -> {
                        val dialog = ConfirmationDialog("Are you sure you want to delete this transaction?")
                        dialog.setOnClickListener {
                            dialog.dismiss()
                            viewModel.onEvent(AddTransactionEvent.OnDeleteConfirmed)
                        }
                        dialog.show(parentFragmentManager, "ConfirmationDialog")
                    }
                }
            }
        }

        return ComposeView(requireContext()).apply {
            setContent {
                XpenseTheme {
                    val state by viewModel.state.collectAsState()
                    AddTransactionScreen(
                        state = state,
                        onEvent = viewModel::onEvent
                    )
                }
            }
        }
    }
}

