package com.github.nebojsavuksic.intellijplatformcomposeplugintemplate.weatherApp.ui.components

import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import org.jetbrains.jewel.foundation.theme.JewelTheme
import org.jetbrains.jewel.ui.component.*
import org.jetbrains.jewel.ui.icons.AllIconsKeys
import com.github.nebojsavuksic.intellijplatformcomposeplugintemplate.ComposeTemplateBundle
import com.github.nebojsavuksic.intellijplatformcomposeplugintemplate.components.PulsingText
import com.github.nebojsavuksic.intellijplatformcomposeplugintemplate.weatherApp.WeatherAppColors
import com.github.nebojsavuksic.intellijplatformcomposeplugintemplate.weatherApp.model.DailyForecast
import com.github.nebojsavuksic.intellijplatformcomposeplugintemplate.weatherApp.model.Location
import com.github.nebojsavuksic.intellijplatformcomposeplugintemplate.weatherApp.model.WeatherForecastData
import com.github.nebojsavuksic.intellijplatformcomposeplugintemplate.weatherApp.ui.WeatherForecastUIState
import com.github.nebojsavuksic.intellijplatformcomposeplugintemplate.weatherApp.ui.WeatherIcons
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter
import java.time.format.TextStyle
import java.util.*


/**
 * Displays the weather details in a styled card format. This card presents various weather information
 * including the current time, temperature, city name, wind details, humidity, and a 7-day forecast.
 * The appearance and content dynamically change based on the given weather state.
 *
 * @param modifier Modifier to be applied to the card layout.
 * @param weatherForecastState The current state of the weather forecast, which dictates the displayed content
 * and appearance. It can represent loading, success, error, or empty states.
 * @param onReloadWeatherData Callback invoked to reload weather data when the refresh action is triggered. It
 * provides the location for which the weather data should be fetched.
 */
@Composable
fun WeatherDetailsCard(
    modifier: Modifier = Modifier,
    weatherForecastState: WeatherForecastUIState,
    onReloadWeatherData: (Location) -> Unit
) {

    val (cardColor, textColor) = when (weatherForecastState) {
        is WeatherForecastUIState.Success -> {
            val isNightTime = isNightTime(weatherForecastState.weatherForecastData.currentWeatherForecast.date)
            val color =
                getCardColorByTemperature(
                    weatherForecastState.weatherForecastData.currentWeatherForecast.temperature,
                    isNightTime
                )
            color to Color.White
        }

        is WeatherForecastUIState.Loading -> WeatherAppColors.mildWeatherColor to Color.White
        is WeatherForecastUIState.Error -> WeatherAppColors.hotWeatherColor to Color.White // Brown for errors
        is WeatherForecastUIState.Empty -> WeatherAppColors.coolWeatherColor to Color.White
    }

    VerticallyScrollableContainer(modifier = modifier.safeContentPadding()) {
        Box(
            modifier = Modifier
                .clip(RoundedCornerShape(16.dp))
                .background(cardColor)
                .padding(16.dp)
        ) {

            // Card content
            Column(
                modifier = Modifier
                    .fillMaxWidth()
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    // Current Time
                    TimeDisplay(weatherForecastState, textColor)

                    ActionButton(
                        modifier = Modifier
                            .clip(RoundedCornerShape(8.dp))
                            .background(Color.Transparent)
                            .padding(8.dp),
                        tooltip = { Text("Refresh weather data") },
                        onClick = {
                            weatherForecastState.getLocationOrNull()?.let { onReloadWeatherData(it) }
                        },
                    ) {
                        Icon(
                            key = AllIconsKeys.Actions.Refresh,
                            contentDescription = "Refresh",
                            tint = Color.White
                        )
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                // Temperature and weather type column (vertically aligned)
                Column(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {

                    WeatherIconDisplay(weatherForecastState)

                    Spacer(modifier = Modifier.height(8.dp))

                    TemperatureDisplay(weatherForecastState, textColor)

                    Spacer(modifier = Modifier.height(8.dp))

                    // City name
                    CityNameDisplay(weatherForecastState, textColor)
                }

                Spacer(modifier = Modifier.height(16.dp))

                // Wind and humidity info
                WeatherDetailsRow(Modifier.fillMaxWidth(), weatherForecastState, textColor)

                Spacer(modifier = Modifier.height(24.dp))

                // 7-day forecast section
                SevenDaysForecastWidget(
                    weatherForecastState,
                    textColor,
                    Modifier
                        .fillMaxWidth()
                        .wrapContentHeight()
                        .align(Alignment.CenterHorizontally)
                )
            }
        }
    }
}

@Composable
private fun SevenDaysForecastWidget(
    weatherForecastData: WeatherForecastData,
    modifier: Modifier,
    textColor: Color
) {
    if (weatherForecastData.dailyForecasts.isNotEmpty()) {
        Column(modifier) {
            Text(
                text = ComposeTemplateBundle.message("weather.app.7days.forecast.title.text"),
                color = textColor,
                fontSize = 18.sp,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.padding(bottom = 8.dp)
            )

            Spacer(modifier = Modifier.height(8.dp))

            val scrollState = rememberLazyListState()

            HorizontallyScrollableContainer(
                modifier = Modifier.fillMaxWidth().safeContentPadding(),
                scrollState = scrollState,
            ) {
                LazyRow(
                    state = scrollState,
                    modifier = Modifier
                        .fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                ) {
                    items(weatherForecastData.dailyForecasts) { forecast ->
                        DayForecastItem(
                            forecast = forecast,
                            currentDate = weatherForecastData.currentWeatherForecast.date,
                            textColor = textColor
                        )
                    }
                }
            }
        }
    }
}

/**
 * A composable function that displays a single day's forecast.
 *
 * @param forecast The forecast data for a single day
 * @param currentDate The current date for determining relative day names (Today, Tomorrow)
 * @param textColor The color of the text
 */
@Composable
private fun DayForecastItem(
    forecast: DailyForecast,
    currentDate: LocalDateTime,
    textColor: Color
) {
    val dayName = getDayName(forecast.date, currentDate)
    val date = formatDateTime(forecast.date, showYear = false, showTime = false)
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = Modifier
            .width(120.dp)
            .border(1.dp, textColor.copy(alpha = 0.3f), RoundedCornerShape(8.dp))
            .padding(8.dp)
    ) {
        // Day name
        Text(
            text = dayName,
            color = textColor,
            fontSize = 14.sp,
            fontWeight = FontWeight.Bold,
            textAlign = TextAlign.Center
        )

        Text(
            text = date,
            color = textColor,
            fontSize = 10.sp,
            fontWeight = FontWeight.Bold,
            textAlign = TextAlign.Center
        )

        Spacer(modifier = Modifier.height(8.dp))

        // Weather icon
        Icon(
            key = if (isNightTime(forecast.date)) forecast.weatherType.nightIconKey else forecast.weatherType.dayIconKey,
            contentDescription = forecast.weatherType.label,
            hint = EmbeddedToInlineCssSvgTransformerHint,
            modifier = Modifier.size(48.dp)
        )

        Spacer(modifier = Modifier.height(8.dp))

        // Temperature
        Text(
            text = ComposeTemplateBundle.message(
                "weather.app.temperature.text",
                forecast.temperature.toInt()
            ),
            color = textColor,
            fontSize = 16.sp,
            fontWeight = FontWeight.Bold
        )

        Spacer(modifier = Modifier.height(8.dp))

        // Humidity
        Text(
            text = ComposeTemplateBundle.message(
                "weather.app.humidity.text",
                forecast.humidity
            ),
            color = textColor,
            fontSize = 12.sp
        )

        Spacer(modifier = Modifier.height(8.dp))

        // Wind direction
        Text(
            text = ComposeTemplateBundle.message(
                "weather.app.wind.direction.text",
                forecast.windSpeed.toInt(),
                forecast.windDirection.label
            ),
            color = textColor,
            fontSize = 12.sp
        )
    }
}

/**
 * Time display component with loading state
 */
@Composable
private fun TimeDisplay(
    weatherState: WeatherForecastUIState,
    textColor: Color
) {
    val text = when (weatherState) {
        is WeatherForecastUIState.Success -> formatDateTime(weatherState.weatherForecastData.currentWeatherForecast.date)
        else -> "-"
    }.let { time -> ComposeTemplateBundle.message("weather.app.time.text", time) }

    PulsingText(
        text,
        weatherState.isLoading,
        color = textColor,
        fontSize = JewelTheme.defaultTextStyle.fontSize,
        fontWeight = FontWeight.Bold
    )
}

/**
 * Weather icon that shows spinning progress during loading
 */
@Composable
private fun WeatherIconDisplay(
    weatherState: WeatherForecastUIState,
    modifier: Modifier = Modifier
) {
    when (weatherState) {
        is WeatherForecastUIState.Loading -> {
            val infiniteTransition = rememberInfiniteTransition(label = "rotating_weather_icon")

            val rotation = infiniteTransition.animateFloat(
                initialValue = 0f,
                targetValue = 360f,
                animationSpec = infiniteRepeatable(
                    animation = tween(
                        durationMillis = 3000, // 3 seconds per rotation
                        easing = FastOutSlowInEasing
                    ),
                    repeatMode = RepeatMode.Reverse
                ),
                label = "icon_rotation"
            ).value

            Icon(
                key = WeatherIcons.dayClear,
                contentDescription = null,
                modifier = modifier.rotate(rotation),
                hint = EmbeddedToInlineCssSvgTransformerHint
            )
        }

        is WeatherForecastUIState.Success -> {
            val currentForecast = weatherState.weatherForecastData.currentWeatherForecast
            val isNightTime = isNightTime(currentForecast.date)

            Icon(
                key = if (isNightTime) {
                    currentForecast.weatherType.nightIconKey
                } else {
                    currentForecast.weatherType.dayIconKey
                },
                contentDescription = currentForecast.weatherType.label,
                hint = EmbeddedToInlineCssSvgTransformerHint,
                modifier = modifier
            )
        }

        is WeatherForecastUIState.Error -> {
            Icon(
                key = AllIconsKeys.General.Warning,
                contentDescription = "Weather data error",
                tint = Color.White.copy(alpha = 0.8f),
                modifier = modifier
            )
        }

        is WeatherForecastUIState.Empty -> {
            Icon(
                key = AllIconsKeys.Actions.Find,
                contentDescription = "No location selected",
                tint = Color.White.copy(alpha = 0.6f),
                modifier = modifier
            )
        }
    }
}

/**
 * Temperature display with loading animation
 */
@Composable
private fun TemperatureDisplay(
    weatherState: WeatherForecastUIState,
    textColor: Color
) {
    val temperatureText = when (weatherState) {
        is WeatherForecastUIState.Success -> ComposeTemplateBundle.message(
            "weather.app.temperature.text",
            weatherState.weatherForecastData.currentWeatherForecast.temperature.toInt()
        )

        is WeatherForecastUIState.Loading -> "--°"
        is WeatherForecastUIState.Error -> "N/A°"
        is WeatherForecastUIState.Empty -> "--°"
    }

    PulsingText(
        text = temperatureText,
        isLoading = weatherState.isLoading,
        color = textColor,
        fontSize = 32.sp,
        fontWeight = FontWeight.ExtraBold
    )
}

/**
 * City name display that shows "Loading..." during loading state
 */
@Composable
private fun CityNameDisplay(
    weatherState: WeatherForecastUIState,
    textColor: Color
) {
    val loadingText = when (weatherState) {
        is WeatherForecastUIState.Success -> weatherState.weatherForecastData.location.label
        is WeatherForecastUIState.Loading -> weatherState.location.label
        is WeatherForecastUIState.Error -> "weatherState.location.label} - Error"
        is WeatherForecastUIState.Empty -> "Select a location"
    }

    PulsingText(
        text = loadingText,
        isLoading = weatherState.isLoading,
        color = textColor,
        fontSize = 18.sp,
        fontWeight = FontWeight.Bold
    )
}

/**
 * Composable function to display a row of weather details including wind and humidity information.
 *
 * @param modifier A [Modifier] that can be used to customize the layout or add behavior to the composable.
 * @param weatherState The current state of the weather forecast, represented by [WeatherForecastUIState].
 *                     This determines the display of wind and humidity information based on state.
 * @param textColor The color to be applied to the text of the weather details.
 */
@Composable
private fun WeatherDetailsRow(
    modifier: Modifier,
    weatherState: WeatherForecastUIState,
    textColor: Color
) {
    Row(
        modifier = modifier,
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        // Wind info
        val windText = when (weatherState) {
            is WeatherForecastUIState.Success -> {
                val forecast = weatherState.weatherForecastData.currentWeatherForecast
                ComposeTemplateBundle.message(
                    "weather.app.wind.direction.text",
                    forecast.windSpeed.toInt(),
                    forecast.windDirection.label
                )
            }

            is WeatherForecastUIState.Loading -> "Wind: --"
            is WeatherForecastUIState.Error -> "Wind: N/A"
            is WeatherForecastUIState.Empty -> "Wind: --"
        }

        PulsingText(
            windText,
            weatherState.isLoading,
            color = textColor,
            fontSize = 18.sp
        )

        // Humidity info
        val humidityText = when (weatherState) {
            is WeatherForecastUIState.Success -> ComposeTemplateBundle.message(
                "weather.app.humidity.text",
                weatherState.weatherForecastData.currentWeatherForecast.humidity
            )

            is WeatherForecastUIState.Loading -> "Humidity: -- %"
            is WeatherForecastUIState.Error -> "Humidity: N/A"
            is WeatherForecastUIState.Empty -> "Humidity: -- %"
        }
        PulsingText(
            text = humidityText,
            weatherState.isLoading,
            color = textColor,
            fontSize = 18.sp
        )

    }
}

/**
 * Forecast section that shows skeleton during loading
 */
@Composable
private fun SevenDaysForecastWidget(
    weatherState: WeatherForecastUIState,
    textColor: Color,
    modifier: Modifier
) {
    when (weatherState) {
        is WeatherForecastUIState.Success -> {
            if (weatherState.weatherForecastData.dailyForecasts.isNotEmpty()) {
                SevenDaysForecastWidget(
                    weatherState.weatherForecastData,
                    modifier,
                    textColor
                )
            }
        }

        is WeatherForecastUIState.Loading -> LoadingForecastSkeleton(textColor)
        is WeatherForecastUIState.Error -> ErrorForecastMessage(textColor)
        is WeatherForecastUIState.Empty -> EmptyForecastMessage(textColor)
    }
}

/**
 * Loading skeleton for forecast section
 */
@Composable
private fun LoadingForecastSkeleton(textColor: Color) {
    Column {
        Text(
            text = ComposeTemplateBundle.message("weather.app.7days.forecast.title.text"),
            color = textColor.copy(alpha = 0.7f),
            fontSize = 18.sp,
            fontWeight = FontWeight.Bold,
            modifier = Modifier.padding(bottom = 8.dp)
        )

        val scrollState = rememberLazyListState()
        HorizontallyScrollableContainer(
            modifier = Modifier.fillMaxWidth().safeContentPadding(),
            scrollState = scrollState,
        ) {
            LazyRow(
                state = scrollState,
                modifier = Modifier
                    .fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
            ) {
                items(count = 7) { LoadingForecastItem(textColor) }
            }
        }
    }
}

@Composable
private fun LoadingForecastItem(textColor: Color) {
    val infiniteTransition = rememberInfiniteTransition()
    val alpha = infiniteTransition.animateFloat(
        initialValue = 0.3f,
        targetValue = 0.6f,
        animationSpec = infiniteRepeatable(
            animation = tween(1000),
            repeatMode = RepeatMode.Reverse
        )
    ).value

    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = Modifier
            .width(120.dp)
            .border(1.dp, textColor.copy(alpha = 0.3f), RoundedCornerShape(8.dp))
            .padding(8.dp)
    ) {
        Text("--", color = textColor.copy(alpha = alpha), fontSize = 14.sp)
        Text("", color = textColor.copy(alpha = alpha), fontSize = 14.sp)

        Spacer(modifier = Modifier.height(8.dp))

        Box(
            modifier = Modifier
                .size(48.dp)
                .background(textColor.copy(alpha = alpha), RoundedCornerShape(4.dp))
        )

        Spacer(modifier = Modifier.height(8.dp))

        Text("--°", color = textColor.copy(alpha = alpha), fontSize = 16.sp)
        Text("", color = textColor.copy(alpha = alpha), fontSize = 14.sp)
    }
}

@Composable
private fun ErrorForecastMessage(textColor: Color) {
    Text(
        text = "Forecast unavailable",
        color = textColor.copy(alpha = 0.7f),
        fontSize = 16.sp,
        textAlign = TextAlign.Center,
        modifier = Modifier.fillMaxWidth()
    )
}

@Composable
private fun EmptyForecastMessage(textColor: Color) {
    Text(
        text = "Select a location to view forecast",
        color = textColor.copy(alpha = 0.7f),
        fontSize = 16.sp,
        textAlign = TextAlign.Center,
        modifier = Modifier.fillMaxWidth()
    )
}

/**
 * Returns the day name for a given date relative to the current date.
 * Returns "Today" for the current date, "Tomorrow" for the next day,
 * and the day of week plus date for other days.
 */
private fun getDayName(date: LocalDateTime, currentDate: LocalDateTime): String {
    val daysDifference = date.toLocalDate().toEpochDay() - currentDate.toLocalDate().toEpochDay()

    return when (daysDifference) {
        0L -> "Today"
        1L -> "Tomorrow"
        else -> {
            val dayOfWeek = date.dayOfWeek.getDisplayName(TextStyle.SHORT, Locale.getDefault())
            date.dayOfMonth
            "$dayOfWeek"
        }
    }
}

/**
 * Determines if it's night time based on the current hour.
 * Night time is considered to be between 7 PM (19:00) and 6 AM (6:00).
 */
fun isNightTime(dateTime: LocalDateTime): Boolean {
    val hour = dateTime.hour
    return hour !in 6..<19
}

/**
 * Returns a color based on the temperature and whether it's night time.
 * - Cold temperatures: blue/purple
 * - Warm temperatures: red/pink
 * - Night time: darker shades
 */
fun getCardColorByTemperature(temperature: Float, isNightTime: Boolean): Color {
    return when {
        isNightTime -> WeatherAppColors.nightWeatherColor
        temperature < 0 -> WeatherAppColors.coldWeatherColor
        temperature < 10 -> WeatherAppColors.coolWeatherColor
        temperature < 20 -> WeatherAppColors.mildWeatherColor
        temperature < 30 -> WeatherAppColors.warmWeatherColor
        else -> WeatherAppColors.hotWeatherColor
    }
}

/**
 * Formats the date time to a readable string.
 */
fun formatDateTime(
    dateTime: LocalDateTime,
    showYear: Boolean = true,
    showTime: Boolean = true
): String {
    val dateFormattingPattern = buildString {
        append("dd MMM")
        if (showYear) append(" yyyy")
        if (showTime) append(", HH:mm")
    }

    val formatter = DateTimeFormatter.ofPattern(dateFormattingPattern)
    return dateTime.format(formatter)
}