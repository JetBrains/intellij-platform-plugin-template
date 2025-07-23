package org.jetbrains.plugins.template.weatherApp.services

import com.intellij.openapi.components.Service
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import org.jetbrains.plugins.template.weatherApp.model.Location
import org.jetbrains.plugins.template.weatherApp.model.WeatherForecastData
import org.jetbrains.plugins.template.weatherApp.model.WeatherType
import org.jetbrains.plugins.template.weatherApp.model.WindDirection
import java.time.LocalDateTime

@Service
internal class WeatherForecastService(private val cs: CoroutineScope) {
    private val _weatherForecast: MutableStateFlow<WeatherForecastData> = MutableStateFlow(WeatherForecastData.EMPTY)

    val weatherForecast: StateFlow<WeatherForecastData> = _weatherForecast.asStateFlow()

    fun loadWeatherForecastFor(location: Location) {
        cs.launch(Dispatchers.IO) {
            // TODO Cache data
            emit(getWeatherData(location))
        }
    }

    private fun emit(weatherData: WeatherForecastData) {
        _weatherForecast.value = weatherData
    }

    /**
     * Provides mock weather data for demonstration purposes.
     * In a real application, this would fetch data from a weather API.
     */
    private suspend fun getWeatherData(location: Location): WeatherForecastData {
        val temperature = (-10..40).random().toFloat()
        val windSpeed = (0..30).random().toFloat()
        val humidity = (30..90).random()

        delay(100)

        return WeatherForecastData(
            cityName = location.name,
            temperature = temperature,
            currentTime = LocalDateTime.now(),
            windSpeed = windSpeed,
            windDirection = WindDirection.random(),
            humidity = humidity,
            weatherType = WeatherType.random()
        )
    }
}