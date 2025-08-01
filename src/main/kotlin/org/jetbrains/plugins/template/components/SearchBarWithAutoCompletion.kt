package org.jetbrains.plugins.template.components

import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.HoverInteraction
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.text.input.TextFieldState
import androidx.compose.foundation.text.input.setTextAndPlaceCursorAtEnd
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.input.key.*
import androidx.compose.ui.input.pointer.PointerIcon
import androidx.compose.ui.input.pointer.pointerHoverIcon
import androidx.compose.ui.layout.onSizeChanged
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.PopupProperties
import kotlinx.coroutines.flow.distinctUntilChanged
import org.jetbrains.jewel.foundation.ExperimentalJewelApi
import org.jetbrains.jewel.ui.component.Icon
import org.jetbrains.jewel.ui.component.PopupMenu
import org.jetbrains.jewel.ui.component.Text
import org.jetbrains.jewel.ui.component.TextField
import org.jetbrains.jewel.ui.icons.AllIconsKeys
import org.jetbrains.plugins.template.weatherApp.model.PreviewableItem
import org.jetbrains.plugins.template.weatherApp.model.Searchable
import org.jetbrains.plugins.template.weatherApp.services.SearchAutoCompletionItemProvider

@OptIn(ExperimentalJewelApi::class)
@Composable
internal fun <T> SearchBarWithAutoCompletion(
    modifier: Modifier = Modifier,
    searchAutoCompletionItemProvider: SearchAutoCompletionItemProvider<T>,
    textFieldState: TextFieldState,
    searchFieldPlaceholder: String = "Type a place name...",
    onClear: () -> Unit = {},
    onSelectCompletion: (T) -> Unit = {},
) where T : Searchable, T : PreviewableItem {
    val focusRequester = remember { FocusRequester() }

    val popupController = remember { CompletionPopupController(searchAutoCompletionItemProvider) }
    val isInputFieldEmpty by remember { derivedStateOf { textFieldState.text.isBlank() } }

    LaunchedEffect(Unit) {
        snapshotFlow { textFieldState.text.toString() }
            .distinctUntilChanged()
            .collect { searchTerm -> popupController.onQueryChanged(searchTerm) }
    }

    Box(
        modifier = modifier
            .padding(8.dp)
    ) {
        var textFieldWidth by remember { mutableIntStateOf(-1) }
        TextField(
            state = textFieldState,
            modifier = Modifier
                .onSizeChanged { coordinates -> textFieldWidth = coordinates.width }
                .fillMaxWidth()
                .handlePopupCompletionKeyEvents(popupController) { item ->
                    textFieldState.setTextAndPlaceCursorAtEnd(item.label)
                    onSelectCompletion(item)
                }
                .focusRequester(focusRequester),
            placeholder = { Text(searchFieldPlaceholder) },
            leadingIcon = {
                Icon(AllIconsKeys.Actions.Find, contentDescription = null, Modifier.padding(end = 8.dp))
            },
            trailingIcon = {
                if (!isInputFieldEmpty) {
                    CloseIconButton {
                        onClear()
                        textFieldState.setTextAndPlaceCursorAtEnd("")
                    }
                }
            },
        )

        if (popupController.isVisible) {
            PopupMenu(
                onDismissRequest = {
                    popupController.reset()
                    true
                },
                horizontalAlignment = Alignment.Start,
                modifier = Modifier
                    // Aligns PopupMenu with TextField
                    .width(with(LocalDensity.current) { textFieldWidth.toDp() }),
                popupProperties = PopupProperties(focusable = false),
            ) {
                popupController.filteredItems.forEach { item ->
                    selectableItem(
                        popupController.isItemSelected(item),
                        onClick = {
                            onSelectCompletion(item)
                            popupController.onItemAutocompleteConfirmed()
                            textFieldState.setTextAndPlaceCursorAtEnd(item.label)
                        },
                    ) {
                        Text(item.label)
                    }
                }
            }
        }
    }
}

@Composable
internal fun CloseIconButton(onClick: () -> Unit) {
    val interactionSource = remember { MutableInteractionSource() }
    var hovered by remember { mutableStateOf(false) }

    LaunchedEffect(interactionSource) {
        interactionSource.interactions.collect {
            when (it) {
                is HoverInteraction.Enter -> hovered = true
                is HoverInteraction.Exit -> hovered = false
            }
        }
    }

    Icon(
        key = if (hovered) AllIconsKeys.Actions.CloseHovered else AllIconsKeys.Actions.Close,
        contentDescription = "Clear",
        modifier = Modifier.pointerHoverIcon(PointerIcon.Default).clickable(
            interactionSource = interactionSource,
            indication = null,
            role = Role.Button,
        ) { onClick() },
    )
}

private class CompletionPopupController<T : Searchable>(
    private val itemsProvider: SearchAutoCompletionItemProvider<T>,
) {
    var selectedItemIndex by mutableIntStateOf(0)
        private set

    /**
     * Ensures a popup is not shown when the user autocompletes an item.
     * Suppresses making popup once onQueryChanged is called after text to TextField is set after autocompletion.
     */
    private var skipPopupShowing by mutableStateOf(false)
    var filteredItems by mutableStateOf(emptyList<T>())
        private set

    val selectedItem: T
        get() = filteredItems[selectedItemIndex]

    var isVisible by mutableStateOf(false)
        private set

    fun onSelectionMovedDown() {
        moveSelectionTo(normalizeIndex(selectedItemIndex + 1))
    }

    fun onSelectionMovedUp() {
        moveSelectionTo(normalizeIndex(selectedItemIndex - 1))
    }

    fun onQueryChanged(searchTerm: String) {
        if (skipPopupShowing) {
            skipPopupShowing = false
            return
        }

        if (searchTerm.isEmpty()) {
            hidePopup()

            return
        }

        updateFilteredItems(itemsProvider.provideSearchableItems(searchTerm))
        moveSelectionToFirstItem()

        if (filteredItems.isNotEmpty()) {
            showPopup()
        } else {
            hidePopup()
        }
    }

    private fun showPopup() {
        isVisible = true
    }

    private fun hidePopup() {
        isVisible = false
    }

    fun reset() {
        moveSelectionToFirstItem()
        hidePopup()
        clearFilteredItems()
    }

    fun isItemSelected(item: T): Boolean = (filteredItems[selectedItemIndex] == item)

    fun onItemAutocompleteConfirmed(): T {
        val selectedItem = this.selectedItem

        skipPopupShowing = true

        reset()

        return selectedItem
    }

    private fun updateFilteredItems(filteredItems: List<T>) {
        this.filteredItems = filteredItems
    }

    private fun clearFilteredItems() {
        filteredItems = emptyList()
    }

    private fun moveSelectionToFirstItem() {
        moveSelectionTo(0)
    }

    private fun moveSelectionTo(index: Int) {
        selectedItemIndex = index
    }

    private fun normalizeIndex(index: Int) = index.coerceIn(0..filteredItems.lastIndex)
}

/**
 * Handles navigation keyboard key events for the completion popup.
 */
private fun <T : Searchable> Modifier.handlePopupCompletionKeyEvents(
    popupController: CompletionPopupController<T>,
    onItemAutocompleteConfirmed: (T) -> Unit = {},
): Modifier {
    return onPreviewKeyEvent { keyEvent ->
        if (keyEvent.type != KeyEventType.KeyDown) return@onPreviewKeyEvent false

        return@onPreviewKeyEvent when (keyEvent.key) {
            Key.Tab, Key.Enter, Key.NumPadEnter -> {
                onItemAutocompleteConfirmed(popupController.onItemAutocompleteConfirmed())
                true
            }

            Key.DirectionUp -> {
                popupController.onSelectionMovedUp()
                true
            }

            Key.DirectionDown -> {
                popupController.onSelectionMovedDown()
                true
            }

            Key.Escape -> {
                popupController.reset()
                true
            }

            else -> false
        }
    }
}