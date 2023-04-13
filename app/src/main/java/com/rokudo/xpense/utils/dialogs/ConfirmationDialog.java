package com.rokudo.xpense.utils.dialogs;

import android.app.Dialog;
import android.os.Bundle;
import android.view.View;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.DialogFragment;

import com.google.android.material.dialog.MaterialAlertDialogBuilder;
import com.rokudo.xpense.R;

public class ConfirmationDialog extends DialogFragment {
    public interface OnBtnClick {
        void onConfirm();
    }

    private OnBtnClick onBtnClick;
    String title;

    public void setOnClickListener(OnBtnClick onClickListener) {
        this.onBtnClick = onClickListener;
    }

    public ConfirmationDialog(String title) {
        this.title = title;
    }

    @NonNull
    @Override
    public Dialog onCreateDialog(@Nullable Bundle savedInstanceState) {
        View view = View.inflate(requireContext(), R.layout.dialog_confirmation, null);
        MaterialAlertDialogBuilder dialog = new MaterialAlertDialogBuilder(requireContext(), R.style.MaterialAlertDialog_rounded)
                .setView(view)
                .setCancelable(false);

        ((TextView) view.findViewById(R.id.dialog_confirmation_title)).setText(title);

        view.findViewById(R.id.cancel_btn).setOnClickListener(view1 -> this.dismiss());
        view.findViewById(R.id.confirm_btn).setOnClickListener(view1 -> onBtnClick.onConfirm());

        return dialog.create();
    }
}
