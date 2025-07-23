package org.jetbrains.plugins.template.weatherApp.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color.Companion.Transparent
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.intellij.openapi.components.service
import org.jetbrains.jewel.bridge.retrieveColorOrUnspecified
import org.jetbrains.jewel.foundation.lazy.SelectableLazyColumn
import org.jetbrains.jewel.foundation.lazy.SelectionMode
import org.jetbrains.jewel.foundation.lazy.items
import org.jetbrains.jewel.foundation.lazy.rememberSelectableLazyListState
import org.jetbrains.jewel.foundation.theme.JewelTheme
import org.jetbrains.jewel.ui.component.*
import org.jetbrains.plugins.template.weatherApp.model.Location
import org.jetbrains.plugins.template.weatherApp.model.WeatherForecastData
import org.jetbrains.plugins.template.weatherApp.services.*
import org.jetbrains.plugins.template.weatherApp.ui.components.SearchToolbarMenu
import org.jetbrains.plugins.template.weatherApp.ui.components.WeatherDetailsCard

@Composable
internal fun WeatherAppSample() {
    val viewModel: MyLocationsViewModel = service<MyLocationsViewModel>()
    val searchAutoCompletionItemProvided = service<LocationsProvider>()

    HorizontalSplitLayout(
        first = { LeftColumn(viewModel, modifier = Modifier.fillMaxSize()) },
        second = {
            RightColumn(
                viewModel,
                viewModel,
                searchAutoCompletionItemProvided,
                modifier = Modifier.fillMaxSize()
            )
        },
        modifier = Modifier.fillMaxSize(),
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

    Column(modifier.fillMaxSize().padding(8.dp)) {
        GroupHeader("My Locations", modifier = Modifier.padding(bottom = 8.dp))

        Spacer(modifier = Modifier.height(4.dp))

        val listState = rememberSelectableLazyListState()

        SelectableLazyColumn(
            modifier = modifier.fillMaxSize(),
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

    Column(modifier.fillMaxWidth().padding(8.dp)) {
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
                .align(Alignment.CenterHorizontally), // optional for positioning
            weatherForecastData
        ) {
            weatherViewModelApi.onReloadWeatherForecast()
        }
    }
}
