package org.jetbrains.plugins.template.services

import com.intellij.openapi.components.Service

@Service
class MyProjectService() {
    fun getRandomNumber() = (1..100).random()
}
