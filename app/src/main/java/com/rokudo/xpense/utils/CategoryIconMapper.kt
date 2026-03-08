package com.rokudo.xpense.utils

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.TrendingUp
import androidx.compose.material.icons.rounded.*
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector

data class CategoryVisual(
    val icon: ImageVector,
    val color: Color,
    val containerColor: Color
)

object CategoryIconMapper {
    private val map = mapOf(
        "Groceries" to CategoryVisual(Icons.Rounded.ShoppingCart, Color(0xFF2E7D32), Color(0xFFE8F5E9)),
        "Restaurant" to CategoryVisual(Icons.Rounded.Restaurant, Color(0xFFC62828), Color(0xFFFFEBEE)),
        "Drinks" to CategoryVisual(Icons.Rounded.LocalBar, Color(0xFFAD1457), Color(0xFFFCE4EC)),
        "Transport" to CategoryVisual(Icons.Rounded.DirectionsCar, Color(0xFF00838F), Color(0xFFE0F7FA)),
        "Fuel" to CategoryVisual(Icons.Rounded.LocalGasStation, Color(0xFF37474F), Color(0xFFECEFF1)),
        "Bills" to CategoryVisual(Icons.Rounded.Receipt, Color(0xFF6A1B9A), Color(0xFFF3E5F5)),
        "Gifts" to CategoryVisual(Icons.Rounded.CardGiftcard, Color(0xFFE65100), Color(0xFFFFF3E0)),
        "Medical" to CategoryVisual(Icons.Rounded.MedicalServices, Color(0xFF1565C0), Color(0xFFE3F2FD)),
        "Housing" to CategoryVisual(Icons.Rounded.Home, Color(0xFF4527A0), Color(0xFFEDE7F6)),
        "Clothing" to CategoryVisual(Icons.Rounded.ShoppingBag, Color(0xFF7B1FA2), Color(0xFFF3E5F5)),
        "Entertainment" to CategoryVisual(Icons.Rounded.TheaterComedy, Color(0xFF00695C), Color(0xFFE0F2F1)),
        "Memeluș" to CategoryVisual(Icons.Rounded.ChildCare, Color(0xFFD81B60), Color(0xFFFCE4EC)),
        "Income" to CategoryVisual(Icons.AutoMirrored.Rounded.TrendingUp, Color(0xFF2E7D32), Color(0xFFE8F5E9)),
        "Transfer" to CategoryVisual(Icons.Rounded.SwapHoriz, Color(0xFF0277BD), Color(0xFFE1F5FE)),
        "Others" to CategoryVisual(Icons.Rounded.Category, Color(0xFF546E7A), Color(0xFFECEFF1))
    )

    private val defaultVisual = CategoryVisual(Icons.Rounded.Category, Color(0xFF546E7A), Color(0xFFECEFF1))

    fun get(categoryName: String?): CategoryVisual =
        if (categoryName == null) defaultVisual else map[categoryName] ?: defaultVisual

    fun getIcon(categoryName: String?): ImageVector = get(categoryName).icon
    fun getColor(categoryName: String?): Color = get(categoryName).color
    fun getContainerColor(categoryName: String?): Color = get(categoryName).containerColor
}



