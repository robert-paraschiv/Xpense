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
                rgb("#C0FF8C")));
        expenseCategoryList.add(new ExpenseCategory("Restaurant",
                R.drawable.ic_baseline_restaurant_24,
                rgb("#FFF78C")));
        expenseCategoryList.add(new ExpenseCategory("Drinks",
                R.drawable.ic_round_wine_bar_24,
                rgb("#FFD08C")));
        expenseCategoryList.add(new ExpenseCategory("Transport",
                R.drawable.ic_round_directions_car_24,
                rgb("#8CEAFF")));
        expenseCategoryList.add(new ExpenseCategory("Fuel",
                R.drawable.baseline_local_gas_station_24,
                rgb("#80CBC4")));
        expenseCategoryList.add(new ExpenseCategory("Bills",
                R.drawable.ic_round_receipt_24,
                rgb("#FF8C9D")));
        expenseCategoryList.add(new ExpenseCategory("Gifts",
                R.drawable.ic_round_card_giftcard_24,
                rgb("#2ecc71")));
        expenseCategoryList.add(new ExpenseCategory("Medical",
                R.drawable.ic_baseline_medication_24,
                rgb("#f1c40f")));
        expenseCategoryList.add(new ExpenseCategory("Others",
                R.drawable.ic_round_category_24,
                rgb("#e74c3c")));

        expenseCategoryList.add(new ExpenseCategory("Housing",
                R.drawable.baseline_house_24,
                rgb("#4E342E")));

        expenseCategoryList.add(new ExpenseCategory("Clothing",
                R.drawable.baseline_shopping_bag_24,
                rgb("#D500F9")));

        expenseCategoryList.add(new ExpenseCategory("Entertainment",
                R.drawable.baseline_theater_comedy_24,
                rgb("#F57F17")));

        expenseCategoryList.add(new ExpenseCategory("Income",
                R.drawable.round_monetization_on_24,
                rgb("#C0FF8C")));
    }
}
