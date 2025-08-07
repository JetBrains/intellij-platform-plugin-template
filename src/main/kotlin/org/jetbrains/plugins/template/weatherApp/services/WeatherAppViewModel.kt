package org.jetbrains.plugins.template.weatherApp.services

import com.intellij.openapi.application.EDT
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.cancel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
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
interface MyLocationsViewModelApi {
    fun onAddLocation(locationToAdd: Location)

    fun onDeleteLocation(locationToDelete: Location)

    fun onLocationSelected(selectedLocationIndex: Int)

    val myLocationsFlow: Flow<List<SelectableLocation>>
}

/**
 * Interface representing a ViewModel for managing weather-related data
 * and user interactions.
 */
interface WeatherViewModelApi {
    val weatherForecast: Flow<WeatherForecastData>

    fun onLoadWeatherForecast(location: Location)

    fun onReloadWeatherForecast()
}

/**
 * A ViewModel responsible for managing the user's locations and corresponding weather data.
 *
 * This class coordinates the interaction between the UI, locations, and weather data. It provides
 * functionality to add, delete, select locations, and reload weather forecasts. Additionally, it
 * supplies observable state flows for the list of selectable locations and the currently selected
 * location's weather forecast.
 *
 * @property myInitialLocations The initial list of user-defined locations.
 * @property viewModelScope The coroutine scope in which this ViewModel operates.
 * @property weatherService The service responsible for fetching weather forecasts for given locations.
 */
class WeatherAppViewModel(
    myInitialLocations: List<Location>,
    private val viewModelScope: CoroutineScope,
    private val weatherService: WeatherForecastServiceApi,
) : MyLocationsViewModelApi, WeatherViewModelApi {

    private val myLocations = MutableStateFlow(myInitialLocations)

    private val selectedLocationIndex = MutableStateFlow(myLocations.value.lastIndex)

    private val _weatherForecast = MutableStateFlow(WeatherForecastData.EMPTY)

    /**
     * A stream of weather forecast data that emits updates whenever the forecast changes.
     *
     * This property exposes a Flow of [WeatherForecastData], which allows consumers to observe
     * the weather forecast information for a selected location.
     */
    override val weatherForecast: Flow<WeatherForecastData> = _weatherForecast.asStateFlow()

    /**
     * A [StateFlow] that emits a list of [SelectableLocation] objects representing the user's
     * current locations along with the selection state of each location.
     */
    override val myLocationsFlow: StateFlow<List<SelectableLocation>> = myLocations
        .combine(selectedLocationIndex) { locations, selectedIndex ->
            locations.mapIndexed { index, location ->
                SelectableLocation(location, index == selectedIndex)
            }
        }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(), emptyList())

    override fun onAddLocation(locationToAdd: Location) {
        if (myLocations.value.contains(locationToAdd)) {
            selectedLocationIndex.value = myLocations.value.indexOf(locationToAdd)
        } else {
            myLocations.value += locationToAdd
            selectedLocationIndex.value = myLocations.value.lastIndex
        }

        onReloadWeatherForecast()
    }

    override fun onDeleteLocation(locationToDelete: Location) {
        myLocations.value -= locationToDelete

        val itemIndex = myLocations.value.indexOf(locationToDelete)
        val currentSelectedIndex = selectedLocationIndex.value
        if (itemIndex in 0..currentSelectedIndex) {
            selectedLocationIndex.value = (currentSelectedIndex - 1).coerceAtLeast(0)
        }

        onReloadWeatherForecast()
    }

    override fun onLocationSelected(selectedLocationIndex: Int) {
        if (this.selectedLocationIndex.value == selectedLocationIndex) return

        this.selectedLocationIndex.value = selectedLocationIndex

        onReloadWeatherForecast()
    }

    override fun onReloadWeatherForecast() {
        myLocations.value.getOrNull(selectedLocationIndex.value)?.let { location ->
            onLoadWeatherForecast(location)
        }
    }

    override fun onLoadWeatherForecast(location: Location) {
        viewModelScope.launch {
            val weatherForecastData = weatherService.loadWeatherForecastFor(location).getOrNull() ?: return@launch

            _weatherForecast.value = weatherForecastData
        }
    }

    /**
     * Cancels all coroutines running within the context of the ViewModel's scope.
     *
     * This method is used to release resources and stop ongoing tasks when the ViewModel
     * is no longer needed, ensuring proper cleanup of coroutine-based operations.
     */
    fun cancel() {
        viewModelScope.cancel()
    }
}
