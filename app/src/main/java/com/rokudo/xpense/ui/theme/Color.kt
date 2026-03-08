package com.rokudo.xpense.ui.theme

import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color

// Primary — warm teal/emerald
val Primary10 = Color(0xFF002018)
val Primary20 = Color(0xFF00382B)
val Primary30 = Color(0xFF005140)
val Primary40 = Color(0xFF006B55)
val Primary50 = Color(0xFF00897B)
val Primary60 = Color(0xFF009688)
val Primary70 = Color(0xFF4DB6AC)
val Primary80 = Color(0xFF80CBC4)
val Primary90 = Color(0xFFB2DFDB)
val Primary95 = Color(0xFFE0F2F1)
val Primary99 = Color(0xFFF5FDFB)

// Secondary — warm amber/gold
val Secondary20 = Color(0xFF3E2E00)
val Secondary40 = Color(0xFF7B5E00)
val Secondary60 = Color(0xFFFFB300)
val Secondary80 = Color(0xFFFFD54F)
val Secondary90 = Color(0xFFFFECB3)
val Secondary95 = Color(0xFFFFF8E1)

// Tertiary — soft rose
val Tertiary40 = Color(0xFF8E4585)
val Tertiary80 = Color(0xFFDDA0DD)
val Tertiary90 = Color(0xFFF3E5F5)

// Neutral palette
val Neutral10 = Color(0xFF1C1B1F)
val Neutral20 = Color(0xFF313033)
val Neutral30 = Color(0xFF484649)
val Neutral40 = Color(0xFF605D62)
val Neutral50 = Color(0xFF787579)
val Neutral60 = Color(0xFF939094)
val Neutral80 = Color(0xFFC9C5CA)
val Neutral90 = Color(0xFFE6E1E5)
val Neutral95 = Color(0xFFF4EFF4)
val Neutral99 = Color(0xFFFFFBFE)

// Neutral variant
val NeutralVariant30 = Color(0xFF49454F)
val NeutralVariant50 = Color(0xFF79747E)
val NeutralVariant60 = Color(0xFF938F99)
val NeutralVariant80 = Color(0xFFCAC4D0)
val NeutralVariant90 = Color(0xFFE7E0EC)

// Error
val Error40 = Color(0xFFBA1A1A)
val Error80 = Color(0xFFFFB4AB)
val Error90 = Color(0xFFFFDAD6)

// Semantic
val IncomeGreen = Color(0xFF2E7D32)
val IncomeGreenLight = Color(0xFFE8F5E9)
val ExpenseRed = Color(0xFFC62828)
val ExpenseRedLight = Color(0xFFFFEBEE)

// Surface / background — warm grays
val SurfaceLight = Color(0xFFFFFBFE)
val BackgroundLight = Color(0xFFF5F5F0)
val SurfaceDark = Color(0xFF1C1B1F)
val BackgroundDark = Color(0xFF141316)

// Gradients
object XpenseGradients {
    val headerLight = Brush.verticalGradient(
        colors = listOf(Primary50, Primary70)
    )
    val headerDark = Brush.verticalGradient(
        colors = listOf(Primary20, Primary40)
    )
    val incomeGradient = Brush.horizontalGradient(
        colors = listOf(Color(0xFF43A047), Color(0xFF66BB6A))
    )
    val expenseGradient = Brush.horizontalGradient(
        colors = listOf(Color(0xFFC62828), Color(0xFFEF5350))
    )
}
