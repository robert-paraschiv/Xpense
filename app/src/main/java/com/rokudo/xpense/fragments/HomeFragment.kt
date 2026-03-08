package com.rokudo.xpense.fragments

import android.content.Context
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.platform.ComposeView
import androidx.core.content.edit
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.findNavController
import com.rokudo.xpense.components.AdjustBalanceSheet
import com.rokudo.xpense.components.WalletPickerSheet
import com.rokudo.xpense.data.viewmodels.*
import com.rokudo.xpense.models.Wallet
import com.rokudo.xpense.ui.theme.XpenseTheme
import java.util.*

class HomeFragment : Fragment() {

    private lateinit var walletsViewModel: WalletsViewModel
    private lateinit var transactionViewModel: TransactionViewModel
    private lateinit var statisticsViewModel: StatisticsViewModel
    private lateinit var bankApiViewModel: BankApiViewModel
    private lateinit var homeViewModel: HomeViewModel

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        walletsViewModel = ViewModelProvider(requireActivity())[WalletsViewModel::class.java]
        transactionViewModel = ViewModelProvider(requireActivity())[TransactionViewModel::class.java]
        statisticsViewModel = ViewModelProvider(requireActivity())[StatisticsViewModel::class.java]
        bankApiViewModel = ViewModelProvider(requireActivity())[BankApiViewModel::class.java]
        homeViewModel = ViewModelProvider(this)[HomeViewModel::class.java]

        val prefs = requireContext().getSharedPreferences("PREFS_NAME", Context.MODE_PRIVATE)
        val savedWalletId = prefs.getString("selectedWalletId", "") ?: ""
        homeViewModel.onEvent(HomeEvent.Init(savedWalletId))

        return ComposeView(requireContext()).apply {
            setContent {
                XpenseTheme {
                    val homeState by homeViewModel.state.collectAsState()

                    // State for showing bottom sheets
                    var showWalletPicker by remember { mutableStateOf(false) }
                    var showAdjustBalance by remember { mutableStateOf(false) }

                    // Bridge LiveData → MVI State
                    val walletState by walletsViewModel.loadWallet(homeState.selectedWalletId).observeAsState()
                    LaunchedEffect(walletState) {
                        homeViewModel.onEvent(HomeEvent.WalletLoaded(walletState))
                    }

                    val wallet = homeState.wallet
                    val (startDate, endDate) = remember { calculateLast7Days() }

                    // Bar chart transactions
                    val barChartLiveData = remember(wallet?.id) {
                        if (wallet?.id != null && wallet.id.isNotEmpty() && wallet.id != "Wallets") {
                            transactionViewModel.loadTransactionsDateInterval(wallet.id, startDate, endDate)
                        } else {
                            androidx.lifecycle.MutableLiveData(emptyList())
                        }
                    }
                    val barChartTx by barChartLiveData.observeAsState(emptyList())
                    LaunchedEffect(barChartTx) {
                        homeViewModel.onEvent(HomeEvent.BarChartTransactionsLoaded(barChartTx ?: emptyList()))
                    }

                    // Statistics
                    val statsLiveData = remember(wallet?.id) {
                        if (wallet?.id != null && wallet.id.isNotEmpty() && wallet.id != "Wallets") {
                            statisticsViewModel.listenForStatisticsDoc(wallet.id, Date())
                        } else {
                            androidx.lifecycle.MutableLiveData(null)
                        }
                    }
                    val stats by statsLiveData.observeAsState()
                    LaunchedEffect(stats) {
                        homeViewModel.onEvent(HomeEvent.StatisticsLoaded(stats))
                        // Use statistics transactions as recent transactions
                        val recentTxs = stats?.transactions?.values?.toList() ?: emptyList()
                        homeViewModel.onEvent(HomeEvent.RecentTransactionsLoaded(recentTxs))
                    }

                    // Latest transaction
                    val latestTxLiveData = remember(wallet?.id) {
                        if (wallet?.id != null && wallet.id.isNotEmpty()) {
                            transactionViewModel.loadLatestTransaction(wallet.id)
                        } else {
                            androidx.lifecycle.MutableLiveData(null)
                        }
                    }
                    val latestTx by latestTxLiveData.observeAsState()
                    LaunchedEffect(latestTx) {
                        homeViewModel.onEvent(HomeEvent.LatestTransactionLoaded(latestTx))
                    }

                    // Bank balance
                    val bAccount = wallet?.getbAccount()
                    val linkedAccId = bAccount?.linked_acc_id
                    val balancesLiveData = remember(linkedAccId) {
                        if (linkedAccId != null) bankApiViewModel.getAccountBalances(linkedAccId)
                        else androidx.lifecycle.MutableLiveData(null)
                    }
                    val balances by balancesLiveData.observeAsState()
                    LaunchedEffect(balances) {
                        val bankBal = balances?.balances?.firstOrNull()?.balanceAmount?.get("amount")
                        val bankCur = balances?.balances?.firstOrNull()?.balanceAmount?.get("currency")
                        homeViewModel.onEvent(HomeEvent.BankBalanceLoaded(bankBal, bankCur))
                    }

                    // Wallets list for picker
                    val walletsList by walletsViewModel.loadWallets().observeAsState(arrayListOf())

                    // Collect effects
                    LaunchedEffect(Unit) {
                        homeViewModel.effect.collect { effect ->
                            when (effect) {
                                is HomeEffect.ShowWalletDialog -> {
                                    showWalletPicker = true
                                }
                                is HomeEffect.ShowAdjustBalanceDialog -> {
                                    showAdjustBalance = true
                                }
                                else -> handleEffect(effect, homeState, prefs)
                            }
                        }
                    }

                    HomeScreen(
                        wallet = homeState.wallet,
                        latestTransaction = homeState.latestTransaction,
                        recentTransactions = homeState.recentTransactions,
                        barChartTransactions = homeState.barChartTransactions,
                        statisticsDoc = homeState.statisticsDoc,
                        spentMostItems = homeState.spentMostItems,
                        bankBalance = homeState.bankBalance,
                        bankCurrency = homeState.bankCurrency,
                        monthlySpent = homeState.monthlySpent,
                        monthlyIncome = homeState.monthlyIncome,
                        topCategories = homeState.topCategories,
                        onWalletClick = { homeViewModel.onEvent(HomeEvent.WalletClicked) },
                        onAdjustBalanceClick = { homeViewModel.onEvent(HomeEvent.AdjustBalanceClicked) },
                        onAddBankClick = { homeViewModel.onEvent(HomeEvent.AddBankClicked) },
                        onFabClick = { homeViewModel.onEvent(HomeEvent.FabClicked) },
                        onBarChartClick = { homeViewModel.onEvent(HomeEvent.BarChartClicked) },
                        onPieChartClick = { homeViewModel.onEvent(HomeEvent.PieChartClicked) },
                        onTransactionClick = { homeViewModel.onEvent(HomeEvent.TransactionsClicked) },
                        onSettingsClick = { homeViewModel.onEvent(HomeEvent.SettingsClicked) }
                    )

                    // ─── Compose Wallet Picker Bottom Sheet ───
                    if (showWalletPicker) {
                        WalletPickerSheet(
                            wallets = walletsList,
                            onWalletClick = { w ->
                                prefs.edit { putString("selectedWalletId", w.id) }
                                homeViewModel.onEvent(HomeEvent.Init(w.id))
                                showWalletPicker = false
                            },
                            onAddClick = {
                                Toast.makeText(requireContext(), "Add Wallet", Toast.LENGTH_SHORT).show()
                                showWalletPicker = false
                            },
                            onEditClick = { w ->
                                Toast.makeText(requireContext(), "Edit Wallet", Toast.LENGTH_SHORT).show()
                                showWalletPicker = false
                            },
                            onDismiss = {
                                showWalletPicker = false
                            }
                        )
                    }

                    // ─── Compose Adjust Balance Bottom Sheet ───
                    if (showAdjustBalance && wallet != null) {
                        AdjustBalanceSheet(
                            currentBalance = wallet.amount ?: 0.0,
                            currency = wallet.currency ?: "$",
                            onApply = { newBalance ->
                                wallet.amount = newBalance
                                walletsViewModel.updateWallet(wallet)
                                showAdjustBalance = false
                            },
                            onDismiss = {
                                showAdjustBalance = false
                            }
                        )
                    }
                }
            }
        }
    }

    private fun handleEffect(
        effect: HomeEffect,
        state: HomeState,
        prefs: android.content.SharedPreferences
    ) {
        val wallet = state.wallet
        when (effect) {
            is HomeEffect.ShowWalletDialog -> { /* handled inline in Compose */ }
            is HomeEffect.ShowAdjustBalanceDialog -> { /* handled inline in Compose */ }
            is HomeEffect.NavigateToAddBank -> {
                Toast.makeText(requireContext(), "Link Bank Account Feature", Toast.LENGTH_SHORT).show()
            }
            is HomeEffect.NavigateToAddTransaction -> {
                if (wallet != null) {
                    val action = HomeFragmentDirections.actionHomeFragmentToAddTransactionLayout(
                        wallet.id, wallet.currency ?: "$", null, false
                    )
                    view?.findNavController()?.navigate(action)
                }
            }
            is HomeEffect.NavigateToBarDetails -> {
                if (wallet != null) {
                    val action = HomeFragmentDirections.actionHomeFragmentToAnalyticsFragment(
                        "bar", wallet, false
                    )
                    view?.findNavController()?.navigate(action)
                }
            }
            is HomeEffect.NavigateToPieDetails -> {
                if (wallet != null) {
                    val action = HomeFragmentDirections.actionHomeFragmentToAnalyticsFragment(
                        "pie", wallet, false
                    )
                    view?.findNavController()?.navigate(action)
                }
            }
            is HomeEffect.NavigateToTransactions -> {
                if (wallet != null) {
                    val action = HomeFragmentDirections.actionHomeFragmentToListTransactionsFragment(
                        wallet.id, wallet.currency ?: "$"
                    )
                    view?.findNavController()?.navigate(action)
                }
            }
            is HomeEffect.NavigateToSettings -> {
                view?.findNavController()?.navigate(
                    HomeFragmentDirections.actionHomeFragmentToSettingsFragment()
                )
            }
            is HomeEffect.ShowToast -> {
                Toast.makeText(requireContext(), effect.message, Toast.LENGTH_SHORT).show()
            }
        }
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
