package com.rokudo.xpense.utils;

import static com.github.mikephil.charting.utils.ColorTemplate.rgb;

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
                rgb("#B2FF59")));
        expenseCategoryList.add(new ExpenseCategory("Restaurant",
                R.drawable.ic_baseline_restaurant_24,
                rgb("#F50057")));
        expenseCategoryList.add(new ExpenseCategory("Drinks",
                R.drawable.ic_round_wine_bar_24,
                rgb("#F06292")));
        expenseCategoryList.add(new ExpenseCategory("Transport",
                R.drawable.ic_round_directions_car_24,
                rgb("#18FFFF")));
        expenseCategoryList.add(new ExpenseCategory("Fuel",
                R.drawable.baseline_local_gas_station_24,
                rgb("#37474F")));
        expenseCategoryList.add(new ExpenseCategory("Bills",
                R.drawable.ic_round_receipt_24,
                rgb("#AD1457")));
        expenseCategoryList.add(new ExpenseCategory("Gifts",
                R.drawable.ic_round_card_giftcard_24,
                rgb("#FFFF00")));
        expenseCategoryList.add(new ExpenseCategory("Medical",
                R.drawable.ic_baseline_medication_24,
                rgb("#448AFF")));
        expenseCategoryList.add(new ExpenseCategory("Others",
                R.drawable.ic_round_category_24,
                rgb("#FF6D00")));

        expenseCategoryList.add(new ExpenseCategory("Housing",
                R.drawable.baseline_house_24,
                rgb("#4527A0")));

        expenseCategoryList.add(new ExpenseCategory("Clothing",
                R.drawable.baseline_shopping_bag_24,
                rgb("#9575CD")));

        expenseCategoryList.add(new ExpenseCategory("Entertainment",
                R.drawable.baseline_theater_comedy_24,
                rgb("#69F0AE")));

        expenseCategoryList.add(new ExpenseCategory("Income",
                R.drawable.round_monetization_on_24,
                rgb("#C0FF8C")));
    }
}
