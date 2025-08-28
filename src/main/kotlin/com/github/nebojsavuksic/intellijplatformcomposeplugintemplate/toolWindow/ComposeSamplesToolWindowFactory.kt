package com.github.nebojsavuksic.intellijplatformcomposeplugintemplate.toolWindow

import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.remember
import com.intellij.openapi.components.service
import com.intellij.openapi.project.DumbAware
import com.intellij.openapi.project.Project
import com.intellij.openapi.wm.ToolWindow
import com.intellij.openapi.wm.ToolWindowFactory
import kotlinx.coroutines.Dispatchers
import org.jetbrains.jewel.bridge.addComposeTab
import com.github.nebojsavuksic.intellijplatformcomposeplugintemplate.CoroutineScopeHolder
import com.github.nebojsavuksic.intellijplatformcomposeplugintemplate.ui.ChatAppSample
import com.github.nebojsavuksic.intellijplatformcomposeplugintemplate.weatherApp.model.Location
import com.github.nebojsavuksic.intellijplatformcomposeplugintemplate.weatherApp.services.LocationsProvider
import com.github.nebojsavuksic.intellijplatformcomposeplugintemplate.weatherApp.ui.WeatherAppViewModel
import com.github.nebojsavuksic.intellijplatformcomposeplugintemplate.weatherApp.services.WeatherForecastService
import com.github.nebojsavuksic.intellijplatformcomposeplugintemplate.weatherApp.ui.WeatherAppSample

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
