package org.jetbrains.plugins.template.services;

import com.intellij.openapi.components.Service;
import org.jetbrains.annotations.NotNull;

@Service(Service.Level.PROJECT)
public final class JavaService {

    public JavaService() {
        System.out.println("FOO1");

        getRandomNumber(null);
    }

    public String getRandomNumber(@NotNull String text) {
        return text.replaceAll("[^0-9]", ""); // set a breakpoint here and debug the `runIde` task
    }
}
