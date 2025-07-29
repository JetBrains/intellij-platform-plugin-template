package org.jetbrains.plugins.template.weatherApp.services

import com.intellij.openapi.components.Service
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import org.jetbrains.plugins.template.weatherApp.model.*
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.LocalTime
import kotlin.random.Random

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
        val currentTime = LocalDateTime.of(LocalDate.now(), getRandomTime())

        // Generate 7-day forecast data
        val dailyForecasts = generateDailyForecasts(currentTime)

        delay(100)

        return WeatherForecastData(
            location = location,
            dailyForecasts.first(),
            dailyForecasts = dailyForecasts.drop(1)
        )
    }

    /**
     * Generates mock daily forecasts for 7 days starting from the given date.
     */
    private fun generateDailyForecasts(startDate: LocalDateTime): List<DailyForecast> {
        val forecasts = mutableListOf<DailyForecast>()

        for (i in 0 until 8) {
            val forecastDate = startDate.plusDays(i.toLong())
            val temperature = (-10..40).random().toFloat()
            val windSpeed = (0..30).random().toFloat()
            val humidity = (30..90).random()
            val weatherType = WeatherType.random()
            val windDirection = WindDirection.random()

            forecasts.add(
                DailyForecast(
                    date = forecastDate,
                    temperature = temperature,
                    weatherType = weatherType,
                    humidity = humidity,
                    windSpeed = windSpeed,
                    windDirection = windDirection
                )
            )
        }

        return forecasts
    }

    private fun getRandomTime(): LocalTime {
        val hour = Random.nextInt(0, 24)
        val minute = Random.nextInt(0, 60)
        val second = Random.nextInt(0, 60)
        return LocalTime.of(hour, minute, second)
    }
}