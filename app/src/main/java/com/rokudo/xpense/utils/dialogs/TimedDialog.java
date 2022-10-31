package com.rokudo.xpense.utils.dialogs;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.animation.ValueAnimator;
import android.app.Dialog;
import android.os.Bundle;
import android.view.View;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.google.android.material.bottomsheet.BottomSheetDialog;
import com.google.android.material.bottomsheet.BottomSheetDialogFragment;
import com.google.android.material.progressindicator.LinearProgressIndicator;
import com.rokudo.xpense.R;

public class TimedDialog extends BottomSheetDialogFragment {
    private String title;

    public TimedDialog(String title) {
        this.title = title;
    }

    @NonNull
    @Override
    public Dialog onCreateDialog(@Nullable Bundle savedInstanceState) {
        final View dialogView = getLayoutInflater().inflate(R.layout.dialog_timed, null);
        final BottomSheetDialog bottomSheetDialog = new BottomSheetDialog(requireContext(), R.style.CustomBottomSheetDialogTheme);

        LinearProgressIndicator progressIndicator =
                dialogView.findViewById(R.id.progressIndicator);

        ValueAnimator animator = ValueAnimator.ofInt(0, progressIndicator.getMax());
        animator.setDuration(1500);
        animator.addUpdateListener(animation -> progressIndicator.setProgress((Integer) animation.getAnimatedValue()));
        animator.addListener(new AnimatorListenerAdapter() {
            @Override
            public void onAnimationEnd(Animator animation) {
                super.onAnimationEnd(animation);
                // start your activity here
                bottomSheetDialog.dismiss();
            }
        });
        animator.start();

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
