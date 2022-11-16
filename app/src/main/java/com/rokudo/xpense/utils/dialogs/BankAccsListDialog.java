package com.rokudo.xpense.utils.dialogs;

import android.annotation.SuppressLint;
import android.app.Dialog;
import android.os.Bundle;
import android.view.View;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.bottomsheet.BottomSheetDialog;
import com.google.android.material.bottomsheet.BottomSheetDialogFragment;
import com.rokudo.xpense.R;
import com.rokudo.xpense.adapters.BankAccAdapter;
import com.rokudo.xpense.data.retrofit.models.Account;
import com.rokudo.xpense.data.retrofit.models.AccountDetails;

import java.util.List;

public class BankAccsListDialog extends BottomSheetDialogFragment {
    private final List<AccountDetails> accountList;

    OnBAccClickListener onBAccClickListener;

    public interface OnBAccClickListener {
        void onAccountClick(int  position);
    }

    public BankAccsListDialog(List<AccountDetails> accountList) {
        this.accountList = accountList;
    }

    public void setClickListener(OnBAccClickListener onBAccClickListener) {
        this.onBAccClickListener = onBAccClickListener;
    }

    @NonNull
    @Override
    public Dialog onCreateDialog(@Nullable Bundle savedInstanceState) {
        @SuppressLint("InflateParams") final View dialogView = getLayoutInflater().inflate(R.layout.dialog_accounts_list, null);
        final BottomSheetDialog bottomSheetDialog = new BottomSheetDialog(requireContext(), R.style.CustomBottomSheetDialogTheme);

        RecyclerView recyclerView = dialogView.findViewById(R.id.bankAccountsListRv);
        recyclerView.setAdapter(new BankAccAdapter(accountList, onBAccClickListener));
        recyclerView.setLayoutManager(new LinearLayoutManager(requireContext(), RecyclerView.VERTICAL, false));

        bottomSheetDialog.setContentView(dialogView);
        bottomSheetDialog.show();

        return bottomSheetDialog;
    }
}
