package com.rokudo.xpense.utils;

import static com.github.mikephil.charting.utils.ColorTemplate.rgb;

import android.graphics.Color;

import com.rokudo.xpense.R;
import com.rokudo.xpense.models.ExpenseCategory;

import java.util.ArrayList;
import java.util.List;

public class CategoriesUtil {
    public static List<ExpenseCategory> expenseCategoryList;

    static {
        expenseCategoryList = new ArrayList<>();
        expenseCategoryList.add(new ExpenseCategory("Groceries",
                R.drawable.ic_baseline_local_grocery_store_24,
                Color.rgb(192, 255, 140)));
        expenseCategoryList.add(new ExpenseCategory("Restaurant",
                R.drawable.ic_baseline_restaurant_24,
                Color.rgb(255, 247, 140)));
        expenseCategoryList.add(new ExpenseCategory("Drinks",
                R.drawable.ic_round_wine_bar_24,
                Color.rgb(255, 208, 140)));
        expenseCategoryList.add(new ExpenseCategory("Transport",
                R.drawable.ic_round_directions_car_24,
                Color.rgb(140, 234, 255)));
        expenseCategoryList.add(new ExpenseCategory("Bills",
                R.drawable.ic_round_receipt_24,
                Color.rgb(255, 140, 157)));
        expenseCategoryList.add(new ExpenseCategory("Gifts",
                R.drawable.ic_round_card_giftcard_24,
                rgb("#2ecc71")));
        expenseCategoryList.add(new ExpenseCategory("Medical",
                R.drawable.ic_baseline_medication_24,
                rgb("#f1c40f")));
        expenseCategoryList.add(new ExpenseCategory("Others",
                R.drawable.ic_round_category_24,
                rgb("#e74c3c")));
        expenseCategoryList.add(new ExpenseCategory("Income",
                R.drawable.round_monetization_on_24,
                Color.rgb(192, 255, 140)));
    }

}
