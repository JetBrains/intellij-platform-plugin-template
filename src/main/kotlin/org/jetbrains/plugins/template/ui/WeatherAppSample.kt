package org.jetbrains.plugins.template.ui

import androidx.compose.foundation.layout.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import org.jetbrains.jewel.foundation.theme.JewelTheme
import org.jetbrains.jewel.ui.component.Text


@Composable
fun WeatherAppSample() {
    Column(Modifier
        .fillMaxWidth()
        .heightIn(20.dp)
        .padding(16.dp)) {
        Text(
            "Not yet implemented",
            style = JewelTheme.defaultTextStyle
        )
    }
}