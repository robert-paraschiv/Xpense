package com.rokudo.xpense.utils.dialogs;

import static com.rokudo.xpense.utils.CategoriesUtil.expenseCategoryList;

import android.annotation.SuppressLint;
import android.app.Dialog;
import android.content.res.ColorStateList;
import android.os.Bundle;
import android.view.View;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.content.ContextCompat;

import com.google.android.material.bottomsheet.BottomSheetDialog;
import com.google.android.material.bottomsheet.BottomSheetDialogFragment;
import com.google.android.material.chip.Chip;
import com.google.android.material.chip.ChipGroup;
import com.rokudo.xpense.R;
import com.rokudo.xpense.models.ExpenseCategory;
import com.rokudo.xpense.utils.CategoriesUtil;

public class CategoryDialog extends BottomSheetDialogFragment {
    private ExpenseCategory selectedCategory;
    private OnClickListener onClickListener;

    public interface OnClickListener {
        void onSaveClick(ExpenseCategory expenseCategory);
    }

    public CategoryDialog(ExpenseCategory selectedCategory) {
        if (selectedCategory == null) {
            this.selectedCategory = expenseCategoryList.get(expenseCategoryList.indexOf(new ExpenseCategory("Groceries")));
        } else {
            this.selectedCategory = selectedCategory;
        }
    }

    public void setClickListener(OnClickListener onClickListener) {
        this.onClickListener = onClickListener;
    }

    @NonNull
    @Override
    public Dialog onCreateDialog(@Nullable Bundle savedInstanceState) {
        @SuppressLint("InflateParams") final View dialogView = getLayoutInflater().inflate(R.layout.dialog_expense_category, null);
        final BottomSheetDialog bottomSheetDialog = new BottomSheetDialog(requireContext(), R.style.CustomBottomSheetDialogTheme);
        ChipGroup categoryChipGroup = dialogView.findViewById(R.id.categoryChipGroup);
        for (int i = 0; i < expenseCategoryList.size(); i++) {
            ExpenseCategory category = expenseCategoryList.get(i);
            if (category.getName().equals("Income")) {
                continue;
            }
            Chip chip = (Chip) getLayoutInflater().inflate(R.layout.item_category, categoryChipGroup, false);
            chip.setText(category.getName());
            chip.setChipIconTint(ColorStateList.valueOf(expenseCategoryList.get(i).getColor()));
            chip.setChipIcon(ContextCompat.getDrawable(requireContext(), category.getResourceId()));
            chip.setChipBackgroundColor(ColorStateList.valueOf(getResources().getColor(R.color.cards_bg_color, requireActivity().getTheme())));
            chip.setElevation(0);
            if (category.getName().equals(selectedCategory.getName())) {
                chip.setChecked(true);
            }

            categoryChipGroup.addView(chip);
        }

        categoryChipGroup.setOnCheckedStateChangeListener((group, checkedIds) -> {
            Chip chip = group.findViewById(checkedIds.get(0));
            if (chip != null)
                selectedCategory = expenseCategoryList.get(
                        expenseCategoryList.indexOf(
                                new ExpenseCategory(chip.getText().toString())
                        )
                );
        });

        dialogView.findViewById(R.id.updateCategoryBtn).setOnClickListener(view -> onClickListener.onSaveClick(selectedCategory));

        bottomSheetDialog.setContentView(dialogView);
        bottomSheetDialog.show();

        return bottomSheetDialog;
    }
}
