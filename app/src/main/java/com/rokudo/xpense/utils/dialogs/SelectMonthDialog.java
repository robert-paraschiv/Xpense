package com.rokudo.xpense.utils.dialogs;

import android.annotation.SuppressLint;
import android.app.Dialog;
import android.os.Bundle;
import android.view.View;
import android.widget.DatePicker;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.google.android.material.bottomsheet.BottomSheetDialog;
import com.google.android.material.bottomsheet.BottomSheetDialogFragment;
import com.google.android.material.button.MaterialButton;
import com.rokudo.xpense.R;

import java.util.Calendar;
import java.util.Date;

public class SelectMonthDialog extends BottomSheetDialogFragment {
    private OnApplySelectedMonth onApplySelectedMonth;
    private final Date date;

    public SelectMonthDialog(Date selectedDate) {
        date = selectedDate;
    }

    public void setOnApplySelectedMonth(OnApplySelectedMonth onApplySelectedMonth) {
        this.onApplySelectedMonth = onApplySelectedMonth;
    }

    public interface OnApplySelectedMonth {
        void onApply(Date dateSelected);
    }

    @SuppressLint("DiscouragedApi")
    @NonNull
    @Override
    public Dialog onCreateDialog(@Nullable Bundle savedInstanceState) {
        @SuppressLint("InflateParams") final View dialogView = getLayoutInflater().inflate(R.layout.dialog_select_month, null);
        final BottomSheetDialog bottomSheetDialog = new BottomSheetDialog(requireContext(), R.style.CustomBottomSheetDialogTheme);

        DatePicker datePicker = dialogView.findViewById(R.id.simpleDatePicker);
        MaterialButton applyBtn = dialogView.findViewById(R.id.applySelectedBtn);

        Calendar calendar = Calendar.getInstance();
        calendar.setTime(date);
        calendar.set(Calendar.HOUR_OF_DAY, 0);
        calendar.set(Calendar.MINUTE, 0);
        calendar.set(Calendar.SECOND, 0);
        datePicker.updateDate(calendar.get(Calendar.YEAR), calendar.get(Calendar.MONTH), calendar.get(Calendar.DAY_OF_MONTH));
        datePicker.setMaxDate(new Date().getTime());
        datePicker.findViewById(getResources().getIdentifier("day", "id", "android")).setVisibility(View.GONE);
        applyBtn.setOnClickListener(v -> {
            calendar.set(datePicker.getYear(),
                    datePicker.getMonth(),
                    datePicker.getDayOfMonth());
            if (onApplySelectedMonth != null) {
                onApplySelectedMonth.onApply(calendar.getTime());
            }
        });
        bottomSheetDialog.setContentView(dialogView);
        bottomSheetDialog.show();

        return bottomSheetDialog;
    }
}
