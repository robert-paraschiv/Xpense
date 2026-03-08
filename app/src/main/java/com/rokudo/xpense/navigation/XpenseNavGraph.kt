package com.rokudo.xpense.navigation

import android.content.Intent
import android.provider.MediaStore
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.runtime.*
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalLifecycleOwner
import androidx.lifecycle.ViewModelStoreOwner
import androidx.lifecycle.viewmodel.compose.LocalViewModelStoreOwner
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.navArgument
import com.google.firebase.auth.FirebaseAuth
import com.rokudo.xpense.components.AdjustBalanceSheet
import com.rokudo.xpense.components.CategoryPickerSheet
import com.rokudo.xpense.components.WalletPickerSheet
import com.rokudo.xpense.data.viewmodels.*
import com.rokudo.xpense.fragments.*
import com.rokudo.xpense.models.Invitation
import com.rokudo.xpense.utils.DatabaseUtils
import com.rokudo.xpense.utils.PrefsUtils
import java.util.*

@Composable
fun XpenseNavGraph(navController: NavHostController) {
    // Capture the activity-level ViewModelStoreOwner so all destinations
    // share the same ViewModel instances for data repositories.
    val activityViewModelStoreOwner = LocalViewModelStoreOwner.current!!

    NavHost(
        navController = navController,
        startDestination = Screen.Home.route
    ) {
        // ════════════════════════════════════════
        // HOME
        // ════════════════════════════════════════
        composable(Screen.Home.route) {
            HomeDestination(navController, activityViewModelStoreOwner)
        }

        // ════════════════════════════════════════
        // SETTINGS
        // ════════════════════════════════════════
        composable(Screen.Settings.route) {
            SettingsDestination(navController, activityViewModelStoreOwner)
        }

        // ════════════════════════════════════════
        // ADD / EDIT TRANSACTION
        // ════════════════════════════════════════
        composable(
            route = Screen.AddTransaction.route,
            arguments = listOf(
                navArgument("walletId") { type = NavType.StringType },
                navArgument("currency") { type = NavType.StringType },
                navArgument("transactionId") { type = NavType.StringType; nullable = true; defaultValue = null },
                navArgument("editMode") { type = NavType.BoolType; defaultValue = false }
            )
        ) { backStackEntry ->
            AddTransactionDestination(navController, backStackEntry, activityViewModelStoreOwner)
        }

        // ════════════════════════════════════════
        // LIST TRANSACTIONS
        // ════════════════════════════════════════
        composable(
            route = Screen.ListTransactions.route,
            arguments = listOf(
                navArgument("walletId") { type = NavType.StringType },
                navArgument("walletCurrency") { type = NavType.StringType },
                navArgument("filterType") { type = NavType.StringType; defaultValue = "all" }
            )
        ) { backStackEntry ->
            ListTransactionsDestination(navController, backStackEntry, activityViewModelStoreOwner)
        }

        // ════════════════════════════════════════
        // ANALYTICS
        // ════════════════════════════════════════
        composable(
            route = Screen.Analytics.route,
            arguments = listOf(
                navArgument("walletId") { type = NavType.StringType },
                navArgument("type") { type = NavType.StringType }
            )
        ) { backStackEntry ->
            AnalyticsDestination(navController, backStackEntry, activityViewModelStoreOwner)
        }

        // ════════════════════════════════════════
        // EDIT WALLET
        // ════════════════════════════════════════
        composable(
            route = Screen.EditWallet.route,
            arguments = listOf(
                navArgument("walletId") { type = NavType.StringType; nullable = true; defaultValue = null }
            )
        ) { backStackEntry ->
            EditWalletDestination(navController, backStackEntry, activityViewModelStoreOwner)
        }

        // ════════════════════════════════════════
        // SELECT BANK
        // ════════════════════════════════════════
        composable(
            route = Screen.SelectBank.route,
            arguments = listOf(
                navArgument("walletId") { type = NavType.StringType }
            )
        ) {
            // SelectBank is complex with external browser launches.
            // Keeping it as a simple placeholder for now — can be fully migrated later.
            val context = LocalContext.current
            Toast.makeText(context, "Select Bank — coming soon", Toast.LENGTH_SHORT).show()
            LaunchedEffect(Unit) { navController.popBackStack() }
        }

        // ════════════════════════════════════════
        // CONTACTS
        // ════════════════════════════════════════
        composable(
            route = Screen.Contacts.route,
            arguments = listOf(
                navArgument("walletId") { type = NavType.StringType }
            )
        ) {
            // Contacts requires Android permissions and ContentProvider queries.
            // Keeping as placeholder — can be fully migrated later.
            val context = LocalContext.current
            Toast.makeText(context, "Contacts — coming soon", Toast.LENGTH_SHORT).show()
            LaunchedEffect(Unit) { navController.popBackStack() }
        }
    }
}

// ─────────────────────────────────────────────
// HOME destination
// ─────────────────────────────────────────────
@Composable
private fun HomeDestination(navController: NavHostController, activityVmOwner: ViewModelStoreOwner) {
    val context = LocalContext.current
    val prefs = remember { context.getSharedPreferences("PREFS_NAME", android.content.Context.MODE_PRIVATE) }

    val homeViewModel: HomeViewModel = viewModel()
    // Shared ViewModels scoped to activity so all destinations see the same data
    val walletsViewModel: WalletsViewModel = viewModel(viewModelStoreOwner = activityVmOwner)
    val transactionViewModel: TransactionViewModel = viewModel(viewModelStoreOwner = activityVmOwner)
    val statisticsViewModel: StatisticsViewModel = viewModel(viewModelStoreOwner = activityVmOwner)
    val bankApiViewModel: BankApiViewModel = viewModel(viewModelStoreOwner = activityVmOwner)

    val savedWalletId = remember { prefs.getString("selectedWalletId", "") ?: "" }

    // Track the selected wallet id locally — no MVI round-trip needed
    var selectedWalletId by remember { mutableStateOf(savedWalletId) }

    var showWalletPicker by remember { mutableStateOf(false) }
    var showAdjustBalance by remember { mutableStateOf(false) }

    // ─── Observe LiveData directly, pass to screen ───
    val wallet by walletsViewModel.loadWallet(selectedWalletId).observeAsState()

    val (startDate, endDate) = remember { calculateLast7Days() }

    val barChartLiveData = remember(wallet?.id) {
        if (wallet?.id != null && wallet!!.id.isNotEmpty() && wallet!!.id != "Wallets")
            transactionViewModel.loadTransactionsDateInterval(wallet!!.id, startDate, endDate)
        else androidx.lifecycle.MutableLiveData(emptyList())
    }
    val barChartTx by barChartLiveData.observeAsState(emptyList())

    val statsLiveData = remember(wallet?.id) {
        if (wallet?.id != null && wallet!!.id.isNotEmpty() && wallet!!.id != "Wallets")
            statisticsViewModel.listenForStatisticsDoc(wallet!!.id, Date())
        else androidx.lifecycle.MutableLiveData(null)
    }
    val stats by statsLiveData.observeAsState()

    val latestTxLiveData = remember(wallet?.id) {
        if (wallet?.id != null && wallet!!.id.isNotEmpty())
            transactionViewModel.loadLatestTransaction(wallet!!.id)
        else androidx.lifecycle.MutableLiveData(null)
    }
    val latestTx by latestTxLiveData.observeAsState()

    val bAccount = wallet?.getbAccount()
    val linkedAccId = bAccount?.linked_acc_id
    val balancesLiveData = remember(linkedAccId) {
        if (linkedAccId != null) bankApiViewModel.getAccountBalances(linkedAccId)
        else androidx.lifecycle.MutableLiveData(null)
    }
    val balances by balancesLiveData.observeAsState()
    val bankBal = balances?.balances?.firstOrNull()?.balanceAmount?.get("amount")
    val bankCur = balances?.balances?.firstOrNull()?.balanceAmount?.get("currency")

    val walletsList by walletsViewModel.loadWallets().observeAsState(arrayListOf())

    // ─── Derived values computed once per data change, no extra state updates ───
    val recentTransactions = remember(stats) {
        stats?.transactions?.values?.toList()
            ?.filter { it.date != null }
            ?.sortedByDescending { it.date?.time ?: 0 }
            ?.take(5)
            ?: emptyList()
    }

    val monthlySpent = stats?.totalAmountSpent ?: 0.0

    val monthlyIncome = remember(stats) {
        var income = 0.0
        stats?.transactions?.values?.forEach { t ->
            if (t.type == com.rokudo.xpense.models.Transaction.INCOME_TYPE) {
                income += (t.amount ?: 0.0)
            }
        }
        income
    }

    val topCategories = remember(stats) {
        stats?.amountByCategory
            ?.entries
            ?.filter { it.key != "Income" && it.value > 0 }
            ?.sortedByDescending { it.value }
            ?.take(3)
            ?.map { Pair(it.key, it.value) }
            ?: emptyList()
    }

    // Keep an always-current reference for the effect collector coroutine
    val currentWallet by rememberUpdatedState(wallet)

    // Collect effects (navigation / dialogs only)
    LaunchedEffect(Unit) {
        homeViewModel.effect.collect { effect ->
            when (effect) {
                is HomeEffect.ShowWalletDialog -> showWalletPicker = true
                is HomeEffect.ShowAdjustBalanceDialog -> showAdjustBalance = true
                is HomeEffect.NavigateToAddBank -> Toast.makeText(context, "Link Bank Account Feature", Toast.LENGTH_SHORT).show()
                is HomeEffect.NavigateToAddTransaction -> {
                    val w = currentWallet
                    if (w != null && w.id.isNotEmpty() && w.id != "Wallets") {
                        navController.navigate(Screen.AddTransaction.createRoute(w.id, w.currency ?: "$"))
                    } else {
                        Toast.makeText(context, "Please create or select a wallet first", Toast.LENGTH_SHORT).show()
                    }
                }
                is HomeEffect.NavigateToBarDetails -> {
                    currentWallet?.let { w -> navController.navigate(Screen.Analytics.createRoute(w.id, "bar")) }
                }
                is HomeEffect.NavigateToPieDetails -> {
                    currentWallet?.let { w -> navController.navigate(Screen.Analytics.createRoute(w.id, "pie")) }
                }
                is HomeEffect.NavigateToTransactions -> {
                    currentWallet?.let { w -> navController.navigate(Screen.ListTransactions.createRoute(w.id, w.currency ?: "$")) }
                }
                is HomeEffect.NavigateToExpenses -> {
                    currentWallet?.let { w -> navController.navigate(Screen.ListTransactions.createRoute(w.id, w.currency ?: "$", "Expense")) }
                }
                is HomeEffect.NavigateToIncome -> {
                    currentWallet?.let { w -> navController.navigate(Screen.ListTransactions.createRoute(w.id, w.currency ?: "$", "Income")) }
                }
                is HomeEffect.NavigateToSettings -> navController.navigate(Screen.Settings.route)
                is HomeEffect.ShowToast -> Toast.makeText(context, effect.message, Toast.LENGTH_SHORT).show()
            }
        }
    }

    HomeScreen(
        wallet = wallet,
        latestTransaction = latestTx,
        recentTransactions = recentTransactions,
        barChartTransactions = barChartTx ?: emptyList(),
        statisticsDoc = stats,
        spentMostItems = emptyList(),
        bankBalance = bankBal,
        bankCurrency = bankCur,
        monthlySpent = monthlySpent,
        monthlyIncome = monthlyIncome,
        topCategories = topCategories,
        onWalletClick = { homeViewModel.onEvent(HomeEvent.WalletClicked) },
        onAdjustBalanceClick = { homeViewModel.onEvent(HomeEvent.AdjustBalanceClicked) },
        onAddBankClick = { homeViewModel.onEvent(HomeEvent.AddBankClicked) },
        onFabClick = { homeViewModel.onEvent(HomeEvent.FabClicked) },
        onBarChartClick = { homeViewModel.onEvent(HomeEvent.BarChartClicked) },
        onPieChartClick = { homeViewModel.onEvent(HomeEvent.PieChartClicked) },
        onTransactionClick = { homeViewModel.onEvent(HomeEvent.TransactionsClicked) },
        onSpentClick = { homeViewModel.onEvent(HomeEvent.SpentClicked) },
        onEarnedClick = { homeViewModel.onEvent(HomeEvent.EarnedClicked) },
        onSettingsClick = { homeViewModel.onEvent(HomeEvent.SettingsClicked) }
    )

    if (showWalletPicker) {
        WalletPickerSheet(
            wallets = walletsList,
            onWalletClick = { w ->
                prefs.edit().putString("selectedWalletId", w.id).apply()
                selectedWalletId = w.id
                showWalletPicker = false
            },
            onAddClick = {
                showWalletPicker = false
                navController.navigate(Screen.EditWallet.createRoute())
            },
            onEditClick = { w ->
                showWalletPicker = false
                navController.navigate(Screen.EditWallet.createRoute(w.id))
            },
            onDismiss = { showWalletPicker = false }
        )
    }

    if (showAdjustBalance && wallet != null) {
        AdjustBalanceSheet(
            currentBalance = wallet!!.amount ?: 0.0,
            currency = wallet!!.currency ?: "$",
            onApply = { newBalance ->
                wallet!!.amount = newBalance
                walletsViewModel.updateWallet(wallet!!)
                showAdjustBalance = false
            },
            onDismiss = { showAdjustBalance = false }
        )
    }
}

// ─────────────────────────────────────────────
// SETTINGS destination
// ─────────────────────────────────────────────
@Composable
private fun SettingsDestination(navController: NavHostController, activityVmOwner: ViewModelStoreOwner) {
    val context = LocalContext.current
    val invitesViewModel: InvitesViewModel = viewModel(viewModelStoreOwner = activityVmOwner)
    val transactionViewModel: TransactionViewModel = viewModel(viewModelStoreOwner = activityVmOwner)
    val walletsViewModel: WalletsViewModel = viewModel(viewModelStoreOwner = activityVmOwner)

    // Observe the current user reactively from Firestore
    var userName by remember { mutableStateOf(DatabaseUtils.getCurrentUser()?.name ?: "User") }
    var userPicUrl by remember { mutableStateOf(DatabaseUtils.getCurrentUser()?.pictureUrl) }

    DisposableEffect(Unit) {
        val firebaseUser = com.google.firebase.auth.FirebaseAuth.getInstance().currentUser
        val phoneNumber = firebaseUser?.phoneNumber
        val registration = if (phoneNumber != null) {
            DatabaseUtils.usersRef.document(phoneNumber)
                .addSnapshotListener { snapshot, _ ->
                    if (snapshot != null && snapshot.exists()) {
                        val user = snapshot.toObject(com.rokudo.xpense.models.User::class.java)
                        if (user != null) {
                            userName = user.name ?: "User"
                            userPicUrl = user.pictureUrl
                            // Keep static reference in sync
                            user.uid = firebaseUser.uid
                            user.phoneNumber = phoneNumber
                            DatabaseUtils.setCurrentUser(user)
                        }
                    }
                }
        } else null
        onDispose { registration?.remove() }
    }

    // Re-load invitations whenever the user becomes available
    val invitationsLiveData = remember(userName) { invitesViewModel.loadInvitations() }
    val invitations by invitationsLiveData.observeAsState(emptyList())

    val pendingInvitations = invitations.filter {
        it.status != Invitation.STATUS_ACCEPTED && it.status != Invitation.STATUS_DECLINED
    }

    val walletsList by walletsViewModel.loadWallets().observeAsState(arrayListOf())
    var showWalletManager by remember { mutableStateOf(false) }

    val firebaseUser = remember { com.google.firebase.auth.FirebaseAuth.getInstance().currentUser }
    val userPhoneNumber = firebaseUser?.phoneNumber

    val galleryLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.StartActivityForResult()
    ) { result ->
        if (result.resultCode == android.app.Activity.RESULT_OK) {
            result.data?.data?.let { uri ->
                uploadProfilePicture(context, uri)
            }
        }
    }

    SettingsScreen(
        userName = userName,
        userProfilePicUrl = userPicUrl,
        userPhoneNumber = userPhoneNumber,
        invitations = pendingInvitations,
        onBackClick = { navController.popBackStack() },
        onSignOutClick = {
            transactionViewModel.removeAllData()
            walletsViewModel.removeAllData()
            PrefsUtils.setSelectedWalletId(context, null)
            FirebaseAuth.getInstance().signOut()
        },
        onProfilePictureClick = {
            val intent = Intent(Intent.ACTION_PICK, android.provider.MediaStore.Images.Media.EXTERNAL_CONTENT_URI)
            galleryLauncher.launch(intent)
        },
        onManageWalletsClick = {
            showWalletManager = true
        },
        onNotificationsClick = {
            try {
                val intent = Intent(android.provider.Settings.ACTION_APP_NOTIFICATION_SETTINGS).apply {
                    putExtra(android.provider.Settings.EXTRA_APP_PACKAGE, context.packageName)
                }
                context.startActivity(intent)
            } catch (_: Exception) {
                Toast.makeText(context, "Unable to open notification settings", Toast.LENGTH_SHORT).show()
            }
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

    if (showWalletManager) {
        WalletPickerSheet(
            wallets = walletsList,
            onWalletClick = { w ->
                showWalletManager = false
                PrefsUtils.setSelectedWalletId(context, w.id)
            },
            onAddClick = {
                showWalletManager = false
                navController.navigate(Screen.EditWallet.createRoute())
            },
            onEditClick = { w ->
                showWalletManager = false
                navController.navigate(Screen.EditWallet.createRoute(w.id))
            },
            onDismiss = { showWalletManager = false }
        )
    }
}

private fun uploadProfilePicture(context: android.content.Context, imageUri: android.net.Uri) {
    try {
        val bitmap = com.rokudo.xpense.utils.RotateBitmap.HandleSamplingAndRotationBitmap(context, imageUri)
        val baos = java.io.ByteArrayOutputStream()
        bitmap.compress(android.graphics.Bitmap.CompressFormat.JPEG, 50, baos)
        val data = baos.toByteArray()

        val userId = FirebaseAuth.getInstance().currentUser?.uid ?: return
        val storageRef = DatabaseUtils.userPicturesRef.child("$userId.jpg")

        storageRef.putBytes(data)
            .addOnSuccessListener {
                storageRef.downloadUrl.addOnSuccessListener { uri ->
                    DatabaseUtils.usersRef.document(userId).update("pictureUrl", uri.toString())
                        .addOnSuccessListener { Toast.makeText(context, "Profile picture updated", Toast.LENGTH_SHORT).show() }
                        .addOnFailureListener { Toast.makeText(context, "Failed to update profile", Toast.LENGTH_SHORT).show() }
                }
            }
            .addOnFailureListener { Toast.makeText(context, "Upload failed", Toast.LENGTH_SHORT).show() }
    } catch (e: Exception) {
        Toast.makeText(context, "Error: ${e.message}", Toast.LENGTH_SHORT).show()
    }
}

// ─────────────────────────────────────────────
// ADD TRANSACTION destination
// ─────────────────────────────────────────────
@Composable
private fun AddTransactionDestination(
    navController: NavHostController,
    backStackEntry: androidx.navigation.NavBackStackEntry,
    activityVmOwner: ViewModelStoreOwner
) {
    val context = LocalContext.current
    val walletId = backStackEntry.arguments?.getString("walletId") ?: ""
    val currency = backStackEntry.arguments?.getString("currency") ?: "$"
    val transactionId = backStackEntry.arguments?.getString("transactionId")
    val editMode = backStackEntry.arguments?.getBoolean("editMode") ?: false

    if (walletId.isEmpty() || walletId == "Wallets") {
        Toast.makeText(context, "Invalid wallet. Please select a wallet first.", Toast.LENGTH_LONG).show()
        LaunchedEffect(Unit) { navController.popBackStack() }
        return
    }

    val viewModel: AddTransactionViewModel = viewModel()

    // Resolve transaction object from stored statistics if editing
    val statisticsViewModel: StatisticsViewModel = viewModel(viewModelStoreOwner = activityVmOwner)
    val mTransaction = remember(transactionId) {
        if (transactionId != null) {
            statisticsViewModel.storedStatisticsDoc?.transactions?.get(transactionId)
        } else null
    }

    LaunchedEffect(Unit) {
        viewModel.onEvent(AddTransactionEvent.Init(walletId, currency, mTransaction, editMode))
    }

    val state by viewModel.state.collectAsState()
    var showCategoryPicker by remember { mutableStateOf(false) }

    LaunchedEffect(Unit) {
        viewModel.effect.collect { effect ->
            when (effect) {
                is AddTransactionEffect.NavigateBack -> navController.popBackStack()
                is AddTransactionEffect.ShowToast -> Toast.makeText(context, effect.message, Toast.LENGTH_SHORT).show()
                is AddTransactionEffect.ShowCategoryDialog -> showCategoryPicker = true
                is AddTransactionEffect.ShowDeleteConfirmation -> {
                    // TODO: replace ConfirmationDialog with Compose AlertDialog
                    viewModel.onEvent(AddTransactionEvent.OnDeleteConfirmed)
                }
            }
        }
    }

    AddTransactionScreen(state = state, onEvent = viewModel::onEvent)

    if (showCategoryPicker) {
        CategoryPickerSheet(
            selectedCategory = state.selectedCategory,
            onCategorySelected = { category ->
                viewModel.onEvent(AddTransactionEvent.OnCategoryChange(category))
                showCategoryPicker = false
            },
            onDismiss = { showCategoryPicker = false }
        )
    }
}

// ─────────────────────────────────────────────
// LIST TRANSACTIONS destination
// ─────────────────────────────────────────────
@Composable
private fun ListTransactionsDestination(
    navController: NavHostController,
    backStackEntry: androidx.navigation.NavBackStackEntry,
    activityVmOwner: ViewModelStoreOwner
) {
    val walletId = backStackEntry.arguments?.getString("walletId") ?: ""
    val currency = backStackEntry.arguments?.getString("walletCurrency") ?: "$"
    val initialFilter = backStackEntry.arguments?.getString("filterType") ?: "all"

    val statisticsViewModel: StatisticsViewModel = viewModel(viewModelStoreOwner = activityVmOwner)
    val lifecycleOwner = LocalLifecycleOwner.current

    var selectedMonth by remember { mutableStateOf(com.rokudo.xpense.utils.DateUtils.monthYearFormat.format(Date())) }
    var transactions by remember { mutableStateOf<List<com.rokudo.xpense.models.Transaction>>(emptyList()) }
    val availableMonths = remember { generateAvailableMonths() }

    LaunchedEffect(selectedMonth) {
        val date = try {
            com.rokudo.xpense.utils.DateUtils.monthYearFormat.parse(selectedMonth) ?: Date()
        } catch (_: Exception) { Date() }

        statisticsViewModel.loadStatisticsDoc(walletId, date, false)
            .observe(lifecycleOwner) { statsDoc ->
                transactions = if (statsDoc?.transactions != null)
                    statsDoc.transactions.values.sortedByDescending { it.dateLong }
                else emptyList()
            }
    }

    LaunchedEffect(Unit) {
        statisticsViewModel.storedStatisticsDoc?.let { statsDoc ->
            if (statsDoc.transactions != null)
                transactions = statsDoc.transactions.values.sortedByDescending { it.dateLong }
        }
    }

    ListTransactionsScreen(
        transactions = transactions,
        selectedMonth = selectedMonth,
        availableMonths = availableMonths,
        initialFilter = initialFilter,
        onBackClick = { navController.popBackStack() },
        onMonthSelected = { selectedMonth = it },
        onTransactionClick = { transaction ->
            navController.navigate(
                Screen.AddTransaction.createRoute(walletId, currency, transaction.id, true)
            )
        }
    )
}

// ─────────────────────────────────────────────
// ANALYTICS destination
// ─────────────────────────────────────────────
@Composable
private fun AnalyticsDestination(
    navController: NavHostController,
    backStackEntry: androidx.navigation.NavBackStackEntry,
    activityVmOwner: ViewModelStoreOwner
) {
    val walletId = backStackEntry.arguments?.getString("walletId") ?: ""
    val type = backStackEntry.arguments?.getString("type") ?: "pie"

    val statisticsViewModel: StatisticsViewModel = viewModel(viewModelStoreOwner = activityVmOwner)
    val walletsViewModel: WalletsViewModel = viewModel(viewModelStoreOwner = activityVmOwner)

    val wallet by walletsViewModel.loadWallet(walletId).observeAsState()
    val showBarChartInitially = type == "bar"

    var isYearMode by remember { mutableStateOf(false) }
    var showBarChart by remember { mutableStateOf(showBarChartInitially) }
    var selectedDate by remember { mutableStateOf(Date()) }
    var categories by remember { mutableStateOf<List<com.rokudo.xpense.models.ExpenseCategory>>(emptyList()) }

    val dateFormat = if (isYearMode) com.rokudo.xpense.utils.DateUtils.yearFormat else com.rokudo.xpense.utils.DateUtils.monthYearFormat
    val selectedDateStr = dateFormat.format(selectedDate)
    val availableDates = remember(isYearMode) { generateAnalyticsDates(isYearMode) }

    val stableStatsLiveData = remember { androidx.lifecycle.MutableLiveData<com.rokudo.xpense.models.StatisticsDoc?>() }
    val statisticsDoc by stableStatsLiveData.observeAsState()

    DisposableEffect(selectedDate, isYearMode) {
        val sourceLiveData = statisticsViewModel.loadStatisticsDoc(walletId, selectedDate, isYearMode)
        val observer = androidx.lifecycle.Observer<com.rokudo.xpense.models.StatisticsDoc> { doc ->
            stableStatsLiveData.value = doc
        }
        sourceLiveData.observeForever(observer)
        onDispose { sourceLiveData.removeObserver(observer) }
    }

    LaunchedEffect(statisticsDoc) {
        statisticsDoc?.let { doc ->
            categories = buildCategoryList(doc.amountByCategory ?: emptyMap(), doc.categories ?: emptyMap())
        } ?: run { categories = emptyList() }
    }

    val totalSpent = statisticsDoc?.totalAmountSpent ?: 0.0
    val transEntryList = statisticsDoc?.let { doc ->
        val txs = doc.transactions?.values?.toList()?.sortedByDescending { it.dateLong } ?: emptyList()
        com.rokudo.xpense.utils.AnalyticsBarUtils.getTransEntryArrayList(ArrayList(txs), isYearMode)
    } ?: emptyList()

    AnalyticsScreen(
        currency = wallet?.currency ?: "$",
        totalSpent = totalSpent,
        isYearMode = isYearMode,
        selectedDate = selectedDateStr,
        availableDates = availableDates,
        categories = categories,
        transEntryList = transEntryList,
        categoryAmounts = statisticsDoc?.amountByCategory ?: emptyMap(),
        showBarChart = showBarChart,
        onBackClick = { navController.popBackStack() },
        onToggleChart = { showBarChart = !showBarChart },
        onToggleMode = { yearMode -> isYearMode = yearMode; selectedDate = Date() },
        onDateSelected = { dateStr ->
            try { selectedDate = dateFormat.parse(dateStr) ?: Date() } catch (_: Exception) {}
        },
        onBarClick = { },
        onTransactionClick = { transaction ->
            navController.navigate(
                Screen.AddTransaction.createRoute(walletId, wallet?.currency ?: "$", transaction.id, true)
            )
        }
    )
}

// ─────────────────────────────────────────────
// EDIT WALLET destination
// ─────────────────────────────────────────────
@Composable
private fun EditWalletDestination(
    navController: NavHostController,
    backStackEntry: androidx.navigation.NavBackStackEntry,
    activityVmOwner: ViewModelStoreOwner
) {
    val context = LocalContext.current
    val walletId = backStackEntry.arguments?.getString("walletId")
    val walletsViewModel: WalletsViewModel = viewModel(viewModelStoreOwner = activityVmOwner)
    val lifecycleOwner = LocalLifecycleOwner.current

    val wallet by walletsViewModel.loadWallet(walletId ?: "").observeAsState()
    val currencies = listOf("$", "€", "£", "₹", "¥", "₽", "lei", "CHF")

    var showDeleteConfirmation by remember { mutableStateOf(false) }

    EditWalletScreen(
        wallet = wallet,
        currencies = currencies,
        onBackClick = { navController.popBackStack() },
        onSaveClick = { title, amountStr, currency ->
            try {
                val amount = amountStr.toDouble()
                if (wallet == null) {
                    // Add new wallet
                    val docRef = DatabaseUtils.walletsRef.document()
                    val newWallet = com.rokudo.xpense.models.Wallet().apply {
                        id = docRef.id
                        this.amount = amount
                        creation_date = Date()
                        this.currency = currency
                        this.title = title
                        users = listOf(DatabaseUtils.getCurrentUser().uid)
                        creator_id = DatabaseUtils.getCurrentUser().uid
                    }
                    PrefsUtils.setSelectedWalletId(context, newWallet.id)
                    walletsViewModel.addWallet(newWallet).observe(lifecycleOwner) { result ->
                        if (result == "Success") navController.popBackStack()
                        else Toast.makeText(context, "Failed to create wallet", Toast.LENGTH_SHORT).show()
                    }
                } else {
                    wallet!!.title = title
                    wallet!!.amount = amount
                    wallet!!.currency = currency
                    walletsViewModel.updateWallet(wallet!!).observe(lifecycleOwner) { result ->
                        if (result == true) navController.popBackStack()
                        else Toast.makeText(context, "Something went wrong", Toast.LENGTH_SHORT).show()
                    }
                }
            } catch (_: NumberFormatException) {
                Toast.makeText(context, "Invalid amount format", Toast.LENGTH_SHORT).show()
            }
        },
        onDeleteClick = { showDeleteConfirmation = true },
        onInviteClick = {
            wallet?.let { w ->
                navController.navigate(Screen.Contacts.createRoute(w.id))
            }
        }
    )

    if (showDeleteConfirmation && wallet != null) {
        androidx.compose.material3.AlertDialog(
            onDismissRequest = { showDeleteConfirmation = false },
            title = { androidx.compose.material3.Text("Delete Wallet") },
            text = {
                androidx.compose.material3.Text(
                    "WARNING!\nYou will not be able to recover the wallet data.\n" +
                            "Are you sure you want to delete this wallet?"
                )
            },
            confirmButton = {
                androidx.compose.material3.TextButton(onClick = {
                    showDeleteConfirmation = false
                    walletsViewModel.deleteWallet(wallet!!.id).observe(lifecycleOwner) { result ->
                        if (result == true) {
                            Toast.makeText(context, "Wallet deleted successfully", Toast.LENGTH_SHORT).show()
                            navController.popBackStack()
                        } else {
                            Toast.makeText(context, "Could not delete wallet", Toast.LENGTH_SHORT).show()
                        }
                    }
                }) { androidx.compose.material3.Text("Delete", color = androidx.compose.material3.MaterialTheme.colorScheme.error) }
            },
            dismissButton = {
                androidx.compose.material3.TextButton(onClick = { showDeleteConfirmation = false }) {
                    androidx.compose.material3.Text("Cancel")
                }
            }
        )
    }
}


// ─────────────────────────────────────────────
// Helper functions
// ─────────────────────────────────────────────
private fun calculateLast7Days(): Pair<Date, Date> {
    val calendar = Calendar.getInstance()
    calendar.set(Calendar.HOUR_OF_DAY, 23); calendar.set(Calendar.MINUTE, 59); calendar.set(Calendar.SECOND, 59)
    val end = calendar.time
    calendar.add(Calendar.DAY_OF_YEAR, -7)
    calendar.set(Calendar.HOUR_OF_DAY, 0); calendar.set(Calendar.MINUTE, 0); calendar.set(Calendar.SECOND, 0)
    val start = calendar.time
    return Pair(start, end)
}

private fun generateAvailableMonths(): List<String> {
    val months = mutableListOf<String>()
    val calendar = Calendar.getInstance()
    val currentYear = calendar.get(Calendar.YEAR)
    val currentMonth = calendar.get(Calendar.MONTH)
    calendar.set(2022, 0, 1)
    while (true) {
        val year = calendar.get(Calendar.YEAR); val month = calendar.get(Calendar.MONTH)
        if (year > currentYear || (year == currentYear && month > currentMonth)) break
        months.add(com.rokudo.xpense.utils.DateUtils.monthYearFormat.format(calendar.time))
        calendar.add(Calendar.MONTH, 1)
    }
    return months.reversed()
}

private fun generateAnalyticsDates(isYearMode: Boolean): List<String> {
    val dates = mutableListOf<String>()
    val calendar = Calendar.getInstance()
    val currentYear = calendar.get(Calendar.YEAR); val currentMonth = calendar.get(Calendar.MONTH)
    calendar.set(2022, 0, 1)
    val format = if (isYearMode) com.rokudo.xpense.utils.DateUtils.yearFormat else com.rokudo.xpense.utils.DateUtils.monthYearFormat
    while (true) {
        val year = calendar.get(Calendar.YEAR); val month = calendar.get(Calendar.MONTH)
        if (year > currentYear || (year == currentYear && month > currentMonth)) break
        dates.add(format.format(calendar.time))
        if (isYearMode) calendar.add(Calendar.YEAR, 1) else calendar.add(Calendar.MONTH, 1)
    }
    return dates.reversed()
}

private fun buildCategoryList(
    amountByCategory: Map<String, Double>,
    transactionsByCategory: Map<String, Map<String, com.rokudo.xpense.models.Transaction>>
): List<com.rokudo.xpense.models.ExpenseCategory> {
    val categoryList = mutableListOf<com.rokudo.xpense.models.ExpenseCategory>()
    val sortedCategories = com.rokudo.xpense.utils.MapUtil.sortByValue(amountByCategory)
    sortedCategories.forEach { (categoryName, categoryAmount) ->
        if (categoryName == "Income" || categoryAmount == 0.0 || !transactionsByCategory.containsKey(categoryName)) return@forEach
        val transactions = transactionsByCategory[categoryName]?.values?.toList()?.sortedByDescending { it.dateLong } ?: emptyList()
        val expenseCategory = com.rokudo.xpense.models.ExpenseCategory(categoryName).apply {
            transactionList = transactions; amount = categoryAmount
        }
        if (com.rokudo.xpense.utils.CategoriesUtil.expenseCategoryList.contains(expenseCategory)) {
            val matching = com.rokudo.xpense.utils.CategoriesUtil.expenseCategoryList[
                com.rokudo.xpense.utils.CategoriesUtil.expenseCategoryList.indexOf(expenseCategory)
            ]
            expenseCategory.resourceId = matching.resourceId; expenseCategory.color = matching.color
            categoryList.add(expenseCategory)
        }
    }
    return categoryList
}

