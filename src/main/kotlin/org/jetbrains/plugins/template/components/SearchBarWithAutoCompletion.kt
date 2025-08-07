package org.jetbrains.plugins.template.components

import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.HoverInteraction
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
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
import org.jetbrains.plugins.template.ComposeTemplateBundle
import org.jetbrains.plugins.template.weatherApp.model.PreviewableItem
import org.jetbrains.plugins.template.weatherApp.model.Searchable
import org.jetbrains.plugins.template.weatherApp.services.SearchAutoCompletionItemProvider

@OptIn(ExperimentalJewelApi::class)
@Composable
fun <T> SearchBarWithAutoCompletion(
    modifier: Modifier = Modifier,
    searchAutoCompletionItemProvider: SearchAutoCompletionItemProvider<T>,
    textFieldState: TextFieldState,
    searchFieldPlaceholder: String = "Type a place name...",
    onClear: () -> Unit = {},
    onSelectCompletion: (T) -> Unit = {},
) where T : Searchable, T : PreviewableItem {
    val focusRequester = remember { FocusRequester() }

    val popupController = remember {
        CompletionPopupController(searchAutoCompletionItemProvider) { completionItem ->
            textFieldState.setTextAndPlaceCursorAtEnd(completionItem.item.label)
            onSelectCompletion(completionItem.item)
        }
    }
    val isInputFieldEmpty by remember { derivedStateOf { textFieldState.text.isBlank() } }

    LaunchedEffect(Unit) {
        snapshotFlow { textFieldState.text.toString() }
            .distinctUntilChanged()
            .collect { searchTerm ->
                if (searchTerm.isEmpty()) {
                    onClear()
                }

                popupController.onQueryChanged(searchTerm)
            }
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
                .handlePopupCompletionKeyEvents(popupController)
                .focusRequester(focusRequester),
            placeholder = { Text(searchFieldPlaceholder) },
            leadingIcon = {
                Icon(AllIconsKeys.Actions.Find, contentDescription = null, Modifier.padding(end = 8.dp))
            },
            trailingIcon = {
                if (!isInputFieldEmpty) {
                    CloseIconButton {
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
                popupController.completionItems.forEach { completionItem ->
                    selectableItem(
                        completionItem.isSelected,
                        onClick = {
                            popupController.onItemClicked(completionItem)
                            textFieldState.setTextAndPlaceCursorAtEnd(completionItem.item.label)
                        },
                    ) {
                        Text(completionItem.item.label)
                    }
                }
            }
        }
    }
}

@Composable
fun CloseIconButton(onClick: () -> Unit) {
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
        contentDescription = ComposeTemplateBundle.message("weather.app.clear.button.content.description"),
        modifier = Modifier
            .pointerHoverIcon(PointerIcon.Default)
            .clickable(
                interactionSource = interactionSource,
                indication = null,
                role = Role.Button,
            ) { onClick() },
    )
}


internal data class CompletionItem<T : Searchable>(
    val item: T,
    val isSelected: Boolean,
)

internal class CompletionPopupController<T : Searchable>(
    private val itemsProvider: SearchAutoCompletionItemProvider<T>,
    private val onSelectCompletion: (CompletionItem<T>) -> Unit = {},
) {
    private var selectedItemIndex by mutableIntStateOf(-1)

    /**
     * Ensures a popup is not shown when the user autocompletes an item.
     * Suppresses making popup once onQueryChanged is called after text to TextField is set after autocompletion.
     */
    private var skipPopupShowing by mutableStateOf(false)

    private val _filteredCompletionItems = mutableStateListOf<CompletionItem<T>>()

    val completionItems: List<CompletionItem<T>> get() = _filteredCompletionItems

    val selectedItem: CompletionItem<T>
        get() = _filteredCompletionItems[selectedItemIndex]

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

        val newItems = itemsProvider.provideSearchableItems(searchTerm)
            .map { CompletionItem(it, false) }

        updateFilteredItems(newItems)

        moveSelectionToFirstItem()

        if (completionItems.isNotEmpty()) {
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
        hidePopup()
        moveSelectionToFirstItem()
        clearFilteredItems()
    }

    fun onItemClicked(clickedItem: CompletionItem<T>) {
        doCompleteSelection(clickedItem)
    }

    fun onSelectionConfirmed() {
        doCompleteSelection(this.selectedItem)
    }

    private fun doCompleteSelection(selectedItem: CompletionItem<T>) {
        if (!isVisible) return

        skipPopupShowing = true

        reset()

        onSelectCompletion(selectedItem)
    }

    private fun updateFilteredItems(newItems: List<CompletionItem<T>>) {
        // TODO Can be done in a more efficient way
        clearFilteredItems()
        _filteredCompletionItems.addAll(newItems)
    }

    private fun clearFilteredItems() {
        _filteredCompletionItems.clear()
    }

    private fun moveSelectionToFirstItem() {
        moveSelectionTo(0)
    }

    private fun moveSelectionTo(index: Int) {
        if (index == selectedItemIndex) return

        // Deselect previous item
        val previousIndex = selectedItemIndex
        if (previousIndex in _filteredCompletionItems.indices) {
            _filteredCompletionItems[previousIndex] = _filteredCompletionItems[previousIndex].copy(isSelected = false)
        }

        // Select a new item
        if (index in _filteredCompletionItems.indices) {
            _filteredCompletionItems[index] = _filteredCompletionItems[index].copy(isSelected = true)
        }

        selectedItemIndex = index
    }

    private fun normalizeIndex(index: Int) = index.coerceIn(0..completionItems.lastIndex)
}

/**
 * Handles navigation keyboard key events for the completion popup.
 */
private fun <T : Searchable> Modifier.handlePopupCompletionKeyEvents(
    popupController: CompletionPopupController<T>
): Modifier {
    return onPreviewKeyEvent { keyEvent ->
        if (keyEvent.type != KeyEventType.KeyDown) return@onPreviewKeyEvent false

        return@onPreviewKeyEvent when (keyEvent.key) {
            Key.Tab, Key.Enter, Key.NumPadEnter -> {
                popupController.onSelectionConfirmed()
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