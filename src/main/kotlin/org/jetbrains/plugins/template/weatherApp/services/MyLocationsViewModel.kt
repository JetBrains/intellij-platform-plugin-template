package org.jetbrains.plugins.template.weatherApp.services

import com.intellij.openapi.components.Service
import com.intellij.openapi.components.service
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.*
import org.jetbrains.plugins.template.weatherApp.model.Location
import org.jetbrains.plugins.template.weatherApp.model.SelectableLocation
import org.jetbrains.plugins.template.weatherApp.model.WeatherForecastData


/**
 * Defines the contract for managing and observing location-related data in the application.
 *
 * This interface provides methods for adding, deleting, and selecting locations, as well as
 * a flow to observe the list of selectable locations. Implementations are expected to handle
 * location-related logic and state management.
 */
internal interface MyLocationsViewModelApi {
    fun onAddLocation(locationToAdd: Location)

    fun onDeleteLocation(locationToDelete: Location)

    fun onLocationSelected(selectedLocationIndex: Int)

    val myLocationsFlow: Flow<List<SelectableLocation>>
}

/**
 * Interface representing a ViewModel for managing weather-related data
 * and user interactions.
 */
internal interface WeatherViewModelApi {
    val weatherForecast: Flow<WeatherForecastData>

    fun onLoadWeatherForecast(location: Location)

    fun onReloadWeatherForecast()
}

@Service
internal class MyLocationsViewModel(cs: CoroutineScope) : MyLocationsViewModelApi, WeatherViewModelApi {

    private val weatherService = service<WeatherForecastService>()

    private val myLocations = MutableStateFlow(listOf(Location("Munich", "Germany")))

    private val selectedLocationIndex = MutableStateFlow(myLocations.value.lastIndex)

    override val weatherForecast: Flow<WeatherForecastData> = weatherService.weatherForecast

    override val myLocationsFlow: StateFlow<List<SelectableLocation>> = myLocations
        .combine(selectedLocationIndex) { locations, selectedIndex ->
            locations.mapIndexed { index, location ->
                SelectableLocation(location, index == selectedIndex)
            }
        }.stateIn(cs, SharingStarted.WhileSubscribed(), emptyList())

    init {
        onReloadWeatherForecast()
    }

    override fun onAddLocation(locationToAdd: Location) {
        if (myLocations.value.contains(locationToAdd)) {
            selectedLocationIndex.value = myLocations.value.indexOf(locationToAdd)
        } else {
            myLocations.value += locationToAdd
            selectedLocationIndex.value = myLocations.value.lastIndex
        }
    }

    override fun onDeleteLocation(locationToDelete: Location) {
        myLocations.value -= locationToDelete

        val itemIndex = myLocations.value.indexOf(locationToDelete)
        val currentSelectedIndex = selectedLocationIndex.value
        if (itemIndex in 0..currentSelectedIndex) {
            selectedLocationIndex.value = (currentSelectedIndex - 1).coerceAtLeast(0)
        }
    }

    override fun onLocationSelected(selectedLocationIndex: Int) {
        this.selectedLocationIndex.value = selectedLocationIndex

        onReloadWeatherForecast()
    }

    override fun onReloadWeatherForecast() {
        myLocations.value.getOrNull(selectedLocationIndex.value)?.let { location ->
            onLoadWeatherForecast(location)
        }
    }

    override fun onLoadWeatherForecast(location: Location) {
        weatherService.loadWeatherForecastFor(location)
    }
}