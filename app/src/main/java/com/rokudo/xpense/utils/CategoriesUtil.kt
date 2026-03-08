package com.rokudo.xpense.utils

import com.rokudo.xpense.R
import com.rokudo.xpense.models.ExpenseCategory

object CategoriesUtil {
    val expenseCategoryList: List<ExpenseCategory> = listOf(
        ExpenseCategory("Groceries", R.drawable.ic_baseline_local_grocery_store_24, 0xFF2E7D32.toInt()),
        ExpenseCategory("Restaurant", R.drawable.ic_baseline_restaurant_24, 0xFFC62828.toInt()),
        ExpenseCategory("Drinks", R.drawable.ic_round_wine_bar_24, 0xFFAD1457.toInt()),
        ExpenseCategory("Transport", R.drawable.ic_round_directions_car_24, 0xFF00838F.toInt()),
        ExpenseCategory("Fuel", R.drawable.baseline_local_gas_station_24, 0xFF37474F.toInt()),
        ExpenseCategory("Bills", R.drawable.ic_round_receipt_24, 0xFF6A1B9A.toInt()),
        ExpenseCategory("Gifts", R.drawable.ic_round_card_giftcard_24, 0xFFE65100.toInt()),
        ExpenseCategory("Medical", R.drawable.ic_baseline_medication_24, 0xFF1565C0.toInt()),
        ExpenseCategory("Others", R.drawable.ic_round_category_24, 0xFF546E7A.toInt()),
        ExpenseCategory("Housing", R.drawable.baseline_house_24, 0xFF4527A0.toInt()),
        ExpenseCategory("Clothing", R.drawable.baseline_shopping_bag_24, 0xFF7B1FA2.toInt()),
        ExpenseCategory("Entertainment", R.drawable.baseline_theater_comedy_24, 0xFF00695C.toInt()),
        ExpenseCategory("Memeluș", R.drawable.outline_child_care_24, 0xFFD81B60.toInt()),
        ExpenseCategory("Income", R.drawable.round_monetization_on_24, 0xFF2E7D32.toInt())
    )
}

