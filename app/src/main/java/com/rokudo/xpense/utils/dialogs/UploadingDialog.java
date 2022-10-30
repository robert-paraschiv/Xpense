package com.rokudo.xpense.utils.dialogs;

import android.app.Dialog;
import android.os.Bundle;
import android.view.View;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.google.android.material.bottomsheet.BottomSheetDialog;
import com.google.android.material.bottomsheet.BottomSheetDialogFragment;
import com.rokudo.xpense.R;

public class UploadingDialog extends BottomSheetDialogFragment {
    private String title;

    public UploadingDialog(String title) {
        this.title = title;
    }

    @NonNull
    @Override
    public Dialog onCreateDialog(@Nullable Bundle savedInstanceState) {
        final View dialogView = getLayoutInflater().inflate(R.layout.dialog_uploading, null);
        final BottomSheetDialog bottomSheetDialog = new BottomSheetDialog(requireContext(), R.style.CustomBottomSheetDialogTheme);

        if (title != null) {
            ((TextView) dialogView.findViewById(R.id.name)).setText(title);
        }

        bottomSheetDialog.setContentView(dialogView);
        bottomSheetDialog.setCancelable(false);
        bottomSheetDialog.setCanceledOnTouchOutside(false);
        bottomSheetDialog.getBehavior().setDraggable(false);
        bottomSheetDialog.getBehavior().setHideable(false);
        bottomSheetDialog.show();

        return bottomSheetDialog;
    }
}
