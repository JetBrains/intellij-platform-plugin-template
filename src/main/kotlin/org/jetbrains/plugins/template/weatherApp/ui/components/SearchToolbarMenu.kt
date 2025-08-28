package org.jetbrains.plugins.template.weatherApp.ui.components

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.text.input.rememberTextFieldState
import androidx.compose.foundation.text.input.setTextAndPlaceCursorAtEnd
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import org.jetbrains.jewel.ui.component.Icon
import org.jetbrains.jewel.ui.component.OutlinedButton
import org.jetbrains.jewel.ui.component.Text
import org.jetbrains.jewel.ui.icons.AllIconsKeys
import org.jetbrains.plugins.template.ComposeTemplateBundle
import org.jetbrains.plugins.template.components.SearchBarWithAutoCompletion
import org.jetbrains.plugins.template.weatherApp.model.PreviewableItem
import org.jetbrains.plugins.template.weatherApp.model.Searchable
import org.jetbrains.plugins.template.weatherApp.services.SearchAutoCompletionItemProvider

@Composable
fun <T> SearchToolbarMenu(
    searchAutoCompletionItemProvider: SearchAutoCompletionItemProvider<T>,
    confirmButtonText: String = "Confirm",
    onSearchPerformed: (T) -> Unit = {},
    onSearchConfirmed: (T) -> Unit = {},
) where T : Searchable, T : PreviewableItem {
    val isConfirmButtonVisible = remember { mutableStateOf(false) }
    val previewItem = remember { mutableStateOf<T?>(null) }
    val searchTextFieldState = rememberTextFieldState("")

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 2.dp, vertical = 4.dp)
            .wrapContentHeight(),
        horizontalArrangement = Arrangement.End,
        verticalAlignment = Alignment.CenterVertically,
    ) {

        if (isConfirmButtonVisible.value) {
            OutlinedButton(
                onClick = {
                    previewItem.value?.let { onSearchConfirmed(it) }

                    searchTextFieldState.setTextAndPlaceCursorAtEnd("")
                    isConfirmButtonVisible.value = false
                    previewItem.value = null
                },
                modifier = Modifier
                    .align(Alignment.CenterVertically),
            ) {
                Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                    Icon(
                        AllIconsKeys.Actions.AddList,
                        contentDescription = ComposeTemplateBundle.message("weather.app.search.toolbar.menu.add.button.content.description")
                    )
                    Text(confirmButtonText)
                }
            }
        }


        Spacer(modifier = Modifier.width(4.dp))

        SearchBarWithAutoCompletion(
            modifier = Modifier
                .fillMaxWidth(0.4f)
                .align(Alignment.CenterVertically),
            searchAutoCompletionItemProvider = searchAutoCompletionItemProvider,
            textFieldState = searchTextFieldState,
            onClear = {
                isConfirmButtonVisible.value = false
                previewItem.value = null
            },
            onSelectCompletion = { autocompletedItem ->
                isConfirmButtonVisible.value = true
                previewItem.value = autocompletedItem
                onSearchPerformed(autocompletedItem)
            },
        )
    }
}