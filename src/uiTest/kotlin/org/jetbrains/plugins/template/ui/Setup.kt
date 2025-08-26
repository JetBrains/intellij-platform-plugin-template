package org.jetbrains.plugins.template.ui

import com.intellij.ide.starter.buildTool.GradleBuildTool
import com.intellij.ide.starter.community.model.BuildType
import com.intellij.ide.starter.di.di
import com.intellij.ide.starter.ide.IDETestContext
import com.intellij.ide.starter.ide.IdeProductProvider
import com.intellij.ide.starter.models.TestCase
import com.intellij.ide.starter.path.GlobalPaths
import com.intellij.ide.starter.plugins.PluginConfigurator
import com.intellij.ide.starter.project.NoProject
import com.intellij.ide.starter.runner.Starter
import com.intellij.ide.starter.utils.Git
import com.intellij.openapi.util.SystemInfo
import org.kodein.di.DI
import org.kodein.di.bindSingleton
import java.nio.file.Paths

/**
 * Custom GlobalPaths implementation that points to the project's build directory.
 * This ensures all test artifacts are stored within the project structure.
 */
class TemplatePaths : GlobalPaths(Git.getRepoRoot().resolve("build"))

/**
 * Setup class for configuring IntelliJ IDE UI tests using the IntelliJ IDE Starter framework.
 *
 * This class provides the necessary configuration to:
 * - Initialize the test environment with proper paths and dependencies
 * - Install the plugin under test into the IDE instance
 * - Configure VM options for optimal test execution
 * - Apply OS-specific settings for compatibility
 *
 * @see [IntelliJ IDE Starter Documentation](https://github.com/JetBrains/intellij-ide-starter)
 */
class Setup {

    companion object {

        init {
            // Configure dependency injection to use our custom paths
            di = DI.Companion {
                extend(di)
                bindSingleton<GlobalPaths>(overrides = true) { TemplatePaths() }
            }
        }

        /**
         * Sets up the test context for UI testing with IntelliJ IDE Starter.
         *
         * This method:
         * 1. Creates a test case with the specified IDE version
         * 2. Configures the test context with the built plugin
         * 3. Applies VM options for proper IDE behavior during tests
         * 4. Applies OS-specific configurations
         *
         * @param hyphenateWithClass The name of the test class (used for test identification)
         * @return IDETestContext configured for running UI tests
         *
         * @throws IllegalStateException if required system properties are not set
         */
        fun setupTestContext(hyphenateWithClass: String): IDETestContext {

            // Create test case with the specified IDE version from gradle.properties
            val testCase = TestCase(
                IdeProductProvider.IC.copy(
                    buildNumber = System.getProperty("uiPlatformBuildVersion"),
                    buildType = BuildType.RELEASE.type
                ), NoProject
            )

            return Starter.newContext(testName = hyphenateWithClass, testCase = testCase).apply {
                // Install the plugin that was built by the buildPlugin task
                val pluginPath = System.getProperty("path.to.build.plugin")
                PluginConfigurator(this).installPluginFromPath(Paths.get(pluginPath))
                withBuildTool<GradleBuildTool>()
            }.applyVMOptionsPatch {

                // === Common system properties for all operating systems ===

                // Required JVM arguments for module access
                addSystemProperty("--add-opens", "java.base/java.lang=ALL-UNNAMED")
                addSystemProperty("--add-opens", "java.desktop/javax.swing=ALL-UNNAMED")

                // Core IDE configuration
                addSystemProperty("idea.trust.all.projects", true) // Trust all projects automatically
                addSystemProperty("jb.consents.confirmation.enabled", false) // Disable consent dialogs
                addSystemProperty("jb.privacy.policy.text", "<!--999.999-->") // Skip privacy policy
                addSystemProperty("ide.show.tips.on.startup.default.value", false) // No tips on startup

                // Test framework configuration
                addSystemProperty("junit.jupiter.extensions.autodetection.enabled", true)
                addSystemProperty("shared.indexes.download.auto.consent", true)

                // UI testing specific
                addSystemProperty("expose.ui.hierarchy.url", true) // Enable UI hierarchy inspection
                addSystemProperty("ide.experimental.ui", true) // Use new UI for testing

                // === OS-specific system properties ===

                when {
                    SystemInfo.isMac -> {
                        // macOS specific settings
                        addSystemProperty("ide.mac.file.chooser.native", false) // Use Java file chooser
                        addSystemProperty("ide.mac.message.dialogs.as.sheets", false) // Use regular dialogs
                        addSystemProperty("jbScreenMenuBar.enabled", false) // Disable native menu bar
                        addSystemProperty("ide.native.launcher", true) // Use native launcher
                    }

                    SystemInfo.isWindows -> {
                        // Windows specific settings
                        
                    }

                    SystemInfo.isLinux -> {
                        // Linux specific settings
                        addSystemProperty("ide.browser.jcef.enabled", true)
                        addSystemProperty("ide.native.launcher", false) // Avoid launcher issues on Linux

                        // X11/Wayland compatibility
                        addSystemProperty("sun.java2d.uiScale.enabled", false)
                        addSystemProperty("sun.java2d.xrender", false)
                    }
                }

            }.addProjectToTrustedLocations()
        }
    }
}