package com.rokudo.xpense.utils.dialogs;

import android.app.Dialog;
import android.os.Bundle;
import android.view.View;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.google.android.material.bottomsheet.BottomSheetDialog;
import com.google.android.material.bottomsheet.BottomSheetDialogFragment;
import com.google.android.material.button.MaterialButton;
import com.rokudo.xpense.R;

import java.text.SimpleDateFormat;
import java.time.Duration;
import java.util.Date;
import java.util.Locale;

public class AgreementExpiredDialog extends BottomSheetDialogFragment {

    private onBtnClickListener onBtnClickListener;
    private final Date creationDate;
    private final Date endDate;

    public AgreementExpiredDialog(Date creationDate, Date endDate) {
        this.creationDate = creationDate;
        this.endDate = endDate;
    }

    public interface onBtnClickListener {
        void onYesClick();

        void onNoClick();
    }

    public void setOnBtnClickListener(AgreementExpiredDialog.onBtnClickListener onBtnClickListener) {
        this.onBtnClickListener = onBtnClickListener;
    }

    @NonNull
    @Override
    public Dialog onCreateDialog(@Nullable Bundle savedInstanceState) {
        final View dialogView = getLayoutInflater().inflate(R.layout.dialog_bank_agreement_expired, null);
        final BottomSheetDialog bottomSheetDialog = new BottomSheetDialog(requireContext(), R.style.CustomBottomSheetDialogTheme);

        SimpleDateFormat simpleDateFormat = new SimpleDateFormat("dd MMMM yyyy", Locale.getDefault());
        if (creationDate != null) {
            ((TextView) dialogView.findViewById(R.id.creation_date)).setText(simpleDateFormat.format(creationDate));
        }

        if (endDate != null) {
            ((TextView) dialogView.findViewById(R.id.end_date)).setText(simpleDateFormat.format(endDate));
        }

        MaterialButton yesBtn = dialogView.findViewById(R.id.yesBtn);
        MaterialButton noBtn = dialogView.findViewById(R.id.noBtn);
        yesBtn.setOnClickListener(view -> {
            if (onBtnClickListener != null) {
                onBtnClickListener.onYesClick();
            }
        });
        noBtn.setOnClickListener(view -> {
            if (onBtnClickListener != null) {
                onBtnClickListener.onNoClick();
            }
        });
        bottomSheetDialog.setContentView(dialogView);
        bottomSheetDialog.setCancelable(false);
        bottomSheetDialog.setCanceledOnTouchOutside(false);
        bottomSheetDialog.getBehavior().setDraggable(false);
        bottomSheetDialog.getBehavior().setHideable(false);
        bottomSheetDialog.show();

        return bottomSheetDialog;
    }
}
