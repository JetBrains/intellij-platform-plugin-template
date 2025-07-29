package org.jetbrains.plugins.template.weatherApp.ui.components

import androidx.compose.foundation.ExperimentalFoundationApi
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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import org.jetbrains.jewel.foundation.theme.JewelTheme
import org.jetbrains.jewel.ui.component.ActionButton
import org.jetbrains.jewel.ui.component.HorizontallyScrollableContainer
import org.jetbrains.jewel.ui.component.Icon
import org.jetbrains.jewel.ui.component.Text
import org.jetbrains.jewel.ui.icons.AllIconsKeys
import org.jetbrains.plugins.template.ComposeTemplateBundle
import org.jetbrains.plugins.template.weatherApp.WeatherAppColors
import org.jetbrains.plugins.template.weatherApp.model.DailyForecast
import org.jetbrains.plugins.template.weatherApp.model.Location
import org.jetbrains.plugins.template.weatherApp.model.WeatherForecastData
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter
import java.time.format.TextStyle
import java.util.*

/**
 * A composable function that displays a weather card with Jewel theme.
 * The card displays city name, temperature, current time, wind information,
 * humidity, and a background icon representing the weather state.
 * The card and text color change based on temperature and time of day.
 *
 * @param weatherForecastData The weather data to display
 * @param modifier Additional modifier for the card
 */
@OptIn(ExperimentalFoundationApi::class)
@Composable
internal fun WeatherDetailsCard(
    modifier: Modifier = Modifier,
    weatherForecastData: WeatherForecastData,
    onReloadWeatherData: (Location) -> Unit
) {
    val currentWeatherForecast = weatherForecastData.currentWeatherForecast
    val isNightTime = isNightTime(currentWeatherForecast.date)
    val cardColor = getCardColorByTemperature(currentWeatherForecast.temperature, isNightTime)
    val textColor = Color.White

    Box(
        modifier = modifier
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
                Text(
                    text = "Time: ${formatDateTime(currentWeatherForecast.date)}",
                    color = textColor,
                    fontSize = JewelTheme.defaultTextStyle.fontSize,
                    fontWeight = FontWeight.Bold
                )

                ActionButton(
                    modifier = Modifier
                        .clip(RoundedCornerShape(8.dp))
                        .background(Color.Transparent)
                        .padding(8.dp),
                    tooltip = { Text("Refresh weather data") },
                    onClick = { onReloadWeatherData(weatherForecastData.location) },
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

                Icon(
                    key = when {
                        isNightTime -> currentWeatherForecast.weatherType.nightIconKey
                        else -> currentWeatherForecast.weatherType.dayIconKey
                    },
                    contentDescription = currentWeatherForecast.weatherType.label,
                    hint = EmbeddedToInlineCssSvgTransformerHint
                )

                Spacer(modifier = Modifier.height(8.dp))

                // Temperature (emphasized)
                Text(
                    text = ComposeTemplateBundle.message(
                        "weather.app.temperature.text",
                        currentWeatherForecast.temperature.toInt()
                    ),
                    color = textColor,
                    fontSize = 32.sp,
                    fontWeight = FontWeight.ExtraBold
                )

                Spacer(modifier = Modifier.height(8.dp))

                // City name
                Text(
                    text = weatherForecastData.location.label,
                    color = textColor,
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold
                )
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Wind and humidity info
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                // Wind info
                Text(
                    text = ComposeTemplateBundle.message(
                        "weather.app.wind.direction.text",
                        currentWeatherForecast.windSpeed.toInt(),
                        currentWeatherForecast.windDirection.label
                    ),
                    color = textColor,
                    fontSize = 18.sp,
                )

                // Humidity info
                Text(
                    text = ComposeTemplateBundle.message(
                        "weather.app.humidity.text",
                        currentWeatherForecast.humidity
                    ),
                    color = textColor,
                    fontSize = 18.sp,
                )
            }

            Spacer(modifier = Modifier.height(24.dp))

            // 7-day forecast section
            SevenDaysForecastWidget(
                weatherForecastData,
                Modifier
                    .fillMaxWidth()
                    .wrapContentHeight()
                    .align(Alignment.CenterHorizontally),
                textColor
            )
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
                modifier = Modifier.fillMaxWidth(),
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