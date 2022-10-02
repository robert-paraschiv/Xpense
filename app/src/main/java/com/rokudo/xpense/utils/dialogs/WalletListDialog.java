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
import com.rokudo.xpense.adapters.WalletsAdapter;
import com.rokudo.xpense.models.Wallet;

import java.util.List;

public class WalletListDialog extends BottomSheetDialogFragment {
    private final List<Wallet> walletList;
    private OnClickListener onClickListener;

    public interface OnClickListener {
        void onWalletClick(Wallet wallet);

        void onAddClick();

        void onEditClick(Wallet wallet);
    }

    public WalletListDialog(List<Wallet> walletList) {
        this.walletList = walletList;
    }

    public void setClickListener(OnClickListener onClickListener) {
        this.onClickListener = onClickListener;
    }

    @NonNull
    @Override
    public Dialog onCreateDialog(@Nullable Bundle savedInstanceState) {
        @SuppressLint("InflateParams") final View dialogView = getLayoutInflater().inflate(R.layout.dialog_wallet_list, null);
        final BottomSheetDialog bottomSheetDialog = new BottomSheetDialog(requireContext(), R.style.CustomBottomSheetDialogTheme);

        RecyclerView recyclerView = dialogView.findViewById(R.id.walletListDialogRv);
        recyclerView.setAdapter(new WalletsAdapter(walletList, onClickListener));
        recyclerView.setLayoutManager(new LinearLayoutManager(requireContext(), RecyclerView.VERTICAL, false));

        dialogView.findViewById(R.id.addWalletBtn).setOnClickListener(view -> onClickListener.onAddClick());

        bottomSheetDialog.setContentView(dialogView);
        bottomSheetDialog.show();

        return bottomSheetDialog;
    }
}
