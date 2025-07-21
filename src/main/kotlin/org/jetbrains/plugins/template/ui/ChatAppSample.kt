package org.jetbrains.plugins.template.ui

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import org.jetbrains.jewel.foundation.theme.JewelTheme
import org.jetbrains.jewel.ui.component.Text

@Composable
fun ChatAppSample() {
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