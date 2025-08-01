package org.jetbrains.plugins.template.weatherApp.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Color.Companion.Transparent
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import org.jetbrains.jewel.bridge.retrieveColorOrUnspecified
import org.jetbrains.jewel.foundation.lazy.SelectableLazyColumn
import org.jetbrains.jewel.foundation.lazy.SelectionMode
import org.jetbrains.jewel.foundation.lazy.items
import org.jetbrains.jewel.foundation.lazy.rememberSelectableLazyListState
import org.jetbrains.jewel.foundation.theme.JewelTheme
import org.jetbrains.jewel.ui.component.*
import org.jetbrains.jewel.ui.icon.IconKey
import org.jetbrains.jewel.ui.icons.AllIconsKeys
import org.jetbrains.plugins.template.ComposeTemplateBundle
import org.jetbrains.plugins.template.weatherApp.model.Location
import org.jetbrains.plugins.template.weatherApp.model.SelectableLocation
import org.jetbrains.plugins.template.weatherApp.model.WeatherForecastData
import org.jetbrains.plugins.template.weatherApp.services.MyLocationsViewModelApi
import org.jetbrains.plugins.template.weatherApp.services.SearchAutoCompletionItemProvider
import org.jetbrains.plugins.template.weatherApp.services.WeatherViewModelApi
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
    val myLocations = myLocationsViewModelApi.myLocationsFlow.collectAsState(emptyList()).value

    if (myLocations.isNotEmpty()) {
        MyLocationList(myLocations, modifier, myLocationsViewModelApi)
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

@Composable
private fun MyLocationList(
    myLocations: List<SelectableLocation>,
    modifier: Modifier,
    myLocationsViewModelApi: MyLocationsViewModelApi
) {
    val listState = rememberSelectableLazyListState()
    // JEWEL-938 This will trigger on SelectableLazyColum's `onSelectedIndexesChange` callback
    LaunchedEffect(myLocations) {
        var lastActiveItemIndex = -1
        val selectedItemKeys = mutableSetOf<String>()
        myLocations.forEachIndexed { index, location ->
            if (location.isSelected) {
                if (lastActiveItemIndex == -1) {
                    // Only the first selected item should be active
                    lastActiveItemIndex = index
                }
                // Must match the key used in the `items()` call's `key` parameter to ensure correct item identity.
                selectedItemKeys.add(location.location.label)
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
        items(
            items = myLocations,
            key = { item -> item.location.label },
        ) { item ->

            ContentItemRow(
                item = item.location, isSelected = item.isSelected, isActive = isActive
            )
        }
    }
}

@Composable
private fun ContentItemRow(item: Location, isSelected: Boolean, isActive: Boolean) {
    val color = when {
        isSelected && isActive -> retrieveColorOrUnspecified("List.selectionBackground")
        isSelected && !isActive -> retrieveColorOrUnspecified("List.selectionInactiveBackground")
        else -> Transparent
    }
    Row(
        modifier = Modifier
            .height(JewelTheme.globalMetrics.rowHeight)
            .background(color)
            .padding(horizontal = 4.dp)
            .padding(end = scrollbarContentSafePadding()),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(4.dp),
    ) {
        Text(text = item.label, modifier = Modifier.weight(1f), overflow = TextOverflow.Ellipsis, maxLines = 1)
    }
}

@Composable
private fun RightColumn(
    myLocationViewModel: MyLocationsViewModelApi,
    weatherViewModelApi: WeatherViewModelApi,
    searchAutoCompletionItemProvider: SearchAutoCompletionItemProvider<Location>,
    modifier: Modifier = Modifier,
) {
    val weatherForecastData = weatherViewModelApi.weatherForecast.collectAsState(WeatherForecastData.EMPTY).value

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
