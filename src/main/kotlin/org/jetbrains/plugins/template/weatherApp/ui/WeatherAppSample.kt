package org.jetbrains.plugins.template.weatherApp.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color.Companion.Transparent
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import org.jetbrains.jewel.bridge.retrieveColorOrUnspecified
import org.jetbrains.jewel.foundation.lazy.SelectableLazyColumn
import org.jetbrains.jewel.foundation.lazy.SelectionMode
import org.jetbrains.jewel.foundation.lazy.items
import org.jetbrains.jewel.foundation.lazy.rememberSelectableLazyListState
import org.jetbrains.jewel.foundation.theme.JewelTheme
import org.jetbrains.jewel.ui.component.*
import org.jetbrains.plugins.template.ComposeTemplateBundle
import org.jetbrains.plugins.template.weatherApp.model.Location
import org.jetbrains.plugins.template.weatherApp.model.WeatherForecastData
import org.jetbrains.plugins.template.weatherApp.services.LocationsProvider
import org.jetbrains.plugins.template.weatherApp.services.MyLocationsViewModelApi
import org.jetbrains.plugins.template.weatherApp.services.SearchAutoCompletionItemProvider
import org.jetbrains.plugins.template.weatherApp.services.WeatherViewModelApi
import org.jetbrains.plugins.template.weatherApp.ui.components.SearchToolbarMenu
import org.jetbrains.plugins.template.weatherApp.ui.components.WeatherDetailsCard

@Composable
internal fun WeatherAppSample(
    myLocationViewModel: MyLocationsViewModelApi,
    weatherViewModelApi: WeatherViewModelApi,
    searchAutoCompletionItemProvided: LocationsProvider
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
                searchAutoCompletionItemProvided,
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
    val myLocations = myLocationsViewModelApi.myLocationsFlow.collectAsState(emptyList()).value

    // TODO Set selected item on initial showing

    Column(modifier) {
        GroupHeader("My Locations", modifier = Modifier.wrapContentHeight().fillMaxWidth())

        Spacer(modifier = Modifier.height(10.dp))

        val listState = rememberSelectableLazyListState()
        LaunchedEffect(myLocations) {
            listState
                .selectedKeys = myLocations
                .mapIndexedNotNull { index, item -> if (item.isSelected) index else null }
                .toSet()
        }

        SelectableLazyColumn(
            modifier = Modifier.fillMaxSize(),
            selectionMode = SelectionMode.Single,
            state = listState,
            onSelectedIndexesChange = { indices ->
                val selectedLocationIndex = indices.firstOrNull() ?: return@SelectableLazyColumn
                myLocationsViewModelApi.onLocationSelected(selectedLocationIndex)
            },
        ) {
            items(
                items = myLocations,
                key = { item -> item },
                contentType = { item -> item.location },
            ) { item ->

                ContentItemRow(
                    item = item.location, isSelected = item.isSelected, isActive = isActive
                )
            }
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
        Text(text = item.id, modifier = Modifier.weight(1f), overflow = TextOverflow.Ellipsis, maxLines = 1)
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
            confirmButtonText = "Add",
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
        ) {
            weatherViewModelApi.onReloadWeatherForecast()
        }
    }
}
