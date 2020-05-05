package org.jetbrains.plugins.template.listeners

import com.intellij.ide.plugins.DynamicPluginListener
import com.intellij.ide.plugins.IdeaPluginDescriptor
import com.intellij.openapi.components.ServiceManager
import org.jetbrains.plugins.template.services.MyApplicationService

internal class MyDynamicPluginListener : DynamicPluginListener {

    override fun beforePluginLoaded(pluginDescriptor: IdeaPluginDescriptor) {
        ServiceManager.getService(MyApplicationService::class.java)
    }

    override fun pluginLoaded(pluginDescriptor: IdeaPluginDescriptor) = Unit

    override fun beforePluginUnload(pluginDescriptor: IdeaPluginDescriptor, isUpdate: Boolean) = Unit

    override fun checkUnloadPlugin(pluginDescriptor: IdeaPluginDescriptor) = Unit

    override fun pluginUnloaded(pluginDescriptor: IdeaPluginDescriptor, isUpdate: Boolean) = Unit
}
