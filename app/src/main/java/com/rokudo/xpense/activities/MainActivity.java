package com.rokudo.xpense.activities;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.lifecycle.ViewModelProvider;
import androidx.navigation.Navigation;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.rokudo.xpense.R;
import com.rokudo.xpense.data.retrofit.models.AccountDetails;
import com.rokudo.xpense.data.retrofit.models.Requisition;
import com.rokudo.xpense.data.viewmodels.BankApiViewModel;
import com.rokudo.xpense.models.BAccount;
import com.rokudo.xpense.utils.DatabaseUtils;
import com.rokudo.xpense.utils.PrefsUtils;
import com.rokudo.xpense.utils.dialogs.BankAccsListDialog;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class MainActivity extends AppCompatActivity {
    private static final String TAG = "MainActivity";

    private FirebaseAuth.AuthStateListener mAuthListener;
    private BankApiViewModel bankApiViewModel;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        bankApiViewModel = new ViewModelProvider(this).get(BankApiViewModel.class);
//        findViewById(R.id.nav_host_fragment).setSystemUiVisibility(View.SYSTEM_UI_FLAG_LAYOUT_STABLE |
//                View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN |
//                View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION);

        setupFirebaseAuth();
        handleIntent(getIntent());
    }

    private void handleIntent(Intent intent) {
        if (intent != null && intent.getData() != null && intent.getData().getHost().equals("xpense")) {
            BAccount bAccount = PrefsUtils.getSavedObjectFromPreference(this);
            BankApiViewModel viewModel = new ViewModelProvider(this).get(BankApiViewModel.class);
            if (bAccount == null) {
                Log.e(TAG, "handleIntent: bank account from prefs was null");
                return;
            }

            viewModel.getRequisitionDetails(bAccount.getRequisition_id()).observe(this, requisition -> {
                if (requisition == null || requisition.getId() == null) {
                    Log.e(TAG, "handleIntent: requisition was null");
                } else {
                    // Show select accounts
                    if (requisition.getAccounts().length == 1) {
                        // TODO: 11/15/2022  add to wallet
                    } else if (requisition.getAccounts().length > 1) {
                        getAccountsDetails(viewModel, requisition, bAccount);
                    }
                }
            });

        }
    }


    private void getAccountsDetails(BankApiViewModel viewModel, Requisition requisition, BAccount bAccount) {
        List<AccountDetails> accountDetailsList = new ArrayList<>();
        for (int i = 0; i < requisition.getAccounts().length; i++) {
            viewModel.getAccountDetails(requisition.getAccounts()[i])
                    .observe(this, accountDetails -> {
                        if (accountDetails == null || accountDetails.getAccount() == null) {
                            Log.e(TAG, "handleIntent: could not get account details ");
                        } else {
                            accountDetailsList.add(accountDetails);
                            if (accountDetailsList.size() == requisition.getAccounts().length) {
                                showAccountsListDialog(requisition, bAccount, accountDetailsList);
                            }
                        }
                    });
        }
    }

    private void showAccountsListDialog(Requisition requisition, BAccount bAccount,
                                        List<AccountDetails> accountDetailsList) {
        BankAccsListDialog bankAccsListDialog = new BankAccsListDialog(accountDetailsList);
        bankAccsListDialog.show(getSupportFragmentManager(), "BankAccountListDialog");
        bankAccsListDialog.setClickListener(position -> {
            Log.d(TAG, "getAccountsDetails: " + requisition.getAccounts()[position]);
            bAccount.setAccounts(new ArrayList<>(
                    Collections.singletonList(accountDetailsList.get(position).getAccount_id())));
            bAccount.setLinked_acc_id(accountDetailsList.get(position).getAccount_id());
            bAccount.setLinked_acc_currency(accountDetailsList.get(position).getAccount().getCurrency());
            bAccount.setLinked_acc_iban(accountDetailsList.get(position).getAccount().getIban());

            DatabaseUtils.walletsRef.document(bAccount.getWalletIds().get(0))
                    .update("bAccount", bAccount)
                    .addOnSuccessListener(unused -> {
                        Log.d(TAG, "getAccountsDetails: updated wallet with bank account");
                        bankAccsListDialog.dismiss();
                    });
        });
    }

    @Override
    protected void onStart() {
        super.onStart();
        FirebaseAuth.getInstance().addAuthStateListener(mAuthListener);
    }

    @Override
    protected void onStop() {
        super.onStop();
        if (mAuthListener != null) {
            FirebaseAuth.getInstance().removeAuthStateListener(mAuthListener);
        }
    }

    private void setupFirebaseAuth() {
        mAuthListener = firebaseAuth -> {
            FirebaseUser user = firebaseAuth.getCurrentUser();
            if (user == null) {
                // User is signed out
                Log.d(TAG, "onAuthStateChanged: signed_out");
                Toast.makeText(this, "Not logged in", Toast.LENGTH_SHORT).show();
                Intent intent = new Intent(MainActivity.this, LoginActivity.class);
                intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                startActivity(intent);
                finish();
            }
        };
    }
}