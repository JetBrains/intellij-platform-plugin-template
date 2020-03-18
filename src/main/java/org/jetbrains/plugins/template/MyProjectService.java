package org.jetbrains.plugins.template;

import com.intellij.openapi.project.Project;
import org.jetbrains.annotations.NotNull;

public class MyProjectService {
    public MyProjectService(@NotNull Project project) {
        System.out.println("MyProjectService");
    }
}
