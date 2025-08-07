package org.jetbrains.plugins.template.weatherApp.services

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.withContext
import kotlinx.coroutines.yield
import org.jetbrains.plugins.template.weatherApp.model.*
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.LocalTime
import kotlin.coroutines.CoroutineContext
import kotlin.random.Random


interface WeatherForecastServiceApi {
    /**
     * Suspending function that returns Result<WeatherForecastData>.
     * This allows callers to handle success/failure explicitly.
     *
     * @param location The location to get weather data for
     * @return Result containing WeatherForecastData on success or exception on failure
     */
    suspend fun loadWeatherForecastFor(location: Location): Result<WeatherForecastData>
}

class WeatherForecastService(
    private val networkCoroutineContext: CoroutineContext = Dispatchers.IO,
) : WeatherForecastServiceApi {

    /**
     * Function that returns a weather forecast for provided [location] param.
     *
     * @param location The location to get weather data for
     * @return Result containing WeatherForecastData on success or exception on failure
     */
    override suspend fun loadWeatherForecastFor(location: Location): Result<WeatherForecastData> {
        return withContext(networkCoroutineContext) {
            runCatching { getWeatherData(location) }
        }
    }

    /**
     * Provides mock weather data for demonstration purposes.
     * In a real application, this would fetch data from a weather API.
     */
    private suspend fun getWeatherData(location: Location): WeatherForecastData {
        val currentTime = LocalDateTime.of(LocalDate.now(), getRandomTime())

        yield() // Check cancellation

        // Generate 7-day forecast data
        val dailyForecasts = generateDailyForecasts(currentTime)

        // Simulates a network request and stops the execution in case the coroutine
        // that launched the getWeatherData task is canceled
        delay(3000)

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