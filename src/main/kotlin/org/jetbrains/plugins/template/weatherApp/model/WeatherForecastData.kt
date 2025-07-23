package org.jetbrains.plugins.template.weatherApp.model

import java.time.LocalDateTime

/**
 * Data class representing weather information to be displayed in the Weather Card.
 */
data class WeatherForecastData(
    val cityName: String,
    val temperature: Float,
    val currentTime: LocalDateTime,
    val windSpeed: Float,
    val windDirection: WindDirection,
    val humidity: Int, // Percentage
    val weatherType: WeatherType
) {
    companion object Companion {
        val EMPTY: WeatherForecastData = WeatherForecastData(
            "",
            0f,
            LocalDateTime.now(),
            0f,
            WindDirection.NORTH,
            0,
            WeatherType.SUNNY
        )
    }
}

/**
 * Enum representing different weather types.
 */
enum class WeatherType(val label: String) {
    SUNNY("Sunny"),
    CLOUDY("Cloudy"),
    PARTLY_CLOUDY("Partly Cloudy"),
    RAINY("Rainy"),
    SNOWY("Snowy"),
    STORMY("Stormy");

    companion object {
        fun random(): WeatherType = entries.toTypedArray().random()
    }
}

/**
 * Enum representing wind directions.
 */
enum class WindDirection(val label: String) {
    NORTH("N"),
    NORTH_EAST("NE"),
    EAST("E"),
    SOUTH_EAST("SE"),
    SOUTH("S"),
    SOUTH_WEST("SW"),
    WEST("W"),
    NORTH_WEST("NW");

    companion object {
        fun random(): WindDirection = entries.toTypedArray().random()
    }
}