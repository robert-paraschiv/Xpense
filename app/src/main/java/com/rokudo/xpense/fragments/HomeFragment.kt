package com.rokudo.xpense.fragments

import android.content.Context
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.ui.platform.ComposeView
import androidx.compose.ui.platform.LocalContext
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.findNavController
import com.rokudo.xpense.R
import com.rokudo.xpense.data.viewmodels.BankApiViewModel
import com.rokudo.xpense.data.viewmodels.StatisticsViewModel
import com.rokudo.xpense.data.viewmodels.TransactionViewModel
import com.rokudo.xpense.data.viewmodels.WalletsViewModel
import com.rokudo.xpense.models.SpentMostItem
import com.rokudo.xpense.models.Transaction
import com.rokudo.xpense.models.Wallet
import com.rokudo.xpense.utils.dialogs.AdjustBalanceDialog
import com.rokudo.xpense.utils.dialogs.WalletListDialog
import java.util.Calendar
import java.util.Date
import androidx.core.content.edit
import java.time.LocalDate
import java.time.ZoneOffset
import java.time.format.DateTimeFormatter
import java.util.Locale
import com.rokudo.xpense.ui.theme.XpenseTheme

class HomeFragment : Fragment() {

    private lateinit var walletsViewModel: WalletsViewModel
    private lateinit var transactionViewModel: TransactionViewModel
    private lateinit var statisticsViewModel: StatisticsViewModel
    private lateinit var bankApiViewModel: BankApiViewModel

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        walletsViewModel = ViewModelProvider(requireActivity())[WalletsViewModel::class.java]
        transactionViewModel = ViewModelProvider(requireActivity())[TransactionViewModel::class.java]
        statisticsViewModel = ViewModelProvider(requireActivity())[StatisticsViewModel::class.java]
        bankApiViewModel = ViewModelProvider(requireActivity())[BankApiViewModel::class.java]

        return ComposeView(requireContext()).apply {
            setContent {
                XpenseTheme {
                val context = LocalContext.current
                val prefs = context.getSharedPreferences("PREFS_NAME", Context.MODE_PRIVATE)

                // State for selected wallet ID
                var selectedWalletId by remember {
                    mutableStateOf(prefs.getString("selectedWalletId", "") ?: "")
                }

                // Load Wallet
                val walletState by walletsViewModel.loadWallet(selectedWalletId).observeAsState()
                val wallet = walletState

                // Determine if we have a valid wallet
                if (wallet == null) {
                    // Handle wallet not selected or invalid (e.g. show empty state or redirect)
                    // For now, let's assume one is selected or handle via dialog
                    if (selectedWalletId.isEmpty()) {
                         // Potentially trigger wallet selection dialog or show 'Add Wallet' UI
                        Toast.makeText(context, "Please create or select a wallet", Toast.LENGTH_SHORT).show()
                    }
                }

                // Load Last 7 Days Transactions for Bar Chart (only if wallet exists)
                val (startDate, endDate) = remember { calculateLast7Days() }
                val barChartTransactionsLiveData = remember(wallet?.id) {
                    if (wallet?.id != null && wallet.id.isNotEmpty() && wallet.id != "Wallets") {
                        transactionViewModel.loadTransactionsDateInterval(wallet.id, startDate, endDate)
                    } else {
                        androidx.lifecycle.MutableLiveData(emptyList())
                    }
                }
                val barChartTransactions by barChartTransactionsLiveData.observeAsState(emptyList())

                // Load Statistics for Pie Chart (only if wallet exists)
                val statisticsDocLiveData = remember(wallet?.id) {
                    if (wallet?.id != null && wallet.id.isNotEmpty() && wallet.id != "Wallets") {
                        statisticsViewModel.listenForStatisticsDoc(wallet.id, Date())
                    } else {
                        androidx.lifecycle.MutableLiveData(null)
                    }
                }
                val statisticsDoc by statisticsDocLiveData.observeAsState()

                // Calculate Spent Most Items
                val spentMostItems = remember(statisticsDoc) {
                    if (statisticsDoc != null && statisticsDoc!!.transactions != null) {
                        calculateSpentMostItems(ArrayList(statisticsDoc!!.transactions.values), wallet?.currency ?: "")
                    } else {
                        emptyList()
                    }
                }

                // Load Latest Transaction (only if wallet exists)
                val latestTransactionLiveData = remember(wallet?.id) {
                    if (wallet?.id != null && wallet.id.isNotEmpty()) {
                        transactionViewModel.loadLatestTransaction(wallet.id)
                    } else {
                        androidx.lifecycle.MutableLiveData(null)
                    }
                }
                val latestTransaction by latestTransactionLiveData.observeAsState()

                // Load Bank Balance
                val bAccount = wallet?.getbAccount()
                val linkedAccId = bAccount?.linked_acc_id

                val balancesLiveData = remember(linkedAccId) {
                    if (linkedAccId != null) {
                        bankApiViewModel.getAccountBalances(linkedAccId)
                    } else {
                        androidx.lifecycle.MutableLiveData(null)
                    }
                }

                val balances by balancesLiveData.observeAsState()

                val bankBalance = balances?.balances?.firstOrNull()?.balanceAmount?.get("amount")
                val bankCurrency = balances?.balances?.firstOrNull()?.balanceAmount?.get("currency")

                // Wallets List for Dialog
                val walletsList by walletsViewModel.loadWallets().observeAsState(arrayListOf())

                HomeScreen(
                    wallet = wallet,
                    latestTransaction = latestTransaction,
                    barChartTransactions = barChartTransactions ?: emptyList(),
                    statisticsDoc = statisticsDoc,
                    spentMostItems = spentMostItems,
                    bankBalance = bankBalance,
                    bankCurrency = bankCurrency,
                    onWalletClick = {
                        val dialog = WalletListDialog(walletsList)
                        dialog.setClickListener(object : WalletListDialog.OnClickListener {
                            override fun onWalletClick(w: Wallet) {
                                prefs.edit { putString("selectedWalletId", w.id) }
                                selectedWalletId = w.id
                                dialog.dismiss()
                            }

                            override fun onAddClick() {
                                // Navigate to Add Wallet
                                // findNavController().navigate(R.id.action_homeFragment_to_addWalletFragment)
                                Toast.makeText(context, "Add Wallet Clicked", Toast.LENGTH_SHORT).show()
                                dialog.dismiss()
                            }

                            override fun onEditClick(w: Wallet) {
                                // Navigate to Edit Wallet
                                // val action = HomeFragmentDirections.actionHomeFragmentToEditWalletFragment(w)
                                // findNavController().navigate(action)
                                Toast.makeText(context, "Edit Wallet Clicked", Toast.LENGTH_SHORT).show()
                                dialog.dismiss()
                            }
                        })
                        dialog.show(parentFragmentManager, "WalletListDialog")
                    },
                    onAdjustBalanceClick = {
                        if (wallet != null) {
                            val dialog = AdjustBalanceDialog(wallet.amount.toString())
                            dialog.setOnDialogClicks(object : AdjustBalanceDialog.OnAdjustBalanceDialogClickListener {
                                override fun onApplyClick(amount: String) {
                                    try {
                                        wallet.amount = amount.toDouble()
                                        walletsViewModel.updateWallet(wallet)
                                        dialog.dismiss()
                                    } catch (_: NumberFormatException) {
                                        Toast.makeText(context, "Invalid amount", Toast.LENGTH_SHORT).show()
                                    }
                                }

                                override fun onCancelClick() {
                                    dialog.dismiss()
                                }
                            })
                            dialog.show(parentFragmentManager, "AdjustBalanceDialog")
                        }
                    },
                    onAddBankClick = {
                        // Navigate to Add Bank
                        // findNavController().navigate(R.id.action_homeFragment_to_selectBankFragment)
                        Toast.makeText(context, "Link Bank Account Feature", Toast.LENGTH_SHORT).show()
                    },
                    onFabClick = {
                        if (wallet != null && wallet.id.isNotEmpty() && wallet.id != "Wallets") {
                            val action = HomeFragmentDirections.actionHomeFragmentToAddTransactionLayout(
                                wallet.id,
                                wallet.currency ?: "$",
                                null,
                                false
                            )
                            findNavController().navigate(action)
                        } else {
                            Toast.makeText(context, "Please create or select a wallet first", Toast.LENGTH_SHORT).show()
                        }
                    },
                    onBarChartClick = {
                        if (wallet != null) {
                            val action = HomeFragmentDirections.actionHomeFragmentToBarDetailsFragment(wallet)
                            findNavController().navigate(action)
                        }
                    },
                    onPieChartClick = {
                        if (wallet != null) {
                            val action = HomeFragmentDirections.actionHomeFragmentToPieDetailsFragment(wallet)
                            findNavController().navigate(action)
                        }
                    },
                    onTransactionClick = {
                        if (wallet != null) {
                            val action = HomeFragmentDirections.actionHomeFragmentToListTransactionsFragment(wallet.id, wallet.currency ?: "$")
                            findNavController().navigate(action)
                        }
                    }
                )
                } // XpenseTheme
            }
        }
    }

    private fun calculateSpentMostItems(transactions: List<Transaction>, currency: String): List<SpentMostItem> {
        val items = ArrayList<SpentMostItem>()
        if (transactions.isEmpty()) return items

        var maxTransaction: Transaction? = null
        val categoryMap = HashMap<String, Double>()
        val dayMap = HashMap<LocalDate, Double>()

        for (t in transactions) {
            if (t.type == Transaction.INCOME_TYPE) continue

            // Max Transaction
            if (maxTransaction == null || (t.amount ?: 0.0) > (maxTransaction?.amount ?: 0.0)) {
                maxTransaction = t
            }

            // Category Sum
            val cat = t.category ?: "Unknown"
            categoryMap[cat] = (categoryMap[cat] ?: 0.0) + (t.amount ?: 0.0)

            // Day Sum
            if (t.date != null) {
                val date = t.date.toInstant().atZone(ZoneOffset.UTC).toLocalDate()
                dayMap[date] = (dayMap[date] ?: 0.0) + (t.amount ?: 0.0)
            }
        }

        // 1. Max Category
        val maxCategoryEntry = categoryMap.maxByOrNull { it.value }
        if (maxCategoryEntry != null) {
            items.add(SpentMostItem("Category", maxCategoryEntry.key, String.format("%.2f %s", maxCategoryEntry.value, currency), ""))
        }

        // 2. Max Day
        val maxDayEntry = dayMap.maxByOrNull { it.value }
        if (maxDayEntry != null) {
            val formatter = DateTimeFormatter.ofPattern("EEE, dd MMM", Locale.getDefault())
            items.add(SpentMostItem("Day", maxDayEntry.key.format(formatter), String.format("%.2f %s", maxDayEntry.value, currency), ""))
        }

        // 3. Max Transaction
        if (maxTransaction != null) {
             items.add(SpentMostItem("Transaction", maxTransaction.title ?: "Unknown", String.format("%.2f %s", maxTransaction.amount, currency), ""))
        }

        return items
    }

    private fun calculateLast7Days(): Pair<Date, Date> {
        val calendar = Calendar.getInstance()
        calendar.set(Calendar.HOUR_OF_DAY, 23)
        calendar.set(Calendar.MINUTE, 59)
        calendar.set(Calendar.SECOND, 59)
        val end = calendar.time

        calendar.add(Calendar.DAY_OF_YEAR, -7)
        calendar.set(Calendar.HOUR_OF_DAY, 0)
        calendar.set(Calendar.MINUTE, 0)
        calendar.set(Calendar.SECOND, 0)
        val start = calendar.time

        return Pair(start, end)
    }
}
