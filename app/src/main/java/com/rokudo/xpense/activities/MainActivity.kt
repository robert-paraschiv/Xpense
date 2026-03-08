package com.rokudo.xpense.activities

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.compose.rememberNavController
import androidx.work.ExistingPeriodicWorkPolicy
import androidx.work.PeriodicWorkRequest
import androidx.work.WorkManager
import com.google.firebase.FirebaseApp
import com.google.firebase.auth.FirebaseAuth
import com.rokudo.xpense.data.viewmodels.BankApiViewModel
import com.rokudo.xpense.models.BAccount
import com.rokudo.xpense.navigation.XpenseNavGraph
import com.rokudo.xpense.ui.theme.XpenseTheme
import com.rokudo.xpense.utils.DatabaseUtils
import com.rokudo.xpense.utils.GsonHelper
import com.rokudo.xpense.utils.PrefsUtils
import com.rokudo.xpense.workmanager.ApiSyncWorker
import java.util.concurrent.TimeUnit

class MainActivity : ComponentActivity() {

    companion object {
        private const val TAG = "MainActivity"
    }

    private lateinit var authListener: FirebaseAuth.AuthStateListener
    private lateinit var bankApiViewModel: BankApiViewModel

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        FirebaseApp.initializeApp(this)

        val periodicWorkRequest = PeriodicWorkRequest.Builder(
            ApiSyncWorker::class.java, 15, TimeUnit.MINUTES
        ).build()

        WorkManager.getInstance(this).enqueueUniquePeriodicWork(
            "workieworkie", ExistingPeriodicWorkPolicy.KEEP, periodicWorkRequest
        )
        WorkManager.getInstance(this).getWorkInfoByIdLiveData(periodicWorkRequest.id)
            .observe(this) { workInfo ->
                if (workInfo != null) {
                    bankApiViewModel.setBalances(
                        GsonHelper.deserializeBalancesFromJson(
                            workInfo.outputData.getString("response")
                        )
                    )
                }
            }

        bankApiViewModel = ViewModelProvider(this)[BankApiViewModel::class.java]

        setupFirebaseAuth()
        handleIntent(intent)

        setContent {
            XpenseTheme {
                val navController = rememberNavController()
                XpenseNavGraph(navController = navController)
            }
        }
    }

    private fun handleIntent(intent: Intent?) {
        if (intent?.data != null && intent.data?.host == "xpense") {
            val bAccount: BAccount? = PrefsUtils.getSavedObjectFromPreference(this)
            if (bAccount == null) {
                Log.e(TAG, "handleIntent: bank account from prefs was null")
                return
            }

            bankApiViewModel.getRequisitionDetails(bAccount.requisition_id).observe(this) { requisition ->
                if (requisition?.id == null) {
                    Log.e(TAG, "handleIntent: requisition was null")
                } else {
                    if (requisition.accounts.size == 1) {
                        // TODO: add to wallet
                    } else if (requisition.accounts.size > 1) {
                        getAccountsDetails(requisition, bAccount)
                    }
                }
            }
        }
    }

    private fun getAccountsDetails(
        requisition: com.rokudo.xpense.data.retrofit.models.Requisition,
        bAccount: BAccount
    ) {
        val accountDetailsList = mutableListOf<com.rokudo.xpense.data.retrofit.models.AccountDetails>()
        for (i in requisition.accounts.indices) {
            bankApiViewModel.getAccountDetails(requisition.accounts[i]).observe(this) { accountDetails ->
                if (accountDetails?.account == null) {
                    Log.e(TAG, "handleIntent: could not get account details")
                } else {
                    accountDetailsList.add(accountDetails)
                    if (accountDetailsList.size == requisition.accounts.size) {
                        showAccountsListDialog(bAccount, accountDetailsList)
                    }
                }
            }
        }
    }

    private fun showAccountsListDialog(
        bAccount: BAccount,
        accountDetailsList: List<com.rokudo.xpense.data.retrofit.models.AccountDetails>
    ) {
        if (accountDetailsList.isNotEmpty()) {
            val selected = accountDetailsList[0]
            bAccount.accounts = listOf(selected.account_id)
            bAccount.linked_acc_id = selected.account_id
            bAccount.linked_acc_currency = selected.account?.currency
            bAccount.linked_acc_iban = selected.account?.iban

            DatabaseUtils.walletsRef.document(bAccount.walletIds[0])
                .update("bAccount", bAccount)
                .addOnSuccessListener { Log.d(TAG, "Updated wallet with bank account") }
        }
    }

    override fun onStart() {
        super.onStart()
        FirebaseAuth.getInstance().addAuthStateListener(authListener)
    }

    override fun onStop() {
        super.onStop()
        FirebaseAuth.getInstance().removeAuthStateListener(authListener)
    }

    private fun setupFirebaseAuth() {
        authListener = FirebaseAuth.AuthStateListener { firebaseAuth ->
            val user = firebaseAuth.currentUser
            if (user == null) {
                Log.d(TAG, "onAuthStateChanged: signed_out")
                Toast.makeText(this, "Not logged in", Toast.LENGTH_SHORT).show()
                val loginIntent = Intent(this, LoginActivity::class.java).apply {
                    flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
                }
                startActivity(loginIntent)
                finish()
            }
        }
    }
}

