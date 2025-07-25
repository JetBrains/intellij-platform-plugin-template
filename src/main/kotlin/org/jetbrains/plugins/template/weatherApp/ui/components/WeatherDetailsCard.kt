package org.jetbrains.plugins.template.weatherApp.ui.components

import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import org.jetbrains.jewel.foundation.theme.JewelTheme
import org.jetbrains.jewel.ui.component.ActionButton
import org.jetbrains.jewel.ui.component.Icon
import org.jetbrains.jewel.ui.component.Text
import org.jetbrains.jewel.ui.icons.AllIconsKeys
import org.jetbrains.plugins.template.weatherApp.model.WeatherForecastData
import org.jetbrains.plugins.template.weatherApp.model.WeatherType
import org.jetbrains.plugins.template.weatherApp.ui.WeatherIcons
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter
import kotlin.random.Random

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
    onReloadWeatherData: () -> Unit
) {
    val isNightTime = isNightTime(weatherForecastData.currentTime)
    val cardColor = getCardColorByTemperature(weatherForecastData.temperature, isNightTime)
    val textColor = Color.White

    Box(
        modifier = modifier
            .fillMaxWidth()
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
                    text = "Time: ${formatDateTime(weatherForecastData.currentTime)}",
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
                    onClick = { onReloadWeatherData() },
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
                    key = WeatherIcons.cloudy,
//                    key = if (isNightTime) weatherForecastData.weatherType.nightIconKey else weatherForecastData.weatherType.dayIconKey,
                    contentDescription = weatherForecastData.weatherType.label,
                    hint = EmbeddedToInlineCssSvgTransformerHint
                )

                Spacer(modifier = Modifier.height(8.dp))

                // Temperature (emphasized)
                Text(
                    text = "${weatherForecastData.temperature.toInt()}°C",
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
                    text = "Wind: ${weatherForecastData.windSpeed.toInt()} km/h ${weatherForecastData.windDirection.label}",
                    color = textColor,
                    fontSize = 16.sp
                )

                // Humidity info
                Text(
                    text = "Humidity: ${weatherForecastData.humidity}%",
                    color = textColor,
                    fontSize = 16.sp
                )
            }
        }
    }
}

/**
 * Returns a color for the weather type indicator.
 */
fun getWeatherTypeColor(weatherType: WeatherType, baseColor: Color): Color {
    return when (weatherType) {
        WeatherType.CLEAR -> Color.Yellow.copy(alpha = 0.2f)
        WeatherType.CLOUDY -> Color.Gray.copy(alpha = 0.2f)
        WeatherType.PARTLY_CLOUDY -> Color.LightGray.copy(alpha = 0.2f)
        WeatherType.RAINY_AND_THUNDER,
        WeatherType.RAINY -> Color.Blue.copy(alpha = 0.2f)

        WeatherType.SNOWY -> Color.White.copy(alpha = 0.3f)
        WeatherType.TORNADO -> Color.DarkGray.copy(alpha = 0.2f)
        WeatherType.THUNDER -> Color.DarkGray.copy(alpha = 0.2f)
        WeatherType.FOG -> Color.LightGray.copy(alpha = 0.2f)
        WeatherType.MIST -> Color.LightGray.copy(alpha = 0.2f)
    }
}

/**
 * Determines if it's night time based on the current hour.
 * Night time is considered to be between 7 PM (19:00) and 6 AM (6:00).
 */
fun isNightTime(dateTime: LocalDateTime): Boolean {
    val hour = Random.nextInt(0, 24)
    return hour < 6 || hour >= 19
}

/**
 * Returns a color based on the temperature and whether it's night time.
 * - Cold temperatures: blue/purple
 * - Warm temperatures: red/pink
 * - Night time: darker shades
 */
fun getCardColorByTemperature(temperature: Float, isNightTime: Boolean): Color {
    return when {
        isNightTime -> Color(0xFF1A237E) // Dark blue for night
        temperature < 0 -> Color(0xFF3F51B5) // Cold: blue
        temperature < 10 -> Color(0xFF5E35B1) // Cool: purple
        temperature < 20 -> Color(0xFF039BE5) // Mild: light blue
        temperature < 30 -> Color(0xFFFF9800) // Warm: orange
        else -> Color(0xFFE91E63) // Hot: pink/red
    }
}

/**
 * Formats the date time to a readable string.
 */
fun formatDateTime(dateTime: LocalDateTime): String {
    val formatter = DateTimeFormatter.ofPattern("MMM dd, yyyy HH:mm")
    return dateTime.format(formatter)
}