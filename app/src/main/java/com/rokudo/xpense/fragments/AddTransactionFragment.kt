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
import com.rokudo.xpense.R
import com.rokudo.xpense.data.viewmodels.TransactionViewModel
import com.rokudo.xpense.models.ExpenseCategory
import com.rokudo.xpense.models.Transaction
import com.rokudo.xpense.utils.CategoriesUtil
import com.rokudo.xpense.utils.DatabaseUtils
import com.rokudo.xpense.utils.TransactionUtils
import com.rokudo.xpense.utils.dialogs.CategoryDialog
import com.rokudo.xpense.utils.dialogs.ConfirmationDialog
import com.rokudo.xpense.utils.dialogs.UploadingDialog
import java.util.*

class AddTransactionFragment : Fragment() {

    private lateinit var viewModel: TransactionViewModel
    private lateinit var walletId: String
    private lateinit var currency: String
    private var mTransaction: Transaction? = null
    private var isEditMode = false

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        viewModel = ViewModelProvider(requireActivity())[TransactionViewModel::class.java]

        val args = AddTransactionFragmentArgs.fromBundle(requireArguments())
        walletId = args.walletId
        currency = args.currency
        mTransaction = args.transaction
        isEditMode = args.editMode

        return ComposeView(requireContext()).apply {
            setContent {
                var selectedCategory by remember {
                    mutableStateOf(
                        when {
                            mTransaction?.type == Transaction.INCOME_TYPE -> ExpenseCategory("Income")
                            mTransaction?.type == Transaction.TRANSFER_TYPE -> ExpenseCategory("Transfer")
                            mTransaction?.category != null -> {
                                val cat = ExpenseCategory(mTransaction!!.category)
                                CategoriesUtil.expenseCategoryList.getOrNull(
                                    CategoriesUtil.expenseCategoryList.indexOf(cat)
                                )
                            }
                            else -> CategoriesUtil.expenseCategoryList.firstOrNull()
                        }
                    )
                }

                AddTransactionScreen(
                    transaction = mTransaction,
                    isEditMode = isEditMode && mTransaction != null,
                    selectedCategory = selectedCategory,
                    onBackClick = {
                        findNavController().popBackStack()
                    },
                    onSaveClick = { amount, title, date, category, type, isCash ->
                        addTransactionToDb(amount, title, date, category, type, isCash)
                    },
                    onDeleteClick = {
                        mTransaction?.let { trans ->
                            val dialog = ConfirmationDialog("Are you sure you want to delete this transaction?")
                            dialog.setOnClickListener {
                                dialog.dismiss()
                                viewModel.deleteTransaction(trans.id, trans.walletId)
                                    .observe(viewLifecycleOwner) { result ->
                                        if (result == true) {
                                            Toast.makeText(
                                                requireContext(),
                                                "Deleted Successfully",
                                                Toast.LENGTH_SHORT
                                            ).show()
                                            findNavController().popBackStack()
                                        }
                                    }
                            }
                            dialog.show(parentFragmentManager, "ConfirmationDialog")
                        }
                    },
                    onCategoryClick = {
                        val categoryDialog = CategoryDialog(selectedCategory)
                        categoryDialog.setClickListener { expenseCategory ->
                            selectedCategory = expenseCategory
                            categoryDialog.dismiss()
                        }
                        categoryDialog.showNow(parentFragmentManager, "transactionCategoryDialog")
                    },
                    onCategoryChange = { category ->
                        selectedCategory = category
                    }
                )
            }
        }
    }

    private fun addTransactionToDb(
        amount: String,
        title: String,
        date: Date,
        category: ExpenseCategory?,
        type: String,
        isCash: Boolean
    ) {
        val transaction = Transaction().apply {
            walletId = this@AddTransactionFragment.walletId
            this.amount = amount.toDouble()
            currency = this@AddTransactionFragment.currency
            this.date = date
            picUrl = DatabaseUtils.getCurrentUser().pictureUrl
            user_id = DatabaseUtils.getCurrentUser().uid
            userName = DatabaseUtils.getCurrentUser().name
            this.category = category?.name ?: ""
            this.title = title
            setCashTransaction(isCash)
            this.type = type
        }

        val uploadingDialog = UploadingDialog("Please Wait...")
        uploadingDialog.show(parentFragmentManager, "wait")

        if (mTransaction == null) {
            // Adding new transaction
            val documentReference = DatabaseUtils.getTransactionsRef(walletId).document()
            transaction.id = documentReference.id

            viewModel.addTransaction(transaction).observe(viewLifecycleOwner) { result ->
                if (result == "Success") {
                    uploadingDialog.dismiss()
                    findNavController().popBackStack(R.id.homeFragment, false)
                }
            }
        } else {
            // Updating existing transaction
            if (TransactionUtils.isTransactionDifferent(mTransaction, transaction)) {
                transaction.id = if (mTransaction!!.id == null || mTransaction!!.id == "NOTPROVIDED") {
                    DatabaseUtils.getTransactionsRef(walletId).document().id
                } else {
                    mTransaction!!.id
                }

                viewModel.updateTransaction(transaction).observe(viewLifecycleOwner) { result ->
                    if (result == "Success") {
                        uploadingDialog.dismiss()
                        findNavController().popBackStack(R.id.homeFragment, false)
                    }
                }
            } else {
                uploadingDialog.dismiss()
                findNavController().popBackStack(R.id.homeFragment, false)
            }
        }
    }
}

