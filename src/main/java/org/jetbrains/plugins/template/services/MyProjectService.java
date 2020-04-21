package org.jetbrains.plugins.template.services;

import com.intellij.openapi.project.Project;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.plugins.template.TemplateBundle;

public class MyProjectService {
    public MyProjectService(@NotNull Project project) {
        System.out.println(TemplateBundle.message("projectService", project.getName()));
    }
}
