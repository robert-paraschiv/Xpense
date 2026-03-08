package com.rokudo.xpense.fragments

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
import com.rokudo.xpense.data.viewmodels.BankApiViewModel
import com.rokudo.xpense.data.viewmodels.StatisticsViewModel
import com.rokudo.xpense.models.BAccount
import com.rokudo.xpense.models.Transaction
import com.rokudo.xpense.utils.dialogs.TimedDialog
import java.text.SimpleDateFormat
import java.util.*
import com.rokudo.xpense.ui.theme.XpenseTheme

class BankAccFragment : Fragment() {

    private lateinit var bankApiViewModel: BankApiViewModel
    private lateinit var statisticsViewModel: StatisticsViewModel
    private lateinit var bAccount: BAccount
    private val simpleDateFormat = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        bankApiViewModel = ViewModelProvider(requireActivity())[BankApiViewModel::class.java]
        statisticsViewModel = ViewModelProvider(requireActivity())[StatisticsViewModel::class.java]

        val args = BankAccFragmentArgs.fromBundle(requireArguments())
        bAccount = args.bAccount

        return ComposeView(requireContext()).apply {
            setContent {
                XpenseTheme {
                var balance by remember { mutableStateOf<String?>(null) }
                var transactions by remember { mutableStateOf<List<Transaction>>(emptyList()) }
                var isLoading by remember { mutableStateOf(true) }

                // Load account data
                LaunchedEffect(Unit) {
                    loadAccountData()
                }

                // Observe balances
                bankApiViewModel.getAccountBalances(bAccount.accounts?.firstOrNull() ?: "")
                    .observe(viewLifecycleOwner) { balances ->
                        if (balances?.balances != null && balances.balances.isNotEmpty()) {
                            balance = balances.balances[0].balanceAmount["amount"]
                            isLoading = false
                        }
                    }

                // Observe transactions
                LaunchedEffect(Unit) {
                    val calendar = Calendar.getInstance()
                    calendar.add(Calendar.DAY_OF_YEAR, -30)
                    val dateFrom = simpleDateFormat.format(calendar.time)

                    bankApiViewModel.getAccountTransactions(
                        bAccount.accounts?.firstOrNull() ?: "",
                        dateFrom
                    ).observe(viewLifecycleOwner) { transactionsResponse ->
                        if (transactionsResponse?.transactions != null) {
                            val bankTransactions = transactionsResponse.transactions.booked?.toList() ?: emptyList()
                            val convertedTransactions = bankTransactions.mapNotNull { bankTrans ->
                                try {
                                    Transaction().apply {
                                        id = bankTrans.internalTransactionId
                                        amount = bankTrans.transactionAmount.amount.toDouble()
                                        title = bankTrans.remittanceInformationUnstructured
                                        category = bankTrans.proprietaryBankTransactionCode
                                        currency = bankTrans.transactionAmount.currency
                                        bankTrans.bookingDate?.let { dateStr ->
                                            date = simpleDateFormat.parse(dateStr)
                                        }
                                    }
                                } catch (e: Exception) {
                                    Log.e("BankAccFragment", "Error converting transaction", e)
                                    null
                                }
                            }.sortedByDescending { it.date?.time ?: 0 }

                            transactions = convertedTransactions
                            isLoading = false
                        } else {
                            isLoading = false
                            Toast.makeText(requireContext(), "Failed to load transactions", Toast.LENGTH_SHORT).show()
                        }
                    }
                }

                BankAccScreen(
                    bAccount = bAccount,
                    balance = balance,
                    transactions = transactions,
                    isLoading = isLoading,
                    onBackClick = {
                        findNavController().popBackStack()
                    },
                    onTransactionClick = { transaction ->
                        // Determine transaction type based on amount sign
                        val transactionType = if (transaction.amount < 0) {
                            Transaction.EXPENSE_TYPE
                        } else {
                            Transaction.INCOME_TYPE
                        }

                        transaction.type = transactionType
                        transaction.amount = Math.abs(transaction.amount)

                        val walletId = bAccount.walletIds?.firstOrNull()
                        val currency = bAccount.linked_acc_currency

                        if (!walletId.isNullOrEmpty() && !currency.isNullOrEmpty()) {
                            val action = BankAccFragmentDirections
                                .actionBAccountDetailsFragmentToAddTransactionFragment(
                                    walletId,
                                    currency,
                                    transaction,
                                    false
                                )
                            findNavController().navigate(action)
                        } else {
                            Toast.makeText(requireContext(), "Please link this account to a wallet first", Toast.LENGTH_SHORT).show()
                        }
                    },
                    onRefreshClick = {
                        isLoading = true
                        loadAccountData()
                    }
                )
                } // XpenseTheme
            }
        }
    }

    private fun loadAccountData() {
        // Check if agreement is expired
        if (bAccount.getEUA_EndDate() != null && bAccount.getEUA_EndDate().before(Date())) {
            val dialog = TimedDialog("Agreement expired. Please reconnect your bank account.", 2000)
            dialog.show(parentFragmentManager, "agreement_expired")
            return
        }

        // Load balances and transactions - handled by LaunchedEffect in compose
    }
}

