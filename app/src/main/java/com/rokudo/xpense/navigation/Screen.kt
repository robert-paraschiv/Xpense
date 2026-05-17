package com.rokudo.xpense.navigation

/**
 * Type-safe route definitions for Compose Navigation.
 * Complex objects (Wallet, Transaction, BAccount) are NOT passed —
 * only their IDs. Each destination loads the data it needs from
 * shared ViewModels / repositories.
 */
sealed class Screen(val route: String) {

    object Home : Screen("home")

    object Settings : Screen("settings")

    object AddTransaction : Screen("add_transaction/{walletId}/{currency}?transactionId={transactionId}&editMode={editMode}") {
        fun createRoute(
            walletId: String,
            currency: String,
            transactionId: String? = null,
            editMode: Boolean = false
        ): String {
            val base = "add_transaction/$walletId/$currency"
            val params = mutableListOf<String>()
            if (transactionId != null) params.add("transactionId=$transactionId")
            if (editMode) params.add("editMode=true")
            return if (params.isEmpty()) base else "$base?${params.joinToString("&")}"
        }
    }

    object ListTransactions : Screen("list_transactions/{walletId}/{walletCurrency}?filterType={filterType}") {
        fun createRoute(
            walletId: String,
            walletCurrency: String,
            filterType: String = "all"
        ): String = "list_transactions/$walletId/$walletCurrency?filterType=$filterType"
    }

    object Analytics : Screen("analytics/{walletId}/{type}") {
        fun createRoute(
            walletId: String,
            type: String = "pie"
        ): String = "analytics/$walletId/$type"
    }

    object EditWallet : Screen("edit_wallet?walletId={walletId}") {
        fun createRoute(walletId: String? = null): String =
            if (walletId != null) "edit_wallet?walletId=$walletId" else "edit_wallet"
    }

    object SelectBank : Screen("select_bank/{walletId}") {
        fun createRoute(walletId: String): String = "select_bank/$walletId"
    }

    object BankAccountDetails : Screen("bank_account_details/{bAccountId}") {
        fun createRoute(bAccountId: String): String = "bank_account_details/$bAccountId"
    }

    object Contacts : Screen("contacts/{walletId}") {
        fun createRoute(walletId: String): String = "contacts/$walletId"
    }
}

