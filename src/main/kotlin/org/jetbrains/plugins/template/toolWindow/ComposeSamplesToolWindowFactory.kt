package org.jetbrains.plugins.template.toolWindow

import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.remember
import com.intellij.openapi.components.service
import com.intellij.openapi.project.DumbAware
import com.intellij.openapi.project.Project
import com.intellij.openapi.wm.ToolWindow
import com.intellij.openapi.wm.ToolWindowFactory
import kotlinx.coroutines.Dispatchers
import org.jetbrains.jewel.bridge.addComposeTab
import org.jetbrains.plugins.template.CoroutineScopeHolder
import org.jetbrains.plugins.template.ui.ChatAppSample
import org.jetbrains.plugins.template.weatherApp.model.Location
import org.jetbrains.plugins.template.weatherApp.services.LocationsProvider
import org.jetbrains.plugins.template.weatherApp.ui.WeatherAppViewModel
import org.jetbrains.plugins.template.weatherApp.services.WeatherForecastService
import org.jetbrains.plugins.template.weatherApp.ui.WeatherAppSample

class ComposeSamplesToolWindowFactory : ToolWindowFactory, DumbAware {

    override fun createToolWindowContent(project: Project, toolWindow: ToolWindow) {
        val coroutineScopeHolder = project.service<CoroutineScopeHolder>()

        toolWindow.addComposeTab("Weather App") {
            val locationProviderApi = remember { service<LocationsProvider>() }
            val viewModel = remember {
                val weatherForecastServiceApi = WeatherForecastService(Dispatchers.IO)
                WeatherAppViewModel(
                    listOf(Location("Munich", "Germany")),
                    coroutineScopeHolder
                        .createScope(WeatherAppViewModel::class.java.simpleName),
                    weatherForecastServiceApi
                )
            }

            DisposableEffect(Unit) {
                viewModel.onReloadWeatherForecast()

                onDispose { viewModel.dispose() }
            }

            WeatherAppSample(
                viewModel,
                viewModel,
                locationProviderApi
            )
        }

        toolWindow.addComposeTab("Chat App") { ChatAppSample() }
    }

    override fun shouldBeAvailable(project: Project) = true
}
