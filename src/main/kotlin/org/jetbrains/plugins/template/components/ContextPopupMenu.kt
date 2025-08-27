package org.jetbrains.plugins.template.components

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.onClick
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.PopupPositionProvider
import org.jetbrains.jewel.foundation.theme.JewelTheme
import org.jetbrains.jewel.ui.component.Icon
import org.jetbrains.jewel.ui.component.PopupContainer
import org.jetbrains.jewel.ui.component.Text
import org.jetbrains.jewel.ui.icon.IconKey

@Composable
fun ContextPopupMenu(
    popupPositionProvider: PopupPositionProvider,
    onDismissRequest: () -> Unit,
    content: @Composable () -> Unit,
) {
    PopupContainer(
        popupPositionProvider = popupPositionProvider,
        modifier = Modifier.wrapContentSize(),
        onDismissRequest = { onDismissRequest() },
        horizontalAlignment = Alignment.Start
    ) {
        content()
    }
}

@Composable
fun ContextPopupMenuItem(
    actionText: String,
    actionIcon: IconKey,
    onClick: () -> Unit,
) {
    Row(
        modifier = Modifier
            .widthIn(min = 100.dp)
            .padding(8.dp)
            .onClick { onClick() },
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(
            actionIcon,
            contentDescription = null,
            modifier = Modifier.size(16.dp)
        )

        Spacer(modifier = Modifier.width(8.dp))

        Text(
            text = actionText,
            style = JewelTheme.defaultTextStyle
        )
    }
}
