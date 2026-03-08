package com.rokudo.xpense.fragments

import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.compose.runtime.*
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.ui.platform.ComposeView
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.fragment.findNavController
import com.rokudo.xpense.data.retrofit.models.AccountDetails
import com.rokudo.xpense.data.retrofit.models.Institution
import com.rokudo.xpense.data.retrofit.models.Requisition
import com.rokudo.xpense.data.viewmodels.BAccountsViewModel
import com.rokudo.xpense.data.viewmodels.BankApiViewModel
import com.rokudo.xpense.models.BAccount
import com.rokudo.xpense.utils.DatabaseUtils
import com.rokudo.xpense.utils.GoCardlessUtils
import com.rokudo.xpense.utils.PrefsUtils
import com.rokudo.xpense.utils.dialogs.BankAccsListDialog
import com.rokudo.xpense.utils.dialogs.UploadingDialog
import java.time.Duration
import java.util.*
import com.rokudo.xpense.ui.theme.XpenseTheme

class SelectBankFragment : Fragment() {

    private lateinit var bankApiViewModel: BankApiViewModel
    private lateinit var bAccountsViewModel: BAccountsViewModel
    private val bAccount = BAccount()
    private var walletId: String = ""

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        bankApiViewModel = ViewModelProvider(requireActivity())[BankApiViewModel::class.java]
        bAccountsViewModel = ViewModelProvider(requireActivity())[BAccountsViewModel::class.java]

        val args = SelectBankFragmentArgs.fromBundle(requireArguments())
        walletId = args.walletId

        bAccount.walletIds = listOf(walletId)
        bAccount.owner_id = DatabaseUtils.getCurrentUser().uid

        return ComposeView(requireContext()).apply {
            setContent {
                XpenseTheme {
                var banks by remember { mutableStateOf<List<Institution>>(emptyList()) }
                var isLoading by remember { mutableStateOf(true) }

                // Load token and institutions
                LaunchedEffect(Unit) {
                    loadInstitutions()
                }

                // Observe institutions
                val institutionsList by bankApiViewModel.institutionList.observeAsState()
                LaunchedEffect(institutionsList) {
                    institutionsList?.let { institutions ->
                        banks = institutions.toList()
                        isLoading = false
                    }
                }

                SelectBankScreen(
                    banks = banks,
                    isLoading = isLoading,
                    onBackClick = {
                        findNavController().popBackStack()
                    },
                    onBankClick = { institution ->
                        handleBankSelection(institution)
                    }
                )
                } // XpenseTheme
            }
        }
    }

    private fun loadInstitutions() {
        val token = requireContext()
            .getSharedPreferences("PREFS_NAME", Context.MODE_PRIVATE)
            .getString(GoCardlessUtils.TOKEN_PREFS_NAME, "")

        if (token.isNullOrEmpty()) {
            getToken()
        } else {
            bankApiViewModel.institutionList.observe(viewLifecycleOwner) { institutions ->
                if (institutions == null) {
                    getToken()
                }
            }
        }
    }

    private fun getToken() {
        bankApiViewModel.token.observe(viewLifecycleOwner) { token ->
            if (token != null) {
                GoCardlessUtils.TOKEN_VAL = token.access
                PrefsUtils.setToken(requireContext(), GoCardlessUtils.TOKEN_VAL)
                // Reload institutions after getting token
                bankApiViewModel.institutionList
            }
        }
    }

    private fun handleBankSelection(institution: Institution) {
        bAccount.institutionId = institution.id
        bAccount.bankName = institution.name
        bAccount.bankPic = institution.logo

        val euaId = requireContext()
            .getSharedPreferences("PREFS_NAME", Context.MODE_PRIVATE)
            .getString("EUA${institution.id}", "")

        if (euaId.isNullOrEmpty()) {
            createEUA(institution)
        } else {
            bAccount.setEUA_id(euaId)
            getRequisition(institution, euaId, true)
        }
    }

    private fun createEUA(institution: Institution) {
        bankApiViewModel.createEUA(institution.id).observe(viewLifecycleOwner) { eua ->
            if (eua?.id != null) {
                PrefsUtils.setString(requireContext(), "EUA${institution.id}", eua.id)
                bAccount.setEUA_id(eua.id)
                bAccount.setEUA_EndDate(Date(Date().time + Duration.ofDays(eua.access_valid_for_days.toLong()).toMillis()))
                getRequisition(institution, eua.id, true)
            } else {
                Toast.makeText(requireContext(), "Failed to create agreement", Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun getRequisition(institution: Institution, euaId: String, accountSelection: Boolean) {
        val requisitionId = requireContext()
            .getSharedPreferences("PREFS_NAME", Context.MODE_PRIVATE)
            .getString("REQUISITION${institution.id}", "")

        if (requisitionId.isNullOrEmpty()) {
            bankApiViewModel.createRequisition(institution.id, euaId, accountSelection)
                .observe(viewLifecycleOwner) { requisition ->
                    if (requisition?.id != null) {
                        PrefsUtils.setString(requireContext(), "REQUISITION${institution.id}", requisition.id)
                        bAccount.requisition_id = requisition.id
                        getAccounts(requisition)
                    } else {
                        // Check if account selection not supported
                        val error = bankApiViewModel.requisitionError
                        if (error?.contains("Account selection not supported") == true) {
                            getRequisition(institution, euaId, false)
                        }
                    }
                }
        } else {
            bAccount.requisition_id = requisitionId
            getRequisitionDetails(requisitionId)
        }
    }

    private fun getRequisitionDetails(requisitionId: String) {
        bankApiViewModel.getRequisitionDetails(requisitionId)
            .observe(viewLifecycleOwner) { requisition ->
                if (requisition?.id != null) {
                    getAccounts(requisition)
                }
            }
    }

    private fun getAccounts(requisition: Requisition) {
        if (requisition.accounts != null && requisition.accounts.isNotEmpty()) {
            bAccount.accounts = requisition.accounts.toList()
            getAccountsDetails(requisition.accounts.toList())
        } else {
            // Open OAuth link
            requisition.link?.let { link ->
                PrefsUtils.saveBAccountToPrefs(requireContext(), bAccount)
                startActivity(Intent(Intent.ACTION_VIEW, Uri.parse(link)))
            } ?: run {
                Toast.makeText(requireContext(), "No accounts available", Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun getAccountsDetails(accounts: List<String>) {
        val dialog = UploadingDialog("Retrieving Data...")
        dialog.show(parentFragmentManager, "wait")

        val accountDetailsList = mutableListOf<AccountDetails>()

        accounts.forEach { accountId ->
            bankApiViewModel.getAccountDetails(accountId)
                .observe(viewLifecycleOwner) { accountDetails ->
                    if (accountDetails?.account != null) {
                        accountDetailsList.add(accountDetails)
                    }

                    if (accountDetailsList.size == accounts.size) {
                        dialog.dismiss()
                        showAccountSelectionDialog(accountDetailsList)
                    }
                }
        }
    }

    private fun showAccountSelectionDialog(accountDetailsList: List<AccountDetails>) {
        val bankAccsListDialog = BankAccsListDialog(ArrayList(accountDetailsList))
        bankAccsListDialog.show(parentFragmentManager, "BankAccountListDialog")
        bankAccsListDialog.setClickListener { position ->
            val selectedAccount = accountDetailsList[position]
            bAccount.accounts = listOf(selectedAccount.account_id)
            bAccount.linked_acc_id = selectedAccount.account_id
            bAccount.linked_acc_currency = selectedAccount.account.currency
            bAccount.linked_acc_iban = selectedAccount.account.iban

            DatabaseUtils.walletsRef.document(walletId)
                .update("bAccount", bAccount)
                .addOnSuccessListener {
                    Log.d("SelectBankFragment", "Wallet updated with bank account")
                    bankAccsListDialog.dismiss()
                    findNavController().popBackStack()
                }
                .addOnFailureListener {
                    Toast.makeText(requireContext(), "Failed to link account", Toast.LENGTH_SHORT).show()
                }
        }
    }
}

