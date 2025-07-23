package org.jetbrains.plugins.template.toolWindow

import com.intellij.openapi.project.DumbAware
import com.intellij.openapi.project.Project
import com.intellij.openapi.wm.ToolWindow
import com.intellij.openapi.wm.ToolWindowFactory
import org.jetbrains.jewel.bridge.addComposeTab
import org.jetbrains.plugins.template.ui.ChatAppSample
import org.jetbrains.plugins.template.weatherApp.ui.WeatherAppSample

class MyToolWindowFactory : ToolWindowFactory, DumbAware {

    override fun createToolWindowContent(project: Project, toolWindow: ToolWindow) {
        toolWindow.addComposeTab("Weather App") { WeatherAppSample() }
        toolWindow.addComposeTab("Chat App") { ChatAppSample() }
    }

    override fun shouldBeAvailable(project: Project) = true
}
