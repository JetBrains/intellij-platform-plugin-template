package org.jetbrains.plugins.template.toolWindow

import androidx.compose.runtime.DisposableEffect
import com.intellij.openapi.components.service
import com.intellij.openapi.project.DumbAware
import com.intellij.openapi.project.Project
import com.intellij.openapi.wm.ToolWindow
import com.intellij.openapi.wm.ToolWindowFactory
import org.jetbrains.jewel.bridge.addComposeTab
import org.jetbrains.plugins.template.CoroutineScopeHolder
import org.jetbrains.plugins.template.ui.ChatAppSample
import org.jetbrains.plugins.template.weatherApp.model.Location
import org.jetbrains.plugins.template.weatherApp.services.LocationsProvider
import org.jetbrains.plugins.template.weatherApp.services.WeatherForecastService
import org.jetbrains.plugins.template.weatherApp.ui.WeatherAppSample
import org.jetbrains.plugins.template.weatherApp.ui.WeatherAppViewModel

class ComposeSamplesToolWindowFactory : ToolWindowFactory, DumbAware {
    override fun shouldBeAvailable(project: Project) = true

    override fun createToolWindowContent(project: Project, toolWindow: ToolWindow) {
        weatherApp(project, toolWindow)
        chatApp(project, toolWindow)
    }

    private fun weatherApp(project: Project, toolWindow: ToolWindow) {
        // create ViewModel once per tool window
        val viewModel = WeatherAppViewModel(
            listOf(Location("Munich", "Germany")),
            project.service<CoroutineScopeHolder>()
                .createScope(WeatherAppViewModel::class.java.simpleName),
            WeatherForecastService()
        )

        toolWindow.addComposeTab("Weather App") {
            DisposableEffect(Unit) {
                viewModel.onReloadWeatherForecast()

                onDispose { viewModel.dispose() }
            }

            WeatherAppSample(
                viewModel,
                viewModel,
                service<LocationsProvider>()
            )
        }
    }

    private fun chatApp(project: Project, toolWindow: ToolWindow) {
        toolWindow.addComposeTab("Chat App") {
            ChatAppSample()
        }
    }
}
