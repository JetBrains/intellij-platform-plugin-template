package org.jetbrains.plugins.template.weatherApp.model

import org.jetbrains.jewel.ui.icon.IconKey
import org.jetbrains.plugins.template.weatherApp.ui.WeatherIcons
import java.time.LocalDateTime

/**
 * Data class representing a daily weather forecast.
 */
data class DailyForecast(
    val date: LocalDateTime,
    val temperature: Float,
    val weatherType: WeatherType,
    val humidity: Int,
    val windSpeed: Float,
    val windDirection: WindDirection
)

/**
 * Data class representing weather information to be displayed in the Weather Card.
 */
data class WeatherForecastData(
    val location: Location,
    val currentWeatherForecast: DailyForecast,
    val dailyForecasts: List<DailyForecast> = emptyList()
) {
    companion object Companion {
        val EMPTY: WeatherForecastData = WeatherForecastData(
            Location("", ""),
            DailyForecast(
                LocalDateTime.now(),
                0f,
                WeatherType.CLEAR,
                0,
                0f,
                WindDirection.NORTH,
            ),
            emptyList()
        )
    }
}

/**
 * Enum representing different weather types.
 */
enum class WeatherType(val label: String, val dayIconKey: IconKey, val nightIconKey: IconKey) {
    CLEAR("Sunny", WeatherIcons.dayClear, WeatherIcons.nightHalfMoonClear),
    CLOUDY("Cloudy", dayIconKey = WeatherIcons.cloudy, nightIconKey = WeatherIcons.cloudy),
    PARTLY_CLOUDY(
        "Partly Cloudy",
        dayIconKey = WeatherIcons.dayPartialCloud,
        nightIconKey = WeatherIcons.nightHalfMoonPartialCloud
    ),
    RAINY("Rainy", dayIconKey = WeatherIcons.dayRain, nightIconKey = WeatherIcons.nightHalfMoonRain),
    RAINY_AND_THUNDER(
        "Rainy and Thunder",
        dayIconKey = WeatherIcons.dayRainThunder,
        nightIconKey = WeatherIcons.nightHalfMoonRainThunder
    ),
    THUNDER("Thunder", dayIconKey = WeatherIcons.thunder, nightIconKey = WeatherIcons.thunder),

    SNOWY("Snowy", dayIconKey = WeatherIcons.daySnow, nightIconKey = WeatherIcons.nightHalfMoonSnow),
    TORNADO("Stormy", dayIconKey = WeatherIcons.tornado, nightIconKey = WeatherIcons.tornado),
    FOG("Fog", dayIconKey = WeatherIcons.fog, nightIconKey = WeatherIcons.fog),
    MIST("Mist", dayIconKey = WeatherIcons.mist, nightIconKey = WeatherIcons.mist);

    companion object {
        fun random(): WeatherType = entries.toTypedArray().random()
    }
}

/**
 * Enum representing wind directions.
 */
enum class WindDirection(val label: String) {
    NORTH("↑"),
    NORTH_EAST("↗"),
    EAST("→"),
    SOUTH_EAST("↘"),
    SOUTH("↓"),
    SOUTH_WEST("↙"),
    WEST("←"),
    NORTH_WEST("↖");

    companion object {
        fun random(): WindDirection = entries.toTypedArray().random()
    }
}