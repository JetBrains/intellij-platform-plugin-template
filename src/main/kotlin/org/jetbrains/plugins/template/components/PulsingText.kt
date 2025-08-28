package org.jetbrains.plugins.template.components

import androidx.compose.animation.core.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.TextUnit
import org.jetbrains.jewel.foundation.theme.JewelTheme
import org.jetbrains.jewel.ui.component.Text

@Composable
fun PulsingText(
    text: String,
    isLoading: Boolean,
    modifier: Modifier = Modifier,
    color: Color = Color.White,
    fontSize: TextUnit = JewelTheme.defaultTextStyle.fontSize,
    fontWeight: FontWeight? = JewelTheme.defaultTextStyle.fontWeight
) {
    val alpha = if (isLoading) {
        val infiniteTransition = rememberInfiniteTransition(label = "pulsing_text")
        infiniteTransition.animateFloat(
            initialValue = 0.3f,
            targetValue = 1f,
            animationSpec = infiniteRepeatable(
                animation = tween(
                    durationMillis = 1000,
                    easing = FastOutSlowInEasing
                ),
                repeatMode = RepeatMode.Reverse
            ),
            label = "text_alpha"
        ).value
    } else {
        1f
    }

    Text(
        text = text,
        color = color.copy(alpha = alpha),
        fontSize = fontSize,
        fontWeight = fontWeight,
        modifier = modifier
    )
}