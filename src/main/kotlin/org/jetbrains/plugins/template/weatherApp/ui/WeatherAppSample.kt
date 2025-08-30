package org.jetbrains.plugins.template.weatherApp.ui

import androidx.compose.foundation.layout.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.PointerEventType
import androidx.compose.ui.input.pointer.isSecondaryPressed
import androidx.compose.ui.input.pointer.onPointerEvent
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.layout.positionInWindow
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.*
import androidx.compose.ui.window.PopupPositionProvider
import org.jetbrains.jewel.foundation.lazy.SelectableLazyColumn
import org.jetbrains.jewel.foundation.lazy.SelectionMode
import org.jetbrains.jewel.foundation.lazy.itemsIndexed
import org.jetbrains.jewel.foundation.lazy.rememberSelectableLazyListState
import org.jetbrains.jewel.foundation.theme.JewelTheme
import org.jetbrains.jewel.ui.component.*
import org.jetbrains.jewel.ui.icon.IconKey
import org.jetbrains.jewel.ui.icons.AllIconsKeys
import org.jetbrains.plugins.template.ComposeTemplateBundle
import org.jetbrains.plugins.template.components.ContextPopupMenu
import org.jetbrains.plugins.template.components.ContextPopupMenuItem
import org.jetbrains.plugins.template.weatherApp.model.Location
import org.jetbrains.plugins.template.weatherApp.services.SearchAutoCompletionItemProvider
import org.jetbrains.plugins.template.weatherApp.ui.components.SearchToolbarMenu
import org.jetbrains.plugins.template.weatherApp.ui.components.WeatherDetailsCard

@Composable
fun WeatherAppSample(
    myLocationViewModel: MyLocationsViewModelApi,
    weatherViewModelApi: WeatherViewModelApi,
    searchAutoCompletionItemProvider: SearchAutoCompletionItemProvider<Location>
) {
    HorizontalSplitLayout(
        first = {
            LeftColumn(
                myLocationViewModel,
                modifier = Modifier
                    .fillMaxSize()
                    .padding(start = 8.dp, end = 8.dp)
            )
        },
        second = {
            RightColumn(
                myLocationViewModel,
                weatherViewModelApi,
                searchAutoCompletionItemProvider,
                modifier = Modifier
                    .fillMaxSize()
                    .padding(start = 8.dp, end = 8.dp)
            )
        },
        modifier = Modifier
            .fillMaxSize()
            .padding(all = 8.dp),
        firstPaneMinWidth = 100.dp,
        secondPaneMinWidth = 300.dp,
        state = rememberSplitLayoutState(.2f)
    )
}

@Composable
private fun LeftColumn(
    myLocationsViewModelApi: MyLocationsViewModelApi,
    modifier: Modifier = Modifier,
) {
    Column(modifier) {
        GroupHeader(
            ComposeTemplateBundle.message("weather.app.my.locations.header.text"),
            modifier = Modifier
                .wrapContentHeight()
                .fillMaxWidth()
        )

        Spacer(modifier = Modifier.height(10.dp))

        MyLocationsListWithEmptyListPlaceholder(Modifier.fillMaxSize(), myLocationsViewModelApi)
    }
}

@Composable
fun MyLocationsListWithEmptyListPlaceholder(
    modifier: Modifier = Modifier,
    myLocationsViewModelApi: MyLocationsViewModelApi
) {
    val myLocationsUIState =
        myLocationsViewModelApi.myLocationsUIStateFlow.collectAsState(LocationsUIState.empty()).value

    if (!myLocationsUIState.isEmpty) {
        MyLocationList(myLocationsUIState, modifier, myLocationsViewModelApi)
    } else {
        EmptyListPlaceholder(modifier)
    }
}

@Composable
private fun EmptyListPlaceholder(
    modifier: Modifier,
    placeholderText: String = ComposeTemplateBundle.message("weather.app.my.locations.empty.list.placeholder.text"),
    placeholderIcon: IconKey = AllIconsKeys.Actions.AddList
) {
    Column(
        modifier = modifier.fillMaxSize().padding(16.dp),
        verticalArrangement = Arrangement.Top,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Icon(
            placeholderIcon,
            contentDescription = ComposeTemplateBundle.message("weather.app.my.locations.empty.list.placeholder.icon.content.description"),
            Modifier.size(32.dp),
            tint = Color.White
        )

        Spacer(modifier = Modifier.height(8.dp))

        Text(
            text = placeholderText,
            style = JewelTheme.defaultTextStyle,
            textAlign = TextAlign.Center
        )
    }
}

@OptIn(ExperimentalComposeUiApi::class)
@Composable
private fun MyLocationList(
    myLocationsUIState: LocationsUIState,
    modifier: Modifier,
    myLocationsViewModelApi: MyLocationsViewModelApi
) {
    val listState = rememberSelectableLazyListState()
    // JEWEL-938 This will trigger on SelectableLazyColum's `onSelectedIndexesChange` callback
    LaunchedEffect(myLocationsUIState) {
        var lastActiveItemIndex = -1
        val selectedItemKeys = mutableSetOf<String>()
        myLocationsUIState.locations.forEachIndexed { index, location ->
            if (index == myLocationsUIState.selectedIndex) {
                if (lastActiveItemIndex == -1) {
                    // Only the first selected item should be active
                    lastActiveItemIndex = index
                }
                // Must match the key used in the `items()` call's `key` parameter to ensure correct item identity.
                selectedItemKeys.add(location.label)
            }
        }
        // Sets the first selected item as an active item to avoid triggering on click event when user clocks on it
        listState.lastActiveItemIndex = lastActiveItemIndex
        // Sets keys of selected items
        listState.selectedKeys = selectedItemKeys
    }

    SelectableLazyColumn(
        modifier = modifier,
        selectionMode = SelectionMode.Single,
        state = listState,
        onSelectedIndexesChange = { indices ->
            val selectedLocationIndex = indices.firstOrNull() ?: return@SelectableLazyColumn
            myLocationsViewModelApi.onLocationSelected(selectedLocationIndex)
        },
    ) {
        itemsIndexed(
            items = myLocationsUIState.locations,
            key = { _, item -> item.label },
        ) { index, locationItem ->

            Box(Modifier.wrapContentSize()) {
                val showPopup = remember { mutableStateOf(false) }
                val popupPosition = remember { mutableStateOf(IntOffset.Zero) }
                val itemPosition = remember { mutableStateOf(Offset.Zero) }

                SimpleListItem(
                    text = locationItem.label,
                    isSelected = myLocationsUIState.selectedIndex == index,
                    isActive = isActive,
                    modifier = Modifier
                        .onGloballyPositioned { coordinates ->
                            itemPosition.value = coordinates.positionInWindow()
                        }
                        .onPointerEvent(PointerEventType.Press) { pointerEvent ->
                            if (!pointerEvent.buttons.isSecondaryPressed) return@onPointerEvent

                            // Calculate exact click position
                            val clickOffset = pointerEvent.changes.first().position
                            popupPosition.value = IntOffset(
                                x = (itemPosition.value.x + clickOffset.x).toInt(),
                                y = (itemPosition.value.y + clickOffset.y).toInt()
                            )

                            showPopup.value = true
                        }
                )

                if (showPopup.value) {
                    val popupPositionProvider = remember(popupPosition.value) {
                        object : PopupPositionProvider {
                            override fun calculatePosition(
                                anchorBounds: IntRect,
                                windowSize: IntSize,
                                layoutDirection: LayoutDirection,
                                popupContentSize: IntSize
                            ): IntOffset = popupPosition.value
                        }
                    }

                    ContextPopupMenu(
                        popupPositionProvider,
                        onDismissRequest = {
                            showPopup.value = false
                            popupPosition.value = IntOffset.Zero
                            itemPosition.value = Offset.Zero
                        }
                    ) {
                        ContextPopupMenuItem(
                            ComposeTemplateBundle.message("weather.app.context.menu.delete.option"),
                            AllIconsKeys.General.Delete
                        ) {
                            showPopup.value = false

                            myLocationsViewModelApi.onDeleteLocation(locationItem)
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun RightColumn(
    myLocationViewModel: MyLocationsViewModelApi,
    weatherViewModelApi: WeatherViewModelApi,
    searchAutoCompletionItemProvider: SearchAutoCompletionItemProvider<Location>,
    modifier: Modifier = Modifier,
) {
    val weatherForecastData = weatherViewModelApi
        .weatherForecastUIState
        .collectAsState(WeatherForecastUIState.Empty).value

    Column(modifier) {
        SearchToolbarMenu(
            searchAutoCompletionItemProvider = searchAutoCompletionItemProvider,
            confirmButtonText = ComposeTemplateBundle.message("weather.app.search.toolbar.menu.add.button.text"),
            onSearchPerformed = { place ->
                weatherViewModelApi.onLoadWeatherForecast(place)
            },
            onSearchConfirmed = { place ->
                myLocationViewModel.onAddLocation(place)
            }
        )

        WeatherDetailsCard(
            modifier = Modifier
                .fillMaxWidth()
                .align(Alignment.CenterHorizontally),
            weatherForecastData
        ) { location ->
            weatherViewModelApi.onLoadWeatherForecast(location)
        }
    }
}
