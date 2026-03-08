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
import androidx.navigation.fragment.findNavController
import com.rokudo.xpense.components.CategoryPickerSheet
import com.rokudo.xpense.data.viewmodels.AddTransactionEffect
import com.rokudo.xpense.data.viewmodels.AddTransactionEvent
import com.rokudo.xpense.data.viewmodels.AddTransactionViewModel
import com.rokudo.xpense.utils.dialogs.ConfirmationDialog
import com.rokudo.xpense.ui.theme.XpenseTheme

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

        return ComposeView(requireContext()).apply {
            setContent {
                XpenseTheme {
                    val state by viewModel.state.collectAsState()

                    // State for showing category picker bottom sheet
                    var showCategoryPicker by remember { mutableStateOf(false) }

                    // Single effect collector
                    LaunchedEffect(Unit) {
                        viewModel.effect.collect { effect ->
                            when (effect) {
                                is AddTransactionEffect.NavigateBack -> findNavController().popBackStack()
                                is AddTransactionEffect.ShowToast ->
                                    Toast.makeText(requireContext(), effect.message, Toast.LENGTH_SHORT).show()
                                is AddTransactionEffect.ShowCategoryDialog -> {
                                    showCategoryPicker = true
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

                    AddTransactionScreen(
                        state = state,
                        onEvent = viewModel::onEvent
                    )

                    // ─── Compose Category Picker Bottom Sheet ───
                    if (showCategoryPicker) {
                        CategoryPickerSheet(
                            selectedCategory = state.selectedCategory,
                            onCategorySelected = { category ->
                                viewModel.onEvent(AddTransactionEvent.OnCategoryChange(category))
                                showCategoryPicker = false
                            },
                            onDismiss = {
                                showCategoryPicker = false
                            }
                        )
                    }
                }
            }
        }
    }
}
