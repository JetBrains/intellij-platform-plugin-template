package org.jetbrains.plugins.template.services

import com.intellij.openapi.project.Project
import org.jetbrains.plugins.template.TemplateBundle

class MyProjectService(project: Project) {

    init {
        println(TemplateBundle.message("projectService", project.name))
    }
}
