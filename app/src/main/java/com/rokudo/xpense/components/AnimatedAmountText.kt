package com.rokudo.xpense.components

import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight

@Composable
fun AnimatedAmountText(
    amount: String,
    modifier: Modifier = Modifier,
    style: TextStyle = MaterialTheme.typography.titleLarge,
    color: Color = MaterialTheme.colorScheme.onSurface,
    fontWeight: FontWeight? = null
) {
    AnimatedContent(
        targetState = amount,
        transitionSpec = {
            (fadeIn(animationSpec = tween(300)) +
                    slideInVertically(animationSpec = tween(300)) { it / 2 })
                .togetherWith(
                    fadeOut(animationSpec = tween(200)) +
                            slideOutVertically(animationSpec = tween(200)) { -it / 2 }
                )
        },
        label = "amount_animation",
        modifier = modifier
    ) { target ->
        Text(
            text = target,
            style = style,
            color = color,
            fontWeight = fontWeight
        )
    }
}

