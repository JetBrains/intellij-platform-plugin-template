package org.jetbrains.plugins.template.toolWindow

import androidx.compose.foundation.layout.*
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.intellij.openapi.components.service
import com.intellij.openapi.project.Project
import com.intellij.openapi.wm.ToolWindow
import com.intellij.openapi.wm.ToolWindowFactory
import com.intellij.ui.content.ContentFactory
import org.jetbrains.jewel.bridge.JewelComposePanel
import org.jetbrains.jewel.ui.component.DefaultButton
import org.jetbrains.jewel.ui.component.Text
import org.jetbrains.plugins.template.ComposeTemplateBundle
import org.jetbrains.plugins.template.services.MyProjectService

class MyToolWindowFactory : ToolWindowFactory {

    override fun createToolWindowContent(project: Project, toolWindow: ToolWindow) {
        val myToolWindow = MyToolWindow()
        val content = ContentFactory.getInstance().createContent(myToolWindow.getContent(), null, false)
        toolWindow.contentManager.addContent(content)
    }

    override fun shouldBeAvailable(project: Project) = true

    class MyToolWindow() {
        private val service = service<MyProjectService>()

        fun getContent() = JewelComposePanel {
            Column(Modifier.fillMaxWidth().padding(16.dp)) {
                var param by remember { mutableStateOf("?") }
                Text(ComposeTemplateBundle.message("randomLabel", param))
                Spacer(Modifier.height(8.dp))
                DefaultButton(onClick = {
                    param = service.getRandomNumber().toString()
                }) {
                    Text(ComposeTemplateBundle.message("shuffle"))
                }
            }
        }
    }
}
