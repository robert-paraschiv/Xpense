package com.rokudo.xpense.ui.theme

import android.os.Build
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp

private val LightColorScheme = lightColorScheme(
    primary = Blue60,
    onPrimary = Neutral99,
    primaryContainer = Blue90,
    onPrimaryContainer = Blue10,
    secondary = SecondaryBlue60,
    onSecondary = Neutral99,
    secondaryContainer = SecondaryBlue90,
    onSecondaryContainer = Blue10,
    tertiary = Teal40,
    onTertiary = Neutral99,
    tertiaryContainer = Teal90,
    onTertiaryContainer = Blue10,
    error = Error40,
    onError = Neutral99,
    errorContainer = Error90,
    onErrorContainer = Color(0xFF410002),
    background = FragmentsBgLight,
    onBackground = Neutral10,
    surface = CardsBgLight,
    onSurface = Neutral10,
    surfaceVariant = NeutralVariant90,
    onSurfaceVariant = NeutralVariant30,
    outline = NeutralVariant50,
    outlineVariant = NeutralVariant80,
    inverseSurface = Neutral20,
    inverseOnSurface = Neutral95,
    inversePrimary = Blue80,
    surfaceTint = Blue60
)

private val DarkColorScheme = darkColorScheme(
    primary = Blue80,
    onPrimary = Blue20,
    primaryContainer = Blue30,
    onPrimaryContainer = Blue90,
    secondary = SecondaryBlue80,
    onSecondary = Blue20,
    secondaryContainer = SecondaryBlue40,
    onSecondaryContainer = SecondaryBlue90,
    tertiary = Teal80,
    onTertiary = Color(0xFF003829),
    tertiaryContainer = Teal40,
    onTertiaryContainer = Teal90,
    error = Error80,
    onError = Color(0xFF690005),
    errorContainer = Error40,
    onErrorContainer = Error90,
    background = FragmentsBgDark,
    onBackground = Neutral90,
    surface = CardsBgDark,
    onSurface = Neutral90,
    surfaceVariant = NeutralVariant30,
    onSurfaceVariant = NeutralVariant80,
    outline = NeutralVariant60,
    outlineVariant = NeutralVariant30,
    inverseSurface = Neutral90,
    inverseOnSurface = Neutral20,
    inversePrimary = Blue40,
    surfaceTint = Blue80
)

private val XpenseShapes = Shapes(
    extraSmall = RoundedCornerShape(4.dp),
    small = RoundedCornerShape(8.dp),
    medium = RoundedCornerShape(16.dp),
    large = RoundedCornerShape(22.dp),
    extraLarge = RoundedCornerShape(28.dp)
)

@Composable
fun XpenseTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    dynamicColor: Boolean = true,
    content: @Composable () -> Unit
) {
    val colorScheme = when {
        dynamicColor && Build.VERSION.SDK_INT >= Build.VERSION_CODES.S -> {
            val context = LocalContext.current
            if (darkTheme) dynamicDarkColorScheme(context) else dynamicLightColorScheme(context)
        }
        darkTheme -> DarkColorScheme
        else -> LightColorScheme
    }

    MaterialTheme(
        colorScheme = colorScheme,
        typography = XpenseTypography,
        shapes = XpenseShapes,
        content = content
    )
}


