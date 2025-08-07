package org.jetbrains.plugins.template.weatherApp.services

import com.intellij.openapi.Disposable
import kotlinx.coroutines.*
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
interface MyLocationsViewModelApi : Disposable {
    fun onAddLocation(locationToAdd: Location)

    fun onDeleteLocation(locationToDelete: Location)

    fun onLocationSelected(selectedLocationIndex: Int)

    val myLocationsFlow: Flow<List<SelectableLocation>>
}

/**
 * Interface representing a ViewModel for managing weather-related data
 * and user interactions.
 */
interface WeatherViewModelApi : Disposable {
    val weatherForecastUIState: Flow<WeatherForecastUIState>

    fun onLoadWeatherForecast(location: Location)

    fun onReloadWeatherForecast()
}

/**
 * Represents the state of a weather data fetching process.
 *
 * This class is sealed, meaning it can have a fixed set of subclasses
 * that represent each possible state of the process. It is designed to
 * handle and encapsulate the different phases or outcomes when fetching
 * weather information, such as loading, success, error, or empty state.
 *
 * Subclasses:
 * - `Loading`: Indicates that the weather data is currently being fetched.
 * - `Success`: Indicates that the weather data was successfully fetched and
 *   contains the loaded data of type `WeatherForecastData`.
 * - `Error`: Indicates a failure in fetching the weather data, carrying the
 *   error message and optional cause details.
 * - `Empty`: Indicates that no weather data is available, typically because
 *   no location has been selected.
 */
sealed class WeatherForecastUIState {
    data class Loading(val location: Location) : WeatherForecastUIState()
    data class Success(val weatherForecastData: WeatherForecastData) : WeatherForecastUIState()
    data class Error(val message: String, val location: Location, val cause: Throwable? = null) :
        WeatherForecastUIState()

    object Empty : WeatherForecastUIState() // When no location is selected

    fun getLocationOrNull(): Location? {
        return when (this) {
            Empty -> null
            is Error -> location
            is Loading -> location
            is Success -> weatherForecastData.location
        }
    }

    val isLoading: Boolean get() = this is Loading
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

    private var currentWeatherJob: Job? = null

    private val myLocations = MutableStateFlow(myInitialLocations)

    private val selectedLocationIndex = MutableStateFlow(myLocations.value.lastIndex)

    private val _weatherState = MutableStateFlow<WeatherForecastUIState>(WeatherForecastUIState.Empty)

    /**
     * A flow representing the current UI state of the weather forecast.
     *
     * This flow emits instances of [WeatherForecastUIState], which encapsulate information
     * about the state of weather data loading and processing. The emitted states can represent
     * scenarios such as the data being loaded, successfully fetched, an error occurring, or
     * the absence of data when no location is selected.
     *
     * Observers of this flow can react to these state changes to update the UI accordingly.
     */
    override val weatherForecastUIState: Flow<WeatherForecastUIState> = _weatherState.asStateFlow()

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

        if (_weatherState.value.getLocationOrNull() != locationToAdd) {
            onReloadWeatherForecast()
        }
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
        currentWeatherJob?.cancel()

        currentWeatherJob = viewModelScope.launch {
            _weatherState.value = WeatherForecastUIState.Loading(location)

            weatherService.loadWeatherForecastFor(location)
                .onSuccess { weatherData ->
                    _weatherState.value = WeatherForecastUIState.Success(weatherData)
                }.onFailure { error ->
                    if (error is CancellationException) {
                        throw error
                    }

                    _weatherState.value = errorStateFor(location, error)
                }
        }
    }

    /**
     * Cancels all coroutines running within the context of the ViewModel's scope.
     *
     * This method is used to release resources and stop ongoing tasks when the ViewModel
     * is no longer needed, ensuring proper cleanup of coroutine-based operations.
     */
    override fun dispose() {
        viewModelScope.cancel()
    }

    private fun errorStateFor(
        location: Location,
        error: Throwable
    ): WeatherForecastUIState.Error = WeatherForecastUIState.Error(
        "Failed to load weather forecast for ${location.label}",
        location,
        error
    )
}
