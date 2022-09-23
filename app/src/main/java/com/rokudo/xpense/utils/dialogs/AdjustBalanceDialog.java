package com.rokudo.xpense.utils.dialogs;

import android.app.Dialog;
import android.content.Context;
import android.os.Bundle;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.EditText;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.DialogFragment;

import com.google.android.material.dialog.MaterialAlertDialogBuilder;
import com.rokudo.xpense.R;

public class AdjustBalanceDialog extends DialogFragment {
    private OnAdjustBalanceDialogClickListener onDialogClicks;

    String amount;

    public AdjustBalanceDialog(String amount) {
        this.amount = amount;
    }

    public void setOnDialogClicks(OnAdjustBalanceDialogClickListener onDialogClicks) {
        this.onDialogClicks = onDialogClicks;
    }

    public interface OnAdjustBalanceDialogClickListener {
        void onApplyClick(String amount);

        void onCancelClick();
    }

    @NonNull
    @Override
    public Dialog onCreateDialog(@Nullable Bundle savedInstanceState) {
        View view = View.inflate(requireContext(), R.layout.dialog_adjust_ballance, null);
        MaterialAlertDialogBuilder dialog = new MaterialAlertDialogBuilder(requireContext(), R.style.MaterialAlertDialog_rounded)
                .setView(view)
                .setCancelable(false);

        EditText balanceEt = view.findViewById(R.id.balance_editText);
        balanceEt.setText(amount);

        balanceEt.postDelayed(() -> {
            InputMethodManager imm = (InputMethodManager) requireContext().getSystemService(Context.INPUT_METHOD_SERVICE);
            imm.toggleSoftInput(InputMethodManager.SHOW_FORCED, InputMethodManager.HIDE_IMPLICIT_ONLY);
        }, 200);

        view.findViewById(R.id.cancel_btn).setOnClickListener(view1 -> onDialogClicks.onCancelClick());
        view.findViewById(R.id.apply_btn).setOnClickListener(view1 -> onDialogClicks.onApplyClick(balanceEt.getText().toString()));

        balanceEt.requestFocus();
        return dialog.create();
    }
}
