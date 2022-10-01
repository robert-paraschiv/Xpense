package com.rokudo.xpense.utils.dialogs;

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
import com.rokudo.xpense.adapters.WalletsAdapter;
import com.rokudo.xpense.models.Wallet;

import java.util.List;

public class WalletListDialog extends BottomSheetDialogFragment {
    List<Wallet> walletList;

    public WalletListDialog(List<Wallet> walletList) {
        this.walletList = walletList;
    }

    @NonNull
    @Override
    public Dialog onCreateDialog(@Nullable Bundle savedInstanceState) {
        final View dialogView = getLayoutInflater().inflate(R.layout.dialog_wallet_list, null);
        final BottomSheetDialog bottomSheetDialog = new BottomSheetDialog(requireContext(), R.style.CustomBottomSheetDialogTheme);

        RecyclerView recyclerView = dialogView.findViewById(R.id.walletListDialogRv);
        recyclerView.setAdapter(new WalletsAdapter(walletList));
        recyclerView.setLayoutManager(new LinearLayoutManager(requireContext(), RecyclerView.VERTICAL, false));

        bottomSheetDialog.setContentView(dialogView);
        bottomSheetDialog.show();

        return bottomSheetDialog;
    }
}
