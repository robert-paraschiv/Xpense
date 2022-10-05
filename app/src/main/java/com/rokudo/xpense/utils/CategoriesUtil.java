package com.rokudo.xpense.utils;

import com.rokudo.xpense.R;
import com.rokudo.xpense.models.ExpenseCategory;

import java.util.ArrayList;
import java.util.List;

public class CategoriesUtil {
    public static List<ExpenseCategory> categoryList;

    static {
        categoryList = new ArrayList<>();
        categoryList.add(new ExpenseCategory("Groceries", R.drawable.ic_baseline_local_grocery_store_24));
        categoryList.add(new ExpenseCategory("Restaurant", R.drawable.ic_baseline_restaurant_24));
        categoryList.add(new ExpenseCategory("Drinks", R.drawable.ic_round_wine_bar_24));
        categoryList.add(new ExpenseCategory("Transport", R.drawable.ic_round_directions_car_24));
        categoryList.add(new ExpenseCategory("Bills", R.drawable.ic_round_receipt_24));
        categoryList.add(new ExpenseCategory("Gifts", R.drawable.ic_round_card_giftcard_24));
        categoryList.add(new ExpenseCategory("Medical", R.drawable.ic_baseline_medication_24));
        categoryList.add(new ExpenseCategory("Others", R.drawable.ic_round_category_24));
    }
}
